---
title: 项目介绍 ✦
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/overview/introduction
---

# 项目介绍 ✦

## 1. Jimmer是什么?

Jimmer是JVM中最先进的ORM，同时面向Java和Kotlin

-   ORM核心

    -   所有读写相关API，都为操作任意复杂的数据结构而设计，而非为操作简单对象而设计

        -   实体对象可以是残缺的的，即使任何读写操作都可以只针对对象的部分属性，而非所有属性
        -   直接读写任意复杂的数据结构，数据结构的层次和形状由具体业务场景随意指定，无需事先统一固化设计

    -   为批量操作而设计

        -   查询功能没有`N + 1`问题，即使实体对象具备用户自定义的复杂的业务计算属性，仍然如此
        -   充分发挥底层数据批量修改的能力，被保存数据结构的每一层对象都采用JDBC批量操作
        -   充分发挥底层数据库的upsert能力，尽可能避免复杂批量修改中夹带select语句，如果无法做到，清晰地告诉用户为什么
        -   即使执行复杂的批量修改，对常见的数据库约束错误进行细致的调查的翻译，业务项目亲自处理数据库约束异常成为历史

    -   强类型DSL支持

        -   强类型DSL和使用数据库特有能力不冲突，你仍然可以使用特定数据库产品的特有功能
        -   为任意复杂的多表动态查询而设计，一改业内绝大部分动态查询方案都是针对单表操作的现状
        -   智能优化生成的SQL
        -   不仅为了生成SQL，更是为了超越Native SQL，提供更便捷的能力

-   以此ORM为基础打造了一套综合性方案解决方案，包括

    -   DTO语言，以极其廉价的成本解决多数情况下业务模型和数据模型不完全一致的问题
    -   更全面更强大的缓存机制，以及高度自动化的缓存一致性
    -   更强大客户端文档和代码生成能力，包括Jimmer独创的远程异常
    -   快速创建GraphQL服务
    -   跨越微服务的远程实体关联

>   目前未完成，但`1.0.0`会支持的功能
>
>   1. 支持子查询作为`from`或`join`的基表
>   2. 支持更强大的非对象查询API
>   3. 支持实体模型的继承和多态
>   4. 支持外键引用非id属性

## 2. 一个新的ORM，为什么？

### 现有痛点

当前技术生态下，访问关系型数据库技术体系存在很大缺陷，请看下图。

