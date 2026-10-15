---
title: Base-Query
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/base-query
---

# Base-Query

所谓Base-Query，即SQL中的`Derived Table`，`CTE`和`Recursive CTE`的统称。

Jimmer使用强类型DSL对`Derived Table`，`CTE`和`Recursive CTE`进行了统一支持。

:::tip
和SQL语言的这些概念不同，Jimmer额外提供了一个全新颠覆性特性：**投影反向传播**。

这是一套全新的设计哲学，也是本文要讨论的重点。
:::

## 初识Derived Table

所谓Derived Table就是利用一个查询作为其他查询的`from`或`join`的基表。例如

```sql
select a, b, c

from (
    select a, b, c, d from t where ....
) derived_table

where d between 1 and 10
```

Jimmer实现derived table的方式如下

**Java**

```java
BookTable book = BookTable.$;
AuthorTableEx author = AuthorTableEx.$;
BaseTable2<BookTable, NumericExpression<Integer>> baseTable =
    sqlClient

        .createBaseQuery(book)
        .addSelect(book)
        .addSelect(
            Expression.numeric().sql(
                Integer.class,
                "dense_rank() over(" +
                    "order by %e desc" +
                    ")",
                sqlClient.createSubQuery(author)
                    .where(author.books().id().eq(book.id()))
                    .selectCount()
            )
        )

        .asBaseTable();
List<Book> books =
    sqlClient

        .createQuery(baseTable)
        .where(baseTable.get_2().eq(1))
        .select(baseTable.get_1())
        .execute();
```

**Kotlin**

```kotlin
val baseTable =

    baseTableSymbol {

        sqlClient.createBaseQuery(Book::class) {
            selections
                .add(table)
                .add(
                    sql(Int::class, "dense_rank() over(order by %e desc)") {
                        expression(
                            subQuery(Author::class) {
                                where(table.books.id eq parentTable.id)
                                select(rowCount())
                            }
                        )
                    }
                )
        }
    }
val books =

    sqlClient.createQuery(baseTable) {
        where(table._2 eq 1)
        select(table._1)
    }.execute()
```

对于这个例子

-   和`createQuery`创建真实查询不同，`createBaseQuery`创建的base-query不会被直接执行。
    它构建类型安全的source，供后续`createQuery`查询使用 *(作为from或join的基表使用)*，也可供`createInsert`和`createUpsert`修改数据使用。

-   内部的base-query返回了两列 *(第一列是表对象，第二列是简单表达式)*。外部查询使用`get_1()`/`_1`和`get_2()`/`_2`来使用他们

    > 这种位置化形式支持返回1~9个逻辑列。对于更大或需要复用的行结构，请使用
    > [typed tuple](./typed-tuple.md)。

-   本例使用位置化的base-query投影API：

    -   Java使用`.addSelect(...).addSelect(...)...addSelect(...)`风格的代码指定投影

    -   Kotlin使用`selections.add(...).add(...)...add(...)`风格的代码指定投影

    :::info
    -   须采用链式编程风格，语法层面不可中断，才能保证成功的编译
    -   该位置化API用于实现稍后介绍的重要特性：**投影反向传播**。另一种方式是把生成的
        typed-tuple mapper传给`select`，从而创建命名base table。
    :::

此查询会生成如下SQL

```sql
select
    tb_1_.c1,
    tb_1_.c2,
    tb_1_.c3,
    tb_1_.c4,
    tb_1_.c5

from (
    select
        tb_2_.ID c1,
        tb_2_.NAME c2,
        tb_2_.EDITION c3,
        tb_2_.PRICE c4,
        tb_2_.STORE_ID c5,
        dense_rank() over(order by (
            select
                count(1)
            from AUTHOR tb_3_
            inner join BOOK_AUTHOR_MAPPING tb_4_
                on tb_3_.ID = tb_4_.AUTHOR_ID
            where
                tb_4_.BOOK_ID = tb_2_.ID
        ) desc) c6
    from BOOK tb_2_
) tb_1_
where
    tb_1_.c6 = ? /* 1 */
```

得到如下数据

```json
[
    {
        "id": 1,
        "name": "Learning GraphQL",
        "edition": 1,
        "price": 50,
        "store": {
            "id": 1
        }
    },
    {
        "id": 2,
        "name": "Learning GraphQL",
        "edition": 2,
        "price": 55,
        "store": {
            "id": 1
        }
    },
    {
        "id": 3,
        "name": "Learning GraphQL",
        "edition": 3,
        "price": 51,
        "store": {
            "id": 1
        }
    }
]
```

## 直接选择实体表

如果source只需要实体表和过滤条件，可以使用`select(table)`。source保留所选table的类型，
因此可以直接访问属性，无需单元素元组或投影mapper：

**Java**

```java
BookStoreTable store = BookStoreTable.$;

ConfigurableBaseQuery<BookStoreTable> query = sqlClient
    .createBaseQuery(store)
    .where(store.name().eq("MANNING"))
    .select(store);

BookStoreTable source = query.asBaseTable();

List<String> names = sqlClient
    .createQuery(source)
    .select(source.name())
    .execute();
```

**Kotlin**

```kotlin
val source = sqlClient.createBaseQuery(BookStore::class) {
    where(table.name eq "MANNING")
    select(table)
}.asBaseTable()

val names = sqlClient.createQuery(source) {
    select(table.name)
}.execute()
```

在Java中，`asBaseTable()`返回`BookStoreTable`。在Kotlin中，它返回
`KBaseTableSymbol<KNonNullTable<BookStore>>`，外层查询通过接收者的`table`访问所选实体表。
如果选择的是可空的连接表，Kotlin会保留`KNullableTable<BookStore>`类型。

