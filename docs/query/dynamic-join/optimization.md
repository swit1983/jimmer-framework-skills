---
title: '优化不必要连接'
---
# 优化不必要连接


## 使用方法


### 基本概念


在Jimmer SQL DSL 中，被创建的join对象，如果不被使用将会被忽略。例如


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;System.out.println("Unused join: " + table.store());List<Book> books = sqlClient    .createQuery(table)    .orderBy(table.name().asc(), table.edition().desc())    .select(table)    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {                println("Unused join: ${table.store}");        orderBy(table.name.asc(), table.edition.desc())        select(table)    }    .execute()
```


虽然通过`table.store`创建了一个JOIN对象，但是，该对象并没有被当前查询的SQL DSL使用，这种情况下，该JOIN对象会被忽略。


因此，最终生成的SQL不会包含任何JOIN操作


```
select     tb_1_.ID,     tb_1_.NAME,     tb_1_.EDITION,     tb_1_.PRICE,     tb_1_.STORE_ID from BOOK as tb_1_     where tb_1_.NAME = ?
```


### 另外一种解决方案


在

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}目前技术方案存在问题@media print{.css-1k371a6{position:absolute!important;}}中，我们列举了两个场景。


在[上一篇文档](query/dynamic-join/merge)中，我们讨论了利用Jimmer SQL DSL自动合并冲突连接的特性解决这两个场景的问题。


这里，我们换用另外一种方式来解决相同的问题。


- 解决场景1的问题


- Java
- Kotlin


```
List<Book> findBooks(        @Nullable String name,        @Nullable String storeName,        @Nullable String storeWebsite) {    BookTable table = Tables.BOOK_TABLE;    // 先无条件建立JOIN对象，若未被后续代码使用，    // 则被自动忽略。因此，此举不会导致无用JOIN    BookStoreTable store = table.store();    return sql()        .createQuery(table)        .whereIf(            name != null,            () -> book.name().like(name)        )        .whereIf(            storeName != null,            () -> store .name().like(storeName)        )        .whereIf(            storeWebsite != null,            () -> store.website().like(storeWebsite)        )        .select(book)        .execute();}
```


```
fun findBooks(    name: String? = null,    storeName: String? = null,    storeWebsite: String? = null): List<Book> {    return sqlClient        .createQuery(Book::class) {            // 先无条件建立JOIN对象，若未被后续代码使用，            // 则被自动忽略。因此，此举不会导致无用JOIN            val store = table.store;            name?.let {                where(table.name like it)            }            storeName?.let {                where(store .name like it)            }            storeName?.let {                where(store.website like it)            }            select(table)        }        .execute()}
```


- 如果仅指定`name`，不指定`storeName`和`storeWebsite`，那么`store`就是一个创建后却不被使用的join对象，因此被忽略。最终生成的SQL不包含任何join操作。


```
select     tb_1_.ID,     tb_1_.NAME,     tb_1_.EDITION,     tb_1_.PRICE,     tb_1_.STORE_ID from BOOK as tb_1_     where tb_1_.NAME = ?
```
- 如果指定`storeName`和`storeWebsite`，`store`就会被使用，从而导致最终生成SQL包含join操作。这很明显，此处不用示范。
- 解决场景2的问题


- Java
- Kotlin


```
List<Long> findDistinctIds(    @Nullable Long aId,    @Nullable Long bId,    @Nullable Long cId,    @Nullable Long dId,    @Nullable Long eId) {    ATable table = Tables.A_TABLE;    // 先无条件建立JOIN对象，若未被后续代码使用，    // 则被自动忽略。因此，此举不会导致无用JOIN    BTableEx b = table.asTableEx().bs();    CTableEx c = b.cs();    DTableEx d = c.ds();    DTableEx e = d.es();    return sqlClient        .createQuery(table)        .whereIf(            aId != null,            () -> table.id().like(aId)        )        .whereIf(            bId != null,            () -> b.id().like(bId)        )        .whereIf(            cId != null,            () -> c.id().like(cId)        )        .whereIf(            dId != null,            () -> d.id().like(dId)        )        .whereIf(            eId != null,            () -> e.id().like(eId)        )        .select(book.id())        .distinct()        .execute();}
```


```
fun findDistinctIds(    aId: Long? = null,    bId: Long? = null,    cId: Long? = null,    dId: Long? = null,    eId: Long? = null): List<Long> {        // 先无条件建立JOIN对象，若未被后续代码使用，    // 则被自动忽略。因此，此举不会导致无用JOIN    val b = table.asTableEx().bs    val c = b.cs    val d = c.ds    val e = d.es    return sqlClient        .createQuery(A::class) {            aId?.let {                where(table.id eq it)            }            bId?.let {                where(b.id eq it)            }            cId?.let {                where(c.id eq it)            }            dId?.let {                where(d.id eq it)            }            eId?.let {                where(e.id eq it)            }            select(table.id)        }        .distinct()        .execute()}
```


信息
- 有了前面的基础，这里不再需要罗列不同的参数组合下会生成何种SQL。明白无论如何最终SQL都不会包含无用连接操作即可。
- 某个JOIN对象被SQL DSL使用时，会将它标记成“被使用”以防止它被忽略。这种标记具备传递性。


例如：将`d`标记成“被使用”，自然也会标记`c`、`b`和`table`
- 这里的`asTableEx` *(以及Java下的各种TableEx类型)* 是后续文档[分页安全性](query/dynamic-join/table-ex)要介绍的概念。这里，请读者先忽略它。


## 幻连接


### 判断关联id的两种方法


对于基于外键的关联而言，有如下两种办法对关联id进行筛查：


1. 直接使用外键属性


- Java
- Kotlin


```
BookTable book = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(book)    .where(        book        .storeId()        .eq(2L)    )    .select(book)    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        where(            table            .storeId            eq 2L        )        select(table)    }    .execute()
```


提示

对于一对一/多对一关联而言 *(这个例子的`Book.store`)而言，
*即使用户不为`Book`实体声明[@IdView](mapping/advanced/view/id-view)属性，
SQL DSL中也可以使用`storeId`属性。
2. 先通过join操作连接到关联对象，再访问id属性


- Java
- Kotlin


```
BookTable book = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(book)    .where(        book        .store()        .id() // 只访问id        .eq(2L)    )    .select(book)    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        where(            table            .store            .id // 只访问id            eq 2L        )        select(table)    }    .execute()
```


- 这两种方法，并不等价


1. `where(table.storeId().eq(2L))`：简单地根据当前表的外键字段进行过滤。
2. `where(table.store().id().eq(2L))`：真正先连接到关联表，再判断关联id的值。


二者不等价，是因为存在以下的情况


- 如果外键是假的，即数据中没有相应的外键约束。这会导致非null的伪外键无法通过连接获取非null的关联对象。
- 如果关联对象受[全局过滤器](query/global-filter)影响，即使数据库中存在关联对象，也应该视而不见。
- 然而，当以下所有条件同时满足时，上述两种方法是完全等价的。


- 关联属性是基于外键的一对一或多对一关联，而非基于中间表，或其他关联的反向映射。
- 外键是真的，即数据中存在对应的外键约束。请参见[真假外键](mapping/base/foreignkey)
- 关联对象不受[全局过滤器](query/global-filter)影响。


注意：这里指以下两种过滤器中任何一种


- [用户自定义过滤器](query/global-filter/user-filter)
- [逻辑删除过滤器](query/global-filter/logical-deleted)


此时，关联对象的id，其实就是当前表的外键，二者等价。

信息

开发人员应该明白这两种方法的差异，按照业务需求选择正确的方式。


然而，当两种方法等价时，自动将第二种方法优化成第一种方法是一件非常划算的事，这就叫做幻连接优化。


两种查询方法等价，是幻连接优化能生效的必要条件，下文不再重复。


### 不适用的场景


幻连接优化仅针对对关联对象的id属性的访问，不支持其他属性。


这里，以关联对象的`name`属性为例，来演示无法去掉join。


- Java
- Kotlin


```
BookTable book = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(book)    .where(book.store().name().eq("MANNING"))    .select(book)    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        where(table.store.name eq "MANNING")        select(table)        }    .execute()
```


生成的SQL如下：


```
select     tb_1_.ID,     tb_1_.NAME,     tb_1_.EDITION,     tb_1_.PRICE,     tb_1_.STORE_ID from BOOK as tb_1_     inner join BOOK_STORE as tb_2_         on tb_1_.STORE_ID = tb_2_.IDwhere     tb_2_.NAME = ?
```


### 适用的场景


如果仅访问关联对象的id属性，则能启动幻连接优化。例如：


- Java
- Kotlin


```
BookTable book = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(book)    .where(        book        .store()        .id() // 只访问id        .eq(2L)    )    .select(book)    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        where(            table            .store            .id // 只访问id            eq 2L        )        select(table)    }    .execute()
```


这次，生成的SQL如下：


```
select     tb_1_.ID,     tb_1_.NAME,     tb_1_.EDITION,     tb_1_.PRICE,     tb_1_.STORE_ID from BOOK as tb_1_     where tb_1_.STORE_ID = ?
```


我们没有在SQL中看到任何表连接，我们只看到条件一个基于外键的判断条件`tb_1_.STORE_ID = ?`。


原因：对于基于外键映射的多对一关联而言，父表的id其实就是子表自己的外键。


## 半连接


半连接是一个 和幻象连接类似的概念，但用于基于中间表的关联。


### 判断关联id的两种方法


对于基于中间表的关联而言，有如下两种办法对关联id进行筛查：


1. 判断中间表中的外键字段


- Java
- Kotlin


```
BookTable book = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(book)    .where(        book        .asTableEx()        .authorIds()        .eq(2L)    )    .select(book)    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        where(            table            .asTableEx()            .authorIds            eq 2L        )        select(table)    }    .execute()
```


备注

例子中的`asTableEx`，并无实质性的功能，将会在下一篇文档[分页安全性](query/dynamic-join/table-ex)中做介绍。这里，请读者先忽略它。


警告

对于一对多/多对多关联而言 *(这个例子的`Book.authors`)而言，
*用户必须`Book`实体声明[@IdView](mapping/advanced/view/id-view)属性，
否则无法在SQL DSL中访问使用`authorIds`属性。
2. 先通过join操作连接到关联对象，再访问id属性


- Java
- Kotlin


```
BookTable book = Tables.BOOK_TABLE;List<Book> books = sqlClient    .createQuery(book)    .where(        book        .asTableEx()        .authors()        .id() // 只访问id        .eq(2L)    )    .select(book)    .execute();
```


```
val books = sqlClient    .createQuery(Book::class) {        where(            table            .asTableEx()            .authors            .id // 只访问id            eq 2L        )        select(table)    }    .execute()
```


备注

例子中的`asTableEx`，并无实质性的功能，将会在下一篇文档[分页安全性](query/dynamic-join/table-ex)中做介绍。这里，请读者先忽略它。


- 这两种方法，并不等价


1. `where(table.authorIds().eq(2L))`：一次JOIN操作


从当前表`BOOK`，通过join操作拿到`BOOK_AUTHOR_MAPPING`表，直接判断`BOOK_AUTHOR_MAPPING.AUTHOR_ID`字段。
2. `where(table.authors().id().eq(2L))`：两次JOIN操作


- 基于当前表`BOOK`，通过join操作拿到`BOOK_AUTHOR_MAPPING`中间表
- 基于中间表`BOOK_AUTHOR_MAPPING`，通过join操作拿到`AUTHOR`表


最后，判断`AUTHOR.ID`字段。


二者不等价，是因为存在以下的情况


- 如果外键`BOOK_AUTHOR_MAPPING.AUTHOR_ID`是假的，即数据中没有相应的外键约束。 这会导致非null的伪外键无法通过连接获取非null的关联对象。
- 如果关联对象受[全局过滤器](query/global-filter)影响，即使数据库中存在关联对象，也应该视而不见。
- 然而，当以下所有条件同时满足时，上述两种方法是完全等价的。


- 中间表中的外键是真的，即数据中存在对应的外键约束。请参见[真假外键](mapping/base/foreignkey)
- 关联对象不受[全局过滤器](query/global-filter)影响。


注意：这里指以下两种过滤器中任何一种


- [用户自定义过滤器](query/global-filter/user-filter)
- [逻辑删除过滤器](query/global-filter/logical-deleted)


此时，关联对象的id，其实就是当前表的外键，二者等价。

信息

开发人员应该明白这两种方法的差异，按照业务需求选择正确的方式。


然而，当两种方法等价时，自动将第二种方法优化成第一种方法是一件非常划算的事，这就叫做半连接优化。


两种查询方法等价，是半连接优化能生效的必要条件，下文不再重复。


### 不适用的场景


半连接优化仅针对对关联对象的id属性的访问，不支持其他属性。


这里，以关联对象的`Author.firstName`属性为例，来演示无法去掉第二个join。


- Java
- Kotlin


```
BookTable book = Tables.BOOK_TABLE;List<Long> bookIds = sqlClient    .createQuery(book)    .where(        book            .asTableEx()            .authors()            .firstName()            .eq("Alex")    )    .select(book.id())    .distinct()    .execute();
```


```
val bookIds = sqlClient    .createQuery(Book::class) {        where(            table                .asTableEx()                .authors                .firstName eq "Alex"        )        select(table.id)    }    .distinct()    .execute()
```


备注

这里的`asTableEx` *(以及Java下的各种TableEx类型)* 是后续文档[分页安全性](query/dynamic-join/table-ex)要介绍的概念。这里，请读者先忽略它。


生成的SQL如下：


```
select     distinct tb_1_.ID from BOOK as tb_1_ inner join BOOK_AUTHOR_MAPPING as tb_2_     on tb_1_.ID = tb_2_.BOOK_ID inner join AUTHOR as tb_3_ on     tb_2_.AUTHOR_ID = tb_3_.IDwhere tb_3_.FIRST_NAME = ?
```


我们看到基于中间表的连接会产生两个SQL JOIN子句


- 第一步：连接到中间表
`inner join BOOK_AUTHOR_MAPPING as tb_2_ on tb_1_.ID = tb_2_.BOOK_ID `
- 第二步：连接到目标表
`inner join AUTHOR as tb_3_ on tb_2_.AUTHOR_ID = tb_3_.ID`


### 适用的场景


如果仅访问关联对象的id属性，则能启动半连接优化。例如：


- Java
- Kotlin


```
BookTable book = Tables.BOOK_TABLE;List<Long> bookIds = sqlClient    .createQuery(book)    .where(        book            .asTableEx()            .authors()            .id() // 只访问id            .eq(2L)    )    .select(book.id())    .distinct()    .execute();
```


```
val bookIds = sqlClient    .createQuery(Book::class) {        where(            table                .asTableEx()                .authors                .id // 只访问id                 eq 2L        )        select(table.id)    }    .distinct()    .execute()
```


备注

例子中的`asTableEx`，并无实质性的功能，将会在下一篇文档[分页安全性](query/dynamic-join/table-ex)中做介绍。这里，请读者先忽略它。


这次，生成的SQL如下：


```
select     distinct tb_1_.ID from BOOK as tb_1_ inner join BOOK_AUTHOR_MAPPING as tb_2_     on tb_1_.ID = tb_2_.BOOK_ID where tb_2_.AUTHOR_ID = ?
```


这一次，我们只看到一个SQL JOIN子句，而不是两个。


原因：目标表的主键，其实就是中间表到目标表的外键。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/dynamic-join/optimization.mdx)最后 于 **2025年9月16日**  更新
- [使用方法](#使用方法)
- [基本概念](#基本概念)
- [另外一种解决方案](#另外一种解决方案)
- [幻连接](#幻连接)
- [判断关联id的两种方法](#判断关联id的两种方法)
- [不适用的场景](#不适用的场景)
- [适用的场景](#适用的场景)
- [半连接](#半连接)
- [判断关联id的两种方法](#判断关联id的两种方法-1)
- [不适用的场景](#不适用的场景-1)
- [适用的场景](#适用的场景-1)