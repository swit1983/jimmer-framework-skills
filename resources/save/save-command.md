# 保存指令 (Save Command) API

## 概念

一句话：**一个方法调用保存任意形状的数据结构**。

无论被保存的数据结构是简单还是复杂，都可以使用一个函数调用存入数据库。Jimmer 自动对比差异，执行相应的 INSERT、UPDATE、DELETE。

> ⚠️ **重要警告**：保存指令的用法和绝大部分 ORM 都不相同，建议仔细阅读文档，不要按照自己对其他 ORM 的理解去做猜测性使用。

## 解决的问题

单表记录的保存从来不是应用开发的难点，直接使用 JDBC 也很容易实现。但保存**复杂嵌套的数据结构**却很不容易：

开发人员不得不：
1. 从数据库查询现有数据结构
2. 和即将保存的数据结构对比
3. 找出有变化的多个局部
4. 手动转化为相应的 INSERT、UPDATE 和 DELETE 语句

这个过程非常繁琐且容易出错。

Jimmer 从一开始就着眼于如何保存复杂的数据结构，而非如何保存一个孤单的实体对象。

## 核心原理

1. 用户传入任意形状的数据结构
2. Jimmer 根据传入数据的形状，从数据库查询出相同形状的现有数据
3. 自动对比新旧数据结构找出差异
4. 根据差异自动执行相应的 SQL 操作：
   - **橙色**：标量属性变化 → 执行 UPDATE
   - **蓝色**：关联变化 → 修改关联关系
   - **绿色**：新增实体 → 执行 INSERT 并建立关联
   - **红色**：移除实体 → 清除关联，可能删除数据

和传统 ORM 不同：
- 传统 ORM 需要在实体建模时描述哪些属性需要被保存
- Jimmer：实体具备动态性，只会保存被指定的属性，忽略未指定的属性，运行时通过数据结构自身描述期望的行为，具备绝对的灵活性

## 保存模式

### 按关联长度分

| 模式 | 说明 | 使用场景 |
|------|------|----------|
| **短关联保存** | 只改变关联关系，不修改关联对象本身 | 修改书籍的所属书店，只需要设置 `storeId`，不需要传递整个书店对象 |
| **长关联保存** | 不仅改变关联关系，还级联修改关联对象 | 表单中同时编辑书籍和其作者信息，一次性保存 |

### 关联覆盖模式

| 模式 | 说明 |
|------|------|
| **REPLACE (默认)** | 用被保存结构全量替换数据库中已有的数据结构 |
| **APPEND** | 追加模式，只添加新的不删除已有的 |

## 支持的数据形状

- 单个简单对象 ✅
- 单级关联 ✅
- 多级深度嵌套 ✅
- 树形结构递归保存 ✅
- 自引用递归结构 ✅

## 代码示例

- [保存示例](../../examples/mutation/save-example.java) - Java
- [保存示例](../../examples/mutation/save-example.kt) - Kotlin

## 保存模式 (SaveMode) - 根对象保存模式

控制**根对象**的保存行为：

| 模式 | 说明 | 适用场景 | 特点 |
|------|------|---------|------|
| `UPSERT` (默认) | **先判断存在性** - 存在则 UPDATE，不存在则 INSERT | 大多数场景 | 默认模式，智能判断 |
| `INSERT_ONLY` | **强制 INSERT** - 无条件执行 INSERT | 确定是新增数据 | ID 通常由数据库生成 |
| `UPDATE_ONLY` | **强制 UPDATE** - 无条件执行 UPDATE | 确定是修改数据 | 按 ID 或 Key 更新 |
| `INSERT_IF_ABSENT` | **不存在才 INSERT** - 存在则忽略 | 幂等插入、防止重复 | 按 ID 或 Key 判断 |
| `NON_IDEMPOTENT_UPSERT` | **非幂等 UPSERT** - 处理 Wild 对象 | Wild 对象（无 ID 无 Key） | 可能混合幂等/非幂等操作，**不推荐** |

> **⚠️ 重要提示**：
> - `UPSERT` 是 **默认模式**，不是 `AUTO`
> - `NON_IDEMPOTENT_UPSERT` 用于处理既无 ID 也无 Key 的 Wild 对象
> - 前 4 种模式要求对象必须有 ID 或 Key，否则报错

