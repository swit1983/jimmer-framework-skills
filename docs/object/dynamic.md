---
title: 动态性
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/object/dynamic
---

# 动态性

:::info
本文的示例代码使用了一个名为`BookDraft`的类型，该接口是Jimmer根据用户定义的类型`Book`自动生成的接口类型。

读者可先行忽略这个自动生成的接口，后续文档[Draft](./draft.md)会对其做出介绍
:::

## 基本概念

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

## 动态对象的特性

动态对象可能缺失任何属性，也可以换一种说法，动态对象并不要求其所有属性都被设置。

:::caution
在动态对象中，属性缺失和属性被设置为null是完全不同的两回事。

-   属性缺失: 对象该属性的值**未知**，当前业务对其不感兴趣

-   属性被设置为null: 对象该属性的值**已知**，真的是啥都没有

而在静态的POJO中，二者其实是无法区分的。更糟的是，开发人员常借助于Java语言不支持null safety的特性，有意或无意地混淆二者。

动态对象这个概念非常重要，是理解Jimmer的关键！
:::

对于缺失的属性而言

-   使用代码直接访问，将会得到异常`org.babyfish.jimmer.UnloadedException`

    :::note
    如果读者有Hibernate经历，可以将其理解为`org.hibernate.LazyInitializationException`
    :::

-   在[Jackson](https://github.com/FasterXML/jackson)序列化中，将会被自动忽略。

    :::info
    这要求对[Jackson](https://github.com/FasterXML/jackson)做一点配置。由于这个细节非常重要，已经被独立成一篇文档，请参阅[和Jackson协同](./jackson.md)
    :::

## 和ORM的互动

动态性是Jimmer对象的内禀特征，具有普适性，ORM框架本身和开发人员双方都可以轻松构建动态对象给对方使用。

-   Jimmer创建动态对象，返回给开发人员

    即，查询任意形状的数据结构。此功能叫[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)

-   开发人员创建动态对象，传递给Jimmer

    即，保存任意形状的数据结构。此功能叫[保存指令](https://babyfish-ct.github.io/jimmer-doc/zh/docs/mutation/save-command)

### 对象抓取器

Jimmer创建动态对象，返回给开发人员

> 📊 此处官方页面包含交互式图示/演示：`ObjectFetcherPanel`。
> 可访问 https://babyfish-ct.github.io/jimmer-doc/zh/docs/object/dynamic 查看。

### 保存指令

开发人员创建动态对象，传递给Jimmer

> 📊 此处官方页面包含交互式图示/演示：`SaveCommandPanel`。
> 可访问 https://babyfish-ct.github.io/jimmer-doc/zh/docs/object/dynamic 查看。
