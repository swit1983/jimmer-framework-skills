---
name: jimmer-framework-skills
description: Jimmer ORM 开发辅助。含官方中文文档完整本地镜像（231 页，已清洗）、经 v0.12.4 源码逐项校验的快速参考与勘误清单、可直接套用的 Java/Kotlin 示例。当用户提到 Jimmer / Jimmer ORM，或需要编写、调试、排错实体映射、SQL DSL 查询、Fetcher 对象抓取、SaveCommand 保存指令、Input/View DTO 与 DTO 语言、三级缓存、全局过滤器、Spring Data 集成时使用此 skill。
---

# jimmer-framework-skills

Jimmer ORM 开发辅助 Skill。包含**完整本地化的官方中文文档**、**经源码校验的快速参考**与**示例代码**。

## 何时使用

- 用户提到 "jimmer" / "jimmer orm" / Jimmer 相关概念
- 需要编写/调试 Jimmer 实体映射、查询（SQL DSL / Fetcher）、保存指令、缓存配置
- 询问 Jimmer 注解（`@Entity` `@ManyToOne` `@IdView` `@Formula` `@Transient` `@LogicalDeleted` …）
- 涉及 Input/View DTO、DTO 语言、SaveCommand、草稿拦截器等

## 版本基准

| 项 | 值 |
|---|---|
| Jimmer 源码版本 | **0.12.4** |
| 官方文档站点 | <https://babyfish-ct.github.io/jimmer-doc/zh/docs/> |
| 文档源仓库 | <https://github.com/babyfish-ct/jimmer-doc> |
| `docs/` 内容 | 上述官方站点的**完整本地镜像**（231 页，已清洗无效 CSS/JSX） |

> ⚠️ 旧域名 `https://jimmer.deno.dev/zh/docs` 已弃用，遇到请替换为上表的官方站点。

---

## ⚠️ 首要提醒：先读勘误

**官方文档存在少量未随 API 演进更新的写法。** 在照抄 `docs/` 或外部示例前，
务必先对照 **[resources/guides/errata.md](resources/guides/errata.md)**。

高频踩坑：

| 常见误写（部分来自官方文档） | 正确写法 |
|---|---|
| `sqlClient.update(entity)` | **方法不存在**，用 `save()` / `saveCommand().setMode(UPDATE_ONLY)` |
| `sqlClient.insert/merge/insertIfAbsent(...)` | **方法不存在**，用 `save(entity, mode, associatedMode)` |
| `sqlClient.saveCommand(list)` | 批量用 `saveEntitiesCommand(list)` |
| `select(fetcher)` | `select(table.fetch(fetcher))` |
| `JSqlClient.builder()` / `addGlobalFilter()` | `JSqlClient.newBuilder()` / `addFilters()` |
| `joinIf(...)` | **不存在**，用链式关联路径 + `whereIf` |
| `Expression.nativeSql/sql(...)` | `Predicate.sql(...)`、`Expression.numeric().sql(...)` |
| `nullFirst()` / `nullLast()` | `nullsFirst()` / `nullsLast()` |
| `@ColumnName` / `@Embedded` / `@Json` / `@TenantId` | `@Column(name=)` / `@Embeddable` / `@Serialized` / 普通属性+Filter |

---

## 目录结构

```
jimmer-framework-skills/
├── SKILL.md                 本文件（导航 + 关键约定）
├── resources/               快速参考（已对照 v0.12.4 源码校正，优先查阅）
│   ├── guides/              入门与避坑
│   │   ├── quick-start.md       5 分钟上手（依赖/配置/CRUD）
│   │   ├── best-practices.md    分层、Repository、事务
│   │   ├── common-pitfalls.md   16 个高频易错点
│   │   ├── errata.md            ⚠️ 文档勘误清单（必读）
│   │   └── apt-ksp-guide.md     APT/KSP 配置、Lombok 顺序
│   ├── mapping/             实体映射：基础/一对多/多对一/多对多/可空性
│   │                        计算属性、视图属性、逻辑删除、脱钩、嵌入
│   ├── query/               查询：Fetcher、动态 JOIN、隐式子查询
│   │                        分页、递归抓取、全局过滤器
│   ├── save/                SaveCommand、保存模式、草稿拦截器
│   ├── dto/                 Input/View DTO、DTO 语言、MapStruct
│   ├── cache/               三级缓存：对象/关联/计算
│   ├── spring/              Spring Data Repository 集成
│   └── features/            框架特性概览
├── examples/                示例代码（Java + Kotlin，已校正 API）
│   ├── annotation/          实体定义
│   ├── query/ fetch/        查询与对象抓取
│   ├── mutation/            保存/删除/Statement
│   └── mapping/ dto/ cache/ configuration/ spring/ showcase/
└── docs/                    官方文档完整镜像（231 页，按需深入）
```