### 各模式详细行为

| 模式 | 对象有 ID | 对象无 ID 有 Key | 对象无 ID 无 Key (Wild) | 行为 |
|------|----------|-----------------|----------------------|------|
| `UPSERT` (默认) | 按 ID 查存在性，存在 UPDATE/不存在 INSERT | 按 Key 查存在性，存在 UPDATE/不存在 INSERT | **抛出异常** | 智能判断 |
| `INSERT_ONLY` | 忽略 ID，执行 INSERT | 执行 INSERT | 执行 INSERT | 强制新增 |
| `UPDATE_ONLY` | 按 ID 执行 UPDATE | 按 Key 执行 UPDATE | **抛出异常** | 强制更新 |
| `INSERT_IF_ABSENT` | 按 ID 查存在性，不存在则 INSERT | 按 Key 查存在性，不存在则 INSERT | **抛出异常** | 幂等插入 |
| `NON_IDEMPOTENT_UPSERT` | 同 UPSERT | 同 UPSERT | 同 INSERT_ONLY | 处理 Wild 对象 |

### 代码示例

```java
// UPSERT 模式（默认）- 根据 ID/Key 判断存在性
Book bookWithId = BookDraft.$.produce(draft -> {
    draft.setId(1L);  // 有 ID，查询存在性，存在则 UPDATE
    draft.setName("Updated Book");
});
Book saved = sqlClient.save(bookWithId);  // 默认 UPSERT 模式

Book bookWithKey = BookDraft.$.produce(draft -> {
    // 无 ID，但有 Key（name + edition）
    draft.setName("Learning GraphQL");
    draft.setEdition(3);
});
Book saved2 = sqlClient.save(bookWithKey);  // 按 Key 查询存在性

// INSERT_ONLY 模式 - 强制 INSERT（ID 由数据库生成）
Book forcedInsert = BookDraft.$.produce(draft -> {
    draft.setId(999L);  // 即使有 ID，也执行 INSERT
    draft.setName("Force Insert");
});
Book inserted = sqlClient
    .saveCommand(forcedInsert)
    .setMode(SaveMode.INSERT_ONLY)
    .execute()
    .getModifiedEntity();

// UPDATE_ONLY 模式 - 强制 UPDATE（按 ID 或 Key）
Book updateById = BookDraft.$.produce(draft -> {
    draft.setId(1L);  // 按 ID 更新
    draft.setName("Updated by ID");
});
Book updated = sqlClient
    .saveCommand(updateById)
    .setMode(SaveMode.UPDATE_ONLY)
    .execute()
    .getModifiedEntity();

Book updateByKey = BookDraft.$.produce(draft -> {
    // 无 ID，按 Key（name + edition）更新
    draft.setName("Learning GraphQL");
    draft.setEdition(3);
});
Book updated2 = sqlClient
    .saveCommand(updateByKey)
    .setMode(SaveMode.UPDATE_ONLY)
    .execute()
    .getModifiedEntity();

// INSERT_IF_ABSENT 模式 - 幂等插入（不存在才插入）
Book idempotentBook = BookDraft.$.produce(draft -> {
    draft.setName("Unique Book");
    draft.setEdition(1);
});
Book savedAbsent = sqlClient
    .saveCommand(idempotentBook)
    .setMode(SaveMode.INSERT_IF_ABSENT)
    .execute()
    .getModifiedEntity();

// NON_IDEMPOTENT_UPSERT 模式 - 处理 Wild 对象（不推荐）
Book wildBook = BookDraft.$.produce(draft -> {
    // 既无 ID 也无 Key
    draft.setPrice(new BigDecimal("49.9"));
});
Book savedWild = sqlClient
    .saveCommand(wildBook)
    .setMode(SaveMode.NON_IDEMPOTENT_UPSERT)
    .execute()
    .getModifiedEntity();
```

### 快捷方法与 SaveMode 等价关系

