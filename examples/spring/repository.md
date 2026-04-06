# Spring Data 风格 Repository 示例

Jimmer 支持 Spring Data 风格的 Repository 用法。

## 基本示例

```java
// 定义 Repository
public interface BookRepository extends JimmerRepository<Book, Long> {
    // 自动实现基本 CRUD
}
```

## 使用

```java
@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    public Book findById(Long id) {
        return bookRepository.findNullable(id);
    }
    
    public Book save(Book book) {
        return bookRepository.save(book);
    }
    
    public void delete(Long id) {
        bookRepository.delete(id);
    }
}
```

## 结合对象抓取器

```java
public List<Book> findBooksWithStore() {
    return bookRepository
        .findAll(fetcher -> fetcher
            .allScalarFields()
            .store(storeFetcher -> storeFetcher
                .allScalarFields()
            )
        );
}
```

## 复杂查询

```java
public List<Book> findByPriceBetween(BigDecimal min, BigDecimal max) {
    return bookRepository
        .findAll(
            query -> query
                .where(query.price().between(min, max))
                .orderBy(query.price().asc())
        );
}
```

## 优点对比 Spring Data JPA

- 原生支持 Jimmer 对象抓取器
- 原生支持动态查询
- 更好的 JOIN 优化
- 继承 `JimmerRepository` 获得通用方法

## 常见 CRUD 方法

| 方法 | 说明 |
|------|------|
| `findNullable(ID id)` | 查询，返回 null 表示不存在 |
| `findRequired(ID id)` | 查询，不存在抛出异常 |
| `save(E entity)` | 保存实体 |
| `delete(ID id)` | 删除实体 |
| `findAll()` | 查询所有 |
| `findAll(Consumer<Fetcher<E>>)` | 指定抓取配置查询所有 |
| `findAll(Consumer<Query<E>>)` | 动态查询 |

