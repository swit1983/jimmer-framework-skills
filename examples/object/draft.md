# Draft 模式示例

Draft 模式是 Jimmer 处理不可变对象修改的核心方案，借鉴自 immer.js。

## 基本用法

### Java

```java
// 已有不可变对象
Book book = ...;

// 创建 draft 进行修改，生成新的不可变对象
Book newBook = BookDraft.$.produce(book, draft -> {
    draft.setName("New Name");
    draft.setPrice(new BigDecimal("99.99"));
});

// 原对象保持不变，newBook 是修改后的新对象
```

### Kotlin

```kotlin
val book: Book = ...

val newBook = book.byDraft {
    name = "New Name"
    price = BigDecimal("99.99")
}
```

## 修改深层对象

```java
// 修改嵌套对象
Order order = ...;

Order newOrder = OrderDraft.$.produce(order, draft -> {
    draft.setCustomer(customer -> {
        customer.setAddress(address -> {
            address.setCity("Shanghai");
        });
    });
});
```

## 创建新对象

```java
// 从零创建新对象
Book newBook = BookDraft.$.produce(draft -> {
    draft.setName("New Book");
    draft.setPrice(new BigDecimal("88.88"));
    draft.setStore(store);
});
```

## 特点

1. **原始对象不变** - 修改不影响原对象
2. **结构共享** - 未修改的部分复用原对象，性能好
3. **类型安全** - 编译时检查，IDE 自动补全
4. **任意深度** - 支持深层嵌套对象修改

## 使用场景

- 基于已有对象创建修改后的新对象
- 深度修改不可变数据结构
- 配合不可变性获得线程安全和缓存好处，同时保持易用性

