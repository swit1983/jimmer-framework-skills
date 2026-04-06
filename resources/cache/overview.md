# 缓存概述

Jimmer 提供三级缓存体系，自动维护缓存一致性。

## 三级缓存架构

```
┌─────────────────────────────────────────┐
│           计算缓存 (Computed Cache)        │
│         @Transient 计算属性缓存            │
└─────────────────────────────────────────┘
                   ↑
┌─────────────────────────────────────────┐
│           关联缓存 (Association Cache)     │
│      多对一/一对多/多对多关联集合缓存        │
└─────────────────────────────────────────┘
                   ↑
┌─────────────────────────────────────────┐
│           对象缓存 (Object Cache)          │
│        实体对象缓存 (ID -> Entity)          │
└─────────────────────────────────────────┘
```

| 缓存级别 | 用途 | 键 | 值 |
|---------|------|-----|-----|
| 对象缓存 | 缓存实体对象 | 表名 + ID | 实体对象 |
| 关联缓存 | 缓存关联关系 | 源表名 + 源ID + 关联属性名 | 目标ID列表 |
| 计算缓存 | 缓存计算属性 | 表名 + ID + 属性名 | 计算结果 |

## 启用缓存

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.babyfish.jimmer</groupId>
    <artifactId>jimmer-spring-boot-starter</artifactId>
    <version>0.8.0</version>
</dependency>

<!-- 选择缓存实现：Caffeine（本地）或 Redis（分布式） -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

### 2. 配置缓存

```java
@Configuration
public class CacheConfig {
    
    // 本地缓存（单机）
    @Bean
    public CacheFactory cacheFactory() {
        return new CaffeineBinder(
            1024,  // 对象缓存最大数量
            Duration.ofMinutes(10)  // 过期时间
        );
    }
    
    // 分布式缓存（集群）
    @Bean
    public CacheFactory redisCacheFactory(RedisTemplate<String, byte[]> redisTemplate) {
        return new RedisCacheBinder(redisTemplate);
    }
}
```

### 3. 在实体中启用缓存

```java
@Entity
public interface Book {
    
    @Id
    long id();
    
    String name();
    
    BigDecimal price();
    
    // 多对一关联自动使用关联缓存
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    BookStore store();
    
    // 多对多关联自动使用关联缓存
    @ManyToMany
    List<Author> authors();
}
```

## 缓存一致性

Jimmer 自动维护缓存一致性：

```java
// 修改数据
Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);
    draft.setName("New Name");  // 修改 name
});

sqlClient.save(book);
// 自动失效：Book-1 的对象缓存
// 自动失效：所有包含 Book-1 的关联缓存
```

### 缓存失效传播

```
修改 Book-1 的 name:
    ↓
失效 Book-1 的对象缓存
    ↓
查找所有与 Book 关联的实体:
    - Author 通过 books 关联 Book
    - BookStore 通过 books 关联 Book
    ↓
失效所有相关关联缓存:
    - Author.books 中包含 Book-1 的条目
    - BookStore.books 中包含 Book-1 的条目
```

## 多视角缓存（高级）

不同用户看到不同的缓存视图：

```java
// 用户 A 的查询（只能看到 public 数据）
sqlClient
    .createQuery(table)
    .forUser("user-a")  // 多视角缓存标识
    .where(table.visibility().eq("public"))
    .select(table)
    .execute();

// 用户 B 的查询（能看到 public + 自己的 private 数据）
sqlClient
    .createQuery(table)
    .forUser("user-b")
    .where(
        table.visibility().eq("public")
            .or(table.ownerId().eq("user-b"))
    )
    .select(table)
    .execute();
```

## 监控与调优

```java
// 缓存统计
CacheManager cacheManager = ...;
Cache cache = cacheManager.getCache("Book");

System.out.println("命中次数: " + cache.getHitCount());
System.out.println("未命中次数: " + cache.getMissCount());
System.out.println("命中率: " + cache.getHitRate());
```

## 注意事项

| 注意点 | 建议 |
|--------|------|
| 缓存粒度 | 对象缓存粒度最细，但缓存数量多；需合理设置上限 |
| 过期策略 | 建议设置合理的 TTL，避免内存溢出 |
| 序列化 | 使用分布式缓存时，确保实体可序列化 |
| 缓存穿透 | 对不存在的数据做空值缓存 |
| 缓存雪崩 | 设置随机过期时间，避免同时失效 |
