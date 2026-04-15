---
title: '2.4 连接抓取'
---
# 2.4 连接抓取


默认情况下，Jimmer使用批量化的额外查询获取关联对象。


然而，对引用关联属性 *(即，非集合关联，`@ManyToOne`或`OneToOne`)* 而言，可以通过SQL的`left join`在查询当前对象时顺带查询关联对象


## 查询动态实体


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(table)    .where(table.name().eq("GraphQL in Action"))    .select(        table.fetch(            Fetchers.BOOK_FETCHER                .allScalarFields()                .store(                    ReferenceFetchType.JOIN_ALWAYS,                    Fetchers.BOOK_STORE_FETCHER                            .allScalarFields()                )        )    )    .execute();
```


```
val books = sqlClient.createQuery(Book::class) {    where(table.name eq "GraphQL in Action")    select(        table.fetchBy {             allScalarFields()            store(                ReferenceFetchType.JOIN_ALWAYS            ) {                allScalarFields()            }        }    )}
```


生成如下SQL


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_2_.ID,    tb_2_.NAME,    tb_2_.WEBSITEfrom BOOK tb_1_left join BOOK_STORE tb_2_    on tb_1_.STORE_ID = tb_2_.IDwhere    tb_1_.NAME = ? /* GraphQL in Action */
```


此功能只会影响对抓取关联对象的幕后机制，对上层功能没有影响，此处忽略返回的数据。


## 查询警静态DTO


在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下


Book.dto

```
export com.yourcompany.yourproject.model.BookStore    -> package com.yourcompany.yourproject.model.dtoBookView {    #allScalars(this)    !fetchType(JOIN_ALWAYS)    store {        #allScalars    }}
```


编译项目，生成Java/Kotlin类型BookView。


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;List<BookView> books = sqlClient    .createQuery(table)    .where(table.name().eq("GraphQL in Action"))    .select(        table.fetch(BookView.class)    )    .execute();
```


```
val books = sqlClient.createQuery(Book::class) {    where(table.name eq "GraphQL in Action")    select(        table.fetch(BookView::class)    )}
```


生成SQL已经在前文中介绍过，此处不再重复。


此功能只会影响对抓取关联对象的幕后机制，对上层功能没有影响，此处忽略返回的数据。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/fetch-association/join-fetch.mdx)最后 于 **2025年9月16日**  更新
- [查询动态实体](#查询动态实体)
- [查询警静态DTO](#查询警静态dto)