# 默认脱钩方式

> 来源: https://jimmer.deno.dev/zh/docs/configuration/dissociate-action-checking

- [配置篇](/zh/docs/configuration/)
- 默认脱钩方式

# 默认脱钩方式

在[OnDissociate](/zh/docs/mapping/advanced/on-dissociate)一文中，我们知道有5种脱钩模式。

- NONE *(默认)*
- LAX
- CHECK
- SET\_NULL
- DELETE

对于伪外键 *(请参见[真假外键](/zh/docs/mapping/base/foreignkey))* 关联属性而言，当其脱钩模式为`NONE`时

- 如果全局配置`jimmer.default-dissociation-action-checkable`为true *(默认)*，等价于`CHECK`。
- 如果全局配置`jimmer.default-dissociation-action-checkable`为false，等价于`NONE`。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/configuration/dissociate-action-checking.md)

最后于 **2025年9月16日** 更新