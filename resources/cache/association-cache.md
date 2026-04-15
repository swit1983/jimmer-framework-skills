# 关联缓存

关联缓存：**把当前对象 ID 映射为关联对象 ID（或集合）**。

```
┌─────────────────────────────────────────┐
│  Association Cache                       │
│  Key: BookStore.books-1                  │
│  Value: [6,5,4,3,2,1,9,8,7]             │
│                                          │
│  Key: Book.store-7                       │
│  Value: 2                                │
│                                          │
│  Key: Book.authors-10                    │
│  Value: [1,2]                            │
└─────────────────────────────────────────┘
```

## 什么时候需要关联缓存

| 场景 | 是否需要 | 说明 |
|------|----------|------|
| 一对一/多对一 基于真实外键 | 不需要 | 外键本身就是关联对象 ID，无需缓存 |
| 一对一/多对一 反向映射（mappedBy） | 需要 | |
| 一对一/多对一 伪外键 | 需要 | 伪外键没有数据库约束，需要缓存过滤合法 ID |
| 一对一/多对一 基于中间表 | 需要 | |
| 一对多 | always | |
| 多对多 | always | |

## 启用关联缓存

```java
@Bean
public CacheFactory cacheFactory(
        RedisConnectionFactory connectionFactory,
        ObjectMapper objectMapper
) {
    return new CacheFactory() {

        // ... createObjectCache 省略 ...

        // 一对一 / 多对一：当前对象 ID → 关联对象 ID
        @Override
        public Cache<?, ?> createAssociatedIdCache(@NotNull ImmutableProp prop) {
            return createPropCache(prop, Duration.ofMinutes(10), Duration.ofHours(10));
        }

        // 一对多 / 多对多：当前对象 ID → 关联对象 ID 集合
        @Override
        public Cache<?, List<?>> createAssociatedIdListCache(@NotNull ImmutableProp prop) {
            return createPropCache(prop, Duration.ofMinutes(5), Duration.ofHours(5));
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

⚠️ 如果对某个关联属性启用关联缓存，必须也对关联对象的类型启用对象缓存，否则会抛出异常。

## 集合关联默认排序

集合关联如需排序且要利用关联缓存，需在实体注解中指定 `orderedProps`：

```java
@Entity
public interface BookStore {
    @OneToMany(
        mappedBy = "store",
        orderedProps = {
            @OrderedProp("name"),
            @OrderedProp(value = "edition", desc = true)
        }
    )
    List<Book> books();
}

@Entity
public interface Book {
    @ManyToMany(
        orderedProps = {
            @OrderedProp("firstName"),
            @OrderedProp("lastName")
        }
    )
    List<Author> authors();
}
```

> 如果使用 Fetcher 字段级过滤排序，会导致无法使用关联缓存。推荐在实体级别配置默认排序。

## 使用示例

一对多 `BookStore.books`：

```java
List<BookStore> stores = sqlClient
        .createQuery(BookStoreTable.$)
        .select(BookStoreFetcher.$
                .allScalarFields()
                .books(BookFetcher.$
                        .allScalarFields()
                ))
        .execute();
```

执行流程：

1. **查询聚合根** BookStore。聚合根对象不缓存
2. 根据每个 BookStore ID，从关联缓存查找 `BookStore.books-{id}` → 得到 Book ID 列表
   - 缓存未命中 → 从数据库查询并放入缓存
   - 缓存命中 → 直接得到 ID 列表，不需要 SQL
3. 根据 ID 列表从**对象缓存**获取 Book 对象
   - 对象缓存未命中 → 从数据库查询并放入缓存

> 缓存过期前，第二次执行不会产生查询 books 的 SQL。

多对多 `Book.authors`：

```java
List<Book> books = sqlClient
        .createQuery(BookTable.$)
        .select(BookFetcher.$
                .allScalarFields()
                .authors(AuthorFetcher.$
                        .allScalarFields()
                ))
        .execute();
```

执行流程同上，只不过关联缓存的 key 是 `Book.authors-{bookId}` → `[authorId1, authorId2, ...]`

## 缓存一致性

要使用自动一致性，必须启用[触发器](../../mutation/trigger.md)。

Jimmer 自动维护一致性：

**修改 Book.store 外键**（从 store=1 改为 store=2）：

```sql
update BOOK set STORE_ID = 2 where ID = 7;
```

自动删除：

| 缓存 Key | 说明 |
|---------|------|
| `Book-7` | 对象缓存 |
| `Book.store-7` | 关联缓存 |
| `BookStore.books-1` | 修改前旧 ID=1 的一对多关联缓存 |
| `BookStore.books-2` | 修改后新 ID=2 的一对多关联缓存 |

**多对多插入 (Book-10, Author-3)**：

自动删除：

| 缓存 Key | 说明 |
|---------|------|
| `Book.authors-10` | Book 侧关联缓存 |
| `Author.books-3` | Author 侧关联缓存 |

## 逻辑删除

如果关联对象支持逻辑删除，默认仍然支持关联缓存。但如果查询时忽略/反转了逻辑删除过滤器，关联缓存会被忽略。
