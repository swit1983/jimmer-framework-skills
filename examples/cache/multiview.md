# 多视角缓存示例

多视角缓存用于**基于用户的数据隔离**，比如行级权限、租户隔离场景。

## 使用场景

- 多租户系统，不同租户看不到彼此数据
- 行级权限，不同用户角色看到的数据不同
- 任何需要根据上下文过滤缓存的场景

## 配置示例

```java
// 实体加上租户 id
@Entity
public interface Book {
    @Id
    long id();
    
    @TenantId
    Long tenantId();
    
    // ... 其他属性
}
```

## 自定义过滤器

```java
public class TenantFilter implements GlobalFilter<Book> {
    
    @Override
    public void filter(
        EntityTable<Book> table, 
        PredicateCollector collector
    ) {
        String currentTenantId = TenantContext.getCurrentTenantId();
        collector.where(
            table.tenantId().eq(Long.parseLong(currentTenantId))
        );
    }
}

// 注册过滤器
sqlClient = JSqlClient
    .builder()
    .addGlobalFilter(new TenantFilter())
    .build();
```

## 缓存自动隔离

启用多视角缓存后，Jimmer 自动：
- 按 `TenantId` 分区缓存
- 不同租户缓存互不干扰
- 数据过期只过期对应租户的缓存

## ⚠️ 注意事项

- 多视角缓存会增加内存占用
- 只对需要数据隔离的实体启用
- 对于 Redis 二级缓存，不同视角存储不同 key

