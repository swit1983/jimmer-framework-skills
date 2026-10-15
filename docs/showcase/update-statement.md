---
title: 9. Update语句
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/showcase/update-statement
---

# 9. Update语句

**Java**

```java
BookTable table = BookTable.$;

int affectedRowCount = sqlClient
    .createUpdate(table)
    .set(table.price(), table.price().plus(BigDecimal.ONE))
    .where(table.name().eq("GraphQL in Action"))
    .execute();
```

**Kotlin**

```kotlin
val affectedRowCount = sqlClient
    .createUpdate(Book::class) {
        set(table.price, table.price + BigDecimal.ONE)
        where(table.name eq "GraphQL in Action")
    }
    .execute()
```
