---
title: MappedSuperclass
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/mapping/advanced/mapped-super-class
---

# MappedSuperclass

## 基本使用

`org.babyfish.jimmer.sql.MappedSuperclass`用于提供可供实体继承的抽象超类型。

该超类型并不是实体，但可以被多个实体类型继承，从而避免多个实体重复声明相同的属性。

让我们来看一个例子，先定义超类型

**Java**

```java title="BaseEntity.java"

@MappedSuperclass
public interface BaseEntity {

    LocalDateTime createdTime();

    @ManyToOne
    User createdBy();

    LocalDateTime modifiedTime();

    @ManyToOne
    User modifiedBy();
}
```

**Kotlin**

```kotlin title="BaseEntity.kt"

@MappedSuperclass
interface BaseEntity {

    val createdTime: LocalDateTime

    @ManyToOne
    val createdBy: User

    val modifiedTime: LocalDateTime

    @ManyToOne
    val modifiedBy: User
}
```

其他实体就可以继承它

-   `BookStore`

**Java**

```java title="BookStore.java"
@Entity

public interface BookStore extends BaseEntity {

    ...省略其他代码...
}
```

**Kotlin**

```kotlin title="BookStore.kt"
@Entity

interface BookStore : BaseEntity {

    ...省略其他代码...
}
```

-   `Book`

**Java**

```java title="Book.java"
@Entity

public interface Book extends BaseEntity {

    ...省略其他代码...
}
```

**Kotlin**

```kotlin title="Book.kt"
@Entity

interface Book : BaseEntity {

    ...省略其他代码...
}
```

-   `Author`

**Java**

```java title="Author.java"
@Entity

public interface Author extends BaseEntity {

    ...省略其他代码...
}
```

**Kotlin**

```kotlin title="Author.kt"
@Entity

interface Author : BaseEntity {

    ...省略其他代码...
}
```

## 多继承

被`MappedSuperclass`修饰的类型支持多继承，其他类型可以从多个`MappedSuperclass`超类型继承。

添加一个新的抽象接口`TenantAware`，所有支持多租户的实体都继承它

**Java**

```java title="TenantAware.java"

@MappedSuperclass
public interface TenantAware {

    String tenant();
}
```

**Kotlin**

```kotlin title="TenantAware.kt"

@MappedSuperclass
interface TenantAware {

    val tenant: String
}
```

**Java**

```java title="Book.java"
@Entity

public interface Book extends BaseEntity, TenantAware {

    ...省略其他代码...
}
```

**Kotlin**

```kotlin title="Book.kt"
@Entity

interface Book : BaseEntity, TenantAware {

    ...省略其他代码...
}
```

修改`Book`，让它不光继承`BaseEntity`，还继承`TenantAware`

:::tip
`@MapperSuperclass`的作用不仅仅是减少重复代码，还可以和其他另外两个功能配合使用

-   [全局过滤器](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/global-filter)
-   [拦截器](../../mutation/draft-interceptor.md)

在和它们配合使用时，多继承可以获得良好的灵活性。
:::
