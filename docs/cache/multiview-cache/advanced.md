---
title: '更高级的用法'
---
# 更高级的用法


## 诉求


在[上一篇](cache/multiview-cache/user-filter)文档中，我们覆盖了`isAffectedBy`方法


- Java
- Kotlin


```
@Componentpublic class TenantFilter implements CacheableFilter<TenantAwareProps> {    @Override     public boolean isAffectedBy(EntityEvent<?> e) {        return e.isChanged(TenantAwareProps.TENANT)    }    ...省略其他代码...}
```


```
@Componentclass TenantFilter(    ...略...) : KCacheableFilter<TenantAware> {    override fun isAffectedBy(e: EntityEvent<*>): Boolean =        e.isChanged(TenantAware::tenant)    ...省略其他代码...}
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

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/cache/multiview-cache/advanced.mdx)最后 于 **2025年9月16日**  更新
- [诉求](#诉求)
- [模型](#模型)
- [过滤](#过滤)
- [效果演示](#效果演示)
- [附带DEMO](#附带demo)