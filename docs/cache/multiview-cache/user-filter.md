---
title: '自定义过滤器'
---
# 自定义过滤器


## 缓存友好


### 基本概念


在[全局过滤器](query/global-filter/user-filter)一文中，我们介绍了自定义全局过滤器需要实现`Filter/KFilter`接口。


然而，使用该接口定义的普通过滤器并不是缓存友好的。


以`Book`实体为例，如果为其设置缓存不友好的全局过滤器，将会导致以下所有对过滤器敏感的属性


- 以`Book`作为目标类型的关联属性。比如，`BookStore.books`、`Author.books`
- 依赖于上述关联属性的计算属性。比如，`BookStore.avgPrice`、`BookStore.newestBooks`*(文档没提及`newestBook`，可参见例子)*


都无法支持缓存。


Jimmer采用`CacheableFilter/KCacheableFilter接口`如下接口定义缓存友好的过滤器


- Java
- Kotlin
CacheableFilter.java

```
package org.babyfish.jimmer.sql.filter;import org.babyfish.jimmer.sql.ast.table.Props;import org.babyfish.jimmer.sql.event.EntityEvent;import java.util.SortedMap;public interface CacheableFilter<P extends Props> extends Filter<P> {    SortedMap<String, Object> getParameters();    boolean isAffectedBy(EntityEvent<?> e);}
```

KCacheableFilter.kt

```
package org.babyfish.jimmer.sql.kt.filterimport org.babyfish.jimmer.sql.event.EntityEventimport java.util.*interface KCacheableFilter<E: Any> : KFilter<E> {    fun getParameters(): SortedMap<String, Any>?    fun isAffectedBy(e: EntityEvent<*>): Boolean}
```


该接口从`Filter/KFilter`接口继承，在其基础上，添加了两个新方法：


- `getParameters`: 该过滤器的为多视图缓存贡献的`SubKey`片段。
- `isAffectedBy`: 接受一个被过滤实体被修改的事件，判断当前过滤器所依赖的过滤字段是否被修改。

信息

一个实体类型允许被多个全局过滤器处理，如果出现了多个全局过滤器


- 任何一个全局过滤器对缓存不友好，都会导致对此过滤器敏感的其他属性都无法支持缓存


因此，这些全局过滤器，要么都是缓存不友好的`Filter/KFilter`，要么都是缓存友好的`CacheableFilter/KCacheableFilter`；二者混用没有意义。


如果不小心导致这种无意义的混用，Jimmer会告诉[为什么缓存未生效](cache/multiview-cache/abandoned-callback)。
- 当所有全局过滤器都缓存友好时，所有`CacheableFilter/KCacheableFilter`对象的`getParameters()`方法返回的数据合并在一起，作为多视图缓存的`SubKey`


例如，实体同时被两个全局过滤器处理。一个是[逻辑删除](query/global-filter/logical-deleted)所隐含的过滤器，记为`a`；另外一个是用户自定义过滤器，记为`b`。


假如


- `a`的`getParameters()`返回`{"logicalDeleted":false}`
- `b`的`getParameters()`返回`{"tenant":"a"}`，


那么最终多视图缓存中的`SubKey`为


`{"logicalDeleted":false,"tenant":"a"}`


### 定义缓存友好过滤器


在[查询篇/自定义过滤器]一文中，我们为实体定义了一个超类型`TenantAware`，让我们再次回顾其代码，如下


- Java
- Kotlin
TenantAware.java

```
@MappedSuperclasspublic interface TenantAware {    String tenant();}
```

TenantAware.kt

```
@MappedSuperclassinterface TenantAware {    val tenant: String}
```


任何需要支持多租户的实体类型都可以继承`TenantAware`，例如`Book`


- Java
- Kotlin
Book.java

```
@Entitypublic interface Book extends TenantAware {    ...省略代码...}
```

Book.kt

```
@Entityinterface Book : TenantAware {        ...省略代码...}
```


假设Spring上下文中有一个类型为`TenantProvider`的对象，其Java方法`get()`和kotlin属性`tenant`用于从当前操作者身份信息中提取所属租户。定义如下过滤器


- Java
- Kotlin


