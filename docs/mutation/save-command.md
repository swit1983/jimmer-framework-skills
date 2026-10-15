---
title: 保存指令
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/mutation/save-command
---

# 保存指令

## 功能

保存指令是Jimmer最为强大的功能，用于保存任意图结构

-   利用数据库本身的upsert能力merge数据
-   每一层的多个对象都用批量DML操作
-   自动翻译违背约束的异常
-   任何层次的被保存对象都可以不完整
-   既可以直接保存实体，也可保存Input DTO

:::caution
保存指令的用法和绝大部分ORM都不相同，建议先阅读文档或本文给出的示例，不要按照自己对其他ORM的理解去做猜测性使用。
:::

## 配套例子

-   Java: [jimmer-examples/java/save-command](https://github.com/babyfish-ct/jimmer-examples/tree/main/java/save-command)

-   Kotlin: [jimmer-examples/kotlin/save-command-kt](https://github.com/babyfish-ct/jimmer-examples/tree/main/kotlin/save-command-kt)

用IDE随意打开其中一个，运行单元测试即可。

## 控制更新分支

-   [Upsert Mask](save-command/upsert-mask.md)：为插入和更新选择不同的属性。
-   [赋值表达式](save-command/assignment.md)：根据数据库值和输入值计算新值。
-   [更新条件](save-command/update-where.md)：拒绝更新而不抛出乐观锁异常。
-   [版本模式](save-command/version-mode.md)：把版本作为乐观锁或普通赋值。
-   [保存结果获取](save-command/returning.md)：获取数据库实际值，并检查修改是否被接受。

输入是查询结果而不是对象图时，使用[从查询插入和合并数据](./insert-from-select.md)。
