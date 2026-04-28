# 脱钩操作 (OnDissociate)

脱钩（Dissociate）是指解除对象间的关联关系，但不删除对象本身。Jimmer 通过 `@OnDissociate` 注解支持自动脱钩。

## 概念

**脱钩 vs 删除：**
- **删除**：删除对象本身（物理删除或逻辑删除）
- **脱钩**：只解除两个对象之间的关联，对象本身保留

**典型场景：**
- 将书籍从书店中移除（书籍仍存在于数据库，只是不再属于该书店）
- 将作者从书籍的贡献者列表中移除（作者仍存在，只是不再关联该书籍）

## @OnDissociate 注解

用于指定当关联被破坏时的脱钩行为。

### 属性

| 属性 | 说明 |
|------|------|
| `value` | 脱钩策略，`DissociateAction` 枚举 |

### DissociateAction 策略

| 策略 | 说明 | 适用场景 |
|------|------|---------|
| `NONE`（默认） | 视全局配置和外键真假而定，等价于 `CHECK` 或 `LAX` | 取决于配置 |
| `LAX` | 不执行任何动作，由数据库级联行为处理（真外键）或放任悬挂（假外键） | 数据库已有级联配置 |
| `CHECK` | 不允许脱钩，如有需要脱钩的子对象则抛出异常 | 强制关联必须存在 |
| `SET_NULL` | 脱钩时将外键设为 NULL | 可选关联，允许为空 |
| `DELETE` | 脱钩时删除关联对象 | 级联删除 |

> ⚠️ `@OnDissociate` **只能用在基于外键映射的多对一关联上**，不能用于一对多关联。虽然脱钩由一对多关联导致，但配置针对逆向的多对一关联，保持与数据库 DDL 外键级联配置的相似性。

## 使用示例

### SET_NULL - 可选关联

书籍可以不归属任何书店，脱钩时将外键设为 NULL：

```java
@Entity
public interface Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    String name();
    
    // 多对一关联，可选
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    @OnDissociate(DissociateAction.SET_NULL)  // 脱钩时设为 NULL
    @Nullable  // 可为空
    BookStore store();
}
```

```kotlin
@Entity
interface Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
    
    val name: String
    
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    @OnDissociate(DissociateAction.SET_NULL)
    val store: BookStore?  // 可为空
}
```

### DELETE - 级联删除

删除书店时，同时删除所有关联的书籍（配置在 `Book.store` 上的多对一关联）：

```java
@Entity
public interface BookStore {
    @Id
    long id();
    
    // 一对多关联
    @OneToMany(mappedBy = "store")
    List<Book> books();
}

@Entity
public interface Book {
    @Id
    long id();
    
    // 多对一关联，@OnDissociate 配置在这里！
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    @OnDissociate(DissociateAction.DELETE)  // 脱钩时删除子对象
    BookStore store();
}
```

### CHECK - 强制关联

不允许解除关联，必须显式处理：

```java
@Entity
public interface Employee {
    @Id
    long id();
    
    // 每个员工必须属于一个部门，不允许脱钩
    @ManyToOne
    @JoinColumn(name = "DEPT_ID", nullable = false)
    @OnDissociate(DissociateAction.CHECK)  // 有子对象需要脱钩时抛异常
    Department department();
}
```

## 触发脱钩的场景

### 1. Save Command 保存指令

使用 `save` 保存根对象时，会自动处理关联的脱钩：

```java
// 保存书籍，不指定 store，触发 store 脱钩
sqlClient.save(BookDraft.$.produce(book -> {
    book.setId(1L);
    book.setName("New Name");
    // store 不设置，原有关联将被脱钩
}));
```

### 2. Delete Command 删除指令

删除父对象时，根据 `@OnDissociate` 配置处理子关联：

```java
// 删除书店，关联的书籍根据 @OnDissociate 配置处理
sqlClient.deleteById(BookStore.class, 1L);
```

### 3. 显式设置关联为 null

```java
// 将书籍从书店中移除
sqlClient.save(BookDraft.$.produce(book -> {
    book.setId(1L);
    book.setStoreId(null);  // 显式设为 null，触发脱钩
}));
```

## 注意事项

| 注意点 | 说明 |
|--------|------|
| 配置位置 | `@OnDissociate` 只能用在多对一关联上，不能用于一对多 |
| 外键约束 | `SET_NULL` 要求外键列可为空 |
| 动态覆盖 | 运行时可通过 save/delete 命令的 API 覆盖静态配置 |
| 与缓存关系 | 脱钩操作会自动触发相关缓存失效 |
