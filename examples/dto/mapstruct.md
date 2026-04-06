# MapStruct 集成示例

Jimmer 可以和 MapStruct 集成，如果你已经在使用 MapStruct 可以继续使用。

## 基本示例

### 实体
```java
@Entity
public interface Book {
    @Id
    long id();
    String name();
    BigDecimal price();
    @ManyToOne
    BookStore store();
}
```

### DTO
```java
public class BookDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Long storeId;
    // getters/setters...
}
```

### MapStruct Mapper
```java
@Mapper
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);
    
    BookDTO toDTO(Book book);
    
    Book toEntity(BookDTO dto);
}
```

## Jimber 与 MapStruct 配合

在 Input DTO 保存场景：

```java
@RestController
public class BookController {
    @PostMapping("/books")
    public Book save(@RequestBody BookDTO dto) {
        Book book = BookMapper.INSTANCE.toEntity(dto);
        return sqlClient.save(book);
    }
}
```

## Input DTO 对比

| 方式 | 优点 | 缺点 |
|------|------|------|
| Jimmer DTO 语言 | 极少代码，自动支持关联 | 需要学习新语法 |
| MapStruct | 已有生态集成 | 需要手写 DTO 和 Mapper |

## Jimmer 推荐方式

Jimmer 推荐使用 **Jimmer 原生 DTO 语言**：
- 更加简洁，代码量极少
- 原生支持关联、递归
- 编译时检查
- 可以和 MapStruct 混用，按需选择

