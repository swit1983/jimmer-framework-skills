# 基础映射

## 实体注解

### @Entity

`@Entity 注解用于修饰不可变接口，表示一个 ORM 实体。

**为什么实体是接口不是类？

Jimmer 实体具备动态性和不可变性，并非简单的 POJO，内部有精巧的运作机制，无法靠开发人员手工编写或 lombok 辅助完成。所以，Jimmer 让开发人员编写接口，由预编译器（Java: Annotation Processor, Kotlin: KSP）在编译时实现这些接口。

示例：

```java
// Java
@Entity
public interface Book {
    // ...
}
```

```kotlin
// Kotlin
@Entity
interface Book {
    // ...
}
```

### @Table

`@Table` 注解为实体指定表名。如果没有使用 `@Table` 注解，Jimmer 会根据命名策略自动推导表名。

```java
// 默认推导：BookStore → BOOK_STORE
@Entity
public interface BookStore {
}

// 等价于
@Entity
@Table(name = "BOOK_STORE")
public interface BookStore {
}
```

### @Column

`@Column` 注解为普通非关联属性指定数据库列名。如果没有使用，根据命名策略自动推导。

```java
// 默认推导：firstName → FIRST_NAME
public interface Author {
    String firstName();
}

// 等价于
public interface Author {
    @Column(name = "FIRST_NAME")
    String firstName();
}
```

> ⚠️ 注意：`@Column` 仅用于非关联属性。对于多对一或一对一关联的外键列，必须使用 `@JoinColumn` 指定。

### @Id

声明某个属性是 ID 主键。

```java
@Entity
public interface Book {
    @Id
    long id();
}
```

⚠️ **重要**：ID 字段必须非空。和 JPA 不同，Jimmer 不鼓励把 ID 声明成可 null，Jimmer 对象的动态性可以轻松表达插入数据时不指定 ID。

### @GeneratedValue

配合 `@Id` 指定自动生成 ID 的策略。支持：

| 策略 | 说明 |
|------|------|
| `GenerationType.IDENTITY` | 数据库自动编号 |
| `GenerationType.SEQUENCE` | 数据库序列 |
| UUID | `generatorType = UUIDIdGenerator.class |
| 自定义 | 实现 `UserIdGenerator<T>` |

示例：

```java
// 数据库自动编号
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
long id();

// UUID
@Id
@GeneratedValue(generatorType = UUIDIdGenerator.class)
UUID id();

// 自定义（如雪花ID
@Id
@GeneratedValue(generateType = MyIdGenerator.class)
Long id();
```

自定义 ID 生成器实现：

```java
public class MyIdGenerator implements UserIdGenerator<Long> {
    @Override
    public Long generate(Class<?> entityType) {
        // 自定义 ID 生成逻辑
        return ...;
    }
}
```

更多内容：
- [关联映射 → association.md
- 命名策略 → naming-strategy.md
- 可空性 → nullity.md
