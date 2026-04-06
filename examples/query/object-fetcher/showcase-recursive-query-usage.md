# 3.1 基本用法

> 来源: https://jimmer.deno.dev/zh/docs/showcase/recursive-query/usage

* [案例展示 ★](/zh/docs/showcase/)
* [3. 递归查询](/zh/docs/showcase/recursive-query/)
* 3.1 基本用法

本页总览

# 3.1 基本用法

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
                .recursiveChildNodes()  
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
                `childNodes*`()  
            }  
        )  
    }  
    .execute()
```

得到如下的数据

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
                    {  
                        "id":3,  
                        "name":"Drinks",  
                        "childNodes":[  
                            {  
                                "id":4,  
                                "name":"Coca Cola",  
                                "childNodes":[]  
                            },  
                            {  
                                "id":5,  
                                "name":"Fanta",  
                                "childNodes":[]  
                            }  
                        ]  
                    },  
                    {  
                        "id":6,  
                        "name":"Bread",  
                        "childNodes":[  
                            {  
                                "id":7,  
                                "name":"Baguette",  
                                "childNodes":[]  
                            },  
                            {  
                                "id":8,  
                                "name":"Ciabatta",  
                                "childNodes":[]  
                            }  
                        ]  
                    }  
                ]  
            },  
            {  
                "id":9,  
                "name":"Clothing",  
                "childNodes":[  
                    {  
                        "id":10,  
                        "name":"Woman",  
                        "childNodes":[  
                            {  
                                "id":11,  
                                "name":"Casual wear",  
                                "childNodes":[  
                                    {  
                                        "id":12,  
                                        "name":"Dress",  
                                        "childNodes":[]  
                                    },  
                                    {  
                                        "id":13,  
                                        "name":"Miniskirt",  
                                        "childNodes":[]  
                                    },  
                                    {  
                                        "id":14,  
                                        "name":"Jeans",  
                                        "childNodes":[]  
                                    }  
                                ]  
                            },  
                            {  
                                "id":15,  
                                "name":"Formal wear",  
                                "childNodes":[  
                                    {  
                                        "id":16,  
                                        "name":"Suit",  
                                        "childNodes":[]  
                                    },  
                                    {  
                                        "id":17,  
                                        "name":"Shirt",  
                                        "childNodes":[]  
                                    }  
                                ]  
                            }  
                        ]  
                    },  
                    {  
                        "id":18,  
                        "name":"Man",  
                        "childNodes":[  
                            {  
                                "id":19,  
                                "name":"Casual wear",  
                                "childNodes":[  
                                    {  
                                        "id":20,  
                                        "name":"Jacket",  
                                        "childNodes":[]  
                                    },  
                                    {  
                                        "id":21,  
                                        "name":"Jeans",  
                                        "childNodes":[]  
                                    }  
                                ]  
                            },  
                            {  
                                "id":22,  
                                "name":"Formal wear",  
                                "childNodes":[  
                                    {  
                                        "id":23,  
                                        "name":"Suit",  
                                        "childNodes":[]  
                                    },  
                                    {  
                                        "id":24,  
                                        "name":"Shirt",  
                                        "childNodes":[]  
                                    }  
                                ]  
                            }  
                        ]  
                    }  
                ]  
            }  
        ]  
    }  
]
```

## 查询静态DTO[​](#查询静态dto "查询静态DTO的直接链接")

在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下

```
export com.yourcompany.yourproject.model.TreeNode  
    -> package com.yourcompany.yourproject.model.dto  
  
RecursiveTreeNodeView {  
    #allScalars(this)  
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
                id=2,   
                name=Food,   
                childNodes=[  
                    RecursiveTreeNodeView(  
                        id=3,   
                        name=Drinks,   
                        childNodes=[  
                            RecursiveTreeNodeView(  
                                id=4,   
                                name=Coca Cola,   
                                childNodes=[]  
                            ),   
                            RecursiveTreeNodeView(  
                                id=5,   
                                name=Fanta,   
                                childNodes=[]  
                            )  
                        ]  
                    ),   
                    RecursiveTreeNodeView(  
                        id=6,   
                        name=Bread,   
                        childNodes=[  
                            RecursiveTreeNodeView(  
                                id=7,   
                                name=Baguette,   
                                childNodes=[]  
                            ),   
                            RecursiveTreeNodeView(  
                                id=8,   
                                name=Ciabatta,   
                                childNodes=[]  
                            )  
                        ]  
                    )  
                ]  
            ),   
            RecursiveTreeNodeView(  
                id=9,   
                name=Clothing,   
                childNodes=[  
                    RecursiveTreeNodeView(  
                        id=10,   
                        name=Woman,   
                        childNodes=[  
                            RecursiveTreeNodeView(  
                                id=11,   
                                name=Casual wear,   
                                childNodes=[  
                                    RecursiveTreeNodeView(  
                                        id=12,   
                                        name=Dress,   
                                        childNodes=[]  
                                    ),   
                                    RecursiveTreeNodeView(  
                                        id=13,   
                                        name=Miniskirt,   
                                        childNodes=[]  
                                    ),   
                                    RecursiveTreeNodeView(  
                                        id=14,   
                                        name=Jeans,   
                                        childNodes=[]  
                                    )  
                                ]  
                            ),   
                            RecursiveTreeNodeView(  
                                id=15,   
                                name=Formal wear,   
                                childNodes=[  
                                    RecursiveTreeNodeView(  
                                        id=16,   
                                        name=Suit,   
                                        childNodes=[]  
                                    ),   
                                    RecursiveTreeNodeView(  
                                        id=17,   
                                        name=Shirt,   
                                        childNodes=[]  
                                    )  
                                ]  
                            )  
                        ]  
                    ),   
                    RecursiveTreeNodeView(  
                        id=18,   
                        name=Man,   
                        childNodes=[  
                            RecursiveTreeNodeView(  
                                id=19,   
                                name=Casual wear,   
                                childNodes=[  
                                    RecursiveTreeNodeView(  
                                        id=20,   
                                        name=Jacket,   
                                        childNodes=[]  
                                    ),   
                                    RecursiveTreeNodeView(  
                                        id=21,   
                                        name=Jeans,   
                                        childNodes=[]  
                                    )  
                                ]  
                            ),   
                            RecursiveTreeNodeView(  
                                id=22,   
                                name=Formal wear,   
                                childNodes=[  
                                    RecursiveTreeNodeView(  
                                        id=23,   
                                        name=Suit,   
                                        childNodes=[]  
                                    ),   
                                    RecursiveTreeNodeView(  
                                        id=24,   
                                        name=Shirt,   
                                        childNodes=[]  
                                    )  
                                ]  
                            )  
                        ]  
                    )  
                ]  
            )  
        ]  
    )  
]
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/recursive-query/usage.mdx)

最后于 **2025年9月16日** 更新