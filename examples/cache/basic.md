# 基础缓存示例

Jimmer 支持三级缓存：对象缓存、关联缓存、计算缓存，自动维护一致性。

## 启用缓存

### 配置（Spring Boot example）

```yaml
jimmer:
  cache:
    enabled: true
```

## 三级缓存说明

| 缓存类型 | 作用 | 说明 |
|----------|------|------|
| **对象缓存** | `id` -> 实体对象 | 最基础的缓存 |
| **关联缓存** | `(id, 关联属性)` -> 关联 id 列表 | 加速关联查询 |
| **计算缓存** | 存储 `@Transient` 计算属性结果 | 避免重复计算 |

## 缓存类型示例

### 对象缓存
```java
// 根据 id 查询自动使用缓存
Book book = sqlClient
    .createQuery(Book.class)
    .where(BookProps.ID.eq(id))
    .findUnique();

// 命中缓存不需要查询数据库
```

### 关联缓存
```java
// 自动缓存 Book.authors 的 id 列表
book.authors(); // 第一次查询后缓存结果
```

## 缓存一致性

只要启用 Trigger（BinLog 或 Transaction），**Jimmer 自动维护缓存一致性**，开发人员不需要做任何额外工作。

- 修改实体 → 自动失效相关缓存
- 支持对象缓存、关联缓存自动一致性
- 计算缓存需要少量开发人员工作

## Trigger 配置

### BinLog Trigger（推荐）

使用 CDC 监听 BinLog，任何方式修改数据库都能自动同步缓存：

```java
// 需要配置 maxwell 或 debezium
// Jimmer 自动监听变化更新缓存
```

### Transaction Trigger

适合简单项目，在事务提交后更新缓存：

```java
// 启用
jimmer:
  cache:
    transaction-trigger:
      enabled: true
```

## 多视角缓存

不同用户看到的数据不同（比如行级权限），支持按用户隔离缓存：

```java
@Entity
public interface Book {
    // ...
    @TenantId
    Long tenantId();
}
```

缓存自动按 tenant 隔离，不同租户看不到彼此数据。

