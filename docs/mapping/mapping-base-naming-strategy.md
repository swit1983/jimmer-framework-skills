# 命名策略

> 来源: https://jimmer.deno.dev/zh/docs/mapping/base/naming-strategy

- [映射篇](/zh/docs/mapping/)
- [基础映射](/zh/docs/mapping/base/)
- 命名策略

本页总览

# 命名策略

## 命名策略接口[​](#命名策略接口 "命名策略接口的直接链接")

前面的章节，我们已经介绍了简单实体映射和关联映射 *（一对一、多对一、一对多、多对多）*。从这些内容中，我们了解到

- 可以使用`@Table(name = "...")`明确地为实体指定表名
- 可以使用`@GeneratedValue(..., sequenceName = "...")`明确地指定生成id所需的序列名称 *（前提是使用序列增长策略）*
- 可以使用`@Column(name = "...")`明确地为普通列指定列名
- 可以使用`@JoinColumn(name = "...")`明确地为外键列指定列名
- 可以使用`@JoinTable(name = "...")`明确地为基于中间表的关联属性指定中间表表名以及其所有列名。

然而，为了提高开发效率，我们不可能过多地使用这些注解。大部分情况下，默认的名字推导行为应该可以工作，少数情况下，才在代码中使用这些注解。

对于某个类或属性，如果用户**不**用这类注解，如何自动决定数据库中的标识名，被称为命名策略，是一个可定制的Java接口

```package org.babyfish.jimmer.sql.meta;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;

public interface DatabaseNamingStrategy {

    String tableName(ImmutableType type);

    String sequenceName(ImmutableType type);

    String columnName(ImmutableProp prop);

    String foreignKeyColumnName(ImmutableProp prop);

    String middleTableName(ImmutableProp prop);

    String middleTableBackRefColumnName(ImmutableProp prop);

    String middleTableTargetRefColumnName(ImmutableProp prop);
}

```

其中，`ImmutableType`和`ImmutableProp`是Jimmer元数据所用类型，可以很直观地用JVM反射API的`Class`和`Field`来做类比理解。

各方法的作用如下

- tableName: 已知一个实体类型，其表名是什么？
- sequenceName: 已知一个id增长策略为序列的实体类型，其序列名是什么？
- columName: 已知一个非关联属性，其列名是什么？
- foreignKeyColumnName: 已知一个基于外键的关联属性，其列名是什么？
- middleTableName: 已知一个基于中间表的关联属性，其中间表表名是什么？
- middleTableBackRefColumnName: 已知一个基于中间表的关联属性，中间表中指向当前实体的外键的列名是什么？
- middleTableTargetRefColumnName: 已知一个基于中间表的关联属性，中间表中指向关联实体的外键的列名是什么？

## 默认命名策略[​](#默认命名策略 "默认命名策略的直接链接")

大部分情况下，开发人员都无需直接实现此接口，Jimmer内置的`org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy`类已经实现了此接口。

DefaultDatabaseNamingStrategy类有两个静态字段

- UPPER\_CASE:

  生成的数据库标识名全部大写

  信息

  如果用户不做任何配置，这就是Jimmer默认的命名策略。
- LOWER\_CASE:

  生成的数据库标识名全部小写。

  一些数据库，比如MySQL，可以配置是否大小写敏感。所以，你很有可能接手一个MySQL数据库，被设置为大小敏感模式且大部分表名和列名都是小写的，这时，你需要用此策略覆盖默认策略。

提示

即使`UPPER_CASE`和`LOWER_CASE`都无法满足你的要求，你需要实现自己的策略，也可以考虑继承这个默认策略，而非从头实现。

在介绍默认策略的行为之前，我们先引入一个字符变换规则：snake。

所谓snake，即把大小写交替的文本转化为下划线拼接的文本，比如类名`BookStore`的snake变形为`BOOK_STORE`，属性名`firstName`的snake变形为`FIRST_NAME`。

