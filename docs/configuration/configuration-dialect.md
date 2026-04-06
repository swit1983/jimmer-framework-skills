# 方言

> 来源: https://jimmer.deno.dev/zh/docs/configuration/dialect

* [配置篇](/zh/docs/configuration/)
* 方言

本页总览

# 方言

不同的数据库，对SQL的支持大相径庭，因此Jimmer采用方言配置来适配不同的数据库。

## 设置方言[​](#设置方言 "设置方言的直接链接")

* 如果使用Jimmer提供的Spring Boot Starter，有两种用法

  + 配置`application.yml`或`application.properties`

    ```
    jimmer:  
      dialect: org.babyfish.jimmer.sql.dialect.MySqlDialect
    ```
  + 提供全局的方言Bean

    - Java
    - Kotlin

    ```
    @Bean  
    public Dialect dialect() {  
        return new MySqlDialect();  
    }
    ```

    ```
    @Bean  
    fun dialect(): Dialect =  
        MySqlDialect()
    ```

  信息

  如果同时采用以上两种方法 *(不推荐)*，则第二种方法优先
* 如果不使用Jimmer提供的Spring Boot Starter

  + Java
  + Kotlin

  ```
  JSqlClient sqlClient = JSqlClient  
      .newBuilder()  
      .setDialect(new MySqlDialect())  
      ...省略其他配置...  
      .build();
  ```

  ```
  val sqlClient = newKSqlClient {  
      setDialect(MySqlDialect())  
      ...省略其他配置...  
  }
  ```

## 方言列表[​](#方言列表 "方言列表的直接链接")

* org.babyfish.jimmer.sql.dialect.DefaultDialect

  这是未指定方言配置时，Jimmer所采用的默认方言，因此，无需显式指定。

  注意

  默认配置仅能用于学习最初阶段的简单demo，绝不能用于实际项目。以下情况都会导致异常:

  + 代码中对Jimmer的操作会导致生成依赖性特定数据库产品的SQL，而非完美的跨数据库SQL
  + 将[触发器](/zh/docs/mutation/trigger)的类型设置为`TRANSACTION_ONLY`并使用缓存时，Jimmer启动时会自动创建`JIMMER_TRANS_CACHE_OPERATOR`表，`DefaultDialect`并不支持此操作，请参考[缓存一致性](/zh/docs/cache/consistency)
* org.babyfish.jimmer.sql.dialect.H2Dialect
* org.babyfish.jimmer.sql.dialect.MySql5Dialect
* org.babyfish.jimmer.sql.dialect.MySqlDialect
* org.babyfish.jimmer.sql.dialect.PostgresDialect
* org.babyfish.jimmer.sql.dialect.OracleDialect
* org.babyfish.jimmer.sql.dialect.TiDBDialect
* org.babyfish.jimmer.sql.dialect.SQLiteDialect

  因为TiDB是一个分布式数据库，无法支持外键约束，因此，真外键不被此方言支持。请参见[真假外键](/zh/docs/mapping/base/foreignkey)

  除此之外，`TiDBDialect`和`MySqlDialect`没有任何差异。
* 至于其他数据库，用户可以自行扩展方言。但需要数据库支持多列`in`表达式，比如

  ```
  where (a, b) in ((3, 4), (8, 13))
  ```

  或

  ```
  where (a, b) in (select x, y from ...)
  ```

  备注

  目前，多列`in`表达式是Jimmer高度依赖的特性，而Microsoft Sql Server暂不支持此特性。

  因此，目前暂时不支持Microsoft Sql Server。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/configuration/dialect.mdx)

最后于 **2025年9月16日** 更新