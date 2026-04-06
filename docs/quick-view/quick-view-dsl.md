# 3. 任意动态查询

> 来源: https://jimmer.deno.dev/zh/docs/quick-view/dsl/

* [快速预览 ★](/zh/docs/quick-view/)
* 3. 任意动态查询

# 3. 任意动态查询

Jimmer支持强类型SQL DSL。

提示

注意，和大部分人采用强类型DSL的框架不同，Jimmer的强类型DSL不仅仅是为了让SQL语句能得到编译时安全保证和IDE智能提示的体验，还为了就解决原生SQL中表连接和子查询过于麻烦的问题，并给予它们更高级的抽象。

为表连接和子查询赋予了更高级的抽象后，利用Jimmer构建任意复杂的动态查询会变得非常容易。从一开始，Jimmer就是为轻松构造任意复杂的动态查询而设计。

另外，对于特定数据库产品拥有的非SQL标准的强大功能，Jimmer的SQL DSL能嵌入Native SQL片段，强类型DSL的抽象不会影响开发人员尽情使用数据的特有功能。*(这部分功能不会在快速预览中介绍，有兴趣的督责请查看[查询篇/Native表达式](/zh/docs/query/native-sql))*

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/dsl/index.md)

最后于 **2025年9月16日** 更新