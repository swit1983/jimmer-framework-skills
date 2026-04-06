# MappedSuperclass

> 来源: https://jimmer.deno.dev/zh/docs/mapping/advanced/mapped-super-class

- [映射篇](/zh/docs/mapping/)
- [进阶映射](/zh/docs/mapping/advanced/)
- MappedSuperclass

本页总览

# MappedSuperclass

## 基本使用[​](#基本使用 "基本使用的直接链接")

`org.babyfish.jimmer.sql.MappedSuperclass`用于提供可供实体继承的抽象超类型。

该超类型并不是实体，但可以被多个实体类型继承，从而避免多个实体重复声明相同的属性。

让我们来看一个例子，先定义超类型

- Java
- Kotlin

BaseEntity.java

```@mappedsuperclass
public interface BaseEntity {

    LocalDateTime createdTime();

    @ManyToOne
    User createdBy();

    LocalDateTime modifiedTime();

    @ManyToOne
    User modifiedBy();
}

```

BaseEntity.kt

```@mappedsuperclass
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

- `BookStore`

  - Java
  - Kotlin

  BookStore.java

  ```@entity
  public interface BookStore extends BaseEntity {

      ...省略其他代码...
  }

```

  BookStore.kt

  ```@entity
  interface BookStore : BaseEntity {

      ...省略其他代码...
  }

```

- `Book`

  - Java
  - Kotlin

  Book.java

  ```@entity
  public interface Book extends BaseEntity {

      ...省略其他代码...
  }

```

  Book.kt

  ```@entity
  interface Book : BaseEntity {

      ...省略其他代码...
  }

```

- `Author`

  - Java
  - Kotlin

  Author.java

  ```@entity
  public interface Author extends BaseEntity {

      ...省略其他代码...
  }

```

  Author.kt

  ```@entity
  interface Author : BaseEntity {

      ...省略其他代码...
  }

```

## 多继承[​](#多继承 "多继承的直接链接")

被`MappedSuperclass`修饰的类型支持多继承，其他类型可以从多个`MappedSuperclass`超类型继承。

添加一个新的抽象接口`TenantAware`，所有支持多租户的实体都继承它

- Java
- Kotlin

TenantAware.java

```@mappedsuperclass
public interface TenantAware {

    String tenant();
}

```

TenantAware.kt

```@mappedsuperclass
interface TenantAware {

    val tenant: String
}

```

- Java
- Kotlin

Book.java

```@entity
public interface Book extends BaseEntity, TenantAware {

    ...省略其他代码...
}

```

Book.kt

```@entity
interface Book : BaseEntity, TenantAware {

    ...省略其他代码...
}

```

修改`Book`，让它不光继承`BaseEntity`，还继承`TenantAware`

提示

`@MapperSuperclass`的作用不仅仅是减少重复代码，还可以和其他另外两个功能配合使用

- [全局过滤器](/zh/docs/query/global-filter)
- [拦截器](/zh/docs/mutation/draft-interceptor)

在和它们配合使用时，多继承可以获得良好的灵活性。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mapping/advanced/mapped-super-class.mdx)

最后于 **2025年9月16日** 更新