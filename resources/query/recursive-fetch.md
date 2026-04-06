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

使用 `recursive()` 方法进行递归抓取：

```java
// Java
Category category = sqlClient
    .createQuery(table)
    .where(table.id().eq(1L))
    .select(table.fetch(
        CategoryFetcher.$
            .name()
            .children(recursive())  // 递归抓取 children
    ))
    .fetchOne();
```

```kotlin
// Kotlin
val category = sqlClient
    .createQuery(Category::class) {
        where(table.id eq 1L)
        select(table.fetchBy {
            name()
            children(recursive())  // 递归抓取 children
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

使用 `depthLimit()` 防止无限递归：

```java
CategoryFetcher.$
    .name()
    .children(
        recursive(
            CategoryFetcher.$
                .name(),
            depthLimit(3)  // 最多递归 3 层
        )
    )
```

## 递归控制（控制递归节点）

使用 `nodePredicate()` 控制哪些节点参与递归：

```java
CategoryFetcher.$
    .name()
    .children(
        recursive(
            CategoryFetcher.$
                .name(),
            // 只递归 isActive=true 的节点
            nodePredicate((draft, node) -> node.isActive())
        )
    )
```

## 递归获取父节点

递归不仅限于子节点，也可以向上递归获取父节点：

```java
Category category = sqlClient
    .createQuery(table)
    .where(table.id().eq(5L))  // 查询叶子节点
    .select(table.fetch(
        CategoryFetcher.$
            .name()
            .parent(recursive())  // 向上递归获取所有父节点
    ))
    .fetchOne();
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

递归的同时可以抓取关联对象的其他属性：

```java
CategoryFetcher.$
    .name()
    .createdTime()
    .children(
        recursive(
            CategoryFetcher.$
                .name()
                .products(  // 同时抓取关联的产品
                    ProductFetcher.$
                        .name()
                        .price()
                )
        )
    )
```

## 注意事项

| 注意点 | 说明 |
|--------|------|
| 循环引用 | Jimmer 会自动检测并防止循环递归 |
| 深度限制 | 建议始终设置 `depthLimit` 防止意外深度递归 |
| 性能考虑 | 深层递归会产生大量 SQL，考虑使用缓存 |
| 内存占用 | 深层嵌套对象可能占用较多内存 |

## 对比 GraphQL

| 特性 | Jimmer 递归 | GraphQL |
|------|-------------|---------|
| 递归深度 | 支持无限深度（可限制） | 不支持无限递归 |
| 使用范围 | ORM 层，任何代码可用 | 仅 HTTP 服务层 |
| 类型安全 | 编译时检查 | 运行时检查 |
