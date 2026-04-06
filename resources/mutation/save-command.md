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
