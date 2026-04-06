# 动态排序

> 来源: https://jimmer.deno.dev/zh/docs/query/dynamic-order

* [查询篇](/zh/docs/query/)
* 动态排序

本页总览

# 动态排序

## 静态排序[​](#静态排序 "静态排序的直接链接")

首先，让我们来看看静态排序的用法，认识Jimmer的排序的概念

* Java
* Kotlin

```
public List<Book> findBooks() {  
  
    BookTable table = Tables.BOOK_TABLE;  
  
    return sqlClient  
        .createQuery(table)  
        .orderBy(table.name())  
        .orderBy(table.edition().desc())  
        .orderBy(table.score().desc().nullsLast())  
        .select(table)  
        .execute();  
}
```

```
fun findBooks(): List<Book> =  
    sqlClient  
        .createQuery(Book::class) {  
            orderBy(table.name)  
            orderBy(table.edition.desc())  
            orderBy(table.score.desc().nullsLast())  
            select(table)  
        }  
        .execute()
```

警告

`nullsFirst/nullsLast`需要数据库支持，比如，Oracle。

对于更多不支持此功能的数据库，请使用[常见表达式](/zh/docs/query/expression)中的`case`表达式。

## 动态排序[​](#动态排序 "动态排序的直接链接")

动态排序有两种用法

* orderByIf
* 客户端指定的排序

### orderByIf[​](#orderbyif "orderByIf的直接链接")

`orderByIf`的用法和`whereIf`的用法类似。

信息

* 和where不同，多个orderBy对先后顺序非常敏感，因此`orderByIf`并不如`whereIf`那样实用。

  尽管如此，Jimmer仍然支持`orderByIf`，毕竟这是一种最简单最基础的用法。
* `orderByIf`其实是动态排序的Java DSL的API，Java DSL使用链式编程风格，为了不打断链式代码的书写，提供`orderByIf`。

  Kotlin DSL使用lambda书写查询，本身就可以混入任意复杂的逻辑，所以，kotlin无需提供`orderByIf`。

假设`OrderMode`是一个枚举，具有取值`NAME`、`PRICE`，则可以按照下面的例子使用`orderByIf`

* Java
* Kotlin

```
public List<Book> findBooks(OrderMode orderMode) {  
  
    BookTable table = Tables.BOOK_TABLE;  
  
    return sqlClient  
        .createQuery(table)  
        .orderByIf(mode == OrderMode.NAME, table.name())  
        .orderByIf(mode == OrderMode.PRICE, table.price())  
        .select(table)  
        .execute();  
}
```

```
fun findBooks(orderMode: OrderMode): List<Book> =  
    sqlClient  
        .createQuery(Book::class) {  
            when (orderMode) {  
                OrderMode.NAME -> orderBy(table.name)  
                OrderMode.PRICE -> orderBy(table.price)  
            }  
            select(table)  
        }  
        .execute()
```

### 客户端指定排序[​](#客户端指定排序 "客户端指定排序的直接链接")

很多时候，前端UI允许用户通过操作表格组件来实现动态排序。即，排序的决定权在于客户端，服务端被动地接受参数，按客户端的排序要求执行查询。

客户端通过传递字符串参数可以指定动态排序，可以通过函数`makeOrders`把字符串转化为Jimmer排序需要的`List<Order>`。

`makeOrders`定义如下

* Java
* Kotlin

```
public class Order {  
  
    public static List<Order> makeOrders(Props table, String ... codes) {  
        ...省略实现...  
    }  
  
    ...省略其他代码...  
}
```

```
fun KProps<*>.makeOrders(vararg codes: String): List<Order> =  
    ...省略实现...
```

其中，第一个参数`table`为SQL DSL中的主表。

`makeOrders`的使用方式非常灵活，例如

* 用多个参数实现多列排序

  + Java
  + Kotlin

  ```
  Order.makeOrders(table, "name", "edition desc")
  ```

  ```
  table.makeOrders("name", "edition desc")
  ```
* 可以把多个参数合并成一个，并在字符串内部用`,`或`;`做分割

  + Java
  + Kotlin

  ```
  Order.makeOrders(table, "name, edition desc")
  ```

  ```
  table.makeOrders("name, edition desc")
  ```

  信息

  实际项目中，大部分情况下都属于这种单参用法，因为只提供一个HTTP参数是最简单的。
