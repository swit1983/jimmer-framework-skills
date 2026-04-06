# Connection Manager

> 来源: https://jimmer.deno.dev/zh/docs/configuration/connection-manager

* [配置篇](/zh/docs/configuration/)
* Connection Manager

本页总览

# Connection Manager

## 基本概念[​](#基本概念 "基本概念的直接链接")

Jimmer中一切可执行的语句和指令都支持两种执行模式：

* 基于用户指定的JDBC连接执行
* 由Jimmer自动决定基于某个JDBC连接执行

这里，以`Executable`(Java)或`KExecutable`(kotlin)接口为例

* Java
* Kotlin

Executable.java

```
package org.babyfish.jimmer.sql.ast;  
  
import java.sql.Connection;  
  
public interface Executable<R> {  
  
    R execute();  
  
    R execute(Connection con);  
}
```

KExecutable.kt

```
package org.babyfish.jimmer.sql.kt  
  
import java.sql.Connection  
  
interface KExecutable<R> {  
    fun execute(con: Connection? = null): R  
}
```

* `execute(Connection)`：在用户指定的JDBC连接上执行。

  以查询为例：

  + Java
  + Kotlin

  ```
  BookTable book = Tables.BOOK_TABLE;  
    
  List<Book> books = sqlClient  
      .createQuery(book)  
      .select(book)  
      .execute(con);
  ```

  ```
  val books = sqlClient  
      .createQuery(Book::class) {  
          select(table)  
      }  
      .execute(con)
  ```

  信息

  对这种使用方式而言，无需对SqlClient做出特别配置。
* `execute()`或`execute(null)`：由Jimmer自主决定在某个JDBC连接上执行。

  以查询为例：

  + Java
  + Kotlin

  ```
  BookTable book = Tables.BOOK_TABLE;  
    
  List<Book> books = sqlClient  
      .createQuery(book)  
      .select(book)  
      .execute();
  ```

  ```
  val books = sqlClient  
      .createQuery(Book::class) {  
          select(table)  
      }  
      .execute()
  ```

  信息

  对这种使用方式而言，必须为SqlClient配置`ConnectionManager`。否则将会导致异常。

  毫无疑问，第2种方式更符合业务系统开发要求，推荐使用。所以强烈建议为SqlClient配置`ConnectionManager`。

## 简单的ConnectionManager[​](#简单的connectionmanager "简单的ConnectionManager的直接链接")

* Java
* Kotlin

```
javax.sql.DataSource dataSource = ...;  
  
JSqlClient sqlClient = JSqlClient  
    .newBuilder()  
    .setConnectionManager(  
        ConnectionManager  
            .simpleConnectionManager(dataSource)  
    )  
    .build();
```

```
//    val dataSource: DataSource = DriverManagerDataSource().apply {  
//        setDriverClassName("com.mysql.cj.jdbc.Driver")  
//        url = "jdbc:mysql://localhost:3306/jimmer_demo"  
//        username = "root"  
//        password = "" // 输入你自己的密码  
//    }  
  
val sqlClient = newKSqlClient {  
        setConnectionManager(  
            ConnectionManager.simpleConnectionManager(dataSource)  
        )  
        setDatabaseNamingStrategy(  
            //DefaultDatabaseNamingStrategy.LOWER_CASE  
        )  
    }
```

危险

这种方式仅负责从DataSource获取连接，并没有事务管理机制。

但是，实际项目中，事务非常重要，因此，除学习和尝试外，不建议在实际项目使用这种方式。

## 受Spring事务管理的ConnectionManager[​](#受spring事务管理的connectionmanager "受Spring事务管理的ConnectionManager的直接链接")

这个话题在[Spring篇/整合Spring事务](/zh/docs/spring/transaction)中详细讨论过，本文不做重复阐述。

提示

让Jimmer受到Spring事务的管理，是推荐用法。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/configuration/connection-manager.mdx)

最后于 **2025年9月16日** 更新