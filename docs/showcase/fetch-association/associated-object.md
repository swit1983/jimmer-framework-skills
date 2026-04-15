---
title: '2.1 关联对象'
---
# 2.1 关联对象


## 基于动态实体


- Java
- Kotlin


```
BookTable table = BookTable.$;List<Book> books = sqlClient    .createQuery(table)    .where(table.name().eq("Learning GraphQL"))    .orderBy(table.edition().desc())    .select(        table.fetch(            BookFetcher.$                .allScalarFields()                .authors(                    AuthorFetcher.$                        .allScalarFields()                )        )    )    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        where(table.name  eq "Learning GraphQL")        orderBy(table.edition.desc())        select(            table.fetchBy {                allScalarFields()                authors {                    allScalarFields()                }            }        )    }    .execute()
```


得到的结果为


```
[    {        "id":3,        "name":"Learning GraphQL",        "edition":3,        "price":51,        "authors":[            {                "id":2,                "firstName":"Alex",                "lastName":"Banks",                "gender":"MALE"            },            {                "id":1,                "firstName":"Eve",                "lastName":"Procello",                "gender":"FEMALE"            }        ]    },    {        "id":2,        "name":"Learning GraphQL",        "edition":2,        "price":55,        "authors":[            {                "id":2,                "firstName":"Alex",                "lastName":"Banks",                "gender":"MALE"            },            {                "id":1,                "firstName":"Eve",                "lastName":"Procello",                "gender":"FEMALE"            }        ]    },    {        "id":1,        "name":"Learning GraphQL",        "edition":1,        "price":50,        "authors":[            {                "id":2,                "firstName":"Alex",                "lastName":"Banks",                "gender":"MALE"            },            {                "id":1,                "firstName":"Eve",                "lastName":"Procello",                "gender":"FEMALE"            }        ]    }]
```


## 查询静态DTO


在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下


```
export com.yourcompany.yourproject.model.Book    -> package com.yourcompany.yourproject.model.dtoBookView {    #allScalars(this)    authors {        #allScalars(this)    }}
```


编译项目，生成Java/Kotlin类型`BookView`


- Java
- Kotlin


```
BookTable table = BookTable.$;List<BookView> books = sqlClient    .createQuery(table)    .where(table.name().eq("Learning GraphQL"))    .orderBy(table.edition().desc())    .select(        table.fetch(BookView.class)    )    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        where(table.name  eq "Learning GraphQL")        orderBy(table.edition.desc())        select(            table.fetch(BookView::class)        )    }    .execute()
```


得到如下结果


```
[    BookView(        id=3,         name=Learning GraphQL,         edition=3,         price=51.00,         authors=[            BookView.TargetOf_authors(                id=2,                 firstName=Alex,                 lastName=Banks,                 gender=MALE            ),             BookView.TargetOf_authors(                id=1,                 firstName=Eve,                 lastName=Procello,                 gender=FEMALE            )        ]    ),     BookView(        id=2,         name=Learning GraphQL,         edition=2,         price=55.00,         authors=[            BookView.TargetOf_authors(                id=2,                 firstName=Alex,                 lastName=Banks,                 gender=MALE            ),             BookView.TargetOf_authors(                id=1,                 firstName=Eve,                 lastName=Procello,                 gender=FEMALE            )        ]    ),     BookView(        id=1,         name=Learning GraphQL,         edition=1,         price=50.00,         authors=[            BookView.TargetOf_authors(                id=2,                 firstName=Alex,                 lastName=Banks,                 gender=MALE            ),             BookView.TargetOf_authors(                id=1,                 firstName=Eve,                 lastName=Procello,                 gender=FEMALE            )        ]    )]
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/fetch-association/associated-object.mdx)最后 于 **2025年9月16日**  更新
- [基于动态实体](#基于动态实体)
- [查询静态DTO](#查询静态dto)