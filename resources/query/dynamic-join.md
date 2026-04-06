# 动态 JOIN

Jimmer 的动态 JOIN 是其核心特性之一，能够自动优化查询性能并解决 N+1 问题。

## 问题背景

传统 ORM 的 JOIN 问题：
- **Over-fetch**：JOIN 了不需要的表
- **Under-fetch**：需要时没有 JOIN，导致 N+1
- **手动优化**：开发人员需要手动决定 JOIN 哪些表

Jimmer 通过动态 JOIN 自动解决这些问题。

## 动态 JOIN 原理

Jimmer 根据以下条件动态决定 JOIN：
1. **查询条件**（WHERE）使用的字段
2. **排序字段**（ORDER BY）
3. **抓取字段**（Fetcher）中的关联对象

只 JOIN 真正需要的表，避免冗余。

## 基本示例

```java
// 查询书店中书名包含 "GraphQL" 的书籍
List<Book> books = sqlClient
    .createQuery(table)
    .where(table.store().name().eq("O'REILLY"))  // 需要 JOIN store
    .where(table.name().like("GraphQL"))
    .select(table)
    .execute();
```

生成的 SQL：
```sql
select tb_1_.ID, tb_1_.NAME, tb_1_.EDITION, tb_1_.PRICE, tb_1_.STORE_ID
from BOOK tb_1_
inner join BOOK_STORE tb_2_ on tb_1_.STORE_ID = tb_2_.ID  -- 动态 JOIN
where tb_2_.NAME = ?
  and tb_1_.NAME like ?
```

## 智能优化

### 1. 合并冲突连接

当多个条件需要 JOIN 同一张表时，Jimmer 会自动合并：

```java
sqlClient.createQuery(table)
    .where(table.store().name().eq("O'REILLY"))      // 需要 JOIN store
    .where(table.store().website().isNotNull())      // 同一表，复用 JOIN
    .select(table)
    .execute();
```

### 2. 优化不必要连接

如果父查询已 JOIN，子查询不会重复 JOIN：

```java
// 分页查询
Page<Book> page = sqlClient.createQuery(table)
    .where(table.store().name().eq("O'REILLY"))
    .orderBy(table.name())
    .select(table)
    .fetchPage(0, 10);  // 内部查询自动优化 JOIN
```

### 3. 弱连接（Weak Join）

对于可选关联，使用弱连接避免强制 INNER JOIN：

```java
// 即使作者没有对应的用户账号，也能查询出来
sqlClient.createQuery(authorTable)
    .where(authorTable.user(UserTable.class).name().eq("admin"))  // 弱连接
    .select(authorTable)
    .execute();
```

## Kotlin 特有语法

Kotlin 支持更简洁的 JOIN 语法：

```kotlin
// 普通 JOIN
val books = sqlClient
    .createQuery(Book::class) {
        where(table.store.name eq "O'REILLY")
        select(table)
    }
    .execute()

// 链式 JOIN
val books = sqlClient
    .createQuery(Book::class) {
        where(
            table.store.website.isNotNull and
            table.authors.any { it.firstName eq "Alex" }
        )
        select(table)
    }
    .execute()
```

## 最佳实践

| 建议 | 说明 |
|------|------|
| 信任动态 JOIN | 不需要手动优化 JOIN，让 Jimmer 自动决定 |
| 使用 Fetcher | 配合 Fetcher 指定需要抓取的关联，避免 over-fetch |
| 弱连接可选关联 | 可选关联使用弱连接，避免强制 INNER JOIN |
| 避免子查询手动 JOIN | 除非必要，否则让 Jimmer 自动处理子查询 JOIN |
