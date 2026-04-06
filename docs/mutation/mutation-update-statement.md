# 修改语句

> 来源: https://jimmer.deno.dev/zh/docs/mutation/update-statement

* [修改篇](/zh/docs/mutation/)
* 修改语句

本页总览

# 修改语句

## 基本用法[​](#基本用法 "基本用法的直接链接")

Update语句用法如下

* Java
* Kotlin

```
AuthorTable author = Tables.AUTHOR_TABLE;  
  
int affectedRowCount = sqlClient  
    .createUpdate(author)  
    .set(  
        author.firstName(),  
        author.firstName().concat("*")  
    )  
    .where(author.firstName().eq("Dan"))  
    .execute();  
System.out.println("Affected row count: " + affectedRowCount);
```

```
val affectedRowCount = sqlClient  
    .createUpdate(Author::class) {  
        set(  
            table.firstName,   
            concat(table.firstName, value("*"))  
        )  
        where(table.firstName eq "Dan")  
    }  
    .execute()  
println("Affected row count: $affectedRowCount")
```

最终生成的SQL

```
update AUTHOR tb_1_   
set FIRST_NAME = concat(tb_1_.FIRST_NAME, ?)   
where tb_1_.FIRST_NAME = ?
```

## 使用JOIN[​](#使用join "使用JOIN的直接链接")

默认情况下，update语句不支持join，这会导致异常

* Java
* Kotlin

```
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;  
  
int affectedRowCount = sqlClient  
    .createUpdate(author)  
    .set(  
        author.firstName(),  
        author.firstName().concat("*")  
    )  
    .where(  
        author  
            .books()  
            .name()  
            .eq("Learning GraphQL")  
    )  
    .execute();  
System.out.println("Affected row count: " + affectedRowCount);
```

```
val affectedRowCount = sqlClient  
    .createUpdate(Author::class) {  
        set(  
            table.firstName,  
            concat(table.firstName, value("*"))  
        )  
        where(  
            table  
                .books  
                .name   
                eq "Learning GraphQL"  
        )  
    }  
    .execute()  
println("Affected row count: $affectedRowCount")
```

异常信息如下

```
Table joins for update statement is forbidden by the current dialect, but there is a join `'Author.books'`.
```

当使用MySql或Postgres时，update语句可以使用JOIN语句。

### MySql[​](#mysql "MySql的直接链接")

首先，需要在创建SqlClient时，指定方言为MySqlDialect

* SpringBoot下的配置

  在`application.yml`或`application.properties`中声明方言

  ```
  jimmer:  
      dialect: org.babyfish.jimmer.sql.dialect.MySqlDialect
  ```
* 非SpringBoot下的配置

  + Java
  + Kotlin

  ```
  JSqlClient sqlClient = JSqlClient  
      .newBuilder()  
      .setDialect(  
          new org.babyfish.jimmer.sql.dialect.MySqlDialect()  
      )  
      ...  
      .build();
  ```

  ```
  val sqlClient = newKSqlClient {  
      setDialect(org.babyfish.jimmer.sql.dialect.MySqlDialect())  
  }
  ```

然后，就可以在update中使用JOIN了

* Java
* Kotlin

```
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;  
  
int affectedRowCount = sqlClient  
    .createUpdate(author)  
    .set(  
        author.firstName(),  
        author.firstName().concat("*")  
    )  
    .set(  
        author.books().name(),  
        author.books().name().concat("*")  
    )  
    .set(  
        author.books().store().name(),  
        author.books().store().name().concat("*")  
    )  
    .where(  
        author.books().store().name().eq("MANNING")  
    )  
    .execute();  
System.out.println("Affected row count: " + affectedRowCount);
```

```
val affectedRowCount = sqlClient  
    .createUpdate(Author::class) {  
        set(  
            table.firstName,  
            concat(table.firstName, value("*"))  
        )  
        set(  
            table.books.name,  
            concat(table.books.name, value("*"))  
        )  
        set(  
            table.books.store.name,  
            concat(table.books.store.name, value("*"))  
        )  
        where(  
            table.books.store.name eq "MANNING"  
        )  
    }  
    .execute()  
println("Affected row count: $affectedRowCount")
```

