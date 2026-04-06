# 整合Spring事务

> 来源: https://jimmer.deno.dev/zh/docs/spring/transaction

* [Spring篇](/zh/docs/spring/)
* 整合Spring事务

本页总览

# 整合Spring事务

## 整合Spring事务[​](#整合spring事务 "整合Spring事务的直接链接")

Jimmer中所有数据库操作API都有两种执行方式：

* 在指定的JDBC连接上执行
* 无需指定JDBC连接即可执行，但需要为Jimmer配置`ConnectionManager`，教会Jimmer如何租借和归还连接。

请参考

这里以了解更多。

所以，Jimmer本身并未提供连接/事务管理能力，这种管理能力完全依赖用户对`ConnectionManager`的定制，`ConnectionManager`就是将Jimmer和任何IOC框架 *(当然，包括Spring)* 的连接/事务管理能力整合在一起的关键点。

### 使用Spring Boot starter[​](#使用spring-boot-starter "使用Spring Boot starter的直接链接")

如果使用Jimmer提供的Spring Boot Starter，则不用做任何工作，Jimmer会自动接入Spring的事务管理机制。

### 不使用Spring Boot Starter[​](#不使用spring-boot-starter "不使用Spring Boot Starter的直接链接")

如果仅使用Spring，并未使用Jimmer提供的Spring Boot Starter。那么需要自己编码将Jimmer接入Spring的事务管理机制。

开发人员需要创建`JSqlClient/KSqlClient`，并设置其`ConnectionManager`，在`ConnectionManager`中，利用Spring的`org.springframework.jdbc.datasource.DataSourceUtils`打开和关闭连接。

* Java
* Kotlin

Book.java

```
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

Book.kt

```
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

警告

不要使用普通的方法从连接池借用 *(dataSource.getConnection)* 和归还 *(con.close)* 连接，
一定要使用Spring的`org.springframework.jdbc.datasource.DataSourceUtils`，因为这可以和Spring的事务管理机制相结合。

## 和JdbcTemplate协同[​](#和jdbctemplate协同 "和JdbcTemplate协同的直接链接")

Jimmer采用极简设计，其API总入口`JSqlClient/KSqlClient`对外暴露的API一律采用无状态设计。

很多数据库操作框架对JDBC连接提供了一个轻量级有状态包装，比如

* JPA的[EntityManager](https://docs.oracle.com/javaee/7/api/javax/persistence/EntityManager.html)
* Hibernate的[Session](https://docs.jboss.org/hibernate/orm/6.2/javadocs/org/hibernate/Session.html)
* MyBatis的[SqlSession](https://javadoc.io/doc/org.mybatis/mybatis/latest/org/apache/ibatis/session/SqlSession.html)。

并且对数据库事务也有有状态封装，比如

* JPA的[EntityManager.getTransaction](https://docs.oracle.com/javaee/7/api/javax/persistence/EntityManager.html#getTransaction--)
* Hibernate的[Session.getTransaction](https://docs.jboss.org/hibernate/orm/6.2/javadocs/org/hibernate/SharedSessionContract.html#getTransaction())
* MyBatis的[SqlSession.commit](https://javadoc.io/doc/org.mybatis/mybatis/latest/org/apache/ibatis/session/SqlSession.html#commit())

Jimmer没有类似的抽象，其API总入口`JSqlClient/KSqlClient`对外暴露的API一律采用无状态设计，JDBC连接是Jimmer唯一的底层依赖。

提示

这促成了一个重要的特性：Jimmer的事务管理和JdbcTemplate的事务管理完全相同。

Jimmer无需提供任何类似于`createNativeQuery`的API

* 对于和ORM关系不大的报表查询，用户期望书写完整的Native SQL，那么直接使用Spring的JdbcTemplate即可，这是因为Jimmer的事务管理和JdbcTemplate的事务管理完全相同。
* 对于Jimmer的ORM风格查询，在强类型SQL DSL中混入[Native SQL](/zh/docs/query/native-sql)表达式即可

## 多数据源[​](#多数据源 "多数据源的直接链接")

上文我们讨论的是单数据源场景下的Spring事务整合，至于多数据源，请查看[这里](/zh/docs/configuration/multi-datasources)。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/spring/transaction.mdx)

最后于 **2025年9月16日** 更新