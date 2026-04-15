---
title: '现状'
---


# 现状


Java从14开始支持不可变record类型，kotlin的data class可轻松实现不可变类，甚至支持copy函数。然而，它们都是为简单的不可变对象而设计，处理复杂的的深层次数据结构会面临问题。


深层次的复杂不可变对象难以处理的原因，不是如何从头创建一个全新的数据结构，这对任何编程语言而言都很简单。真正的难点在于，期望对已有的数据结构做一些局部调整，构建新的数据结构。这对当前的Java和Kotlin而言很难处理，请参见


首先定义一个不可变的树节点


- Java
- Kotlin
TreeNode.java

```
record TreeNode(    String name,     List<TreeNode> childNodes) {}
```

TreeNode.java

```
data class TreeNode(    val name: String,     val childNodes: List<TreeNode>)
```


准备一个旧对象


- Java
- Kotlin


```
var oldTreeNode = ...blabla...
```


```
val oldTreeNode = ...blabla...
```


让我们从简单到复杂依次实现三种数据变更操作，创建全新的对象


1. 变更根节点的name属性


- Java
- Kotlin


```
TreeNode newTreeNode = new TreeNode(    "Hello", // 设置根节点的名称    oldTreeNode.childNodes());
```


```
val newTreeNode = oldTreeNode.copy(    name = "Hello" // 设置根节点的名称);
```


这种案例非常简单，没看出Java/Kotlin的问题。别急，轻往后看。
2. 变更第一级子节点的name属性


面包屑条件如下：


- 第一级子节点的位置: pos1


- Java
- Kotlin


```
TreeNode newTreeNode = new TreeNode(    oldTreeNode.name(),    IntStream        .range(0, oldTreeNode.childNodes().size())        .mapToObj(index1 -> {            TreeNode oldChild1 = oldTreeNode.childNodes().get(index1);            if (index1 != pos1) {                return oldChild1;            }            return new TreeNode(                "Hello", // 设置第一级子节点的名称                oldChild1.childNodes()            );        })        .toList());
```


```
val newTreeNode = oldTreeNode.copy(    childNodes = oldTreeNode        .childNodes        .mapIndexed { index1, child1 ->            if (index1 == pos1) {                child1.copy(                    name = "Hello" // 设置第一级子节点的名称                )            } else {                child1            }        })
```
3. 变更第二级子节点的name属性


面包屑条件如下：


- 第一级子节点的位置: pos1
- 第二级子节点的位置: pos2


- Java
- Kotlin


```
TreeNode newTreeNode = new TreeNode(    oldTreeNode.name(),    IntStream        .range(0, oldTreeNode.childNodes().size())        .mapToObj(index1 -> {            TreeNode oldChild1 = oldTreeNode.childNodes().get(index1);            if (index1 != pos1) {                return oldChild1;            }            return new TreeNode(                oldChild1.name(),                IntStream                    .range(0, oldChild1.childNodes().size())                    .mapToObj(index2 -> {                        TreeNode oldChild2 = oldChild1.childNodes().get(index2);                        if (index2 != pos2) {                            return oldChild2;                        } else {                            return new TreeNode(                                "Hello", // 设置第二级子节点的名称                                oldChild2.childNodes()                            );                        }                    })                    .toList()            );        })        .toList());
```


```
val newTreeNode = oldTreeNode.copy(    childNodes = oldTreeNode        .childNodes        .mapIndexed { index1, child1 ->            if (index1 == pos1) {                child1.copy(                    childNodes = child1                        .childNodes                        .mapIndexed { index2, child2 ->                             if (index2 == pos2) {                                child2.copy(                                    name = "Hello" // 设置第二级子节点的名称                                )                            } else {                                child2                            }                        }                )            } else {                child1            }        })
```

信息

可见，只要对象树有一点深度，基于另一个不可变对象创建新的不可变对象 *(即，二次"修改")* 将是一场噩梦。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/object/immutable/current-situation.mdx)最后 于 **2025年9月16日**  更新