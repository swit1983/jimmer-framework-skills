# 全局过滤器

全局过滤器（Global Filter）用于自动为所有查询添加过滤条件，常用于多租户、逻辑删除、权限控制等场景。

## 基本概念

过滤器会在每次查询时自动应用，无需在业务代码中重复编写过滤条件。

```java
// 定义过滤器
@Component
public class TenantFilter implements Filter<YourEntity> {
    
    @Override
    public void filter(FilterArgs<YourEntity> args) {
        // 所有查询都会自动添加这个条件
        args.where(args.getTable().tenantId().eq(currentTenantId()));
    }
}
```

## 实现 Filter 接口

```java
@Component
public class LogicalDeletedFilter implements Filter<Book> {
    
    @Override
    public void filter(FilterArgs<Book> args) {
        // 过滤逻辑删除的数据
        args.where(args.getTable().deleted().eq(false));
    }
    
    @Override
    public boolean isAffectedBy(EntityEvent<?> event) {
        // 当 deleted 字段变化时，相关缓存需要刷新
        return event.isChanged(BookProps.DELETED);
    }
}
```

```kotlin
@Component
class LogicalDeletedFilter : KFilter<Book> {
    
    override fun filter(args: KFilterArgs<Book>) {
        args.where(args.table.deleted eq false)
    }
    
    override fun isAffectedBy(event: EntityEvent<*>): Boolean {
        return event.isChanged(BookProps.DELETED)
    }
}
```

## 禁用过滤器

某些特殊场景需要临时禁用过滤器：

```java
// Java: 在查询中禁用过滤器
List<Book> books = sqlClient
    .createQuery(table)
    .disableFilter(LogicalDeletedFilter.class)  // 禁用逻辑删除过滤
    .where(...)
    .select(table)
    .execute();
```

```kotlin
// Kotlin
val books = sqlClient
    .createQuery(Book::class) {
        disableFilter(LogicalDeletedFilter::class)
        where(...)
        select(table)
    }
    .execute()
```

## 参数化过滤器

需要动态参数的过滤器：

```java
@Component
public class TenantFilter implements Filter<BusinessEntity> {
    
    private final TenantContext tenantContext;
    
    public TenantFilter(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }
    
    @Override
    public void filter(FilterArgs<BusinessEntity> args) {
        Long tenantId = tenantContext.getCurrentTenantId();
        if (tenantId != null) {
            args.where(args.getTable().tenantId().eq(tenantId));
        }
    }
}
```

## 常见过滤器场景

### 1. 逻辑删除过滤

```java
@Component
public class LogicalDeletedFilter implements Filter<SoftDeleteEntity> {
    @Override
    public void filter(FilterArgs<SoftDeleteEntity> args) {
        args.where(args.getTable().deleted().eq(false));
    }
}
```

### 2. 多租户过滤

```java
@Component
public class TenantIsolationFilter implements Filter<TenantEntity> {
    @Override
    public void filter(FilterArgs<TenantEntity> args) {
        args.where(
            args.getTable().tenantId().eq(
                TenantContextHolder.getTenantId()
            )
        );
    }
}
```

### 3. 权限范围过滤

```java
@Component
public class DataScopeFilter implements Filter<DataEntity> {
    @Override
    public void filter(FilterArgs<DataEntity> args) {
        Set<Long> allowedIds = PermissionService.getAllowedDataIds();
        if (allowedIds != null && !allowedIds.isEmpty()) {
            args.where(args.getTable().id().in(allowedIds));
        }
    }
}
```

## 过滤器执行顺序

多个过滤器按 Spring 的 `@Order` 注解顺序执行：

```java
@Component
@Order(1)  // 先执行
public class TenantFilter implements Filter<...> { ... }

@Component
@Order(2)  // 后执行
public class PermissionFilter implements Filter<...> { ... }
```

## 注意事项

| 注意点 | 说明 |
|--------|------|
| 默认启用 | 过滤器定义后自动对所有查询生效 |
| 性能影响 | 过滤器会增加所有查询的条件，确保索引合理 |
| 禁用谨慎 | 禁用过滤器可能暴露不应见的数据，注意安全 |
| 缓存影响 | 修改 `isAffectedBy` 确保缓存一致性 |
