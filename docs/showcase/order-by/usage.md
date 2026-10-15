---
title: 5.1 基本用法
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/order-by/usage
---

# 5.1 基本用法

## 多个orderBy

**Java**

```java
BookTable table = BookTable.$;

List<Book> books = sqlClient
    .createQuery(table)

    .orderBy(table.name().asc())

    .orderBy(table.edition().desc())
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {

        orderBy(table.name().asc())

        orderBy(table.edition().desc())
        select(table)
    }
    .execute()
```

## orderBy带多个参数

还有另外一种写法与上述代码等价

**Java**

```java
BookTable table = BookTable.$;

List<Book> books = sqlClient
    .createQuery(table)

    .orderBy(
        table.name().asc(),
        table.edition().desc()
    )
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {

        orderBy(
            table.name.asc(),
            table.edition.desc()
        )
        select(table)
    }
    .execute()
```

然而，之前的写法更利于代码结构组织，故而更推荐之前的写法。

## 基于子查询的排序

**Java**

```java
BookTable table = BookTable.$;
AuthorTableEx author = AuthorTableEx.$;

List<Book> books = sqlClient
    .createQuery(table)

    .orderBy(
        sqlClient
            .createSubQuery(author)
            .where(author.books().eq(table))
            .select(Expression.rowCount())
            .desc()
    )
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {

        orderBy(
            subQuery(Author::class) {
                where(table.books eq parentTable)
                select(rowCount())
            }
            .desc()
        )
        select(table)
    }
    .execute()
```