---

## 查阅路径

通常不必一次读完 `docs/`，建议顺序：

1. **`resources/guides/errata.md`** — 避开已知的错误写法
2. **`resources/` 对应模块** — 快速参考，API 已校验
3. **`examples/`** — 可直接套用的完整代码
4. **`docs/`** — 需要深入原理/细节时再查

### 常见任务 → 入口

| 任务 | 首选文件 |
|---|---|
| 搭环境、写第一个 CRUD | `resources/guides/quick-start.md` |
| 定义实体/关联/逻辑删除 | `resources/mapping/*.md` |
| 查询对象图、控制返回形状 | `resources/query/fetcher.md` |
| 动态条件、JOIN、子查询 | `resources/query/dynamic-join.md`、`implicit-subquery.md` |
| 分页 / 树形递归 | `resources/query/paging.md`、`recursive-fetch.md` |
| 多租户、数据权限 | `resources/query/global-filter.md` |
| 保存复杂嵌套结构 | `resources/save/save-command.md` |
| 前后端数据结构不一致 | `resources/dto/input-dto.md`、`dto-language.md` |
| 缓存与一致性 | `resources/cache/overview.md` |
| 排查报错 / "为什么数据被删了" | `resources/guides/common-pitfalls.md` |

### docs 章节索引

`cache` `client` `configuration` `graphql` `mapping` `mutation` `object`
`overview` `query` `quick-view` `resource` `showcase` `spring`

每篇同名顶层 `xxx.md` 是该章节导航页，例如 `docs/mapping.md` → `docs/mapping/**`。

---

## 关键行为约定（务必牢记）

1. **残缺对象**：只给部分属性赋值 = 只更新这些列；未赋值属性是 *unloaded*，
   访问它会抛 `UnloadedException`（`null` 是有效值，不等于未赋值）。

2. **`AssociatedSaveMode` 默认是 `REPLACE`**
   保存时未提交上来的旧关联**会被脱钩处理**。只想追加请显式设置 `APPEND` / `MERGE`。

3. **`SaveMode` 默认是 `UPSERT`**：有 id/key 走更新，无则插入。

4. **短关联 vs 长关联**：仅改归属用 `setStoreId(id)`（不改 `STORE` 表）；
   要连带修改关联对象本体才用 `setStore(obj)`。

5. **不可变 + Draft**：用 `Immutables.createXxx(draft -> {...})` 或
   `XxxDraft.$.produce(...)`；**不要在 produce 的 lambda 内部调用 save**。

6. **动态 JOIN 无需手写**：链式关联路径（如 `table.store().city()`）自动生成 JOIN，
   配合 `whereIf` 即可实现"条件成立才 JOIN"。

7. **逻辑删除约定 `0` 表示未删除**，其余为已删除；该约定源码写死，不可反转。

---

## 常用坐标

```xml
<dependency>
    <groupId>org.babyfish.jimmer</groupId>
    <artifactId>jimmer-spring-boot-starter</artifactId>
    <version>0.12.4</version>
</dependency>
```

| 用途 | artifactId |
|---|---|
| Spring Boot 集成 | `jimmer-spring-boot-starter` |
| 独立使用（Java） | `jimmer-sql` |
| 独立使用（Kotlin） | `jimmer-sql-kotlin` |

常用包名：

| 类型 | 包 |
|---|---|
| 实体注解 | `org.babyfish.jimmer.sql`（`@Entity` `@Column` `@JoinColumn` `@Serialized` …） |
| 保存 API | `org.babyfish.jimmer.sql.ast.mutation`（`SaveMode` `AssociatedSaveMode` `SimpleSaveResult`） |
| 过滤器 | `org.babyfish.jimmer.sql.filter`（`Filter` `FilterArgs` `FilterConfig`） |
| Fetcher | `org.babyfish.jimmer.sql.fetcher` |
| Specification（超级 QBE） | `org.babyfish.jimmer.Specification` |
| Spring 仓储 | `org.babyfish.jimmer.spring.repository.JRepository` |


## Jimmer ORM 实践要点

### 结构与生成物

