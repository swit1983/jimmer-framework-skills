# Draft

> 来源: https://jimmer.deno.dev/zh/docs/object/draft

* [对象篇](/zh/docs/object/)
* Draft

本页总览

# Draft

之前的文档我们发现

* 用户定义了`Book`类型，例子代码就可以使用`BookDraft`类型
* 用户定义了`TreeNode`类型，例子代码就可以使用`TreeNodeDraft`类型

这些名称以`Draft`结尾且和用户定义的类型一一对应的类型，叫Draft类型

## 使用预编译器[​](#使用预编译器 "使用预编译器的直接链接")

### 定义TreeNode[​](#定义treenode "定义TreeNode的直接链接")

用户定先义不可变数据接口，这里无需使用ORM实体注解`@Entity`，使用非ORM的注解`@Immutable`即可达到演示目的。

* Java
* Kotlin

TreeNode.java

```
package yourpackage;  
  
import javax.validation.constraints.Null;  
import java.util.List;  
  
import org.babyfish.jimmer.Immutable;  
  
@Immutable  
public interface TreeNode {  
      
    String name();  
  
    @Null  
    TreeNode parent();  
  
    List<TreeNode> childNodes();  
}
```

TreeNode.kt

```
package yourpackage  
  
import org.babyfish.jimmer.Immutable  
  
@Immutable  
interface TreeNode {  
  
    val name: String  
  
    val parent: TreeNode?  
  
    val childNodes: List<TreeNode>  
}
```

### 生成TreeNodeDraft[​](#生成treenodedraft "生成TreeNodeDraft的直接链接")

要自动生成`TreeNodeDraft`需要启用预编译器

* Java: 使用AnnotationProcessor `jimmer-apt`
* Kotlin: 使用KSP `jimmer-ksp`

信息

如何使用`jimmer-apt/jimmer-ksp`、以及遇到可能的问题后该如何处理，
在[生成代码](/zh/docs/quick-view/get-started/generate-code)中有非常详细的介绍，本文不再赘述。

* Java
* Kotlin

TreeNodeDraft.java

```
package org.babyfish.jimmer.example.core.model;  
  
import java.util.List;  
import org.babyfish.jimmer.DraftConsumer;  
import org.babyfish.jimmer.lang.OldChain;  
  
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    TreeNodeDraft.Producer $ = Producer.INSTANCE;  
  
  
    @OldChain  
    TreeNodeDraft setName(String name);  
  
  
    TreeNodeDraft parent();  
  
    TreeNodeDraft parent(boolean autoCreate);  
  
  
    @OldChain  
    TreeNodeDraft setParent(TreeNode parent);  
  
    @OldChain  
    TreeNodeDraft applyParent(DraftConsumer<TreeNodeDraft> block);  
  
    @OldChain  
    TreeNodeDraft applyParent(TreeNode base, DraftConsumer<TreeNodeDraft> block);  
  
  
  
    List<TreeNodeDraft> childNodes(boolean autoCreate);  
  
    @OldChain  
    TreeNodeDraft setChildNodes(List<TreeNode> childNodes);  
  
    @OldChain  
    TreeNodeDraft addIntoChildNodes(DraftConsumer<TreeNodeDraft> block);  
  
    @OldChain  
    TreeNodeDraft addIntoChildNodes(TreeNode base, DraftConsumer<TreeNodeDraft> block);  
  
  
  
    class Producer {  
  
        private Producer() {}  
  
        public TreeNode produce(  
            DraftConsumer<TreeNodeDraft> block  
        ) {  
            return produce(null, block);  
        }  
  
        public TreeNode produce(  
            TreeNode base,   
            DraftConsumer<TreeNodeDraft> block  
        ) {  
            ...omit...  
        }  
  
        ...省略其他代码...  
    }  
  
    ...省略其他代码...  
}
```

TreeNodeDraft.kt

```
@DslScope  
public interface TreeNodeDraft : TreeNode {  
    public override var name: String  
  
    public override var parent: TreeNode?  
  
    public override var childNodes: List<TreeNode>  
  
    public fun parent(): TreeNodeDraft  
  
    public fun childNodes(): MutableList<TreeNodeDraft>  
  
    public object `$` {  
          
        ...省略其他代码...  
  
        public fun produce(  
            base: TreeNode? = null,   
            block: TreeNodeDraft.() -> Unit  
        ): TreeNode {  
            ...omit code...  
        }  
    }  
  
    ...省略其他代码...  
}  
  
public fun ImmutableCreator<TreeNode>.`by`(  
    base: TreeNode? = null,   
    block: TreeNodeDraft.() -> Unit  
): TreeNode =   
    TreeNodeDraft.`$`.produce(base, block)  
  
public fun MutableList<TreeNodeDraft>.addBy(  
    base: TreeNode? = null,  
    block: TreeNodeDraft.() -> Unit  
): MutableList<TreeNodeDraft> {  
    add(TreeNodeDraft.`$`.produce(base, block) as TreeNodeDraft)  
    return this  
}  
  
public fun TreeNode.copy(block: TreeNodeDraft.() -> Unit): TreeNode =  
    TreeNodeDraft.`$`.produce(this, block)
```

