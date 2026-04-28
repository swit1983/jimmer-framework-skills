# Jimmer 常见易错点汇总

> 本文档基于 [Jimmer 官方 FAQ](https://jimmer.deno.dev/zh/faq) 整理，汇总了最常见的问题和易错点。

---

## 1. UnloadedException - 访问未赋值属性

### ❌ 错误示例

```java
// 假设 Book 实体有 name, edition, price 等属性
// 这里给 name 赋值字符串，给 edition 赋值 null，未给 price 赋值
Book book = BookDraft.$.produce(draft -> 
    draft.setName("Java入门").setEdition(null)
);

String name = book.name();      // ✅ name = "Java入门"
Integer edition = book.edition(); // ✅ edition = null
Double price = book.price();    // ❌ 抛出 UnloadedException！
```

### ✅ 正确理解

Jimmer 使用 `unloaded` 概念区分"没有值"和"值为 null"：

| 情况 | 含义 | 访问结果 |
|------|------|----------|
| `setEdition(null)` | 赋值为 null | 返回 null |
| 未调用 `setPrice(...)` | 未赋值 (unloaded) | 抛出 UnloadedException |

**核心原则**：Jimmer 的属性中，**null 就是有效值**，不表示"未赋值"。

---

## 2. 主动方 vs 从动方（mappedBy）

### ❌ 错误示例

```java
@Entity
public interface Book {
    @ManyToOne
    @OneToMany(mappedBy = "bookStore")  // ❌ 错误：@ManyToOne 不能用 mappedBy
    BookStore bookStore();
}
```

### ✅ 正确理解

| 角色 | 数据库层面 | 实体层面 | 特征 |
|------|-----------|----------|------|
| **主动方** | 表字段中包含外键 | 关联注解**不包含** mappedBy | 控制关联关系 |
| **从动方** | 表字段中不包含外键 | 关联注解**包含** mappedBy | 镜像配置 |

```java
// 主动方：book 表有 store_id 外键
@Entity
public interface Book {
    @ManyToOne  // 主动方，无 mappedBy
    BookStore bookStore();
}

// 从动方：book_store 表无外键
@Entity
public interface BookStore {
    @OneToMany(mappedBy = "bookStore")  // 从动方，有 mappedBy
    List<Book> books();
}
```

---

## 3. 不要在嵌套 lambda 中保存实体

### ❌ 错误示例

```java
@Test
void test() {
    Book book = BookDraft.$.produce(draft -> {
        BookDraft bookDraft = draft.setName("abc").setEdition(1);
        
        // ❌ 错误：在嵌套 lambda 中保存 draft 对象
        sqlClient.save(bookDraft);
        
        // ❌ 错误：同样不能在嵌套 lambda 中创建并保存其他实体
        BookStore bookStore = BookStoreDraft.$.produce(storeDraft -> 
            storeDraft.setName("def")
        );
        sqlClient.save(bookStore);
    });
}
```

**报错**：`java.lang.IllegalArgumentException: entity cannot be a draft object`

### ✅ 正确示例

```java
@Test
void test() {
    // 1. 先创建所有草稿对象
    Book book = BookDraft.$.produce(draft -> 
        draft.setName("abc").setEdition(1)
    );
    
    BookStore bookStore = BookStoreDraft.$.produce(draft -> 
        draft.setName("def")
    );
    
    // 2. 在 lambda 外部保存
    sqlClient.save(book);
    sqlClient.save(bookStore);
}
```

---

## 4. 逻辑删除标志位的约定

### ⚠️ 重要约定

| 值 | 含义 |
|----|------|
| **0** | 未删除（默认值） |
| **其他数字** | 已删除 |

**为什么不能反过来（1未删除，0已删除）？**

这是 Jimmer 源码中写死的约定，无法修改。

### ✅ 解决方案

如果需要反直觉的设计（1=未删除，0=已删除），可以使用枚举规避：

```java
@EnumType(EnumType.Strategy.ORDINAL)
public enum DeleteStatus {
    DELETED,    // 0
    ACTIVE      // 1
}

@Entity
public interface Book {
    @LogicalDeleted
    DeleteStatus status();
}
```

---

## 5. 假外键/过滤器/逻辑删除要求 Nullable

### ❌ 错误理解

```java
@Entity
public interface Book {
    // ❌ 错误：假外键不能设为 not null
    @ManyToOne
    @Column(nullable = false)  // 不能这样！
    BookStore store();
}
```

### ✅ 正确理解

以下情况关联对象**必须为 Nullable**：

| 场景 | 原因 |
|------|------|
| **假外键** | 即使外键字段 not null，也可能是非法悬挂值，无法确保关联对象存在 |
| **过滤器** | 过滤后可能没有匹配数据 |
| **逻辑删除** | 本质也是过滤器 |

```java
@Entity
public interface Book {
    @ManyToOne
    @Nullable  // ✅ 必须为 Nullable
    BookStore store();
}
```

---

## 6. bookStoreId() vs bookStore().id() 的区别

### 关键区别

| 写法 | 含义 | SQL 行为 |
|------|------|----------|
| `book.storeId()` | 当前表的**外键字段** | 不触发连表查询 |
| `book.store().id()` | 关联表的**主键** | 触发关联表查询 |

### Fetcher 中的特殊用法

如果希望获取外键值但不想触发过滤器：

```java
Fetcher<Book> fetcher = BookFetcher.$
    .name()
    .store(IdOnlyFetcherType);  // 只获取 ID，不走过滤器
```

---

## 7. 数据库校验报错：xxx not in table "xxx.null.book"

### 问题原因

数据库隔离使用的是 **catalog** 而非 **schema**。

### ✅ 解决方案

修改连接配置，使用 `catalog.schema.table` 格式：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mycatalog
    # 在实体或配置中指定 schema
```

或在实体中指定：

```java
@Entity
@Table(name = "book", schema = "public")  // 指定 schema
public interface Book {
    // ...
}
```

---

## 8. 怎么只更新部分属性

### ❌ 错误思路

寻找类似 JPA 的 `@DynamicUpdate` 注解。

### ✅ 正确做法：使用 Jimmer 的动态性

```java
// 只给想更新的属性赋值
Book book = BookDraft.$.produce(draft -> 
    draft.setId(1L).setName("Java入门")
);

// 这将执行：UPDATE book SET name = ? WHERE id = ?
sqlClient.update(book);
```

**说明**：只更新 `name`，其他字段不会被修改。

---

## 9. 新增数据后怎么返回全部属性

### ❌ 错误期望

```java
Book saved = sqlClient.save(book).getModifiedEntity();
// 期望 saved 包含全部属性（包括数据库生成的）
```

### ✅ 正确做法

**Jimmer 设计理念**：不执行额外 SQL，由用户决定执行哪些 SQL。

`save()` 后的 `getModifiedEntity()` 只会在旧实体上**新增一个 id**。

如需返回所有属性，**自行查询**：

```java
// 1. 保存
Book saved = sqlClient.save(book).getModifiedEntity();

// 2. 自行查询完整数据
Book fullBook = sqlClient.findById(Book.class, saved.id());
```

---

## 10. 怎么给实体属性设置默认值

### ✅ 解决方案：使用保存前过滤器

```java
@Component
public class BookDraftInterceptor implements DraftInterceptor<BookDraft> {
    
    @Override
    public void beforeSave(BookDraft draft, boolean isNew) {
        if (isNew && draft.status() == null) {
            draft.setStatus(Status.ACTIVE);  // 设置默认值
        }
    }
}
```

**建议**：抽取公用属性到 `MappedSuperclass`，配合过滤器统一处理。

**优势**：比数据库默认值更强大，可结合业务逻辑给出默认值。

---

## 11. 为什么设计 DTO 语言

### 核心原因

| 场景 | 推荐做法 | 原因 |
|------|----------|------|
| **查询** | 使用动态对象 | 减少 DTO 编写 |
| **保存/更新** | 使用 DTO 语言 | 校验数据结构，防止意外修改 |

### 具体场景

保存书籍接口只应修改 `book` 表，但若直接使用 `Book` 实体接收参数：

```java
// 危险：如果传入参数包含关联 BookStore，会级联修改 book_store 表
@PostMapping("/books")
public void saveBook(@RequestBody Book book) {
    sqlClient.save(book);  // 可能意外修改关联表！
}
```

**DTO 语言的优势**：
- 定义明确的输入结构
- 自动生成 DTO 与实体转换代码
- 比手动校验更方便

---

## 12. 配合 Lombok/MapStruct 的 APT 顺序

### 问题

多个 APT（Annotation Processing Tool）框架混用时，需要声明顺序。

### ✅ 解决方案

**顺序规则**：Lombok → MapStruct → Jimmer

**Maven 配置**：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
            <configuration>
                <annotationProcessorPaths>
                    <!-- 注意顺序：Lombok 在最前面 -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.babyfish.jimmer</groupId>
                        <artifactId>jimmer-apt</artifactId>
                        <version>${jimmer.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Gradle**：不需要特别指定顺序。

---

## 13. select tuple 最多只能选 9 列

### 问题

为什么不支持更多列？

### ✅ 推荐做法

**不推荐使用** `select(tuple)`，因为这需要手动将 tuple 映射为 DTO。

**推荐**：使用 [对象抓取器](../query/fetcher.md)：

```java
// ❌ 不推荐
List<Tuple9<...>> result = sqlClient
    .createQuery(table)
    .select(table.name(), table.price(), ..., table.edition())  // 最多9个
    .execute();

// ✅ 推荐
Fetcher<Book> fetcher = BookFetcher.$.allScalarFields();
List<Book> books = sqlClient
    .createQuery(table)
    .select(table.fetch(fetcher))
    .execute();
```

---

## 14. 为什么不支持 from 子查询

```sql
-- 不支持这种写法
SELECT ... FROM (SELECT ... FROM book) AS sub
```

**原因**：Jimmer 是彻底的 ORM，这种形式与面向对象偏离太远，因此没有支持。

**替代方案**：使用视图（View）或中间表，或者分步查询。

---

## 15. 嵌入原生 SQL 指定多个值

### ✅ 示例

```java
BookTable table = BookTable.$;

List<Double> ranks = sqlClient
    .createQuery(table)
    .select(
        Expression.numeric()
            .sql(
                Double.class,
                "rank() over(order by %e desc, %e desc)",
                // 使用多个 value() 绑定，与 %e 个数一致
                it -> it.value(table.name()).value(table.price())
            )
    )
    .execute();
```

---

## 16. 自定义排序（null first / null last）

### ✅ 示例

```java
// null first
.orderBy(table.name().nullFirst())

// null last
.orderBy(table.name().nullLast())

// 更复杂的自定义排序
.orderBy(
    Expression.sql("CASE WHEN name IS NULL THEN 0 ELSE 1 END")
)
```

---

## 总结

本文档基于 Jimmer 官方 FAQ 整理，涵盖了最常见的问题：

1. **UnloadedException** - 区分未赋值和值为 null
2. **主动方/从动方** - 理解 mappedBy 的正确用法
3. **保存时机** - 不要在嵌套 lambda 中保存
4. **逻辑删除** - 默认 0=未删除的约定
5. **Nullable 要求** - 假外键/过滤器/逻辑删除场景
6. **外键 vs 关联** - `id()` vs `().id()` 的区别
7. **数据库隔离** - catalog/schema 配置
8. **部分更新** - 使用 Jimmer 动态性
9. **返回全属性** - save 后自行 findById
10. **默认值** - 使用保存前过滤器
11. **DTO 语言** - 为什么需要静态输入
12. **APT 顺序** - 配合 Lombok/MapStruct
13. **tuple 限制** - 推荐使用对象抓取器
14. **子查询限制** - 不支持的语法
15. **原生 SQL** - 绑定多个值
16. **排序控制** - null first/last

**参考**：https://jimmer.deno.dev/zh/faq
