# 3.2 控制节点是否递归

> 来源: https://jimmer.deno.dev/zh/docs/showcase/recursive-query/node-control

* [案例展示 ★](/zh/docs/showcase/)
* [3. 递归查询](/zh/docs/showcase/recursive-query/)
* 3.2 控制节点是否递归

本页总览

# 3.2 控制节点是否递归

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
                .recursiveChildNodes(cfg -> {  
                    cfg.recursive(it -> {  
                        return !it.getEntity().name().equals("Clothing");  
                    });  
                })  
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
                    recursive {  
                        entity.name != "Clothing"  
                    }  
                }  
            }  
        )  
    }  
    .execute()
```

如果当前树节点的名称等于`Clothing`则终止递归，否则继续递归。得到如下结果

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
            {"id":9,"name":"Clothing"}  
        ]  
    }  
]
```

信息

`Clothing`对象并非表现为`childNodes`属性为`[]`，而是根本没有`childNodes`属性。

这表示，`Clothing`对象是否有下级对象是未知的，因为递归过程因人为干预而被提前终止。

## 查询静态DTO[​](#查询静态dto "查询静态DTO的直接链接")

创建`ChildNodesPropFilter`类，实现`org.babyfish.jimmer.sql.fetcher.RecursionStrategy<E>`接口

* Java
* Kotlin

ChildNodesRecursionStrategy.java

```
package com.yourcompany.yourpackage.strategy;  
  
...省略import语句...  
  
public class ChildNodesRecursionStrategy implements RecursionStrategy<TreeNode> {  
  
    @Override  
    public boolean isRecursive(Args<TreeNode> args) {  
        return !args.getEntity().name().equals("Clothing");  
    }  
}
```

ChildNodesRecursionStrategy.kt

```
package com.yourcompany.yourpackage.strategy  
  
...省略import语句...  
  
class ChildNodesRecursionStrategy : RecursionStrategy<TreeNode> {  
    override fun isRecursive(args: RecursionStrategy.Args<TreeNode>): Boolean {  
        return args.entity.name != "Clothing"  
    }  
}
```

在`src/main/dto`文件夹下新建任何一个扩展名为dto的文件，编辑代码如下

```
export com.yourcompany.yourproject.model.TreeNode  
    -> package com.yourcompany.yourproject.model.dto  
  
import com.yourcompany.yourpackage.strategy.ChildNodesRecursionStrategy  
  
RecursiveTreeNodeView {  
    #allScalars(this)  
      
    !recursion(ChildNodesRecursionStrategy)  
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
                childNodes=null  
            ),   
            RecursiveTreeNodeView(  
                id=2,   
                name=Food,   
                childNodes=[  
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
                    ),   
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
                    )  
                ]  
            )  
        ]  
    )  
]
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/recursive-query/node-control.mdx)

最后于 **2025年9月16日** 更新