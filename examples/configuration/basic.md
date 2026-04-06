# 配置示例

常用配置项示例（以 Spring Boot 为例）。

## 基本配置

```yaml
jimmer:
  # 数据库方言
  dialect: mysql
  # 默认枚举策略
  default-enum-strategy: NAME  # 可选 NAME ORDINAL
  # 默认命名策略
 -default-database-strategy:
  # 批量操作大小
  batch-size: 500
  # SQL 日志
  sql-log:
    enabled: true
```

## 缓存配置

```yaml
jimmer:
  cache:
    enabled: true
    # 事务触发器
    transaction-trigger:
      enabled: false
  # BinLog 触发器需要额外配置
```

## 多数据源

```yaml
jimmer:
  default-datasource: primary
  datasources:
    primary:
      dialect: mysql
    secondary:
      dialect: postgres
```

## 常用配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|---------|
| `jimmer.dialect` | 数据库方言 | 自动探测 |
| `jimmer.default-enum-strategy` | 默认枚举映射策略 | `NAME` |
| `jimmer.batch-size` | 批量操作大小 | `500` |
| `jimmer.sql-log.enabled` | 是否打印 SQL | `false` |
| `jimmer.cache.enabled` | 是否启用缓存 | `false` |
| `jimmer.cache.transaction-trigger.enabled` | 是否启用事务触发器 | `false` |

## 非 Spring Boot 项目

```java
JSqlClient sqlClient = JSqlClient
    .newBuilder()
    .setDialect(new MySQLDialect())
    .setDefaultEnumStrategy(EnumType.Strategy.NAME)
    .setDefaultBatchSize(500)
    .build();
```