```
@Componentpublic class TenantFilter implements CacheableFilter<TenantAwareProps> {    private final TenantProvider tenantProvider;    public TenantFilter(TenantProvider tenantProvider) {        this.tenantProvider = tenantProvider;    }    @Override    public void filter(FilterArgs<TenantAwareProps> args) {        String tenant = tenantProvider.get();        if (tenant != null) {            args.where(args.getTable().tenant().eq(tenant));        }    }    @Override    public SortedMap<String, Object> getParameters() {        String tenant = tenantProvider.get();        if (tenant == null) {            return null;        }        SortedMap<String, Object> map = new TreeMap<>();        map.put("tenant", tenant);        return map;    }    @Override     public boolean isAffectedBy(EntityEvent<?> e) {        return e.isChanged(TenantAwareProps.TENANT)    }}
```


```
@Componentclass TenantFilter(    private val tenantProvider: TenantProvider) : KCacheableFilter<TenantAware> {    override fun filter(args: KFilterArgs<TenantAware>) {        tenantProvider.tenant?.let {            args.apply {                where(table.tenant.eq(it))            }        }    }    override fun getParameters(): SortedMap<String, Any>? =        tenantProvider.tenant?.let {            sortedMapOf("tenant" to it)        }    override fun isAffectedBy(e: EntityEvent<*>): Boolean =        e.isChanged(TenantAware::tenant)}
```


## 启用多视图缓存


### 普通的写法


- Java
- Kotlin


```
@Beanpublic CacheFactory cacheFactory(    RedisConnectionFactory connectionFactory,    ObjectMapper objectMapper) {    return new CacheFactory() {        @Override        public Cache<?, ?> createObjectCache(@NotNull ImmutableType type) {            ...省略代码...        }        @Override        public Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) {            ...省略代码...        }        @Override        public Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) {            return createPropCache(                prop == BookStoreProps.BOOKS.unwrap() ||                     prop == AuthorProps.BOOKS.unwrap()                prop,                 Duration.ofMinutes(5),                Duration.ofHours(5),            );        }        @Override        public Cache<?, ?> createResolverCache(ImmutableProp prop) {            return createPropCache(                prop == BookStoreProps.AVG_PRICE.unwrap() ||                     prop == BookStoreProps.NEWEST_BOOKS.unwrap()                prop,                 Duration.ofHours(1),                Duration.ofHours(24)            );        }        private <K, V> Cache<K, V> createPropCache(            boolean isMultiviewCache,            ImmutableProp prop,             Duration caffeineDuration,            Duration redisDuration        ) {            if (isMultiView) {                return new ChainCacheBuilder<K, V>()                        .add(                            CaffeineHashBinder                                .forProp(prop)                                .maximumSize(512)                                .duration(caffeineDuration)                                .build()                        )                        .add(                            RedisHashBinder                                .forProp(prop)                                .redis(connectionFactory)                                .objectMapper(objectMapper)                                .duration(redisDuration)                                .build()                        )                        .build();            }            return new ChainCacheBuilder<>()                .add(                    CaffeineValueBinder                        .forProp(prop)                        .maximumSize(128)                        .duration(caffeineDuration)                        .build()                )                .add(                    RedisValueBinder                        .forProp(prop)                        .redis(connectionFactory)                        .objectMapper(objectMapper)                        .duration(redisDuration)                        .build()                )                .build();        }    };}
```


```
@Beanfun cacheFactory(    connectionFactory: RedisConnectionFactory,    objectMapper: ObjectMapper): KCacheFactory {    return object: KCacheFactory {        override fun createObjectCache(type: ImmutableType): Cache<*, *>? =            ...省略代码...        override fun createAssociatedIdCache(prop: ImmutableProp): Cache<*, *>? =            ...省略代码...        override fun createAssociatedIdListCache(prop: ImmutableProp): Cache<*, List<*>>? =            createPropCache(                prop === BookStore::books.toImmutableProp() ||                    prop === Author::books.toImmutableProp(),                 prop,                Duration.ofMinutes(1),                Duration.ofHours(1)            )        override fun createResolverCache(prop: ImmutableProp): Cache<*, *> =            createPropCache(                prop === BookStore::avgPrice.toImmutableProp() ||                    prop === BookStore::newestBooks.toImmutableProp(),                 prop,                Duration.ofMinutes(1),                Duration.ofHours(1)            )        private fun <K, V> createPropCache(            isMultiView: Boolean,            prop: ImmutableProp,             duration: Duration        ): Cache<K, V> {            if (isMultiView) {                return ChainCacheBuilder<K, V>()                        .add(                            CaffeineHashBinder                                .forProp(prop)                                .maximumSize(128)                                .duration(caffeineDuration)                                .build()                        )                        .add(                            RedisHashBinder                                .forProp(prop)                                .redis(connectionFactory)                                .objectMapper(objectMapper)                                .duration(redisDuration)                                .build()                        )                        .build();            }            ChainCacheBuilder<Any, Any>()                .add(                    CaffeineValueBinder                        .forProp(prop)                        .maximumSize(512)                        .duration(caffeineDuration)                        .build()                )                .add(                    RedisValueBinder                        .forProp(prop)                        .redis(connectionFactory)                        .objectMapper(objectMapper)                        .duration(redisDuration)                        .build()                )                .build()        }    }}
```


