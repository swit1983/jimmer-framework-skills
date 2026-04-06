# Base-Query

> 来源: https://jimmer.deno.dev/zh/docs/query/base-query

- [查询篇](/zh/docs/query/)
- Base-Query

本页总览

# Base-Query

所谓Base-Query，即SQL中的`Derived Table`，`CTE`和`Recursive CTE`的统称。

Jimmer使用强类型DSL对`Derived Table`，`CTE`和`Recursive CTE`进行了统一支持。

提示

和SQL语言的这些概念不同，Jimmer额外提供了一个全新颠覆性特性：**投影反向传播**。

这是一套全新的设计哲学，也是本文要讨论的重点。

## 初识Derived Table[​](#初识derived-table "初识Derived Table的直接链接")

所谓Derived Table就是利用一个查询作为其他查询的`from`或`join`的基表。例如

```select a, b, c
from (
    select a, b, c, d from t where ....
) derived_table
where d between 1 and 10

```

Jimmer实现derived table的方式如下

- Java
- Kotlin

```booktable book = booktable.$;
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

```val basetable =
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

- 和`createQuery`创建真实查询不同，`createBaseQuery`创建的base-query不会被直接执行。
  其目的仅仅在于构建derived table，供后续`createQuery`创建真正的查询使用 *(作为from或join的基表使用)*
- 内部的base-query返回了两列 *(第一列是表对象，第二列是简单表达式)*。外部查询使用`get_1()`/`_1`和`get_2()`/`_2`来使用他们

  > base-query支持返回1~9列
- 和`createQuery`以及`createSubQuery`*(截止目前为止暂时没有被介绍，后学章节出现)* 不同，
  `createBaseQuery`不使用`select(selection1, selection2, ..., selectionN)`方法指定投影，而是

  - Java使用`.addSelect(...).addSelect(...)...addSelect(...)`风格的代码指定投影
  - Kotlin使用`selections.add(...).add(...)...add(...)`风格的代码指定投影

  信息

  - 须采用链式编程风格，语法层面不可中断，才能保证成功的编译
  - base-query采用特殊的投影指定方法是为了实现稍后即将介绍的重要特性：**投影反向传播**，这里请读者先行忽略此设计的意图。

此查询会生成如下SQL