| 快捷方法 | 等价的 saveCommand 调用 | 说明 |
|---------|----------------------|------|
| `save(entity)` | `save(entity, SaveMode.UPSERT)` | 默认行为 |
| `insert(entity)` | `save(entity, SaveMode.INSERT_ONLY)` | 强制 INSERT |
| `update(entity)` | `save(entity, SaveMode.UPDATE_ONLY)` | 强制 UPDATE |
| `insertIfAbsent(entity)` | `save(entity, SaveMode.INSERT_IF_ABSENT)` | 不存在才 INSERT |
| `upsert(entity)` | `save(entity, SaveMode.UPSERT)` | 同 save |

## 关联保存模式

控制关联对象的保存行为：

### AssociatedSaveMode - 关联对象保存模式

| 模式 | 适用范围 | 接受Wild对象 | 描述 |
|------|---------|-------------|------|
| `APPEND` | 所有关联 | ✅ 是 | **无条件 INSERT** - 直接插入关联对象，不做任何存在性检查 |
| `APPEND_IF_ABSENT` | 所有关联 | ❌ 否 | **不存在才插入** - 先按id/key检查是否存在，存在则忽略，不存在则插入 |
| `UPDATE` | 所有关联 | ✅ 是 | **强制更新** - 按id或key更新关联对象，要求对象必须已存在 |
| `MERGE` | 所有关联 | ❌ 否 | **智能合并** - 先检查是否存在，存在则更新，不存在则插入 |
| `REPLACE` | 后置关联 | ❌ 否 | **替换模式** - 在MERGE基础上，对不再需要的关联进行脱钩操作 |
| `VIOLENTLY_REPLACE` | 后置关联 | ✅ 是 | **暴力替换** - 删除所有旧关联和对象，重新插入所有新对象 |

> **⚠️ 注意**：
> - `APPEND` 和 `UPDATE` 接受 **Wild对象**（无id无key的对象）
> - `APPEND_IF_ABSENT` / `MERGE` / `REPLACE` 要求对象必须有 **id 或 key**
> - `VIOLENTLY_REPLACE` 性能较低，且可能导致级联删除，**不推荐，请慎用**

#### 各模式详解

| 模式 | 行为流程 | 适用场景 |
|------|---------|---------|
| **APPEND** | 直接执行 `INSERT`，不做任何检查 | 确定是新增数据，或批量导入时 |
| **APPEND_IF_ABSENT** | 1. 按id/key查询是否存在<br>2. 存在 → 忽略<br>3. 不存在 → `INSERT` | 幂等插入，防止重复数据 |
| **UPDATE** | 1. 有id → 按id更新<br>2. 无id有key → 按key更新 | 确定是修改现有数据 |
| **MERGE** | 1. 按id/key查询是否存在<br>2. 存在 → `UPDATE`<br>3. 不存在 → `INSERT` | 不确定是新增还是修改（最常用） |
| **REPLACE** | 1. 执行 `MERGE`<br>2. 对旧结构中不再需要的数据执行脱钩 | 全量替换关联列表 |
| **VIOLENTLY_REPLACE** | 1. 删除所有旧关联和相关对象<br>2. 重新插入所有新对象 | 只传递了简单的关联信息，无id/key |

#### 代码示例

```java
// APPEND - 无条件插入（不关心是否已存在）
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.APPEND)
    .execute();

// APPEND_IF_ABSENT - 不存在才插入（幂等）
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.APPEND_IF_ABSENT)
    .execute();

// UPDATE - 强制更新（要求对象必须存在）
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.UPDATE)
    .execute();

// MERGE - 智能判断插入或更新（推荐）
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.MERGE)
    .execute();

// REPLACE - 替换关联列表（全量替换）
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.REPLACE)
    .execute();

// VIOLENTLY_REPLACE - 暴力替换（慎用）
sqlClient.saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.VIOLENTLY_REPLACE)
    .execute();

// 批量设置所有关联的模式
sqlClient.saveCommand(book)
    .setAssociatedModeAll(AssociatedSaveMode.MERGE)
    .execute();
```

#### 快捷方法的默认模式

| 方法 | 默认 AssociatedSaveMode |
|------|------------------------|
| `save()` / `saveEntities()` / `saveInputs()` | `REPLACE` |
| `insert()` / `insertEntities()` / `insertInputs()` | `APPEND` |
| `insertIfAbsent()` / `insertEntitiesIfAbsent()` / `insertInputsIfAbsent()` | `APPEND_IF_ABSENT` |
| `update()` / `updateEntities()` / `updateInputs()` | `UPDATE` |
| `merge()` / `mergeEntities()` / `mergeInputs()` | `MERGE` |

