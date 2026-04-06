# 不可变性示例

Jimmer 实体是不可变对象，借鉴了 [immer](https://github.com/immerjs/immer) 的思想。

## 为什么需要不可变性

1. **避免循环引用** - 不可变对象天然避免循环引用导致的问题
2. **线程安全** - 不可变对象天然线程安全
3. **利于缓存** - 不可变对象可以放心缓存
4. **Jackson 序列化** - 天然支持 Jackson 序列化，无需特殊处理

## 不可变性带来的问题

对于深层次的数据结构，基于不可变性修改比较困难。Jimmer 使用 **Draft 模式** 解决这个问题。

## Draft 模式使用示例

```java
// 原始不可变对象
Book book = ...;

// 创建 draft，可以修改
Book draft = BookDraft.$.produce(book, d -> {
    d.setName("New Name");
    // 修改关联
    d.setStore(anotherStore);
});

// draft 就是新的不可变对象，原始对象不变
```

## Kotlin 示例

```kotlin
val book: Book = ...

val newBook = book.byDraft {
    name = "New Name"
    store = anotherStore
}
```

## 核心思想

1. **原始对象不变** - 修改不会改变原对象
2. **结构共享** - 未修改的部分复用原对象，节省内存
3. **修改完成生成新对象** - 整个过程对开发人员透明

## ⚠️ 常见误解

- Jimmer 的不可变性是 **设计选择**，不是技术限制
- Draft 模式解决了不可变性难以修改的问题
- 受益：循环引用安全、缓存友好、线程安全

