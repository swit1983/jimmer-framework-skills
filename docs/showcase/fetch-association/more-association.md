---
title: 2.2 更多关联
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/fetch-association/more-association
---

# 2.2 更多关联

## 查询动态实体

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
                .store( ❶
                    BookStoreFetcher.$
                        .allScalarFields()
                )
                .authors( ❷
                    AuthorFetcher.$
                        .allScalarFields()
                )
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
                store { ❶
                    allScalarFields()
                }
                authors { ❷
                    allScalarFields()
                }
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
        "store":{ ❶
            "id":1,
            "name":"O'REILLY",
            "website":null
        },
        "authors":[ ❷
            {
                "id":2,
                "firstName":"Alex",
                "lastName":"Banks",
                "gender":"MALE"
            },
            {
                "id":1,
                "firstName":"Eve",
                "lastName":"Procello",
                "gender":"FEMALE"
            }
        ]
    },
    {
        "id":2,
        "name":"Learning GraphQL",
        "edition":2,
        "price":55,
        "store":{ ❶
            "id":1,
            "name":"O'REILLY",
            "website":null
        },
        "authors":[ ❷
            {
                "id":2,
                "firstName":"Alex",
                "lastName":"Banks",
                "gender":"MALE"
            },
            {
                "id":1,
                "firstName":"Eve",
                "lastName":"Procello",
                "gender":"FEMALE"
            }
        ]
    },
    {
        "id":1,
        "name":"Learning GraphQL",
        "edition":1,
        "price":50,
        "store":{ ❶
            "id":1,
            "name":"O'REILLY",
            "website":null
        },
        "authors":[ ❷
            {
                "id":2,
                "firstName":"Alex",
                "lastName":"Banks",
                "gender":"MALE"
            },
            {
                "id":1,
                "firstName":"Eve",
                "lastName":"Procello",
                "gender":"FEMALE"
            }
        ]
    }
]
```

## 查询静态DTO

在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下

```sh
export com.yourcompany.yourproject.model.Book
    -> package com.yourcompany.yourproject.model.dto

BookView {
    #allScalars(this)
    store { ❶
        #allScalars(this)
    }
    authors { ❷
        #allScalars(this)
    }
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
        id=1,
        name=Learning GraphQL,
        edition=1,
        price=50.00,
        store=BookView.TargetOf_store( ❶
            id=1,
            name=O'REILLY,
            website=null
        ),
        authors=[ ❷
            BookView.TargetOf_authors(
                id=2,
                firstName=Alex,
                lastName=Banks,
                gender=MALE
            ),
            BookView.TargetOf_authors(
                id=1,
                firstName=Eve,
                lastName=Procello,
                gender=FEMALE
            )
        ]
    ),
    BookView(
        id=2,
        name=Learning GraphQL,
        edition=2,
        price=55.00,
        store=BookView.TargetOf_store( ❶
            id=1,
            name=O'REILLY,
            website=null
        ),
        authors=[ ❷
            BookView.TargetOf_authors(
                id=2,
                firstName=Alex,
                lastName=Banks,
                gender=MALE
            ),
            BookView.TargetOf_authors(
                id=1,
                firstName=Eve,
                lastName=Procello,
                gender=FEMALE
            )
        ]
    ),
    BookView(
        id=3,
        name=Learning GraphQL,
        edition=3,
        price=51.00,
        store=BookView.TargetOf_store( ❶
            id=1,
            name=O'REILLY,
            website=null
        ),
        authors=[ ❷
            BookView.TargetOf_authors(
                id=2,
                firstName=Alex,
                lastName=Banks,
                gender=MALE
            ),
            BookView.TargetOf_authors(
                id=1,
                firstName=Eve,
                lastName=Procello,
                gender=FEMALE
            )
        ]
    )
]
```
