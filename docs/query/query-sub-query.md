# 子查询

> 来源: https://jimmer.deno.dev/zh/docs/query/sub-query

- [查询篇](/zh/docs/query/)
- 子查询

本页总览

# 子查询

## 有类型子查询[​](#有类型子查询 "有类型子查询的直接链接")

### 基于单列的IN表达式[​](#基于单列的in表达式 "基于单列的IN表达式的直接链接")

- Java
- Kotlin

```booktable book = tables.book_table;
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;

List<Book> books = sqlClient
    .createQuery(book)
    .where(
        book.id().in(sqlClient
            .createSubQuery(author)
            .where(author.firstName().eq("Alex"))
            .select(author.books().id())
        )
    )
    .select(book)
    .execute();

```

```val books = sqlclient
    .createQuery(Book::class) {
        where(
            table.id valueIn subQuery(Author::class) {
                where(table.firstName eq "Alex")
                select(table.books.id)
            }
        )
        select(table)
    }
    .execute()

```

最终生成的SQL如下

```select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_
where
    tb_1_.ID in (
        select
            tb_3_.BOOK_ID
        from AUTHOR as tb_2_
        inner join BOOK_AUTHOR_MAPPING as tb_3_
            on tb_2_.ID = tb_3_.AUTHOR_ID
        where
            tb_2_.FIRST_NAME = ?
    )

```

### 基于多列的IN表达式[​](#基于多列的in表达式 "基于多列的IN表达式的直接链接")

- Java
- Kotlin

```booktable book = tables.book_table;
List<Book> newestBooks = sqlClient
    .createQuery(book)
    .where(
        Expression.tuple(
            book.name(),
            book.edition()
        ).in(sqlClient
            .createSubQuery(book)
            .groupBy(book.name())
            .select(
                book.name(),
                book.edition().max()
            )
        )
    )
    .select(book)
    .execute();

```

```val newestbooks = sqlclient
    .createQuery(Book::class) {
        where(
            tuple(
                table.name,
                table.edition
            ) valueIn subQuery(Book::class) {
                groupBy(table.name)
                select(
                    table.name,
                    max(table.edition).asNonNull()
                )
            }
        )
        select(table)
    }
    .execute()

```

最终生成的SQL如下

```select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_
where
    (tb_1_.NAME, tb_1_.EDITION) in (
        select
            tb_2_.NAME,
            max(tb_2_.EDITION)
            from BOOK as tb_2_
            group by tb_2_.NAME
    )

```

### 将子查询视为简单值[​](#将子查询视为简单值 "将子查询视为简单值的直接链接")

- Java
- Kotlin

```booktable book = tables.book_table;
List<Book> newestBooks = sqlClient
    .createQuery(book)
    .where(
        book.price().gt(sqlClient
            .createSubQuery(book)
            .groupBy(book.name())
            .select(
                book
                    .price()
                    .avg()
                    .coalesce(BigDecimal.ZERO)
            )
        )
    )
    .select(book)
    .execute();

```

```val books = sqlclient
    .createQuery(Book::class) {
        where(
            table.price gt subQuery(Book::class) {
                select(
                    avg(table.price)
                        .coalesce(BigDecimal.ZERO)
                )
            }
        )
        select(table)
    }
    .execute()

```

最终生成的SQL如下

```select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_
where
    tb_1_.PRICE > (
        select
            coalesce(avg(tb_2_.PRICE), ?)
        from BOOK as tb_2_
    )

```

### 在select和orderBy子句中使用子查询[​](#在select和orderby子句中使用子查询 "在select和orderBy子句中使用子查询的直接链接")

- Java
- Kotlin

```bookstoretable store = tables.book_store_table;
BookTable book = Tables.BOOK_TABLE;

MutableRootQuery<BookStoreTable> query =
    sqlClient.createQuery(store);
TypedSubQuery<BigDecimal> subQuery =
    sqlClient
        .createSubQuery(book)
        .where(book.store().eq(store))
        .select(
            book
                .price()
                .avg()
                .coalesce(BigDecimal.ZERO)
        );
List<Tuple2<BookStore, BigDecimal>> storeAvgPriceTuples =
    query

        .orderBy(
            subQuery.desc()
        )
        .select(
            store,
            subQuery
        )
        .execute();

```

```val storeavgpricetuples = sqlclient
    .createQuery(BookStore::class) {
        val avgPriceSubQuery = subQuery(Book::class) {
            where(table.store eq parentTable)
            select(avg(table.price))
        }

        orderBy(
            avgPriceSubQuery.desc()
        )
        select(
            table,
            avgPriceSubQuery
        )
    }
    .execute()

```

最终生成的SQL如下

```select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.WEBSITE,
    (
        select coalesce(avg(tb_2_.PRICE), ?)
        from BOOK as tb_2_
    )
from BOOK_STORE as tb_1_
order by (
    select coalesce(avg(tb_2_.PRICE), ?)
    from BOOK as tb_2_
) desc

```

### 使用any运算符[​](#使用any运算符 "使用any运算符的直接链接")

- Java
- Kotlin

