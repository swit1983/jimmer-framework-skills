---
title: '9. Update语句'
---


# 9. Update语句


- Java
- Kotlin


```
BookTable table = BookTable.$;int affectedRowCount = sqlClient    .createUpdate(table)    .set(table.price(), table.price().plus(BigDecimal.ONE))    .where(table.name().eq("GraphQL in Action"))    .execute();
```


```
val affectedRowCount = sqlClient    .createUpdate(Book::class) {        set(table.price, table.price + BigDecimal.ONE)        where(table.name eq "GraphQL in Action")    }    .execute()
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/update-statement.mdx)最后 于 **2025年9月16日**  更新