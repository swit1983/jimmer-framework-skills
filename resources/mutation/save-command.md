# 保存指令 (Save Command)

Jimmer 的 `save` 指令是其核心特性，支持一次保存任意形状的数据结构，自动处理新增、修改、删除、关联等复杂场景。

## 基本概念

### save 与 insert/update 的区别

| 操作 | 说明 |
|------|------|
| `insert` | 强制新增 |
| `update` | 强制修改 |
| `save` | 智能判断：根据 ID 是否存在自动选择 insert 或 update，并级联处理关联对象 |

### 数据分类

save 指令根据 ID 判断对象状态：

| 状态 | ID 特征 | 处理方式 |
|------|---------|---------|
| **Transient** | 无 ID 或 ID 不在数据库中 | INSERT |
| **Detached** | 有 ID 且存在于数据库 | UPDATE |
| **Persistent** | 从数据库查询得到 | 根据配置决定 |

## 基本用法

### 保存单个对象

```java
// 新增（无 ID）
Book newBook = BookDraft.$.produce(draft -> {
    draft.setName("New Book");
    draft.setPrice(new BigDecimal("59.99"));
});
Book saved = sqlClient.save(newBook).getModifiedEntity();

// 修改（有 ID）
Book existingBook = BookDraft.$.produce(draft -> {
    draft.setId(1L);
    draft.setName("Updated Name");
});
Book saved = sqlClient.save(existingBook).getModifiedEntity();
```

### 保存关联对象

```java
// 保存书籍及其作者
Book book = BookDraft.$.produce(draft -> {
    draft.setName("GraphQL in Action");
    draft.setPrice(new BigDecimal("49.99"));
    
    // 关联书店（设置 ID 表示关联已存在的书店）
    draft.setStoreId(1L);
    
    // 关联作者（混合：既有新作者，也有已存在作者）
    draft.addIntoAuthors(author -> {
        // 新增作者
        author.setFirstName("Sam");
        author.setLastName("Smith");
    });
    draft.addIntoAuthors(author -> {
        // 关联已存在作者（只设置 ID）
        author.setId(1L);
    });
});

SaveResult<Book> result = sqlClient.save(book);
Book savedBook = result.getModifiedEntity();
```

## 保存模式 (SaveMode)

控制根对象的保存行为：

| 模式 | 说明 | 适用场景 |
|------|------|---------|
| `AUTO` (默认) | 自动判断 INSERT 或 UPDATE | 大多数场景 |
| `INSERT_ONLY` | 强制 INSERT | 确定是新增 |
| `UPDATE_ONLY` | 强制 UPDATE | 确定是修改 |
| `UPSERT` | INSERT 或 UPDATE（数据库原生） | 批量操作 |
| `INSERT_IF_ABSENT` | 不存在才 INSERT | 幂等插入 |

```java
// 强制 INSERT（ID 由数据库生成）
Book saved = sqlClient
    .saveCommand(newBook)
    .setMode(SaveMode.INSERT_ONLY)
    .execute()
    .getModifiedEntity();

// 仅当不存在时才插入
Book saved = sqlClient
    .saveCommand(book)
    .setMode(SaveMode.INSERT_IF_ABSENT)
    .execute()
    .getModifiedEntity();
```

## 关联保存模式

控制关联对象的保存行为：

### AssociatedSaveMode

| 模式 | 说明 |
|------|------|
| `AUTO` | 根据 ID 自动判断 |
| `APPEND` | 只建立关联，不修改关联对象 |
| `APPEND_IF_ABSENT` | 不存在时新增，存在时只建立关联 |
| `UPDATE` | 更新关联对象 |
| `MERGE` | 根据 ID 判断 INSERT 或 UPDATE |
| `REPLACE` | 删除旧关联，建立新关联 |

```java
// 只建立关联，不修改作者信息
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.APPEND)
    .execute();

// 替换作者（删除旧关联，建立新关联）
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.REPLACE)
    .execute();
```

## 脱钩操作 (Dissociate)

当关联被解除时，对关联对象的处理：

```java
// 脱钩时删除作者（级联删除）
sqlClient.saveCommand(book)
    .setDissociateAction(
        BookProps.AUTHORS, 
        DissociateAction.DELETE
    )
    .execute();

// 脱钩时将作者的外键设为 NULL
sqlClient.saveCommand(book)
    .setDissociateAction(
        BookProps.AUTHORS,
        DissociateAction.SET_NULL
    )
    .execute();
```

## 批量保存

