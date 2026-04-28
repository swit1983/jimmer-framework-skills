# 智能分页

Jimmer 提供强大的分页支持，包括深分页优化、反排序优化等。

## 基础分页

```java
// Java
Page<Book> page = sqlClient
    .createQuery(table)
    .orderBy(table.name())
    .select(table)
    .fetchPage(pageIndex, pageSize);
```

```kotlin
// Kotlin
val page = sqlClient
    .createQuery(Book::class) {
        orderBy(table.name)
        select(table)
    }
    .fetchPage(pageIndex, pageSize)
```

## 分页结果

```java
// Jimmer 的 Page 类（非 Spring Data 的 Page）
public class Page<T> {
    private final List<T> rows;           // 当前页数据
    private final int totalRowCount;      // 总行数
    private final int totalPageCount;     // 总页数
}
```

## 深分页优化

当页码很大时（如第 10000 页），传统 `LIMIT offset, size` 性能很差。

### 深分页自动优化

当 `offset` 过大时，Jimmer 可自动优化 deep pagination。通过配置阈值 `offset-optimizing-threshold`：

```yaml
# application.yml
jimmer:
  offset-optimizing-threshold: 1000
```

```java
// Java 底层 API
JSqlClient sqlClient = JSqlClient
    .newBuilder()
    .setOffsetOptimizingThreshold(1000)
    .build();
```

当 offset 达到阈值时，Jimmer 先对 id 列分页查询，再回表获取完整数据，避免大 offset 的性能问题：

```sql
-- 未优化（offset 大时性能差）
select ... from BOOK limit 10 offset 10000

-- 优化后（先查 id 再 join 回表）
select optimize_.ID, optimize_.NAME, ...
from (
    select tb_1_.ID optimize_core_id_
    from BOOK tb_1_
    limit 10 offset 10000
) optimize_core_
inner join BOOK optimize_ on optimize_.ID = optimize_core_.optimize_core_id_
```

> 对于 MySQL 等数据库，可将阈值设为 0，让所有分页查询都使用优化策略。

### 使用反向排序优化

当查询后半部分数据时，Jimmer 自动颠倒排序方向以减少扫描行数：

前提条件需同时满足：
1. 使用 `fetchPage()` 查询（非简单 `limit` 查询）
2. 查询具备明确的 `orderBy` 子句
3. 当前页偏后：`offset + pageSize / 2 > totalCount / 2`

```java
// 查询第 3 页（共 6 页，每页 2 条，共 12 条数据）
// 偏后，Jimme 自动使用反排序查询
Page<Book> page = sqlClient
    .createQuery(table)
    .orderBy(table.name().asc(), table.edition().desc())
    .select(table)
    .fetchPage(3, 2);  // 自动优化，无需手动处理
```

自动优化后，SQL 排序方向会反转（asc 变 desc，desc 变 asc），结果自动还原为正确顺序。

## 分页与 Fetcher

```java
// 分页 + Fetcher 抓取指定字段
Page<Book> page = sqlClient
    .createQuery(table)
    .orderBy(table.name())
    .select(
        table.fetch(
            BookFetcher.$
                .name()
                .price()
                .store(BookStoreFetcher.$.name())
        )
    )
    .fetchPage(0, 20);
```

## 分页最佳实践

| 场景 | 推荐方案 |
|------|---------|
| 前 100 页 | 普通 `fetchPage` |
| 深分页（offset 大） | 配置 `offset-optimizing-threshold` |
| 尾部数据查询 | 反排序优化（自动生效） |
| 移动端下拉刷新 | `limit` + 排序条件 |

## 注意事项

1. **必须指定排序**：分页查询必须 `orderBy`，否则结果不稳定
2. **深分页性能**：配置 `offset-optimizing-threshold` 启用自动优化
3. **关联查询分页**：对多对多关联分页需要特殊处理，建议先查主表再查关联
4. **count-query 自动优化**：Jimmer 自动优化 count-query，去掉不必要的 JOIN
