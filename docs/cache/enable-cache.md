---
title: '启用缓存'
---
# 启用缓存


## CacheFactory接口


要启用缓存，首先需要实现`CacheFactory`/`KCacheFactory`接口，该接口定义如下


- Java
- Kotlin
CacheFactory.java

```
package org.babyfish.jimmer.sql.cache;import org.babyfish.jimmer.meta.ImmutableProp;import org.babyfish.jimmer.meta.ImmutableType;import org.jetbrains.annotations.NotNull;import org.jetbrains.annotations.Nullable;import java.util.List;public interface CacheFactory {    @Nullable    default Cache<?, ?> createObjectCache(@NotNull ImmutableType type) {        return null;    }    @Nullable    default Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) {        return null;    }    @Nullable    default Cache<?, List<?>> createAssociatedIdListCache(@NotNull ImmutableProp prop) {        return null;    }    @Nullable    default Cache<?, ?> createResolverCache(@NotNull ImmutableProp prop) {        return null;    }}
```

KCacheFactory.kt

```
package org.babyfish.jimmer.sql.kt.cacheimport org.babyfish.jimmer.meta.ImmutablePropimport org.babyfish.jimmer.sql.cache.Cacheimport org.babyfish.jimmer.sql.cache.CacheFactoryinterface KCacheFactory : CacheFactory {    override fun createObjectCache(type: ImmutableType): Cache<*, *>? =        null    override fun createAssociatedIdCache(prop: ImmutableProp): Cache<*, *>? =        null    override fun createAssociatedIdListCache(prop: ImmutableProp): Cache<*, List<*>>? =        null    override fun createResolverCache(prop: ImmutableProp): Cache<*, *>? =        null}
```


信息

对于Kotlin而言，为了在覆盖`createAssociatedIdListCache`时让IDE生成更好的代码，请实现`org.babyfish.jimmer.sql.kt.cache.KCacheFactory`接口。


否则，IDE生成的override方法代码时，返回类型是`Cache<*, MutableList<*>>`，而非期望的`Cache<*, List<*>>`


Jimmer调用此接口初始化缓存系统，用户实现此接口回答问题


- createObjectCache：启用对象缓存


参数指定一个实体类型，如果想让它支持对象缓存，就创建缓存并返回；否则，返回null。


所谓

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}对象缓存@media print{.css-1k371a6{position:absolute!important;}}，指把id映射为实体对象


信息

该实体对象是孤单的，没有关联属性。


基于外键的一对一/多对一关联除外，它们可以持有只有id属性的关联对象，因为关联对象的id其实就是当前表的外键字段。
- createAssociatedIdCache和createAssociatedIdListCache：启用关联缓存


`createAssociatedIdCache`和`createAssociatedIdListCache`都用于启用关联缓存。二者唯一的区别是：


- `createAssociatedIdCache`： 一对一或多对一关联
- `createAssociatedIdListCache`：一对多或多对多关联


参数指定一个[关联属性](mapping/base/association)，如果想让它支持关联缓存，就创建缓存并返回；否则返回null。


所谓

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}关联缓存@media print{.css-1k371a6{position:absolute!important;}}，指把id映射为关联id *(或其集合)*
- createResolverCache：启用计算缓存


参数指定一个[复杂计算属性](mapping/advanced/calculated/transient)，如果想让它支持计算属性，就创建缓存并返回；否则，返回null。


所谓

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}计算缓存@media print{.css-1k371a6{position:absolute!important;}}缓存，指把id映射为计算结果


## 多级缓存架构


`CacheFactory`接口的所有方法的返回类型都是`org.babyfish.jimmer.sql.cache.Cache<K, V>`。


用户无需直接实现`Cache<K, V>`，而需要使用`org.babyfish.jimmer.sql.cache.chain.ChainCacheBuilder`来构建多级别缓存。


从理论上讲，`ChainCacheBuilder`支持任意级缓存。然而，大部分项目中，两级缓存已经足够了，例如


- Java
- Kotlin


```
return new CacheFactory() {    @Override    @Nullable    public Cache<?, ?> createObjectCache(@NotNull ImmutableType type) {        return new ChainCacheBuilder<>()            .add( ❶                CaffeineValueBinder                    .forObject(type)                    .maximumSize(1024)                    .duration(caffeineDuration)                    .build()            )            .add( ❷                RedisValueBinder                    .forObject(type)                    .redis(connectionFactory)                    .objectMapper(objectMapper)                    .duration(redisDuration)                    .build()            )            .build();    }};
```


