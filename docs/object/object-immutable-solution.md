# 方案

> 来源: https://jimmer.deno.dev/zh/docs/object/immutable/solution

- [对象篇](/zh/docs/object/)
- [不可变性](/zh/docs/object/immutable/)
- 方案

本页总览

# 方案

为了解决JVM生态目前对复杂不可变对象二次"修改" *(基于另一个不可变对象创建新的不可变对象)* 不便的问题，诞生了一些不可变对象框架

- [Immutables](https://immutables.github.io/immutable.html)
- [FreeBuilder](https://github.com/inferred/FreeBuilder)
- [Arrow-Kt(Kotlin)](https://arrow-kt.io/learn/immutable-data/intro/)
- [Kopyk(Kotlin)](https://kopyk.at/)
- [MuteKt(Kotlin)](https://github.com/PatilShreyas/mutekt)

Jimmer是一个ORM框架，而处理深层次数据结构是ORM的核心问题，所以，Jimmer必须完成类似的工作。

Jimmer需要同时为Java和Kotlin提供优雅的双语支持，上述所有方案都无法达到这个要求，因此Jimmer并未使用它们中的任何一个，而是选择移植JavaScript/TypeScript领域的[immer](https://github.com/immerjs/immer)。

接下来，我们会用三个步骤展示移植自[immer](https://github.com/immerjs/immer)的不可变对象的强大。

1. 定义不可变类型
2. 从头创建一个不可变数据结构
3. 基于已有的数据结构，按照一些修改愿望，创建新的不可变数据结构。

   > 最后这步是[immer](https://github.com/immerjs/immer)核心价值所在，请注意观察。

## 1. 定义不可变类型[​](#1-定义不可变类型 "1. 定义不可变类型的直接链接")

演示此功能无需ORM实体的`@Entity`注解，非ORM的`@Immutable`足够。

- Java
- Kotlin

TreeNode.java

```package yourpackage;
import java.util.List;
import org.babyfish.jimmer.Immutable;

@Immutable
public interface TreeNode {

    String name();

    List<TreeNode> childNodes();
}

```

TreeNode.kt

```package yourpackage
import org.babyfish.jimmer.Immutable

@Immutable
interface TreeNode {

    val name: String

    val childNodes: List<TreeNode>
}

```

## 2. 从头构建全新的数据[​](#2-从头构建全新的数据 "2. 从头构建全新的数据的直接链接")

- Java
- Kotlin

```treenode treenode = immutables.createtreenode(root -> {
    root.setName("Root").addIntoChildNodes(food -> {
        food
            .setName("Food")
            .addIntoChildNodes(drink -> {
                drink
                    .setName("Drink")
                    .addIntoChildNodes(cocacola -> {
                        cocacola.setName("Cocacola");
                    })
                    .addIntoChildNodes(fanta -> {
                        fanta.setName("Fanta");
                    });
                ;
            });
        ;
    });
});

```

```val treenode = treenode {
    name = "Root"
    childNodes().addBy {
        name = "Food"
        childNodes().addBy {
            name = "Drinks"
            childNodes().addBy {
                name = "Cocacola"
            }
            childNodes().addBy {
                name = "Fanta"
            }
        }
    }
}

```

## 3. 基于现有数据创建新数据[​](#3-基于现有数据创建新数据 "3. 基于现有数据创建新数据的直接链接")

- Java
- Kotlin

```treenode newtreenode = immutables.createtreenode(
        treeNode, // existing data
        root -> {
            root
                .childNodes(false).get(0) // Food
                .childNodes(false).get(0) // Drink
                .childNodes(false).get(0) // Cocacola
                .setName("Cocacola plus");
        }
);

// 展示`newTreeNode`体现了开发人员的修改愿望
// 注意，这不会对已有的`treeNode`造成任何影响

System.out.println("treeNode:" + treeNode);
System.out.println("newTreeNode:" + newTreeNode);

```

```/*
 - val newTreeNode = treeNode.copy {
 - ...
 - }
 - 其实是
 - val newTreeNode = TreeNode(treeNode) {
 - ...
 - }
 - 的简写方式
 - /
val newTreeNode = treeNode.copy {
    childNodes()[0] // Food
        .childNodes()[0] // Drinks
        .childNodes()[0] // Cocacola
        .name += " plus"
}

// 展示`newTreeNode`体现了开发人员的修改愿望
// 注意，这不会对已有的`treeNode`造成任何影响

println("treeNode: $treeNode")
println("newTreeNode: $newTreeNode")

```

输出结果（实际打印结果是紧凑的，但为了方便阅读，这里进行了格式化）

```treenode: {
    "name":"Root",
    "childNodes":[
        {
            "name":"Food",
            "childNodes":[
                {
                    "name":"Drink",
                    "childNodes":[
                        {"name":"Coco Cola"},
                        {"name":"Fanta"}
                    ]
                }
            ]
        }
    ]
}
newTreeNode: {
    "name":"Root",
    "childNodes":[
        {
            "name":"Food",
            "childNodes":[
                {
                    "name":"Drink",
                    "childNodes":[
                        {"name":"Coco Cola plus"},
                        {"name":"Fanta"}
                    ]
                }
            ]
        }
    ]
}

```

可见，`treeNode`没有受任何影响，`newTreeNode`体现了用户的修改愿望。

这个移植是对JVM生态的有力补充。

本框架命名为Jimmer，即为致敬[immer](https://github.com/immerjs/immer)

信息

上面的示例代码使用了一个名为`TreeNodeDraft`的类型，该接口是Jimmer根据用户定义的类型`TreeNode`自动生成的接口类型。

读者可先行忽略这个自动生成的接口，后续文档[Draft](/zh/docs/object/draft)会对其做出介绍

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/object/immutable/solution.mdx)

最后于 **2025年9月16日** 更新