```java
// 批量保存书籍
List<Book> books = Arrays.asList(book1, book2, book3);

BatchSaveResult<Book> result = sqlClient
    .saveCommand(books)
    .setMode(SaveMode.UPSERT)
    .execute();

List<Book> savedBooks = result.getModifiedEntities();
```

## 级联保存详解

级联保存是 `save` 指令的核心功能，自动处理嵌套对象的保存。

### 级联保存流程

```
保存 Book（根对象）
    ↓
识别 Book 的状态（Transient/Detached）
    ↓
保存根对象到数据库
    ↓
级联处理 Book.store（多对一）
    - 如果有 ID：检查是否存在，存在则关联，不存在则报错
    - 如果无 ID：根据 AssociatedSaveMode 处理
    ↓
级联处理 Book.authors（多对多）
    - 遍历所有 author
    - 根据 AssociatedSaveMode 决定 INSERT/UPDATE/仅关联
    - 更新中间表 BOOK_AUTHOR_MAPPING
    ↓
处理脱钩（Dissociate）
    - 对比原关联和新关联，找出被移除的
    - 根据 DissociateAction 处理脱钩对象
```

### 级联保存示例

**场景：** 保存书籍，同时关联书店和多个作者

```java
// 场景数据
// 1. 书籍 "GraphQL in Action" 是全新的（无 ID）
// 2. 书店 "O'REILLY" 已存在（ID=1）
// 3. 作者 "Sam Smith" 是全新的（无 ID）
// 4. 作者 "Eve Procello" 已存在（ID=2）

Book book = BookDraft.$.produce(draft -> {
    // 根对象：Transient（无 ID）
    draft.setName("GraphQL in Action");
    draft.setPrice(new BigDecimal("49.99"));
    
    // 多对一：关联已存在的书店（只设置 ID）
    draft.setStoreId(1L);  // 仅关联，不修改书店信息
    
    // 多对多：混合 Transient 和 Detached
    draft.addIntoAuthors(author -> {
        // Transient 作者（无 ID）
        author.setFirstName("Sam");
        author.setLastName("Smith");
    });
    draft.addIntoAuthors(author -> {
        // Detached 作者（有 ID）
        author.setId(2L);
    });
});

// 级联保存
SaveResult<Book> result = sqlClient
    .saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.MERGE)
    .execute();

// 执行流程分析：
// 1. INSERT INTO book (name, price, store_id) VALUES (...)
//    - Book 获得新 ID（假设为 100）
// 
// 2. 处理 authors[0] (Sam Smith):
//    - 无 ID → 根据 MERGE 模式 INSERT
//    - INSERT INTO author (first_name, last_name) VALUES ('Sam', 'Smith')
//    - Author 获得新 ID（假设为 50）
//    - INSERT INTO book_author_mapping (book_id, author_id) VALUES (100, 50)
// 
// 3. 处理 authors[1] (Eve Procello):
//    - 有 ID=2 → 根据 MERGE 模式 UPDATE
//    - UPDATE author SET ... WHERE id = 2
//    - INSERT INTO book_author_mapping (book_id, author_id) VALUES (100, 2)
```

### 脱钩（Dissociate）详解

脱钩是指关联被解除，但对象本身不一定被删除。

**场景：** 将作者从书籍中移除

```java
// 当前状态：书籍1关联作者1、作者2、作者3
// 目标状态：书籍1只关联作者1

Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);  // 已存在的书籍
    
    // 只保留作者1，移除作者2和作者3
    draft.addIntoAuthors(author -> author.setId(1L));
    // 注意：没有添加 author2 和 author3
});

// 不同脱钩策略的效果

// 策略1：SET_NULL - 将脱钩的外键设为 NULL
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.REPLACE)  // 替换模式
    .setDissociateAction(BookProps.AUTHORS, DissociateAction.SET_NULL)
    .execute();
// 结果：
// - 删除 BOOK_AUTHOR_MAPPING 中 (book_id=1, author_id=2)
// - 删除 BOOK_AUTHOR_MAPPING 中 (book_id=1, author_id=3)
// - Author 2 和 3 仍然存在，只是不再关联书籍1

// 策略2：DELETE - 级联删除脱钩对象
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.REPLACE)
    .setDissociateAction(BookProps.AUTHORS, DissociateAction.DELETE)
    .execute();
// 结果：
// - 删除 BOOK_AUTHOR_MAPPING 记录
// - DELETE FROM author WHERE id IN (2, 3)  -- 级联删除！

// 策略3：NONE - 不允许脱钩
sqlClient.saveCommand(book)
    .setDissociateAction(BookProps.AUTHORS, DissociateAction.NONE)
    .execute();
// 结果：
// - 报错：Dissociation is not allowed for property "authors"
```

