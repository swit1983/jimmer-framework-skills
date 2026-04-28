# 视图属性

视图属性（View Properties）是 Jimmer 的特色功能，用于简化常用关联查询，避免编写重复代码。

## 概念

在实际开发中，经常需要：
- 查询对象时，同时获取其关联对象的 ID
- 查询多对多关联时，只需要关联对象的 ID 列表

传统方式需要写 Fetcher 或手动处理，视图属性将这些常用模式内置为实体属性。

## @IdView - 关联对象 ID

将关联对象属性替换为其 ID。

### 多对一 / 一对一

```java
@Entity
public interface Book {
    @Id
    long id();
    
    String name();
    
    // 关联属性
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    BookStore store();
    
    // 视图属性：直接获取 store 的 ID
    @IdView("store")
    Long storeId();
}
```

```kotlin
@Entity
interface Book {
    @Id
    val id: Long
    
    val name: String
    
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    val store: BookStore
    
    @IdView("store")
    val storeId: Long?
}
```

使用：

```java
// 查询时自动处理，无需 JOIN store 表
List<Book> books = sqlClient
    .createQuery(table)
    .select(table.fetch(
        BookFetcher.$
            .name()
            .storeId()  // 只需要 ID，无需获取完整 store 对象
    ))
    .execute();
```

### 一对多 / 多对多

```java
@Entity
public interface Book {
    @ManyToMany
    List<Author> authors();
    
    // 视图属性：获取所有 author 的 ID 列表
    @IdView("authors")
    List<Long> authorIds();
}
```

```kotlin
@Entity
interface Book {
    @ManyToMany
    val authors: List<Author>
    
    @IdView("authors")
    val authorIds: List<Long>
}
```

## @ManyToManyView - 多对多简化

当多对多中间表有业务字段时，需要将中间表建模为独立实体。但很多时候上层业务只关心两端实体的直接关联，`@ManyToManyView` 可以在这种情况下模拟出简洁的多对多关联效果。

### 使用 @ManyToManyView

首先，定义基础关联（中间表实体 + 双向一对多）：

```java
@Entity
public interface BookAuthorMapping {
    @Id
    @ManyToOne
    @JoinColumn(name = "BOOK_ID")
    Book book();

    @Id
    @ManyToOne
    @JoinColumn(name = "AUTHOR_ID")
    Author author();

    // 中间表特有字段
    int order();
}

@Entity
public interface Book {
    @OneToMany(mappedBy = "book")
    List<BookAuthorMapping> authorMappings();
}

@Entity
public interface Author {
    @OneToMany(mappedBy = "author")
    List<BookAuthorMapping> bookMappings();
}
```

然后，用 `@ManyToManyView` 在此基础上模拟多对多关联：

```java
@Entity
public interface Book {
    @OneToMany(mappedBy = "book")
    List<BookAuthorMapping> authorMappings();

    // 视图属性：直接获取关联的作者
    @ManyToManyView(prop = "authorMappings", deeperProp = "author")
    List<Author> authors();
}
```

`prop` 指向当前实体的一对多属性，`deeperProp` 指向中间表实体指向目标实体的多对一属性。

```kotlin
@Entity
interface Book {
    @OneToMany(mappedBy = "book")
    val authorMappings: List<BookAuthorMapping>
    
    @ManyToManyView(prop = "authorMappings", deeperProp = "author")
    val authors: List<Author>
}
```

## 对比总结

| 注解 | 用途 | 替代代码 |
|------|------|---------|
| `@IdView` | 获取关联对象的 ID | `fetcher.xxxId()` 或手动转换 |
| `@ManyToManyView` | 简化多对多中间表查询 | 显式的 `@OneToMany` + `@ManyToOne` 链 |

## 使用建议

1. **优先使用 `@IdView`**：当只需要关联对象 ID 时，避免加载完整对象
2. **`@ManyToManyView` 按需使用**：当中间表有业务字段导致无法使用简单多对多映射时，可用此注解模拟多对多效果
3. **简单多对多**：如果中间表无业务字段，直接使用 `@ManyToMany` + `@JoinTable` 即可
