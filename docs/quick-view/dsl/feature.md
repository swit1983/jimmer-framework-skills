---
title: '功能介绍'
---
# 功能介绍


## Jimmer SQL DSL


1. 和大部分ORM中DSL仅仅为了引入强类型体验不同，Jimmer DSL为解决原生SQL在复杂场景下开发效率低下的问题，给予全新的抽象。


> 因此，Jimmer SQL DSL和绝大部分SQL DSL存在本质差异 *(这也是Jimmer项目立项的最初动机)*
2. Jimmer DSL可以嵌入原生SQL片段，和特定数据库产品特有功能的联系没有被切断。


本章章节为了快速预览，所以我们仅仅专注于1。


> 至于2，有兴趣者可参见[Native表达式](query/native-sql)


## 动态谓词


- Java
- Kotlin
BookRepository.java

```
@Repositorypublic class BookRepository {    private final JSqlClient sqlClient;    public BookRepository(JSqlClient sqlClient) {        this.sqlClient = sqlClient;    }    List<Book> findBooks(        @Nullable String name,        @Nullable BigDecimal minPrice,        @Nullable BigDecimal maxPrice,        @Nullable Fetcher<Book> fetcher    ) {        BookTable table = Tables.BOOK_TABLE;        return sqlClient            .createQuery(table)            .where(table.name().ilikeIf(name)) ❶            .where(table.price().betweenIf(minPrice, maxPrice)) ❷            .select(table.fetch(fetcher))            .execute();    }}
```

BookRepository.kt

```
@Repositoryclass BookRepository(    private val sqlClient: KSqlClient) {    fun findBooks(        name: String? = null,        minPrice: BigDecimal? = null,        maxPrice: BigDecimal? = null,        fetcher: Fetcher<Book>? = null    ): List<Book> =        sqlClient            .createQuery(Book::class) {                where(table.name `ilike?` name) ❶                where(table.price.`between?`(minPrice, maxPrice)) ❷                select(table.fetch(table))            }            .execute()}
```


其中，`fetcher`参数的作用已经在[快速浏览/查询任意形状](quick-view/fetch)中做过介绍，本文不再重复，请读者忽略之。


- ❶ 和静态谓词`ilike`不同，`ilikeIf`/`ilike?`是动态谓词，根据参数来决定是否添加SQL条件


如果`name`既非null也非empty string，则添加SQL条件`name ilike :name`
- ❷ 和静态谓词`between`不同，`betweenIf`/`between?`是动态谓词，根据参数来决定是否添加SQL条件。存在如下四种情况


- 如果`minPrice`和`maxPrice`都非null，则添加SQL条件`price between :minPrice and :maxPrice`。
- 如果仅`minPrice`非null，则添加SQL条件`name >= :minPrice`
- 如果仅`maxPrice`非null，则添加SQL条件`name <= :maxPrice`
- 如果`minPrice`和`maxPrice`都为null，则不添加任何添加SQL条件


现在让我们来看看效果


- 当三个参数都为null时


- Java
- Kotlin


```
List<Book> books = bookRepository.findBooks(    null, // nae    null, // minPrice    null, // maxPrice    null);
```


```
val books = bookRepository.findBooks()
```


