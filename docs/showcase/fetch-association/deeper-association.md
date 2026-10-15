---
title: 2.3 更深关联
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/fetch-association/deeper-association
---

# 2.3 更深关联

## 查询动态实体

**Java**

```java
BookStoreTable table = BookStoreTable.$;

List<BookStore> stores = sqlClient
    .createQuery(table)
    .where(table.name().eq("MANNING"))
    .select(
        table.fetch(
            BookStoreFetcher.$
                .allScalarFields()
                .books( ❶
                    BookFetcher.$
                        .allScalarFields()
                        .authors( ❷
                            AuthorFetcher.$
                                .allScalarFields()
                        )
                )
        )
    )
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(BookStore::class) {
        where(table.name  eq "MANNING")
        select(
            table.fetchBy {
                allScalarFields()
                books { ❶
                    allScalarFields()
                    authors { ❷
                        allScalarFields()
                    }
                }
            }
        )
    }
    .execute()
```

得到如下的数据

```json
[
    {
        "id":2,
        "name":"MANNING",
        "website":null,
        "books":[ ❶
            {
                "id":12,
                "name":"GraphQL in Action",
                "edition":3,
                "price":80,
                "authors":[ ❷
                    {
                        "id":5,
                        "firstName":"Samer",
                        "lastName":"Buna",
                        "gender":"MALE"
                    }
                ]
            },
            {
                "id":11,
                "name":"GraphQL in Action",
                "edition":2,
                "price":81,
                "authors":[ ❷
                    {
                        "id":5,
                        "firstName":"Samer",
                        "lastName":"Buna",
                        "gender":"MALE"
                    }
                ]
            },
            {
                "id":10,
                "name":"GraphQL in Action",
                "edition":1,
                "price":82,
                "authors":[ ❷
                    {
                        "id":5,
                        "firstName":"Samer",
                        "lastName":"Buna",
                        "gender":"MALE"
                    }
                ]
            }
        ]
    }
]
```

## 查询静态DTO

在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下

```sh
export com.yourcompany.yourproject.model.BookStore
    -> package com.yourcompany.yourproject.model.dto

BookStoreView {
    #allScalars(this)
    books { ❶
        #allScalars(this)
        authors { ❷
            #allScalars(this)
        }
    }
}
```

编译项目，生成Java/Kotlin类型`BookStoreView`

**Java**

```java
BookStoreTable table = BookStoreTable.$;

List<BookStoreView> stores = sqlClient
    .createQuery(table)
    .where(table.name().eq("MANNING"))
    .select(
        table.fetch(BookStoreView.class)
    )
    .execute();
```

**Kotlin**

```kotlin
val stores = sqlClient
    .createQuery(BookStore::class) {
        where(table.name  eq "Learning GraphQL")
        select(
            table.fetch(BookStoreView::class)
        )
    }
    .execute()
```

得到如下结果

```json
[
    BookStoreView(
        id=2,
        name=MANNING,
        website=null,
        books=[
            BookStoreView.TargetOf_books(
                id=12,
                name=GraphQL in Action,
                edition=3,
                price=80.00,
                authors=[
                    BookStoreView.TargetOf_books.TargetOf_authors_2(
                        id=5,
                        firstName=Samer,
                        lastName=Buna,
                        gender=MALE
                    )
                ]
            ),
            BookStoreView.TargetOf_books(
                id=11,
                name=GraphQL in Action,
                edition=2,
                price=81.00,
                authors=[
                    BookStoreView.TargetOf_books.TargetOf_authors_2(
                        id=5,
                        firstName=Samer,
                        lastName=Buna,
                        gender=MALE
                    )
                ]
            ),
            BookStoreView.TargetOf_books(
                id=10,
                name=GraphQL in Action,
                edition=1,
                price=82.00,
                authors=[
                    BookStoreView.TargetOf_books.TargetOf_authors_2(
                        id=5,
                        firstName=Samer,
                        lastName=Buna,
                        gender=MALE
                    )
                ]
            )
        ]
    )
]
```
