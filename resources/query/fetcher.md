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
// 抓取所有表字段：包含 allScalarFields + 基于外键的一对一/多对一属性（仅 id）
// 不包含：一对多、多对多、基于中间表的关联、计算属性、视图属性
Fetcher<Book> fetcher = BookFetcher.$.allTableFields();
// 等价于：id, name, edition, price, store(仅id)
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

属性过滤器用于为关联对象 *(而非主查询语句所针对的当前对象)* 设置 `where` 过滤条件和 `orderBy` 排序。

### 基本用法

#### Java

```java
BookTable table = Tables.BOOK_TABLE;

List<Book> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("GraphQL in Action"))
    .select(
        table.fetch(
            BookFetcher.$
                .allScalarFields()
                .authors(
                    AuthorFetcher.$.allScalarFields(),
                    cfg -> cfg.filter(args -> {
                        AuthorTable author = args.getTable();
                        args.where(
                            Predicate.or(
                                author.firstName().ilike("a"),
                                author.lastName().ilike("a")
                            )
                        );
                        args.orderBy(
                            author.firstName(),
                            author.lastName()
                        );
                    })
                )
        )
    )
    .execute();
```

#### Kotlin

```kotlin
val books = sqlClient.createQuery(Book::class) {
    where(table.name eq "GraphQL in Action")
    select(
        table.fetchBy {
            allScalarFields()
            authors({
                filter {
                    where(
                        or(
                            table.firstName ilike "a",
                            table.lastName ilike "a"
                        )
                    )
                    orderBy(
                        table.firstName,
                        table.lastName
                    )
                }
            }) {
                allScalarFields()
            }
        }
    )
}
```

### 效果说明

对于每一个返回的 `Book` 对象，其关联集合 `Book.authors` **可能不包含**数据库中所有关联对象，因为该关联集合被施加了属性级过滤。

### ⚠️ 重要说明

1. **作用对象**：属性过滤器针对的是**关联对象**，而非主查询的对象
2. **API 风格**：使用 `cfg.filter(args -> { ... })`，通过 `args.getTable()` 获取关联表，通过 `args.where()` 和 `args.orderBy()` 设置条件
3. **复杂条件**：支持使用 `Predicate.or()`、`Predicate.and()` 等组合复杂过滤条件
4. **与主查询的区别**：
   - 主查询 `where()`：过滤主对象（如 Book）
   - 属性过滤器：过滤关联集合（如 Book.authors）

### 与 DTO 配合使用

也可以在 DTO 中定义属性过滤器：

**Book.dto**
```
export com.yourcompany.yourproject.model.Book
    -> package com.yourcompany.yourproject.model.dto

BookDetailView {
    #allScalars

    !where(firstName ilike '%a%' or lastName ilike '%a%')
    !orderBy(firstName asc, lastName asc)
    authors {
        #allScalars
    }
}
```

使用时直接抓取：

```java
List<BookDetailView> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("GraphQL in Action"))
    .select(table.fetch(BookDetailView.class))
    .execute();
```

## 递归抓取（自关联）

对于自关联实体，Jimmer 自动生成递归抓取方法（Java：`recursiveXxx()`，Kotlin：`属性名*`）：

```java
// 部门层级：递归抓取所有子部门（最大深度5）
Fetcher<Department> fetcher = Fetchers.DEPARTMENT_FETCHER
    .name()
    .recursiveChildDepartments(it -> it.depth(5));  // 最大深度5

// 评论树：无限深度递归
Fetcher<Comment> fetcher = CommentFetcher.$
    .content()
    .author(UserFetcher.$.name())
    .recursiveReplies();  // 不指定深度，无限递归
```

```kotlin
// 部门层级
val fetcher = newFetcher(Department::class).by {
    name()
    `childDepartments*` {
        depth(5)
    }
}

// 评论树
val fetcher = newFetcher(Comment::class).by {
    content()
    author { name() }
    `replies*`()
}
```

详细用法参见[递归查询](recursive-fetch.md)。

## vs GraphQL

| 对比 | Jimmer Fetcher | GraphQL |
|------|----------------|---------|
| 使用范围 | ORM 基础 API，可在任何代码中使用 | 仅能在 HTTP 服务层面使用 |
| 递归查询 | 支持无限深度递归 | 不支持 |
| 类型安全 | 编译时检查 | 运行时检查 |

## 代码示例

- [基础示例](../../examples/fetch/basic-fetch-example.java) - Java
- [基础示例](../../examples/fetch/basic-fetch-example.kt) - Kotlin
