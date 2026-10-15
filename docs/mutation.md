---
title: 修改篇
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/mutation
---

# 修改篇

在这个章节中，我们会介绍Jimmer中修改数据库相关的所有功能。

其中，有两个功能具备颠覆性。

-   [保持指令](https://babyfish-ct.github.io/jimmer-doc/zh/docs/mutation/save-command)

    一句话保存任意复杂的数据结构，自动找出DIFF并修改数据库，类似于React/Vue。

-   [触发器](mutation/trigger.md)

    无论通过Jimmer自身能力还是借助现有CDC方案，都能感知数据的变化。这也是[缓存](https://babyfish-ct.github.io/jimmer-doc/zh/docs/cache)自动清理的基础。

## 基于查询的修改

[从查询插入和合并数据](mutation/insert-from-select.md)把类型安全的base-query结果映射到目标表，支持显式
冲突键、不同的插入和更新赋值、更新条件以及returning。