不会生成任何where条件，SQL如下


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.STORE_IDfrom BOOK tb_1_// No SQL predicates
```
- 当三个参数都非null时


- Java
- Kotlin


```
List<Book> books = bookRepository.findBooks(    "GraphQL", // name    new BigDecimal(20), // minPrice    new BigDecimal(50), // maxPrice    null);
```


```
val books = bookRepository.findBooks(    name = "GraphQL",    minPrice = BigDecimal(20),    maxPrice = BigDecimal(50))
```


会生成所有where条件，SQL如下


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.STORE_IDfrom BOOK tb_1_where        lower(tb_1_.NAME) like ? /* %graphql% */    and        (tb_1_.PRICE between ? /* 20 */ and ? /* 50 */)
```


## 动态表连接


### 定义动态动态表连接


前面的例子中，我们的动态SQL条件都是作用于当前实体 *(Book)* 的。接下来我们通过引用关联 *(一对一或多对一)* 获取关联对象并为之添加过滤条件


多对一关联`Book.store`关联到`BookStore`实体，让我们为`BookStore.name`和`BookStore.website`添加动态SQL条件。


- Java
- Kotlin
BookRepository.java

```
@Repositorypublic class BookRepository {    private final JSqlClient sqlClient;    public BookRepository(JSqlClient sqlClient) {        this.sqlClient = sqlClient;    }    List<Book> findBooks(        @Nullable String name,        @Nullable BigDecimal minPrice,        @Nullable BigDecimal maxPrice,        @Nullable String storeName,        @Nullable String storeWebsite,        @Nullable Fetcher<Book> fetcher    ) {        BookTable table = Tables.BOOK_TABLE;        return sqlClient            .createQuery(table)            .where(table.name().ilikeIf(name))            .where(table.price().betweenIf(minPrice, maxPrice))            .where(table.store().name().ilikeIf(storeName)) ❶            .where(table.store().website().ilikeIf(storeWebsite)) ❷            .select(table.fetch(fetcher))            .execute();    }}
```

BookRepository.kt

```
@Repositoryclass BookRepository(    private val sqlClient: KSqlClient) {    fun findBooks(        name: String? = null,        minPrice: BigDecimal? = null,        maxPrice: BigDecimal? = null,        storeName: String? = null,        storeWebsite: String? = null,        fetcher: Fetcher<Book>? = null    ): List<Book> =        sqlClient            .createQuery(Book::class) {                where(table.name `ilike?` name)                 where(table.price.`between?`(minPrice, maxPrice))                 where(table.store.name `ilike?` storeName) ❶                where(table.store.name `ilike?` storeWebsite) ❷                select(table.fetch(table))            }            .execute()}
```


提示

Java代码中的路径`table.store()`或Kotlin代码中的路径`table.store`叫做动态表连接，表示如下SQL逻辑


```
from BOOK binner join BOOK_STORE s on b.STORE_ID = s.ID
```


你也可以使用外连接，Java代码为`table.store(JoinType.LEFT)`，kotlinJava代码为`table.`storeId?``。


事实上，假如实体模型更加丰富，可以书写更长的路径，比如`table.store().city().province()`。


这里，仅仅做入门示范和快速预览，没必要构建更丰富的实体模型以演示更长的路径，最短表链连接路径`table.store()`足够。


- ❶ 当`storeName`既非null也非empty string时


1. 先通过`Book.store`关联到`BookStore`实体
2. 再为`BookStore.name`添加SQL条件
- ❷ 当`storeWebsite`既非null也非empty string时


1. 先通过`Book.store`关联到`BookStore`实体
2. 再为`BookStore.website`添加SQL条件


### 忽略无用表连接


如果参数`storeName`和`storeWebsite`都为null


- Java
- Kotlin


```
List<Book> books = bookRepository.findBooks(    null,    null,     null,     null, // storeName    null, // storeWebsite    null);
```


```
val books = bookRepository.findBooks()
```


这会导致❶和❷两处的`ilikeIf`/`ilike?`无效，进一步导致`table.store()`/`table.store`被忽略。即，虽然创建了表连接，但未被使用。


提示

如果在DSL中创建了表连接但没有被真正使用，那么表连接将会被自动忽略，最终在生成的SQL中没有对应的SQL连接。


> 注意：Jimmer没有`JPA`中的`join fetch`概念，join的唯一目的是为了被其他SQL表达式引用，所以未被真正使用的join对象会被忽略。


生成的SQL如下


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.STORE_IDfrom BOOK tb_1_// No SQL table joins
```


### 合并冲突表连接


如果参数`storeName`和`storeWebsite`都非null


- Java
- Kotlin


```
List<Book> books = bookRepository.findBooks(    null,    null,     null,     "M", // storeName    ".com", // storeWebsite    null);
```


```
val books = bookRepository.findBooks(    storeName = "M",    storeWebsite = ".com")
```


这会导致❶和❷两处的`ilikeIf`/`ilike?`都生效，进一步导致两处的表连接`table.store()`/`table.store`都生效。即，表连接被创建和使用了多次。


提示

如果在DSL中相同关联的表连接被多次创建，那么所有冲突表连接将会被自动合并，最终在生成的SQL中只有一个JOIN，不会重复JOIN。


生成的SQL如下


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.STORE_IDfrom BOOK tb_1_/* Multiple conflicting table joins are merged into one */inner join BOOK_STORE tb_2_    on tb_1_.STORE_ID = tb_2_.IDwhere        lower(tb_2_.NAME) like ? /* %m% */    and        lower(tb_2_.WEBSITE) like ? /* %.com% */
```


## 隐式子查询


### 定义 隐式子查询


在前面的例子中，动态表连接是基于引用关联 *(一对一或多对一)* 创建的。


对于集合关联 *(一对多或多对多)* 而言，我们可以创建隐含子查询。


> 其实，使用特殊的DSL语法，也可以基于集合关联创建动态表连接，但是，更推荐基于集合关联创建隐式子查询


接下来，我们使用多对多关联`Book.authors`展示相关功能


- Java
- Kotlin
BookRepository.java

```
@Repositorypublic class BookRepository {    private final JSqlClient sqlClient;    public BookRepository(JSqlClient sqlClient) {        this.sqlClient = sqlClient;    }    List<Book> findBooks(        @Nullable String name,        @Nullable BigDecimal minPrice,        @Nullable BigDecimal maxPrice,        @Nullable String storeName,        @Nullable String storeWebsite,        @Nullable String authorName,        @Nullable Gender authorGender,        @Nullable Fetcher<Book> fetcher    ) {        BookTable table = Tables.BOOK_TABLE;        return sqlClient            .createQuery(table)            .where(table.name().ilikeIf(name))            .where(table.price().betweenIf(minPrice, maxPrice))            .where(table.store().name().ilikeIf(storeName))             .where(table.store().website().ilikeIf(storeWebsite))             .where(                 table.authors(author -> ❶                    Predicate.or(                        author.firstName().ilikeIf(authorName),                        author.lastName().ilikeIf(authorName)                    )                )            )            .where(                table.authors(author -> ❷                    author.gender().eqIf(authorGender)                )            )            .select(table.fetch(fetcher))            .execute();    }}
```

BookRepository.kt

```
@Repositoryclass BookRepository(    private val sqlClient: KSqlClient) {    fun findBooks(        name: String? = null,        minPrice: BigDecimal? = null,        maxPrice: BigDecimal? = null,        storeName: String? = null,        storeWebsite: String? = null,        authorName: String? = null,        authorGender: String? = null,        fetcher: Fetcher<Book>? = null    ): List<Book> =        sqlClient            .createQuery(Book::class) {                where(table.name `ilike?` name)                 where(table.price.`between?`(minPrice, maxPrice))                 where(table.store.name `ilike?` storeName)                 where(table.store.name `ilike?` storeWebsite)                 where += table.authors { ❶                    or(                        firstName `ilike?` authorName,                        lastName `ilike?` authorName                    )                }                where += table.authors { ❷                    gender `eq?` authorGender                }                select(table.fetch(table))            }            .execute()}
```


❶和❷处的两个基于lambda表达式的SQL条件，就是隐式子查询。


> 其实，这两个隐式子查询是可以合并为一个，但是为了展示后续功能，故意写了两个隐式子查询。


- ❶ 通过多对多关联`Book.authors`建立关联对象`Author`的子查询，并检查`Author`对象的`firstName`或`lastName`属性是否模糊匹配参数`authorName`
- ❷ 通过多对多关联`Book.authors`建立关联对象`Author`的子查询，并检查`Author`对象的`gender`属性是否等于参数`authorGender`

提示

隐式子查询如果生效，最终都会生成SQL的`exists`语句。在SQL中，`exists`的子查询通常有一个SQL条件用于关联父子查询。


然而，从上面代码中我们看到，子查询中的所有SQL条件都是作用于`Author`对象的，没有用于关联父子查询的SQL条件。


其实，关联父子查询的SQL条件会被隐式子查询隐含了，它总会被自动生成。所以，用户只需要编写关联对象相关的SQL条件。


注意：这里所讨论的是隐式子查询，不是普通子查询 *(Jimmer也支持普通子查询，但不在快速预览的讨论范围内)*。这里所讨论的规则对普通子查询不适用。


### 忽略无用子查询


如果参数`authorName`和`authorGender`都为null


- Java
- Kotlin


```
List<Book> books = bookRepository.findBooks(    null,    null,     null,     null,     null,     null, // authorName    null, // authorGender    null);
```


```
val books = bookRepository.findBooks()
```


- 第一个隐式子查询将会被忽略


- Java
- Kotlin


```
where( ⑤    table.authors(author -> ④        Predicate.or( ③            author.firstName().ilikeIf(authorName), ①            author.lastName().ilikeIf(authorName) ②        )    ))
```


```
where += ⑤    table.authors { ④        or( ③            firstName `ilike?` authorName, ①            lastName `ilike?` authorName ②        )    }
```


当`authorName`为null或empty string时，


- ①和②处的 `ilikeIf`/`ilike?`被忽略，返回null
- 由于①和②的表达都是null，导致③处的`or`为null
- ③处的`or`为null将会导致隐式子查询没有任何SQL条件，这时创建子查询毫无意义，最终④处得到的隐式子查询条件为null
- ④处得到的子查询条件为null，将会导致⑤处的where毫无意义，此操作将会被忽略。


即，第一个隐式子查询被忽略
- 同理，第二个隐式子查询也会被忽略


最终生  成的SQL不包含任何子查询


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.STORE_IDfrom BOOK tb_1_// No SQL sub queries
```


### 合并冲突子查询


如果参数`authorName`和`authorGender`都非null


- Java
- Kotlin


```
List<Book> books = bookRepository.findBooks(    null,    null,     null,     null,     null,     "A", // authorName    Gender.MALE, // authorGender    null);
```


```
val books = bookRepository.findBooks(    authorName = "A",    authorGender = Gender.MALE)
```


此时，两个隐式子查询都会生效。即使我们基于同一个关联 *(Book.authors)* 创建了多个隐式子查询。


提示

如果在DSL中相同关联的隐含子查询被多次创建，那么所有冲突子查询将会被自动合并，最终在生成的SQL中只有一个子查询，不会有多个子查询。


最终生成的SQL为


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.EDITION,    tb_1_.PRICE,    tb_1_.STORE_IDfrom BOOK tb_1_where    /* Multiple conflicting implicit subqueries are merged into one */    exists(        select            1        from AUTHOR tb_2_        inner join BOOK_AUTHOR_MAPPING tb_3_            on tb_2_.ID = tb_3_.AUTHOR_ID        where                /* Parent-child query join condition implied by the implicit subquery */                tb_3_.BOOK_ID = tb_1_.ID            and                (                    lower(tb_2_.FIRST_NAME) like ? /* %a% */                or                    lower(tb_2_.LAST_NAME) like ? /* %a% */                )            and                tb_2_.GENDER = ? /* M */    )
```


> 冲突隐式子查询合并有一个限制，被合并的多个子查询必须位于同一个and、or或not内部。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/dsl/feature.mdx)最后 于 **2025年9月16日**  更新
- [Jimmer SQL DSL](#jimmer-sql-dsl)
- [动态谓词](#动态谓词)
- [动态表连接](#动态表连接)
- [定义动态动态表连接](#定义动态动态表连接)
- [忽略无用表连接](#忽略无用表连接)
- [合并冲突表连接](#合并冲突表连接)
- [隐式子查询](#隐式子查询)
- [定义隐式子查询](#定义隐 式子查询)
- [忽略无用子查询](#忽略无用子查询)
- [合并冲突子查询](#合并冲突子查询)