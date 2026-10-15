---
title: 版本模式
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/mutation/save-command/version-mode
---

# 版本模式

保存指令可以把根实体的`@Version`视为乐观锁，也可以把它视为普通赋值。默认仍然是
`VersionMode.OPTIMISTIC_LOCK`。

| 模式 | 更新时已加载的版本 | 自动递增 |
| --- | --- | --- |
| `OPTIMISTIC_LOCK` | 添加隐式版本检查 | 是，带版本的更新成功后递增 |
| `ASSIGNMENT` | 由对象形状、mask和赋值控制 | 否 |

并发编辑需要检测过期版本时使用默认模式。导入由外部维护版本的数据，或实现显式版本策略时，
使用`ASSIGNMENT`。此时已加载的版本既不是隐式乐观锁条件，也不代表要求自动递增。

## 写入输入版本

假设`store`包含外部提供的版本以及插入所需的值。对于已有行，只接受更高的输入版本：

**Java**

```java
sqlClient
    .saveCommand(store)
    .setVersionMode(VersionMode.ASSIGNMENT)
    .setUpdateWhere(
        BookStoreTable.class,
        (table, values) -> values.newNumber(BookStoreProps.VERSION).gt(table.version())
    )
    .execute();
```

**Kotlin**

```kotlin
sqlClient.save(store) {
    setVersionMode(VersionMode.ASSIGNMENT)
    setUpdateWhere(BookStore::class) {
        newNonNull(BookStore::version) gt table.version
    }
}
```

被接受的更新会原样写入输入版本。更低或相等的版本被正常拒绝，可检查[结果是否被接受](./returning.md#acceptance)。
新行的插入不受更新条件限制。

未加载的版本在插入时仍然获得框架初始值。在赋值模式下，如果更新mask排除了已加载版本，
冲突时数据库中的版本会保持不变。

## 计算下一个版本

赋值模式下，`@Version`也可以作为[保存赋值表达式](./assignment.md)的目标：

**Java**

```java
sqlClient
    .saveCommand(store)
    .setVersionMode(VersionMode.ASSIGNMENT)
    .set(
        BookStoreTable.class,
        BookStoreProps.VERSION,
        (target, values) -> target.version().plus(1)
    )
    .execute();
```

**Kotlin**

```kotlin
sqlClient.save(store) {
    setVersionMode(VersionMode.ASSIGNMENT)
    set(BookStore::version) {
        target.version + 1
    }
}
```

普通保存赋值规则仍然适用：目标必须已加载，而且被mask选中用于更新。需要比较后递增时，
应显式添加`setUpdateWhere`或`setOptimisticLock`条件，对比数据库版本和输入版本。

:::caution
`setVersionMode`只作用于根实体，不会传播到图保存中的关联实体。切换到`ASSIGNMENT`会移除隐式
版本保护，不会暗中替换成另一种检查。
:::

[从查询插入或upsert](../insert-from-select.md)对版本使用赋值语义，source中的版本值不会意外启用乐观锁。
