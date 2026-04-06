# 3.4 多属性递归

> 来源: https://jimmer.deno.dev/zh/docs/showcase/recursive-query/multiple-props

* [案例展示 ★](/zh/docs/showcase/)
* [3. 递归查询](/zh/docs/showcase/recursive-query/)
* 3.4 多属性递归

本页总览

# 3.4 多属性递归

## 查询动态实体[​](#查询动态实体 "查询动态实体的直接链接")

* Java
* Kotlin

```
TreeNodeTable table = TreeNodeTable.$;  
  
TreeNode treeNode = sqlClient  
    .findById(  
        TreeNodeFetcher.$  
                .allScalarFields()  
                .recursiveParent() ❶   
                .recursiveChildNodes() ❷,  
        10L  
    );
```

```
val treeNode = sqlClient  
    .findById(  
        newFetcher(TreeNode::class).by {  
            allScalarFields()  
                `parent*`() ❶  
                `childNodes*`() ❷  
        },  
        10L  
    )
```

得到如下的数据

```
{  
    "id":10,  
    "name":"Woman",  
    "parent":{ ❶  
        "id":9,  
        "name":"Clothing",  
        "parent":{ ❶  
            "id":1,  
            "name":"Home",  
            "parent":null ❶  
        }  
    },  
    "childNodes":[ ❷  
        {  
            "id":11,  
            "name":"Casual wear",  
            "childNodes":[ ❷  
                {  
                    "id":12,  
                    "name":"Dress",  
                    "childNodes":[] ❷  
                },  
                {  
                    "id":13,  
                    "name":"Miniskirt",  
                    "childNodes":[] ❷  
                },  
                {  
                    "id":14,  
                    "name":"Jeans",  
                    "childNodes":[] ❷  
                }  
            ]  
        },  
        {  
            "id":15,  
            "name":"Formal wear",  
            "childNodes":[ ❷  
                {  
                    "id":16,  
                    "name":"Suit",  
                    "childNodes":[] ❷  
                },  
                {  
                    "id":17,  
                    "name":"Shirt",  
                    "childNodes":[] ❷  
                }  
            ]  
        }  
    ]  
}
```

## 查询静态DTO[​](#查询静态dto "查询静态DTO的直接链接")

在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下

```
export com.yourcompany.yourproject.model.TreeNode  
    -> package com.yourcompany.yourproject.model.dto  
  
RecursiveTreeNodeView {  
    #allScalars(this)  
    parent* ❶  
    childNodes* ❷  
}
```

编译项目，生成Java/Kotlin类型`RecursiveTreeNodeView`

* Java
* Kotlin

```
TreeNodeTable table = TreeNodeTable.$;  
  
RecursiveTreeNodeView treeNode = sqlClient  
    .findById(  
        RecursiveTreeNodeView.class,  
        10L  
    );
```

```
val treeNode = sqlClient  
    .findById(  
        RecursiveTreeNodeView::class,  
        10L  
    )
```

得到如下数据

```
RecursiveTreeNodeView(  
    id=10,   
    name=Woman,   
    parent=RecursiveTreeNodeView.TargetOf_parent( ❶  
        id=9,   
        name=Clothing,   
        parent=RecursiveTreeNodeView.TargetOf_parent( ❶  
            id=1,   
            name=Home,   
            parent=null ❶  
        )  
    ),   
    childNodes=[ ❷  
        RecursiveTreeNodeView.TargetOf_childNodes(  
            id=11,   
            name=Casual wear,   
            childNodes=[ ❷  
                RecursiveTreeNodeView.TargetOf_childNodes(  
                    id=12,   
                    name=Dress,   
                    childNodes=[] ❷  
                ),   
                RecursiveTreeNodeView.TargetOf_childNodes(  
                    id=13,   
                    name=Miniskirt,   
                    childNodes=[] ❷  
                ),   
                RecursiveTreeNodeView.TargetOf_childNodes(  
                    id=14,   
                    name=Jeans,   
                    childNodes=[] ❷  
                )  
            ]  
        ),   
        RecursiveTreeNodeView.TargetOf_childNodes(  
            id=15,   
            name=Formal wear,   
            childNodes=[ ❷  
                RecursiveTreeNodeView.TargetOf_childNodes(  
                    id=16,   
                    name=Suit,   
                    childNodes=[] ❷  
                ),   
                RecursiveTreeNodeView.TargetOf_childNodes(  
                    id=17,   
                    name=Shirt,   
                    childNodes=[] ❷  
                )  
            ]  
        )  
    ]  
)
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/recursive-query/multiple-props.mdx)

最后于 **2025年9月16日** 更新