- 实体是不可变接口，Draft、Immutables 工厂、View、Specification、Props 全部由 `jimmer-apt` 注解处理器在编译期生成到 `target/generated-sources/annotations`。
- 判断某个 View/Specification/Draft 是否存在：先看源码里对应的 `.dto` 文件定义，再编译确认生成物；不要凭空 import 或手写这些类。
- 修改实体或 `.dto` 后必须重新编译（让 apt 重新生成），再做依赖该模块的其他模块编译；多模块项目里只编译下游模块在生成类缺失时会报“找不到符号”，这是正常现象。
- 实体加字段后，若接口需要返回它，必须同步在 `.dto` 的对应 View 中显式加上该属性——View 是白名单式取数，不会自动带出新字段。

### 实体定义

- 主键用 `@Id` + ID 生成器（如 `@GeneratedValue(generatorType = ...)`），避免依赖数据库自增与业务代码手工赋号混用。
- `@Column` 注解没有 `length` 属性，列长度/精度只在 DDL 中定义；实体与迁移脚本要同步修改。
- 长整型 ID 直接序列化给 JS 前端会丢精度，对外暴露的实体/View 需配置 Long→String 转换（如 `@JsonConverter(LongToStringConverter.class)`）。
- 审计字段（创建/更新时间、操作人）和多租户字段优先用 `@MappedSuperclass` 抽象 + `DraftInterceptor` 自动填充，业务代码不要手工 set。

### 保存

- append-only 记录表用 `SaveMode.INSERT_ONLY`，防止意外改写历史。
- 聚合根整树保存（父+子集合）用 `saveCommand(root).setMode(NON_IDEMPOTENT_UPSERT).setAssociatedModeAll(AssociatedSaveMode.MERGE)`。
- 按“业务唯一键”upsert：在实体业务键属性上标 `@Key`，保存命令 `.setKeyProps(XxxProps.BUSINESS_KEY)`；或先精确查询存在性，再决定 INSERT_ONLY 还是带 id 的 UPSERT。
- 部分字段更新优先 `sql.createUpdate(...).set(...).where(...)`，不要查出整实体、改两个字段再整棵 save，避免覆盖并发修改。
- 数据库唯一索引**不要包含可空列**（MySQL 中 NULL 不参与唯一性去重，重复插入不会报冲突）。用哨兵值表示“通配/全部”（如 -1 或 0），并把哨兵值写入唯一键。
- Draft 顶层构造推荐 `Immutables.createXxx(draft -> ...)`（返回实体类型）；`XxxDraft.$.produce(...)` 也可，注意其返回类型是 Draft。

### Draft lambda 编译坑

- `produce(d -> ...)` / `createXxx(d -> ...)` 的 lambda 只能捕获 effectively final 的局部变量；变量在 lambda 之前被重新赋值过（如“查不到就初始化”模式），必须先赋给一个 `final` 局部变量再进 lambda。
- lambda 内引用的可变集合/对象本身可以，引用被重新赋值的**变量**不行。

### 查询

- `allScalarFields()` 只取标量属性，**不包含关联**。拿到实体后直接导航 `entity.assoc().prop()` 前必须确认该关联已被 fetch，否则 NPE；带关联查询要在 Fetcher 链上显式加 `.assoc(...)`。
- 列表/分页带动态条件优先用 Specification（`.dto` 中 `specification XxxSpecification` 定义），仓库用 `findList/findPage(specification, ViewType, "排序串")`；Web 层可直接用 Specification 类型接查询参数自动绑定。
- `.fetch(ViewType.class)` 返回的是 View 对象（不可再导航关联），需要继续导航就用 Fetcher 查实体。
- 分页对象是 `Page<T>`；`fetchPage(index, size)` 的 index 从 0 开始。

### 多租户/上下文类字段（若项目启用）

- 由 DraftInterceptor 填充的租户/操作人字段，依赖 ThreadLocal 上下文。Web 请求由拦截器设置；**异步线程、定时任务、消息消费者必须自行设置上下文**，并在 finally 中清理，否则保存时抛异常或静默填错。
- 排查“保存时租户为空”类报错，先看当前线程是否设过上下文，而不是看实体赋值代码。

### 排错习惯

- 编译错误“找不到符号 XxxDraft/XxxView/XxxSpecification”：九成是 `.dto` 没定义或没重新编译生成，先查 dto 文件再编译。
- 运行时“字段未加载”（UnloadedException）：访问了未 fetch 的属性，回查询处补 Fetcher 或改用 View。
- 唯一键冲突/该冲突却没报：检查唯一索引是否含 NULL 列。