上面的代码中`RedisHashBinder`是非常重要的实现类，利用redis支持多视图缓存，其背后存储结构对应[Redis Hashes](https://redis.io/docs/data-types/hashes/)，即，嵌套的Hash结构。


| 缓存技术风格 | 是否多视图 | 抽象接口 | 内置实现 |
| --- | --- | --- | --- |
| 支持自Loading的缓存(通常是一级缓存技术：比如Guava, Caffeine) | 单视图 | LoadingBinder<K, V> | CaffeineValueBinder<K, V> |
| 多视图 | LoadingBinder.Parameterized<K, V> | 无 |  |
| 不支持自Loading的缓存(通常是二级缓存技术：比如Redis) | 单视图 | SimpleBinder<K, V> | RedisValueBinder<K, V> |
| 多视图 | SimpleBinder.Parameterized<K, V> | CaffeineHashBinder<K, V> |  |
| RedisHashBinder<K, V> |  |  |  |


### 更好的写法


在上面的代码中，`createAssociatedIdListCache`方法内部对参数`prop`进行判断，以决定究竟应该构建多视图缓存还是单视图缓存。然而


提示

就关联属性而言，是否需要构建多视图缓存的唯一判断依据是目标实体是否被施加了过滤器，Jimmer为此提供了更好的支持。


开发人员只需要把超接口`CacheFactory/KCacheFactory`替换为超类`AbstractCacheFactory/AbstractKCacheFactory`，
即可继承一个叫做`getFilterState/filterState`的成员，它可以帮助我们判断是否应该构建多视图缓存。


- Java
- Kotlin


```
@Beanpublic CacheFactory cacheFactory(    RedisConnectionFactory connectionFactory,    ObjectMapper objectMapper) {    return new AbstractCacheFactory() {        @Override        public Cache<?, ?> createObjectCache(@NotNull ImmutableType type) {            ...省略代码...        }        @Override        public Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) {            return createPropCache(                getFilterState().isAffectedBy(prop.getTargetType()),                prop,                 Duration.ofMinutes(1),                Duration.ofHours(1)            );        }        @Override        public Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) {            return createPropCache(                getFilterState().isAffectedBy(prop.getTargetType()),                prop,                 Duration.ofMinutes(1),                Duration.ofHours(1)            );        }        @Override        public Cache<?, ?> createResolverCache(ImmutableProp prop) {            return createPropCache(                prop == BookStoreProps.AVG_PRICE.unwrap() ||                     prop == BookStoreProps.NEWEST_BOOKS.unwrap()                prop,                 Duration.ofMinutes(1),                Duration.ofHours(1)            );        }        private <K, V> Cache<K, V> createPropCache(            boolean isMultiviewCache,            ImmutableProp prop,             Duration caffeineDuration,            Duration redisDuration        ) {            ...省略代码...        }    };}
```


```
@Beanfun cacheFactory(    connectionFactory: RedisConnectionFactory,    objectMapper: ObjectMapper): KCacheFactory {    return object: AbstractKCacheFactory() {        override fun createObjectCache(type: ImmutableType): Cache<*, *>? =            ...省略代码...        override fun createAssociatedIdCache(prop: ImmutableProp): Cache<*, *>? =            createPropCache(                filterState.isAffectedBy(prop.targetType),                prop,                Duration.ofMinutes(1),                Duration.ofHours(1)            )        override fun createAssociatedIdListCache(prop: ImmutableProp): Cache<*, List<*>>? =            createPropCache(                filterState.isAffectedBy(prop.targetType),                prop,                Duration.ofMinutes(1),                Duration.ofHours(1)            )        override fun createResolverCache(prop: ImmutableProp): Cache<*, *> =            createPropCache(                prop === BookStore::avgPrice.toImmutableProp() ||                    prop === BookStore::newestBooks.toImmutableProp(),                prop,                Duration.ofMinutes(1),                Duration.ofHours(1)            )        private fun <K, V> createPropCache(            isMultiView: Boolean,            prop: ImmutableProp,             caffeineDuration: Duration,            redisDuration: Duration        ): Cache<K, V> {            ...省略代码...        }    }}
```


警告

很遗憾，这种方法仅能用于简化关联缓存构建，即，简化`createAssociatedIdCache`和`createAssociatedIdListCache`方法。


对于计算属性而言，由于框架对用户自定义计算属性的内部逻辑一无所知，无法简化，用户需根据自己业务特点自行决定是否需要构建多视图缓存。


## 计算属性的SubKey


我们在`TenantFilter`中定义了`getParameters`方法，所有受影响的关联属性都会自动为其关联缓存指定`SubKey`。


然而，不幸的是，由于计算属性引入了框架无法理解的用户自定义计算规则，开发人员必须手动为计算属性的`Resolver`实现指定`SubKey`


- Java
- Kotlin
BookStoreAvgPriceResolver.java

```
@Componentpublic class BookStoreAvgPriceResolver implements TransientResolver<Long, BigDecimal> {        private final JSqlClient sqlClient;    @Override    public Ref<SortedMap<String, Object>> getParameterMapRef() {        return sqlClient            .getFilters()            .getTargetParameterMapRef(BookStoreProps.BOOKS);    }    ...省略其他代码...}
```

BookStoreAvgPriceResolver.kt

```
@Componentclass BookStoreAvgPriceResolver(    private val sqlClient: KSqlClient) : KTransientResolver<Long, BigDecimal> {    override fun getParameterMapRef(): Ref<SortedMap<String, Any>?>? {        return sqlClient            .filters            .getTargetParameterMapRef(BookStore::books)    }    ...省略其他代码...}
```


很明显，计算属性`BookStore.avgPrice`其实是由关联属性`BookStore.books`决定的，随后者的变化而变化。


因此，在当前调用上下文中，`BookStore.books`为多视图缓存系统指定什么`SubKey`，`BookStore.avgPrice`就指定什么`SubKey`。


备注

`BookStore.avgPrice`也受`Book.price`的影响，随后者的变化而变化。


然而，`Book.price`是对象的非关联属性，而非关联属性，因此一定和多视图缓存系统无关，这里实现`getParameterMapRef`方法时无需考虑。


## 使用


现在，我们已经让关联属性`BookStore.books`和计算属性`BookStore.avgPrice`都支持了多视角缓存，让我们使用对象抓取器来查询它们


- Java
- Kotlin


```
BookStoreTable table = Tables.BOOK_STORE_TABLE;List<BookStore> stores = sqlClient    .createQuery(table)    .select(        table.fetch(            Fetchers.BOOK_STORE_FETCHER                .allScalarFields()                .books( ❶                    Fetchers.BOOK_FETCHER                        .allScalarFields()                )                .avgPrice() ❷        )    )    .execute();System.out.println(stores);
```


```
val stores = sqlClient    .createQuery(BookStore::class) {        select(            table.fetchBy {                allScalarFields()                books { ❶                    allScalarFields()                }                avgPrice() ❷            }        )    }    .execute()println(stores)
```


### 以一种身份执行


假设当前租户名称为`a`，执行过程如下


- 第一步：查询聚合根


首先查询聚合根对象，执行如下SQL


```
select    tb_1_.ID,    tb_1_.NAME,    tb_1_.WEBSITEfrom BOOK_STORE tb_1_
```


这里实现了代码中的查询，得到了一些BookStore对象。这种被用户直接查询而得的对象叫做聚合根对象
- 第二步：通过关联缓存查询❶处的`BookStore.books`


上面的代码会得到一系列聚合根对象，如果数据库采用官方例子的数据，会得到两个聚合根对象，这两条BOOK_STORE的主键`ID`为1和2。


Jimmer先从Redis查找数据，被查找的键为`BookStore.books-1`和`BookStore.books-2`。


假设无法  在Redis中找到这些键所对应的数据


```
127.0.0.1:6379> keys BookStore.books-*(empty array)
```


所以，执行如下SQL，完成关联属性`BookStore.books`


```
SQL: select,    tb_1_.STORE_ID,    tb_1_.IDfrom BOOK tb_1_where        tb_1_.STORE_ID in (            ? /* 1 */, ? /* 2 */        )    and        tb_1_.TENANT = ? /* a */order by    tb_1_.NAME asc,    tb_1_.EDITION desc
```


信息

其中`tb_1_.TENANT = 'a'`这个过滤条件来自用户过滤器`TenantFilter`。


Jimmer会把从查询结构放入Redis，因此，我们可以从redis中查看这些数据


```
127.0.0.1:6379> keys BookStore.books-*1) "BookStore.books-2"2) "BookStore.books-1"127.0.0.1:6379> hgetall BookStore.books-11) "{\"tenant\":\"a\"}"2) "[5,3,1,9,7]"127.0.0.1:6379> hgetall BookStore.books-21) "{\"tenant\":\"a\"}"2) "[11]"
```


信息

多视角缓存采用Redis的Hash结构，所以，这里需要使用redis的`hgetall`指令，普通`get`指令会报错


Redis的Hash是两层KV结构的嵌套


- 外层结构的Redis Key，`BookStore.books-1`和`BookStore.books-2`，和单视图缓存的RedisKey无异。
- 内层结构的Hash Key，在Jimmer中也称SubKey，由全局过滤器的提供。


这里，`{"tenant":"a"}`由`TenantProvider`提供，表示当前缓存项的值并非所有关联对象的id集合，而仅仅包含租户`a`可以看到的关联对象的id集合。

提示

毫无疑问，在Redis中的数据因超时而被清除之前，**用相同的租户身份**再次执行上述Java/Kotlin代码，将直接从Redis中返回关联数据，相关SQL不会被生成。
- 第三步：把id集合转化为关联对象


在上一步中，我们得到了关联属性`BookStore.books`所对应的关联对象id集合，它们表示租户`a`可以看到的关联对象id集合。


现在，就可以利用`Book`类型的对象缓存，把`Book` id的集合转化为`Book`的集合。


这个步骤非常简单，无需讨论。
- 第四步：通过计算缓存查询❷处的`BookStore.avgPrice`


上面的代码会得到一系列聚合根对象，如果数据库采用官方例子的数据，会得到两个聚合根对象，这两条BOOK_STORE的主键`ID`为1和2。


Jimmer先从Redis查找数据，被查找的键为`BookStore.avgPrice-1`和`BookStore.avgPrice-2`。


假 设无法在Redis中找到这些键所对应的数据


```
127.0.0.1:6379> keys BookStore.avgPrice-*(empty array)
```


所以，执行如下SQL，完成计算属性的计算


```
select    tb_1_.ID,    avg(tb_2_.PRICE)from BOOK_STORE tb_1_left join BOOK tb_2_    on tb_1_.ID = tb_2_.STORE_IDwhere        tb_1_.ID in (            ? /* 1 */, ? /* 2 */        )    and        tb_1_.TENANT = ? /* a */group by    tb_1_.ID
```


信息

其中`tb_1_.TENANT = 'a'`这个过滤条件来自用户过滤器`TenantFilter`。


Jimmer会把从查询结构放入Redis，因此，我们可以从redis中查看这些数据


```
127.0.0.1:6379> keys BookStore.avgPrice-*1) "BookStore.avgPrice-2"2) "BookStore.avgPrice-1"127.0.0.1:6379> hgetall BookStore.avgPrice-11) "{\"tenant\":\"a\"}"2) "53.1"127.0.0.1:6379> hgetall BookStore.avgPrice-21) "{\"tenant\":\"a\"}"2) "81"
```


信息

多视角缓存采用Redis的Hash结构，所以  ，需要使用redis的`hgetall`指令，而非`get`


Redis的Hash是两层KV结构的嵌套


- 外层结构的Redis Key，`BookStore.avgPrice-1`和`BookStore.avgPrice-2`，和单视图缓存的RedisKey无异。
- 内层结构的Hash Key，在Jimmer中也称SubKey，由全局过滤器的提供。


这里，`{"tenant": "a"}`由`TenantProvider`提供，表示当前缓存项的值并非所有关联对象的平均价格，而是租户`a`可以看到的关联对象的平均价格。

提示

毫无疑问，在Redis中的数据因超时而被清除之前，**用相同的租户身份**再次执行上述Java/Kotlin代码，将直接从Redis中返回关联数据，相关SQL不会被生成。


最终，Jimmer把4个步骤的结果拼接在一起，作为最终返回给用户的数据


```
[    {        "id":2,        "name":"MANNING",        "website":null,        "books":[            {                "id":11,                "name":"GraphQL in Action",                "edition":2,                "price":81            }        ],        "avgPrice":81    },    {        "id":1,        "name":"O'REILLY",        "website":null,        "books":[            {                "id":5,                "name":"Effective TypeScript",                "edition":2,                "price":69            },            {                "id":3,                ...略...            },            {                "id":1,                ...略...            },            {                "id":9,                ...略...            },            {                "id":7,                ...略...            }        ],        "avgPrice":53.1    }]
```


### 多种身份反复执行


之前讨论过了租户为`a`时的查询执行过程。同理，我们那可以利用不同租户身份执行多次，让如下三种视角都在Redis中留下缓存数据。


- `tenant` = null
- `tenant` = "a"
- `tenant` = "b"

信息

对于官方附带例子而言，`TenantProvider`基于HTTP请求头实现，并附有

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}swagger界面@media print{.css-1k371a6{position:absolute!important;}}的支持，用三种不同的用户身份执行三次很容易。


