# Jimmer Framework Resources

快速参考文档索引，按功能模块组织。

## 📚 指南 (Guides)

| 文档 | 内容 |
|------|------|
| [快速开始](./guides/quick-start.md) | 5 分钟上手指南，包含完整 CRUD 示例 |
| [最佳实践](./guides/best-practices.md) | 推荐代码组织方式、Repository 模式、Service 设计 |
| [常见陷阱](./guides/common-pitfalls.md) | 易错点和解决方案检查清单 |

## 🗺️ 实体映射 (Mapping)

| 文档 | 内容 |
|------|------|
| [基础映射](./mapping/basic-mapping.md) | `@Entity`, `@Id`, `@Column`, `@Table` 基础注解 |
| [关联映射](./mapping/one-to-many.md) | `@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@OneToOne` |
| [计算属性](./mapping/computed-properties.md) | `@Formula` (简单计算) 和 `@Transient` (复杂计算) |
| [复合字段](./mapping/embedded.md) | `@Embeddable` 复合类型映射 |
| [视图属性](./mapping/view-properties.md) | `@IdView`, `@ManyToManyView` 简化关联查询 |
| [逻辑删除](./mapping/logical-delete.md) | `@LogicalDeleted` 实体表和中间表逻辑删除 |
| [脱钩操作](./mapping/on-dissociate.md) | `@OnDissociate` 解除关联时的行为控制 |

## 🔍 查询 (Query)

| 文档 | 内容 |
|------|------|
| [动态 JOIN](./query/dynamic-join.md) | 自动优化 JOIN，解决 N+1 问题 |
| [隐式子查询](./query/implicit-subquery.md) | 简化集合关联的子查询 |
| [智能分页](./query/paging.md) | 基础分页、深分页优化、反排序优化 |
| [递归查询](./query/recursive-fetch.md) | 树形结构递归抓取 |
| [全局过滤器](./query/global-filter.md) | 多租户、权限控制等全局过滤 |

## ✏️ 修改保存 (Mutation)

| 文档 | 内容 |
|------|------|
| [保存指令详解](./save/save-command.md) | `save` 指令、保存模式、关联保存、脱钩 |
| [Input DTO](./dto/input-dto.md) | 前后端数据传输、空值处理 |

## 🔧 API 参考 (API)

| 文档 | 内容 |
|------|------|
| [对象抓取器 Fetcher](./query/fetcher.md) | 按需抓取任意形状的数据结构 |
| [保存指令 SaveCommand](./save/save-command.md) | 保存配置的详细说明 |
| [DTO 语言](./dto/dto-language.md) | Input/View DTO 定义语法 |

## ⚡ 缓存 (Cache)

| 文档 | 内容 |
|------|------|
| [缓存概述](./cache/overview.md) | 三级缓存体系简介 |
| [对象缓存](./cache/object-cache.md) | 实体对象缓存配置 |
| [关联缓存](./cache/association-cache.md) | 关联集合缓存配置 |

## 🌱 Spring 集成 (Spring)

| 文档 | 内容 |
|------|------|
| [Spring Data Repository](./spring/repository.md) | JRepository 使用 |

---

## 快速导航

### 新手入门路径
1. [快速开始](./guides/quick-start.md) - 先跑通第一个程序
2. [基础映射](./mapping/basic-mapping.md) - 学习实体定义
3. [对象抓取器](./query/fetcher.md) - 掌握查询核心
4. [保存指令详解](./save/save-command.md) - 掌握保存核心

### 常见问题解决
- 关联查询慢 → [动态 JOIN](./query/dynamic-join.md)
- 分页性能差 → [智能分页](./query/paging.md)
- 树形结构 → [递归查询](./query/recursive-fetch.md)
- 前后端数据不一致 → [Input DTO](./dto/input-dto.md)
- 数据权限控制 → [全局过滤器](./query/global-filter.md)