* 甚至支持按照引用关联 *(一对一、多对一)* 进行连接

  + Java
  + Kotlin

  ```
  Order.makeOrders(table, "store.city.name; store.name; name")
  ```

  ```
  table.makeOrders("store.city.name; store.name; name")
  ```

  信息

  [动态连接](/zh/docs/query/dynamic-join)中所有特性对这种隐含连接有效

`Order.makeOrders`的使用方式如下

* Java
* Kotlin

```
public List<Book> findBooks(String sort) {  
  
    BookTable table = Tables.BOOK_TABLE;  
  
    return sqlClient  
        .createQuery(table)  
        .orderBy(Order.makeOrders(table, sort))  
        .select(table)  
        .execute();  
}
```

```
fun findBooks(sort: String): List<Book> =  
    sqlClient  
        .createQuery(Book::class) {  
            orderBy(table.makeOrders(sort))  
            select(table)  
        }  
        .execute()
```

如果调用`findBooks("store.name asc, name asc")`，则生成如下SQL

```
select  
    tb_1_.ID,  
    tb_1_.NAME,  
    tb_1_.EDITION,  
    tb_1_.PRICE,  
    tb_1_.STORE_ID  
from BOOK tb_1_  
inner join BOOK_STORE tb_2_  
    on tb_1_.STORE_ID = tb_2_.ID  
order by  
    tb_2_.NAME asc,  
    tb_1_.NAME desc
```

由此可见，如果排序中包含隐含连接，Jimmer一样会构建动态连接。

### 处理SpringData的Sort[​](#处理springdata的sort "处理SpringData的Sort的直接链接")

在上面的例子中，我们直接把客户端传递的排序字符串转化为Jimmer SQL AST的排序。

然而，和SpringData协作时，可能需要处理[org.springframework.data.domain.Sort](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Sort.html)类型。

我们可以如此编写查询。这次，参数不再是字符串，而是Spring Data的Sort类。

* Java
* Kotlin

```
public List<Book> findBooks(Sort sort) {  
  
    BookTable table = Tables.BOOK_TABLE;  
  
    return sqlClient  
        .createQuery(table)  
        .orderBy(table, SpringOrders.toOrders(sort))  
        .select(table)  
        .execute();  
}
```

```
fun findBooks(sort: Sort): List<Book> =  
    sqlClient  
        .createQuery(Book::class) {  
            orderBy(sort)  
            select(table)  
        }  
        .execute()
```

这段代码解释如下：

* Java:

  Jimmer的Java API提供了一个工具类，`org.babyfish.jimmer.spring.repository.SpringOrders`，其静态方法`toOrders`把spring-data的`Sort`对象转化成Jimmer SQL DSL中的`Order`对象数组。

  `SpringOrders.toOrders`具备两个参数

  + `table`: SQL DSL中的主表
  + `sort`：外部传递的spring-data之`Sort`对象

  `SpringOrders.toOrders`把Spring Data的`Sort`对象转化为Jimmer SQL DSL的`Order`对象数组后，就可以用Jimmer查询对象的`orderBy`实现排序。
* Kotlin

  Jimmer的Kotlin API扩展了Jimmer查询对象，可以直接按照Spring Data的`Sort`对象排序。

至此，我们已经示范了如何把Spring Data的`Sort`对象转化为Jimmer中的排序操作。

为了进一步简化用户代码，Jimmer还提供了工具类`org.babyfish.jimmer.spring.model.SortUtils`, 其静态方法`toSort`可以把客户端传递的排序字符串转化为spring-data的`Sort`对象。比如

* Java
* Kotlin

```
Sort sort = SortUtils.toSort("name asc, edition desc");
```

```
var sort = SortUtils.toSort("name asc, edition desc")
```

即

```
+------------------------+  
| 客户端构建的动态排序字符串 |  
+-----------+------------+  
            |  
    SortUtils.toSort  
            |  
           \|/  
+------------------------+  
|  Spring-Data的Sort对象  |  
+-----------+------------+  
            |  
Java: 先调SpringOrders.toOrders，再orderBy  
Kotlin: 直接用sort对象进行orderBy操作  
            |  
           \|/  
+------------------------+  
|  Jimmer查询AST中的排序   |  
+-----------+------------+
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/query/dynamic-order.mdx)

最后于 **2025年9月16日** 更新