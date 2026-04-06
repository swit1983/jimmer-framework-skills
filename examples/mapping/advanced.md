# 进阶映射示例

包含逻辑删除、计算属性、视图属性、复合字段等进阶映射示例。

## 逻辑删除

### 实体表

```java
@Entity
public interface Book {
    @Id
    long id();
    
    // ... 其他属性
    
    @LogicalDeleted
    boolean deleted();
}
```

### 中间表（多对多）

```java
@Entity
public interface Book {
    @ManyToMany
    @JoinTable(name = "BOOK_AUTHOR_MAPPING")
    @LogicalDeleted("deleted")
    List<Author> authors();
}
```

## 计算属性

### formula（简单计算）

```java
@Entity
public interface Book {
    @Id
    long id();
    
    String name();
    
    BigDecimal price();
    
    @Formula("price * 0.8")
    BigDecimal discountPrice();
}
```

### transient（复杂计算）

```java
@Entity
public interface Book {
    @Id
    long id();
    
    @ManyToOne
    BookStore store();
    
    @Transient
    @TransientResolver(BookStoreDiscountResolver.class)
    BigDecimal discount();
}

// 自己实现 TransientResolver
public class BookStoreDiscountResolver 
    implements TransientResolver<Book, BigDecimal> {
    
    @Override
    public BigDecimal resolve(Book book, SqlClient sqlClient) {
        BigDecimal basePrice = book.price();
        BookStore store = book.store();
        if (store == null) {
            return basePrice;
        }
        return basePrice.multiply(BigDecimal.ONE.subtract(store.discount()));
    }
}
```

## 视图属性

### IdView - 仅需要关联 id

```java
@Entity
public interface Book {
    @Id
    long id();
    
    @IdView("store")
    Long storeId();
    
    @ManyToOne
    Store store();
}
```

### ManyToManyView - 多对多仅获取 id 列表

```java
@Entity
public interface Book {
    @Id
    long id();
    
    @ManyToMany
    @JoinTable(name = "BOOK_AUTHOR_MAPPING")
    List<Author> authors();
    
    @IdView("authors")
    List<Long> authorIds();
}
```

## 复合字段（Embedded）

```java
@Embedded
public interface Address {
    String street();
    
    String city();
    
    String zipCode();
}

@Entity
public interface User {
    @Id
    long id();
    
    String name();
    
    @Embedded
    Address address();
}
```

## 枚举映射

```java
@EnumType(EnumType.Strategy.NAME)
public enum Gender {
    @EnumItem(name = "M")
    MALE,
    
    @EnumItem(name = "F")
    FEMALE
}
```

## JSON 映射

```java
@Entity
public interface Book {
    @Id
    long id();
    
    @Json
    Map<String, Object> metadata();
}
```

## 非结构化映射（Join SQL）

适用于遗留数据库非 3NF 设计。

```java
// 假设 book.author_ids 是逗号分隔的 id 列表
// 需要自定义 SQL 函数判断是否包含

@Entity
public interface Book {
    @Id
    long id();
    
    @ManyToMany
    @JoinSql(
        "concatenation_string_contains(%alias.author_ids, %target_alias.id)"
    )
    List<Author> authors();
}
```

## 逻辑删除 - 进阶说明

| 类型 | 配置位置 | 说明 |
|------|----------|------|
| 实体表 | 在实体属性上加 `@LogicalDeleted` | 自动过滤已删除实体 |
| 多对多中间表 | 在关联属性上加 `@LogicalDeleted("deleted")` | 自动过滤已删除中间表行 |

## 计算属性 - 两种方式对比

| 方式 | 使用场景 | 说明 |
|------|----------|------|
| `@Formula` | 简单 SQL 计算 | 直接在 SQL 中计算，无需编码 |
| `@Transient` | 复杂业务计算 | 需要自己实现 `TransientResolver`，可以访问数据库 |

## ⚠️ 易错点

- **非结构化关联** `@JoinSql` 不支持保存，也不支持缓存、远程关联，只能用于查询
- **多对多视图** `ManyToManyView` 是 Jimmer 特有特性，可以减少 DTO 编写
- **逻辑删除** 实体表和中间表配置方式不同

