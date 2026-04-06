# 分页安全性

> 来源: https://jimmer.deno.dev/zh/docs/query/dynamic-join/table-ex

* [查询篇](/zh/docs/query/)
* [动态JOIN](/zh/docs/query/dynamic-join/)
* 分页安全性

本页总览

# 分页安全性

分页安全性是为分页设计的功能，但是表连接功能为其提供底层支持。

## 集合关联的问题[​](#集合关联的问题 "集合关联的问题的直接链接")

这里，我们先介绍两种关联属性，引用关联和集合关联。

* 被`@OneToOne`或`@ManyToOne`修饰的关联属性，称为引用关联
* 被`@OneToMany`或`@ManyToMany`修饰的关联属性，称为集合关联

请参考例子,

关联分类的例子

### 集合JOIN导致重复查询结果[​](#集合join导致重复查询结果 "集合JOIN导致重复查询结果的直接链接")

如果我们把当前被查询的第一张表所对应的实体对象称为聚合根对象，引用关联和集合关联之间有如下差异：

* 查询一个对象，通过引用关联进行table join，在SQL查询结果中，当前主对象不会出现重复记录。例如：

  ```
  select   
      b.id as root_id,  
      b.name as root_name,  
      s.id as associated_id,  
      s.name as associated_name   
  from book b  
  inner join book_store s  
      on b.store_id = s.id  
  where b.id = 1;
  ```

  查询结果如下，聚合根对象没有出现重复

  | root\_id | root\_name | associated\_id | associated\_name |
  | --- | --- | --- | --- |
  | 1 | Learning GraphQL | 1 | O'REILLY |
* 查询一个对象，通过集合关联进行table join，在SQL查询结果中，当前主对象会出现重复记录：

  ```
  select   
      b.id as root_id,  
      b.name as root_name,  
      a.id as associated_id,  
      a.first_name as associated_first_name,  
      a.last_name as associated_last_name   
  from book b  
  inner join book_author_mapping m  
      on b.id = m.book_id  
  inner join author a  
      on m.author_id = a.id  
  where b.id = 1;
  ```

  查询结果如下，聚合根对象有可能出现重复：

  | root\_id | root\_name | associated\_id | associated\_first\_name | associated\_last\_name |
  | --- | --- | --- | --- | --- |
  | 1 | Learning GraphQL | 1 | Eve | Procello |
  | 1 | Learning GraphQL | 2 | Alex | Banks |

### 查询结果重复的危害[​](#查询结果重复的危害 "查询结果重复的危害的直接链接")

基于集合关联的table join会导致重复数据，进而引发以下问题：

1. 如果开发者忘记去重，就会引入bug。
2. 即便开发者没有忘记去重，在客户端使用`java.util.LinkedHashSet`进行去重，也并非一个好的选择。
   因为事后的去重操作也不能改变数据库返回的原始结果包含重复数据既定事实，且会消耗额外的网络传输和JVM处理数据的成本。