其中，`tenant = null`对应swagger UI的unauthorized/logout状态。


打开redis-cli，我们可以验证Redis中的数据


```
127.0.0.1:6379> keys BookStore.books-*1) "BookStore.books-2"2) "BookStore.books-1"127.0.0.1:6379> hgetall BookStore.books-11) "{\"tenant\":\"b\"}"2) "[6,4,2,8]"3) "{\"tenant\":\"a\"}"4) "[5,3,1,9,7]"5) "{}"6) "[6,5,4,3,2,1,9,8,7]"127.0.0.1:6379> hgetall BookStore.books-21) "{\"tenant\":\"b\"}"2) "[12,10]"3) "{\"tenant\":\"a\"}"4) "[11]"5) "{}"6) "[12,11,10]"127.0.0.1:6379> keys BookStore.avgPrice-*1) "BookStore.avgPrice-2"2) "BookStore.avgPrice-1"127.0.0.1:6379> hgetall BookStore.avgPrice-11) "{\"tenant\":\"b\"}"2) "65.25"3) "{\"tenant\":\"a\"}"4) "53.1"5) "{}"6) "58.500000"127.0.0.1:6379> hgetall BookStore.avgPrice-21) "{\"tenant\":\"b\"}"2) "80"3) "{\"tenant\":\"a\"}"4) "81"5) "{}"6) "80.333333"
```


