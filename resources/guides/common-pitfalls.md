# Jimmer 常见陷阱

使用 Jimmer 时容易出错的地方，帮助避免踩坑。

## 1. 实体必须是接口，不能用类

**❌ 错误：**
```java
@Entity
public class Book {  // 错误：用了 class
    @Id
    private Long id;
    private String name;
    // getter/setter...
}
```

**✅ 正确：**
```java
@Entity
public interface Book {  // 正确：用 interface
    @Id
    Long id();
    String name();
}
```

---

## 2. Fetcher 抓取自身导致无限循环

**❌ 错误：**
```java
// Book 有 authors，Author 有 books，循环引用
BookFetcher fetcher = BookFetcher.$
    .name()
    .authors(author -> author
        .firstName()
        .books(book -> book  // 又抓 books！
            .name()
            .authors(...)  // 无限循环！
        )
    );
```

**✅ 正确：**
```java
// 控制深度，避免循环
BookFetcher fetcher = BookFetcher.$
    .name()
    .authors(author -> author
        .firstName()
        // 不抓 books，或者只抓简单字段
    );

// 或者双向抓取时限制深度
AuthorFetcher authorFetcher = AuthorFetcher.$
    .firstName()
    .books(book -> book
        .name()
        // 不抓 authors
    );
```

---

## 3. Input DTO 的 null vs 未设置

**❌ 错误理解：**
```java
// 前端请求：{ "id": 1, "name": "New Name" }
// 没有传 price 字段

BookInput input = parseJson(json);
// 误以为 price 保持原值

sqlClient.save(input);
// 结果：price 被设为 null！
```

**✅ 正确理解：**
```java
// Input DTO 会区分 "未设置" 和 "设置为 null"
// 不传字段 = 未设置 = 不修改
// 传 null = 清除字段 = 设为 null

// 使用 BookInput 时要注意：
BookInput input = BookInput.builder()
    .id(1L)
    .name("New Name")
    // price 不设置，表示不修改
    .build();

// 或者使用 toBuilder 修改现有对象
BookInput updated = existingInput.toBuilder()
    .name("Updated Name")
    .build();
```

---

## 4. 保存时误解脱钩行为

**❌ 错误：**
```java
// 想删除作者与书的关联，但保留作者
Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);
    draft.setAuthors(Collections.emptyList());  // 清空作者
});

sqlClient.save(book);
// 结果：作者还存在，但与书脱钩（中间表记录被删除）
// 但如果 Book 是 Author 的拥有方，可能有意外行为
```

**✅ 正确：**
```java
// 明确控制脱钩行为
sqlClient.saveCommand(book)
    // 脱钩时对关联对象的操作
    .setDissociateAction(BookProps.AUTHORS, DissociateAction.SET_NULL)
    // SET_NULL: 将 Author.bookId 设为 null
    // DELETE: 删除 Author
    // NONE: 报错，不允许脱钩
    .execute();
```

---

## 5. 关联查询导致 N+1

**❌ 错误：**
```java
// 先查主表
List<Book> books = sqlClient
    .createQuery(table)
    .select(table)
    .execute();

// 再循环查关联（N+1！）
for (Book book : books) {
    BookStore store = sqlClient
        .createQuery(storeTable)
        .where(storeTable.id().eq(book.storeId()))
        .select(storeTable)
        .fetchOne();
    // 处理 store...
}
// 查询次数：1 + N 次
```

**✅ 正确：**
```java
// 使用 Fetcher 一次性抓取
List<Book> books = sqlClient
    .createQuery(table)
    .select(table.fetch(
        BookFetcher.$
            .name()
            .price()
            .store(BookStoreFetcher.$.name())  // 一起抓取
    ))
    .execute();
// 查询次数：2 次（主表 + 关联表）
```

---

## 6. 忘记启用缓存导致性能问题

**❌ 问题：**
```java
// 每个查询都走数据库
List<Book> books = sqlClient
    .createQuery(table)
    .select(table)
    .execute();
// 频繁查询相同数据，但每次都查数据库
```

**✅ 正确：**
```java
// 配置缓存（见缓存文档）
// Jimmer 自动处理缓存一致性

// 业务代码无变化，缓存自动生效
List<Book> books = sqlClient
    .createQuery(table)
    .select(table)
    .execute();
```

---

## 7. Spring Boot 启动失败：找不到实体

**❌ 问题：**
```
Error creating bean with name 'sqlClient': 
Could not find entity class: com.example.Book
```

**✅ 解决：**
```java
// 确保实体是 public interface
@Entity
public interface Book { ... }

// 确保包被扫描
@SpringBootApplication
@EnableJimmerRepositories(basePackages = "com.example")
public class Application { ... }
```

---

## 总结检查清单

| 检查项 | 状态 |
|--------|------|
| 实体是 `interface` 不是 `class` | ☐ |
| Fetcher 没有循环引用 | ☐ |
| Input DTO 区分 null vs 未设置 | ☐ |
| 保存时理解脱钩行为 | ☐ |
| 避免 N+1，使用 Fetcher | ☐ |
| 包路径被正确扫描 | ☐ |