### 中间表的脱钩

**场景：** 中间表有业务字段（如关联排序、角色等）

```java
@Entity
public interface BookAuthorMapping {
    @Id
    @ManyToOne
    @JoinColumn(name = "BOOK_ID")
    Book book();
    
    @Id
    @ManyToOne
    @JoinColumn(name = "AUTHOR_ID")
    Author author();
    
    // 中间表的业务字段
    int order();
    String role();
    
    // 中间表的逻辑删除标记
    @LogicalDeleted("true")
    boolean deleted();
}

// 脱钩时只标记中间表为删除，不删除 Author
Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);
    draft.addIntoAuthorMappings(mapping -> {
        mapping.setAuthorId(2L);
        mapping.setDeleted(true);  // 标记删除
    });
});

sqlClient.saveCommand(book)
    .setDissociateAction(BookProps.AUTHOR_MAPPINGS, DissociateAction.SET_NULL)
    .execute();
// 结果：
// - UPDATE book_author_mapping SET deleted = true 
//   WHERE book_id = 1 AND author_id = 2
// - Author 2 仍然存在
```

### 级联保存 + 脱钩组合策略

| 场景 | 关联保存模式 | 脱钩策略 | 效果 |
|------|-------------|---------|------|
| 添加新作者，保留现有作者 | `APPEND` | 不适用 | 只新增不删除 |
| 完全替换作者列表 | `REPLACE` | `DELETE` | 删除旧作者，新增新作者 |
| 保留部分作者，解除关联其他作者 | `REPLACE` | `SET_NULL` | 解除关联但保留作者 |
| 新增或更新作者，不处理未传递的作者 | `MERGE` | 不适用 | 只处理传递的作者 |

```java
// 实战：文章标签管理

// 场景1：添加新标签（保留现有）
public Article addTags(Long articleId, List<String> newTagNames) {
    Article article = ArticleDraft.$.produce(draft -> {
        draft.setId(articleId);
        for (String name : newTagNames) {
            draft.addIntoTags(tag -> tag.setName(name));
        }
    });
    
    return sqlClient.saveCommand(article)
        .setAssociatedMode(ArticleProps.TAGS, AssociatedSaveMode.APPEND_IF_ABSENT)
        .execute()
        .getModifiedEntity();
}

// 场景2：完全替换标签（删除旧标签）
public Article setTags(Long articleId, List<String> tagNames) {
    Article article = ArticleDraft.$.produce(draft -> {
        draft.setId(articleId);
        for (String name : tagNames) {
            draft.addIntoTags(tag -> tag.setName(name));
        }
    });
    
    return sqlClient.saveCommand(article)
        .setAssociatedMode(ArticleProps.TAGS, AssociatedSaveMode.REPLACE)
        .setDissociateAction(ArticleProps.TAGS, DissociateAction.SET_NULL)  // 保留标签，解除关联
        .execute()
        .getModifiedEntity();
}

// 场景3：级联删除脱钩的标签（清理孤儿标签）
public Article replaceTagsWithCleanup(Long articleId, List<String> tagNames) {
    Article article = ArticleDraft.$.produce(draft -> {
        draft.setId(articleId);
        for (String name : tagNames) {
            draft.addIntoTags(tag -> tag.setName(name));
        }
    });
    
    return sqlClient.saveCommand(article)
        .setAssociatedMode(ArticleProps.TAGS, AssociatedSaveMode.REPLACE)
        .setDissociateAction(ArticleProps.TAGS, DissociateAction.DELETE)  // 删除孤儿标签
        .execute()
        .getModifiedEntity();
}
```

## 完整示例

```java
public Book saveBookWithDetails(BookInput input) {
    Book book = BookDraft.$.produce(draft -> {
        draft.setId(input.getId());  // 有 ID 则更新，无则新增
        draft.setName(input.getName());
        draft.setPrice(input.getPrice());
        
        // 关联书店
        draft.setStoreId(input.getStoreId());
        
        // 关联作者
        for (AuthorInput authorInput : input.getAuthors()) {
            draft.addIntoAuthors(author -> {
                author.setId(authorInput.getId());
                author.setFirstName(authorInput.getFirstName());
                author.setLastName(authorInput.getLastName());
            });
        }
    });
    
    return sqlClient
        .saveCommand(book)
        .setMode(SaveMode.AUTO)
        .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.MERGE)
        .setDissociateAction(BookProps.AUTHORS, DissociateAction.SET_NULL)
        .execute()
        .getModifiedEntity();
}
```
