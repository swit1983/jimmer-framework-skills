---
title: 整合Spring事务
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/spring/transaction
---

# 整合Spring事务

## 整合Spring事务

Jimmer中所有数据库操作API都有两种执行方式：

-   在指定的JDBC连接上执行

-   无需指定JDBC连接即可执行，但需要为Jimmer配置`ConnectionManager`，教会Jimmer如何租借和归还连接。

请参考
Jimmer中一切可执行的语句和指令都支持两种执行模式：

-   基于用户指定的JDBC连接执行

-   由Jimmer自动决定基于某个JDBC连接执行

这里，以`Executable`(Java)或`KExecutable`(kotlin)接口为例

**Java**

```java title="Executable.java"
package org.babyfish.jimmer.sql.ast;

import java.sql.Connection;

public interface Executable<R> {

    R execute();

    R execute(Connection con);
}
```

**Kotlin**

```kotlin title="KExecutable.kt"
package org.babyfish.jimmer.sql.kt

import java.sql.Connection

interface KExecutable<R> {
    fun execute(con: Connection? = null): R
}
```

-   `execute(Connection)`：在用户指定的JDBC连接上执行。

    以查询为例：

**Java**

```java
BookTable book = Tables.BOOK_TABLE;

List<Book> books = sqlClient
    .createQuery(book)
    .select(book)

    .execute(con);
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        select(table)
    }

    .execute(con)
```

    :::info
    对这种使用方式而言，无需对SqlClient做出特别配置。
    :::

-   `execute()`或`execute(null)`：由Jimmer自主决定在某个JDBC连接上执行。

    以查询为例：

**Java**

```java
BookTable book = Tables.BOOK_TABLE;

List<Book> books = sqlClient
    .createQuery(book)
    .select(book)

    .execute();
```

**Kotlin**

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        select(table)
    }

    .execute()
```

    :::info
    对这种使用方式而言，必须为SqlClient配置`ConnectionManager`。否则将会导致异常。

    毫无疑问，第2种方式更符合业务系统开发要求，推荐使用。所以强烈建议为SqlClient配置`ConnectionManager`。
    :::以了解更多。

所以，Jimmer本身并未提供连接/事务管理能力，这种管理能力完全依赖用户对`ConnectionManager`的定制，`ConnectionManager`就是将Jimmer和任何IOC框架 *(当然，包括Spring)* 的连接/事务管理能力整合在一起的关键点。

### 使用Spring Boot starter

如果使用Jimmer提供的Spring Boot Starter，则不用做任何工作，Jimmer会自动接入Spring的事务管理机制。

### 不使用Spring Boot Starter

如果仅使用Spring，并未使用Jimmer提供的Spring Boot Starter。那么需要自己编码将Jimmer接入Spring的事务管理机制。

开发人员需要创建`JSqlClient/KSqlClient`，并设置其`ConnectionManager`，在`ConnectionManager`中，利用Spring的`org.springframework.jdbc.datasource.DataSourceUtils`打开和关闭连接。

**Java**

```java title="Book.java"
@Bean
public JSqlClient sqlClient(DataSource dataSource) {
    return JSqlClient.newBuilder()

        .setConnectionManager(
            new ConnectionManager() {
                @Override
                public <R> R execute(
                    Function<Connection, R> block
                ) {
                    Connection con = DataSourceUtils

                        .getConnection(dataSource);
                    try {
                        return block.apply(con);
                    } finally {
                        DataSourceUtils

                            .releaseConnection(con, dataSource);
                    }
                }
            }
        )
        ...省略其他配置...
        .build();
}
```

**Kotlin**

```kotlin title="Book.kt"
@Bean
fun sqlClient(dataSource: DataSource): KSqlClient =
    newKSqlClient {

        setConnectionManager {
            val con = DataSourceUtils

                .getConnection(dataSource)
            try {
                proceed(con)
            } finally {
                DataSourceUtils

                    .releaseConnection(con, dataSource)
            }
        }
        ...省略其他配置...
    }
```

:::caution
不要使用普通的方法从连接池借用 *(dataSource.getConnection)* 和归还 *(con.close)* 连接，
一定要使用Spring的`org.springframework.jdbc.datasource.DataSourceUtils`，因为这可以和Spring的事务管理机制相结合。
:::

## 和JdbcTemplate协同

Jimmer采用极简设计，其API总入口`JSqlClient/KSqlClient`对外暴露的API一律采用无状态设计。

很多数据库操作框架对JDBC连接提供了一个轻量级有状态包装，比如
-   JPA的[EntityManager](https://docs.oracle.com/javaee/7/api/javax/persistence/EntityManager.html)
-   Hibernate的[Session](https://docs.jboss.org/hibernate/orm/6.2/javadocs/org/hibernate/Session.html)
-   MyBatis的[SqlSession](https://javadoc.io/doc/org.mybatis/mybatis/latest/org/apache/ibatis/session/SqlSession.html)。

并且对数据库事务也有有状态封装，比如
-   JPA的[EntityManager.getTransaction](https://docs.oracle.com/javaee/7/api/javax/persistence/EntityManager.html#getTransaction--)
-   Hibernate的[Session.getTransaction](https://docs.jboss.org/hibernate/orm/6.2/javadocs/org/hibernate/SharedSessionContract.html#getTransaction())
-   MyBatis的[SqlSession.commit](https://javadoc.io/doc/org.mybatis/mybatis/latest/org/apache/ibatis/session/SqlSession.html#commit())

Jimmer没有类似的抽象，其API总入口`JSqlClient/KSqlClient`对外暴露的API一律采用无状态设计，JDBC连接是Jimmer唯一的底层依赖。

:::tip
这促成了一个重要的特性：Jimmer的事务管理和JdbcTemplate的事务管理完全相同。
:::

Jimmer无需提供任何类似于`createNativeQuery`的API

-   对于和ORM关系不大的报表查询，用户期望书写完整的Native SQL，那么直接使用Spring的JdbcTemplate即可，这是因为Jimmer的事务管理和JdbcTemplate的事务管理完全相同。

-   对于Jimmer的ORM风格查询，在强类型SQL DSL中混入[Native SQL](../query/native-sql.md)表达式即可

## 多数据源

上文我们讨论的是单数据源场景下的Spring事务整合，至于多数据源，请查看[这里](../configuration/multi-datasources.md)。