## 脱钩操作 (Dissociate) - 详解

### 什么是脱钩？

**脱钩（Dissociate）** 是指在保存数据时，新旧数据结构对比后，**存在于旧数据但不存在于新数据中的关联对象**需要被"解除关联"的操作。

**示例场景：**
```
数据库现有：  书籍A → 关联作者1、作者2、作者3
用户提交：   书籍A → 关联作者1（只保留作者1）
脱钩对象：   作者2、作者3 需要被脱钩
```

### 脱钩的两种关联类型

| 关联类型 | 说明 | 示例 | 脱钩行为 |
|---------|------|------|---------|
| **中间表关联** | 多对多关联，通过中间表连接 | `Book.authors` ↔ `Author.books` | 删除中间表记录，关联对象本身不受影响 |
| **子表关联** | 一对多/多对一，通过外键关联 | `BookStore.books`（子表指向父表的外键） | 需要配置脱钩策略，行为取决于配置 |

### 五种脱钩模式详解

| 模式 | 行为描述 | 适用场景 | 注意事项 |
|------|---------|---------|---------|
| **`NONE`** (默认) | 行为取决于全局配置 `jimmer.default-dissociate-action-checking` | 默认情况 | 若配置为 `true` 或有真实外键约束，实际表现为 `CHECK`；否则表现为 `LAX` |
| **`LAX`** | 脱钩时不执行任何操作 | 不关心孤儿数据 | 与 `REPLACE` 模式冲突时会被忽略 |
| **`CHECK`** | 如果存在需要脱钩的对象，抛出异常阻止操作 | 禁止意外脱钩 | 用于保护数据完整性，防止误操作 |
| **`SET_NULL`** | 将被脱钩对象的外键设置为 `NULL` | 保留子对象，仅解除关联 | 要求外键字段可为空 |
| **`DELETE`** | 删除被脱钩的关联对象 | 级联删除孤儿数据 | 如果对象还有更深层的关联，会递归处理 |

### 脱钩模式配置方式

#### 方式一：实体注解（静态配置，全局生效）

```java
@Entity
public interface Book {
    
    @OnDissociate(DissociateAction.SET_NULL)  // 配置脱钩模式
    @Nullable
    @ManyToOne
    BookStore store();
    
    // ... 其他属性
}
```

```kotlin
@Entity
interface Book {
    
    @OnDissociate(DissociateAction.SET_NULL)
    @ManyToOne
    val store: BookStore?
    
    // ... 其他属性
}
```

#### 方式二：保存指令（动态配置，单次生效）

```java
// SET_NULL 模式 - 脱钩时将外键设为 NULL
Book saved = sqlClient
    .saveCommand(book)
    .setDissociateAction(
        BookProps.STORE, 
        DissociateAction.SET_NULL
    )
    .execute()
    .getModifiedEntity();

// DELETE 模式 - 脱钩时删除关联对象
Book saved = sqlClient
    .saveCommand(book)
    .setDissociateAction(
        BookProps.AUTHORS, 
        DissociateAction.DELETE
    )
    .execute()
    .getModifiedEntity();

// CHECK 模式 - 禁止脱钩，存在脱钩对象时抛出异常
Book saved = sqlClient
    .saveCommand(book)
    .setDissociateAction(
        BookProps.AUTHORS, 
        DissociateAction.CHECK
    )
    .execute()
    .getModifiedEntity();
```

```kotlin
// Kotlin 版本
sqlClient.save(book) {
    setDissociateAction(
        Book::store,
        DissociateAction.SET_NULL
    )
}
```

### 完整实战示例

#### 场景 1：书籍更换书店（SET_NULL 模式）

```java
// 书籍原本属于 "O'REILLY" 书店
// 现在要将书籍移到 "MANNING" 书店
// 原书店中的该书籍记录外键设为 NULL

Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);  // 已存在的书籍
    draft.setName("Learning GraphQL");
    draft.setStoreId(2L);  // 新书店 MANNING 的 ID
});

Book saved = sqlClient
    .saveCommand(book)
    .setDissociateAction(
        BookProps.STORE, 
        DissociateAction.SET_NULL  // 原关联的外键设为 NULL
    )
    .execute()
    .getModifiedEntity();
```

