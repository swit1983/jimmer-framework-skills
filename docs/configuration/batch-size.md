---
title: 批量控制
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/configuration/batch-size
---

# 批量控制

SqlClient支持两个配置：`DefaultBatchSize`和`DefaultListBatchSize`。如果不配置，默认值为128和16。

这两个配置为[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)中的batchSize提供默认值，
其作用在[对象抓取器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/object-fetcher)做了详细描述，本文只交代配置，不重复阐述其作用。

有两个方法可以设置这两个配置

-   使用Spring Boot Starter
-   不使用Spring Boot Starter

## 使用Spring Boot Starter

```
jimmer:
   default-batch-size: 256
   default-list-batch-size: 32
```

## 不使用Spring Boot Starter

**Java**

```java
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

**Kotlin**

```kotlin
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