![mirror](https://babyfish-ct.github.io/jimmer-doc/img/cmp.svg)

1.  以JPA为代表的静态语言ORM ## 优点

便捷，代码安全*(本身基于强类型语言，大部分代码是安全的。如果结合QueryDSL使用，则可以保证所有代码都是安全的)*

## 缺点

缺乏灵活性

-   即使JPA从2.1开始支持EntityGraph，控制被查询数据格式的灵活性仍然非常有限。该方案粒度仍然太粗，控制能力远没GraphQL这类技术的细腻。

-   保存对象时，细节行为受普通属性的`insertable`、`updateable`和关联属性的`cascade`配置的控制，这类配置在实体类型中被硬编码固化，被保存的数据结构的格式是固定的，没有灵活性。

-   如果要发挥ORM的优势，就必须查询对象的大部分非关联属性 *(少数`@Basic(fetch = FetchType.Lazy)`属性除外，它们多为lob设计)*；如果只想查询一部分属性，就必须放弃对象查询，转而使用这些属性的多列查询，丧失ORM本该有的便捷性和核心价值。

2.  以为ActiveRecord *(Ruby)* 为代表的动态语言ORM ## 优点

基于动态语言的ORM，只需将动态语言对象结构的灵活性和ORM的实现结合起来，就能兼顾便捷和灵活。

## 缺点

动态语言虽然既便捷又灵活，但是代码缺乏可维护性且不利于多人协同开发是众所周知的缺点。

现代软件往往是复杂的，需要团队协作来完成，是否利于团队成员之间协同，远比个人对编程的认知重要。

这里，不想过多地讨论动静之争，但是有一点需要指出：既然选择了静态语言Java/Kotlin，就应该以静态语言的方式使用它，
而不能使用以jFinal为代表的将静态语言当成动态语言用的方案，更不能在应用中频繁地使用`java.util.Map`来代替数据对象。
这类做法违背了选择Java/Kotlin的初衷，如果一定要怎么做，为什么不直接选动态语言呢？

3.  以MyBatis为代表的轻量级SQL Builder/Mapper ## 优点

直接编写SQL，随意且灵活；本身是强类型框架，具有代码安全性 *(MyBatis生态也有强类型SQL DSL扩展，可以解决原生SQL字符串导致的代码不安全问题)*

## 缺点

便捷性的严重缺失，重复劳动量极大。

MyBatis没有统一实体的概念，而是面对具体业务场景DTO，实现ResultSet和这些DTO的映射。由于业务场景多，各DTO类型之间相似却不同，冗余度很高，导致重复劳动量极高。

除了以孤单对象为载体的CRUD外，对多个对象彼此关联而成的复杂数据结构的支持较弱，缺乏必要抽象，导致太多繁重的低级任务被推卸给开发人员
*(不少开发人员长期被这类繁重的任务所累，但自己一直没察觉)*。

## 原生SQL真的是最好方案吗？

这个派别最引以为豪的观点是："直接书写SQL会带来更直接的控制力，这种直接控制力优于任何ORM"。在这个领域长期的技术停滞中，不少开发人员对此深信不疑。
然而，后续章节`3.4. SQL DSL`为此准备一个惊喜，来改变这个根深蒂固的认知。

### 根本原因

上文中，我们阐述了关系型数据库领域的三种常见方案，但无论如何选择，我们都无法兼顾便捷性、灵活性和代码安全性。为什么会导致这样呢？

:::info
就JVM生态而言，POJO是导致这个问题的根本原因。

POJO*(也可以叫结构体)*缺乏必要的灵活性和表达力，却几乎被所有的JVM框架作为数据模型和核心，严重限制了JVM生态的技术创新。
:::

因此，在Jimmer中，ORM实体对象并非POJO。而是另外一种独特的万能数据对象*(后文会介绍)*，这种独特的实体对象撑起了Jimmer所有上层重大的变革，是整个框架的基石。

:::note
事实上，Jimmer实体对象不仅可以应用在ORM领域，它几乎可以用在任何以结构化数据维护为目的的场景，并提升各种技术栈的表达力。

目前，Jimmer实体仅在关系型数据库访问领域发挥出作用，只是因为精力不够所致。
:::

## 3. 完整的功能

![mirror](https://babyfish-ct.github.io/jimmer-doc/img/jimmer.svg)

### 3.1. Business Model

在信息类系统中，存在两种对象。

-   实体：实体对象是全局统一的，对象之间的存在丰富彼此关联。

    实体对象往往和数据库非常接近，具备极高的稳定性。

-   DTO：针对特定业务的输入/输出对象，通常是从全局实体关系网上撕下来的一个局部碎片，该碎片的大小和形状非常灵活。

    DTO类型数量庞大，每一个业务接口对DTO对象的格式都有独特的需求，彼此可能相似但又不同，具备明显的`DtoExplosion`（官方页面含交互演示）。而且易受到需求变化的影响，不稳定。

:::tip
Entity类型是全局统一数据存储模型，不易被需求变更影响，相对稳定，被视为高价值类型。

DTO类型作为每个业务输入/输出，相对随意，容易因需求变动而不稳定，被视为低价值类型。

Jimmer主张开发人员把精力集中在高价值的实体模式的设计上；对于低价值的DTO类型，有的时候并不需要，有的时候需要。
即使需要，也可以用极其廉价的方式自动生成。因此，基于Jimmer构建的项目具备优秀的抗需求变动的能力。
:::

#### 3.1.1. Jimmer Entity

Jimmer实体定义和JPA实体很接近。
<table>
<thead>
<tr>
<th>描述</th>
<th>类型定义</th>
</tr>
</thead>
<tbody>
<tr>
<td>

书店，具备一个关联属性

-   `books`: 一对多关联

    > 该书店拥有的书籍

</td>
<td>

**Java**

```java
@Entity
public interface BookStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id();

    String name();

    @Nullable
    String website();

    @OneToMany(mappedBy = "store")
    List<Book> books();
}
```

**Kotlin**

```kotlin
@Entity
interface BookStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val name: String

    val website: String?

    @OneToMany(mappedBy = "store")
    val books: List<Book>
}
```

</td>
</tr>
<tr>
<td>

书籍，具备两个关联属性

-   `store`: 多对一关联

    > 该书籍所属的书店

-   `authors`: 多对多关联

    > 参与编写该书的所有作者

</td>
<td>

**Java**

```java
@Entity
public interface Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id();

    @Key
    String name();

    @Key
    int edition();

    BigDecimal price();

    @Nullable
    @ManyToOne
    BookStore store();

    @ManyToMany
    List<Author> authors();
}
```

**Kotlin**

```kotlin
@Entity
interface Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val name: String

    @Key
    val edition: Int

    val price: BigDecimal

    @ManyToOne
    val store: BookStore?

    @ManyToMany
    val authors: List<Author>
}
```

</td>
</tr>
<tr>
<td>

作者，具备一个关联属性

-   `books`: 多对多关联

    > 当前作者参与编写的所有书籍

</td>
<td>

**Java**

```java
@Entity
public interface Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    UUID id();

    @Key
    String firstName();

    @Key
    String lastName();

    Gender gender();

    @ManyToMany(mappedBy = "authors")
    List<Book> books();
}
```

**Kotlin**

```kotlin
@Entity
interface Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val firstName String

    @Key
    val lastName: String

    val gender: Gender

    @ManyToMany(mappedBy = "authors")
    val books: List<Book>
}
```

</td>
</tr>
</tbody>
</table>

之前讨论过，Jimmer实体并非POJO，所以，被声明为`interface`，而非`class`。

那么，谁负责实现此接口呢？是上图中的`Jimmer Precompiler`
*(对于Java而言，就是[APT](https://www.jetbrains.com/help/idea/annotation-processors-support.html)；
对于Kotlin而言，就是[KSP](https://kotlinlang.org/docs/ksp-overview.html))*

Jimmer实体支持两个重要特征，动态性和不可变性

-   动态性

    Jimmer对象在静态语言和动态语言之间寻求最佳平衡，把二者的优点结合起来：

    -   静态语言数据对象具有高性能、拼写安全、类型安全、甚至空安全*(如果使用Kotlin的话)*的优点，Jimmer实体吸收了这些优点。

    -   动态语言数据对象具有高度的灵活性，Jimmer实体吸收了这个优点，每个属性都可以缺失*(但是不能如同动态语言一样增加属性，因为这必然会破坏静态语言的特性，Jimmer也不需要此能力)*

        > 对Jimmer而言，对象缺少某个属性 *(其值未知)* 和 对象的某个属性为null *(其值已知)* 是完全不同的两回事。

    :::tip
    这种平衡设计，可以在享受静态语言好处的同时，为数据结构赋予
## 1. 较少属性

**Java**

```java
Book book = Immutables.createBook(draft -> {
    draft.setName("Learning GraphQL");
});
```

**Kotlin**

```kotlin
val book = Book {
    name = "Learning GraphQL"
}
```

这时，`book`对象对应的JSON如下

```json
{"name": "Learning GraphQL"}
```

## 2. 较多属性

**Java**

```java
Book book = Immutables.createBook(draft -> {
    draft.setName("Learning GraphQL");
    draft.setEdition(1);
    draft.setPrice(new BigDecimal("49.99"));
});
```

**Kotlin**

```kotlin
val book = Book {
    name = "Learning GraphQL"
    edition = 1
    price = BigDecimal("49.99")
}
```

这时，`book`对象对应的JSON如下

```json
{
    "name": "Learning GraphQL",
    "edition": 1,
    "price": 49.99
}
```

## 3. 包含关联

**Java**

```java
Book book = Immutables.createBook(draft -> {
    draft.setName("Learning GraphQL");
    draft.setEdition(1);
    draft.setPrice(new BigDecimal("49.99"));
    draft.applyStore(store -> {
        store.setName("O'REILLY");
        store.setWebsite("https://www.oreilly.com/");
    });
});
```

**Kotlin**

```kotlin
val book = Book {
    name = "Learning GraphQL"
    edition = 1
    price = BigDecimal("49.99")
    store {
        name = "O'REILLY"
        website = "https://www.oreilly.com/"
    }
}
```

这时，`book`对象对应的JSON如下

```json
{
    "name": "Learning GraphQL",
    "edition": 1,
    "price": 49.99,
    "store": {
        "name": "O'REILLY",
        "website": "https://www.oreilly.com/"
    }
}
```

## 4. 更多关联 (更大广度)

**Java**

```java
Book book = Immutables.createBook(draft -> {
    draft.setName("Learning GraphQL");
    draft.setEdition(1);
    draft.setPrice(new BigDecimal("49.99"));
    draft.applyStore(store -> {
        store.setName("O'REILLY");
        store.setWebsite("https://www.oreilly.com/");
    });
    draft.addIntoAuthors(author -> {
        author.setFirstName("Eve");
        author.setLastName("Procello");
        author.setGender(Gender.FEMALE);
    });
    draft.addIntoAuthors(author -> {
        author.setFirstName("Alex");
        author.setLastName("Banks");
        author.setGender(Gender.MALE);
    });
});
```

**Kotlin**

```kotlin
val book = Book {
    name = "Learning GraphQL"
    edition = 1
    price = BigDecimal("49.99")
    store {
        name = "O'REILLY"
        website = "https://www.oreilly.com/"
    }
    authors().addBy {
        firstName = "Eve"
        setLastName = "Procello"
        gender = Gender.FEMALE
    }
    authors().addBy {
        firstName = "Alex"
        lastName = "Banks"
        gender = Gender.MALE
    }
}
```

这时，`book`对象对应的JSON如下

```json
{
    "name": "Learning GraphQL",
    "edition": 1,
    "price": 49.99,
    "store": {
        "name": "O'REILLY",
        "website": "https://www.oreilly.com/"
    },
    "authors": [
        {
            "firstName": "Eve",
            "lastName": "Procello",
            "gender:" "FEMALE"
        },
        {
            "firstName": "Alex",
            "lastName": "Banks",
            "gender:" "MALE"
        }
    ]
}
```

### 5. 更深关联 (更大深度)

和前面的例子不同，这里，我们选择用`BookStore`作为数据结构的聚合根，而非`Book`。

**Java**

```java
BookStore store = Immutables.createBookStore(draft -> {
    draft.setName("O'REILLY");
    draft.setWebsite("https://www.oreilly.com/");
    draft.addIntoBooks(book -> {
        book.setName("Learning GraphQL");
        book.setEdition(1);
        book.setPrice(new BigDecimal("49.99"));
        book.addIntoAuthors(author -> {
            author.setFirstName("Eve");
            author.setLastName("Procello");
            author.setGender(Gender.FEMALE);
        });
        book.addIntoAuthors(author -> {
            author.setFirstName("Alex");
            author.setLastName("Banks");
            author.setGender(Gender.MALE);
        });
    });
});
```

**Kotlin**

```kotlin
val store = BookStore {
    name = "O'REILLY"
    website = "https://www.oreilly.com/"
    books().addBy {
        name = "Learning GraphQL"
        edition = 1
        price = BigDecimal("49.99")
        authors().addBy {
            firstName = "Eve"
            setLastName = "Procello"
            gender = Gender.FEMALE
        }
        authors().addBy {
            firstName = "Alex"
            lastName = "Banks"
            gender = Gender.MALE
        }
    }
}
```

这时，`store`对象对应的JSON如下

```json
{
    "name": "O'REILLY",
    "website": "https://www.oreilly.com/",
    "books": [
        {
            "name": "Learning GraphQL",
            "edition": 1,
            "price": 49.99,
            "authors": [
                {
                    "firstName": "Eve",
                    "lastName": "Procello",
                    "gender:" "FEMALE"
                },
                {
                    "firstName": "Alex",
                    "lastName": "Banks",
                    "gender:" "MALE"
                }
            ]
        }
    ]
}
```
。

    这种绝对的灵活性，既可用于表达查询业务的输出格式，也可用于表达保存业务的输入格式。

    这导致Jimmer拥有了崭新的定位：一个为任意形状数据结构设计的ORM。其所有功能都是为了操作任意形状的数据结构，而非一个个简单的实体对象。
    :::

-   不可变性

    Jimmer对象是不可变对象。不可变对象的好处是多方面的，相关文章和论述非常多，本文不做重复性讨论。

    :::tip
    Jimmer选择不可变对象是为了让数据结构绝不包含[循环引用](https://en.wikipedia.org/wiki/Circular_reference)。

    这可以保证由Jimmer实体及彼此关联组合而成的数据结构一定能够被直接Jackson序列化，并不需要使用诡异的序列化技巧为JSON植入任何特殊的额外信息，任何编程语言都可以轻松理解。
    :::

    然而，不可变对象也存在缺点。比如，现有一个很深的数据结构，那么基于它按照一些修改的愿望创建出新的数据结构会很困难，难度随着深度的变大急剧增加。

    -   ORM和很深的数据结构打交道，而Java的record和Kotlin的data class不适合处理很深数据结构。

    -   既对Java和Kotlin进行双语支持，又善于基于现有深层次数据结构按照一些修改的愿望创建出新的不可变数据结构的方案，目前的JVM生态没有。

    幸运的是，`JavaScript/TypeScript`领域存在一个足够强大的方案: [immer](https://github.com/immerjs/immer)，可以完美解决这个问题。该方案工作方式如下

    1.  基于现有不可变数据结构开启一个临时作用域。

    2.  在这个作用域内，开发人员可得到一个draft数据结构，该数据结构的形状和初始值和原数据结构完全一致，且可以被随意修改，包括修改任意深的子对象。

    3.  作用域结束后，draft数据结构会利用收集到的修改行为创建另外一个新的数据结构。其中，未被修改的局部会被优化处理，复用以前的旧对象。

    Immer完美结合了不可变对象和可变对象的优点，代码简单、功能强大、性能卓越。因此，Jimmer选择为JVM生态移植了immer，项目名称也是对其致敬。
接下来，我们会用三个步骤展示移植自[immer](https://github.com/immerjs/immer)的不可变对象的强大。

1.  定义不可变类型

2.  从头创建一个不可变数据结构

3.  基于已有的数据结构，按照一些修改愿望，创建新的不可变数据结构。

    > 最后这步是[immer](https://github.com/immerjs/immer)核心价值所在，请注意观察。

## 1. 定义不可变类型

演示此功能无需ORM实体的`@Entity`注解，非ORM的`@Immutable`足够。

**Java**

```java title="TreeNode.java"
package yourpackage;

import java.util.List;
import org.babyfish.jimmer.Immutable;

@Immutable
public interface TreeNode {

    String name();

    List<TreeNode> childNodes();
}
```

**Kotlin**

```kotlin title="TreeNode.kt"
package yourpackage

import org.babyfish.jimmer.Immutable

@Immutable
interface TreeNode {

    val name: String

    val childNodes: List<TreeNode>
}
```

## 2. 从头构建全新的数据

**Java**

```java
TreeNode treeNode = Immutables.createTreeNode(root -> {
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

**Kotlin**

```kotlin
val treeNode = TreeNode {
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

## 3. 基于现有数据创建新数据

**Java**

```java
TreeNode newTreeNode = Immutables.createTreeNode(

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

**Kotlin**

```kotlin
/*
 * val newTreeNode = treeNode.copy {
 *     ...
 * }
 * 其实是
 * val newTreeNode = TreeNode(treeNode) {
 *     ...
 * }
 * 的简写方式
 */
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

```javascript
treeNode: {
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

参考链接: [动态性](../object/dynamic.md)，[不可变性](https://babyfish-ct.github.io/jimmer-doc/zh/docs/object/immutable)

#### 3.1.2. Generated DTO Type

前文谈到，Jimmer实体在静态语言数据对象和动态语言数据对象之间寻找最佳平衡，其中动态性带来了极大的灵活性，并以此决定了整个框架的定位。

Jimmer对象允许某些属性缺失，对象缺少某个属性 *(其值未知)* 和 对象的某个属性为null *(其值已知)* 是完全不同的两回事。

1.  对于Jackson序列化而言，缺失的属性会被自动忽略，就如同我们之前展示的那样。

    如果服务端自己并不使用查询得到的实体对象，而是直接写入到Http Response中。对于这种情况，无需DTO，直接使用实体对象很方便。

2.  如果直接用Java/Kotlin代码访问不存在的属性，会导致异常。

**Java**

```java
Book book = Immutables.createBook(draft -> {
    draft.setName("SQL in Action");
    draft.setEdition(1);
});

System.out.println(book.price());
```

**Kotlin**

```kotlin
val book = Book {
    name = "SQL in Action"
    edition = 1
}

println(book.price)
```

这里，Book对象具备`name`和`edition`属性，但是，被标记处的代码企图访问不存在的`price`属性，这会导致如下异常:

```
Exception in thread "main" org.babyfish.jimmer.UnloadedException: The property "yourpackage.Book.price" is unloaded
	at yourpackage.BookDraft$Producer$Impl.price(BookDraft.java:285)
	at yourpackage.App.main(App.java:10)
```

    如果服务端自己要使用查询得到的实体对象，这会带来风险，尽管Jimmer实体在其他方面依旧保留了静态语言的特色，比如拼写安全、类型安全、甚至空安全*(如果使用kotlin)*。

    > 以JPA为例，从Hibernate3开始，lazy配置不再局限于关联属性，而是可以用于标量属性。后来演化为JPA之`@Basic`注解的`fetch`参数，
    > 请参考这[这里](https://docs.oracle.com/javaee%2F7%2Fapi%2F%2F/javax/persistence/Basic.html)。
    >
    > 这和Jimmer对象任何属性都可以缺失有一定相似性，只是Jimmer将此特征推广到了任何属性。所以，Jimmer的这个异常和[org.hibernate.LazyInitializationException](https://docs.jboss.org/hibernate/orm/5.6/javadocs/org/hibernate/LazyInitializationException.html)有一定相似性。

    所以，这并非由Jimmer制造的新问题，而是一个在静态语言ORM生态中早已存在和被接受的问题。然而，不可否认这的确对静态语言的安全性形成了破坏。

    如果要追求100%的静态语言安全性，使用`DTO`对象是唯一的方法。然而，目前JVM生态的DTO映射技术存在很大缺陷。
    -   要么显式地映射属性*(例如纯手工映射和转化)*，这种做法工作量巨大，枯燥且容易出错。
    -   要么隐式地映射属性*(例如采用[BeanUtils](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/BeanUtils.html)技术)*，这种做法会引入新的不安全问题，即，无法在编译发现的问题。

    即使你使用强大的[mapstruct](https://mapstruct.org/)，你所能做的也只是在这两个极端之间作出选择而已。

    因此，Jimmer提供了DTO语言，用户使用该语言编写非常简单的代码，编译项目即可自动生成各种丰富的DTO类型定义。

    :::tip
    DTO语言的设计目的，在于

    -   让生成DTO类型的过程足够简单，从而让DTO类型足够廉价。

    -   100%符合静态语言安全性，在编译时发现所有问题并报错。
    :::

    在任何子项目中 *(并不限制为实体定义子项目)*，开发人员都可以在`src/main/dto`目录下随意建立扩展名为`dto`的文件，
    廉价的自动生成各种DTO类型。
<table>
<thead>
<tr>
<th>DTO代码</th>
<th>生成的DTO类型定义</th>
</tr>
</thead>
<tbody>
<tr>
<td>

**较少属性**

```sh
export yourpackage.Book
    -> package yourpackage.dto

BookView {
    id
    name
}
```

</td>
<td>

**Java**

```java
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
public class BookView implements View<Book> {

    private long id;

    private String name;

    ...省略其他成员...
}
```

**Kotlin**

```kotlin
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
open class BookView(
    val id: Long
    val name: String
): View<Book> {
    ...省略其他成员...
}
```

</td>
</tr>
<tr>
<td>

**较多属性**

```sh
export yourpackage.Book
    -> package yourpackage.dto

BookView {
    #allScalars
}
```

</td>
<td>

**Java**

```java
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
public class BookView implements View<Book> {

    private long id;

    private String name;

    private int edition;

    private BigPrice price;

    ...省略其他成员...
}
```

**Kotlin**

```kotlin
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
open class BookView(
    val id: Long
    val name: String,
    val edition: Int,
    val price: BigDecimal
): View<Book> {
    ...省略其他成员...
}
```

</td>
</tr>
<tr>
<td>

**关联id**

```sh
export yourpackage.Book
    -> package yourpackage.dto

BookView {
    #allScalars

    id(store)

    id(authors) as authorIds
}
```

</td>
<td>

**Java**

```java
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
public class BookView implements View<Book> {

    private long id;

    private String name;

    private int edition;

    private BigPrice price;

    @Nullable

    private Long storeId;

    private List<Long> authorIds;

    ...省略其他成员...
}
```

**Kotlin**

```kotlin
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
open class BookView(
    val id: Long
    val name: String,
    val edition: Int,
    val price: BigDecimal,

    val storeId: Long? = null,

    val authorIds: List<Long> = emptyList()
): View<Book> {
    ...省略其他成员...
}
```

</td>
</tr>
<tr>
<td>

**关联对象**

```sh
export yourpackage.Book
    -> package yourpackage.dto

BookView {
    #allScalars

    store {
        #allScalars
        -id
    }

    authors {
        #allScalars
        -id
    }
}
```

</td>
<td>

**Java**

```java
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
public class BookView implements View<Book> {

    private long id;

    private String name;

    private int edition;

    private BigPrice price;

    @Nullable

    private TargetOf_store store;

    private List<TargetOf_authors> authors;

    ...省略其他成员...

    public static class TargetOf_store implements View<BookStore> {

        private String name;

        @Nullable
        private String website;

        ...省略其他成员...
    }

    public static class TargetOf_authors implements View<Author> {

        private String firstName;

        private String lastName;

        private Gender gender;

        ...省略其他成员...
    }
}
```

**Kotlin**

```kotlin
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
open class BookView(
    val id: Long
    val name: String,
    val edition: Int,
    val price: BigDecimal,

    val store: LonTargetOf_store? = null,

    val authors: List<TargetOf_authors> = emptyList()
): View<Book> {

    ...省略其他成员...

    class TargetOf_store(
        val name: String,
        val website: String? = null
    ): View<BookStore> {

        ...省略其他成员...
    }

    class TargetOf_authors(
        val firstName: String,
        val lastName: String,
        val gender: Gender
    ): View<Author> {

        ...省略其他成员...
    }
}
```

</td>
</tr>
<tr>
<td>

**递归关联**

```sh
export yourpackage.Tree
    -> package yourpackage.dto

RecursiveTreeView {
    #allScalars

    childNodes*
}
```

</td>
<td>

**Java**

```java
@GeneratedBy(file = "<yourproject>/yourpackage/RecursiveTreeView.dto")
public class RecursiveTreeView implements View<TreeNode> {

    private long id;

    private String name;

    @Nullable

    private List<RecursiveTreeView> childNodes;

    ...省略其他成员...
}
```

**Kotlin**

```kotlin
@GeneratedBy(file = "<yourproject>/yourpackage/Book.dto")
open class RecursiveTreeView(
    val id: Long
    val name: String,

    val childNodes: List<RecursiveTreeView>? = null
): View<TreeNode> {

    ...省略其他成员...
}
```

</td>
</tr>
</tbody>
</table>

    这种以极低成本快速生成的DTO类型可以和Jimmer实体对象彼此转换；因此，任何两种DTO类型都可以以Jimmer实体为中间媒介彼此转换。

参考链接：[DTO语言](../object/view/dto-language.md)

### 3.2. Fetcher ★

:::tip
Fetcher，是Jimmer三个最基础的核心功能之一 *(另外两个是Save Command和SQL DSL)*。

Jimmer为查询任意形状的数据结构而设计，能像[GraphQL](https://graphql.org/)那样细腻地控制被查询数据的格式。
:::

首先，通过这个动画来感受一下Jimmer随意控制被查询数据结构形状的能力 *(建立初步印象即可，不用看得太细)*。 `ObjectFetcherPanel`（官方页面含交互演示）

前文提到，既可以直接使用实体对象，也可以使用被廉价生成的DTO对象。Fetcher对这两种数据对象的查询都提供了一流的支持。让我们通过3个场景来展示其用法

1.  查询残缺对象

    所谓残缺对象，就是指查询对象的部分属性，其信息量还不如一个孤单对象丰富
    。
<table>
<thead>
<tr>
<th>用法1: 查询实体</th>
<th>用法2: 查询DTO</th>
</tr>
</thead>
<tbody>
<tr>
<td rowspan="2">

**查询代码**

**Java**

```java
BookTable table = Tables.BOOK_TABLE;
List<Book> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("Learning GraphQL"))
    .select(

        table.fetch(
            Fetchers.BOOK_FETCHER
                .name()
        )
    )
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        table.name eq "Learning GraphQL"
        select(

            table.fetchBy {
                name()
            }
        )
    }
    .execute()
```

</td>
<td>

**DTO代码**

```sh
export yourpackage.Book
    -> package yourpackage.dto;

BookView {
    id
    name
}
```

编译后，会自动生成Java/Kotlin类型`BookView`

</td>
</tr>
<tr>
<td>

**查询代码**

**Java**

```java
BookTable table = Tables.BOOK_TABLE;
List<BookView> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("Learning GraphQL"))
    .select(

        table.fetch(BookView.class)
    )
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        table.name eq "Learning GraphQL"
        select(

            table.fetch(BookView::class)
        )
    }
    .execute()
```

</td>
</tr>

<tr>
<td>

**输出结果**

```json
[
    {"id":10,"name":"Learning GraphQL"},
    {"id":11,"name":"Learning GraphQL"},
    {"id":12,"name":"Learning GraphQL"}
]
```

</td>
<td>

**输出结果**

```json
[
    BookView(id=10, name=Learning GraphQL),
    BookView(id=11, name=Learning GraphQL),
    BookView(id=12, name=Learning GraphQL)
]
```

</td>
</tr>
</tbody>
</table>

2.  附带关联对象

    选定一个实体作为聚合根，不但要查询聚合根对象，还要查询其关联对象，且深度和广度都不受限制，这种格式控制能力的细腻程度和GraphQL一样
    。
<table>
<thead>
<tr>
<th>用法1: 查询实体</th>
<th>用法2: 查询DTO</th>
</tr>
</thead>
<tbody>
<tr>
<td rowspan="2">

**查询代码**

**Java**

```java
BookTable table = Tables.BOOK_TABLE;
List<Book> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("Learning GraphQL"))
    .select(
        table.fetch(
            Fetchers.BOOK_FETCHER
                .allScalarFields()

                .store(
                    Fetchers.BOOK_STORE_FETCHER
                        .allScalarFields()
                )

                .authors(
                    Fetchers.AUTHOR_FETCHER
                        .allScalarFields()
                )
        )
    )
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        table.name eq "Learning GraphQL"
        select(

            table.fetchBy {
                allScalarFields()

                store {
                    allScalarFields()
                }

                authors {
                    allScalarFields()
                }
            }
        )
    }
    .execute()
```

</td>
<td>

**DTO代码**

```sh
export yourpackage.Book
    -> package yourpackage.dto;

BookView {
    #allScalars
    store {
        #allScalars
    }
    authors {
        #allScalars
    }
}
```

编译后，会自动生成Java/Kotlin类型`BookView`

</td>
</tr>
<tr>
<td>

**查询代码**

**Java**

```java
BookTable table = Tables.BOOK_TABLE;
List<BookView> books = sqlClient
    .createQuery(table)
    .where(table.name().eq("Learning GraphQL"))
    .select(

        table.fetch(BookView.class)
    )
    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        table.name eq "GraphQL"
        select(

            table.fetch(BookView::class)
        )
    }
    .execute()
```

</td>
</tr>

<tr>
<td>

**输出结果**

```json
[
    {
        "id": 1,
        "name": "Learning GraphQL",
        "edition": 1,
        "price": 50,

        "store": {
            "id": 1,
            "name": "O'REILLY",
            "website": null
        },

        "authors": [
            {
                "id": 2,
                "firstName": "Alex",
                "lastName": "Banks",
                "gender": "MALE"
            },
            {
                "id": 1,
                "firstName": "Eve",
                "lastName": "Procello",
                "gender": "FEMALE"
            }
        ]
    },
    {
        "id": 2,
        "name": "Learning GraphQL",
        "edition": 2,
        "price": 55,

        "store": {
            "id": 1,
            "name": "O'REILLY",
            "website": null
        },

        "authors": [
            {
                "id": 2,
                "firstName": "Alex",
                "lastName": "Banks",
                "gender": "MALE"
            },
            {
                "id": 1,
                "firstName": "Eve",
                "lastName": "Procello",
                "gender": "FEMALE"
            }
        ]
    },
    {
        "id": 3,
        "name": "Learning GraphQL",
        "edition": 3,
        "price": 51,

        "store": {
            "id": 1,
            "name": "O'REILLY",
            "website": null
        },

        "authors": [
            {
                "id": 2,
                "firstName": "Alex",
                "lastName": "Banks",
                "gender": "MALE"
            },
            {
                "id": 1,
                "firstName": "Eve",
                "lastName": "Procello",
                "gender": "FEMALE"
            }
        ]
    }
]
```

</td>
<td>

**输出结果**

```json
[
    BookView(
        id=1,
        name=Learning GraphQL,
        edition=1,
        price=50.00,

        store=BookView.TargetOf_store(
            id=1,
            name=O'REILLY,
            website=null
        ),

        authors=[
            BookView.TargetOf_authors(
                id=2,
                firstName=Alex,
                lastName=Banks,
                gender=MALE
            ),
            BookView.TargetOf_authors(
                id=1,
                firstName=Eve,
                lastName=Procello,
                gender=FEMALE
            )
        ]
    ),
    BookView(
        id=2,
        name=Learning GraphQL,
        edition=2,
        price=55.00,

        store=BookView.TargetOf_store(
            id=1,
            name=O'REILLY,
            website=null
        ),

        authors=[
            BookView.TargetOf_authors(
                id=2,
                firstName=Alex,
                lastName=Banks,
                gender=MALE
            ),
            BookView.TargetOf_authors(
                id=1,
                firstName=Eve,
                lastName=Procello,
                gender=FEMALE
            )
        ]
    ),
    BookView(
        id=3,
        name=Learning GraphQL,
        edition=3,
        price=51.00,

        store=BookView.TargetOf_store(
            id=1,
            name=O'REILLY,
            website=null
        ),

        authors=[
            BookView.TargetOf_authors(
                id=2,
                firstName=Alex,
                lastName=Banks,
                gender=MALE
            ),
            BookView.TargetOf_authors(
                id=1,
                firstName=Eve,
                lastName=Procello,
                gender=FEMALE
            )
        ]
    )
]
```

</td>
</tr>
</tbody>
</table>

3.  递归查询

    如果实体包含自关联属性，可以进行递归查询 *(截止到目前为止，GraphQL不支持递归查询)*
    。
<table>
<thead>
<tr>
<th>用法1: 查询实体</th>
<th>用法2: 查询DTO</th>
</tr>
</thead>
<tbody>
<tr>
<td rowspan="2">

**查询代码**

**Java**

```java
TreeNode rootNode = sqlClient
    .findById(
        Fetchers.TREE_NODE_FETCHER
                .allScalarFields()
                // 向上递归

                .recursiveParent() ❶
                // 向下递归

                .recursiveChildNodes() ❷
        10L
    );
```

**Kotlin**

```kotlin
val rootNode = sqlClient
    .findById(
        newFetcher(TreeNode::class).by {
            allScalarFields()
            // 向上递归

            `parent*`() ❶
            // 向下递归

            `childNodes*`() ❷
        }
    )
```

</td>
<td>

**DTO代码**

```sh
export yourpackage.TreeNode
    -> package yourpackage.dto;

RecursiveTreeNodeView {
    id
    // 向上递归

    parent* ❶
    // 向下递归

    childNodes* ❷
}
```

编译后，会自动生成Java/Kotlin类型`RecursiveTreeNodeView`

</td>
</tr>
<tr>
<td>

**查询代码**

**Java**

```java
RecursiveTreeNodeView rootNode = sqlClient
    .findById(
        RecursiveTreeNodeView.class,
        10L
    );
```

**Kotlin**

```kotlin
val rootNode = sqlClient
    .findById(
        RecursiveTreeNodeView::class,
        10L
    );
```

</td>
</tr>

<tr>
<td>

**输出结果**

```json
{
    "id": 10,
    "name": "Woman",
    "parent": { ❶
        "id": 9,
        "name": "Clothing", ❶
        "parent": {
            "id": 1,
            "name": "Home",
            "parent": null ❶
        }
    },
    "childNodes": [ ❷
        {
            "id": 11,
            "name": "Casual wear",
            "childNodes": [ ❷
                {
                    "id": 12,
                    "name": "Dress",
                    "childNodes": [] ❷
                },
                {
                    "id": 13,
                    "name": "Miniskirt",
                    "childNodes": [] ❷
                },
                {
                    "id": 14,
                    "name": "Jeans",
                    "childNodes": [] ❷
                }
            ]
        },
        {
            "id": 15,
            "name": "Formal wear",
            "childNodes": [ ❷
                {
                    "id": 16,
                    "name": "Suit",
                    "childNodes": [] ❷
                },
                {
                    "id": 17,
                    "name": "Shirt",
                    "childNodes": [] ❷
                }
            ]
        }
    ]
}
```

</td>
<td>

**输出结果**

```json
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

</td>
</tr>
</tbody>
</table>

:::info
用户应该积极地使用该功能查询任意复杂的数据结构，而非自己编写业务代码去获取不同部分的数据再拼接成一个整体，因为Fetcher具有如下很难用业务代码替代的优势：

-   不但能处理关联属性，还能处理[复杂计算属性](../mapping/advanced/calculated/transient.md)
-   能充分利用的Jimmer的`AssociationCache`（官方页面含交互演示）
    和`CalculatedCache`（官方页面含交互演示）大幅优化查询复杂数据结构的性能
-   实体支持[远程关联](../spring/spring-cloud.md)，即，跨越微服务边界的ORM关联，Jimmer自动从不同的微服务查询数据并组装成一个数据结构
-   整个层层深入的过程由广度递归驱动，以支持批量查询优化
:::

无需开发人员做任何工作，ORM本身就具备了可媲美GraphQL的强大能力。所以，无论用Jimmer来构建REST服务，还是GraphQL服务，查询相关任务都非常简单。

-   快速构建REST服务

    由服务端控制返回对象的形状。如果某个HTTP API需要返回的数据结构形状是什么，开发人员都有两种选择：直接使用实体类型，或用DTO语言廉价地生成DTO类型。

    无论如何选择，客户端都是被动地接受服务端返回的数据格式。即使，客户端需要的数据结构形状种类非常多，对基于Jimmer开发的服务端影响很小。

    如果开发人员选择直接返回实体对象而非DTO。这时服务端没有`DtoExplosion`（官方页面含交互演示），
    这对服务端不是问题；但是对于客户端而言，这是非常糟糕的。

    Jimmer为客户端生成Open Api文档和TypeScript代码，如果服务端开发人员选择直接返回实体对象，
    则可用`@FetchBy`注解修饰Web方法的返回类型，即可在Open Api和TypeScript代码中为客户端定义DTO类型。`CommunicationPanel`（官方页面含交互演示）

-   快速构建GraphQL服务

    通常情况下，提供GraphQL服务工作量不小，开发人员要花很大的精力去支持GraphQL对象之间丰富的关联。

    然而，基于Jimmer实现GraphQL是非常容易的，因为ORM本身已经有了和GraphQL类似的能力，开发人员只需为GraphQL查询API实现聚合根对象的查询即可，GraphQL对象之间丰富的关联由Jimmer自动实现。

<details>
<summary>参考连接</summary>

|连接|描述|
|---|---|
|[快速预览/查询任意形状](https://babyfish-ct.github.io/jimmer-doc/zh/docs/quick-view/fetch)|快速预览：查询任意形状|
|[jimmer-examples/tree/main/java/jimmer-sql](https://github.com/babyfish-ct/jimmer-examples/tree/main/java/jimmer-sql)|例子项目，使用Java和Jimmer构建REST服务|
|[jimmer-examples/tree/main/kotlin/jimmer-sql-kt](https://github.com/babyfish-ct/jimmer-examples/tree/main/kotlin/jimmer-sql-kt)|例子项目，使用Kotlin和Jimmer构建REST服务|
|[jimmer-examples/tree/main/java/jimmer-sql-graphql](https://github.com/babyfish-ct/jimmer-examples/tree/main/java/jimmer-sql-graphql)|例子项目，使用Java和Jimmer构建GraphQL服务|
|[jimmer-examples/tree/main/kotlin/jimmer-sql-graphql-kt](https://github.com/babyfish-ct/jimmer-examples/tree/main/kotlin/jimmer-sql-graphql-kt)|例子项目，使用Kotlin和Jimmer构建GraphQL服务|
|[jimmer-examples/tree/main/java/jimmer-cloud](https://github.com/babyfish-ct/jimmer-examples/tree/main/java/jimmer-cloud)|例子项目，使用Java和Jimmer支持跨越微服务边界的ORM远程关联|
|[jimmer-examples/tree/main/kotlin/jimmer-cloud-kt](https://github.com/babyfish-ct/jimmer-examples/tree/main/kotlin/jimmer-cloud-kt)|例子项目，使用Kotlin和Jimmer支持跨越微服务边界的ORM远程关联|

</details>

### 3.3. Save Command ★

:::tip
Save Command，是Jimmer三个最基础的核心功能之一 *(另外两个是Fetcher和SQL DSL)*。

保存指令专为为复杂表单设计。无论表单多么复杂，本质上就是任意形状的数据结构，Jimmer让开发人员只需使用一个方法调用就可以把任意形状的数据结构写入数据库。
:::

无论用户传入的数据结构的复杂度如何，Jimmer都会从数据库查询出同等复杂度的数据结构并找出二者差异 *(这并非真正的工作机制，但是从用户视角如此理解这个功能没问题)*，
并执行`insert`、`update`和`delete`语句更新不一样的地方。`SaveCommandPanel`（官方页面含交互演示）

然而，如果允许客户端上传代表任意形状的数据结构的实体对象，那么客户端将获得不受限制的数据修改能力，这会严重破安全性。所以，必须通过DTO语言生成Input DTO，再用Input DTO作为Web Api的输入参数。即

-   动态实体作为内部机制，让保存指令能保存任意形状的数据结构，从功能层面支持无限种可能。

-   Input DTO作为安全卫士，严格限制用户的输入格式，只对外暴露有限的数据录入能力。

接下来，我们通过4个案例，展示保存指令的基本用法:

1.  保存孤单对象

    这是最简单的情况，因为孤单的对象不存在任何关联数据。
## 1. DTO代码

```sh
export yourpackage.Book
    -> yourpackage.dto

input BookInput {
    #allScalars
}
```

编译后，将会自动生成Java/Kotlin类型`BookInput`

## 2. 保存代码

**Java**

```java
@PutMapping("/book")
public int saveBook(
    @RequestBody BookInput input
) {
    return sqlClient
        .save(input)
        .getTotalAffectedRowCount();
}
```

**Kotlin**

```kotlin
@PutMapping("/book")
fun saveBook(
    @RequestBody input: Book
): Int =
    sqlClient
        .save(input)
        .totalAffectedRowCount
```

2.  保存短关联

    所谓短关联，指只改变当前对象和其他对象之间的关联关系，不进一步修改关联对象。
    对于UI界面而言，引用关联 (一对一和多对一) 表现为单选菜单；
    集合关联 (一对多和多对多) 表现为多选菜单
    。`ShortAssociation`（官方页面含交互演示）
## 1. DTO代码

```sh
export yourpackage.Book
    -> yourpackage.dto

input BookInput {
    #allScalars

    id(store) // as storeId

    id(author) as authorIds
}
```

编译后，将会自动生成Java/Kotlin类型`BookInput`

## 2. 保存代码

**Java**

```java
@PutMapping("/book")
public int saveBook(
    @RequestBody BookInput input
) {
    return sqlClient
        .save(input)
        .getTotalAffectedRowCount();
}
```

**Kotlin**

```kotlin
@PutMapping("/book")
fun saveBook(
    @RequestBody input: Book
): Int =
    sqlClient
        .save(input)
        .totalAffectedRowCount
```

3.  保存长关联

    所谓长关联，指不仅要改变当前对象和其他对象之间的关联关系，还要进一步修改关联对象。
    对于UI界面而言，形式多样，以表单内嵌子表最为常见 *(一对多)*
    ：`LongAssociation`（官方页面含交互演示）
## 1. DTO代码

```sh
export yourpackage.BookStore
    -> yourpackage.dto

input CompositeBookStoreInput {
    #allScalars

    books {
        #allScalars
        -id
    }
}
```

编译后，将会自动生成Java/Kotlin类型`BookInput`

## 2. 保存代码

**Java**

```java
@PutMapping("/store")
public int saveBookStore(
    @RequestBody CompositeBookStoreInput input
) {
    return sqlClient
        .save(input)
        .getTotalAffectedRowCount();
}
```

**Kotlin**

```kotlin
@PutMapping("/store")
fun saveBookStore(
    @RequestBody input: CompositeBookStoreInput
): Int =
    sqlClient
        .save(input)
        .totalAffectedRowCount
```

4.  递归保存树形结构

    这个例子稍有不同，需要在保存之前稍微需改一下根节点的数据。
## 1. DTO代码

```sh
export yourpackage.TreeNode
    -> yourpackage.dto

input RecursiveTreeInput {
    name

    childNodes*
}
```

编译后，将会自动生成Java/Kotlin类型`RecursiveTreeInput`

## 2. Key

`RecursiveTreeInput`代表一颗树。如上代码所示，所有节点对象都没有id属性。

:::note
很多ORM是这样设计的，在对象有id属性时执行`update`操作；在对象没有id属性时执行`insert`操作。

Jimmer并没有这样设计，采用了更智能处理方式: @Key
:::

假如原实体`TreeNode`定义如下

**Java**

```java
@Entity
public interface TreeNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    @Key
    String name();

    @Key
    @Nullable
    @ManyToOne
    TreeNode parent();

    @OneToMany(mappedBy = "parent")
    List<TreeNode> childNodes();
}
```

**Kotlin**

```kotlin
@Entity
interface TreeNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val name: String

    @Key
    @ManyToOne
    val parent: TreeNode?

    @OneToMany(mappedBy = "parent")
    val childNodes: List<TreeNode>
}
```

:::info
这里`TreeNode.name`和`TreeNode.parent`属性被`@Key`修饰。这两个属性对应于数据库中`BOOK`表的普通字段`NAME`和外键字段`PARENT_ID`。

这表示，如果`TreeNode.id`属性没有被指定 *(任何被指定了自动分配策略的`id`属性都可以缺失)*，那么，默认情况下，Jimmer将基于`NAME`和`PARENT_ID`字段查询数据库中是否存在已有数据，最终决定应该执行插入操作还是更新操作。
:::

请看一个具体的DTO对象树

```json
RecursiveTreeInput(
    name=Animal
    childNodes=[
        RecursiveTreeInput(
            name=Mammal,
            childNodes=[
                RecursiveTreeInput(
                    name=Dog
                    childNodes=[
                        RecursiveTreeInput(
                            name=Huskies
                            childNodes=[]
                        ),
                        RecursiveTreeInput(
                            name=Chihuahua,
                            childNodes=[]
                        ),
                        RecursiveTreeInput(
                            name=Bulldog
                            childNodes=[]
                        )
                    ]
                ),
                RecursiveTreeInput(
                    name=Dolphin
                    childNodes=[]
                ),
                RecursiveTreeInput(
                    name=Bat,
                    childNodes=[]
                )
            ]
        ),
        RecursiveTreeInput(
            name=Birds,
            childNodes=[]
        )
    ]
)
```

DTO类型`RecursiveTreeInput`只有向下递归的能力，所以没有映射`parent`属性；`parent`属性是`@Key`的一部分，该怎么办呢 *(如果id属性未指定，则必须指定key属性)*？

事实上，对于一对多关联 *(比如，这里的`childNodes`)* 而言，Jimmer有一个自动处理过程：一旦父对象因为被保存而有了确定的`id`值，其子对象的逆向多对一关联属性 *(比如，这里的`parent`)* 将会被自动设置。

可见，除了根对象外，其余对象的`parent`属性都会在保存过程中被自动设置。

:::info
因此，后续的保存代码需要调整用户上传的数据，为根对象指定`parent`属性。
:::

## 3. 保存代码

**Java**

```java
@PutMapping("/tree")
public int saveTree(
    @RequestBody RecursiveTreeInput input
) {
    TreeNode treeNode = TreeNodeDraft.$
        .produce(input.toEntity(), draft -> {
            // 前文已经讨论了：保存前需要为根节点
            // 设置parent属性，这里不再赘述

            draft.setParent(null);
        });
    return sqlClient
        .save(treeNode)
        .getTotalAffectedRowCount();
}
```

**Kotlin**

```kotlin
@PutMapping("/tree")
fun saveTree(
    @RequestBody input: RecursiveTreeInput
): Int
    sqlClient
        .save(
            input.toEntity().copy {
                // 前文已经讨论了：保存前需要为根节点
                // 设置parent属性，这里不再赘述

                parent = null
            }
        )
        .totalAffectedRowCount
```

<details>
<summary>参考连接</summary>

|连接|描述|
|---|---|
|[快速预览/查询任意形状](https://babyfish-ct.github.io/jimmer-doc/zh/docs/quick-view/save)|快速预览：保存任意形状|
|[jimmer-examples/tree/main/java/save-command](https://github.com/babyfish-ct/jimmer-examples/tree/main/java/save-command)|例子项目，使用Java和Jimmer展示Save Command的各种场景|
|[jimmer-examples/tree/main/kotlin/save-command-kt](https://github.com/babyfish-ct/jimmer-examples/tree/main/kotlin/save-command-kt)|例子项目，使用Kotlin和Jimmer展示Save Command的各种场景|
|[jimmer-examples/tree/main/java/jimmer-sql](https://github.com/babyfish-ct/jimmer-examples/tree/main/java/jimmer-sql)|例子项目，使用Java和Jimmer构建REST服务|
|[jimmer-examples/tree/main/kotlin/jimmer-sql-kt](https://github.com/babyfish-ct/jimmer-examples/tree/main/kotlin/jimmer-sql-kt)|例子项目，使用Kotlin和Jimmer构建REST服务|
|[jimmer-examples/tree/main/java/jimmer-sql-graphql](https://github.com/babyfish-ct/jimmer-examples/tree/main/java/jimmer-sql-graphql)|例子项目，使用Java和Jimmer构建GraphQL服务|
|[jimmer-examples/tree/main/kotlin/jimmer-sql-graphql-kt](https://github.com/babyfish-ct/jimmer-examples/tree/main/kotlin/jimmer-sql-graphql-kt)|例子项目，使用Kotlin和Jimmer构建GraphQL服务|

</details>

### 3.4. SQL DSL ★

:::tip
SQL DSL，是Jimmer三个最基础的核心功能之一 *(另外两个是Fetcher和Save Command)*。

Jimmer SQL DSL为任意复杂的动态SQL而设计。
:::

现在，整个JVM生态有几十种SQL DSL。其中，以ORM风格的[QueryDSL](http://querydsl.com/)和NativeSQL风格的[JOOQ](https://www.jooq.org/)最为有名。那么Jimmer的SQL DSL有什么特色呢？

-   Jimmer的SQL DSL天生为任意复杂的动态SQL而设计，包括两个强大的能力：动态表连接、隐式子查询。

    > 原生SQL并不利于构建复杂的动态查询，Jimmer SQL DSL的目的是对此给出相应的方案；而所有SQL DSL都能做到的强类型安全性只是附带效果。

-   非常智能的分页支持，开发人员只需要用DSL构建普通的列表查询，Jimmer自动生成分页前的总行数查询，并自动结合二者完成分页查询。

-   Jimmer的SQL DSL可以嵌入原生SQL表达式。

篇幅有限，本文只讨论上面的第一点。让我们通过三个小例子来了解Jimmer SQL DSL的基本能力:

1.  简单的动态查询

    简单的动态查询，建立初步印象。
## 定义数据查询方法

**Java**

```java title="BookRepository.java"
@Repository
public class BookRepository {

    private final JSqlClient sqlClient;

    public BookRepository(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    List<Book> findBooks(

        @Nullable String name,

        @Nullable BigDecimal minPrice,

        @Nullable BigDecimal maxPrice,
        @Nullable Fetcher<Book> fetcher
    ) {
        BookTable table = Tables.BOOK_TABLE;

        return sqlClient
            .createQuery(table)

            .where(table.name().ilikeIf(name))

            .where(table.price().betweenIf(minPrice, maxPrice))
            .select(table.fetch(fetcher))
            .execute();
    }
}
```

**Kotlin**

```kotlin title="BookRepository.kt"
@Repository
class BookRepository(
    private val sqlClient: KSqlClient
) {

    fun findBooks(

        name: String? = null,

        minPrice: BigDecimal? = null,

        maxPrice: BigDecimal? = null,
        fetcher: Fetcher<Book>? = null
    ): List<Book> =
        sqlClient
            .createQuery(Book::class) {

                where(table.name `ilike?` name)

                where(table.price.`between?`(minPrice, maxPrice))
            }
            .select(table.fetch(fetcher))
            .execute()
}
```

:::info
这里，Java的`ilikeIf`、`betweenIf`以及kotlin的`ilike?`、`between?`是动态谓词。如果参数为null或""，直接忽略相关的SQL条件。

> 对于静态谓词 *(比如，`ilike`、`between`)* 而言，任何参数为null都将被视为用户代码的BUG。
:::

## 用法1: 不指定参数

**Java**

```java
List<Book> books = bookRepository.findBooks(
    null, // name
    null, // minPrice
    null, // maxPrice
    null
);
```

**Kotlin**

```kotlin
val books = bookRepository.findBooks()
```

不会生成任何where条件，SQL如下

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK tb_1_

// No SQL predicates
```

## 用法2: 指定参数

**Java**

```java
List<Book> books = bookRepository.findBooks(
    "GraphQL", // name
    new BigDecimal(20), // minPrice
    new BigDecimal(50), // maxPrice
    null
);
```

**Kotlin**

```kotlin
val books = bookRepository.findBooks(
    name = "GraphQL",
    minPrice = BigDecimal(20),
    maxPrice = BigDecimal(50)
)
```

会生成所有where条件，SQL如下

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK tb_1_
where

        lower(tb_1_.NAME) like ? /* %graphql% */
    and

        (tb_1_.PRICE between ? /* 20 */ and ? /* 50 */)
```

2.  动态表连接

    为引用关联 *(一对一或多对一)* 属性的关联对象动态添加SQL条件。
## 定义数据查询方法

**Java**

```java title="BookRepository.java"
@Repository
public class BookRepository {

    private final JSqlClient sqlClient;

    public BookRepository(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    List<Book> findBooks(

        @Nullable String storeName,

        @Nullable String storeWebsite,
        @Nullable Fetcher<Book> fetcher
    ) {
        BookTable table = Tables.BOOK_TABLE;

        return sqlClient
            .createQuery(table)

            .where(table.store().name().ilikeIf(storeName))

            .where(table.store().website().ilikeIf(storeWebsite))
            .select(table.fetch(fetcher))
            .execute();
    }
}
```

**Kotlin**

```kotlin title="BookRepository.kt"
@Repository
class BookRepository(
    private val sqlClient: KSqlClient
) {

    fun findBooks(

        storeName: String? = null,

        storeWebsite: String? = null,
        fetcher: Fetcher<Book>? = null
    ): List<Book> =
        sqlClient
            .createQuery(Book::class) {

                where(table.store.name `ilike?` storeName)

                where(table.store.name `ilike?` storeWebsite)
            }
            .select(table.fetch(fetcher))
            .execute()
}
```

:::info
其中，Java的`table.store()`和Kotlin的`table.store`表示表连接
:::

## 用法1: 不指定参数

**Java**

```java
List<Book> books = bookRepository.findBooks(
    null, // storeName
    null, // storeWebsite
    null
);
```

**Kotlin**

```kotlin
val books = bookRepository.findBooks()
```

:::info
所有SQL条件都没有生效，所有的表连接并未被真正使用，全部被忽略。

> 注意：Jimmer没有`JPA`中的`join fetch`概念，join的唯一目的是为了被其他SQL表达式引用，所以未被真正使用的join对象会被忽略。
:::

生成的SQL如下

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK tb_1_

// No SQL table joins
```

## 用法2: 指定参数

如果参数`storeName`和`storeWebsite`都非null

**Java**

```java
List<Book> books = bookRepository.findBooks(
    "M", // storeName
    ".com", // storeWebsite
    null
);
```

**Kotlin**

```kotlin
val books = bookRepository.findBooks(
    storeName = "M",
    storeWebsite = ".com"
)
```

:::info
所有冲突表连接将会被自动合并，最终在生成的SQL中只有一个JOIN，不会重复JOIN。
:::

生成的SQL如下

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK tb_1_

/* Multiple conflicting table joins are merged into one */
inner join BOOK_STORE tb_2_
    on tb_1_.STORE_ID = tb_2_.ID

where
        lower(tb_2_.NAME) like ? /* %m% */
    and
        lower(tb_2_.WEBSITE) like ? /* %.com% */
```

3.  隐式子查询

    Jimmer支持功能更完整的普通子查询。但是，一部分因实体关联紧密相关的子查询可以写成这种更简单的隐式子查询。

    为集合关联 *(一对多或多对多)* 属性的关联对象动态添加SQL条件。
## 定义数据查询方法

**Java**

```java title="BookRepository.java"
@Repository
public class BookRepository {

    private final JSqlClient sqlClient;

    public BookRepository(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    List<Book> findBooks(

        @Nullable String authorName,

        @Nullable Gender authorGender,
        @Nullable Fetcher<Book> fetcher
    ) {
        BookTable table = Tables.BOOK_TABLE;

        return sqlClient
            .createQuery(table)
            .where(

                table.authors(author ->
                    Predicate.or(
                        author.firstName().ilikeIf(authorName),
                        author.lastName().ilikeIf(authorName)
                    )
                )
            )
            .where(

                table.authors(author ->
                    author.gender().eqIf(authorGender)
                )
            )
            .select(table.fetch(fetcher))
            .execute();
    }
}
```

**Kotlin**

```kotlin title="BookRepository.kt"
@Repository
class BookRepository(
    private val sqlClient: KSqlClient
) {

    fun findBooks(

        authorName: String? = null,

        authorGender: String? = null,
        fetcher: Fetcher<Book>? = null
    ): List<Book> =
        sqlClient
            .createQuery(Book::class) {

                where += table.authors {
                    or(
                        firstName `ilike?` authorName,
                        lastName `ilike?` authorName
                    )
                }

                where += table.authors {
                    gender `eq?` authorGender
                }
            }
            .select(table.fetch(fetcher))
            .execute()
}
```

:::info
其中，`table.authors`表示隐式子查询，在其lambda中给出关联对象的过滤条件即可。
:::

## 用法1: 不指定参数

**Java**

```java
List<Book> books = bookRepository.findBooks(
    null, // authorName
    null, // authorGender
    null
);
```

**Kotlin**

```kotlin
val books = bookRepository.findBooks()
```

:::info
两个子查询都失效，它们都会被忽略
:::

最终生成的SQL不包含任何子查询

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK tb_1_

// No SQL sub queries
```

## 用法2: 指定参数

**Java**

```java
List<Book> books = bookRepository.findBooks(
    "A", // authorName
    Gender.MALE, // authorGender
    null
);
```

**Kotlin**

```kotlin
val books = bookRepository.findBooks(
    authorName = "A",
    authorGender = Gender.MALE
)
```

:::info
两个子查询都生效且彼此冲突，会被被合并为一个子查询
:::

最终生成的SQL只有一个子查询

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK tb_1_
where

    /* Multiple conflicting implicit subqueries are merged into one */
    exists(

        select
            1
        from AUTHOR tb_2_
        inner join BOOK_AUTHOR_MAPPING tb_3_
            on tb_2_.ID = tb_3_.AUTHOR_ID
        where

                /* Parent-child query join condition implied by the implicit subquery */
                tb_3_.BOOK_ID = tb_1_.ID

            and
                (
                    lower(tb_2_.FIRST_NAME) like ? /* %a% */
                or
                    lower(tb_2_.LAST_NAME) like ? /* %a% */
                )
            and
                tb_2_.GENDER = ? /* M */
    )
```

:::tip
事实上，用户还可以利用DTO语言的编写specification DTO，让Jimmer自动生成查询条件参数，以及你在上面看到的所有动态查询行为。

这个更为便捷的功能叫`超级QBE`。这里，我们采用这个功能来替代上面介绍过的所有功能。
## 1. DTO代码

```sh title="Book.dto"
export com.yourcompany.yourproject.model.Book
    -> package com.yourcompany.yourproject.model.dto

// Specification DTO, 既非Output DTO也非Input DTO

specification BookSpecification {
    like/i(name)
    ge(price) // Default alias: minPrice
    le(price) // Default alias: maxPrice
    flat(store) {
        as(^ -> store) {
            like/i(name)
            like/i(website)
        }
    }
    flat(authors) {
        like/i(firstName, lastName) as authorName
        gender as authorGender
    }
}
...Omit other DTO definitions...
```

编译后生成Java/Kotlin类型`BookSpecification`

**Java**

```java title="BookSpecification.java"
// DTO语言编译自动生成，并非开发人员提供

@GeneratedBy(file = "<yourproject>/src/main/dto/Book.dto")
public class BookSpecification
implements JSpecification<Book, BookTable> {

    @Nullable
    private String name;

    @Nullable
    private BigDecimal minPrice;

    @Nullable
    private BigDecimal maxPrice;

    @Nullable
    private String storeName;

    @Nullable
    private String storeWebsite;

    @Nullable
    private String authorName;

    @Nullable
    private Gender authorGender;

    @Override
    public void applyTo(SpecificationArgs<Book, BookTable> args) {
        ...Omit complex dynamic query logic...
    }

    ...Omit other members...
}
```

**Kotlin**

```kotlin title="BookSpecification.kt"
@GeneratedBy(file = "<yourproject>/src/main/dto/Book.dto")
data class BookSpecification(
    val name: String? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val storeName: String? = null,
    val storeWebsite: String? = null,
    val authorName: String? = null,
    val authorGender: Gender? = null
) : KSpecification<Book> {

    ...Omit other members...
}
```

## 2. 查询代码

**Java**

```java
@GetMapping("/books")
public List<Book> findBooks(
    BookSpecification specification,
) {
    BookTable table = BookTable.$;
    return sqlClient
        .createQuery(table)

        .where(specification)
        .select(table)
        .execute();
}
```

**Kotlin**

```kotlin
@GetMapping("/books")
fun findBooks(
    specification: BookSpecification
): List<Book> =
    sqlClient
        .createQuery(Book::class) {
            .createQuery(table)

            .where(specification)
            .select(table)
        }
        .execute()
```

:::

<details>
<summary>参考链接</summary>

|连接|描述|
|---|---|
|[快速预览/查询任意形状](https://babyfish-ct.github.io/jimmer-doc/zh/docs/quick-view/dsl)|快速预览：任意动态查询|
|[动态表连接](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/dynamic-join)||
|[隐式子查询](../query/implicit-subquery.md)||
|[智能分页](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/paging)|本文未展示|
|[Native表达式](../query/native-sql.md)|本文未展示，如何在强类型SQL DSL中插入原生SQL片段|
|[动态排序](../query/dynamic-order.md)|本文未展示|
|[超级QBE](../query/super_qbe.md)||

这里，并未给出相关例子的链接，因为所有例子都在使用SQL DSL。

</details>

### 3.5 Trigger

这个功能类似于数据库的后置触发器，在数据库变更发生后通知应用程序。

Trigger不但能通知开发人员哪些实体对象发生了什么变化，还对变化事件的信息做了ORM映射，以通知开发人员哪些实体关联发生了变化。

Trigger为另外一个功能：缓存，打下了坚实的基础。

Jimmer支持两种各不同类型的触发器: BinLog触发器和Transaction触发器

<table>
<thead>
<tr>
<th></th>
<th>BinLog触发器</th>
<th>Transaction触发器(默认关闭)</th>
</tr>
</thead>
<tbody>
<tr>
<td>工作原理</td>
<td>通过整合业内成熟的CDC方案<i>(比如：maxwell，debezium)</i>发现数据的变化</td>
<td>靠Jimmer自身的能力发现数据库的变化</td>
</tr>
<tr>
<td>通知时机</td>
<td>事务提交后</td>
<td>事务提交前</td>
</tr>
<tr>
<td>优点</td>
<td>能感知因任何原因导致的数据库变化，包括绕过系统的数据库变化</td>
<td>原变更和触发器导致的新变更要么都成功要么都失败</td>
</tr>
<tr>
<td rowspan="3">缺点</td>
<td  rowspan="3">事务提交后，CDC服务推送存在轻微延迟</td>
<td>只能感知当前JVM进程自己通过Jimmer对数据库的修改导致的变更，对其他任何原因导致的数据库变更无能为力</td>
</tr>
<tr>
<td>当前事务的持续时间会被拖长，相关资源的的解锁操作也会被滞后</td>
</tr>
<tr>
<td>会导致Jimmer对数据库的修改行为内部需要更多的额外查询</td>
</tr>
<tr>
<td>适用场景</td>
<td>绝大部分场景，包含但不限于：缓存同步，异构系统数据同步</td>
<td>必须和主业务参与同一个事务的附加行为</td>
</tr>
</tbody>
</table>

参考链接：[触发器](../mutation/trigger.md)

### 3.6. Cache

Jimmer的缓存功能非常强大，具备以下3个特点：

-   支持多级缓存，且每一级的缓存都允许用户选择自己喜欢的技术。

    > 就附带的例子而言，一级缓存为[Caffeine](https://github.com/ben-manes/caffeine); 二级缓存为[Redis](https://redis.io/)

-   显著增加缓存的适用范围。

    不再局限于最常见的`id->为对象`缓存，即`ObjectCache`（官方页面含交互演示），还支持以下三种缓存

    -   `id->关联id`缓存，即 `AssociationCache`（官方页面含交互演示）

        关联属性缓存能显著提高按照关联在不同对象之间导航的性能，对Jimmer中查询任意复杂的数据结构这个核心功能很有帮助。

    -   `id->计算结果`缓存，即 `CalculatedCache`（官方页面含交互演示）

        计算属性缓存为[复杂计算属性](../mapping/advanced/calculated/transient.md)而设计，避免多次查询重复计算，
        这同样对Jimmer中查询任意复杂的数据结构这个核心功能很有帮助。

    -   还可以和权限系统相结合，既然不同身份的用户看到的数据库数据是不同的，那么他们看到的缓存数据也应该不同，
        即 `MultiViewCache`（官方页面含交互演示）

        > 为不同身份的用户存储不同的数据非常耗费内存，绝对不能用于JVM内部的缓存，对于Redis这类外部缓存，也要谨慎使用，仅对非常重要的数据才开启。

    :::info
    Q: 为什么长久以来，业务系统的缓存主要局限于对象缓存，缺乏多样性？

    A: 在底层框架没有为缓存一致性实现高度自动化的前提下，利用业务代码维护其他类型的缓存的成本过于高昂。
    :::

-   高度自动化的缓存一致性。

    Jimmer为缓存一致性提供了高度的自动化支持，只需启用了Jimmer Trigger即可 *(无论是BinLog触发器还是Transaction触发器)*。

    -   对`对象缓存`和`关联属性缓存`而言，其一致性维护是全自动的，无需开发人员进行任何干预。

    -   对`计算属性缓存`和`多视图缓存`而言，需要开发人员的帮助，但开发需要做的事非常简单。

    `CacheConsistencyPanel`（官方页面含交互演示）

参考链接: [缓存篇](https://babyfish-ct.github.io/jimmer-doc/zh/docs/cache)

### 3.7. Global Filter

全局过滤器，在SQL DSL体系之外以插件的方式为实体添加固有的SQL过滤条件。最常见的适用场景为基于行的权限管理。

全局过滤器非常灵活，很容易和任何IOC框架*(比如Spring)*结合，从而自由地访问业务上下文的信息，从而实现和业务高度紧密结合的过滤逻辑。

参考链接：[全局过滤器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/global-filter)

### 3.8. Draft Interceptor

Draft Interceptor类似于数据库的前置触发器，在数据库变更发生前，给予用户最后一次机会调整即将保存的数据。

Draft Interceptor非常灵活，很容易和任何IOC框架*(比如Spring)*结合，从而自由地访问业务上下文的信息，从而实现和业务高度紧密结合的数据调整逻辑。

参考链接：[拦截器](../mutation/draft-interceptor.md)

### 3.9. Logical Deletion

所谓逻辑删除，指业务层面的数据删除操作并不会导致其从数据库中被真正删除，只是修改数据的一个字段，将其标注为“已删除”即可。

逻辑删除也提供一个内置的Global Filter，该过滤器会导致被标注为“已删除”得数据不会被任何查询获得，也不会被任何修改语句影响。

Jimmer的逻辑删除，既支持实体表，也支持中间关联表。

参考链接：[实体表的逻辑删除](../mapping/advanced/logical-deleted/entity.md)，[中间表的逻辑删除](../mapping/advanced/logical-deleted/join-table.md)

### 3.10. Remote Associations

Jimmer实体支持[远程关联](../spring/spring-cloud.md)，即，跨越微服务边界的ORM关联。

当查询特定形状复杂的数据结构时，如果数据结构的形状跨越了多个微服务，那么Jimmer会自动从不同的微服务查询数据并组装成一个整体返回给开发人员。

:::note
Remote Associations只是在有限时间内为微服务体系开发的首个功能，目的在于向开发人员证明ORM技术体系在微服务技术体系下依旧可以很强大。

等Jimmer逐渐完善发布1.0正式版本后，将会在微服务技术体系添加更多的功能，为该体系下更多的繁琐的细节进行抽象和简化。
:::

参考链接：[远程关联映射](../mapping/advanced/remote.md)，[远程关联支持](../spring/spring-cloud.md)

## 4. 生态

接下来介绍Jimmer的第三方生态，这些作品全部由Jimmer用户贡献，在此表达诚挚的谢意

-   DTO语言Intellij插件

    https://github.com/ClearPlume/jimmer-dto

-   实体模型生成工具

    以下工具专注于根据数据库生成实体模型

    |项目类型|语言支持|项目地址|
    |---|---|---|
    |Intellij插件|Java&Kotlin|https://github.com/ClearPlume/jimmer-generator|
    |Intellij插件|Java&Kotlin|https://github.com/huyaro/CodeGenX|
    |Maven插件|Java|https://github.com/TokgoRonin/code-generator-jimmer|
    |Gradle插件|Java&Kotlin|https://github.com/Enaium/jimmer-gradle|
    |模型设计器|Java&Kotlin|https://pot-mot.github.io/jimmer-code-gen-doc|

-   Quarkus扩展

    https://github.com/flynndi/quarkus-jimmer-extension

-   Solon扩展

    https://github.com/zengyufei/xm-jimmer-solon-plugin

-   Gradle插件

    https://github.com/ArgonarioD/gradle-plugin-jimmer

    https://github.com/Enaium/jimmer-gradle

## 5. 注意事项

使用Jimmer开发时，需要留意一个注意事项，请参考[这里](./apt-ksp.md#注意事项)
