# 对象抓取器 (Fetcher)

> 一句话：**查询任意形状的数据结构，就如同 GraphQL 所做的那样**。

## 引入对象抓取器的原因

在了解对象抓取器之前，有必要先了解常规的属性查询。

例如，我需要这样一个 SQL：

```sql
select
    b.id,
    b.name,
    b.edition
    /* 不需要`b.price`和`b.store_id` */
from book b
where b.edition = 3;
```

在这个 SQL 中，我们只查询部分列。如果我们需要返回对象而不是元组，通常需要定义一个 DTO 类并手动转换。

Jimmer 的对象抓取器很好地解决了这个问题：**无需定义 DTO，直接查询指定形状的数据结构**。

## 基本用法

### Java

```java
BookTable book = BookTable.$;

// 创建 Fetcher
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .edition()
    .price()
    .store(BookStoreFetcher.$.name())  // 关联对象
    .authors(AuthorFetcher.$.firstName().lastName());  // 关联集合

// 使用 Fetcher 查询
List<Book> books = sqlClient
    .createQuery(book)
    .where(book.edition().eq(3))
    .select(book.fetch(fetcher))
    .execute();
```

### Kotlin

```kotlin
val books = sqlClient
    .createQuery(Book::class) {
        where(table.edition eq 3)
        select(table.fetchBy {
            name()
            edition()
            price()
            store { name() }
            authors { firstName(); lastName() }
        })
    }
    .execute()
```

## Fetcher 构建方式

### 1. 基本属性选择

```java
// 选择特定字段
Fetcher<Book> fetcher = BookFetcher.$
    .id()
    .name()
    .price();

// 选择所有标量字段（非关联属性）
Fetcher<Book> fetcher = BookFetcher.$.allScalarFields();

// 选择所有表字段
Fetcher<Book> fetcher = BookFetcher.$.allTableFields();
```

### 2. 关联对象抓取

```java
// 多对一关联
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .store(BookStoreFetcher.$.name().website());  // 抓取书店的名字和网站

// 一对多关联
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .authors(AuthorFetcher.$.firstName().lastName());  // 抓取作者列表

// 控制关联集合的过滤和排序
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .authors(
        AuthorFetcher.$.firstName().lastName(),
        author -> author.firstName().asc()  // 按名字升序
    );
```

### 3. 递归抓取（自关联）

```java
// 递归抓取所有下级部门
Fetcher<Department> fetcher = DepartmentFetcher.$
    .name()
    .recursive(Department::subDepartments, 5);  // 最大深度5

// 无限深度递归（直到没有更多数据）
Fetcher<Comment> fetcher = CommentFetcher.$
    .content()
    .author(UserFetcher.$.name())
    .recursive(Comment::replies);  // 不指定深度，无限递归
```

## 高级用法

### 1. 条件抓取

```java
// 根据条件决定是否抓取某个属性
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .price(ctx -> ctx.detachedCriteria() > 10)  // 只在价格大于10时抓取
    .store(ctx -> ctx.detachedCriteria() != null);  // 只在有关联书店时抓取
```

### 2. 动态构建 Fetcher

```java
// 根据业务条件动态构建 Fetcher
public Fetcher<Book> buildFetcher(boolean needStore, boolean needAuthors) {
    BookFetcher.Builder builder = BookFetcher.$.name().edition();
    
    if (needStore) {
        builder.store(BookStoreFetcher.$.name());
    }
    
    if (needAuthors) {
        builder.authors(AuthorFetcher.$.firstName().lastName());
    }
    
    return builder.build();
}
```

## Fetcher 与 DTO 对比

| 特性 | Fetcher | DTO |
|------|---------|-----|
| 类型定义 | 无需定义，动态构建 | 需要预先定义类 |
| 类型安全 | 编译时检查 | 编译时检查 |
| 灵活性 | 极高，运行时决定形状 | 固定，需为每种形状定义新类 |
| 代码量 | 少 | 多（DTO 爆炸问题） |
| 维护成本 | 低 | 高 |

## 最佳实践

1. **按需抓取**：只抓取实际需要的字段，避免过度查询
2. **关联控制**：对关联对象的抓取要谨慎，避免深层嵌套导致性能问题
3. **复用 Fetcher**：对于常用的抓取模式，可以定义常量复用
4. **动态构建**：根据业务场景动态决定抓取内容，提高灵活性

```java
// 定义常用的 Fetcher 常量
public class Fetchers {
    public static final Fetcher<Book> BOOK_SIMPLE = BookFetcher.$.name().price();
    
    public static final Fetcher<Book> BOOK_WITH_STORE = BookFetcher.$
        .name()
        .price()
        .store(BookStoreFetcher.$.name());
    
    public static final Fetcher<Book> BOOK_FULL = BookFetcher.$
        .allScalarFields()
        .store(BookStoreFetcher.$.allScalarFields())
        .authors(AuthorFetcher.$.allScalarFields());
}
```
