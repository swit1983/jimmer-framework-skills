# 4.3 关联id

> 来源: https://jimmer.deno.dev/zh/docs/showcase/where/associated-id

* [案例展示 ★](/zh/docs/showcase/)
* [4. 条件](/zh/docs/showcase/where/)
* 4.3 关联id

本页总览

# 4.3 关联id

## IsNull判断[​](#isnull判断 "IsNull判断的直接链接")

查询所有`parentId`为null的`TreeNode`，即根节点。

* Java
* Kotlin

```
TreeNodeTable table = TreeNodeTable.$;  
List<TreeNode> rootNodes = sqlClient  
    .createQuery(table)  
    .where(table.parentId().isNull())  
    .select(table)  
    .execute();
```

```
val rootNodes = sqlClient  
    .createQuery(TreeNode::class) {  
        where(table.parentId.isNull())  
        select(table)  
    }  
    .execute()
```

提示

上述代码中的`parentId`，是Jimmer在编译时根据多对一属性`TreeNode.parent`自动生成的，
即使开发人员没有配套定义名称为`parentId`的[@IdView属性](/zh/docs/mapping/advanced/view/id-view)也仍然如此。

## 任意值的判断[​](#任意值的判断 "任意值的判断的直接链接")

事实上，`eq`支持null参数，当`eq`的参数为null时，则渲染`is null`

警告

注意：Java的`eqIf`和Kotlin的`eq?`不同，null被认为是动态查询，不会渲染任何SQL条件。

* Java
* Kotlin

```
@Nullable Long parentId = ...略...;  
  
TreeNodeTable table = TreeNodeTable.$;  
List<TreeNode> rootNodes = sqlClient  
    .createQuery(table)  
    .where(table.parentId().eq(parentId))  
    .select(table)  
    .execute();
```

```
val parentId: Long? = ...略...;  
  
val rootNodes = sqlClient  
    .createQuery(TreeNode::class) {  
        where(table.parentId eq parentId)  
        select(table)  
    }  
    .execute()
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/where/associated-id.mdx)

最后于 **2025年9月16日** 更新