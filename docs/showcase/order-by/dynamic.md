---
title: 5.2 动态排序
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/order-by/dynamic
---

# 5.2 动态排序

所谓动态排序，指排序方式由客户端动态指定。

## 直接使用字符串

客户端指定排序的方式最简单的方法是字符串。

Jimmer支持的字符串格式如下

`store.name asc, name asc, edition desc`
或
`store.name asc; name asc; edition desc`

其中`store.name`表示先通过`Book.store`属性inner join到关联对象`BookStore`再按照关联对象的`name`属性排序。连接路径长度无限，但需要沿途所有属性的关联类型都是一对一或多对一。

**Java**

```java
String sortCode = ...略...;

BookTable table = BookTable.$;
List<Book> books = sqlClient
    .createQuery(table)

    .orderBy(Order.makeOrders(table, sortCode))
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val sortCode: String = ...略...

val books = sqlClient
    .createQuery(Book::class) {

        orderBy(table.makeOrders(sortCode))
        select(table)
    }
    .execute()
```

## 通过Spring Sort中转

有时，和Spring Data结合开发，可能使用`org.springframework.data.domain.Sort`表示动态排序。

这时，开发人员需要执行两个步骤

### I. 字符串转Sort对象

客户端最容易上传的的还是之前所讨论的形如
`store.name asc, name asc, edition desc`
或
`store.name asc; name asc; edition desc`
的字符串。因此，首先需要把字符串转化为`org.springframework.data.domain.Sort`对象。

导入依赖`org.babyfish.jimmer:jimmer-spring-boot-starter:$version`后，
然后就可以使用静态方法`org.babyfish.jimmer.spring.model.SortUtils.toSort`，如下

**Java**

```java
String sortCode = ...略...;
Sort sort = SortUtils.toSort(sortCode);
```

**Kotlin**

```kotlin
val sortCode: String = ...略...
val sort = SortUtils.toSort(sortCode)
```

### II. 利用Sort对象排序

导入依赖`org.babyfish.jimmer:jimmer-spring-boot-starter:$version`后

-   Java：采用工具方法`org.babyfish.jimmer.spring.repository.SpringOrders.toOrders`将`org.springframework.data.domain.Sort`对象转化为Jimmer接受的排序对象集合。

-   Kotlin：直接使用接受`org.springframework.data.domain.Sort`对象的`orderBy`扩展函数即可。

**Java**

```java
org.springframework.data.domain.Sort sort = ...略...;

BookTable table = BookTable.$;
List<Book> books = sqlClient
    .createQuery(table)

    .orderBy(SpringOrders.toOrders(table, sort))
    .select(table)
    .execute();
```

**Kotlin**

```kotlin
val sort: org.springframework.data.domain.Sort = ...略...

val books = sqlClient
    .createQuery(Book::class) {

        orderBy(sort)
        select(table)
    }
    .execute()
```
