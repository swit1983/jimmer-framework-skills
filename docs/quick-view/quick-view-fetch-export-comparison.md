# 比较

> 来源: https://jimmer.deno.dev/zh/docs/quick-view/fetch/export/comparison

- [快速预览 ★](/zh/docs/quick-view/)
- [1. 查询任意形状](/zh/docs/quick-view/fetch/)
- [暴露功能](/zh/docs/quick-view/fetch/export/)
- 比较

# 比较

二者对比如下

|                                             | 直接返回实体                              | 使用DTO语言            |
|---------------------------------------------|-------------------------------------|--------------------|
| ---                                         | ---                                 | ---                |
| 相同点                                         | 都能为客户端开发人员呈现每个API的返回类型的DTO类型定义      |                    |
| 都能生成OpenApi在线文档和TypeScript RPC代码            |                                     |                    |
| 都能把代码中的文档注释的信息展现给客户端                        |                                     |                    |
| 不同点                                         | 直接返回实体**(易)**                       | 需要使用DTO语言快速生成DTO类型 |
| 需要在RestController中使用`@FetchBy`注解为实体类型补充类型信息 | 直接基于生成的DTO类型开发RestController**(易)** |                    |
| 更偏向对外暴露Api                                  | 对外暴露和内部使用皆可                         |                    |

提示

无论用户如何选择，Jimmer都能以JVM生态中其他技术栈难以想象的低开发成本优雅地解决

DTO爆炸问题

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/fetch/export/comparison.mdx)

最后于 **2025年9月16日** 更新