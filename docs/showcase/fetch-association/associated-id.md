---
title: 2.4 关联Id
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/fetch-association/associated-id
---

# 2.4 关联Id

和之前的例子不同，这里我们只需要关联对象id，不需要整个关联对象

## 查询动态实体

### 没有IdView属性时

**Java**

```java
BookTable table = BookTable.$;

List<Book> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("Learning GraphQL"))
    .orderBy(table.edition().desc())
    .select(
        table.fetch(
            BookFetcher.$
                .allScalarFields()

                .authors()
        )
    )
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        where(table.name  eq "Learning GraphQL")
        orderBy(table.edition.desc())
        select(
            table.fetchBy {
                allScalarFields()

                authors()
            }
        )
    }
    .execute()
```

得到的结果为

```json
[
    {
        "id":3,
        "name":"Learning GraphQL",
        "edition":3,
        "price":51,
        "authors":[

            { "id":2 },

            { "id":1 }
        ]
    },
    {
        "id":2,
        "name":"Learning GraphQL",
        "edition":2,
        "price":55,
        "authors":[

            { "id":2 },

            { "id":1 }
        ]
    },
    {
        "id":1,
        "name":"Learning GraphQL",
        "edition":1,
        "price":50,
        "authors":[

            { "id":2 },

            { "id":1 }
        ]
    }
]
```

这会导致大量的只有id属性的对象 *(熟悉JPA的用户可以将其理解为实体代理)*，虽然是正确的，但未必是开发人员喜欢的格式，下一节我们讨论如何解决这个问题

### 有IdView属性时

为了解决上面的问题，我们可以为实体添加[@IdView](../../mapping/advanced/view/id-view.md)属性，即关联id属性

**Java**

```java
@Entity
public interface Book {

    @ManyToMany
    List<Author> authors();

    @IdView("authors")
    List<Long> authorIds();

    ....省略其他属性...
}
```

**Kotlin**

```kotlin
@Entity
interface Book {

    @ManyToMany
    val authors: List<Author>

    @IdView("authors")
    val authorIds: List<Long>

    ....省略其他属性...
}
```

这里，`authorIds`属性是`authors`属性的视图，二者共享数据。所以，并没有改变实体的格式，只是让表达方式多样化了

:::info
这里仅作案例展示，不对@IdView属性做深入探讨，清参考[这里](../../mapping/advanced/view/id-view.md)
:::

**Java**

```java
BookTable table = BookTable.$;

List<Book> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("Learning GraphQL"))
    .orderBy(table.edition().desc())
    .select(
        table.fetch(
            BookFetcher.$
                .allScalarFields()

                .authorIds()
        )
    )
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        where(table.name  eq "Learning GraphQL")
        orderBy(table.edition.desc())
        select(
            table.fetchBy {
                allScalarFields()

                authorIds()
            }
        )
    }
    .execute()
```

得到如下的数据

```json
[
    {
        "id":1,
        "name":"Learning GraphQL",
        "edition":1,
        "price":50,

        "authorIds":[2, 1]
    },
    {
        "id":2,
        "name":"Learning GraphQL",
        "edition":2,
        "price":55,

        "authorIds":[2, 1]
    },
    {
        "id":3,
        "name":"Learning GraphQL",
        "edition":3,
        "price":51,

        "authorIds":[2, 1]
    }
]
```

## 查询静态DTO

无需在实体中定义@IdView属性，在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下

```sh
export com.yourcompany.yourproject.model.Book
    -> package com.yourcompany.yourproject.model.dto

BookView {
    #allScalars

    id(authors) as authorIds
}
```

编译项目，生成Java/Kotlin类型`BookView`

**Java**

```java
BookTable table = BookTable.$;

List<BookView> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("Learning GraphQL"))
    .orderBy(table.edition().desc())
    .select(
        table.fetch(BookView.class)
    )
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        where(table.name  eq "Learning GraphQL")
        orderBy(table.edition.desc())
        select(
            table.fetch(BookView::class)
        )
    }
    .execute()
```

得到如下结果

```json
[
    BookView(
        id=3,
        name=Learning GraphQL,
        edition=3,
        price=51.00,
        authorIds=[2, 1]
    ),
    BookView(
        id=2,
        name=Learning GraphQL,
        edition=2,
        price=55.00,
        authorIds=[2, 1]
    ),
    BookView(
        id=1,
        name=Learning GraphQL,
        edition=1,
        price=50.00,
        authorIds=[2, 1]
    )
]
```