提示

读者可以仔细看看这些`redis-cli`命令，很容易发现，Subey`{"tenant":"a"}`的数据和SubKey`{"tenant":"b"}`的数据合并起来刚好就是SubKey`{}`的数据。


不同租户查询到的数据分别为


- tenant=null
- tenant=a
- tenant=b


```
[    {        "id":2,        "name":"MANNING",        "website":null,        "books":[            {                "id":12,                "name":"GraphQL in Action",                "edition":3,                "price":80,            },            {                "id":11,                ...略...            },            {                "id":10,                ...略...            }        ],        "avgPrice":80.333333    },    {        "id":1,        "name":"O'REILLY",        "website":null,        "books":[            {                "id":6,                "name":"Effective TypeScript",                "edition":3,                "price":88            },            {                "id":5,                ...略...            },            {                "id":4,                ...略...            },            {                "id":3,                "name":"Learning GraphQL",                "edition":3,                "price":51            },            {                "id":2,                ...略...            },            {                "id":1,                ...略...            },            {                "id":9,                "name":"Programming TypeScript",                "edition":3,                "price":48            },            {                "id":8,                ...略...            },            {                "id":7,                ...略...            }        ],        "avgPrice":58.5    }]
```


```
[    {        "id":2,        "name":"MANNING",        "website":null,        "books":[            {                "id":11,                "name":"GraphQL in Action",                "edition":2,                "price":81            }        ],        "avgPrice":81    },    {        "id":1,        "name":"O'REILLY",        "website":null,        "books":[            {                "id":5,                "name":"Effective TypeScript",                "edition":2,                "price":69            },            {                "id":3,                ...略...            },            {                "id":1,                ...略...            },            {                "id":9,                ...略...            },            {                "id":7,                ...略...            }        ],        "avgPrice":53.1    }]
```


