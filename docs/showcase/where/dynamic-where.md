---
title: 4.2 动态条件
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/where/dynamic-where
---

# 4.2 动态条件

## 使用whereIf

**Java**

```java
@Nullable String name = ...略...;
@Nullable Integer edition = ...略...;

BookTable table = BookTable.$;
List<Book> books = sqlClient
    .createQuery(table)

    .whereIf(
        name != null && !name.isEmpty(),
        () -> table.name().ilike(name)
    )

    .whereIf(
        edition != null,
        table.edition().eq(edition)
    )
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val name: String? = ...略...
val edition: Int? = ...略...

val books = sqlClient
    .createQuery(Book::class) {

        name?.takeIf { it.isNotEmpty() }?.let {
            where(table.name ilike it)
        }

        edition?.let {
            where(table.edition eq it)
        }
        select(table)
    }
    .execute()
```

:::caution
Java代码中，第一个whereIf采用Lambda传入表达式。

这是因为除了`eq`和`ne`能接受null值*(翻译为`is null`和`is not null`)*外，
其他条件*(比如，这里的`ilike`)*不接受null并视之为开发人员犯下的BUG。
:::

## 使用动态谓词

`whereIf`并非唯一的动态查询的写法，上述代码可以用另外一种写法来代替。

**Java**

```java
@Nullable String name = ...略...;
@Nullable Integer edition = ...略...;

BookTable table = BookTable.$;
List<Book> books = sqlClient
    .createQuery(table)

    .where(table.name().ilikeIf(name))

    .whereIf(table.edition().eqIf(edition))
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val name: String? = ...略...
val edition: Int? = ...略...

val books = sqlClient
    .createQuery(Book::class) {

        where(table.name `ilike?` name)

        where(table.edition `eq?` edition)
        select(table)
    }
    .execute()
```

:::tip
大部分SQL条件*(比如，这里的`ilike`)*不接受null，并视之为开发人员犯下的BUG。*(`eq`和`ne`除外，因为它们会将null渲染为`is null`和`not null`)*

然而，动态谓词 *(Java中`ilikeIf`和Kotlin中的`ilike?`)* 则完全不同，它们接受null值并将之理解为动态查询。即，如果参数为null或""，则忽略当前SQL条件。
:::

## 比较

虽然`whereIf`具备良好的可读性，但是动态谓词能适应更复杂的场景 *(可以在复杂的条件中使用，包括嵌套多层的and, not, or结构)*。后文中我们一律使用动态谓词。
