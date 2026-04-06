# 3.2 限制深度

> 来源: https://jimmer.deno.dev/zh/docs/showcase/recursive-query/depth

* [案例展示 ★](/zh/docs/showcase/)
* [3. 递归查询](/zh/docs/showcase/recursive-query/)
* 3.2 限制深度

本页总览

# 3.2 限制深度

## 查询动态实体[​](#查询动态实体 "查询动态实体的直接链接")

* Java
* Kotlin

```
TreeNodeTable table = TreeNodeTable.$;  
  
List<TreeNode> rootNodes = sqlClient  
    .createQuery(table)  
    .where(table.parentId().isNull())  
    .select(  
        table.fetch(  
            TreeNodeFetcher.$  
                .allScalarFields()  
                .recursiveChildNodes(  
                    cfg -> cfg.depth(2)  
                )  
        )  
    )  
    .execute();
```

```
val rootNodes = sqlClient  
    .createQuery(TreeNode::class) {  
        where(table.parentId.isNull())  
        select(  
            table.fetchBy {  
                allScalarFields()  
                `childNodes*` {  
                    depth(2)  
                }  
            }  
        )  
    }  
    .execute()
```

当前被查询的聚合根被规定为第0层，在其基础上向下查询两层子对象，得到如下的数据

```
[  
    {  
        "id":1,  
        "name":"Home",  
        "childNodes":[  
            {  
                "id":2,  
                "name":"Food",  
                "childNodes":[  
                    {"id":3,"name":"Drinks"},  
                    {"id":6,"name":"Bread"}  
                ]  
            },  
            {  
                "id":9,  
                "name":"Clothing",  
                "childNodes":[  
                    {"id":10,"name":"Woman"},  
                    {"id":18,"name":"Man"}  
                ]  
            }  
        ]  
    }  
]
```

信息

最深的4个对象，并非表现为`childNodes`属性为`[]`，而是根本没有`childNodes`属性。

这表示，这4个对象是否有下级对象是未知的，因为递归过程因人为干预而被提前终止。

## 查询静态DTO[​](#查询静态dto "查询静态DTO的直接链接")

在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下

```
export com.yourcompany.yourproject.model.TreeNode  
    -> package com.yourcompany.yourproject.model.dto  
  
RecursiveTreeNodeView {  
    #allScalars(this)  
      
    !depth(2)  
    childNodes*  
}
```

编译项目，生成Java/Kotlin类型`RecursiveTreeNodeView`

* Java
* Kotlin

```
TreeNodeTable table = TreeNodeTable.$;  
  
List<RecursiveTreeNodeView> rootNodes = sqlClient  
    .createQuery(table)  
    .where(table.parentId().isNull())  
    .select(  
        table.fetch(RecursiveTreeNodeView.class)  
    )  
    .execute();
```

```
val rootNodes = sqlClient  
    .createQuery(TreeNode::class) {  
        where(table.parentId.isNull())  
        select(  
            table.fetch(RecursiveTreeNodeView::class)  
        )  
    }  
    .execute()
```

得到如下结果

```
[  
    RecursiveTreeNodeView(  
        id=1,   
        name=Home,   
        childNodes=[  
            RecursiveTreeNodeView(  
                id=9,   
                name=Clothing,   
                childNodes=[  
                    RecursiveTreeNodeView(  
                        id=18,   
                        name=Man,   
                        childNodes=null  
                    ),   
                    RecursiveTreeNodeView(  
                        id=10,   
                        name=Woman,   
                        childNodes=null  
                    )  
                ]  
            ),   
            RecursiveTreeNodeView(  
                id=2,   
                name=Food,   
                childNodes=[  
                    RecursiveTreeNodeView(  
                        id=6,   
                        name=Bread,   
                        childNodes=null  
                    ),   
                    RecursiveTreeNodeView(  
                        id=3,   
                        name=Drinks,   
                        childNodes=null  
                    )  
                ]  
            )  
        ]  
    )  
]
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/recursive-query/depth.mdx)

最后于 **2025年9月16日** 更新