你可以这样使用

1. 从头创建全新的对象

   * Java
   * Kotlin

   ```
   TreeNode oldTreeNode = Immutables.createTreeNode(treeNodeDraft -> {  
           ...省略...  
       });
   ```

   ```
   vak oldTreeNode = TreeNode {  
       ...省略...  
   }
   ```
2. 根据已有的对象，进行某些“变更”后，创建新的对象

   * Java
   * Kotlin

   ```
   TreeNode newTreeNode = Immutables.createTreeNode(oldTreeNode, treeNodeDraft -> {  
           ...省略...  
       });
   ```

   ```
   val newTreeNode = TreeNode(oldTreeNode) {  
       ...省略...  
   }
   ```

   或

   ```
   val newTreeNode = oldTreeNode.copy {  
       ...省略...  
   }
   ```

## 标量属性[​](#标量属性 "标量属性的直接链接")

`TreeNode.name`是一个标量属性。`TreeNodeDraft`会定义如下一个`setter方法/可写属性`

* Java
* Kotlin

TreeNodeDraft.java

```
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    @OldChain  
    TreeNodeDraft setName(String name);  
  
    ...省略其他代码...  
}
```

TreeNodeDraft.kt

```
@DslScope  
public interface TreeNodeDraft : TreeNode {  
      
    // var, not val  
    public override var name: String  
  
    ...省略其他代码...  
}
```

开发人员可以通过此方法设置draft代理的`name`属性

* Java
* Kotlin

```
TreeNode treeNode = Immutables.createTreeNode(draft -> {  
        draft.setName("Root Node");  
    });
```

```
val treeNode = TreeNode {  
    name = "Root Node"  
}
```

## 引用关联[​](#引用关联 "引用关联的直接链接")

`TreeNode.parent`是一个关联属性。其类型是对象，而非集合。如果用ORM的话来讲，是一对一或多对一关联。

`TreeNodeDraft`为其定义了多个方法

### 覆盖getter `parent()`[​](#覆盖getter-parent "覆盖getter-parent的直接链接")

* Java
* Kotlin

TreeNodeDraft.java

```
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    TreeNodeDraft parent();  
  
    ...省略其他代码...  
}
```

TreeNodeDraft.kt

```
@DslScope  
public interface TreeNodeDraft : TreeNode {  
      
    public fun parent(): TreeNodeDraft  
  
    ...省略其他代码...  
}
```

信息

注意，此方法的返回类型是`TreeNodeDraft`，而非`TreeNode`。

即，如果draft对象的引用关联被设置过且被设置为非null，那么该方法一定返回draft对象。这样，用户就可以直接修改更深的关联对象。

* Java
* Kotlin

```
TreeNode newTreeNode = Immutables.createTreeNode(treeNode, draft -> {  
        draft.parent().setName("Daddy");  
        draft.parent().parent().setName("Grandpa");  
    });
```

```
@DslScope  
var newTreeNode = TreeNode(treeNode) {  
    parent().name = "Daddy"  
    parent().parent().name = "Grandpa"  
}
```

### 新增getter `parent(boolean)`[​](#新增getter-parentboolean "新增getter-parentboolean的直接链接")

* Java
* Kotlin

TreeNodeDraft.java

```
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    TreeNode parent(boolean autoCreate);  
  
    ...省略其他代码...  
}
```

TreeNodeDraft.kt

```
@DslScope  
public interface TreeNodeDraft : TreeNode {  
  
    // 这个属性的getter等价于Java的`parent(false)`   
    override fun parent: TreeNode  
      
    // 这个函数等价于Java的`parent(true)`   
    public fun parent(): TreeNodeDraft  
  
    ...省略其他代码...  
}
```

Java的`parent(false)`和Kotlin的`parent`，具备以下两个问题：

* 如果draft对象的属性`parent`未被设置，访问它会导致异常
* 如果draft对象的属性`parent`被设置为null，访问它会返回null，null无法支持进一步修改。

`parent(true)`可以解决以上的问题，如果上述任何一种情况满足，就自动创建并设置一个关联对象，然后允许用户修改。这是一个非常实用的功能，尤其是从头创建对象时。

* Java
* Kotlin

```
TreeNode treeNode = Immutables.createTreeNode(/* No `base` here */ draft -> {  
        draft.parent(true).setName("Daddy");  
        draft.parent(true).parent(true).setName("Grandpa");  
    });
```

