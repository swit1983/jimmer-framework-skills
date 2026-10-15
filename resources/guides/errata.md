# Jimmer 文档勘误清单

> **校验基准**：`jimmer` 源码 v0.12.4（本地 `I:/github/jimmer/project`）
> **官方文档源**：<https://babyfish-ct.github.io/jimmer-doc/zh/docs/>
>
> 官方文档整体质量很高，但仓库的 `current` 分支中存在少量**未随 API 演进更新**的写法。
> 本清单记录全部经源码核实为**错误或已失效**的内容，按 `docs/` 原文照抄前请先对照此表。

---

## 一、方法级勘误

### 1. `sqlClient.update(entity)` — 方法不存在 ❌

| 项 | 内容 |
|---|---|
| 出处 | `docs/mutation/save-command/usage.md`（"用不完整对象避免先查后改"小节） |
| 官方写法 | `sqlClient.update(Immutables.createBook(draft -> {...}));` |
| 实际 | `JSqlClient` 与 `SaveOperations` 中**均无** `update(E)` 方法（Java/Kotlin 均无） |
| 正确写法 | 见下 |

```java
// 局部更新：依赖 SaveMode.UPSERT（默认） + 不完整对象
sqlClient.save(book);

// 若确定只更新、绝不新增
sqlClient.saveCommand(book)
        .setMode(SaveMode.UPDATE_ONLY)
        .execute();
```

---

### 2. `insert()` / `merge()` / `insertIfAbsent()` 等动词快捷方法 — 均不存在 ❌

`SaveOperations` 接口实际只提供三类入口：

| 方法 | 返回 |
|---|---|
| `save(entity)` / `save(entity, mode, associatedMode)` | `SimpleSaveResult<E>` |
| `saveEntities(entities)` / `saveEntities(entities, mode, associatedMode)` | `BatchSaveResult<E>` |
| `saveInputs(inputs)` / `saveInputs(inputs, mode, associatedMode)` | `BatchSaveResult<E>` |

需要改变行为时**显式指定 `SaveMode`**，而不是调用不同动词的方法。

---

### 3. `saveCommand(List)` — 类型不匹配 ❌

`saveCommand(E entity)` 只接受单个对象。批量场景：

```java
BatchSaveResult<Book> result = sqlClient
        .saveEntitiesCommand(books)      // ✅ 批量
        .execute();
```

---

### 4. Fetcher 不能直接传给 `select` ❌

`select` 需要 `Selection`，Fetcher 必须先包一层：

```java
// ❌ 错误
.select(bookFullFetcher)

// ✅ 正确
.select(table.fetch(bookFullFetcher))
```

DTO 同理：`select(table.fetch(BookStoreDto.class))`。

---

### 5. `JSqlClient.builder()` → 应为 `newBuilder()` ❌

```java
JSqlClient sqlClient = JSqlClient.newBuilder()   // ✅ 静态工厂
        .addFilters(new TenantFilter())          // ✅ 不是 addGlobalFilter
        .build();
```

---

### 6. `joinIf(...)` — 不存在 ❌

Jimmer 的动态 JOIN 靠**链式关联路径**与 `whereIf`，集合关联会自动转成 exists 子查询：

```java
.whereIf(
    StringUtils.hasText(authorName),
    table.authors(author ->
        Predicate.or(
            author.firstName().ilike(authorName),
            author.lastName().ilike(authorName)
        )
    )
)
```

条件不成立时，该分支不进入 SQL，JOIN 也就自动省去。

---

### 7. `Expression.nativeSql(...)` / `Expression.sql(...)` — 不存在 ❌

`Expression` **没有**静态 `sql` 方法。原生 SQL 的正确入口：

| 场景 | 写法 |
|---|---|
| where 条件 | `Predicate.sql("JSON_CONTAINS(%e, %e)", args)` |
| select 数值列 | `Expression.numeric().sql(Integer.class, "... %e ...", args)` |
| select 字符串列 | `Expression.string().sql("... %e ...", args)` |
| select 可比较类型 | `Expression.comparable().sql(Type.class, "... %e ...", args)` |

占位符统一为 `%e`。

---

### 8. 排序：是 `nullsFirst()` / `nullsLast()` ❌ 不是 `nullFirst()` / `nullLast()`

源码位置：`org.babyfish.jimmer.sql.ast.query.Order`。

---

## 二、注解级勘误

| 误写 | 正解 | 说明 |
|---|---|---|
| `@ColumnName("X")` | `@Column(name = "X")` | 标量列名用 `@Column` |
| `@Embedded` | `@Embeddable` | 复合类型的声明注解 |
| `@Json` | `@Serialized` | JSON 列映射 |
| `@TenantId` | 普通属性 + `Filter` | Jimmer **无内置多租户注解**，隔离靠过滤器实现 |
| `@Column(nullable=false)` 于假外键 | 去掉，标注 `@Nullable` | 假外键必须是 nullable |

> `IdOnlyFetchType` 只有 `DEFAULT` 和 `RAW` 两个枚举值，
> 注意拼写（`FetchType`，不是 `FetcherType`）。

---

## 三、默认值要点（易误解）

| 项 | 默认值 | 影响 |
|---|---|---|
| `SaveMode` | `UPSERT` | 有 id/key 则更新，否则插入 |
| `AssociatedSaveMode` | **`REPLACE`** | 未出现在新数据中的旧关联**会被脱钩**；只想追加请显式改 `APPEND`/`MERGE` |
| 逻辑删除取值 | `0` 表示未删除 | 该约定在源码中写死，不可反转 |

> `REPLACE` 是默认值这一点极其关键：不显式设置时，保存操作可能删除数据库中存在、
> 但本次没有提交上来 的关联。

---

## 四、站务信息

- 旧域名 `https://jimmer.deno.dev/zh/docs` 已弃用，官方现址为
  <https://babyfish-ct.github.io/jimmer-doc/zh/docs/>
- 仓库地址：<https://github.com/babyfish-ct/jimmer-doc>（中文源在 `i18n/zh/docusaurus-plugin-content-docs/current/`）
- 当前文档 `current` 版本对应的代码示例可能落后于 release，**API 最终以源码为准**