```booktable book = tables.book_table;
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;

List<Book> books = sqlClient
    .createQuery(book)
    .where(
        book.id().eq(sqlClient
            .createSubQuery(author)
            .where(
                author.firstName().in(
                    Arrays.asList("Alex", "Bill")
                )
            )
            .select(author.books().id())
            .any()
        )
    )
    .select(book)
    .execute();

```

```val books = sqlclient
    .createQuery(Book::class) {
        where(
            table.id eq any(
                subQuery(Author::class) {
                    where(
                        table.firstName valueIn listOf(
                            "Alex",
                            "Bill"
                        )
                    )
                    select(table.id)
                }
            )
        )
        select(table)
    }
    .execute()

```

最终生成的SQL如下

```select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_
where tb_1_.ID =
    any(
        select
            tb_3_.BOOK_ID
        from AUTHOR as tb_2_
        inner join BOOK_AUTHOR_MAPPING as tb_3_
            on tb_2_.ID = tb_3_.AUTHOR_ID
        where
            tb_2_.FIRST_NAME in (?, ?)
    )

```

### 使用all运算符[​](#使用all运算符 "使用all运算符的直接链接")

- Java
- Kotlin

```booktable book = tables.book_table;
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;

List<Book> books = sqlClient
    .createQuery(book)
    .where(
        book.id().ne(sqlClient
            .createSubQuery(author)
            .where(
                author.firstName().in(
                    Arrays.asList("Alex", "Bill")
                )
            )
            .select(author.books().id())
            .all()
        )
    )
    .select(book)
    .execute();

```

```val books = sqlclient
    .createQuery(Book::class) {
        where(
            table.id ne all(
                subQuery(Author::class) {
                    where(
                        table.firstName valueIn listOf(
                            "Alex",
                            "Bill"
                        )
                    )
                    select(table.id)
                }
            )
        )
        select(table)
    }
    .execute()

```

最终生成的SQL如下

```select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_
where tb_1_.ID =
    all(
        select
            tb_3_.BOOK_ID
        from AUTHOR as tb_2_
        inner join BOOK_AUTHOR_MAPPING as tb_3_
            on tb_2_.ID = tb_3_.AUTHOR_ID
        where
            tb_2_.FIRST_NAME in (?, ?)
    )

```

### 使用exists运算符[​](#使用exists运算符 "使用exists运算符的直接链接")

- Java
- Kotlin

```booktable book = tables.book_table;
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;

List<Book> books = sqlClient
    .createQuery(book)
    .where(sqlClient
        .createSubQuery(author)
        .where(
            author.books().eq(book),
            author.firstName().eq("Alex")
        )
        .select(author)
        .exists()
    )
    .select(book)
    .execute();

```

```val books = sqlclient
    .createQuery(Book::class) {
        where(
            exists(
                subQuery(Author::class) {
                    where(
                        table.books eq parentTable,
                        table.firstName eq "Alex"
                    )
                    select(table)
                }
            )
        )
        select(table)
    }
    .execute()

```

最终生成的SQL如下

```select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_
where
    exists (
        select
            1
        from AUTHOR as tb_2_
        inner join BOOK_AUTHOR_MAPPING as tb_3_
            on tb_2_.ID = tb_3_.AUTHOR_ID
        where
            tb_1_.ID = tb_3_.BOOK_ID
        and
            tb_2_.FIRST_NAME = ?
    )

```

信息

注意，在最终生成的SQL中，子查询选取的列是常量`1`，并非Java/Kotlin代码的设置。

这是因为`exists`运算符只在乎子查询是否能匹配到数据，并不在乎子查询选取了那些列。无论你在Java/Kotlin代码中让子查询选取什么，都会被无视。

## 无类型子查询[​](#无类型子查询 "无类型子查询的直接链接")

上一节最后一个例子是`exists`子查询，无论你在Java代码中让子查询选取什么都会被无视。

既然如此，为什么要为`exists`子查询指定返回格式呢？

因此，jimmer-sql支持无类型子查询(Wild sub query)，和普通子查询不同，无类型子查询实现中，不再需要最后那一句select方法调用，即，不需要返回类型。

- Java
- Kotlin

```booktable book = tables.book_table;
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;

List<Book> books = sqlClient
    .createQuery(book)
    .where(sqlClient
        .createSubQuery(author)
        .where(
            author.books().eq(book),
            author.firstName().eq("Alex")
        )
        // 此处无select
        .exists()
    )
    .select(book)
    .execute();

```

```val books = sqlclient
    .createQuery(Book::class) {
        where(
            exists(
                wildSubQuery(Author::class) {
                    where(
                        table.books eq parentTable,
                        table.firstName eq "Alex"
                    )
                    // 此处无select
                }
            )
        )
        select(table)
    }
    .execute()

```

最终生成的SQL不变，仍然是

```select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_
where
    exists (
        select
            1
        from AUTHOR as tb_2_
        inner join BOOK_AUTHOR_MAPPING as tb_3_
            on tb_2_.ID = tb_3_.AUTHOR_ID
        where
            tb_1_.ID = tb_3_.BOOK_ID
        and
            tb_2_.FIRST_NAME = ?
    )

```

信息

无类型子查询唯一的价值，就是和`exists`运算符配合。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/sub-query.mdx)

最后于 **2025年9月16日** 更新