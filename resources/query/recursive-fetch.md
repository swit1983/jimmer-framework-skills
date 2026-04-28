# 递归查询

Jimmer 的对象抓取器支持递归查询，用于处理树形结构（如分类、评论、组织结构）。

## 基本概念

递归查询用于处理**自关联**（Self-Referencing）实体：

```java
@Entity
public interface Category {
    @Id
    long id();
    
    String name();
    
    // 父分类（可为空，表示根节点）
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    @Nullable
    Category parent();
    
    // 子分类列表
    @OneToMany(mappedBy = "parent")
    List<Category> children();
}
```

```kotlin
@Entity
interface Category {
    @Id
    val id: Long
    
    val name: String
    
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    val parent: Category?
    
    @OneToMany(mappedBy = "parent")
    val children: List<Category>
}
```

## 基本递归查询

使用 `recursiveXxx()` 方法（根据关联属性名自动生成）进行递归抓取：

```java
// Java：recursiveChildNodes 由属性名 childNodes 生成
TreeNode category = sqlClient
    .createQuery(table)
    .where(table.parentId().isNull())
    .select(table.fetch(
        Fetchers.TREE_NODE_FETCHER
            .name()
            .recursiveChildNodes()  // 递归抓取 childNodes
    ))
    .fetchOne();
```

```kotlin
// Kotlin：使用 `属性名*` 语法
val category = sqlClient
    .createQuery(TreeNode::class) {
        where(table.parentId.isNull())
        select(table.fetchBy {
            allScalarFields()
            `childNodes*`()  // 递归抓取 childNodes
        })
    }
    .fetchOne()
```

结果示例：

```json
{
    "id": 1,
    "name": "电子产品",
    "children": [
        {
            "id": 2,
            "name": "手机",
            "children": [
                {
                    "id": 5,
                    "name": "智能手机",
                    "children": []
                }
            ]
        },
        {
            "id": 3,
            "name": "电脑",
            "children": []
        }
    ]
}
```

## 限制递归深度

使用 `depth()` 防止无限递归：

```java
// Java
Fetchers.TREE_NODE_FETCHER
    .name()
    .recursiveChildNodes(
        it -> it.depth(3)  // 最多递归 3 层
    )
```

```kotlin
// Kotlin
newFetcher(TreeNode::class).by {
    allScalarFields()
    `childNodes*` {
        depth(3)  // 最多递归 3 层
    }
}
```

## 递归控制（控制递归节点）

使用 `recursive()` 控制哪些节点参与递归：

```java
// Java
Fetchers.TREE_NODE_FETCHER
    .name()
    .childNodes(
        Fetchers.TREE_NODE_FETCHER.name(),
        it -> it.recursive(args ->
            // 只递归 name 不等于 "Clothing" 的节点
            !args.getEntity().name().equals("Clothing")
        )
    )
```

```kotlin
// Kotlin
newFetcher(TreeNode::class).by {
    allScalarFields()
    childNodes {
        recursive {
            // 只递归 name 不等于 "Clothing" 的节点
            entity.name != "Clothing"
        }
    }
}
```

`recursive()` 的 lambda 参数提供两个属性：
1. `args.getEntity()`：当前节点对象
2. `args.getDepth()`：当前节点深度（根节点为 0，随递归深入增大）

## 递归获取父节点

递归不仅限于子节点，也可以向上递归获取父节点：

```java
// Java：使用 recursiveParent() 向上递归
TreeNode treeNode = sqlClient
    .createQuery(table)
    .where(table.id().eq(5L))  // 查询叶子节点
    .select(table.fetch(
        Fetchers.TREE_NODE_FETCHER
            .name()
            .recursiveParent()  // 向上递归获取所有父节点
    ))
    .fetchOne();
```

```kotlin
// Kotlin：使用 `parent*` 向上递归
val treeNode = sqlClient
    .createQuery(TreeNode::class) {
        where(table.id eq 5L)
        select(table.fetchBy {
            allScalarFields()
            `parent*`()  // 向上递归获取所有父节点
        })
    }
    .fetchOne()
```

结果：

```json
{
    "id": 5,
    "name": "智能手机",
    "parent": {
        "id": 2,
        "name": "手机",
        "parent": {
            "id": 1,
            "name": "电子产品",
            "parent": null
        }
    }
}
```

## 递归 + 子属性抓取

递归查询无需指定关联对象格式，因为关联对象格式就是当前对象格式本身：

```java
// Java：递归抓取 childNodes，每个节点同时抓取 products
Fetchers.TREE_NODE_FETCHER
    .name()
    .products(ProductFetcher.$.name().price())
    .recursiveChildNodes()
```

```kotlin
// Kotlin
newFetcher(TreeNode::class).by {
    allScalarFields()
    products {
        name()
        price()
    }
    `childNodes*`()
}
```

> 多个关联属性可同时递归，如 `recursiveParent()` 和 `recursiveChildNodes()`，
> 向上递归和向下递归路径完全独立，不会混合。

## 注意事项

| 注意点 | 说明 |
|--------|------|
| 循环引用 | Jimmer 会自动检测并防止循环递归 |
| 深度限制 | 建议始终设置 `depth()` 防止意外深度递归 |
| 性能考虑 | 深层递归会产生大量 SQL，考虑使用缓存 |
| 内存占用 | 深层嵌套对象可能占用较多内存 |

## 对比 GraphQL

| 特性 | Jimmer 递归 | GraphQL |
|------|-------------|---------|
| 递归深度 | 支持无限深度（可限制） | 不支持无限递归 |
| 使用范围 | ORM 层，任何代码可用 | 仅 HTTP 服务层 |
| 类型安全 | 编译时检查 | 运行时检查 |
