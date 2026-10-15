---
title: 10. Delete语句
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/delete-statement
---

# 10. Delete语句

**Java**

```java
BookTable table = BookTable.$;

int affectedRowCount = sqlClient
    .createDelete(table)
    .where(table.name().eq("GraphQL in Action"))
    .execute();
```

**Kotlin**

```kotlin
val affectedRowCount = sqlClient
    .createDelete(Book::class) {
        where(table.name eq "GraphQL in Action")
    }
    .execute()
```
