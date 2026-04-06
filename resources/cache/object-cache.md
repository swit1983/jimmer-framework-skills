# 对象缓存

对象缓存是最简单最基础的缓存类型：**把 id 映射为实体对象**。

## 启用对象缓存

示例配置（Spring Boot）：

```java
@Bean
public CacheFactory cacheFactory(
        RedisConnectionFactory connectionFactory,
        ObjectMapper objectMapper
) {
    return new CacheFactory() {
        @Override
        public Cache<?, ?> createObjectCache(@NotNull ImmutableType type) {
            // 不缓存某些实体
            if (type.getJavaClass() == SomeUncachedEntity.class) {
                return null;
            }
            
            // 多级缓存：一级 Caffeine，二级 Redis
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
    };
}
```

## 使用对象缓存

有两种方式可以利用对象缓存：

### 1. 按 id 查询

```java
// 根据一批 id 查询实体
Map<Long, Book> bookMap = sqlClient.findMapByIds(
        Book.class,
        Arrays.asList(1L, 2L, 3L, 4L, 999L)
);
```

工作流程：
1. Jimmer 先从缓存查找 `Book-1`, `Book-2` ...
2. 缓存未命中的数据从数据库查询
3. 将查询结果放入缓存
4. 即使 id 不存在，也会缓存 `<null>` 防止缓存穿透

> 缓存过期前，重复查询直接从缓存返回，不会生成 SQL。

### 2. 对象抓取器关联查询

当使用 Fetcher 抓取关联对象的非 id 属性时，自动利用对象缓存：

```java
// 查询书籍，同时抓取书店名称
List<Book> books = sqlClient
        .createQuery(BookTable.$)
        .where(BookTable.$.name().like("GraphQL"))
        .select(BookFetcher.$
                .allScalarFields()
                .store(BookStoreFetcher.$
                        .name()
                ))
        .execute();
```

执行流程：
1. 第一条 SQL 查询书籍（聚合根），聚合根不缓存
2. 需要获取书店的名称，根据外键得到书店 id，先从对象缓存查找书店对象
3. 缓存未命中，批量查询数据库并放入缓存
4. 下次同样查询直接从缓存获取书店数据，不会生成第二条 SQL

## 缓存一致性

要启用自动缓存一致性，必须先启用[触发器](../../mutation/trigger.md)。

- **BinLog 触发器**：任何方式修改数据库（包括直接 SQL 修改）都能自动维护缓存一致性
- **Transaction 触发器**：只有通过 Jimmer API 修改数据库才能维护缓存一致性

修改数据后，Jimmer 自动删除缓存中对应的数据：

```
Delete data from redis: [BookStore-2]
```

下次查询会从数据库加载新值。
