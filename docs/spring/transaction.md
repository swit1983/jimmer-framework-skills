---
title: '整合Spring事务'
---
# 整合Spring事务


## 整合Spring事务


Jimmer中所有数据库操作API都有两种执行方式：


- 在指定的JDBC连接上执行
- 无需指定JDBC连接即可执行，但需要为Jimmer配置`ConnectionManager`，教会Jimmer如何租借和归还连接。


请参考

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}这里@media print{.css-1k371a6{position:absolute!important;}}以了解更多。


所以，Jimmer本身并未提供连接/事务管理能力，这种管理能力完全依赖用户对`ConnectionManager`的定制，`ConnectionManager`就是将Jimmer和任何IOC框架 *(当然，包括Spring)* 的连接/事务管理能力整合在一起的关键点。


### 使用Spring Boot starter


如果使用Jimmer提供的Spring Boot Starter，则不用做任何工作，Jimmer会自动接入Spring的事务管理机制。


### 不使用Spring Boot Starter


如果仅使用Spring，并未使用Jimmer提供的Spring Boot Starter。那么需要自己编码将Jimmer接入Spring的事务管理机制。


开发人员需要创建`JSqlClient/KSqlClient`，并设置其`ConnectionManager`，在`ConnectionManager`中，利用Spring的`org.springframework.jdbc.datasource.DataSourceUtils`打开和关闭连接。


- Java
- Kotlin
Book.java

```
@Beanpublic JSqlClient sqlClient(DataSource dataSource) {    return JSqlClient.newBuilder()        .setConnectionManager(            new ConnectionManager() {                @Override                public <R> R execute(                    Function<Connection, R> block                ) {                    Connection con = DataSourceUtils                        .getConnection(dataSource);                    try {                        return block.apply(con);                    } finally {                        DataSourceUtils                            .releaseConnection(con, dataSource);                    }                }            }        )        ...省略其他配置...        .build();}
```

Book.kt

```
@Beanfun sqlClient(dataSource: DataSource): KSqlClient =    newKSqlClient {        setConnectionManager {            val con = DataSourceUtils                .getConnection(dataSource)            try {                proceed(con)            } finally {                DataSourceUtils                    .releaseConnection(con, dataSource)            }        }        ...省略其他配置...    }
```


警告

不要使用普通的方法从连接池借用 *(dataSource.getConnection)* 和归还 *(con.close)* 连接，
一定要使用Spring的`org.springframework.jdbc.datasource.DataSourceUtils`，因为这可以和Spring的事务管理机制相结合。


## 和JdbcTemplate协同


Jimmer采用极简设计，其API总入口`JSqlClient/KSqlClient`对外暴露的API一律采用无状态设计。


很多数据库操作框架对JDBC连接提供了一个轻量级有状态包装，比如


- JPA的[EntityManager](https://docs.oracle.com/javaee/7/api/javax/persistence/EntityManager.html)
- Hibernate的[Session](https://docs.jboss.org/hibernate/orm/6.2/javadocs/org/hibernate/Session.html)
- MyBatis的[SqlSession](https://javadoc.io/doc/org.mybatis/mybatis/latest/org/apache/ibatis/session/SqlSession.html)。


并且对数据库事务也有有状态封装，比如


- JPA的[EntityManager.getTransaction](https://docs.oracle.com/javaee/7/api/javax/persistence/EntityManager.html#getTransaction--)
- Hibernate的[Session.getTransaction](https://docs.jboss.org/hibernate/orm/6.2/javadocs/org/hibernate/SharedSessionContract.html#getTransaction())
- MyBatis的[SqlSession.commit](https://javadoc.io/doc/org.mybatis/mybatis/latest/org/apache/ibatis/session/SqlSession.html#commit())


Jimmer没有类似的抽象，其API总入口`JSqlClient/KSqlClient`对外暴露的API一律采用无状态设计，JDBC连接是Jimmer唯一的底层依赖。


提示

这促成了一个重要的特性：Jimmer的事务管理和JdbcTemplate的事务管理完全相同。


Jimmer无需提供任何类似于`createNativeQuery`的API


- 对于和ORM关系不大的报表查询，用户期望书写完整的Native SQL，那么直接使用Spring的JdbcTemplate即可，这是因为Jimmer的事务管理和JdbcTemplate的事务管理完全相同。
- 对于Jimmer的ORM风格查询，在强类型SQL DSL中混入[Native SQL](query/native-sql)表达式即可


## 多数据源


上文我们讨论的是单数据源场景下的Spring事务整合，至于多数据源，请查看[这里](configuration/multi-datasources)。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/spring/transaction.mdx)最后 于 **2025年9月16日**  更新
- [整合Spring事务](#整合spring事务)
- [使用Spring Boot starter](#使用spring-boot-starter)
- [不使用Spring Boot Starter](#不使用spring-boot-starter)
- [和JdbcTemplate协同](#和jdbctemplate协同)
- [多数据源](#多数据源)