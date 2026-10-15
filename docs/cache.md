---
title: 缓存篇
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/cache
---

# 缓存篇

Jimmer提供了强大的缓存支持能力。

-   是否使用缓存，对开发人员完全透明

-   使用哪些缓存技术，由开发人员决定

-   不仅支持简单的`ObjectCache`（官方页面含交互演示），
    还支持属性级缓存，包括`AssociationCache`（官方页面含交互演示）和`CalculatedCache`（官方页面含交互演示）。
    最终提供任意形状的数据结构的缓存能力，而非简单对象的缓存能力

-   以权限系统为代表的功能会让不同用户看到不同的数据，因此，Jimmer通过`MultiViewCache`（官方页面含交互演示）来让不同用户看到不同的缓存。

-   强大的缓存一致性支持，开发人员专注于修改数据库即可，Jimmer自动`CacheConsistencyPanel`（官方页面含交互演示）
