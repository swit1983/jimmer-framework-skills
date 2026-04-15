---
title: '2.5 属性过滤器'
---
# 2.5 属性过滤器


Jimmer支持属性级别过滤器，为关联对象 *(而非主查询语句所针对的当前对象)* 设置`where`过滤条件和`orderBy`排序。


## 查询动态实体


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(table)    .where(table.name().eq("GraphQL in Action"))    .select(        table.fetch(            Fetchers.BOOK_FETCHER                .allScalarFields()                .authors(                    Fetchers.AUTHOR_FETCHER                        .allScalarFields(),                    cfg -> cfg.filter(args -> {                        AuthorTable author = args.getTable();                        args.where(                            Predicate.or(                                author.firstName().ilike("a"),                                author.lastName().ilike("a")                            )                        );                        args.orderBy(                            author.firstName(),                            author.lastName()                        );                    })                )        )    )    .execute();
```


```
val books = sqlClient.createQuery(Book::class) {    where(table.name eq "GraphQL in Action")    select(        table.fetchBy {            allScalarFields()            authors({                filter {                    where(                        or(                            table.firstName ilike "a",                            table.lastName ilike "a"                        )                    )                    orderBy(                        table.firstName,                        table.lastName                    )                }            }) {                allScalarFields()            }        }    )}
```


对于每一个返回的`Book`对象而言，其关联集合`Book.authors`都有可能不包含数据库所有关联对象，因为该关联集合被施加了属性级过滤。


## 查询静态DTO


在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下


Book.dto

```
export com.yourcompany.yourproject.model.Book     -> package com.yourcompany.yourproject.model.dtoBookDetailView {    #allScalars    !where(firstName ilike '%a%' or lastName ilike '%a%')    !orderBy(firstName asc, lastName asc)    authors {        #allScalars    }}
```


编译，自动生成Java/Kotlin类型`BookDetailView`


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;List<BookDetailView> books = sqlClient    .createQuery(table)    .where(table.name().eq("GraphQL in Action"))    .select(        table.fetch(BookDetailView.class)    )    .execute();
```


```
val books = sqlClient.createQuery(Book::class) {    where(table.name eq "GraphQL in Action")    select(        table.fetch(BookDetailView::class)    )}
```


对于每一个返回的`Book`对象而言，其关联集合`Book.authors`都有可能不包含数据库所有关联对象，因为该关联集合被施加了属性级过滤。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/fetch-association/prop-filter.mdx)最后 于 **2025年9月16日**  更新
- [查询动态实体](#查询动态实体)
- [查询静态DTO](#查询静态dto)