考虑到大小写问题，我们定义两个函数, `u_snake`和`l_snake`，其行为如下

- `u_snake("BookStore")` -> "BOOK\_STORE"
- `l_snake("BookStore")` -> "book\_store"
- `u_snake("firstName")` -> "FIRST\_NAME"
- `l_snake("firstName")` -> "first\_name"

有了`u_snake`和`l_snake`的规定后，我们很容易阐述`DefaultDatabaseNamingStrategy`的行为

备注

下文中的ClassName，指Java类的SimpleName，并非QualifiedName。

### UPPER\_CASE[​](#upper_case "UPPER_CASE的直接链接")

- tableName

  规则：`u_snake(ClassName)`

  例子：BookStore -> BOOK\_STORE
- sequenceName

  规则：`u_snake(ClassName)`\_ID\_SEQ

  例子：BookStore -> BOOK\_STORE\_ID\_SEQ
- columName

  规则：`u_snake(ClassName)`

  例子：firstName -> FIRST\_NAME
- foreignKeyColumnName

  规则：`u_snake(ClassName)`\_ID

  例子：parentNode -> PARENT\_NODE\_ID
- middleTableName

  规则：`u_snake(SourceClassName)`\_`u_snake(TargetClassName)`\_MAPPING

  例子：Book::authors -> BOOK\_AUTHOR\_MAPPING
- middleTableBackRefColumnName

  规则：`u_snake(SourceClassName)`\_ID

  例子：Book::authors -> BOOK\_ID
- middleTableTargetRefColumnName

  规则：`u_snake(TargetClassName)`\_ID

  例子：Book::authors -> AUTHOR\_ID

### LOWER\_CASE[​](#lower_case "LOWER_CASE的直接链接")

- tableName

  规则：`l_snake(ClassName)`

  例子：BookStore -> book\_store
- sequenceName

  规则：`l_snake(ClassName)`\_id\_seq

  例子：BookStore -> book\_store\_id\_seq
- columName

  规则：`l_snake(ClassName)`

  例子：firstName -> first\_name
- foreignKeyColumnName

  规则：`l_snake(ClassName)`\_id

  例子：parentNode -> parent\_node\_id
- middleTableName

  规则：`l_snake(SourceClassName)`\_`l_snake(TargetClassName)`\_mapping

  例子：Book::authors -> book\_author\_mapping
- middleTableBackRefColumnName

  规则：`l_snake(SourceClassName)`\_id

  例子：Book::authors -> book\_id
- middleTableTargetRefColumnName

  规则：`l_snake(TargetClassName)`\_id

  例子：Book::authors -> author\_id

## 覆盖策略[​](#覆盖策略 "覆盖策略的直接链接")

现在，我们来演示如何用`DefaultDatabaseNamingStrategy.LOWER_CASE`覆盖默认的`DefaultDatabaseNamingStrategy.UPPER_CASE`。

### 使用SpringBoot时[​](#使用springboot时 "使用SpringBoot时的直接链接")

- Java
- Kotlin

```@bean
public DatabaseNamingStrategy databaseNamingStrategy() {
    return DefaultDatabaseNamingStrategy.LOWER_CASE;
}

```

```@bean
fun databaseNamingStrategy(): DatabaseNamingStrategy =
    DefaultDatabaseNamingStrategy.LOWER_CASE

```

### 不使用SpringBoot时[​](#不使用springboot时 "不使用SpringBoot时的直接链接")

- Java
- Kotlin

```jsqlclient sqlclient = jsqlclient
    .newBuilder()
    .setDatabaseNamingStrategy(
        DefaultDatabaseNamingStrategy.LOWER_CASE
    )
    ...省略其他配置...
    .build();

```

```val sqlclient = newksqlclient {
    setDatabaseNamingStrategy(
        DefaultDatabaseNamingStrategy.LOWER_CASE
    )
    ...省略其他配置...
}

```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mapping/base/naming-strategy.mdx)

最后于 **2025年9月16日** 更新