3. **最重要的一点，对分页查询不友好**。

   在SQL层面对table join连接结果进行分页，往往不是人们所需；更多的场景下，人们希望分页操作被应用在聚合根对象上。

   以Hibernate为例，在这种情况下，Hibernate不得不放弃SQL层面的分页策略，而采用内存层面的分页策略。这样做性能非常低下，为了引起开发人员的重视，Hibernate会在日志中进行告警。如果读者你有Hibernate使用经验，相信下面这条日志会让你感到非常头疼。

   [firstResult/maxResults specified with collection fetch; applying in memory](https://tech.asimio.net/2021/05/19/Fixing-Hibernate-HHH000104-firstResult-maxResults-warning-using-Spring-Data-JPA.html)

   提示

   注意，Jimmer的另外一个功能[对象抓取器](/zh/docs/query/object-fetcher)，并不是基于table join来实现对集合关联属性进行fetch的，故而无此问题，请放心使用。

由于SQL支持子查询，我们把最外层的查询称为顶层查询。综上，在顶级查询中使用集合连接的缺点非常明显，但不可否认，在子查询中使用集合连接仍然有客观的价值。

所以，Jimmer的SQL DSL有这样的特色

信息

1. 基于集合关联的table join，在**顶级**查询中需要被禁止。
2. 基于集合关联的table join，在子查询、update语句和delete语句中仍然可用。

## 隐含子查询[​](#隐含子查询 "隐含子查询的直接链接")

提示

对于集合关联而言，其实不推荐JOIN，更推荐[隐式子查询](/zh/docs/query/implicit-subquery)。

如果仍然要对集合关联属性进行JOIN，请继续阅读下文。

## Table和TableEx[​](#table和tableex "Table和TableEx的直接链接")

在Jimmer的SQL DSL中，存在两种表对象，`Table<E>`和`TableEx<E>`。

* Table

  特点：只能通过引用关联进行join，无法通过集合关联属性进行join

  Java类型：org.babyfish.jimmer.sql.ast.table.Table<E>

  Kotlin类型：org.babyfish.jimmer.sql.kt.ast.table.KTable<E>
* TableEx

  特点：可以通过任何关联属性进行join

  Java类型：org.babyfish.jimmer.sql.ast.table.TableEx<E>

  Kotlin类型：org.babyfish.jimmer.sql.kt.ast.table.KTableEx<E>

### 生成的代码[​](#生成的代码 "生成的代码的直接链接")

为了实现强类型的SQL DSL，Jimmer使用Annotation processor(Java)或KSP(kotlin)，根据用户定义的实体接口生成SQL DSL相关的源代码。

还是以文章开头的Book实体接口为例，如下两个类型会被自动生成

* Java
* Kotlin

Generated java code

```
/*   
 * BookTable.java  
 */  
package org.babyfish.jimmer.sql.example.model;  
  
import java.lang.Integer;  
import java.math.BigDecimal;  
import javax.persistence.criteria.JoinType;  
import org.babyfish.jimmer.sql.ast.Expression;  
import org.babyfish.jimmer.sql.ast.PropExpression;  
import org.babyfish.jimmer.sql.ast.table.Table;  
import org.babyfish.jimmer.sql.ast.table.spi.AbstractTableWrapper;  
  
public class BookTable extends AbstractTableWrapper<Book> {  
    public BookTable(Table<Book> table) {  
        super(table);  
    }  
  
    public Expression<Long> id() {  
        return get("id");  
    }  
  
    public PropExpression.Str name() {  
        return get("name");  
    }  
  
    public PropExpression.Num<Integer> edition() {  
        return get("edition");  
    }  
  
    public PropExpression.Num<BigDecimal> price() {  
        return get("price");  
    }  
  
    public BookStoreTable store() {  
        return join("store");  
    }  
  
    public BookStoreTable store(JoinType joinType) {  
        return join("store", joinType);  
    }  
}  
  
/*   
 * BookTableEx.java  
 */  
package org.babyfish.jimmer.sql.example.model;  
  
import javax.persistence.criteria.JoinType;  
import org.babyfish.jimmer.sql.ast.table.TableEx;  
  
public class BookTableEx extends BookTable implements TableEx<Book> {  
    public BookTableEx(TableEx<Book> table) {  
        super(table);  
    }  
  
    public AuthorTableEx authors() {  
        return join("authors");  
    }  
  
    public AuthorTableEx authors(JoinType joinType) {  
        return join("authors", joinType);  
    }  
}
```

Generated kotlin code

```
package org.babyfish.jimmer.example.kt.sql.model  
  
import java.math.BigDecimal  
import org.babyfish.jimmer.sql.ast.Selection  
import org.babyfish.jimmer.sql.kt.ast.expression.KNonNullPropExpression  
import org.babyfish.jimmer.sql.kt.ast.expression.KNullablePropExpression  
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable  
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTableEx  
import org.babyfish.jimmer.sql.kt.ast.table.KNullableTable  
import org.babyfish.jimmer.sql.kt.ast.table.KNullableTableEx  
import org.babyfish.jimmer.sql.kt.ast.table.KTable  
import org.babyfish.jimmer.sql.kt.ast.table.KTableEx  
  
/*   
 * Extension for Table<Book>  
 */  
public val KTable<Book>.id: KNullablePropExpression<Long>  
    get() = get("id")  
  
public val KNonNullTable<Book>.id: KNonNullPropExpression<Long>  
    get() = get("id")  
  
public val KTable<Book>.name: KNullablePropExpression<String>  
    get() = get("name")  
  
public val KNonNullTable<Book>.name: KNonNullPropExpression<String>  
    get() = get("name")  
  
public val KTable<Book>.edition: KNullablePropExpression<Int>  
    get() = get("edition")  
  
public val KNonNullTable<Book>.edition: KNonNullPropExpression<Int>  
    get() = get("edition")  
  
public val KTable<Book>.price: KNullablePropExpression<BigDecimal>  
    get() = get("price")  
  
public val KNonNullTable<Book>.price: KNonNullPropExpression<BigDecimal>  
    get() = get("price")  
  
public val KNullableTable<Book>.store: KNullableTable<BookStore>  
    get() = join("store")  
  
public val KNonNullTable<Book>.store: KNonNullTable<BookStore>  
    get() = join("store")  
  
public val KTable<Book>.`store?`: KNullableTable<BookStore>  
    get() = outerJoin("store")  
  
/*   
 * Extension for TableEx<Book>  
 */  
  
public val KNullableTableEx<Book>.authors: KNullableTableEx<Author>  
    get() = join("authors")  
  
public val KNonNullTableEx<Book>.authors: KNonNullTableEx<Author>  
    get() = join("authors")  
  
public val KTableEx<Book>.`authors?`: KNullableTableEx<Author>  
    get() = outerJoin("authors")
```

观察这两个自动生成的类型，不难看出

* `BookTableEx`继承了`BookTable`。
* `BookTable`不支持集合关联，但支持普通字段和引用关联(本例中为`store`)。
* `BookTableEx`增加了对集合关联的支持（本例中为`authors`）。

因此jimmer-sql的API遵循如下的模式

* 顶级查询只能基于`Table`创建。
* 子查询、update语句和delete语句既可基于`Table`创建，也可基于`TableEx`创建。

接下来，以顶 层查询和子查询为例，做对比性示范。

### 只能基于Table创建的顶层查询[​](#只能基于table创建的顶层查询 "只能基于Table创建的顶层查询的直接链接")

所以，你的代码看起来如此

* Java
* Kotlin

```
BookTable table = Tables.BOOK_TABLE;  
  
List<Book> books = sqlClient  
    .createQuery(table)  
    .where(  
        table  
            .name() // 可以访问普通字段name  
            .eq("Book Name")  
    )  
    .where(  
        table  
            .store() // 也可以对引用关联store进行join  
            .name()  
            .eq("Store Name")  
    )  
    /*   
     * 然而，无法使用"table.authors()"，因为authors()  
     * 方法被定义在了BookTableEx中，而非BookTable中。  
     *   
     * 即，编译时禁止了用户在顶级查询中对集合关联进行join  
     */  
    .select(table)  
    .execute();
```

```
val books = sqlClient  
    .createQuery(Book::class) {  
        where(  
            table  
                .name // 可以访问普通字段name  
                eq "Book Name"  
        )  
        where(  
            table  
                .store // 也可以对引用关联store进行join  
                .name  
                eq "Store Name"  
        )  
        /*   
         * 然而，无法使用"table.authors"，因为扩展属性authors  
         * 被定义为TableEx<Book>定义，而非Table<Book>。  
         *   
         * 即，编译时禁止了用户在顶级查询中对集合关联进行join  
         */  
        select(table)  
    }  
    .execute()
```

信息

* Java: `createQuery`的参数由用户指定，只能指定为`Table`类型，如果指定为`TableEx`类型，报错
* Kotlin: `createQuery`自动创建表对象，lambda中的自动变量`table`的类型被自动识别为`Table`而非`TableEx`

最终生成的SQL如下

```
select   
    tb_1_.ID,   
    tb_1_.NAME,   
    tb_1_.EDITION,   
    tb_1_.PRICE,   
    tb_1_.STORE_ID   
from BOOK as tb_1_   
inner join BOOK_STORE as tb_2_ on tb_1_.STORE_ID = tb_2_.ID   
where   
    tb_1_.NAME = ?   
and   
    tb_2_.NAME = ?
```

### 允许基于TableEx创建子查询[​](#允许基于tableex创建子查询 "允许基于TableEx创建子查询的直接链接")

和顶级查询不同，子查询、update语句和delete语句允许使用TableEx。

下面例子中的`Author.books`和上面讨论的`Book.authors`一样，也是一个多对多关联。

* Java
* Kotlin

```
BookTable table = Tables.BOOK_TABLE;  
  
// `author`为子查询所用，采用TableEx  
AuthorTableEx author = TableExes.AUTHOR_TABLE_EX;  
  
List<Book> books = sqlClient  
    .createQuery(table)  
    .where(sqlClient  
        .createSubQuery(author)  
        .where(  
            author  
                // `author`是TableEx，  
                // 所以集合关联`books`是允许的  
                .books()  
                .eq(table),  
  
            author.firstName().eq("Alex")  
        )  
        .exists()  
    )  
    .select(table)  
    .execute();
```

```
val books = sqlClient  
    .createQuery(Book::class) {  
  
        // 在父查询中`table`表示`Table<Book>`  
          
        where(  
            exists(  
                wildSubQuery(Author::class) {  
  
                    // 在子查询中，`table`表示TableEx<Author>  
                      
                    where(  
                        table  
                            // 子查询的`table`是TableEx，  
                            // 所以集合关联`books`是允许的  
                            .books eq  
                            parentTable,  
  
                        table.firstName.eq("Alex")  
                    )  
                }  
            )  
        )  
        select(table)  
    }  
    .execute()
```

信息

* Java: `createSubQuery`的参数由用户指定，不强制但建议指定为`TableEx`类型
* Kotlin: `wildSubQuery` *(或`subQuery`)* 自动创建表对象，lambda中的自动变量`table`的类型被自动识别为`TableEx`而非`Table`

最终生成的SQL如下

```
select   
    tb_1_.ID,   
    tb_1_.NAME,   
    tb_1_.EDITION,   
    tb_1_.PRICE,   
    tb_1_.STORE_ID   
from BOOK as tb_1_   
where exists(  
    select 1   
    from AUTHOR as tb_2_   
    inner join BOOK_AUTHOR_MAPPING as tb_3_   
        on tb_2_.ID = tb_3_.AUTHOR_ID   
    where   
        tb_3_.BOOK_ID = tb_1_.ID   
    and   
        tb_2_.FIRST_NAME = ?  
)
```

## asTableEx[​](#astableex "asTableEx的直接链接")

禁止在顶级查询中使用集合关联，绝大部分情况下都是合理的，但并非所有情况都合理。

比如，用户并不查询整个对象，而且查询个别字段，并使用SQL关键字`distinct`来抵消对集合关联join所带来的副作用。这种场景是完全合理的。

所以，禁止在顶级查询中使用集合关联是一个软性限制，而非刚性限制。可以轻松突破。

* Java
* Kotlin

```
BookTable book = Tables.BOOK_TABLE;  
  
List<Long> bookIds = sqlClient  
    .createQuery(book)  
    .where(  
        book  
            .asTableEx() ❶  
            .authors()  
            .firstName()  
            .ilike("A%")  
    )  
    .select(book.id())  
    .distinct() ❷  
    .execute();
```

```
val bookIds = sqlClient  
    .createQuery(Book::class) {  
        where(  
            table  
                .asTableEx() ❶  
                .authors  
                .firstName ilike "A%"  
        )  
        select(table.id)  
    }  
    .distinct() ❷  
    .execute()
```

信息

* ❶ 开发人员向Jimmer表示TA清楚自己在干什么，请求Jimmer允许TA基于集合关联进行表连接
* ❷ 开发人员对自己的行为负责，如果此处有额外的操作，则应该是distinct而不应该是分页相关的操作

最终生成的SQL如下

```
select   
    distinct tb_1_.ID   
from BOOK as tb_1_   
inner join BOOK_AUTHOR_MAPPING as tb_2_   
    on tb_1_.ID = tb_2_.BOOK_ID   
inner join AUTHOR as tb_3_   
    on tb_2_.AUTHOR_ID = tb_3_.ID   
where lower(tb_3_.FIRST_NAME) like ?
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/dynamic-join/table-ex.mdx)

最后于 **2025年9月16日** 更新