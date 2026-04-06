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
| `NONE` | 默认，不允许脱钩，尝试脱钩会报错 | 强制关联必须存在 |
| `DELETE` | 脱钩时删除关联对象 | 级联删除 |
| `SET_NULL` | 脱钩时将外键设为 NULL | 可选关联，允许为空 |
| `SET_DEFAULT` | 脱钩时将外键设为默认值 | 有默认值的场景 |

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

删除订单时，同时删除所有订单项：

```java
@Entity
public interface Order {
    @Id
    long id();
    
    // 一对多关联
    @OneToMany(mappedBy = "order")
    @OnDissociate(DissociateAction.DELETE)  // 脱钩时删除子对象
    List<OrderItem> items();
}

@Entity
public interface OrderItem {
    @Id
    long id();
    
    @ManyToOne
    @JoinColumn(name = "ORDER_ID")
    Order order();
}
```

### NONE - 强制关联

不允许解除关联，必须显式处理：

```java
@Entity
public interface Employee {
    @Id
    long id();
    
    // 每个员工必须属于一个部门，不允许脱钩
    @ManyToOne
    @JoinColumn(name = "DEPT_ID", nullable = false)
    @OnDissociate(DissociateAction.NONE)  // 默认，显式声明
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
| 外键约束 | `SET_NULL` 要求外键列可为空 |
| 级联深度 | `DELETE` 只会删除直接关联对象，不会级联更深 |
| 性能影响 | 大量脱钩操作可能影响性能，考虑批量处理 |
| 与缓存关系 | 脱钩操作会自动触发相关缓存失效 |
