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
| `int` | 指定值（如 `1`） | 指定值（如 `0`） |
| `long` | 当前时钟毫秒数 | `0L` |
| `Long`（可空） | 当前时钟毫秒数 | `null` |
| `UUID` | 随机 UUID | 所有字节为 0 的 UUID |
| `UUID`（可空） | 随机 UUID | `null` |
| 枚举 | 指定枚举值（如 `DELETED`） | 指定枚举值（如 `INITIALIZED`） |
| `LocalDateTime`（可空） | 当前时间 | `null` |

> 对于 `long`、`UUID` 等类型的已删除值，默认生成行为可自定义，实现 `LogicalDeletedValueGenerator<T>` 接口。

### 自定义已删除值

```java
// 使用 int 类型，已删除 = 1，未删除 = 0
@Default("0")
@LogicalDeleted("1")
int state();

// 使用枚举
@Default("INITIALIZED")
@LogicalDeleted("DELETED")
Status status();

// 使用 long 类型（自动生成当前毫秒数）
@LogicalDeleted
long deletedMillis();
```

## 中间表逻辑删除

⚠️ **重要**：中间表的逻辑删除配置与实体表不同！

### 场景说明

假设有 `Book` 和 `Author` 的多对多关系，中间表为 `BOOK_AUTHOR_MAPPING`。

需要在中间表增加逻辑删除标记，表示某作者不再参与某书的编写，但保留历史关联记录。

### 配置方式

中间表逻辑删除通过 `@JoinTable` 的 `logicalDeletedFilter` 属性配置：

```java
@Entity
public interface Book {
    
    @ManyToMany
    @JoinTable(
        name = "BOOK_AUTHOR_MAPPING",
        joinColumnName = "BOOK_ID",
        inverseJoinColumnName = "AUTHOR_ID",
        logicalDeletedFilter = @JoinTable.LogicalDeletedFilter(
            columnName = "DELETED",
            type = boolean.class,
            value = "true"
        )
    )
    List<Author> authors();
}
```

### 支持的类型

中间表逻辑删除的类型与实体表类似，都通过 `@JoinTable.LogicalDeletedFilter` 配置：

| 类型 | 示例配置 |
|------|---------|
| `boolean` | `type = boolean.class, value = "true"` |
| `int` | `type = int.class, value = "1", initializedValue = "0"` |
| `long` | `type = long.class`（自动生成当前毫秒数） |
| `UUID` | `type = UUID.class`（自动生成随机 UUID） |

> 如果实体被逻辑删除，相关中间表记录的处理方式取决于配置：
> - 如果 `@JoinTable` 的 `logicalDeletedFilter` 被指定，相关中间表记录会被逻辑删除
> - 如果 `@JoinTable` 的 `deletedWhenEndpointIsLogicallyDeleted = true`，相关中间表记录会被物理删除
> - 否则，相关中间表记录不会被做任何处理

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
