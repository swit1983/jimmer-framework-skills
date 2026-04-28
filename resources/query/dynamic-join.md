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

被创建的 JOIN 对象如果不被使用，将会被忽略：

```java
// 即使创建了 table.store()，如果未被查询条件使用，JOIN 不会出现在 SQL 中
BookStoreTable store = table.store();  // 创建 JOIN 对象

List<Book> books = sqlClient
    .createQuery(table)
    .orderBy(table.name().asc())
    .select(table)  // 未使用 store，JOIN 被忽略
    .execute();
```

分页查询时，`count-query` 会自动移除仅被 `select` 或 `orderBy` 使用的 JOIN，进一步优化性能。

### 3. 幻连接和半连接

- **幻连接**：对基于外键的多对一关联，仅访问关联对象 `id()` 时，自动优化为直接使用当前表外键，去掉 JOIN
- **半连接**：对基于中间表的多对多关联，仅访问关联对象 `id()` 时，自动去掉到目标表的 JOIN，只保留到中间表的 JOIN

### 4. 弱连接（Weak Join）

弱连接用于基于**非外键的业务属性**连接两张表。需要实现 `WeakJoin` 接口定义连接条件：

```java
// Java: 定义弱连接条件
private static class BookAuthorJoin
        implements WeakJoin<BookTable, AuthorTable> {
    @Override
    public Predicate on(BookTable source, AuthorTable target) {
        return Predicate.and(
            source.businessProp1().eq(target.businessPropA()),
            source.businessProp2().eq(target.businessPropB())
        );
    }
}

// 使用弱连接（需要 asTableEx 将 Table 转为 TableEx）
List<Long> bookIds = sqlClient
    .createQuery(table)
    .where(
        table
            .asTableEx()  // weakJoin 只被 TableEx 支持
            .weakJoin(BookAuthorJoin.class)
            .firstName().eq("Alex")
    )
    .select(table.id())
    .distinct()
    .execute();
```

```kotlin
// Kotlin: 定义弱连接条件
private class BookAuthorJoin : KWeakJoin<Book, Author> {
    override fun on(
        source: KNonNullTable<Book>,
        target: KNonNullTable<Author>
    ): KNonNullExpression<Boolean> =
        and(
            source.businessProp1 eq target.businessPropA,
            source.businessProp2 eq target.businessPropB
        )
}

// 使用弱连接
val bookIds = sqlClient
    .createQuery(Book::class) {
        where(
            table
                .asTableEx()
                .weakJoin(BookAuthorJoin::class)
                .firstName eq "Alex"
        )
        select(table.id)
    }
    .distinct()
    .execute()
```

> **注意**：`WeakJoin`/`KWeakJoin` 实现类不能用 lambda 或匿名类，因为同类多次 `weakJoin` 可自动合并。

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

// 链式 JOIN（使用 asTableEx 访问集合关联）
val books = sqlClient
    .createQuery(Book::class) {
        where(
            table.store.website.isNotNull and
            table.asTableEx().authors.firstName.eq("Alex")
        )
        select(table.id)
    }
    .distinct()
    .execute()
```

## 最佳实践

| 建议 | 说明 |
|------|------|
| 信任动态 JOIN | 不需要手动优化 JOIN，让 Jimmer 自动决定 |
| 使用 Fetcher | 配合 Fetcher 指定需要抓取的关联，避免 over-fetch |
| 弱连接用于非外键关联 | 需要基于非外键业务属性连接表时，使用 WeakJoin |
| 使用隐式子查询 | 对集合关联优先使用隐式子查询而非 JOIN |
