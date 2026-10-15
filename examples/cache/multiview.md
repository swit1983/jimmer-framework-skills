# 多视角缓存示例

多视角缓存用于**基于用户的数据隔离**，比如行级权限、租户隔离场景。

## 使用场景

- 多租户系统，不同租户看不到彼此数据
- 行级权限，不同用户角色看到的数据不同
- 任何需要根据上下文过滤缓存的场景

## 核心机制

多视角缓存按「当前生效的**过滤器集合**」划分缓存视角。只要查询受 `Filter` 影响，
不同视角的数据就天然隔离，无需按租户手动分区。

> ⚠️ Jimmer **没有**内置的 `@TenantId` 注解，租户标识就是一个普通标量属性；
> 隔离能力来自 `Filter`，而非注解。

## 1. 抽取公共父接口

推荐用 `@MappedSuperclass` 收敛可过滤的公共属性，以便后续得到强类型的 `Props`：

```java
@MappedSuperclass
public interface TenantAware {
    String tenant();
}

@Entity
public interface Book extends TenantAware {
    @Id
    long id();

    String name();
}
```

APT/KSP 会为 `TenantAware` 生成 `TenantAwareProps`。

## 2. 定义过滤器

```java
import org.babyfish.jimmer.sql.filter.Filter;
import org.babyfish.jimmer.sql.filter.FilterArgs;

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

弱类型写法（不借助 `MappedSuperclass` 时）：

```java
public class TenantFilter implements Filter<Props> {
    @Override
    public void filter(FilterArgs<Props> args) {
        args.where(args.getTable().<String>get("tenant").eq(tenant));
    }
}
```

> `Filter<P extends Props>` 只有 `void filter(FilterArgs<P> args)` 一个方法；
> `FilterArgs` 继承 `Sortable`，因此可直接调用 `where(...)`。

## 3. 注册过滤器

使用 Spring Boot Starter 时，把过滤器声明为 `@Component` 即可自动装配。
脱离 Spring 手动构建时：

```java
JSqlClient sqlClient = JSqlClient
    .newBuilder()
    .addFilters(new TenantFilter(tenantProvider))   // 注意：不是 addGlobalFilter
    .build();
```

> ⚠️ 构造入口是 `JSqlClient.newBuilder()`（静态方法），注册方法是 `addFilters`。

## 4. 缓存自动隔离

启用多视角缓存后，Jimmer 会：

- 按当前生效的过滤器组合分别建立缓存视角
- 不同租户/用户之间的缓存互不干扰
- 数据变更时只失效受影响视角的缓存

## ⚠️ 注意事项

- 多视角缓存会增加内存占用
- 只对确实需要数据隔离的实体启用
- 使用 Redis 二级缓存时，不同视角会落到不同的 key

## 相关文档

- 官方：全局过滤器 → `docs/query/global-filter/user-filter.md`
- 官方：多视角缓存 → `docs/cache/multiview-cache.md`
