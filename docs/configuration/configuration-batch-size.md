# 批量控制

> 来源: https://jimmer.deno.dev/zh/docs/configuration/batch-size

* [配置篇](/zh/docs/configuration/)
* 批量控制

本页总览

# 批量控制

SqlClient支持两个配置：`DefaultBatchSize`和`DefaultListBatchSize`。如果不配置，默认值为128和16。

这两个配置为[对象抓取器](/zh/docs/query/object-fetcher/)中的batchSize提供默认值，
其作用在[对象抓取器](/zh/docs/query/object-fetcher/)做了详细描述，本文只交代配置，不重复阐述其作用。

有两个方法可以设置这两个配置

* 使用Spring Boot Starter
* 不使用Spring Boot Starter

## 使用Spring Boot Starter[​](#使用spring-boot-starter "使用Spring Boot Starter的直接链接")

```
jimmer:  
   default-batch-size: 256  
   default-list-batch-size: 32
```

## 不使用Spring Boot Starter[​](#不使用spring-boot-starter "不使用Spring Boot Starter的直接链接")

* Java
* Kotlin

```
@Bean  
public JSqlClient sqlClient() {  
    return JSqlClient  
        .newBuilder()  
        .setConnectionManager(...)  
        .setDialect(new H2Dialect())  
        .setExecutor(...)  
        .addScalarProvider(...)  
        .setDefaultBatchSize(256)  
        .setDefaultListBatchSize(32)  
        .build();  
}
```

```
@Bean  
fun sqlClient(): KSqlClient =   
    newKSqlClient {  
        setConnectionManager { ... }  
        setDialect(H2Dialect())  
        setExecutor { ... }  
        addScalarProvider { ... }  
        setDefaultBatchSize(256)  
        setDefaultListBatchSize(32)  
    }
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/configuration/batch-size.mdx)

最后于 **2025年9月16日** 更新