# 动态性

> 来源: https://jimmer.deno.dev/zh/docs/object/dynamic

* [对象篇](/zh/docs/object/)
* 动态性

本页总览

# 动态性

信息

本文的示例代码使用了一个名为`BookDraft`的类型，该接口是Jimmer根据用户定义的类型`Book`自动生成的接口类型。

读者可先行忽略这个自动生成的接口，后续文档[Draft](/zh/docs/object/draft)会对其做出介绍

## 基本概念[​](#基本概念 "基本概念的直接链接")

## 1. 较少属性[​](#1-较少属性 "1. 较少属性的直接链接")

* Java
* Kotlin

```
Book book = Immutables.createBook(draft -> {  
    draft.setName("Learning GraphQL");  
});
```

```
val book = Book {  
    name = "Learning GraphQL"  
}
```

这时，`book`对象对应的JSON如下

```
{"name": "Learning GraphQL"}
```

## 2. 较多属性[​](#2-较多属性 "2. 较多属性的直接链接")

* Java
* Kotlin

```
Book book = Immutables.createBook(draft -> {  
    draft.setName("Learning GraphQL");  
    draft.setEdition(1);  
    draft.setPrice(new BigDecimal("49.99"));  
});
```

```
val book = Book {  
    name = "Learning GraphQL"  
    edition = 1  
    price = BigDecimal("49.99")  
}
```

这时，`book`对象对应的JSON如下

```
{  
    "name": "Learning GraphQL",  
    "edition": 1,  
    "price": 49.99  
}
```

## 3. 包含关联[​](#3-包含关联 "3. 包含关联的直接链接")

* Java
* Kotlin

```
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

```
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

```
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

## 4. 更多关联 (更大广度)[​](#4-更多关联-更大广度 "4. 更多关联 (更大广度)的直接链接")

* Java
* Kotlin

```
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

```
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

```
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

### 5. 更深关联 (更大深度)[​](#5-更深关联-更大深度 "5. 更深关联 (更大深度)的直接链接")

和前面的例子不同，这里，我们选择用`BookStore`作为数据结构的聚合根，而非`Book`。

* Java
* Kotlin

```
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

```
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

```
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

## 动态对象的特性[​](#动态对象的特性 "动态对象的特性的直接链接")

动态对象可能缺失任何属性，也可以换一种说法，动态对象并不要求其所有属性都被设置。

警告

在动态对象中，属性缺失和属性被设置为null是完全不同的两回事。

* 属性缺失: 对象该属性的值**未知**，当前业务对其不感兴趣
* 属性被设置为null: 对象该属性的值**已知**，真的是啥都没有

而在静态的POJO中，二者其实是无法区分的。更糟的是，开发人员常借助于Java语言不支持null safety的特性，有意或无意地混淆二者。

动态对象这个概念非常重要，是理解Jimmer的关键！

对于缺失的属性而言

* 使用代码直接访问，将会得到异常`org.babyfish.jimmer.UnloadedException`

  备注

  如果读者有Hibernate经历，可以将其理解为`org.hibernate.LazyInitializationException`
* 在[Jackson](https://github.com/FasterXML/jackson)序列化中，将会被自动忽略。

  信息

  这要求对[Jackson](https://github.com/FasterXML/jackson)做一点配置。由于这个细节非常重要，已经被独立成一篇文档，请参阅[和Jackson协同](/zh/docs/object/jackson)

## 和ORM的互动[​](#和orm的互动 "和ORM的互动的直接链接")

动态性是Jimmer对象的内禀特征，具有普适性，ORM框架本身和开发人员双方都可以轻松构建动态对象给对方使用。

* Jimmer创建动态对象，返回给开发人员

  即，查询任意形状的数据结构。此功能叫[对象抓取器](/zh/docs/query/object-fetcher)
* 开发人员创建动态对象，传递给Jimmer

  即，保存任意形状的数据结构。此功能叫[保存指令](/zh/docs/mutation/save-command)

### 对象抓取器[​](#对象抓取器 "对象抓取器的直接链接")

Jimmer创建动态对象，返回给开发人员

* Java
* Kotlin

信息

和GraphQL比较

* GraphQL基于HTTP服务，该功能只有在跨越HTTP服务的边界才能呈现；而在Jimmer中，这是ORM的基础API，你可以在任何代码逻辑中使用此能力。
* 截止到目前为止，GraphQL协议不支持对深度无限的自关联属性的递归查询；而Jimmer支持。

### 保存指令[​](#保存指令 "保存指令的直接链接")

开发人员创建动态对象，传递给Jimmer

保存指令允许开发人员保存任意形状的数据结构，而非保存简单的对象。

在默认情况下，即在*AssociatedSaveMode*为**REPLACE**情况下，Jimmer会用被保存结构去全量替换数据库中已有的数据结构，如图所示：

![](/zh/assets/images/save-d37c62a7516ccbf2bc561f935cf77de2.webp)

* **右上角:** 用户传入一个任意形状的数据结构，让Jimmer写入数据库。
* **左上角:** 从数据库中查询已有的数据结构，用于和用户传入的新数据结构对比。

  用户传入什么形状的数据结构，就从数据查询什么形状的数据结构，新旧数据结构的形状完全一致。所以，查询成本和对比成本由用户传入的数据结构的复杂度决定。
* **下方:** 对比新旧数据结构，找出DIFF并执行相应的SQL操作，让新旧数据一致：

  + 橙色部分：对于在新旧数据结构中存在的实体对象，如果某些标量属性发生变化，修改数据
  + 蓝色部分：对于在新旧数据结构中存在的实体对象，如果某些关联发生变化，修改关联
  + 绿色部分：对于在新数据结构中存在但在旧数据结构中不存在实体对象，插入数据并建  立关联
  + 红色部分：对于在旧数据结构中存在但在新数据结构中不存在实体对象，对此对象进行脱钩，清除关联并有可能删除数据

提示

和其他ORM不同，Jimmer无需在实体模型上描述数据如何保存

* 某些标量属性是否需要被保存

  以JPA为例，通过[Column.insertable](https://docs.oracle.com/javaee/7/api/javax/persistence/Column.html#insertable--)和[Column.updatable](https://docs.oracle.com/javaee/7/api/javax/persistence/Column.html#updatable--)控制。
* 某些关联属性是否需要被保存

  以JPA为例，通过[OneToOne.cascade](https://docs.oracle.com/javaee/7/api/javax/persistence/OneToOne.html#cascade--)、[ManyToOne.cascade](https://docs.oracle.com/javaee/7/api/javax/persistence/ManyToOne.html#cascade--)、[OenToMany.cascade](https://docs.oracle.com/javaee/7/api/javax/persistence/OneToMany.html#cascade--)和[ManyToOne.cascade](https://docs.oracle.com/javaee/7/api/javax/persistence/ManyToMany.html#cascade--)控制。

Jimmer采用完全不同的策略，其实体对象并非POJO，可以灵活地控制数据结构的形状。

即，实体对象具备动态性，不为实体对象指定某个属性和将实体的某个属性指定为null，是完全不同的事情。

**对于任何一个实体对象而言，Jimmer只会保存被指定的属性，而忽略未指定的属性。**

因此，Jimmer无需在实体建模时考虑数据的保存行为，而是在运行时通过被保存的数据结构自身来描述期望的行为，具备绝对的灵活性。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/object/dynamic.mdx)

最后于 **2025年9月16日** 更新