# 更多类型

> 来源: https://jimmer.deno.dev/zh/docs/mapping/base/more-type

- [映射篇](/zh/docs/mapping/)
- [基础映射](/zh/docs/mapping/base/)
- 更多类型

本页总览

# 更多类型

除了boolean，数字，字符串，UUID，日期，枚举外，标量属性还支持其他类型，包括

- 数组类型
- JSON类型
- 自定义类型

信息

枚举类型不在本文讨论范围内，请参见[Enum映射](/zh/docs/mapping/advanced/enum)

## 数组类型[​](#数组类型 "数组类型的直接链接")

注意

要使用数组类型，需要底层数据库支持数组类型

- Java
- Kotlin

```@entity
public interface Book {

    @Id
    long id();

    String[] tags();
}

```

```@entity
interface Book {

    @Id
    val id: Long

    val tags: Array<String>
}

```

对于Postgres而言，需要指定SQL中的数据元素类型，例如

- Java
- Kotlin

```@entity
public interface Book {

    @Id
    long id();

    @Column(sqlElementType = "text")
    String[] tags();
}

```

```@entity
interface Book {

    @Id
    val id: Long

    @Column(sqlElementType = "text")
    val tags: Array<String>
}

```

## JSON类型[​](#json类型 "JSON类型的直接链接")

可以利用Jackson支持任何类型的标量属性，无论自定义Java/Kotlin类型，还是集合类型，甚至二者的混合类型。

只需要使用`@org.babyfish.jimmer.sql.Serialized`注解，就可以使用JSON类型。

这里，以集合类型为例，展示其用法

- Java
- Kotlin

```@entity
public interface Book {

    @Id
    long id();

    @Serialized
    Map<String, Map<String, List<Integer>> data();
}

```

```@entity
interface Book {

    @Id
    val id: Long

    @Serialized
    val data: Map<String, Map<String, List<Integer>>
}

```

那么JSON类型在SQL对应何种类型呢？

- 如果数据库支持JSON或JSONB类型，就使用该类型
- 否则，请使用字符串类型

对于Postgres而言，支持对JSON内部结构的操作，请详见[Postgres中的JSON操作](https://www.postgresql.org/docs/9.5/functions-json.html)。那么Jimmer应该如何实现这种操作呢？

Jimmer的SQL DSL可以混入Native SQL表达式，请查看[Native表达式](/zh/docs/query/native-sql)，本文不再赘述。需要注意

警告

[Postgres中的JSON操作](https://www.postgresql.org/docs/9.5/functions-json.html)会用到`?`，而`?`恰好是JDBC的参数，请使用`??`代替。

## 自定义类型[​](#自定义类型 "自定义类型的直接链接")

如果JSON类型仍然无法满足你要求 *(例如：你期望映射一些Postgres特有的类型)*，你可以使用`ScalarProvider`自定义类型。

请参见[ScalarProvider](/zh/docs/configuration/scala-provider)，本文不再赘述。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mapping/base/more-type.mdx)

最后于 **2025年9月16日** 更新