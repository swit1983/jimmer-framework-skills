# 1. 基本查询

> 来源: https://jimmer.deno.dev/zh/docs/showcase/base

* [案例展示 ★](/zh/docs/showcase/)
* 1. 基本查询

本页总览

# 1. 基本查询

Jimmer中有两个概念

* 查询多列
* 查询一个对象列，并指定被查询对象的多个属性

它们是完全不同的概念

## 查询多列[​](#查询多列 "查询多列的直接链接")

* Java
* Kotlin

```
BookTable table = BookTable.$;  
  
List<Tuple3<Long, String, Integer>> tuples = sqlClient  
    .createQuery(table)  
    .where(table.name().eq("GraphQL in Action"))  
    .orderBy(table.edition().desc())  
    .select(  
        table.id(),  
        table.name(),  
        table.edition()  
    )  
    .execute();
```

```
val tuples = sqlClient  
    .createQuery(Book::class) {  
        where(table.name eq "GraphQL in Action")  
        orderBy(table.edition.desc())  
        select(  
            table.id,  
            table.name,  
            table.edition  
        )  
    }  
    .execute()
```

在Jimmer中，除了只返回一列的查询外，其他都是多列查询。这个例子查询了三列，故返回类型为`Tuple3<T1, T2, T3>`。

等到如下数据

```
[  
    {  
        "_1" : 12,  
        "_2" : "GraphQL in Action",  
        "_3" : 3  
    }, {  
        "_1" : 11,  
        "_2" : "GraphQL in Action",  
        "_3" : 2  
    }, {  
        "_1" : 10,  
        "_2" : "GraphQL in Action",  
        "_3" : 1  
    }  
]
```

## 指定被查询对象的多个属性[​](#指定被查询对象的多个属性 "指定被查询对象的多个属性的直接链接")

* Java
* Kotlin

```
BookTable table = BookTable.$;  
  
List<Book> books = sqlClient  
    .createQuery(table)  
    .where(table.name().eq("GraphQL in Action"))  
    .orderBy(table.edition().desc())  
    .select(  
        table.fetch(  
            BookTableFetcher.$  
                // `id()`是隐含的，总是被查询  
                .name()  
                .edition()  
        )  
    )  
    .execute();
```

```
val books = sqlClient  
    .createQuery(Book::class) {  
        where(table.name eq "GraphQL in Action")  
        orderBy(table.edition.desc())  
        select(  
            table.fetchBy {  
                // `id()`是隐含的，总是被查询  
                name()  
                edition()  
            }  
        )  
    }  
    .execute()
```

等到如下数据

```
[  
    {  
        "id" : 12,  
        "name" : "GraphQL in Action",  
        "edition" : 3  
    },   
    {  
        "id" : 11,  
        "name" : "GraphQL in Action",  
        "edition" : 2  
    },   
    {  
        "id" : 10,  
        "name" : "GraphQL in Action",  
        "edition" : 1  
    }   
]
```

不难看出，虽然这个查询只有一列，但是我们可以精确地控制这个对象的格式。

这个例子并没有查询出Book对象的所有属性 *(`price`和多对一关联`store`未被查询)*，由于Jimmer实体对象`Book`执行动态特性，对象被查询属性的多或少不会影响返回类型，一律为`Book`类型。

提示

Jimmer实体对象具备动态性，可以用统一的类型表达任意格式的对象。

因此，Jimmer下不需要使用多列查询来控制返回格式，我们更应该使用单列查询返回一列对象，并灵活控制对象格式。

这会导致Jimmer很少使用基于元组的多列查询，更加面向对象。

## 综合应用二者[​](#综合应用二者 "综合应用二者的直接链接")

* Java
* Kotlin

```
BookTable table = BookTable.$;  
  
List<Tuple2<Book, Integer>> tuples = sqlClient  
    .createQuery(table)  
    .where(table.name().eq("GraphQL in Action"))  
    .orderBy(table.edition().desc())  
    .select(  
            table.fetch(  
                    BookFetcher.$  
                            .allScalarFields()  
            ),  
            Expression.numeric().sql(  
                    Integer.class,  
                    "row_number() over(partition by %e order by %e desc)",  
                    new Expression<?>[] { table.storeId(), table.price() }  
            )  
    )  
    .execute();
```

```
val tuples = sqlClient  
    .createQuery(Book::class) {  
        where(table.name eq "GraphQL in Action")  
        orderBy(table.edition.desc())  
        .select(  
            table.fetchBy {  
                allScalarFields()  
            },  
            sql(Int::class, "row_number() over(partition by %e order by %e desc)") {  
                expression(table.storeId)  
                expression(table.price)  
            }  
        )  
    }  
    .execute()
```

得到的结果为

```
[  
    {  
        "_1":{  
            "id":12,  
            "name":"GraphQL in Action",  
            "edition":3,  
            "price":80  
        },  
        "_2":3  
    },  
    {  
        "_1":{  
            "id":11,  
            "name":"GraphQL in Action",  
            "edition":2,  
            "price":81  
        },  
        "_2":2  
    },  
    {  
        "_1":{  
            "id":10,  
            "name":"GraphQL in Action",  
            "edition":1,  
            "price":82  
        },  
        "_2":1  
    }  
]
```

这个例子查询类两列

* 第一列为对象类型，对象格式为`allScalarFields`*(包含所有非关联属性)*
* 第二列是一个Native SQL表达式，调用底层数据库的分析函数 *(`%e`表示用为Native SQL代码植入DSL表达式)*

很明显这是二者的综合运用。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/base.mdx)

最后于 **2025年9月16日** 更新