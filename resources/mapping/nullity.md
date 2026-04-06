# 可空性

Jimmer 对属性可空性处理非常严格。即使 Java 开发者，也需要像 Kotlin 开发者一样认真考虑每个属性是否允许为 null。

明确说明每个属性是否可空对 Jimmer 非常重要，很多功能都会受其影响。

## 定义可空性

### Kotlin

直接使用 Kotlin 本身的空安全语法：
- `T` → 非空
- `T?` → 可为空

### Java

Java 根据以下规则判断：

1. **基本类型** (`boolean`, `char`, `byte`, `short`, `int`, `long`, `float`, `double`) → 非 null
2. **包装类型** (`Boolean`, `Character`, `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`) → 可 null
3. **其他类型**：
   - 如果被 `@Nullable`（或简名为 `Null` 的注解）修饰 → 可 null
   - 否则 → 默认非 null

> 💡 推荐使用 `org.jetbrains.annotations.Nullable`，Intellij 原生支持，不需要额外配置注解处理器。

## 重要规则

| 场景 | 可空性要求 | 说明 |
|------|------------|------|
| `@Id` 属性 | **必须非空** | 和 JPA 不同！Jimmer 靠动态性表达 id 缺失，不需要将 id 设为可空 |
| 一对多 / 多对多 | **必须非空** | 没有关联时返回空集合（长度 0），不是 null |
| 一对一 / 多对一（基于外键，真实外键） | 可以非空 | 如果外键不允许 null |
| 一对一 / 多对一（基于中间表） | **必须可空** | 无法保证一定有数据 |
| 一对一 / 多对一（基于伪外键） | **必须可空** | 伪外键没有数据库约束 |
| 远程关联 | **必须可空** | 跨服务关联可能不存在 |
| 作为镜像端的 `@OneToOne` | **必须可空** | 镜像端要求 |
| 关联实体有全局过滤器 | 可能需要可空 | 除非过滤器满足完整性保证 |

## `inputNotNull`

某些情况下，由于查询场景要求，基于外键的一对一/多对一必须声明为可空，但保存数据时，你可能不允许用户将外键设置为 null。

这时可以使用 `inputNotNull = true`：

```java
@OneToOne(inputNotNull = true)
Address address();

// 或者
@ManyToOne(inputNotNull = true)
BookStore store();
```

开启后，数据库验证会检查外键字段不能为 null。