```
val treeNode = TreeNode /* No `base` here */ {  
    parent().setName("Daddy");  
    parent().parent().setName("Grandpa");  
}
```

### 新增`setParent`[​](#新增setparent "新增setparent的直接链接")

* Java
* Kotlin

TreeNodeDraft.java

```
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    @OldChain  
    TreeNodeDraft setParent(TreeNode parent);  
  
    ...省略其他代码...  
}
```

TreeNodeDraft.kt

```
@DslScope  
public interface TreeNodeDraft : TreeNode, Draft {  
  
    // var, not val  
    public var parent: TreeNode  
  
    ...省略其他代码...  
}
```

该setter允许用户替换整个关联对象。

* Java
* Kotlin

```
TreeNode treeNode = Immutables.createTreeNode(draft -> {  
        draft.setParent(  
            Immutables.createTreeNode(daddyDraft -> {  
                daddyDraft.setName("Daddy")  
            })  
        )  
    });
```

```
val treeNode = TreeNode {  
    parent = TreeNode {  
        name = "Daddy"  
    }  
}
```

### 新增基于lambda的`applyParent`[​](#新增基于lambda的applyparent "新增基  于lambda的applyparent的直接链接")

信息

此功能仅适用于Java

kotlin的代码已经足够简洁了，不需要这种方法来简化代码。

TreeNodeDraft.java

```
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    @OldChain  
    TreeNodeDraft applyParent(  
        DraftConsumer<TreeNodeDraft> block  
    );  
  
    @OldChain  
    TreeNodeDraft applyParent(  
        TreeNode base,   
        DraftConsumer<TreeNodeDraft> block  
    );  
  
    ...省略其它代码...  
}
```

这两个setter用于简化的代码。由于两个方法的用法高度相似，仅以第一个举例。

1. 比较啰嗦的写法

   ```
   TreeNode oldTreeNode = Immutables.createTreeNode(draft -> {  
           draft.setParent(  
               Immutables.createTreeNode(daddyDraft -> {  
                   daddyDraft.setName("Daddy")  
               })  
           )  
       });
   ```
2. 比较简洁的写法

   ```
   TreeNode oldTreeNode = Immutables.createTreeNode(draft -> {  
           draft.applyParent(daddyDraft -> {  
               daddyDraft.setName("Daddy")  
           })  
       });
   ```

二者完全等价。

## 集合关联[​](#集合关联 "集合关联的直接链接")

`TreeNode.childNodes`是一个关联属性。其类型是集合，而非对象。如果用ORM的话来讲，是一对多或多对多关联。

`TreeNodeDraft`为其定义了多个方法

### 继承getter `childNodes()`[​](#继承getter-childnodes "继承getter-childnodes的直接链接")

无论对Java还是kotlin，`TreeNodeDraft`无法覆盖`childNodes()`方法的返回类型，从语法层面上讲，只能继承`TreeNode`的`childNodes()`方法。

* Java
* Kotlin

```
List<TreeNode> childNodes();
```

```
var childNodes: List<TreeNode>
```

警告

虽然这个的方法在`TreeNode`接口中定义的返回类型是`List<TreeNode>`，但是，被`TreeNodeDraft`接口继承后，应该将其返回类型理解成`List<TreeNodeDraft>`。

信息

如果draft对象的集合关联被设置过，返回的集合中的所有元素都是draft。这样，用户就可以直接修改集合中更深的关联对象。

* Java
* Kotlin

```
TreeNode newTreeNode = Immutables.createTreeNode(treeNode, draft -> {  
        ((TreeNodeDraft)  
            draft  
                .childNodes().get(0)  
        ).setName("Son");  
        ((TreeNodeDraft)  
            draft  
                .childNodes().get(0)  
                .childNodes().get(0)  
        ).setName("Grandson");  
    });
```

```
val newTreeNode = TreeNode(treeNode) {  
    (childNodes[0] as TreeNodeDraft)  
        .name = "Son"  
    (childNodes[0].childNodes[0] as TreeNodeDraft)  
        .name = "Son"  
}
```

危险

上述代码中，两个强制类型转换对开发体验产生了显著的破坏，所以，不推荐在实际项目中如此使用。

为实现相同目的，更推荐接下来要介绍的`childNodes(boolean)`方法。

### 新增getter `childNodes(boolean)`[​](#新增getter-childnodesboolean "新增getter-childnodesboolean的直接链接")

* Java
* Kotlin

TreeNodeDraft.java

```
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    List<TreeNodeDraft> childNodes(boolean autoCreate);  
  
    ...省略其他代码...  
}
```

`childNodes(false)`和`childNodes()`等价，如果draft对象的属性`childNodes`未被设置，访问它会导致异常。
`childNodes(true)`会解决这个问题，如果集合关联属性还未被设置，则自动创建并设置一个集合，然后允许用户修改该集合。

