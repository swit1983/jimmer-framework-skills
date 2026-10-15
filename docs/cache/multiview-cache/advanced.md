---
title: 更高级的用法
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/cache/multiview-cache/advanced
---

# 更高级的用法

## 诉求

在[上一篇](./user-filter.md)文档中，我们覆盖了`isAffectedBy`方法

**Java**

```java
@Component
public class TenantFilter implements CacheableFilter<TenantAwareProps> {

    @Override

    public boolean isAffectedBy(EntityEvent<?> e) {
        return e.isChanged(TenantAwareProps.TENANT)
    }

    ...省略其他代码...
}
```

**Kotlin**

```kotlin
@Component
class TenantFilter(
    ...略...
) : KCacheableFilter<TenantAware> {

    override fun isAffectedBy(e: EntityEvent<*>): Boolean =
        e.isChanged(TenantAware::tenant)

    ...省略其他代码...
}
```

该方法告诉Jimmer，对于任何继承`TenantAware`的实体而言，当其`tenant`属性被修改时，所有以该实体或其集合为目标类型的关联属性 *(例如: `BookStore.books`)* 的缓存都需要被被自动清理
*(在后续的连锁行为中，有可能导致更多的计算属性的缓存也被自动清理)*。

然而，这样的代码有个限制：只有被过滤实体自身的属性被修改时，才能保证相关联缓存的一致性。

是否可以突破这个限制，让过滤器利用和被过滤实体无关的其他实体或关联被来实施过滤，并继续保持缓存友好呢？

## 模型

TODO

## 过滤

TODO

## 效果演示

TODO

## 附带DEMO

TODO
