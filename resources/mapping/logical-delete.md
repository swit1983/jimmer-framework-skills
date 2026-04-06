# 逻辑删除

Jimmer 支持实体表和中间表的逻辑删除。

## 实体表逻辑删除

### 基本用法

使用 `@LogicalDeleted` 注解标记逻辑删除字段：

```java
@Entity
public interface Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    String name();
    
    // 逻辑删除标记
    @LogicalDeleted("true")
    boolean deleted();
}
```

```kotlin
@Entity
interface Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
    
    val name: String
    
    // 逻辑删除标记
    @LogicalDeleted("true")
    val deleted: Boolean
}
```

### 支持的类型

| 类型 | 已删除值 | 未删除值 |
|------|---------|---------|
| `boolean` | `true` | `false` |
| `int` | 非零 | 0 |
| `long` | 非零 | 0 |
| `String` | 非空字符串 | `null` |
| `UUID` | 非空 | `null` |
| 枚举 | 非空 | `null` |

### 自定义已删除值

```java
// 使用 int 类型，已删除 = 1，未删除 = 0
@LogicalDeleted("1")
int deleted();

// 使用 String 类型
@LogicalDeleted("'Y'")
String deleted();

// 使用枚举
@LogicalDeleted("DELETED")
Status status();
```

## 中间表逻辑删除

⚠️ **重要**：中间表的逻辑删除配置与实体表不同！

### 场景说明

假设有 `Book` 和 `Author` 的多对多关系，中间表为 `BOOK_AUTHOR_MAPPING`。

需要在中间表增加逻辑删除标记，表示某作者不再参与某书的编写，但保留历史关联记录。

### 配置方式

**1. 中间表实体定义**

```java
@Entity
@Table(name = "BOOK_AUTHOR_MAPPING")
public interface BookAuthorMapping {
    
    @Id
    @Column(name = "BOOK_ID")
    long bookId();
    
    @Id
    @Column(name = "AUTHOR_ID")
    long authorId();
    
    // 中间表的逻辑删除标记！
    @LogicalDeleted("true")
    @Column(name = "DELETED")
    boolean deleted();
}
```

**2. 关联映射中使用中间表实体**

```java
@Entity
public interface Book {
    
    @ManyToMany
    @JoinTable(
        name = "BOOK_AUTHOR_MAPPING",
        joinColumns = @JoinColumn(name = "BOOK_ID"),
        inverseJoinColumns = @JoinColumn(name = "AUTHOR_ID")
    )
    List<Author> authors();
    
    // 使用中间表实体定义关联
    @ManyToMany(mappedBy = "book")
    List<BookAuthorMapping> authorMappings();
}

@Entity
public interface Author {
    
    @ManyToMany(mappedBy = "authors")
    List<Book> books();
    
    @OneToMany(mappedBy = "author")
    List<BookAuthorMapping> bookMappings();
}

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
    
    @LogicalDeleted("true")
    boolean deleted();
}
```

### 关键区别

| 场景 | 实体表逻辑删除 | 中间表逻辑删除 |
|------|---------------|---------------|
| 注解位置 | 实体本身的字段 | 中间表实体/关联实体 |
| 影响范围 | 实体本身是否可见 | 关联关系是否有效 |
| 查询行为 | 自动过滤已删除 | 通过关联过滤 |

## 全局过滤器

逻辑删除本质上是全局过滤器的特例。可以自定义全局过滤器：

```java
@Component
public class DeletedFilter implements Filter<Book> {
    
    @Override
    public void filter(FilterArgs<Book> args) {
        // 添加过滤条件
        args.where(args.getTable().deleted().eq(false));
    }
    
    @Override
    public boolean isAffectedBy(EntityEvent<?> event) {
        // 判断是否需要刷新缓存
        return event.isChanged(BookProps.DELETED);
    }
}
```

更多信息见 [全局过滤器](./global-filter.md)。
