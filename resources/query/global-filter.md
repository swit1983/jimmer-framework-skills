# 全局过滤器

全局过滤器（Global Filter）用于自动为所有查询添加过滤条件，常用于多租户、逻辑删除、权限控制等场景。

## 基本概念

过滤器会在每次查询时自动应用，无需在业务代码中重复编写过滤条件。

```java
// 定义过滤器（Java 泛型参数为预编译器生成的 Props 类型）
@Component
public class TenantFilter implements Filter<TenantAwareProps> {

    @Override
    public void filter(FilterArgs<TenantAwareProps> args) {
        // 所有查询都会自动添加这个条件
        args.where(args.getTable().tenant().eq(tenantProvider.get()));
    }
}
```

## 实现 Filter 接口

```java
// Java: Filter 的泛型参数为预编译器自动生成的 Props 类型
@Component
public class TenantFilter implements Filter<TenantAwareProps> {

    private final TenantProvider tenantProvider;

    public TenantFilter(TenantProvider tenantProvider) {
        this.tenantProvider = tenantProvider;
    }

    @Override
    public void filter(FilterArgs<TenantAwareProps> args) {
        String tenant = tenantProvider.get();
        if (tenant != null) {
            args.where(args.getTable().tenant().eq(tenant));
        }
    }
}
```

```kotlin
// Kotlin: KFilter 的泛型参数为实体类型本身
@Component
class TenantFilter(
    private val tenantProvider: TenantProvider
) : KFilter<TenantAware> {

    override fun filter(args: KFilterArgs<TenantAware>) {
        tenantProvider.tenant?.let {
            args.where(args.table.tenant eq it)
        }
    }
}
```

> 逻辑删除过滤器是内置的，无需手动实现。使用 `@LogicalDeleted` 注解标注实体属性即可。

## 禁用过滤器

某些特殊场景需要临时禁用过滤器。通过 `sqlClient.filters()` 创建临时 SqlClient：

```java
// Java: 创建临时 SqlClient 禁用过滤器
JSqlClient tmpSqlClient = sqlClient.filters(it -> {
    it.disableByTypes(TenantFilter.class);
});
List<Book> books = tmpSqlClient
    .createQuery(table)
    .select(table)
    .execute();
```

```kotlin
// Kotlin
val tmpSqlClient = sqlClient.filters {
    disableByTypes(TenantFilter::class)
}
val books = tmpSqlClient
    .createQuery(Book::class) {
        select(table)
    }
    .execute()
```

> 对于逻辑删除过滤器，使用 `LogicalDeletedBehavior.IGNORED` 或 `LogicalDeletedBehavior.REVERSED`：
> ```java
> sqlClient.filters(cfg -> cfg.setBehavior(LogicalDeletedBehavior.IGNORED))
> ```

## 参数化过滤器

需要动态参数的过滤器：

```java
@Component
public class TenantFilter implements Filter<TenantAwareProps> {

    private final TenantProvider tenantProvider;

    public TenantFilter(TenantProvider tenantProvider) {
        this.tenantProvider = tenantProvider;
    }

    @Override
    public void filter(FilterArgs<TenantAwareProps> args) {
        String tenant = tenantProvider.get();
        if (tenant != null) {
            args.where(args.getTable().tenant().eq(tenant));
        }
    }
}
```

> 建议从实体中提取 `@MappedSuperclass` 抽象基类（如 `TenantAware`），
> 这样一个过滤器可以过滤多种实体类型，且支持多继承。

## 常见过滤器场景

### 1. 逻辑删除过滤（内置）

逻辑删除过滤器是 Jimmer 内置的，使用 `@LogicalDeleted` 注解标注实体属性即可自动生效，无需手动实现 Filter：

```java
@Entity
public interface Book {
    @LogicalDeleted("true")
    boolean isDeleted();
    // ...省略其他代码...
}
```

如需忽略或反转逻辑删除，使用 `LogicalDeletedBehavior`：
```java
sqlClient.filters(cfg -> cfg.setBehavior(LogicalDeletedBehavior.IGNORED));
```

### 2. 多租户过滤

```java
@Component
public class TenantFilter implements Filter<TenantAwareProps> {
    @Override
    public void filter(FilterArgs<TenantAwareProps> args) {
        String tenant = TenantContextHolder.getTenant();
        if (tenant != null) {
            args.where(args.getTable().tenant().eq(tenant));
        }
    }
}
```

### 3. 权限范围过滤

```java
@Component
public class DataScopeFilter implements Filter<DataEntityProps> {
    @Override
    public void filter(FilterArgs<DataEntityProps> args) {
        Set<Long> allowedIds = PermissionService.getAllowedDataIds();
        if (allowedIds != null && !allowedIds.isEmpty()) {
            args.where(args.getTable().id().in(allowedIds));
        }
    }
}
```

## 更多的过滤器接口

除了最基本的 `Filter`/`KFilter` 接口外，过滤器还可以实现以下接口：

| 接口 | 说明 |
|------|------|
| `CacheableFilter`/`KCacheableFilter` | 支持多视角缓存，使全局过滤器和关联缓存兼容 |
| `AssociationIntegrityAssuranceFilter` | 承诺只有遵循相同过滤规则的对象才有关联，允许非空多对一关联 |
| `ShardingFilter`/`KShardingFilter` | 保证按 id 查询也应用过滤器，配合 sharding-jdbc 使用 |

## 注意事项

| 注意点 | 说明 |
|--------|------|
| 默认启用 | 过滤器定义后自动对所有查询生效 |
| 性能影响 | 过滤器会增加所有查询的条件，确保索引合理 |
| 禁用谨慎 | 禁用过滤器可能暴露不应见的数据，注意安全 |
| 缓存影响 | 自定义过滤器会导致关联缓存失效，需实现 `CacheableFilter` 支持多视角缓存 |
