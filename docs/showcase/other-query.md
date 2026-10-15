---
title: 8. 其他查询
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/other-query
---

# 8. 其他查询

## 标准子查询

**Java**

```java
BookTable book = BookTable.$;

List<Book> newestBooks = sqlClient
    .createQuery(book)
    .where(
        Expression.tuple(
            book.name(),
            book.edition()
        ).in(sqlClient
            .createSubQuery(book)
            .groupBy(book.name())
            .select(
                book.name(),
                book.edition().max()
            )
        )
    )
    .select(book)
    .execute();
```

**Kotlin**

```kotlin
val newestBooks = sqlClient
    .createQuery(Book::class) {
        where(
            tuple(
                table.name,
                table.edition
            ) valueIn subQuery(Book::class) {
                groupBy(table.name)
                select(
                    table.name,
                    max(table.edition).asNonNull()
                )
            }
        )
        select(table)
    }
    .execute()
```

## Native SQL

为了支持特有数据库产品特有的能力，Jimmer的SQL DSL支持嵌入Native SQL表达式。以正则表达式查询为例。

**Java**

```java
AuthorTable table = Tables.AUTHOR_TABLE;

List<Author> authors = sqlClient
    .createQuery(table)
    .where(
        Predicate.sql(
            "regexp_like(%e, %v)",
            it -> it
                .expression(table.firstName())
                .value("^Ste(v|ph)en$")
        )
    )
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val authors = sqlClient
    .createQuery(Author::class) {
        where(
            sql(Boolean::class, "regexp_like(%e, %v)") {
                expression(table.firstName)
                value("^Ste(v|ph)en$")
            }
        )
        select(table)
    }
    .execute()
```

-   Native SQL片段中的`%e`表示可以嵌入一个强类型的Jimmer DSL表达式。

    Lambda中的`expression(...)`指定用于替换`%e`的表达式。

-   Native SQL片段中的`%v`表示可以嵌入一个字面量。

    Lambda中的`value(...)`指定用于替换`%v`的字面量。
