# 计算属性

Jimmer 提供两种计算属性方式：`@Formula` 简单计算和 `@Transient` 复杂计算。

## @Formula 简单计算

适用于简单、快速的计算场景。

### 基于 Java/Kotlin 的计算

```java
@Entity
public interface Author {
    String firstName();
    String lastName();

    @Formula(dependencies = {"firstName", "lastName"})
    default String fullName() {
        return firstName() + ' ' + lastName();
    }
}
```

```kotlin
@Entity
interface Author {
    val firstName: String
    val lastName: String

    @Formula(dependencies = ["firstName", "lastName"])
    val fullName: String
        get() = "$firstName $lastName"
}
```

**特点：**
- 属性使用 `default` 实现（Java）或 getter（Kotlin）
- `@Formula` 必须指定 `dependencies` 表示依赖的属性
- 查询时自动加载依赖字段，计算结果不存储到数据库

### 基于 SQL 的计算

```java
@Entity
public interface Author {
    @Formula(sql = "concat(%alias.FIRST_NAME, ' ', %alias.LAST_NAME)")
    String fullName();
}
```

```kotlin
@Entity
interface Author {
    @Formula(sql = "concat(%alias.FIRST_NAME, ' ', %alias.LAST_NAME)")
    val fullName: String
}
```

**特点：**
- 属性是抽象的
- `%alias` 是特殊占位符，会被替换为实际表别名
- 生成的 SQL 直接包含该表达式
- **可用于 SQL DSL 查询条件**（这是相比 Java/Kotlin 计算的优势）

### 两种计算方式对比

| 特性 | Java/Kotlin 计算 | SQL 计算 |
|------|------------------|----------|
| 能否用于 DSL 条件 | ❌ 不能 | ✅ 能 |
| 数据冗余 | 无 | 可能（同时查依赖字段） |
| 复杂计算 | 适合 | 适合简单表达式 |

---

## @Transient 复杂计算

适用于复杂计算场景，需要自定义计算逻辑。

### 实现步骤

**1. 定义计算属性**

```java
@Entity
public interface BookStore {
    @Transient(BookStoreAvgPriceResolver.class)
    BigDecimal avgPrice();
}
```

```kotlin
@Entity
interface BookStore {
    @Transient(BookStoreAvgPriceResolver::class)
    val avgPrice: BigDecimal
}
```

**2. 实现 Resolver**

```java
@Component
public class BookStoreAvgPriceResolver implements TransientResolver<Long, BigDecimal> {
    
    private final JSqlClient sqlClient;
    
    public BookStoreAvgPriceResolver(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }
    
    @Override
    public Map<Long, BigDecimal> resolve(Collection<Long> ids) {
        return Tuple2.toMap(
            sqlClient
                .createQuery(table)
                .where(table.storeId().in(ids))
                .groupBy(table.storeId())
                .select(
                    table.storeId(),
                    table.price().avg()
                )
                .execute()
        );
    }
    
    @Override
    public BigDecimal getDefaultValue() {
        return BigDecimal.ZERO;
    }
}
```

```kotlin
@Component
class BookStoreAvgPriceResolver(
    private val sqlClient: KSqlClient
) : KTransientResolver<Long, BigDecimal> {
    
    override fun resolve(ids: Collection<Long>): Map<Long, BigDecimal> =
        sqlClient
            .createQuery(Book::class) {
                where(table.store.id valueIn ids)
                groupBy(table.store.id)
                select(
                    table.store.id,
                    avg(table.price).asNonNull()
                )
            }
            .execute()
            .associateBy({ it._1 }) { it._2 }
    
    override fun getDefaultValue(): BigDecimal = BigDecimal.ZERO
}
```

### Resolver 关键点

| 点 | 说明 |
|---|------|
| 批量计算 | `resolve` 接收 `Collection<ID>`，返回 `Map<ID, Value>`，避免 N+1 |
| 默认值 | 通过 `getDefaultValue()` 为无计算结果的 ID 提供默认值 |
| 关联属性 | 计算属性可以是关联类型（如 `List<Book>`），只需将泛型改为 `List<Long>` |
| 多工程 | 如果实体和 Resolver 不在同一工程，使用 `@Transient(ref = "beanName")` |

### 关联型计算属性

当计算属性本身是关联类型时：

```java
@Entity
public interface BookStore {
    @Transient(BookStoreNewestBooksResolver.class)
    List<Book> newestBooks();
}
```

Resolver 实现：

```java
@Component
public class BookStoreNewestBooksResolver implements TransientResolver<Long, List<Long>> {
    @Override
    public Map<Long, List<Long>> resolve(Collection<Long> ids) {
        // 批量查询，返回 Map<StoreId, List<BookId>>
    }
    
    @Override
    public List<Long> getDefaultValue() {
        return Collections.emptyList();
    }
}
```

---

## 选择建议

| 场景 | 推荐方式 | 理由 |
|------|---------|------|
| 简单字段计算 | `@Formula` (Java/Kotlin) | 简洁，无数据库冗余 |
| 需要在 DSL 中过滤 | `@Formula` (SQL) | 可被 SQL DSL 使用 |
| 复杂业务逻辑计算 | `@Transient` | 灵活，可批量计算 |
| 关联数据聚合 | `@Transient` | 可实现关联型计算属性 |