TreeNodeDraft.kt

```
@DslScope  
public interface TreeNodeDraft : TreeNode, Draft {  
  
    override var childNodes: List<TreeNode>  
      
    fun childNodes(): MutableList<TreeNode>  
  
    ...省略其他代码...  
}
```

* `childNodes`属性的getter等价于Java的`childNodes(false)`。如果可变代理的此属性未被设置, 访问它会导致异常。
* `childNodes()`函数等价于Java的`childNodes(true)`，如果属性未被设置则自动创建, 允许用户修改该集合。

* Java
* Kotlin

```
TreeNode newTreeNode = Immutables.createTreeNode(treeNode, draft -> {  
        draft  
            .childNodes(false)  
            .get(0)  
            .setName("Son");  
        draft  
            .childNodes(false)  
            .get(0)  
            .childNodes(false)  
            .get(0)  
            .setName("Grandson");  
    });
```

提示

诚然，当参数为 true 时，childNodes(boolean) 能够在未指定 childNodes 属性时自动创建集合。

然而，当参数为 false 时，同样也很有用。由于此方法的返回类型是`List<TreeNodeDraft>`而不是`List<TreeNode>`，因此上一个示例中对开发人员不友好的代码将不再存在。

```
val newTreeNode = TreeNode(treeNode) {  
    childNodes()[0].name = "Son"  
    childNodes()[0].childNodes()[0].name = "Grandson"  
}
```

### 新增`setChildNodes`[​](#新增setchildnodes "新增setchildnodes的直接链接")

* Java
* Kotlin

TreeNodeDraft.java

```
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    @OldChain  
    TreeNodeDraft setChildNodes(List<TreeNode> childNodes);  
  
    ...省略其他代码...  
}
```

TreeNodeDraft.kt

```
@DslScope  
public interface TreeNodeDraft : TreeNode, Draft {  
  
    // var, not val  
    override var childNodes: List<TreeNode>  
  
    ...省略其他代码...  
}
```

该setter允许用户替换整个关联集合。

* Java
* Kotlin

```
TreeNode treeNode = Immutables.createTreeNode(draft -> {  
        draft.setChildNodes(  
            Arrays.asList(  
                Immutables.createTreeNode(childDraft -> {  
                    childDraft.setName("Eldest son")  
                }),  
                Immutables.createTreeNode(childDraft -> {  
                    childDraft.setName("Second son")  
                })  
            )  
        )  
    });
```

```
val treeNode = TreeNode {  
    childNodes = listOf(  
        TreeNode {  
            name = "Eldest son"  
        },  
        TreeNode {  
            name = "Second son"  
        }  
    )  
}
```

信息

略显繁琐，更推荐下面即将介绍的`addIntoChildNodes`

### 新增`addIntoChildNodes`[​](#新增addintochildnodes "新增addintochildnodes的直接链接")

上面的例子中，我们使用`setChildNodes`替换了整个集合，但是还可以选择以逐个添加集合元素，而非一次性替换整个集合。

生成代码如下

* Java
* Kotlin

TreeNodeDraft.java

```
public interface TreeNodeDraft extends TreeNode, Draft {  
  
    @OldChain  
    TreeNodeDraft addIntoChildNodes(  
        DraftConsumer<TreeNodeDraft> block  
    );  
  
    @OldChain  
    TreeNodeDraft addIntoChildNodes(  
        TreeNode base,   
        DraftConsumer<TreeNodeDraft> block  
    );  
  
    ...省略其他代码...  
}
```

TreeNodeDraft.java

```
@DslScope  
public interface TreeNodeDraft : TreeNode, Draft {  
  
    public fun childNodes(): MutableList<TreeNodeDraft>  
  
    ...省略其他代码...  
}  
  
public fun MutableList<TreeNodeDraft>.addBy(  
    base: TreeNode? = null,  
    block: TreeNodeDraft.() -> Unit  
): MutableList<TreeNodeDraft> {  
    ...omit...  
    return this;  
}
```

你应该如此使用。

* Java
* Kotlin

```
TreeNode treeNode = Immutables.createTreeNode(draft -> {  
        draft  
            .addIntoChildNodes(childDraft ->  
                childDraft.setName("Eldest son")  
            )  
            .addIntoChildNodes(childDraft ->  
                childDraft.setName("Second son")  
            )  
    });
```

```
val treeNode = TreeNode {  
    childNodes().addBy {  
        name = "Eldest son"  
    }  
    childNodes().addBy {  
        name = "Second son"  
    }  
}
```

信息

这种写法隐含了一个功能，draft对象的属性`childNodes`未被设置，自动创建集合。即，内置了一个`childNodes(true)`。

很明显，这种写法比使用setter替换整个集合更简单，所以，更推荐这种写法。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/object/draft.mdx)

最后于 **2025年9月16日** 更新