base-query中的`select(table)`描述可用的属性，不会加载实体，也不要求SQL选择所有列。
本例中，投影反向传播只导出`NAME`，source的过滤条件仍保留在内层查询中：

```sql
select tb_1_.c1
from (
    select tb_2_.NAME c1
    from BOOK_STORE tb_2_
    where tb_2_.NAME = ? /* MANNING */
) tb_1_
```

这个source也可以用于`createBaseQuery(source)`、`asCteBaseTable()`、类型兼容的union、
recursive CTE，以及[insert/upsert指令](../mutation/insert-from-select.md#直接选择实体表)。
所需的列会穿过嵌套source逐层传播。对于`distinct`、`union`、`intersect`或`minus`，
实体投影会保留维持行身份和结果行数所需的字段。

构建位置化元组时，仍使用Java的`addSelect(table)`或Kotlin的`selections.add(table)`，
通过`get_1()`/`_1`访问其元素。命名的自定义投影则使用`select(mapper)`。

## 命名Base Table

对于报表查询，`_1`、`_2`这样的位置化属性很快就会变得难以阅读。
[`@TypedTuple`](./typed-tuple.md)可以让一个声明同时作为普通查询的结果投影，以及derived
table或CTE的命名行结构：

```kotlin
val statistics = sqlClient.createBaseQuery(Book::class) {
    groupBy(table.storeId)
    select(
        StoreStatisticsMapper
            .storeId(table.storeId.asNonNull())
            .bookCount(rowCount())
    )
}.asCteBaseTable()

sqlClient.createQuery(statistics) {
    where(table.bookCount gt 2L)
    select(table.storeId)
}
```

代码生成器为Java提供`StoreStatisticsTable`，为Kotlin提供non-null和nullable两个版本。
命名table也适用于weak join、union和recursive CTE，并且不受九个逻辑属性的限制。
Java和Kotlin的声明方式及完整示例请参阅[Typed Tuple](./typed-tuple.md)。

## 投影反向传播

### BaseQuery独有的投影列设置

上面的位置化API使用如下投影方法：

-   Java使用`.addSelect(...).addSelect(...)...addSelect(...)`。

-   Kotlin使用`selections.add(...).add(...)...add(...)`。

:::caution
须采用链式编程风格，语法层面不可中断，才能保证成功的编译
:::

位置化投影的每个元素支持以下两种操作。直接使用`select(table)`时，也遵循相同的table投影规则：

1.  查询简表达式

**Java**

```java
.addSelect(
    Expression.concat(
        table.firstName(),
        Expression.constant(" "),
        table.lastName()
    )
)
```

**Kotlin**

```kotlin
selections.add(
    concat(
        table.firstName,
        constant(" "),
        table.lastName
    )
)
```

-   查询表对象

**Java**

```java
.addSelect(table)
.addSelect(table.store())
.addSelect(table.asTableEx().authors())
```

**Kotlin**

```kotlin
selections
    .add(table)
    .add(table.store)
    .add(table.asTableEx().authors)
```

    :::info
    即
    1.  要么返回简单表达式
    2.  要么返回对象 *(形状未知)*

    其中，返回形状未知的表对象 *(2)*，才是投影反向传播的所关心的话题，是本章节的重点。

    > 注意：不能对返回的表对象施加[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)，或返回[Output DTO](object-fetcher/dto.md)
    :::

而如下方式是不被允许的

-   <span style={{textDecoration:"line-through"}}>禁止使用[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)的对象查询</span>

**Java**

```java
// 注意，这里示范的是非法代码
.addSelect(
    table.fetch(
        BookFetcher.$
            .allScalarFields()
            .store(
                BookStoreFetcher.$.name()
            )
            .authors(
                AuthorFetcher.$.name()
            )
    )
)
```

**Kotlin**

```kotlin
// 注意，这里示范的是非法代码
selections.add(
    table.fetchBy {
        allScalarFields()
        store {
            name()
        }
        authors {
            authors()
        }
    }
)
```

-   <span style={{textDecoration:"line-through"}}>禁止返回[Output DTO](object-fetcher/dto.md)</span>

**Java**

```java
// 注意，这里示范的是非法代码
.addSelect(table.fetch(BookView.class))
```

**Kotlin**

```kotlin
// 注意，这里示范的是非法代码
selections.add(table.fetch(BookView::class))
```

:::info
基于[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)/[Output DTO](object-fetcher/dto.md)的对象查询是Jimmer最核心的功能之一，
BaseQuery投影查询的的API却不允许这样查询。

这绝非为了禁止使用此能力；**恰恰相反**，这是为了给予更好的抽象，
更好地使用[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)和[Output DTO](object-fetcher/dto.md)。
:::

### 投影反向传播机制介绍

在原生SQL中，基于dervided table的查询往往是这样的。

```sql
select

    c1,
    c2,
    c3,
    ...,
    cM

from (
    select

        c1,
        c2,
        c3,
        ...,
        cM,
        ...,
        CN

    from real_table
) derived_table
```
> 其中，M &lt;= N

我们先决定内部base query的返回列集合 `(c1, c2, ..., cN)`，然后在选择其中的一个子集合 `(c1, c2, ..., cM)` *(M &lt;= N)*，让外部真实查询返回。

很明显，这是一个自内向外的投影传播过程，内部查询的投影和外部查询的投影包含大量的重复列，这是一件非常繁琐且容易出错的事。
在列的数量较多的时候，这对开发、重构和维护极为不利。不幸的是，实际项目几乎总是如此。

:::tip
因此, Jimmer采用 **自外向内** 的投影反向传播机制

-   内部的base-query如果如果返回对象，必须返回原始的table对象，不能使用[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)或[Output DTO](object-fetcher/dto.md)，表示返回的对象的形状是未知的

-   最终，内部base-query应该返回的哪些具体的列，而是根据外部查询的需要自动决定的，无需在base-query中显式指定。

    外部查询控制内部base-query应该返回哪些具体列的方法有：

    -   [对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)
    -   [Output DTO](object-fetcher/dto.md)类型
    -   DSL中被使用的列属性

    投影反向传播不仅能让大幅答复简化，还带了原始SQL不具备的智能行实用性。
:::

现在以[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)为例，编写一个函数

**Java**

```java
private List<Book> findBooks(

    Fetcher<Book> fetcher
) {
    BookTable table = BookTable.$;
    AuthorTableEx author = AuthorTableEx.$;
    BaseTable2<BookTable, NumericExpression<Integer>> baseTable =
        sqlClient
            .createBaseQuery(table)
            .addSelect(table)
            .addSelect(
                Expression.numeric().sql(
                    Integer.class,
                    "dense_rank() over(" +
                        "order by %e desc" +
                        ")",
                    sqlClient.createSubQuery(author)
                        .where(author.books().id().eq(table.id()))
                        .selectCount()
                )
            )
            .asBaseTable();
    return sqlClient
        .createQuery(baseTable)
        .where(baseTable.get_2().eq(1))
        .select(
            baseTable.get_1().fetch(

                fetcher
            )
        )
        .execute();
}
```

**Kotlin**

```kotlin
private fun findBooks(

    fetcher: Fetcher<Book>
): List<Book> {
    val baseTable = baseTableSymbol {
        sqlClient.createBaseQuery(Book::class) {
            selections
                .add(table)
                .add(
                    sql(Int::class, "dense_rank() over(order by %e desc)") {
                        expression(
                            subQuery(Author::class) {
                                where(table.books.id eq parentTable.id)
                                select(rowCount())
                            }
                        )
                    }
                )
        }
    }
    return sqlClient.createQuery(baseTable) {
        where(table._2 eq 1)
        select(
            table._1.fetch(

                fetcher
            )
        )
    }.execute()
}
```

现在让我们来看3个案例

-   简单的[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)

**Java**

```java
List<Book> books = findBooks(
    BookFetcher.$

        .name()
);
```

**Kotlin**

```kotlin
val books = findBooks(
    newFetcher(Book::class).by {

        name()
    }
)
```

    由于[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)很简单，内外查询返回的列都较少，生成如下SQL

```sql
select
    tb_1_.c1,

    tb_1_.c2
from (
    select
        tb_2_.ID c1,

        tb_2_.NAME c2,
        dense_rank() over(order by (
            select
                count(1)
            from AUTHOR tb_3_
            inner join BOOK_AUTHOR_MAPPING tb_4_
                on tb_3_.ID = tb_4_.AUTHOR_ID
            where
                tb_4_.BOOK_ID = tb_2_.ID
        ) desc) c3
    from BOOK tb_2_
) tb_1_
where
    tb_1_.c3 = ? /* 1 */
```

    查询得到如下数据

```json
[
    {
        "id": 1,

        "name": "Learning GraphQL"
    },
    {
        "id": 2,

        "name": "Learning GraphQL"
    },
    {
        "id": 3,

        "name": "Learning GraphQL"
    }
]
```

-   中等复杂的[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)

**Java**

```java
List<Book> books = findBooks(
    BookFetcher.$

        .allScalarFields()
);
```

**Kotlin**

```kotlin
val books = findBooks(
    newFetcher(Book::class).by {

        allScalarFields()
    }
)
```

    由于[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)相对复杂一些，内外查询返回的列都变多，生成如下SQL

```sql
select
    tb_1_.c1,

    tb_1_.c2,
    tb_1_.c3,
    tb_1_.c4

from (
    select
        tb_2_.ID c1,

        tb_2_.NAME c2,
        tb_2_.EDITION c3,
        tb_2_.PRICE c4,

        dense_rank() over(order by (
            select
                count(1)
            from AUTHOR tb_3_
            inner join BOOK_AUTHOR_MAPPING tb_4_
                on tb_3_.ID = tb_4_.AUTHOR_ID
            where
                tb_4_.BOOK_ID = tb_2_.ID
        ) desc) c5
    from BOOK tb_2_
) tb_1_
where
    tb_1_.c5 = ? /* 1 */
```

    查询得到如下数据

```json
[
    {
        "id": 1,

        "name": "Learning GraphQL",
        "edition": 1,
        "price": 50

    },
    {
        "id": 2,

        "name": "Learning GraphQL",
        "edition": 2,
        "price": 55

    },
    {
        "id": 3,

        "name": "Learning GraphQL",
        "edition": 3,
        "price": 51

    }
]
```

-   复杂的[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)

**Java**

```java
List<Book> books = findBooks(
    BookFetcher.$
        .name()

        .store(
            ReferenceFetchType.JOIN_ALWAYS,
            BookStoreFetcher.$.name()
        )
);
```

**Kotlin**

```kotlin
val books = findBooks(
    newFetcher(Book::class).by {
        name()

        store(ReferenceFetchType.JOIN_ALWAYS) {
            name()
        }
    }
)
```

    由于[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)包含join fetch操作，查询语句自然也会通过join抓取关联对象，生成如下SQL

```sql
select
    tb_1_.c1,
    tb_1_.c2,

    tb_6_.ID,
    tb_6_.NAME

from (
    select
        tb_2_.ID c1,
        tb_2_.NAME c2,
        tb_2_.STORE_ID c3,
        dense_rank() over(order by (
            select
                count(1)
            from AUTHOR tb_3_
            inner join BOOK_AUTHOR_MAPPING tb_4_
                on tb_3_.ID = tb_4_.AUTHOR_ID
            where
                tb_4_.BOOK_ID = tb_2_.ID
        ) desc) c4
    from BOOK tb_2_
) tb_1_

left join BOOK_STORE tb_6_
    on tb_1_.c3 = tb_6_.ID

where
    tb_1_.c4 >= ? /* 1 */
```

    查询得到如下数据

```json
[
    {
        "id": 1,
        "name": "Learning GraphQL",

        "store": {
            "id": 1,
            "name": "O'REILLY"
        }

    },
    {
        "id": 2,
        "name": "Learning GraphQL",

        "store": {
            "id": 1,
            "name": "O'REILLY"
        }

    },
    {
        "id": 3,
        "name": "Learning GraphQL",

        "store": {
            "id": 1,
            "name": "O'REILLY"
        }

    }
]
```

:::tip
可见，所谓投影反向传播，就是内部的base-query并不直接控制返回的表对象的格式 *(返回原始表对象并视之为形状未知)*，返回的对象格式统一由外部查询控制，
内部的base-query按照外部查询的需要自动决定返回哪些列。
:::

### 集合操作的投影传播

SQL支持`UNION`、`UNION ALL`、`INTERSECT`和`MINUS`这样集合操作，base-query一样，让我们修改`findBooks`如下

**Java**

```java
private List<Book> findBooks(
    Fetcher<Book> fetcher
) {
    BookTable table = BookTable.$;
    AuthorTableEx author = AuthorTableEx.$;
    BaseTable2<BookTable, NumericExpression<Integer>> baseTable =

        TypedBaseQuery.unionAll(
            sqlClient
                .createBaseQuery(table)

                .where(table.edition().eq(2))
                .addSelect(table)
                .addSelect(
                    Expression.numeric().sql(
                        Integer.class,
                        "dense_rank() over(" +
                            "order by %e desc" +
                            ")",
                        sqlClient.createSubQuery(author)
                            .where(author.books().id().eq(table.id()))
                            .selectCount()
                    )
                ),
            sqlClient
                .createBaseQuery(table)

                .where(table.edition().eq(3))
                .addSelect(table)
                .addSelect(
                    Expression.numeric().sql(
                        Integer.class,
                        "dense_rank() over(" +
                            "order by %e desc" +
                            ")",
                        sqlClient.createSubQuery(author)
                            .where(author.books().id().eq(table.id()))
                            .selectCount()
                    )
                )
        ).asBaseTable();
    return sqlClient
        .createQuery(baseTable)
        .where(baseTable.get_2().eq(1))
        .select(baseTable.get_1().fetch(fetcher))
        .execute();
}
```

**Kotlin**

```kotlin
private fun findBooks(
    fetcher: Fetcher<Book>
): List<Book> {
    val baseTable = baseTableSymbol {
        sqlClient.createBaseQuery(Book::class) {

            where(table.edition eq 2)
            selections
                .add(table)
                .add(
                    sql(Int::class, "dense_rank() over(order by %e desc)") {
                        expression(
                            subQuery(Author::class) {
                                where(table.books.id eq parentTable.id)
                                select(rowCount())
                            }
                        )
                    }
                )

        } unionAll
        sqlClient.createBaseQuery(Book::class) {

            where(table.edition eq 3)
            selections
                .add(table)
                .add(
                    sql(Int::class, "dense_rank() over(order by %e desc)") {
                        expression(
                            subQuery(Author::class) {
                                where(table.books.id eq parentTable.id)
                                select(rowCount())
                            }
                        )
                    }
                )
        }
    }
    return sqlClient.createQuery(baseTable) {
        where(table._2 eq 1)
        select(
            table._1.fetch(fetcher)
        )
    }.execute()
}
```

现在让我们来看两个案例

-   简单的[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)

**Java**

```java
List<Book> books = findBooks(
    BookFetcher.$

        .name()
);
```

**Kotlin**

```kotlin
val books = findBooks(
    newFetcher(Book::class).by {

        name()
    }
)
```

    由于[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)很简单，内外查询返回的列都较少，生成如下SQL

```sql
select
    tb_1_.c1,

    tb_1_.c2
from (
        select
            tb_2_.ID c1,

            tb_2_.NAME c2,
            dense_rank() over(order by (
                select
                    count(1)
                from AUTHOR tb_3_
                inner join BOOK_AUTHOR_MAPPING tb_4_
                    on tb_3_.ID = tb_4_.AUTHOR_ID
                where
                    tb_4_.BOOK_ID = tb_2_.ID
            ) desc) c3
        from BOOK tb_2_
        where
            tb_2_.EDITION = ? /* 2 */
        union all
        select
            tb_6_.ID c1,

            tb_6_.NAME c2,
            dense_rank() over(order by (
                select
                    count(1)
                from AUTHOR tb_7_
                inner join BOOK_AUTHOR_MAPPING tb_8_
                    on tb_7_.ID = tb_8_.AUTHOR_ID
                where
                    tb_8_.BOOK_ID = tb_6_.ID
            ) desc) c3
        from BOOK tb_6_
        where
            tb_6_.EDITION = ? /* 3 */
) tb_1_
where
    tb_1_.c3 = ? /* 1 */
```

    查询得到如下数据

```json
[
    {
        "id": 2,

        "name": "Learning GraphQL"
    },
    {
        "id": 3,

        "name": "Learning GraphQL"
    }
]
```

-   相对复杂的[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)

**Java**

```java
List<Book> books = findBooks(
    BookFetcher.$

        .allScalarFields()
);
```

**Kotlin**

```kotlin
val books = findBooks(
    newFetcher(Book::class).by {

        allScalarFields()
    }
)
```

    由于[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)相对复杂一些，内外查询返回的列都变多，生成如下SQL

```sql
select
    tb_1_.c1,

    tb_1_.c2,
    tb_1_.c3,
    tb_1_.c4

from (
        select
            tb_2_.ID c1,

            tb_2_.NAME c2,
            tb_2_.EDITION c3,
            tb_2_.PRICE c4,

            dense_rank() over(order by (
                select
                    count(1)
                from AUTHOR tb_3_
                inner join BOOK_AUTHOR_MAPPING tb_4_
                    on tb_3_.ID = tb_4_.AUTHOR_ID
                where
                    tb_4_.BOOK_ID = tb_2_.ID
            ) desc) c5
        from BOOK tb_2_
        where
            tb_2_.EDITION = ? /* 2 */
        union all
        select
            tb_6_.ID c1,

            tb_6_.NAME c2,
            tb_6_.EDITION c3,
            tb_6_.PRICE c4,

            dense_rank() over(order by (
                select
                    count(1)
                from AUTHOR tb_7_
                inner join BOOK_AUTHOR_MAPPING tb_8_
                    on tb_7_.ID = tb_8_.AUTHOR_ID
                where
                    tb_8_.BOOK_ID = tb_6_.ID
            ) desc) c5
        from BOOK tb_6_
        where
            tb_6_.EDITION = ? /* 3 */
) tb_1_
where
    tb_1_.c5 = ? /* 1 */
```

    查询得到如下数据

```json
[
    {
        "id": 2,

        "name": "Learning GraphQL",
        "edition": 2,
        "price": 55

    },
    {
        "id": 3,

        "name": "Learning GraphQL",
        "edition": 3,
        "price": 51

    }
]
```

:::tip
在投影形状从外向内的反向传播过程中，如果base-query本身使用了集合操作 *(`UNION`、`UNION ALL`、`INTERSECT`或`MINUS`)*，
那么这个传播过程会分裂，影响参与集合运算的每一个更细小的base-query。
:::

### 投影列合并

事实上，[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)和[Output DTO](object-fetcher/dto.md)并非导致投影反向传播的所有原因。

如果base-query返回形状未知表对象，而外部查询使用SQL DSL依赖该表对象的某些属性，一样可以导致投影反向传播机制。

**Java**

```java
private List<Book> findBooks(
    @Nullable String name,
    Fetcher<Book> fetcher
) {
    BookTable table = BookTable.$;
    BaseTable1<BookTable> baseTable =
        sqlClient
            .createBaseQuery(table)
            .where(table.edition().eq(3))
            .addSelect(table)
            .asBaseTable();
    return sqlClient
        .createQuery(baseTable)
        .where(

            baseTable.get_1().name().eqIf(name)
        )
        .select(baseTable.get_1().fetch(fetcher))
        .execute();
}
```

**Kotlin**

```kotlin
private fun findBooks(
    name: String?,
    fetcher: Fetcher<Book>
): List<Book> {
    val baseTable = baseTableSymbol {
        sqlClient.createBaseQuery(Book::class) {
            where(table.edition eq 3)
            selections.add(table)
        }
    }
    return sqlClient.createQuery(baseTable) {
        where(

            table._1.name `eq?` name
        )
        select(table._1.fetch(fetcher))
    }.execute()
}
```

让我来观看两个案例

1.  不触发投影反向传播

**Java**

```java
List<Book> books = findBooks(
    null,
    BookFetcher.$,
)
```

**Kotlin**

```kotlin
val books = findBooks(
    null,
    newFetcher(Book::class).by {  }
)
```

    这里
    -   name参数无效，动态谓词 *(java的`eqIf`，以及kotlin的`eq?`)* 导致外部查询的where被忽略
    -   [对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)参数过于简单，只需要`id-only`的对象

    因此，最终没有任何非id字段的投影诉求被反向传播给base-query，最终生成的SQL语句非常简单

```sql
select
    tb_1_.c1
from (
    select
        tb_2_.ID c1
    from BOOK tb_2_
    where
        tb_2_.EDITION = ? /* 3 */
) tb_1_
```

2.  外部查询添加where条件触发投影反向传播 *(但并未通过[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)触发)*，让base-query返回name列

**Java**

```java
List<Book> books = findBooks(

    "GraphQL in Action",
    BookFetcher.$,
)
```

**Kotlin**

```kotlin
val books = findBooks(

    "GraphQL in Action",
    newFetcher(Book::class).by {  }
)
```

    生成的SQL如下

```sql
select
    tb_1_.c1 from (
    select
        tb_2_.ID c1,

        tb_2_.NAME c2
    from BOOK tb_2_
    where
        tb_2_.EDITION = ? /* 3 */
) tb_1_
where

    tb_1_.c2 = ? /* GraphQL in Action */
```

3.  外部查询
    -   既通过where条件触发投影反向传播，要求内部base-query返回name列
    -   也通过[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)触发投影反向传播，要求内部base-query返回name列

    base-query会自动 **合并** 二者，只查询一次name列

**Java**

```java
List<Book> books = findBooks(

    "GraphQL in Action",

    BookFetcher.$.name(),
)
```

**Kotlin**

```kotlin
val books = findBooks(

    "GraphQL in Action",
    newFetcher(Book::class).by {

        name()
     }
)
```

    生成的SQL如下

```sql
select
    tb_1_.c1,

    tb_1_.c2 // 1. 外部查询抓取name
from (
    select
        tb_2_.ID c1,

        tb_2_.NAME c2 // 最终，base-query只查询一次name
    from BOOK tb_2_
    where
        tb_2_.EDITION = ? /* 3 */
) tb_1_
where

    tb_1_.c2 = ? // 2. 外部查询使用where判断name
```

    可见，外部查询通过两种不同的方式 *([对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)和where条件)* 触发投影反向传播 *(需要内部base-query返回name列)*
    内部查询只把name列查询了一次。

    即，外部查询中不同的投影反向传播诉求中相同的列的投影诉求会被自动合并。

    :::tip
    由于投影反向传播会自动合并相同的列请求，只有当表达式非常复杂时，利用base-query返回表达式列才会显得有意义。

    因此。不建议内部base-query单独返回简单的列属性表达式。大部分情况下，内部base-query直接返回原始table对象即可。
    :::

## Weak Join

在这里，我们
-   将原始实体类型所对应的表称为实体表，Entity Table
-   将base-query构建的临时表成为派生表，Derived Table

那么，无论实体表和派生表，都可以对派生表进行`weakJoin`操作。

:::info
Jimmer表连接有两个重要特性
-   无用的表连接会被自动忽略，请见[优化不必要连接](dynamic-join/optimization.md)
-   冲突的表连接会被自动合并，请见[合并冲突连接](dynamic-join/merge.md)

由于这些内容在相关章节中已经被详细论述，本文不做重复，仅罗列和派生表相关的的JOIN操作的书写方法。
:::

### 派生表 JOIN 派生表

**Java**

```java
BookTable rawBook = BookTable.$;
BaseTable2<BookTable, NumericExpression<Integer>> baseBook =
    sqlClient
        .createBaseQuery(rawBook)
        .addSelect(rawBook)
        .addSelect(
            Expression.numeric().sql(
                Integer.class,
                "row_number() over(order by %e desc)",
                rawBook.price()
            )
        )
        .asBaseTable();
AuthorTable rawAuthor = AuthorTable.$;
BaseTable2<AuthorTable, NumericExpression<Integer>> baseAuthor =
    sqlClient
        .createBaseQuery(rawAuthor)
        .addSelect(rawAuthor)
        .addSelect(
            Expression.numeric().sql(
                Integer.class,
                "row_number() over(order by %e asc)",
                rawAuthor.firstName().length()
                    .plus(rawAuthor.lastName().length())
            )
        )
        .asBaseTable();
BaseTable2<AuthorTable, NumericExpression<Integer>> joinedBaseAuthor =

    baseBook.weakJoin(
        baseAuthor,
        (bb, ba) ->
            bb.get_1().id().eq(ba.get_1().asTableEx().books().id())
    );
List<Tuple2<Book, Author>> tuples = sqlClient
    .createQuery(baseBook)
    .where(baseBook.get_2().lt(4))
    .where(joinedBaseAuthor.get_2().lt(4))
    .select(
        baseBook.get_1(),
        joinedBaseAuthor.get_1()
    )
    .execute();
```

**Kotlin**

```kotlin
val baseBook = baseTableSymbol {
    sqlClient.createBaseQuery(Book::class) {
        selections
            .add(table)
            .add(
                sql(Int::class, "row_number() over(order by %e desc)") {
                    expression(table.price)
                }
            )
    }
}
val baseAuthor = baseTableSymbol {
    sqlClient.createBaseQuery(Author::class) {
        selections
            .add(table)
            .add(
                sql(Int::class, "row_number() over(order by %e asc)") {
                    expression(
                        table.firstName.length() +
                            table.lastName.length()
                    )
                }
            )
    }
}
val tuples = sqlClient.createQuery(baseBook) {

    val joinedAuthor = table.weakJoin(baseAuthor) {
        source._1.id eq target._1.asTableEx().books.id
    }
    where += table._2 lt 4
    where += joinedAuthor._2 lt 4
    select(
        table._1,
        joinedAuthor._1
    )
}.execute()
```

生成如下SQL

```sql
select
    tb_1_.c1,
    tb_1_.c2,
    tb_1_.c3,
    tb_1_.c4,
    tb_1_.c5,
    tb_2_.c6,
    tb_2_.c7,
    tb_2_.c8,
    tb_2_.c9 from (
    select
        tb_3_.ID c1,
        tb_3_.NAME c2,
        tb_3_.EDITION c3,
        tb_3_.PRICE c4,
        tb_3_.STORE_ID c5,
        row_number() over(order by tb_3_.PRICE desc) c10
    from BOOK tb_3_
) tb_1_

inner join (
    select
        tb_4_.ID c6,
        tb_4_.FIRST_NAME c7,
        tb_4_.LAST_NAME c8,
        tb_4_.GENDER c9,
        row_number() over(order by (
            length(tb_4_.FIRST_NAME) +
            length(tb_4_.LAST_NAME)
        ) asc) c11
    from AUTHOR tb_4_
) tb_2_
inner join BOOK_AUTHOR_MAPPING tb_5_
    on tb_2_.c6 = tb_5_.AUTHOR_ID
    on tb_1_.c1 = tb_5_.BOOK_ID
where
        tb_1_.c10 < ? /* 4 */
    and
        tb_2_.c11 < ? /* 4 */
```

### 实体表 JOIN 派生表

**Java**

```java
BookTable book = BookTable.$;
AuthorTable rawAuthor = AuthorTable.$;
BaseTable2<AuthorTable, NumericExpression<Integer>> baseAuthor =
    sqlClient
        .createBaseQuery(rawAuthor)
        .addSelect(rawAuthor)
        .addSelect(
            Expression.numeric().sql(
                Integer.class,
                "row_number() over(order by %e asc)",
                rawAuthor.firstName().length().plus(rawAuthor.lastName().length())
            )
        )
        .asBaseTable();
BaseTable2<AuthorTable, NumericExpression<Integer>> joinedBaseAuthor =

    book.asTableEx().weakJoin(
        baseAuthor,
        (b, ba) ->
            b.id().eq(ba.get_1().asTableEx().books().id())
    );
List<Tuple2<Book, Author>> tuples = sqlClient
    .createQuery(book)
    .where(joinedBaseAuthor.get_2().lt(4))
    .select(
        book,
        joinedBaseAuthor.get_1()
    )
    .execute();
```

**Kotlin**

```kotlin
val baseAuthor = baseTableSymbol {
    sqlClient.createBaseQuery(Author::class) {
        selections
            .add(table)
            .add(
                sql(Int::class, "row_number() over(order by %e asc)") {
                    expression(
                        table.firstName.length() +
                            table.lastName.length()
                    )
                }
            )
    }
}
val tuples = sqlClient.createQuery(Book::class) {

    val joinedAuthor = table.asTableEx().weakJoin(baseAuthor) {
        source.id eq target._1.asTableEx().books.id
    }
    where += joinedAuthor._2 lt 4
    select(
        table,
        joinedAuthor._1
    )
}.execute()
```

生成如下SQL

```sql
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID,
    tb_2_.c1,
    tb_2_.c2,
    tb_2_.c3,
    tb_2_.c4
from BOOK tb_1_

inner join (
    (
        select
            tb_3_.ID c1,
            tb_3_.FIRST_NAME c2,
            tb_3_.LAST_NAME c3,
            tb_3_.GENDER c4,
            row_number() over(order by (
                length(tb_3_.FIRST_NAME) +
                length(tb_3_.LAST_NAME)
            ) asc) c5
        from AUTHOR tb_3_
    ) tb_2_
    inner join BOOK_AUTHOR_MAPPING tb_4_
        on tb_2_.c1 = tb_4_.AUTHOR_ID
)
    on tb_1_.ID = tb_4_.BOOK_ID
where
    tb_2_.c5 < ? /* 4 */
```

## CTE

所谓CTE，就是把之前Drived Table的SQL

```sql
select ...

from (
    select ...
    from my_table
) tb_1_

```

改写为CTE风格的SQL，如下

```sql

with tb_1_(...) as (
    select ...
    from my_table
)

select ...
from tb_1_
```

除了生成的SQL采用不同的写法外，**功能层面和前文阐述的内容并无任何差异**。

因此，我们把本文开头的第一个例子该用CTE的写法改写即可，不作过多阐述。

**Java**

```java
BookTable book = BookTable.$;
AuthorTableEx author = AuthorTableEx.$;
BaseTable2<BookTable, NumericExpression<Integer>> baseTable =
    sqlClient
        .createBaseQuery(book)
        .addSelect(book)
        .addSelect(
            Expression.numeric().sql(
                Integer.class,
                "dense_rank() over(" +
                    "order by %e desc" +
                    ")",
                sqlClient.createSubQuery(author)
                    .where(author.books().id().eq(book.id()))
                    .selectCount()
            )
        )

        .asCteBaseTable();
List<Book> books =
    sqlClient
        .createQuery(baseTable)
        .where(baseTable.get_2().eq(1))
        .select(baseTable.get_1())
        .execute();
```

**Kotlin**

```kotlin
val baseTable =

    cteBaseTableSymbol {
        sqlClient.createBaseQuery(Book::class) {
            selections
                .add(table)
                .add(
                    sql(Int::class, "dense_rank() over(order by %e desc)") {
                        expression(
                            subQuery(Author::class) {
                                where(table.books.id eq parentTable.id)
                                select(rowCount())
                            }
                        )
                    }
                )
        }
    }
val books =
    sqlClient.createQuery(baseTable) {
        where(table._2 eq 1)
        select(table._1)
    }.execute()
```

-   Java代码使用`asCteBaseTable`，而非`asBaseTable`
-   Kotlin代码使用`cteBaseTableSymbol`，而非`baseTableSymbol`

最终生成的SQL如下

```sql

with tb_1_(c1, c2, c3, c4, c5, c6) as (
    select
        tb_2_.ID,
        tb_2_.NAME,
        tb_2_.EDITION,
        tb_2_.PRICE,
        tb_2_.STORE_ID,
        dense_rank() over(order by (
            select
                count(1)
            from AUTHOR tb_3_
            inner join BOOK_AUTHOR_MAPPING tb_4_
                on tb_3_.ID = tb_4_.AUTHOR_ID
            where
                tb_4_.BOOK_ID = tb_2_.ID
        ) desc)
    from BOOK tb_2_
)
select
    tb_1_.c1,
    tb_1_.c2,
    tb_1_.c3,
    tb_1_.c4,
    tb_1_.c5 from tb_1_
where
    tb_1_.c6 = ? /* 1 */
```

## Recursive-CTE

SQL的CTE有一个非常重要的功能，就是在利用union all操作构建base-query时

-   union all操作的第一部分简单地选取一些初始数据
-   union all操作的第二个部分对 **CTE本身** 进行join

由于定义CTE的查询语句join CTE所定义的派生表本身，所以，这是一种递归查询

> 在处理自关联表所描述树形结构时，Recursive CTE非常有用

**Java**

```java
TreeNodeTable table = TreeNodeTable.$;
BaseTable2<TreeNodeTable, NumericExpression<Integer>> baseTable =

    TypedBaseQuery.unionAllRecursively(
        sqlClient
            .createBaseQuery(table)
            .where(table.parentId().isNull())
            .addSelect(table)
            .addSelect(Expression.constant(1)),

        recursiveRef -> {
            MutableRecursiveBaseQuery<BaseTable2<TreeNodeTable, NumericExpression<Integer>>> q =
                sqlClient
                    .createBaseQuery(
                        table,
                        recursiveRef,
                        (t, r) -> t.parentId().eq(r.get_1().id())
                    );
            return q
                .addSelect(table)
                .addSelect(

                    q.recursive()
                        .get_2()
                        .plus(Expression.constant(1))
                );
        }
    ).asCteBaseTable();
List<Tuple2<TreeNode, Integer>> tuples = sqlClient
    .createQuery(baseTable)
    .orderBy(baseTable.get_2(), baseTable.get_1().name())
    .select(
        baseTable.get_1().fetch(TreeNodeFetcher.$.name()),
        baseTable.get_2()
    )
    .execute();
```

**Kotlin**

```kotlin
val baseTable = cteBaseTableSymbol {
    sqlClient.createBaseQuery(TreeNode::class) {
        where(table.parentId.isNull())
        selections
            .add(table)
            .add(constant(1))

    }.unionAllRecursively {
        sqlClient.createBaseQuery(
            TreeNode::class,

            it,
            {source.parentId eq target._1.id }
        ) {
            selections
                .add(table)
                .add(

                    recursive._2 + 1
                )
        }
    }
}
val tuples = sqlClient.createQuery(baseTable) {
    orderBy(table._2, table._1.name)
    select(
        table._1.fetchBy {
            name()
        },
        table._2
    )
}.execute()
```

生成如下SQL

```sql
with recursive tb_1_(c1, c2, c3) as (
        select
            tb_2_.ID,
            tb_2_.NAME,
            1
        from TREE_NODE tb_2_
        where
            tb_2_.PARENT_ID is null

        union all
        select
            tb_4_.ID,
            tb_4_.NAME,
            tb_1_.c3 + 1
        from TREE_NODE tb_4_

        inner join tb_1_
            on tb_4_.PARENT_ID = tb_1_.c1
    ) select
    tb_1_.c1,
    tb_1_.c2,
    tb_1_.c3 from tb_1_
order by
    tb_1_.c3 asc,
    tb_1_.c2 asc
```

最终查询得到如下数据 *(为了提高可读性，手动引入了一些空行)*

```json
Tuple2(_1={"id":1,"name":"Home"}, _2=1)

Tuple2(_1={"id":9,"name":"Clothing"}, _2=2)
Tuple2(_1={"id":2,"name":"Food"}, _2=2)

Tuple2(_1={"id":6,"name":"Bread"}, _2=3)
Tuple2(_1={"id":3,"name":"Drinks"}, _2=3)
Tuple2(_1={"id":18,"name":"Man"}, _2=3)
Tuple2(_1={"id":10,"name":"Woman"}, _2=3)

Tuple2(_1={"id":7,"name":"Baguette"}, _2=4)
Tuple2(_1={"id":19,"name":"Casual wear"}, _2=4)
Tuple2(_1={"id":11,"name":"Casual wear"}, _2=4)
Tuple2(_1={"id":8,"name":"Ciabatta"}, _2=4)
Tuple2(_1={"id":4,"name":"Coca Cola"}, _2=4)
Tuple2(_1={"id":5,"name":"Fanta"}, _2=4)
Tuple2(_1={"id":22,"name":"Formal wear"}, _2=4)
Tuple2(_1={"id":15,"name":"Formal wear"}, _2=4)

Tuple2(_1={"id":12,"name":"Dress"}, _2=5)
Tuple2(_1={"id":20,"name":"Jacket"}, _2=5)
Tuple2(_1={"id":21,"name":"Jeans"}, _2=5)
Tuple2(_1={"id":14,"name":"Jeans"}, _2=5)
Tuple2(_1={"id":13,"name":"Miniskirt"}, _2=5)
Tuple2(_1={"id":24,"name":"Shirt"}, _2=5)
Tuple2(_1={"id":17,"name":"Shirt"}, _2=5)
Tuple2(_1={"id":23,"name":"Suit"}, _2=5)
Tuple2(_1={"id":16,"name":"Suit"}, _2=5)
```

可见

-   无论Java还是Kotlin，使用`unionAllRecursively`是使用Recursive CTE的关键
-   createBaseQuery具备一个lambda参数形式的重载，接受代表CTE本身占位符的`recurisveRef`参数 *(kotlin中为`it`)*
-   Java需要记录`MutableRecursiveBaseQuery`类型的对象，以便通过其`.recursive()`去获取CTE自身所代表的表对象

:::info
其实，在SQL规范中，`union`也可以用于Recursive CTE，但通常被认为是低效操作，应该予以避免。

因此，Jimmer的Recursive CTE规定只能使用`union all`，不能使用`union`。
:::

## 无根表查询和修改操作的source

`createBaseQuery()`也可以不指定根实体或关联表，直接选择字面量、参数、原生表达式或标量
子查询。Kotlin使用不带实体类型的`createBaseQuery { select(...) }`。
[类型化元组mapper](./typed-tuple.md)可以为结果提供具名属性；[从查询插入的示例](../mutation/insert-from-select.md)
包含完整的Java/Kotlin声明以及无根表source。

结果可以通过`asBaseTable()`或`asCteBaseTable()`暴露，与其他兼容查询通过`union`/`unionAll`
组合，并被`createQuery(source)`或`createBaseQuery(source)`复用。

`createInsert`和`createUpsert`也接受base-table。Source可以使用连接、过滤、聚合、联合查询
或递归CTE。修改操作的单物理表限制针对目标，不针对source。
详情参阅[从查询插入和合并数据](../mutation/insert-from-select.md)。
