# jimmer-framework-skills

面向 AI 编码助手的 [Jimmer](https://github.com/babyfish-ct/jimmer) ORM 开发技能包（Agent Skill）。

把 Jimmer 官方中文文档**完整镜像到本地**（231 页），并附上一份**经源码逐项校验过的**快速参考与勘误清单，让助手在写 Jimmer 代码时不至于照抄到过时的官方写法。

---

## 它解决什么问题

1. **官方文档站是 SPA，抓取会带一堆内联 CSS 垃圾。** 本仓库改为从官方文档源码仓库的中文源（`i18n/zh/`）转成纯 Markdown，输出干净、可离线全文检索。
2. **官方文档本身有少量未随 API 演进更新的写法。** 例如 `docs/mutation/save-command/usage.md` 里的 `sqlClient.update(entity)` —— 在 v0.12.4 源码中**根本不存在这个方法**。照抄会直接编译不过。
   → 所有已发现的此类问题固化在 [`resources/guides/errata.md`](resources/guides/errata.md)，**建议先看这一篇**。

---

## 安装

把本仓库放到 skills 目录下即可，目录名需与 `SKILL.md` 的 `name` 保持一致：

```bash
# WorkBuddy / CodeBuddy
git clone https://github.com/swit1983/jimmer-framework-skills.git \
    ~/.workbuddy/skills/jimmer-framework-skills

# Claude Code
git clone https://github.com/swit1983/jimmer-framework-skills.git \
    ~/.claude/skills/jimmer-framework-skills
```

仓库结构即标准 Agent Skill 结构，`SKILL.md` 带 `name` / `description` frontmatter：

```
jimmer-framework-skills/
├── SKILL.md          导航入口 + 关键行为约定（助手唯一必读文件）
├── resources/        快速参考（33 篇，已对照源码校正）
├── examples/         示例代码（44 个：4 Java + 4 Kotlin + 35 说明 + 1 .dto）
├── docs/             官方文档完整镜像（231 页 / 2.4 MB）
└── README.md         本文件
```

---

## 三层内容，按需取用

| 层 | 内容 | 什么时候用 |
|---|---|---|
| **`resources/`** | 快速参考，API 已对照 v0.12.4 源码验证 | **默认先查这里**，答案最可靠 |
| **`examples/`** | 可直接套用的完整代码（Java / Kotlin 双版本） | 需要写具体代码时 |
| **`docs/`** | 官方文档完整镜像（231 页） | 需要原理、细节、边界场景时 |

`docs/` 的顶层 `xxx.md` 是该章节的导航页，例如 `docs/mapping.md` → `docs/mapping/**`。
章节：`cache` `client` `configuration` `graphql` `mapping` `mutation` `object`
`overview` `query` `quick-view` `resource` `showcase` `spring`。

---

## 版本基准

| 项 | 值 |
|---|---|
| Jimmer 源码版本 | **0.12.4** |
| 官方文档站点 | <https://babyfish-ct.github.io/jimmer-doc/zh/docs/> |
| 文档源仓库 | <https://github.com/babyfish-ct/jimmer-doc> |

> ⚠️ 旧域名 `https://jimmer.deno.dev/zh/docs` 已弃用，遇到请替换为上表的官方站点。

常用坐标：

```xml
<dependency>
    <groupId>org.babyfish.jimmer</groupId>
    <artifactId>jimmer-spring-boot-starter</artifactId>
    <version>0.12.4</version>
</dependency>
```

---

## 几个最容易踩的坑

完整清单见 [`resources/guides/errata.md`](resources/guides/errata.md)，高频几条：

| 常见误写（部分来自官方文档） | 正确写法 |
|---|---|
| `sqlClient.update(entity)` | **方法不存在** → `save()` / `saveCommand().setMode(UPDATE_ONLY)` |
| `sqlClient.insert/merge/insertIfAbsent(...)` | **方法不存在** → `save(entity, mode, associatedMode)` |
| `sqlClient.saveCommand(list)` | 批量用 `saveEntitiesCommand(list)` |
| `select(fetcher)` | `select(table.fetch(fetcher))` |
| `JSqlClient.builder()` / `addGlobalFilter()` | `JSqlClient.newBuilder()` / `addFilters()` |
| `joinIf(...)` | **不存在** → 链式关联路径 + `whereIf` |
| `Expression.nativeSql/sql(...)` | `Predicate.sql(...)`、`Expression.numeric().sql(...)`（占位符 `%e`） |
| `nullFirst()` / `nullLast()` | `nullsFirst()` / `nullsLast()` |
| `@ColumnName` / `@Embedded` / `@Json` / `@TenantId` | `@Column(name=)` / `@Embeddable` / `@Serialized` / 普通属性 + Filter |

另外两条会**静默丢数据**的行为，务必记住：

- **`AssociatedSaveMode` 默认是 `REPLACE`** —— 保存时未提交上来的旧关联会被脱钩。只想追加请显式设 `APPEND` / `MERGE`。
- **`SaveMode` 默认是 `UPSERT`** —— 有 id/key 走更新，无则插入。

---

## 维护

`docs/` 是生成产物，不要手工编辑。Jimmer 文档更新后重新生成：

```bash
git clone --depth 1 https://github.com/babyfish-ct/jimmer-doc.git jimmer-doc-tmp
cd jimmer-doc-tmp
# 把转换脚本 convert_docs.py 放到此目录（脚本中的 SRC / DST 路径需按本机调整）
python convert_docs.py
```

转换脚本负责：剥离 frontmatter 与 `import`、递归内联片段、React 交互组件转占位说明、
`Tabs/TabItem` → `**Java**` / `**Kotlin**` 小节、代码块 dedent、站内链接重写为相对 `.md`、
清理 `highlight-*` 指令，并把源路径 `X/index` 映射为站点真实 URL `.../zh/docs/X`（不带 `/index`）。
生成后 `docs/` 的 CSS / JSX 残留应为 0。

`resources/` 与 `examples/` 是手工维护的，修改前请对照 Jimmer 源码验证 API 是否仍存在。

---

## 已知缺口

- 官方仓库中 `showcase/where`、`showcase/order-by` 的中英文源均为空占位（官方尚未编写），生成页已标注，不是抓取失败。
- `docs/` 中的交互式 Demo（如 `ShortAssociation`、`CalculatedCache`）已替换为文字说明占位，需看效果请访问对应官方页面。

---

## License

文档内容版权归 Jimmer 官方（babyfish-ct）所有，本仓库仅做格式转换与本地镜像。
`resources/` 与 `examples/` 中的整理、校验与勘误内容可自由使用。
