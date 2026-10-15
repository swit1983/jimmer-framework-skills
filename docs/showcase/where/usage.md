---
title: 4.1 基本用法
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/where/usage
---

# 4.1 基本用法

以下三种写法等价

## 使用多个where

**Java**

```java
String name = ...略...;
int edition = ...略...;

BookTable table = BookTable.$;
List<Book> books = sqlClient
    .createQuery(table)

    .where(table.name().ilike(name))

    .where(table.edition().eq(edition))
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val name: String = ...略...
val edition: Int = ...略...

val books = sqlClient
    .createQuery(Book::class) {

        where(table.name ilike name)

        where(table.edition eq edition)
        select(table)
    }
    .execute()
```

## where带多个参数

**Java**

```java
String name = ...略...;
int edition = ...略...;

BookTable table = BookTable.$;
List<Book> books = sqlClient
    .createQuery(table)

    .where(
        table.name().ilike(name),
        table.edition().eq(edition)
    )
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val name: String = ...略...
val edition: Int = ...略...

val books = sqlClient
    .createQuery(Book::class) {

        where(
            table.name ilike name,
            table.edition eq edition
        )
        select(table)
    }
    .execute();
```

## 使用逻辑与

**Java**

```java
String name = ...略...;
int edition = ...略...;

BookTable table = BookTable.$;
List<Book> books = sqlClient
    .createQuery(table)
    .where(

        Predicate.and(
            table.name().ilike(name),
            table.edition().eq(edition)
        )
    )
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val name: String = ...略...
val edition: Int = ...略...

val books = sqlClient
    .createQuery(Book::class) {
        where(

            and(
                table.name ilike name,
                table.edition eq edition
            )
        )
        select(table)
    }
    .execute()
```

## 建议

以上三种写法完全等价。然而，毫无疑问，第一种是最简单的，故而推荐。
