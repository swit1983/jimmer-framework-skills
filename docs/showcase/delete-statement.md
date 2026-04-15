---
title: '10. Delete语句'
---


# 10. Delete语句


- Java
- Kotlin


```
BookTable table = BookTable.$;int affectedRowCount = sqlClient    .createDelete(table)    .where(table.name().eq("GraphQL in Action"))    .execute();
```


```
val affectedRowCount = sqlClient    .createDelete(Book::class) {        where(table.name eq "GraphQL in Action")    }    .execute()
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/delete-statement.mdx)最后 于 **2025年9月16日**  更新