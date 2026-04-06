# 1. 查询任意形状

> 来源: https://jimmer.deno.dev/zh/docs/quick-view/fetch/

- [快速预览 ★](/zh/docs/quick-view/)
- 1. 查询任意形状

本页总览

# 1. 查询任意形状

## 基本概念[​](#基本概念 "基本概念的直接链接")

虽然Jimmer实体是强类型的，但也是动态的，可以表达千变万化的数据结构的形状。

因此，Jimmer支持对象抓取器，可以控制被查询的数据结构的形状，然后统一返回Jimmer实体。

提示

这是一个和GraphQL极其类似的概念，但和GraphQL存在巨大的差异

- GraphQL是一个基于HTTP的应用层协议，只能通过构建HTTP服务来暴露这个功能，只有HTTP客户端可以享受这种能力。
- Jimmer对任意形状的查询，是一种ORM的固有行为，可以在任何地方编程使用。

  - 你既可以用它暴露[GraphQL服务](/zh/docs/graphql)。
  - 也可以如本章节即将的展示的内容一样暴露REST服务。
  - 甚至还可以不跨越任何远程边界在应用内部使用。

## 效果图[​](#效果图 "效果图的直接链接")

- Java
- Kotlin

信息

和GraphQL比较

- GraphQL基于HTTP服务，该功能只有在跨越HTTP服务的边界才能呈现；而在Jimmer中，这是ORM的基础API，你可以在任何代码逻辑中使用此能力。
- 截止到目前为止，GraphQL协议不支持对深度无限的自关联属性的递归查询；而Jimmer支持。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/fetch/index.mdx)

最后于 **2025年9月16日** 更新