**执行的 SQL 流程：**
```sql
-- 1. 更新书籍的 store_id
UPDATE book SET store_id = 2 WHERE id = 1;

-- 2. 查找原书店中被脱钩的其他书籍（如果有）
-- 将这些书籍的 store_id 设为 NULL
UPDATE book 
SET store_id = NULL 
WHERE store_id = 1 
  AND id NOT IN (1, 2, 3, ...);  -- 保留当前保存的书籍
```

#### 场景 2：彻底删除书籍的关联作者（DELETE 模式）

```java
// 书籍有 3 个作者：作者1、作者2、作者3
// 现在只保留作者1，作者2和作者3需要被彻底删除

Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);
    draft.setName("GraphQL in Action");
    
    // 只保留作者1
    draft.addIntoAuthors(author -> {
        author.setId(1L);  // 已存在的作者1
    });
    // 注意：没有添加作者2和作者3，它们会被脱钩
});

Book saved = sqlClient
    .saveCommand(book)
    .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.REPLACE)  // 替换模式
    .setDissociateAction(
        BookProps.AUTHORS, 
        DissociateAction.DELETE  // 彻底删除脱钩的作者
    )
    .execute()
    .getModifiedEntity();
```

**执行的 SQL 流程：**
```sql
-- 1. 更新书籍基本信息
UPDATE book SET name = 'GraphQL in Action' WHERE id = 1;

-- 2. 删除中间表中被脱钩的关联（作者2、作者3）
DELETE FROM book_author_mapping 
WHERE book_id = 1 
  AND author_id IN (2, 3);

-- 3. 彻底删除被脱钩的作者对象
DELETE FROM author WHERE id IN (2, 3);
```

#### 场景 3：禁止意外脱钩（CHECK 模式）

```java
// 业务规则：禁止随意移除书籍的关联作者
// 如果尝试移除会抛出异常

Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);
    draft.setName("GraphQL in Action");
    
    // 尝试只保留作者1，移除作者2和作者3
    draft.addIntoAuthors(author -> author.setId(1L));
});

try {
    Book saved = sqlClient
        .saveCommand(book)
        .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.REPLACE)
        .setDissociateAction(
            BookProps.AUTHORS, 
            DissociateAction.CHECK  // 禁止脱钩
        )
        .execute()
        .getModifiedEntity();
} catch (Exception e) {
    // 抛出异常：
    // "Cannot dissociate child objects because the dissociation 
    //  action of the many-to-one property 'Book.authors' 
    //  is configured as CHECK"
    System.err.println("禁止移除书籍的作者！" + e.getMessage());
}
```

### 脱钩模式选择指南

| 业务场景 | 推荐模式 | 说明 |
|---------|---------|------|
| 更换父对象，保留子对象 | `SET_NULL` | 子对象变为"孤儿"但不被删除 |
| 彻底清理孤儿数据 | `DELETE` | 子对象随关联解除被删除 |
| 防止误操作导致数据丢失 | `CHECK` | 存在脱钩风险时抛出异常 |
| 不关心孤儿数据 | `LAX` | 脱钩时不做任何处理（谨慎使用） |

### 注意事项

1. **递归脱钩风险**：使用 `DELETE` 模式时，如果被删除的对象还有更深层的关联，可能会触发级联删除，导致大量数据被意外删除。建议配合 `@OnDissociate` 注解在实体层面做好防护。

2. **外键约束**：`SET_NULL` 模式要求外键字段必须是可空的（`nullable`），否则会导致数据库约束错误。

3. **缓存一致性**：当使用 `LAX` 模式配合数据库级联（如 `ON DELETE CASCADE`）时，Jimmer 的缓存可能无法感知数据变化，导致缓存不一致。在启用缓存的项目中请谨慎使用。

4. **优先级关系**：动态配置（保存指令中设置）会覆盖静态配置（实体注解），可以根据具体业务场景灵活调整。

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
    .setDissociateAction(BookProps.AUTHORS, DissociateAction.DELETE)  // 删除孤儿标签
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