```
return object: KCacheFactory {    override fun createObjectCache(type: ImmutableType): Cache<*, *>? =        ChainCacheBuilder<Any, Any>()            .add( ❶                CaffeineValueBinder                    .forObject(type)                    .maximumSize(1024)                    .duration(Duration.ofHours(1))                    .build()            )            .add( ❷                RedisValueBinder                    .forObject(type)                    .redis(connectionFactory)                    .objectMapper(objectMapper)                    .duration(Duration.ofHours(24))                    .build()            )            .build()}
```


- ❶ 表示一级缓存，基于[Caffeine](https://github.com/ben-manes/caffeine)的进程内JVM缓存
- ❷ 表示二级缓存，基于[Redis](https://redis.io/)的分远程内部缓存。


如果我们所见，`ChainCacheBuilder`采用链式编程风格，多次调用`add`方法就可以构建多级缓存。


`ChainCacheBuilder.add`方法的定义如下


```
public class ChainCacheBuilder<K, V> {        public ChainCacheBuilder<K, V> add(LoadingBinder<K, V> binder) { ❶        ...省略代码...        return this;    }    public ChainCacheBuilder<K, V> add(LoadingBinder.Parameterized<K, V> binder) { ❷        ...省略代码...        return this;    }    public ChainCacheBuilder<K, V> add(SimpleBinder<K, V> binder) { ❸        ...省略代码...        return this;    }    ...省略其他代码...}
```


- ❶ `org.babyfish.jimmer.sql.cache.chain.LoadingBinder`是一个接口，任何首次访问某个键时会自动加载值的缓存技术都可以通过该接口适配。


几乎进程内的JVM缓存，都具备自动加载能力。比如上文代码中所用的[Caffeine](https://github.com/ben-manes/caffeine)或Guava Cache。
- ❷ 处代码仅被[多视角缓存](cache/multiview-cache)使用，读者可以先行忽略
- ❸ `org.babyfish.jimmer.sql.cache.chain.SimpleBinder`是一个接口，任何不具备自动加载值行为的缓存技术都可以通过该接口适配。


几乎所有远程缓存，都不具备自动加载能力。比如上文代码中所用的[Redis](https://redis.io/)。

提示

任何缓存技术都可以被适配成抽象接口`LoadingBinder`或`SimpleBinder`，所以，在Jimmer的多级缓存架构中，任何一级都不会对缓存的技术选型做出任何假设或限制。


如果采用Jimmer的SpringBoot Starter，则可以使用三个缓存技术适配类，如同上文代码中那样


| Jimmer内置的适配类 | 实现接口 | 支持多视角缓存 |
| --- | --- | --- |
| org.babyfish.jimmer.spring.cache.CaffeineBinder | org.babyfish.jimmer.sql.cache.chain.LoadingBinder | 否 |
| org.babyfish.jimmer.spring.cache.RedisValueBinder | org.babyfish.jimmer.sql.cache.chain.SimpleBinder | 否 |
| org.babyfish.jimmer.spring.cache.RedisHashBinder | org.babyfish.jimmer.sql.cache.chain.SimpleBinder.Parameterized | 是 |


备注

[多视角缓存](cache/multiview-cache)会在后续文章中阐述，这里请读者先忽略之。


## 配置CacheFactory


现在，我们已经介绍了`CacheFactory`接口和多级缓存架构，但离启用缓存还差最后一步。


最后一步，为Jimmer把注册`CacheFactory`。


### SpringBoot配置


如果使用SpringBoot Starter，让`CacheFactory`受到Spring的托管即可。


- Java
- Kotlin


```
@Beanpublic CacheFactory cacheFactory() {    return new CacheFactory() {        ...省略代码...    };}
```


```
@Beanfun cacheFactory(): KCacheFactory =    object: KCacheFactory {        ...省略代码...    }
```


### 底层API配置


- Java
- Kotlin


```
JSqlClient sqlClient = JSqlClient    .newBuilder()    .setCacheFactory(        new CacheFactory() {                ...省略代码...        }    )    ...省略其他配置...    .build();}
```


```
val sqlClient = newKSqlClient {    setCacheFactory(        object: KCacheFactory {            ...省略代码...        }    )    ...省略其他配置...}
```


## Redis缓存辅助API


前面我们提到了，如果采用Jimmer的SpringBoot Starter，则可以用现成的`org.babyfish.jimmer.spring.cache.RedisValueBinder`，无需自己去适配Redis。


备注

`RedisHashBinder`和[多视角缓存](cache/multiview-cache)相关，本文不讨论。


要构建`RedisValueBinder`，需要一个[RedisOptions<String, byte[]>](https://docs.spring.io/spring-data/redis/docs/current/api/org/springframework/data/redis/core/RedisOperations.html)。


Jimmer的SpringBoot Stater提供`org.babyfish.jimmer.spring.cache.RedisCaches`类，其静态方法`RedisCaches.cacheRedisTemplate`可快速构建这个[RedisOptions<String, byte[]>](https://docs.spring.io/spring-data/redis/docs/current/api/org/springframework/data/redis/core/RedisOperations.html)对象。


辅助方法`RedisCaches.cacheRedisTemplate`的例子如下：


- Java
- Kotlin


```
@Beanpublic CacheFactory cacheFactory(    RedisConnectionFactory connectionFactory,    ObjectMapper objectMapper) {    return new CacheFactory() {        @Override        @Nullable        public Cache<?, ?> createObjectCache(@NotNull ImmutableType type) {            return new ChainCacheBuilder<Object, Object>()                .add(                    RedisValueBinder                        .forProp(prop)                        .redis(connectionFactory)                        .objectMapper(objectMapper)                        .duration(Duration.ofHours(24))                        .build()                )                .add(                    CaffeineValueBinder                        .forProp()                        .maximumSize(1024)                        .duration(RedisCaches.ofMinutes(24))                        .bind()                )                .build();        }        @Override        @Nullable        public Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) {            return createPropCache(                prop,                 Duration.ofMinutes(10),                Duration.ofHours(10)            );        }        @Override        @Nullable        public Cache<?, List<?>> createAssociatedIdListCache(@NotNull ImmutableProp prop) {            return createPropCache(                prop,                 Duration.ofMinutes(5),                Duration.ofHours(5)            );        }        @Override        @Nullable        public Cache<?, ?> createResolverCache(@NotNull ImmutableProp prop) {            return createPropCache(                prop,                 Duration.ofMinutes(5),                Duration.ofHours(5)            );        }        private Cache<?, ?> createPropCache(            ImmutableProp prop,             Duration redisDuration,            Duration caffeineDuration        ) {            return new ChainCacheBuilder<K, V>()                .add(                    RedisValueBinder                        .forProp(prop)                        .redis(connectionFactory)                        .objectMapper(objectMapper)                        .duration(redisDuration)                        .build()                )                .add(                    CaffeineValueBinder                        .forProp()                        .maximumSize(128)                        .duration(caffeineDuration)                        .bind()                )                .build();        }    };}
```


```
@Beanfun cacheFactory(    connectionFactory: RedisConnectionFactory,    objectMapper: ObjectMapper): KCacheFactory {    return object: KCacheFactory {        override fun createObjectCache(type: ImmutableType): Cache<*, *>? =            ChainCacheBuilder<Any, Any>()                .add(                    CaffeineValueBinder                        .forObject(type)                        .maximumSize(1024)                        .duration(caffeineDuration)                        .build()                )                .add(                    RedisValueBinder                        .forObject(type)                        .redis(connectionFactory)                        .objectMapper(objectMapper)                        .duration(redisDuration)                        .build()                )                .build()        override fun createAssociatedIdCache(prop: ImmutableProp): Cache<*, *>? =            createPropCache(                prop,                 Duration.ofMinutes(10),                Duration.ofHours(10)            )        override fun createAssociatedIdListCache(prop: ImmutableProp): Cache<*, List<*>>? =            createPropCache(                prop,                 Duration.ofMinutes(5),                Duration.ofHours(5)            )        override fun createResolverCache(prop: ImmutableProp): Cache<*, *>? =            createPropCache(                prop,                 Duration.ofMinutes(5),                Duration.ofHours(5)            )        private fun createPropCache(            type: ImmutableType,             duration: caffeineDuration,            duration: redisDuration        ): Cache<*, *> =            ChainCacheBuilder<Any, Any>()                .add(                    CaffeineValueBinder                        .forProp(prop)                        .maximumSize(512)                        .duration(caffeineDuration)                        .build()                )                .add(                    RedisValueBinder                        .forProp(prop)                        .redis(connectionFactory)                        .objectMapper(objectMapper)                        .duration(redisDuration)                        .build()                )                .build()    }}
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/cache/enable-cache.mdx)最后 于 **2025  年9月16日**  更新
- [CacheFactory接口](#cachefactory接口)
- [多级缓存架构](#多级缓存架构)
- [配置CacheFactory](#配置cachefactory)
- [SpringBoot配置](#springboot配置)
- [底层API配置](#底层api配置)
- [Redis缓存辅助API](#redis缓存辅助api)