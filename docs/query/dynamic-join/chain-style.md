---
title: '链式关联路径'
---
# 链式关联路径


Jimmer是如何解决动态连接问题将在后续文档中讨论，本文先介绍Jimmer中表连接的风格。


## 基本概念


首先，对于理论上存在但实际项目中几乎不用甚至需要小心避免笛卡尔乘积，Jimmer放弃支持。


这样，Jimmer SQL DSL就无需支持多个`from`子句，所有跨表操作全部统一为`join`。


Jimmer中，所有表连接的隐式的，由任意数量关联属性连接而成路径，即被被视为多个表连接。例如


- Java
- Kotlin


```
public List<Book> findBooksByStoreCityName(@Nullable String storeCityName) {    BookTable table = Tables.BOOK_TABLE;    return sqlClient        .createQuery(table)        .whereIf(            storeCityName != null && !storeCityName.isEmpty(),            table.store().city().name().eq(name)        )        .orderBy(table.name().asc(), table.edition().desc())        .select(table)        .execute();}
```


```
fun findBooksByStoreCityName(name: storeCityName?): List<Book> =    sqlClient        .createQuery(Book::class) {            storeCityName?.takeIf { it.isNotEmpty() }?.let {                where(table.store.city.name eq it)            }            orderBy(table.name.asc(), table.edition.desc())            select(table)        }        .execute()
```


信息

上面得代码中，Java的`table.store().city()`或Kotlin的`table.store.city`就是关联路径。


关联路径长度不受限制，其中的每个关联属性引用都对应一个SQL表连接操作。


```
from BOOK tb_1_inner join BOOK_STORE tb_2_ // `.store`    on tb_1_.STORE_ID = tb_2_.IDinner join CITY tb_3_ // `.city`    on tb_2_.CITY_ID = tb_3_.ID
```


- `findBooksByStoreCityName(null)`生成的SQL为


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.STORE_IDfrom BOOK tb_1_order by    tb_1_.NAME asc,    tb_1_.EDITION desc
```
- `findBooksByStoreCityName("ChengDu")`生成的SQL为


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.STORE_IDfrom BOOK tb_1_inner join BOOK_STORE tb_2_ // `.store`    on tb_1_.STORE_ID = tb_2_.IDinner join CITY tb_3_ // `.city`    on tb_2_.CITY_ID = tb_3_.IDwhere     // `.name eq "ChengDu"`    tb_3_.NAME = ? /* ChengDu */order by    tb_1_.NAME asc,    tb_1_.EDITION desc
```


## 外连接


上面的例子中，关联路径中`.store`和`.city`都表示内连接。


我们也很容易表达外连接，以左外连接为例


- Java
- Kotlin


```
table.store().city(JoinType.LEFT)
```


```
table.store.`city?`
```


警告

为了充分利用kotlin的语言优势充分优化其开发体验，Jimmer对Java和Kotlin提供不同的API，但二者本质相同。


然而，外连接却是唯一的例外，对于这个细节，Java API和Kotlin API的行为并不一样


- Java DSL
采用JoinType表示连接类型，可以是`INNER` *(默认)*, `LEFT`, `RIGHT`或`FULL`
- Kotlin DSL


- 和实体属性同名的DSL属性表示内连接
- 相比于实体属性名后面多了一个`?`的DSL属性表示左连接


即，Kotlin DSL不支持`RIGHT`和`FULL`，这种牺牲是仔细权衡后的结果，目的为了换取在对Kotlin而言更重要的功能：把kotlin的null safety和SQL DSL完美结合。


这个细节会在[Kotlin表连接特有功能](query/dynamic-join/kotlin-join)中详细讨论。
[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/dynamic-join/chain-style.mdx)最后 于 **2025年9月16日**  更新
- [基本概念](#基本概念)
- [外连接](#外连接)