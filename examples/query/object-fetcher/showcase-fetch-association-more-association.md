# 2.2 更多关联

> 来源: https://jimmer.deno.dev/zh/docs/showcase/fetch-association/more-association

* [案例展示 ★](/zh/docs/showcase/)
* [2. 抓取关联](/zh/docs/showcase/fetch-association/)
* 2.2 更多关联

本页总览

# 2.2 更多关联

## 查询动态实体[​](#查询动态实体 "查询动态实体的直接链接")

* Java
* Kotlin

```
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

```
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

```
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

## 查询静态DTO[​](#查询静态dto "查询静态DTO的直接链接")

在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下

```
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

* Java
* Kotlin

```
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

```
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

```
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

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/fetch-association/more-association.mdx)

最后于 **2025年9月16日** 更新