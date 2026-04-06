# 对象抓取器 (Fetcher) API

## 概念

一句话：**查询任意形状的数据结构，就如同 GraphQL 所做的那样**。

对象抓取器是 Jimmer 的核心功能之一，抓取指定的字段，自动组装成对象，节省了很多手写转换逻辑的时间。

对象抓取器和以下技术类似，但更加强大：
- JPA 的 EntityGraph
- ADO.NET EntityFramework 的 Include
- ActiveRecord 的 include

## 解决的问题

传统 ORM 容易遇到两个问题：

1. **Over-fetch 问题**：不需要的对象属性也被查询了，形成浪费。传统 ORM 返回的默认是完整对象。

2. **Under-fetch 问题**：需要的对象属性没有被获取，处于不可用的 unloaded 状态，程序无法正确运行。

对象抓取器很好地解决了这个问题：通过让用户指定要抓取的属性，利用动态对象的特性，让查询返回的对象既不 over-fetch 也不 under-fetch。

你可以决定某个业务视角是否需要查询哪些实体、关系、甚至每一个属性。

## 核心特性

| 特性 | 说明 |
|------|------|
| 任意深度嵌套 | 支持多级关联嵌套，可以精确控制每一层的返回形状 |
| 递归查询 | 支持对自关联属性进行递归查询，GraphQL 不支持无限深度递归 |
| 批量优化 | 自动优化查询性能，无论多少层嵌套都不会有 N+1 问题 |
| 支持 DTO | 既可以直接返回实体对象，也可以直接投影到 DTO |
| 编译时检查 | 强类型支持，所有属性都有编译检查和 IDE 智能提示 |

## 使用场景

- REST 服务：服务端控制返回对象形状，直接返回实体，无需手动 DTO 转换
- GraphQL 服务：天生支持类似 GraphQL 的数据形状控制，容易构建 GraphQL 服务
- 复杂报表查询：只查询需要的字段，避免加载大量无用数据
- 树形结构查询：支持递归查询无限深度的树形结构（如分类、评论）

## vs GraphQL

| 对比 | Jimmer Fetcher | GraphQL |
|------|----------------|---------|
| 使用范围 | ORM 基础 API，可在任何代码中使用 | 仅能在 HTTP 服务层面使用 |
| 递归查询 | 支持无限深度递归 | 不支持 |
| 类型安全 | 编译时检查 | 运行时检查 |

## 基本语法

### Java

```java
Fetcher<Book> bookFetcher = BookFetcher.$
    .id()
    .name()
    .edition()
    .price()
    .store(store -> store
        .id()
        .name()
        .website()
    )
    .authors(author -> author
        .id()
        .firstName()
        .lastName()
    );
```

### Kotlin

```kotlin
val bookFetcher = BookFetcher {
    it.id()
    it.name()
    it.edition()
    it.price()
    it.store { store ->
        store.id()
        store.name()
        store.website()
    }
    it.authors { author ->
        author.id()
        author.firstName()
        author.lastName()
    }
}
```

## 快捷方法

| 方法 | 说明 |
|------|------|
| `allScalarFields()` | 包含所有标量属性（非关联属性） |
| `allTableFields()` | 包含所有表字段 |
| `recursive(...)` | 递归查询自关联 |
| `depthLimit(int)` | 限制递归深度，避免无限递归 |

## 代码示例

- [基础示例](../../examples/fetch/basic-fetch-example.java) - Java
- [基础示例](../../examples/fetch/basic-fetch-example.kt) - Kotlin
