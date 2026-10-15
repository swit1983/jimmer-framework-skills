---
title: 4.3 关联id
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/where/associated-id
---

# 4.3 关联id

## IsNull判断

查询所有`parentId`为null的`TreeNode`，即根节点。

**Java**

```java
TreeNodeTable table = TreeNodeTable.$;
List<TreeNode> rootNodes = sqlClient
    .createQuery(table)

    .where(table.parentId().isNull())
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val rootNodes = sqlClient
    .createQuery(TreeNode::class) {

        where(table.parentId.isNull())
        select(table)
    }
    .execute()
```

:::tip
上述代码中的`parentId`，是Jimmer在编译时根据多对一属性`TreeNode.parent`自动生成的，
即使开发人员没有配套定义名称为`parentId`的[@IdView属性](../../mapping/advanced/view/id-view.md)也仍然如此。
:::

## 任意值的判断

事实上，`eq`支持null参数，当`eq`的参数为null时，则渲染`is null`

:::caution
注意：Java的`eqIf`和Kotlin的`eq?`不同，null被认为是动态查询，不会渲染任何SQL条件。
:::

**Java**

```java
@Nullable Long parentId = ...略...;

TreeNodeTable table = TreeNodeTable.$;
List<TreeNode> rootNodes = sqlClient
    .createQuery(table)

    .where(table.parentId().eq(parentId))
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val parentId: Long? = ...略...;

val rootNodes = sqlClient
    .createQuery(TreeNode::class) {

        where(table.parentId eq parentId)
        select(table)
    }
    .execute()
```