```
[    {        "id":2,        "name":"MANNING",        "website":null,        "books":[            {                "id":12,                "name":"GraphQL in Action",                "edition":3,                "price":80            },            {                "id":10,                ...略...            }        ],        "avgPrice":80    },    {        "id":1,        "name":"O'REILLY",        "website":null,        "books":[            {                "id":6,                "name":"Effective TypeScript",                "edition":3,                "price":88            },            {                "id":4,                ...略...            },            {                "id":2,                ...略...            },            {                "id":8,                ...略...            }        ],        "avgPrice":65.25    }]
```


## 一致性


现在，让我们来修改id为6的`Book`对象的`tenant`属性, 从"b"改为"a"。


由于`Book-6`隶属于`BookStore-1`，所以可以预见的是，属性`BookStore.books-1`和`BookStore.avgPrice-1`所对应的多视角缓存必然失效。


- 假如启用了BinLog触发器，用任何手段修改数据库都可以导致Jimmer缓存一致性的介入。比如直接在SQL IDE中执行如下SQL


```
update BOOKset TENANT = 'a' where ID = 6;
```
- 假如只启用了Transaction触发器，则必须用Jimmer的API修改数据库


- Java
- Kotlin


```
sqlClient.save(    Immutables.createBook(draft -> {        draft.setId(6L);        draft.setTenant("a");    }));
```


