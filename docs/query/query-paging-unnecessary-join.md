# 表连接优化

> 来源: https://jimmer.deno.dev/zh/docs/query/paging/unnecessary-join

* [查询篇](/zh/docs/query/)
* [智能分页](/zh/docs/query/paging/)
* 表连接优化

本页总览

# 表连接优化

在上一篇文章中，我们提及，Jimmer不仅能根据`data-query`自动生成`count-query`，还能自动优化`count-query`。

由于`count-query`只关心数据的总行数，不在乎数据的顺序和格式。所以，原`data-query`中的某些table join可能不需要复制到`count-query`中。

Jimmer会自动优化`count-query`，让其从原data-query中复制尽可能少的table join。

## 优化规则[​](#优化规则 "优化规则的直接链接")

1. 被where子句使用的表连接，不能被优化。

   即，**仅**被原data-query的顶级查询的`select`或`order by`子句所使用的表连接，才可被优化。
2. 基于集合关联(一对多、多对多)的表连接，不能被优化。

   集合关联会导致重复数据，进而影响数据行数，因此这些表连接必需复制到`count-query`，无法优化。
3. 基于引用关联(一对一、多对一)的表连接，有可能被优化，需满足以下任何一个条件：

   * 连接类型为左外连接
   * 虽然连接类型是内连接，但关联基于非null真实外键。

     所谓真实外键，指数据库中存在外键约束，请参见[真假外键](/zh/docs/mapping/base/foreignkey)

综上描述，要判断原data-query中某个表连接是否可以在count-query中被自动删除，需采用如下优化规则：

|  |  |  |
| --- | --- | --- |
| 与 | 是否基于引用关联，即多对一或一对一 | |
| 是否 **仅** 被原data-query的顶级查询的select或orderBy子句所用 | |
| 或 | 是否是左外连接 |
| 关联非空，且数据库中存在外键约束 |

## 无法优化的场景示范[​](#无法优化的场景示范 "无法优化的场景示范的直接链接")

* Java
* Kotlin

```
BookTable book = Tables.BOOK_TABLE;  
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;  
  
ConfigurableRootQuery<BookTable, Book> query = sqlClient  
    .createQuery(book)  
    .where(  
        book.price().between(  
            new BigDecimal(20),  
            new BigDecimal(30)  
        )  
    )  
    .orderBy(book.store().name())  
    .orderBy(book.name())  
    .select(book);  
  
int rowCount = query.fetchUnlimitedCount();
```

```
val query = sqlClient.createQuery(Book::class) {  
    where(  
        table.price.between(  
            BigDecimal(20),  
            BigDecimal(30)  
        )  
    )  
    orderBy(table.store.name) // α  
    orderBy(table.name)  
    select(table)  
}  
  
val rowCount = query.fetchUnlimitedCount()
```

注释α处

虽然`table.store()`仅被orderBy子句使用，没有被where子句使用，但是

* `table.store()`是内连接
* `Book.store`关联可空

所以，优化规则并不能生效，count-query仍然需要保留`table.store()`，并最终在SQL中生成JOIN子句

```
select   
    count(tb_1_.ID)   
from BOOK as tb_1_   
inner join BOOK_STORE as tb_2_   
    on tb_1_.STORE_ID = tb_2_.ID  
where tb_1_.PRICE between ? and ?
```

## 可优化的场景示范[​](#可优化的场景示范 "可优化的场景示范的直接链接")

针对上文所讨论的无法被优化的这种情况，采用以下任何一种修改，都可以让优化生效

1. 把`Book.store`关联修改为非空
2. 采用左外连接

在这里，我们选用第二种方案，左外连接

* Java
* Kotlin

```
BookTable book = Tables.BOOK_TABLE;  
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;  
  
ConfigurableRootQuery<BookTable, Book> query = sqlClient  
    .createQuery(book)  
    .where(  
        book.price().between(  
            new BigDecimal(20),  
            new BigDecimal(30)  
        )  
    )  
    .orderBy(book.store(JoinType.LEFT).name())  
    .orderBy(book.name())  
    .select(book);  
  
int rowCount = query.fetchUnlimitedCount();
```

```
val query = sqlClient.createQuery(Book::class) {  
    where(  
        table.price.between(  
            BigDecimal(20),  
            BigDecimal(30)  
        )  
    )  
    orderBy(table.`store?`.name) // α  
    orderBy(table.name)  
    select(table)  
}  
  
val rowCount = query.fetchUnlimitedCount()
```

现在，优化可以生效，最终count-query生成SQL如下

```
select   
    count(tb_1_.ID)   
from BOOK as tb_1_   
where tb_1_.PRICE between ? and ?
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/paging/unnecessary-join.mdx)

最后于 **2025年9月16日** 更新