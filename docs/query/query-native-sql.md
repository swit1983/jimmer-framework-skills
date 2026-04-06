# Native表达式

> 来源: https://jimmer.deno.dev/zh/docs/query/native-sql

* [查询篇](/zh/docs/query/)
* Native表达式

本页总览

# Native表达式

NativeSQL表达式是一个重要的功能，数据库产品总会有特有的功能，需要把数据库产品特有的能力发挥出来。

## 例子一：正则表达式匹配[​](#例子一正则表达式匹配 "例子一：正则表达式匹配的直接链接")

这个例子，演示如何使用Oracle和HSQLDB的正则表达式匹配。

* Java
* Kotlin

```
AuthorTable table = Tables.AUTHOR_TABLE;  
  
List<Author> authors = sqlClient  
    .createQuery(table)  
    .where(  
        Predicate.sql(  
            "regexp_like(%e, %v)",  
            it -> it  
                .expression(table.firstName())  
                .value("^Ste(v|ph)en$")  
        )  
    )  
    .select(table)  
    .execute();
```

```
val authors = sqlClient  
    .createQuery(Author::class) {  
        where(  
            sql(Boolean::class, "regexp_like(%e, %v)") {  
                expression(table.firstName)  
                value("^Ste(v|ph)en$")  
            }  
        )  
        select(table)  
    }  
    .execute()
```

信息

在Java代码中，我们调用了`Predicate.sql`创建基于本地SQL的查询条件。事实上，其它表达式类型也支持NativeSQL表达式，`sql`函数共有5个

1. Predicate.sql(...)
2. Expression.string().sql(...)
3. Expression.numeric().sql(...)
4. Expression.comparable().sql(...)
5. Expression.any().sql(...)

kotlin没有这个问题，其API是统一的

`sql(...)`方法的第一个参数是SQL字符串模板，可以包含特殊符号`%e`和`%v`。

* `%e`: 即Expression，植入一个表达式
* `%v`: 即Value，植入一个值

`sql(...)`方法的第二个参数是一个可选的，是一个lambda表达式，lambda表达式的参数是一个对象，该对象支持两个方法：

* `expression(Expression<?>)`: 植入一个表达式，和SQL模板中的"%e"匹配。
* `value(Object)`: 植入一个值，和SQL模板中的"%v"匹配。

最终生成的SQL是

```
select   
    tb_1_.ID,   
    tb_1_.FIRST_NAME,   
    tb_1_.LAST_NAME,   
    tb_1_.GENDER   
from AUTHOR as tb_1_   
where  
    regexp_like(tb_1_.FIRST_NAME, ?)
```

## 例子二：分析函数[​](#例子二分析函数 "例子二：分析函数的直接链接")

让我们再来看一个例子，使用分析函数

* Java
* Kotlin

```
List<Tuple3<Book, Integer, Integer>> tuples = sqlClient  
    .createQuery(table)  
    .select(  
        table,  
        Expression.numeric().sql(  
            Integer.class,  
            "rank() over(order by %e desc)",  
            table.price()  
        ),  
        Expression.numeric().sql(  
            Integer.class,  
            "rank() over(partition by %e order by %e desc)",  
            new Expression[] { table.storeId(), table.price() }  
        )  
    )  
    .execute();
```

```
val tuples = sqlClient  
    .createQuery(Author::class) {  
        select(  
            table,  
            sql(Int::class, "rank() over(order by %e desc)") {  
                expression(table.price)  
            },  
            sql("rank() over(partition by %e order by %e desc)") {  
                expression(table.store.id)  
                expression(table.price)  
            }  
        )  
    }  
    .execute()
```

这里查询了三列

* 第一列：Book对象
* 第二列：书籍的价格在所有书籍中的名次
* 第二列：书籍的价格在所属书店的中的名次

生成的SQL如下

```
select  
    tb_1_.ID,  
    tb_1_.NAME,  
    tb_1_.EDITION,  
    tb_1_.PRICE,  
    tb_1_.STORE_ID,  
    rank() over(  
        order by tb_1_.PRICE desc  
    ),  
    rank() over(  
        partition by tb_1_.STORE_ID   
        order by tb_1_.PRICE desc  
    )  
from BOOK tb_1_
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/native-sql.mdx)

最后于 **2025年9月16日** 更新