最终生成针对MySQL的SQL语句，如下：

```
update   
    AUTHOR tb_1_   
    inner join BOOK_AUTHOR_MAPPING as tb_2_   
        on tb_1_.ID = tb_2_.AUTHOR_ID   
    inner join BOOK as tb_3_   
        on tb_2_.BOOK_ID = tb_3_.ID   
    inner join BOOK_STORE as tb_4_   
        on tb_3_.STORE_ID = tb_4_.ID   
set   
    tb_1_.FIRST_NAME = concat(tb_1_.FIRST_NAME, ?),   
    tb_3_.NAME = concat(tb_3_.NAME, ?),   
    tb_4_.NAME = concat(tb_4_.NAME, ?)   
where   
    tb_4_.NAME = ?
```

### Postgres[​](#postgres "Postgres的直接链接")

首先，需要在创建SqlClient时，指定方言为PostgresDialect

* SpringBoot下的配置

  在`application.yml`或`application.properties`中声明方言配置配置

  ```
  jimmer:  
      dialect: org.babyfish.jimmer.sql.dialect.PostgresDialect
  ```
* 非SpringBoot下的配置

  + Java
  + Kotlin

  ```
  JSqlClient sqlClient = JSqlClient  
      .newBuilder()  
      .setDialect(  
          new org.babyfish.jimmer.sql.dialect.PostgresDialect()  
      )  
      ...  
      .build();
  ```

  ```
  val sqlClient = newKSqlClient {  
      setDialect(org.babyfish.jimmer.sql.dialect.PostgresDialect())  
  }
  ```

然后，就可以在update中使用JOIN了

* Java
* Kotlin

```
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;  
  
int affectedRowCount = sqlClient  
    .createUpdate(author)  
    .set(  
        author.firstName(),  
        author.firstName().concat("*")  
    )  
    .where(  
        author.books().store().name().eq("MANNING")  
    )  
    .execute();  
System.out.println("Affected row count: " + affectedRowCount);
```

```
val affectedRowCount = sqlClient  
    .createUpdate(Author::class) {  
        set(  
            table.firstName,  
            concat(table.firstName, value("*"))  
        )  
        where(  
            table.books.store.name eq "MANNING"  
        )  
    }  
    .execute()  
println("Affected row count: $affectedRowCount")
```

警告

和MySql不同，在Postgres中使用update语句的JOIN存在如下限制

1. 只能在`where`子句中使用表连接，不能在`set`子句中使用表连接。即Postgres还是只允许修改当前表的字段，支持连接到其它表仅仅是为了做条件过滤。
2. 连接路径可以具有多级，如`author.books().store()`，其中，`books()`是第1级，`store()`是第2级。其中，第一级连接的类型必须是`inner join`。

最终生成针对Postgres的SQL语句，如下：

```
update   
    AUTHOR tb_1_   
set   
    FIRST_NAME = concat(tb_1_.FIRST_NAME, ?)   
from BOOK_AUTHOR_MAPPING as tb_2_ ❶  
inner join BOOK as tb_3_ ❷  
    on tb_2_.BOOK_ID = tb_3_.ID   
inner join BOOK_STORE as tb_4_ ❸  
    on tb_3_.STORE_ID = tb_4_.ID   
where   
    tb_1_.ID = tb_2_.AUTHOR_ID ❹  
and   
    tb_4_.NAME = ?
```

信息

连接路径`author.books().store()`有两级，`books()`是第1级，`store()`是第2级。

1. 第一级`books()`涉及两张表

   * ❶ 处的`BOOK_AUTHOR_MAPPING`表，但这里缺少连接条件，连接条件在❹处补上。
   * ❷ 处的`BOOK`表
2. 第二级`store()`涉及一张表

   * ❹ `处的`BOOK\_STORE`表

可见，在Postgres的update语句中，直接和主表相关的表连接不能使用`join` + `on`的写法，必须等价变换为`from` + `where`的写法。

这就是Jimmer规定Postgres方言下update语句第一级连接的类型必须是`inner join`的原因。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mutation/update-statement.mdx)

最后于 **2025年9月16日** 更新