# 缓存概述

Jimmer 提供三级缓存体系，数据修改时自动删除受影响的缓存，并由触发器驱动保证缓存清理最终成功。

## 三级缓存架构

```
┌─────────────────────────────────────────┐
│           计算缓存 (Resolver Cache)        │
│   createResolverCache(prop)               │
│   ID → 复杂计算属性(@Transient)的计算值     │
└─────────────────────────────────────────┘
                   ↑
┌─────────────────────────────────────────┐
│           关联缓存 (Association Cache)     │
│   createAssociatedIdCache(prop)           │
│   createAssociatedIdListCache(prop)       │
│   ID → 关联对象ID(或ID集合)                │
└─────────────────────────────────────────┘
                   ↑
┌─────────────────────────────────────────┐
│           对象缓存 (Object Cache)          │
│   createObjectCache(type)                 │
│   ID → 实体对象                            │
└─────────────────────────────────────────┘
```

| 缓存级别 | CacheFactory 方法 | 键 | 值 |
|---------|-------------------|-----|-----|
| 对象缓存 | `createObjectCache(type)` | 类型名 + ID | 实体对象 |
| 关联缓存 | `createAssociatedIdCache(prop)` / `createAssociatedIdListCache(prop)` | 属性名 + 源ID | 关联目标ID(或集合) |
| 计算缓存 | `createResolverCache(prop)` | 属性名 + ID | 计算结果 |

## 启用缓存

### CacheFactory 接口

实现 `CacheFactory`(Java) 或 `KCacheFactory`(Kotlin) 接口：

```java
public interface CacheFactory {
    @Nullable
    default Cache<?, ?> createObjectCache(@NotNull ImmutableType type) { return null; }
    @Nullable
    default Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) { return null; }
    @Nullable
    default Cache<?, List<?>> createAssociatedIdListCache(@NotNull ImmutableProp prop) { return null; }
    @Nullable
    default Cache<?, ?> createResolverCache(@NotNull ImmutableProp prop) { return null; }
}
```

- 返回 `null` → 不启用该缓存
- 返回 `Cache` → 启用该缓存

### 多级缓存架构

使用 `ChainCacheBuilder` 构建多级缓存（通常两级：Caffeine 本地 + Redis 远程）：

```java
@Bean
public CacheFactory cacheFactory(
        RedisConnectionFactory connectionFactory,
        ObjectMapper objectMapper
) {
    return new CacheFactory() {
        @Override
        public Cache<?, ?> createObjectCache(@NotNull ImmutableType type) {
            return new ChainCacheBuilder<>()
                    .add(CaffeineValueBinder
                            .forObject(type)
                            .maximumSize(1024)
                            .duration(Duration.ofHours(1))
                            .build())
                    .add(RedisValueBinder
                            .forObject(type)
                            .redis(connectionFactory)
                            .objectMapper(objectMapper)
                            .duration(Duration.ofHours(24))
                            .build())
                    .build();
        }

        @Override
        public Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) {
            return createPropCache(prop, Duration.ofMinutes(10), Duration.ofHours(10));
        }

        @Override
        public Cache<?, List<?>> createAssociatedIdListCache(@NotNull ImmutableProp prop) {
            return createPropCache(prop, Duration.ofMinutes(5), Duration.ofHours(5));
        }

        @Override
        public Cache<?, ?> createResolverCache(@NotNull ImmutableProp prop) {
            return createPropCache(prop, Duration.ofHours(1), Duration.ofHours(24));
        }

        private <K, V> Cache<K, V> createPropCache(
                ImmutableProp prop,
                Duration caffeineDuration,
                Duration redisDuration
        ) {
            return new ChainCacheBuilder<>()
                    .add(CaffeineValueBinder
                            .forProp(prop)
                            .maximumSize(512)
                            .duration(caffeineDuration)
                            .build())
                    .add(RedisValueBinder
                            .forProp(prop)
                            .redis(connectionFactory)
                            .objectMapper(objectMapper)
                            .duration(redisDuration)
                            .build())
                    .build();
        }
    };
}
```

### Binder 类型

| Jimmer内置适配类 | 实现接口 | 支持多视图缓存 |
| --- | --- | --- |
| `CaffeineBinder` | `LoadingBinder` | 否 |
| `RedisValueBinder` | `SimpleBinder` | 否 |
| `RedisHashBinder` | `SimpleBinder.Parameterized` | 是 |

- `LoadingBinder`: 适配具备自动加载能力的缓存（如 Caffeine）
- `SimpleBinder`: 适配不具备自动加载能力的缓存（如 Redis）
- `SimpleBinder.Parameterized`: 用于多视图缓存

### 注册 CacheFactory

**SpringBoot**: 让 `CacheFactory` 受 Spring 托管（`@Bean`）即可。

**底层API**:

```java
JSqlClient sqlClient = JSqlClient.newBuilder()
        .setCacheFactory(new CacheFactory() { ... })
        .build();
```

## 缓存一致性

Jimmer 的缓存一致性由[触发器](../save/save-command.md)驱动，分两种：

### BinLog 触发器（推荐）

`trigger-type` 为 `BINLOG_ONLY` 或 `BOTH` 时使用。

- 开发人员响应消息队列通知，调用 Jimmer 的 `BinLog` API
- 以 Kafka 为例：调用 `BinLog` API 成功后才提交消费进度 → 缓存清理最终成功
- **任何方式修改数据库（包括直接 SQL）都能自动维护缓存一致性**

### Transaction 触发器

`trigger-type` 为 `TRANSACTION_ONLY` 时使用。

- 只有通过 Jimmer API 修改数据库才会触发
- 缓存删除操作被延迟存入 `JIMMER_TRANS_CACHE_OPERATOR` 表（与业务表同事务）
- 事务提交后立即执行 `Flush` 操作，周期重试保证最终成功
- 间隔由 `jimmer.transaction-cache-operator-fixed-delay` 配置（毫秒），默认 `5000`
- **注意**：不要使用 `DefaultDialect`，需明确指定数据库方言

## 多视图缓存（高级）

不同客户端看到不同的缓存数据（通常由权限系统/全局过滤器导致）。

- 关联缓存和计算缓存可以多视图化，对象缓存不行
- 必须实现 `CacheableFilter` 接口（而非普通 `Filter`）
- SubKey 是 `SortedMap<String, Object>` 经 JSON 序列化后的字符串
- 详见多视图缓存文档

## 各级缓存详情

| 缓存类型 | 说明 | 详情文档 |
|---------|------|---------|
| 对象缓存 | ID → 实体对象 | [object-cache.md](object-cache.md) |
| 关联缓存 | ID → 关联对象ID(集合) | [association-cache.md](association-cache.md) |
| 计算缓存 | ID → @Transient 计算值 | 见 docs/cache/cache-type/calculation.md |
