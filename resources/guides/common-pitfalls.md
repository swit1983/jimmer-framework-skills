# Jimmer 常见易错点汇总

> 本文档汇总了 Jimmer 框架开发中最常见、最容易踩坑的问题，帮助你避开这些陷阱。

---

## 目录

1. [实体与映射层](#1-实体与映射层)
2. [查询与对象抓取器](#2-查询与对象抓取器)
3. [保存与级联操作](#3-保存与级联操作)
4. [缓存配置](#4-缓存配置)
5. [DTO 与转换](#5-dto-与转换)
6. [Spring 集成](#6-spring-集成)

---

## 1. 实体与映射层

### ❌ 错误 1.1：在实体中使用基本类型而非包装类型

```java
// ❌ 错误：使用基本类型
@Entity
public interface Book {
    @Id
    long id();  // 基本类型，无法表示 null
    
    int price();  // 基本类型，默认值为 0
}

// ✅ 正确：使用包装类型
@Entity
public interface Book {
    @Id
    Long id();  // 包装类型，可以表示 null
    
    Integer price();  // 包装类型，默认值为 null
}
```

**原因：** Jimmer 实体是可空的，使用基本类型会导致默认值问题（如 int 默认为 0），无法区分"未设置"和"设置为 0"。

---

### ❌ 错误 1.2：忘记在多对多关系中指定 @JoinTable

```java
// ❌ 错误：没有指定 @JoinTable
@Entity
public interface Book {
    @ManyToMany
    List<Author> authors();  // 缺少 @JoinTable
}

// ✅ 正确：使用 @JoinTable 指定中间表
@Entity
public interface Book {
    @ManyToMany
    @JoinTable(
        name = "BOOK_AUTHOR_MAPPING",
        joinColumnName = "BOOK_ID",
        inverseJoinColumnName = "AUTHOR_ID"
    )
    List<Author> authors();
}
```

**原因：** Jimmer 需要知道多对多关系的中间表信息，否则无法正确生成 SQL。

---

### ❌ 错误 1.3：在计算属性中直接使用数据库查询

```java
// ❌ 错误：在 @Formula 中直接使用复杂查询
@Entity
public interface Book {
    @Formula(sql = "SELECT COUNT(*) FROM BOOK_COMMENT WHERE BOOK_ID = ${t.ID}")
    int commentCount();  // 这会导致性能问题
}

// ✅ 正确：使用关联缓存或手动查询
@Entity
public interface Book {
    @OneToMany(mappedBy = "book")
    List<BookComment> comments();
    
    // 在业务层计算，或使用关联缓存
}
```

**原因：** 复杂的子查询会导致性能问题，应该使用关联缓存或应用层计算。

---

## 2. 查询与对象抓取器

### ❌ 错误 2.1：在 Fetcher 中循环抓取深层关联导致 N+1

```java
// ❌ 错误：逐层抓取，可能导致 N+1
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .store(BookStoreFetcher.$.name())  // 第一层
    .authors(AuthorFetcher.$
        .firstName()
        .books(BookFetcher.$.name())  // 递归抓取，危险！
    );

// ✅ 正确：使用批量抓取，避免递归
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .store(BookStoreFetcher.$.name())
    .authors(AuthorFetcher.$.firstName().lastName());
// 需要作者的书籍时，单独查询
```

**原因：** 深层递归抓取会导致大量 SQL 查询（N+1 问题），应该使用批量抓取或分步查询。

---

### ❌ 错误 2.2：忘记在属性过滤器中处理 null 值

```java
// ❌ 错误：未处理 null 的过滤值
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .authors(
        AuthorFetcher.$.firstName(),
        filter -> {
            if (searchFirstName != null) {  // 忘记处理 null
                filter.firstName().eq(searchFirstName);
            }
        }
    );

// ✅ 正确：使用条件判断
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .authors(
        AuthorFetcher.$.firstName(),
        filter -> {
            filter.firstName().eqIf(searchFirstName);  // 自动处理 null
        }
    );
```

**原因：** 未处理的 null 值可能导致空指针或无效查询，应使用 `eqIf`、`ilikeIf` 等安全方法。

---

### ❌ 错误 2.3：混淆 select 和 fetch

```java
// ❌ 错误：在 select 中直接使用实体
List<Book> books = sqlClient
    .createQuery(book)
    .select(book)  // 这样会查询所有字段
    .execute();

// ✅ 正确：使用 fetch 指定需要的字段
List<Book> books = sqlClient
    .createQuery(book)
    .select(book.fetch(BookFetcher.$.name().price()))
    .execute();
```

**原因：** `select(table)` 会查询所有字段（包括关联），应使用 `fetch()` 精确控制查询内容。

---

## 3. 保存与级联操作

### ❌ 错误 3.1：混淆 SAVE 和 UPDATE 语义

```java
// ❌ 错误：使用 save 期望只更新指定字段
Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);  // 指定 ID
    draft.setPrice(new BigDecimal("99.00"));  // 只更新价格
});
sqlClient.save(book);  // 危险！这可能覆盖其他字段

// ✅ 正确：使用 update 语句精确控制
BookTable book = BookTable.$;
sqlClient
    .createUpdate(book)
    .set(book.price(), new BigDecimal("99.00"))
    .where(book.id().eq(1L))
    .execute();
```

**原因：** `save()` 默认使用 `UPSERT` 语义，可能覆盖未设置字段。使用 `UPDATE` 语句可精确控制更新内容。

---

### ❌ 错误 3.2：在级联保存中未正确设置双向关联

```java
// ❌ 错误：只设置单向关联
Book book = BookDraft.$.produce(draft -> {
    draft.setName("Java in Action");
    draft.setAuthors(Arrays.asList(
        AuthorDraft.$.produce(author -> {
            author.setFirstName("James");
            // 忘记设置 author.setBooks(...) 反向关联
        })
    ));
});

// ✅ 正确：Jimmer 自动维护双向关联
// 只需设置一方，Jimmer 会自动处理反向关联
Book book = BookDraft.$.produce(draft -> {
    draft.setName("Java in Action");
    draft.setAuthors(Arrays.asList(
        AuthorDraft.$.produce(author -> {
            author.setFirstName("James");
        })
    ));
});
```

**原因：** Jimmer 会自动维护双向关联，无需手动设置双方。只需设置一方即可。

---

### ❌ 错误 3.3：在脱钩操作中未考虑数据库约束

```java
// ❌ 错误：使用 DELETE 脱钩策略但未考虑外键约束
@Entity
public interface Book {
    @ManyToOne
    @OnDissociate(DissociateAction.DELETE)  // 删除时级联删除作者
    Author author();
}

// 当执行 dissociate 时，如果作者还有其他书，会违反外键约束

// ✅ 正确：根据业务场景选择脱钩策略
@Entity
public interface Book {
    @ManyToOne
    @OnDissociate(DissociateAction.SET_NULL)  // 脱钩时设为空
    Author author();
}
```

**原因：** 脱钩策略需要根据数据库约束和业务逻辑选择，避免违反外键约束。

---

## 4. 缓存配置

### ❌ 错误 4.1：在实体中忘记配置 @Id 导致缓存失效

```java
// ❌ 错误：忘记 @Id 注解
@Entity
public interface Book {
    Long id();  // 没有 @Id，缓存无法识别主键
    String name();
}

// ✅ 正确：使用 @Id 标记主键
@Entity
public interface Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id();
    String name();
}
```

**原因：** 缓存依赖实体主键进行识别，缺少 `@Id` 会导致缓存机制失效。

---

### ❌ 错误 4.2：配置多级缓存时未考虑一致性策略

```java
// ❌ 错误：只配置本地缓存，多实例时数据不一致
@Bean
public CacheFactory cacheFactory() {
    return new CaffeineCacheFactory();  // 仅本地缓存
}

// ✅ 正确：使用多级缓存 + 远程缓存保持一致性
@Bean
public CacheFactory cacheFactory(RedisTemplate<String, String> redisTemplate) {
    return new CacheFactory() {
        @Override
        public <K, V> Cache<K, V> create(ObjectMapper objectMapper, Type type) {
            // 一级：Caffeine 本地缓存
            Cache<K, V> localCache = new CaffeineCache<>();
            // 二级：Redis 远程缓存
            Cache<K, V> remoteCache = new RedisCache<>(redisTemplate, objectMapper);
            // 多级缓存组合
            return new MultiLevelCache<>(localCache, remoteCache);
        }
    };
}
```

**原因：** 多级缓存需要考虑一致性问题，本地缓存应与远程缓存配合使用。

---

## 5. DTO 与转换

### ❌ 错误 5.1：在 DTO 中直接暴露实体字段导致安全问题

```java
// ❌ 错误：直接暴露敏感字段
public interface BookDTO {
    String name();
    String secretKey();  // 敏感信息暴露！
    UserDTO owner();
}

// ✅ 正确：使用 DTO 映射控制暴露字段
@Dto(Book.class)
public interface BookDTO {
    String name();
    // secretKey 故意不映射，不暴露
    
    @Dto(Book.class, include = {"firstName", "lastName"})  // 只暴露部分用户信息
    UserDTO owner();
}
```

**原因：** DTO 应用于控制数据暴露范围，直接映射所有实体字段可能导致敏感信息泄露。

---

### ❌ 错误 5.2：在转换时忘记处理 null 值

```java
// ❌ 错误：未处理 null 的转换
BookDTO dto = BookMapper.INSTANCE.toDTO(book);  // book 可能为 null
String name = dto.name();  // 空指针异常！

// ✅ 正确：使用安全转换
BookDTO dto = book != null ? BookMapper.INSTANCE.toDTO(book) : null;
// 或使用 Optional
String name = Optional.ofNullable(book)
    .map(BookMapper.INSTANCE::toDTO)
    .map(BookDTO::name)
    .orElse("Unknown");
```

**原因：** 实体查询可能返回 null，转换时需要处理空值情况。

---

## 6. Spring 集成

### ❌ 错误 6.1：忘记配置 @EnableJimmerRepositories

```java
// ❌ 错误：没有启用 Jimmer 仓库
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// ✅ 正确：启用 Jimmer 仓库支持
@SpringBootApplication
@EnableJimmerRepositories  // 关键注解！
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**原因：** 使用 Spring Data 风格的 Repository 需要启用 `@EnableJimmerRepositories`。

---

### ❌ 错误 6.2：在事务中混用不同数据源

```java
// ❌ 错误：在事务中混用主库和从库
@Service
public class BookService {
    
    @Transactional(readOnly = true)
    public Book getBook(Long id) {
        // 从库查询（readOnly=true）
        Book book = readOnlySqlClient.findById(Book.class, id);
        
        // 错误：在 readOnly 事务中尝试写入主库
        BookDraft draft = BookDraft.$.produce(book, b -> {
            b.setPrice(b.price().add(new BigDecimal("10")));
        });
        return writeSqlClient.save(draft).getModifiedEntity();  // 事务冲突！
    }
}

// ✅ 正确：分离读写操作
@Service
public class BookService {
    
    @Transactional(readOnly = true)
    public Book getBook(Long id) {
        return readOnlySqlClient.findById(Book.class, id);
    }
    
    @Transactional  // 读写事务
    public Book updatePrice(Long id, BigDecimal newPrice) {
        Book book = writeSqlClient.findById(Book.class, id);
        BookDraft draft = BookDraft.$.produce(book, b -> {
            b.setPrice(newPrice);
        });
        return writeSqlClient.save(draft).getModifiedEntity();
    }
}
```

**原因：** 在 `readOnly` 事务中尝试写入会导致数据源冲突和事务异常。

---

## 总结

| 类别 | 常见错误数 | 核心原则 |
|------|-----------|----------|
| 实体与映射层 | 3 | 使用包装类型，配置正确的关联和脱钩策略 |
| 查询与对象抓取器 | 3 | 避免深层递归，正确处理 null 值 |
| 保存与级联操作 | 3 | 区分 SAVE/UPDATE，理解级联和脱钩 |
| 缓存配置 | 2 | 使用包装类型主键，多级缓存考虑一致性 |
| DTO 与转换 | 2 | 控制暴露字段，处理 null 值 |
| Spring 集成 | 2 | 启用仓库支持，分离读写事务 |

**记住：Jimmer 的设计理念是 "约定优于配置"，但理解这些约定背后的原理，能帮助你避开大部分坑。**