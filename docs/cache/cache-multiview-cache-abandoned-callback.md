# 缓存未生效的原因

> 来源: https://jimmer.deno.dev/zh/docs/cache/multiview-cache/abandoned-callback

* [缓存篇](/zh/docs/cache/)
* [多视角缓存](/zh/docs/cache/multiview-cache/)
* 缓存未生效的原因

本页总览

# 缓存未生效的原因

## CacheAbandonedCallback[​](#cacheabandonedcallback "CacheAbandonedCallback的直接链接")

按照前面文档的阐述，使用多视图缓存相对严苛，必须同时满足以下3个条件

1. 必须明确为需要属性置顶多视图缓存。

   缓存是多层的，需要为用于构建缓存实例的`ChainCacheBuilder<K, V>`指定多个`Binder`对象，即`LoadingBinder/KLoadingBinder`或`SimpleBinder/KSimpleBinder`。

   对于多视图缓存而言，所有`Binder`对象都必须实现`LoadingBinder.Parameterized<K, V>/KLoadingBinder.Parameterized<K, V>`或`SimpleBinder.Parameterized<K, V>/KSimpleBinder.Parameterized<K, V>`接口。否则，最终`ChainCacheBuilder<K, V>`创建的缓存是单视图缓存。
2. 关联属性的过滤规则和计算属性的计算规则必须支持缓存友好。

   * 对于关联属性而言，影响其关联对象的 **所有** 全局过滤器必须是缓存友好的过滤器。任何一个过滤器不对缓存友好都可能导致该属性无法使用缓存。

     + 对于用户自定义的缓存过滤器而言，必须实现`CacheableFilter<E>/KCacheableFilter<E>`接口。
     + 对于软删除注解`@LogicalDeleted`而言，必须将其属性`useMultiViewCache`配置为`true`*(这会导致其内置全局过滤器实现`CacheableFilter<E>/KCacheableFilter<E>`接口)*。
   * 对于计算属性而言，其`TransientResolver<ID, V>/KTransientResolver<ID, V>`的方法`getParameterMapRef`不得返回null。
3. 对象抓取器不使用[属性级过滤器](/zh/docs/query/object-fetcher/association#%E5%B1%9E%E6%80%A7%E8%BF%87%E6%BB%A4%E5%99%A8)。

如果这些前提条件并未完全满足，即便为关联属性或计算属性指定了缓存，Jimmer也会弃之不用。虽然前面的文档详细解释过原因，但实际开发中遇到这类问题时排查不方便。

为了让开发人员快速得知属性级缓存未生效的事实及其原因，Jimmer定义了一个回调接口，如下

```
package org.babyfish.jimmer.sql.cache;  
  
import org.babyfish.jimmer.meta.ImmutableProp;  
  
public interface CacheAbandonedCallback {  
  
    void abandoned(ImmutableProp prop, Reason reason);  
  
    enum Reason {  
  
        CACHEABLE_FILTER_REQUIRED,  
  
        PARAMETERIZED_CACHE_REQUIRED,  
  
        FIELD_FILTER_USED  
    }  
}
```

该回调接口告诉开发人员，虽然某个属性的缓存被指定了但并未被Jimmer所采纳，以及原因。其`abandoned`方法具备两个参数

* `prop`： 哪个属性并为采纳用户配置的缓存。
* `reason`：该属性的缓存未被采纳的原因，有以下三种可能：

  + CACHEABLE\_FILTER\_REQUIRED: 关联对象受到某些全局过滤器的影响，但并非所有过滤器都实现了`CacheableFilter<E>/KCacheableFilter<E>`接口。
  + PARAMETERIZED\_CACHE\_REQUIRED: 影响关联属性的某些`CacheableFilter<E>/KCacheableFilter<E>`对象的`getParameters`方法或实现计算属性的`TransientResolver<ID, V>/KTransientResolver<ID, V>`的`getParameterMapRef`方法返回了长度非0的Map，
    但是，开发人员并未为该属性配置的缓存并不是多视图缓存。
  + FIELD\_FILTER\_USED: 开发人员对属性使用了[对象抓取器](/zh/docs/query/object-fetcher)中的属性级过滤器。

开发人员可以自己实现`CacheAbandonedCallback`接口，创建对象并将其注册到SqlClient中。被注册的回调对象数量无限制。

## 注册Callback[​](#注册callback "注册Callback的直接链接")

有两种方法可以为Jimmer注册`CacheAbandonedCallback`

* 使用Spring Boot Starter

  只需用`@Component`修饰`CacheAbandonedCallback`的实现类即可

  + Java
  + Kotlin

  ```
  @Component  
  public class MyCallback implements CacheAbandonedCallback {  
      ...省略代码...  
  }
  ```

  ```
  @Component  
  class MyCallback : CacheAbandonedCallback {  
      ...省略代码...  
  }
  ```
* 使用底层API

  + Java
  + Kotlin

  ```
  JSqlClient sqlClient =   
      JSqlClient  
          .newBuilder()  
          .addCacheAbandonedCallback(  
              new CacheAbandonedCallback() {  
                  ...省略代码...  
              }  
          )  
          ...省略其他配置...  
          .build();
  ```

  ```
  val sqlClient = newKSqlClient {  
      addCacheAbandonedCallback(  
          object: CacheAbandonedCallback {  
              ...省略代码...  
          }  
      )  
      ...省略其他配置...  
  }
  ```

## Spring Boot Starter默认行为[​](#spring-boot-starter默认行为 "Spring Boot Starter默认行为的直接链接")

如果用户并未注册任何callback实现，Spring Boot Starter会默认注册一个Callback实现，将缓存未被采纳的原因作为警告日志输出，例如：

```
!!!Jimmer warning!!!  
Property-level cache is abandoned.  
    Property: `com.yourcompany.yourproject.model.BookStore.books` +  
    Reason: CACHEABLE_FILTER_REQUIRED
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/cache/multiview-cache/abandoned-callback.mdx)

最后于 **2025年9月16日** 更新