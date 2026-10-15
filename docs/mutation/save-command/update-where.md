---
title: 更新条件
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/mutation/save-command/update-where
---

# 更新条件

如果只有满足业务条件时才能更新现有行，使用`setUpdateWhere`。更新被拒绝是一种正常结果，
不会抛出乐观锁异常。例如，仅接受高于数据库当前价格的输入价格。

## 基本用法

假设`book`包含id、价格以及插入新行时所需的其他值：

**Java**

```java
SimpleSaveResult<Book> result = sqlClient
    .saveCommand(book)
    .setMode(SaveMode.UPSERT)
    .setUpdateWhere(
        BookTable.class,
        (table, values) -> values.newNumber(BookProps.PRICE).gt(table.price())
    )
    .execute();

if (result.isAccepted()) {
    Book modifiedBook = result.getModifiedEntity();
}
```

**Kotlin**

```kotlin
val result = sqlClient.save(book) {
    setMode(SaveMode.UPSERT)
    setUpdateWhere(Book::class) {
        newNonNull(Book::price) gt table.price
    }
}

if (result.isAccepted) {
    val modifiedBook = result.modifiedEntity
}
```

table代表数据库中的现有行，`newNumber`/`newNonNull`代表被保存对象中已加载的值。Kotlin可空
输入值使用`newNullable`。条件引用的输入属性可以不是更新目标，但必须已加载。
条件可以读取本地物理属性，不能创建表连接。

| 情况 | 结果 |
| --- | --- |
| `UPSERT`没有冲突 | 插入，不受更新条件限制 |
| 行已存在，条件为true | 执行更新 |
| 行已存在，条件为false或SQL unknown | 拒绝更新，不抛出异常 |

条件适用于`UPDATE_ONLY`、`UPSERT`以及`NON_IDEMPOTENT_UPSERT`的更新分支。回调返回`null`表示
不为该实体类型添加限制。多个业务要求使用普通条件DSL组合。批量保存也支持相同配置，每个结果项
分别报告是否被接受。

## 与赋值和乐观锁组合

[赋值表达式](./assignment.md)决定被接受更新写入什么值，`setUpdateWhere`决定是否允许该更新。
即使[upsert mask](./upsert-mask.md)没有选中普通更新赋值，条件仍然有效。

`setOptimisticLock`的失败策略不同：检查失败会抛出已有的乐观锁错误。同时配置两者时，先判断
是否允许更新，只有被接受的更新才进行乐观锁检查。

Java的两个API都使用`UpdateCondition<E, T>`。原有回调可以继续使用；显式声明了已弃用
`UserOptimisticLock`接口的代码应迁移到`UpdateCondition`。Kotlin的两个方法使用相同的接收者
上下文，包含`table`、`newNonNull`和`newNullable`。

## 关联、事件和并发

被拒绝的主对象不会继续修改其后置关联，该行也不会产生事务事件或缓存失效。图保存会先处理
关联维护方的引用对象，再处理主对象。因此，当更新条件有效时，指令会在执行SQL前拒绝可变的
维护方引用对象；这种情况下应使用`null`或id-only引用。

支持的方言会直接把条件放入更新分支。原生`UPDATE_ONLY`仍然是`UPDATE ... WHERE ...`，
不会改写成upsert。不支持原生条件upsert时，保存流程可能先查询条件，再执行DML。
fallback不会暗中保证多条SQL之间的并发一致性。需要排除并发修改时，请使用适当的事务隔离级别
或[悲观锁](./pessimistic-locking.md)。

`isAccepted`的含义请参阅[保存结果获取](./returning.md#acceptance)。
