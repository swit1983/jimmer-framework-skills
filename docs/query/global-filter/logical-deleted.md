---
title: '逻辑删除'
---
# 逻辑删除

信息

在本文中，读者无法发现和全局过滤器相关的内容，这是因为逻辑删除所需要的过滤器是内置的，且被Jimmer隐藏起来了。


## 映射


逻辑删除也称软删除，表示并不真正从数据库中删除数据，而是通过隐藏数据来达到数据被删除的假象。这可以为误操作留下一次恢复的机会。


逻辑删除相关的实体映射，在[映射篇/进阶映射/逻辑删除](mapping/advanced/logical-deleted/entity)有非常详细的介绍，这里不再重复所有细节，仅做一个简要回顾


- Java
- Kotlin
Book.java

```
@Entitypublic interface Book {    @LogicalDeleted("true")    boolean isDeleted();    ...省略其他代码...}
```

Book.kt

```
@Entityinterface Book {    @LogicalDeleted("true")    val isDeleted: Boolean    ...省略其他代码...}
```


## 使用


### 过滤聚合根


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(table)    .select(table)    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        select(table)    }    .execute()
```


生成的SQL为


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.DELETED,    tb_1_.STORE_IDfrom BOOK tb_1_where    tb_1_.DELETED <> ? /* true */
```


### 过滤关联对象


- Java
- Kotlin


```
AuthorTable author = Tables.AUTHOR_TABLE;List<Author> authors = sqlClient    .createQuery(author)    .select(        author.fetch(            Fetchers.AUTHOR_FETCHER                .allScalarFields()                .books(                    Fetchers.BOOK_FETCHER                        .allScalarFields()                )        )    )    .execute();
```


```
val authors = sqlClient    .createQuery(Author::class) {        select(            table.fetchBy {                allScalarFields()                books {                    allScalarFields()                }            }        )    }    .execute()
```


在未启用缓存的情况下，这会生成两条SQL


- 查询聚合根


```
select    tb_1_.ID,    tb_1_.FIRST_NAME,    tb_1_.LAST_NAME,    tb_1_.GENDERfrom AUTHOR tb_1_
```
- 查询关联对象，应用逻辑删除过滤器


```
select    tb_2_.AUTHOR_ID,    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICEfrom BOOK tb_1_inner join BOOK_AUTHOR_MAPPING tb_2_    on tb_1_.ID = tb_2_.BOOK_IDwhere        tb_2_.AUTHOR_ID in (            ? /* 1 */, ? /* 2 */, ? /* 3 */, ? /* 4 */, ? /* 5 */        )    and        tb_1_.DELETED <> ? /* true */
```


## 忽略逻辑删除过滤器


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;List<Book> books = sqlClient    .filters(cfg -> { ❶        cfg.setBehavior(LogicalDeletedBehavior.IGNORED); ❷    })    .createQuery(table)    .select(table)    .execute();
```


```
val books = sqlClient    .filters { ❶        setBehavior(LogicalDeletedBehavior.IGNORED)    }    .createQuery(table)    .select(table)    .execute()
```


- ❶ 在不影响当前`sqlClient`的前提下，调整过滤器配置，创建新的临时`sqlClient`
- ❷ 忽略软删除标志


这次，生成的SQL不再包含


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.DELETED,    tb_1_.STORE_IDfrom BOOK tb_1_
```


## 反转逻辑删除过滤器


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;List<Book> books = sqlClient    .filters(cfg -> { ❶        cfg.setBehavior(LogicalDeletedBehavior.REVERSED); ❷    })    .createQuery(table)    .select(table)    .execute();
```


```
val books = sqlClient    .filters { ❶        setBehavior(LogicalDeletedBehavior.REVERSED)    }    .createQuery(table)    .select(table)    .execute()
```


- ❶ 在不影响当前`sqlClient`的前提下，调整过滤器配置，创建新的临时`sqlClient`
- ❷ 反转软删除标志，即查询被删除的数据


再次执行，生成的SQL为


生成的SQL为


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.DELETED,    tb_1_.STORE_IDfrom BOOK tb_1_where    tb_1_.DELETED = ? /* true */
```


这次过滤条件为`tb_1_.DELETED = true`，即查询已经被删除数据，和默认的过滤规则刚好相反。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/global-filter/logical-deleted.mdx)最后 于 **2025年9月16日**  更新
- [映射](#映射)
- [使用](#使用)
- [过滤聚合根](#过滤聚合根)
- [过滤关联对象](#过滤关联对象)
- [忽略逻辑删除过滤器](#忽略逻辑删除过滤器)
- [反转逻辑删除过滤器](#反转逻辑删除过滤器)