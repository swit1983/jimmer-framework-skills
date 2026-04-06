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
public interface Page<T> {
    int getTotalRowCount();      // 总行数
    int getTotalPageCount();     // 总页数
    List<T> getRows();           // 当前页数据
}
```

## 深分页优化

当页码很大时（如第 10000 页），传统 `LIMIT offset, size` 性能很差。

### 使用游标分页

```java
// 记录上一页最后一条数据的排序值
String lastName = "xxx";

List<Book> books = sqlClient
    .createQuery(table)
    .where(
        // 从上一页最后一条之后开始
        table.name().gt(lastName)
    )
    .orderBy(table.name())
    .select(table)
    .limit(pageSize)  // 只取 pageSize 条
    .execute();
```

### 使用反向排序优化

当查询后半部分数据时，反向排序查询可以显著减少扫描行数：

```java
// 查询第 9990-10000 条（共 10000 条）
// 反向排序：只扫描前 10 条
List<Book> books = sqlClient
    .createQuery(table)
    .orderBy(table.name().desc())  // 反向排序
    .select(table)
    .fetchPage(0, 10);  // 取第一页

// 再将结果反向回来
Collections.reverse(books);
```

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
| 深分页（> 1000 页） | 游标分页（记录 lastId） |
| 尾部数据查询 | 反向排序优化 |
| 移动端下拉刷新 | 游标分页 |

## 注意事项

1. **必须指定排序**：分页查询必须 `orderBy`，否则结果不稳定
2. **深分页性能**：超过 1000 页建议改用游标方式
3. **关联查询分页**：对多对多关联分页需要特殊处理，建议先查主表再查关联