```
sqlClient.save(    Book {        id = 6L        tenant = "a"    })
```


无论通过上述何种方式修改数据，你都会在看到如下日志输出


```
Delete data from redis: [Book-6] ❶Delete data from redis: [Author.books-3] ❷Delete data from redis: [BookStore.books-1] ❸Delete data from redis: [BookStore.avgPrice-1] ❹
```


- ❶ 更新被修改实体的对象缓存
- ❷ 所有目标类型为`Book`的关联属性必然被影响，当然包括`Author.books`


按数据库现有数据，受影响的`Author`对象的id为3
- ❸ 所有目标类型为`Book`的关联属性必然被影响，当然包括`BookStore.books`


按数据库现有数据，受影响的`BookStore`对象的id为1
- ❹ id为1的`BookStore`对象的计算缓存`BookStore.avgPrice`也被影响了，这是最奇妙的特征。


虽然框架并不知道用户在计算属性中使用何种计算方法，但是在[计算缓存](cache/cache-type/calculation)一文中，我们在`BookStoreAvgPriceResolver`类中讨论过如下代码


- Java
- Kotlin


```
@EventListenerpublic void onAssociationChange(AssociationEvent e) {    if (sqlClient.getCaches().isAffectedBy(e) &&         e.isChanged(BookStoreProps.BOOKS)     ) {        ...省略...    }}
```


```
@EventListenerfun onAssociationChange(e: AssociationEvent) {    if (sqlClient.caches.isAffectedBy(e) &&        e.isChanged(BookStore::books)    ) {        ...省略...    }}
```


如果忘记了这段代码的具体逻辑，可以回顾[计算缓存](cache/cache-type/calculation)一文，这里关注高量的那行代码即可。这里，当前计算属性对`BookStore.books`关联的变化感兴趣。


提示

修改表之间关联字段并非触发关联属性变更事件的唯一方法，修改关联对象中能影响全局过滤器的被过滤字段，*比如这里的`TENANT`*，也可以触发关联属性变更事件。


这是Jimmer触发器机制中一个非常重要的特性！


很明显，❸处已经感知到了`BookStore.books`关联属性发生了变更，因此，会进一步导致此处计算属性也会受影响。
[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/cache/multiview-cache/user-filter.mdx)最后 于 **2025年9月16日**  更新
- [缓存友好](#缓存友好)
- [基本概念](#基本概念)
- [定义缓存友好过滤器](#定义缓存友好过滤器)
- [启用多视图缓存](#启用多视图缓存)
- [普通的写法](#普通的写法)
- [更好的写法](#更好的写法)
- [计算属性的SubKey](#计算属性的subkey)
- [使用](#使用)
- [以一种身份执行](#以一种身份执行)
- [多种身份反复执行](#多种身份反复执行)
- [一致性](#一致性)