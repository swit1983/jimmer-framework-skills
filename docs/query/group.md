---
title: '聚合和分组'
---
# 聚合和分组


## 聚合


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;long count = sqlClient    .createQuery(table)    .where(table.name().ilike("graphql"))    .select(        table            .asTableEx().authors().id()            .count(true) // distinct: true    )    .fetchOne();
```


```
val count = sqlClient    .createQuery(Book::class) {        where(table.name.ilike("graphql"))        select(            count(                table.asTableEx().authors.id,                distinct = true            )        )    }    .fetchOne()
```


生成的SQL为


```
select    count(distinct tb_2_.AUTHOR_ID)from BOOK tb_1_inner join BOOK_AUTHOR_MAPPING tb_2_    on tb_1_.ID = tb_2_.BOOK_IDwhere    lower(tb_1_.NAME) like ? /* %graphql% */
```


## 分组


- Java
- Kotlin


```
BookTable table = Tables.BOOK_TABLE;List<Tuple2<Long, BigDecimal>> tuples = sqlClient    .createQuery(table)    .groupBy(table.storeId()) ❶    .select(            table.storeId(), ❷            table.price().avg() ❸    )    .execute();
```


```
val tuples: List<Tuple2<Long, BigDecimal>> = sqlClient    .createQuery(Book::class) {        groupBy(table.store.id) ❶        select(            table.store.id, ❷            avg(table.price).asNonNull() ❸        )    }    .execute()
```


- ❶ 按照`BOOK`表的外键`STORE_ID`分组


信息

这里，Jimmer不会把`table.store`视为表连接操作，而是整体视`table.store.id`为外键字段


请参见[幻连接](query/dynamic-join/optimization#%E5%B9%BB%E8%BF%9E%E6%8E%A5)
- ❷ 分组列可以被直接查询
- ❸ 非分组列不能直接查询，只能作为聚合函数表达式的参数查询


警告

Kotlin代码多调用了个函数：`asNonNull()`


在Jimmer的Kotlin DSL中，聚合函数`avg` *(以及`sum`、`min`、`max`)* 返回类型都是Nullable的。在不使用分组的前提下，对没有数据的表的任何列进行聚合运算都会得到null。


然而，和分组配合使用后不再如此。分组后，每一组内部至少有一条数据，如果被聚合的原始字段本身非null，则聚合后的结果不会为null。


所以，这里通过`asNonNull()`将Nullable表达式转换为NonNull表达式，最终`execute()`函数查询到的数据类型是`List<Tuple2<Long, BigDecimal>>`，和第一行明确指定变量`tuples`的类型相同。


如果去掉这里的`asNonNull()`，`execute()`函数查询到的数据类型是`List<Tuple2<Long, BigDecimal?>>`，从而导致编译错误。
[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/group.mdx)最后 于 **2025年9月16日**  更新
- [聚合](#聚合)
- [分组](#分组)