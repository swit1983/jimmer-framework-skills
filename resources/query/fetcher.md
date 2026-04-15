# 对象抓取器 (Fetcher)

> 一句话：**查询任意形状的数据结构，就如同 GraphQL 所做的那样**。

## 概念

对象抓取器是 Jimmer 的核心功能之一，用于**指定查询返回的数据结构形状**。

传统 ORM 容易遇到两个问题：
1. **Over-fetch**：不需要的属性也被查询，造成浪费
2. **Under-fetch**：需要的属性未被获取，导致程序无法正确运行

对象抓取器很好地解决了这个问题：**只查询需要的字段，返回指定形状的数据**。

## 基本用法

### 1. 创建 Fetcher

#### Java

```java
// 基本 Fetcher：指定需要的字段
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .edition()
    .price();

// 关联对象抓取
Fetcher<Book> fetcherWithStore = BookFetcher.$
    .name()
    .price()
    .store(BookStoreFetcher.$.name().website());

// 关联集合抓取
Fetcher<Book> fetcherWithAuthors = BookFetcher.$
    .name()
    .authors(AuthorFetcher.$.firstName().lastName());
```

#### Kotlin

```kotlin
val fetcher = BookFetcher {
    name()
    edition()
    price()
}

val fetcherWithStore = BookFetcher {
    name()
    price()
    store {
        name()
        website()
    }
}

val fetcherWithAuthors = BookFetcher {
    name()
    authors {
        firstName()
        lastName()
    }
}
```

### 2. 在查询中使用 Fetcher

#### Java

```java
BookTable book = BookTable.$;

List<Book> books = sqlClient
    .createQuery(book)
    .where(book.edition().eq(3))
    .select(book.fetch(BookFetcher.$
        .name()
        .edition()
        .price()
        .store(BookStoreFetcher.$.name())
    ))
    .execute();
```

#### Kotlin

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        where(table.edition eq 3)
        select(table.fetchBy {
            name()
            edition()
            price()
            store { name() }
        })
    }
    .execute()
```

## 快捷方法

### allScalarFields() - 所有标量字段

```java
// 抓取所有标量属性（非关联属性）
Fetcher<Book> fetcher = BookFetcher.$.allScalarFields();
// 等价于：id, name, edition, price 等所有非关联字段
```

### allTableFields() - 所有表字段

```java
// 抓取所有表字段（包括外键）
Fetcher<Book> fetcher = BookFetcher.$.allTableFields();
```

## 关联属性抓取

### 引用关联（多对一）

```java
// Book.store: 多对一关联
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .store(BookStoreFetcher.$.name().website());
```

### 集合关联（多对多/一对多）

```java
// Book.authors: 多对多关联
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .authors(AuthorFetcher.$.firstName().lastName());
```

## 属性过滤器（Property Filters）

属性过滤器用于在抓取关联集合时对子元素进行**过滤和限制**，这是处理大量关联数据的重要工具。

### 基本用法

```java
// 只抓取启用状态的用户
Fetcher<Department> fetcher = DepartmentFetcher.$
    .name()
    .employees(
        EmployeeFetcher.$.firstName().lastName(),
        filter -> filter.status().eq(Status.ACTIVE)  // 过滤条件
    );
```

### 限制数量（Limit）

```java
// 每个部门只抓取前5名员工
Fetcher<Department> fetcher = DepartmentFetcher.$
    .name()
    .employees(
        EmployeeFetcher.$.firstName().lastName(),
        filter -> filter.limit(5)  // 限制数量
    );
```

### 过滤 + 限制组合

```java
// 抓取每个部门中工资最高的前3名正式员工
Fetcher<Department> fetcher = DepartmentFetcher.$
    .name()
    .employees(
        EmployeeFetcher.$.firstName().lastName().salary(),
        filter -> filter
            .employeeType().eq(EmployeeType.FULL_TIME)  // 过滤正式员工
            .orderBy(SalaryTable.$.amount.desc())          // 按工资降序
            .limit(3)                                       // 取前3名
    );
```

### Kotlin 语法

```kotlin
val fetcher = DepartmentFetcher {
    name()
    employees({
        firstName()
        lastName()
    }, filter = {
        status() eq Status.ACTIVE
        limit(10)
    })
}
```

### ⚠️ 重要注意事项

1. **性能影响**：属性过滤器在数据库层面执行过滤，但如果关联数据量巨大，仍可能影响性能

2. **内存控制**：配合 `limit()` 使用可以有效控制内存占用

3. **与 Fetcher 的区别**：
   - Fetcher：决定**返回什么字段**
   - Property Filter：决定**返回哪些记录**

4. **排序限制**：在使用 `limit()` 时，建议同时指定排序，否则返回的结果可能不确定

### 典型应用场景

| 场景 | 解决方案 |
|------|----------|
| 用户消息列表只显示最新10条 | `filter -> filter.limit(10).orderBy(...desc())` |
| 只显示启用状态的数据 | `filter -> filter.status().eq(Status.ACTIVE)` |
| 动态过滤（根据权限） | 在 filter lambda 中加入条件判断 |
| 分页加载大量关联数据 | 配合 `limit()` 和 `offset()` 实现 |

## 递归抓取（自关联）

```java
// 部门层级：递归抓取所有子部门
Fetcher<Department> fetcher = DepartmentFetcher.$
    .name()
    .recursive(Department::subDepartments, 5);  // 最大深度5

// 评论树：无限深度递归
Fetcher<Comment> fetcher = CommentFetcher.$
    .content()
    .author(UserFetcher.$.name())
    .recursive(Comment::replies);  // 不指定深度，无限递归
```

## vs GraphQL

| 对比 | Jimmer Fetcher | GraphQL |
|------|----------------|---------|
| 使用范围 | ORM 基础 API，可在任何代码中使用 | 仅能在 HTTP 服务层面使用 |
| 递归查询 | 支持无限深度递归 | 不支持 |
| 类型安全 | 编译时检查 | 运行时检查 |

## 代码示例

- [基础示例](../../examples/fetch/basic-fetch-example.java) - Java
- [基础示例](../../examples/fetch/basic-fetch-example.kt) - Kotlin