```select
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

```[
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

## 投影反向传播[​](#投影反向传播 "投影反向传播的直接链接")

### BaseQuery独有的投影列设置[​](#basequery独有的投影列设置 "BaseQuery独有的投影列设置的直接链接")

上文提及

> 和`createQuery`以及`createSubQuery`*(截止目前为止暂时没有被介绍，后学章节出现)* 不同，`createBaseQuery`不使用`select(selection1, selection2, ..., selectionN)`方法制定投影，而是
>
> * Java使用`.addSelect(...).addSelect(...)...addSelect(...)`风格的代码指定投影
> * Kotlin使用`selections.add(...).add(...)...add(...)`风格的代码指定投影

警告

须采用链式编程风格，语法层面不可中断，才能保证成功的编译

`createBaseQuery`如此设计的，是为了只允许如下两种操作

1. 查询简表达式

   - Java
   - Kotlin

   ```.addselect(
       Expression.concat(
           table.firstName(),
           Expression.constant(" "),
           table.lastName()
       )
   )

```

   ```selections.add(
       concat(
           table.firstName,
           constant(" "),
           table.lastName
       )
   )

```

- 查询表对象

  - Java
  - Kotlin

  ```.addselect(table)
  .addSelect(table.store())
  .addSelect(table.asTableEx().authors())

```

  ```selections
      .add(table)
      .add(table.store)
      .add(table.asTableEx().authors)

```

  信息

  即

  1. 要么返回简单表达式
  2. 要么返回对象 *(形状未知)*

  其中，返回形状未知的表对象 *(2)*，才是投影反向传播的所关心的话题，是本章节的重点。

  > 注意：不能对返回的表对象施加[对象抓取器](/zh/docs/query/object-fetcher/)，或返回[Output DTO](/zh/docs/query/object-fetcher/dto)

而如下方式是不被允许的

- 禁止使用[对象抓取器](/zh/docs/query/object-fetcher/)的对象查询

  - Java
  - Kotlin

  ```// 注意，这里示范的是非法代码
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

  ```// 注意，这里示范的是非法代码
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

- 禁止返回[Output DTO](/zh/docs/query/object-fetcher/dto)

  - Java
  - Kotlin

  ```// 注意，这里示范的是非法代码
  .addSelect(table.fetch(BookView.class))

```

  ```// 注意，这里示范的是非法代码
  selections.add(table.fetch(BookView::class))

```

信息

基于[对象抓取器](/zh/docs/query/object-fetcher/)/[Output DTO](/zh/docs/query/object-fetcher/dto)的对象查询是Jimmer最核心的功能之一，
BaseQuery投影查询的的API却不允许这样查询。

这绝非为了禁止使用此能力；**恰恰相反**，这是为了给予更好的抽象，
更好地使用[对象抓取器](/zh/docs/query/object-fetcher/)和[Output DTO](/zh/docs/query/object-fetcher/dto)。

### 投影反向传播机制介绍[​](#投影反向传播机制介绍 "投影反向传播机制介绍的直接链接")

在原生SQL中，基于dervided table的查询往往是这样的。

```select
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

> 其中，M <= N

我们先决定内部base query的返回列集合 `(c1, c2, ..., cN)`，然后在选择其中的一个子集合 `(c1, c2, ..., cM)` *(M <= N)*，让外部真实查询返回。

很明显，这是一个自内向外的投影传播过程，内部查询的投影和外部查询的投影包含大量的重复列，这是一件非常繁琐且容易出错的事。
在列的数量较多的时候，这对开发、重构和维护极为不利。不幸的是，实际项目几乎总是如此。

提示

因此, Jimmer采用 **自外向内** 的投影反向传播机制

- 内部的base-query如果如果返回对象，必须返回原始的table对象，不能使用[对象抓取器](/zh/docs/query/object-fetcher/)或[Output DTO](/zh/docs/query/object-fetcher/dto)，表示返回的对象的形状是未知的
- 最终，内部base-query应该返回的哪些具体的列，而是根据外部查询的需要自动决定的，无需在base-query中显式指定。

  外部查询控制内部base-query应该返回哪些具体列的方法有：

  - [对象抓取器](/zh/docs/query/object-fetcher/)
  - [Output DTO](/zh/docs/query/object-fetcher/dto)类型
  - DSL中被使用的列属性

  投影反向传播不仅能让大幅答复简化，还带了原始SQL不具备的智能行实用性。

现在以[对象抓取器](/zh/docs/query/object-fetcher/)为例，编写一个函数

- Java
- Kotlin

```private list<book> findbooks(
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

```private fun findbooks(
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

- 简单的[对象抓取器](/zh/docs/query/object-fetcher/)

  - Java
  - Kotlin

  ```list<book> books = findbooks(
      BookFetcher.$
          .name()
  );

```

  ```val books = findbooks(
      newFetcher(Book::class).by {
          name()
      }
  )

```

  由于[对象抓取器](/zh/docs/query/object-fetcher/)很简单，内外查询返回的列都较少，生成如下SQL

  ```select
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

  ```[
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

- 中等复杂的[对象抓取器](/zh/docs/query/object-fetcher/)

  - Java
  - Kotlin

  ```list<book> books = findbooks(
      BookFetcher.$
          .allScalarFields()
  );

```

  ```val books = findbooks(
      newFetcher(Book::class).by {
          allScalarFields()
      }
  )

```

  由于[对象抓取器](/zh/docs/query/object-fetcher/)相对复杂一些，内外查询返回的列都变多，生成如下SQL

  ```select
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

  ```[
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

- 复杂的[对象抓取器](/zh/docs/query/object-fetcher/)

  - Java
  - Kotlin

  ```list<book> books = findbooks(
      BookFetcher.$
          .name()
          .store(
              ReferenceFetchType.JOIN_ALWAYS,
              BookStoreFetcher.$.name()
          )
  );

```

  ```val books = findbooks(
      newFetcher(Book::class).by {
          name()
          store(ReferenceFetchType.JOIN_ALWAYS) {
              name()
          }
      }
  )

```

  由于[对象抓取器](/zh/docs/query/object-fetcher/)包含join fetch操作，查询语句自然也会通过join抓取关联对象，生成如下SQL

  ```select
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

  ```[
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

提示

可见，所谓投影反向传播，就是内部的base-query并不直接控制返回的表对象的格式 *(返回原始表对象并视之为形状未知)*，返回的对象格式统一由外部查询控制，
内部的base-query按照外部查询的需要自动决定返回哪些列。

### 集合操作的投影传播[​](#集合操作的投影传播 "集合操作的投影传播的直接链接")

SQL支持`UNION`、`UNION ALL`、`INTERSECT`和`MINUS`这样集合操作，base-query一样，让我们修改`findBooks`如下

- Java
- Kotlin

```private list<book> findbooks(
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

```private fun findbooks(
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

- 简单的[对象抓取器](/zh/docs/query/object-fetcher/)

  - Java
  - Kotlin

  ```list<book> books = findbooks(
      BookFetcher.$
          .name()
  );

```

  ```val books = findbooks(
      newFetcher(Book::class).by {
          name()
      }
  )

```

  由于[对象抓取器](/zh/docs/query/object-fetcher/)很简单，内外查询返回的列都较少，生成如下SQL

  ```select
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

  ```[
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

- 相对复杂的[对象抓取器](/zh/docs/query/object-fetcher/)

  - Java
  - Kotlin

  ```list<book> books = findbooks(
      BookFetcher.$
          .allScalarFields()
  );

```

  ```val books = findbooks(
      newFetcher(Book::class).by {
          allScalarFields()
      }
  )

```

  由于[对象抓取器](/zh/docs/query/object-fetcher/)相对复杂一些，内外查询返回的列都变多，生成如下SQL

  ```select
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

  ```[
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

提示

在投影形状从外向内的反向传播过程中，如果base-query本身使用了集合操作 *(`UNION`、`UNION ALL`、`INTERSECT`或`MINUS`)*，
那么这个传播过程会分裂，影响参与集合运算的每一个更细小的base-query。

### 投影列合并[​](#投影列合并 "投影列合并的直接链接")

事实上，[对象抓取器](/zh/docs/query/object-fetcher/)和[Output DTO](/zh/docs/query/object-fetcher/dto)并非导致投影反向传播的所有原因。

如果base-query返回形状未知表对象，而外部查询使用SQL DSL依赖该表对象的某些属性，一样可以导致投影反向传播机制。

- Java
- Kotlin

```private list<book> findbooks(
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

```private fun findbooks(
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

1. 不触发投影反向传播

   - Java
   - Kotlin

   ```list<book> books = findbooks(
       null,
       BookFetcher.$,
   )

```

   ```val books = findbooks(
       null,
       newFetcher(Book::class).by {  }
   )

```

   这里

   - name参数无效，动态谓词 *(java的`eqIf`，以及kotlin的`eq?`)* 导致外部查询的where被忽略
   - [对象抓取器](/zh/docs/query/object-fetcher/)参数过于简单，只需要`id-only`的对象

   因此，最终没有任何非id字段的投影诉求被反向传播给base-query，最终生成的SQL语句非常简单

   ```select
       tb_1_.c1
   from (
       select
           tb_2_.ID c1
       from BOOK tb_2_
       where
           tb_2_.EDITION = ? /* 3 */
   ) tb_1_

```

2. 外部查询添加where条件触发投影反向传播 *(但并未通过[对象抓取器](/zh/docs/query/object-fetcher/)触发)*，让base-query返回name列

   - Java
   - Kotlin

   ```list<book> books = findbooks(
       "GraphQL in Action",
       BookFetcher.$,
   )

```

   ```val books = findbooks(
       "GraphQL in Action",
       newFetcher(Book::class).by {  }
   )

```

   生成的SQL如下

   ```select
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

3. 外部查询

   - 既通过where条件触发投影反向传播，要求内部base-query返回name列
   - 也通过[对象抓取器](/zh/docs/query/object-fetcher/)触发投影反向传播，要求内部base-query返回name列

   base-query会自动 **合并** 二者，只查询  一次name列

   - Java
   - Kotlin

   ```list<book> books = findbooks(
       "GraphQL in Action",
       BookFetcher.$.name(),
   )

```

   ```val books = findbooks(
       "GraphQL in Action",
       newFetcher(Book::class).by {
           name()
        }
   )

```

   生成的SQL如下

   ```select
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

   可见，外部查询通过两种不同的方式 *([对象抓取器](/zh/docs/query/object-fetcher/)和where条件)* 触发投影反向传播 *(需要内部base-query返回name列)*
   内部查询只把name列查询了一次。

   即，外部查询中不同的投影反向传播诉求中相同的列的投影诉求会被自动合并。

   提示

   由于投影反向传播会自动合并相同的列请求，只有当表达式非常复杂时，利用base-query返回表达式列才会显得有意义。

   因此。不建议内部base-query单独返回简单的列属性表达式。大部分情况下，内部base-query直接返回原始table对象即可。

## Weak Join[​](#weak-join "Weak Join的直接链接")

在这里，我们

- 将原始实体类型所对应的表称为实体表，Entity Table
- 将base-query构建的临时表成为派生表，Derived Table

那么，无论实体表和派生表，都可以对派生表进行`weakJoin`操作。

信息

Jimmer表连接有两个重要特性

- 无用的表连接会被自动忽略，请见[优化不必要连接](/zh/docs/query/dynamic-join/optimization)
- 冲突的表连接会被自动合并，请见[合并冲突连接](/zh/docs/query/dynamic-join/merge)

由于这些内容在相关章节中已经被详细论述，本文不做重复，仅罗列和派生表相关的的JOIN操作的书写方法。

### 派生表 JOIN 派生表[​](#派生表-join-派生表 "派生表 JOIN 派生表的直接链接")

- Java
- Kotlin

```booktable rawbook = booktable.$;
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

```val basebook = basetablesymbol {
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

```select
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

### 实体表 JOIN 派生表[​](#实体表-join-派生表 "实体表 JOIN 派生表的直接链接")

- Java
- Kotlin

```booktable book = booktable.$;
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

```val baseauthor = basetablesymbol {
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

```select
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

## CTE[​](#cte "CTE的直接链接")

所谓CTE，就是把之前Drived Table的SQL

```select ...
from (
    select ...
    from my_table
) tb_1_

```

改写为CTE风格的SQL，如下

```with tb_1_(...) as (
    select ...
    from my_table
)
select ...
from tb_1_

```

除了生成的SQL采用不同的写法外，**功能层面和前文阐述的内容并无任何差异**。

因此，我们把本文开头的第一个例子该用CTE的写法改写即可，不作过多阐述。

- Java
- Kotlin

```booktable book = booktable.$;
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

```val basetable =
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

- Java代码使用`asCteBaseTable`，而非`asBaseTable`
- Kotlin代码使用`cteBaseTableSymbol`，而非`baseTableSymbol`

最终生成的SQL如下

```with tb_1_(c1, c2, c3, c4, c5, c6) as (
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

## Recursive-CTE[​](#recursive-cte "Recursive-CTE的直接链接")

SQL的CTE有一个非常重要的功能，就是在利用union all操作构建base-query时

- union all操作的第一部分简单地选取一些初始数据
- union all操作的第二个部分对 **CTE本身** 进行join

由于定义CTE的查询语句join CTE所定义的派生表本身，所以，这是一种递归查询

> 在处理自关联表所描述树形结构时，Recursive CTE非常有用

- Java
- Kotlin

```treenodetable table = treenodetable.$;
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

```val basetable = ctebasetablesymbol {
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

```with recursive tb_1_(c1, c2, c3) as (
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

最终查询得到如下数据 *(为了提高可读性  ，手动引入了一些空行)*

```tuple2(_1={"id":1,"name":"home"}, _2=1)
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

- 无论Java还是Kotlin，使用`unionAllRecursively`是使用Recursive CTE的关键
- createBaseQuery具备一个lambda参数形式的重载，接受代表CTE本身占位符的`recurisveRef`参数 *(kotlin中为`it`)*
- Java需要记录`MutableRecursiveBaseQuery`类型的对象，以便通过其`.recursive()`去获取CTE自身所代表的表对象

信息

其实，在SQL规范中，`union`也可以用于Recursive CTE，但通常被认为是低效操作，应该予以避免。

因此，Jimmer的Recursive CTE规定只能使用`union all`，不能使用`union`。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/base-query.mdx)

最后于 **2025年9月16日** 更新