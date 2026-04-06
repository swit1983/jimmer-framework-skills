# Jimmer 常见陷阱与最佳实践

汇总使用 Jimmer 时容易出错的地方和推荐做法。

## 常见陷阱

### 1. 脱钩操作误解

**❌ 错误理解：**
```java
// 以为这样是删除书籍
Book book = BookDraft.$.produce(draft -> {
    draft.setId(1L);
    draft.setStore(null);  // 只是脱钩，不会删除 Book！
});
sqlClient.save(book);
```

**✅ 正确做法：**
```java
// 真正删除书籍
sqlClient.deleteById(Book.class, 1L);

// 或脱钩时删除关联
sqlClient.saveCommand(book)
    .setDissociateAction(BookProps.AUTHORS, DissociateAction.DELETE)
    .execute();
```

---

### 2. Input DTO 空值混淆

**❌ 错误：**
```java
// 前端不传 price 字段，表示不修改价格
// 但误以为不传 = 设为 null
BookInput input = parseJson(json);  // json 中没有 price
sqlClient.save(input);  // price 被设为 null！
```

**✅ 正确理解：**
```java
// Jimmer 自动区分：不传 = 不修改，传 null = 设为 null
BookInput input = parseJson(json);

// 如果需要显式清除，传入 null
// 如果不需要修改，不要包含该字段
```

---

### 3. Fetcher 循环引用

**❌ 错误：**
```java
// Book 有 authors，Author 有 books，循环引用
BookFetcher fetcher = BookFetcher.$
    .name()
    .authors(author -> author
        .firstName()
        .books(book -> book  // 又抓 books！
            .name()
            .authors(...)  // 无限循环！
        )
    );
```

**✅ 正确：**
```java
// 限制抓取深度，避免循环
BookFetcher fetcher = BookFetcher.$
    .name()
    .authors(author -> author
        .firstName()
        // 不抓 books，避免循环
    );

// 或只抓作者的书名
BookFetcher fetcher2 = BookFetcher.$
    .name()
    .authors(author -> author
        .firstName()
        .books(book -> book.name())  // 只抓书名，不深抓
    );
```

---

### 4. 动态查询 N+1

**❌ 错误：**
```java
// 先查主表
List<Book> books = sqlClient
    .createQuery(table)
    .select(table)
    .execute();

// 再循环查关联（N+1！）
for (Book book : books) {
    BookStore store = sqlClient
        .createQuery(storeTable)
        .where(storeTable.id().eq(book.storeId()))
        .select(storeTable)
        .fetchOne();
}
```

**✅ 正确：**
```java
// 使用 Fetcher 一次性抓取
List<Book> books = sqlClient
    .createQuery(table)
    .select(table.fetch(
        BookFetcher.$
            .name()
            .price()
            .store(BookStoreFetcher.$.name())  // 一起抓取
    ))
    .execute();

// 或分批查询（batch loading）
List<Long> storeIds = books.stream()
    .map(Book::storeId)
    .distinct()
    .collect(toList());

Map<Long, BookStore> storeMap = sqlClient
    .createQuery(storeTable)
    .where(storeTable.id().in(storeIds))
    .select(storeTable)
    .execute()
    .stream()
    .collect(toMap(BookStore::id, identity()));
```

---

## 最佳实践

### 1. 实体设计

```java
// ✅ 使用接口而非类
@Entity
public interface Book {  // 不是 class！
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    String name();
    
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    @Nullable  // 可选关联使用 @Nullable
    BookStore store();
}
```

### 2. Repository 设计

```java
// ✅ 基础 CRUD 使用继承
public interface BookRepository extends JRepository<Book, Long> {
    
    // ✅ 复杂查询用 default 方法，内部用 sqlClient
    default List<Book> findByComplexCondition(String name, BigDecimal minPrice) {
        return sql().createQuery(table)
            .whereIf(name != null, table.name().like(name))
            .whereIf(minPrice != null, table.price().ge(minPrice))
            .select(table)
            .execute();
    }
}
```

### 3. Fetcher 使用

```java
// ✅ 定义可复用的 Fetcher 常量
public class BookFetchers {
    public static final BookFetcher SIMPLE = BookFetcher.$.name().price();
    
    public static final BookFetcher WITH_STORE = BookFetcher.$
        .name()
        .price()
        .store(BookStoreFetcher.$.name());
    
    public static final BookFetcher DETAIL = BookFetcher.$
        .allScalarFields()
        .store(BookStoreFetcher.$.allScalarFields())
        .authors(AuthorFetcher.$.allScalarFields());
}

// ✅ 使用
public Book findById(Long id) {
    return bookRepository.findById(id, BookFetchers.WITH_STORE)
        .orElseThrow(() -> new EntityNotFoundException("Book not found: " + id));
}
```

### 4. 保存操作

```java
// ✅ 使用 Draft 构造对象
@Service
public class BookService {
    
    @Autowired
    private JSqlClient sqlClient;
    
    public Book createBook(BookCreateRequest request) {
        Book book = BookDraft.$.produce(draft -> {
            draft.setName(request.getName());
            draft.setPrice(request.getPrice());
            draft.setStoreId(request.getStoreId());
        });
        
        return sqlClient.save(book).getModifiedEntity();
    }
    
    public Book updateBook(Long id, BookUpdateRequest request) {
        Book book = BookDraft.$.produce(draft -> {
            draft.setId(id);  // 有 ID = 更新
            
            // 只更新传了值的字段
            if (request.getName() != null) {
                draft.setName(request.getName());
            }
            if (request.getPrice() != null) {
                draft.setPrice(request.getPrice());
            }
        });
        
        return sqlClient.save(book).getModifiedEntity();
    }
}
```

### 5. 使用 Input DTO

```java
// ✅ 定义 Input DTO（在 .dto 文件中）
// BookInput.dto
/*
input BookInput {
    id
    name
    price
    storeId
}
*/

// ✅ 直接使用 Input DTO 保存
@RestController
@RequestMapping("/api/books")
public class BookController {
    
    @Autowired
    private JSqlClient sqlClient;
    
    @PostMapping
    public Book create(@RequestBody BookInput input) {
        // Input DTO 可以直接用于 save
        return sqlClient.save(input).getModifiedEntity();
    }
    
    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody BookInput input) {
        // 确保 ID 正确
        BookInput finalInput = input.toBuilder()
            .id(id)
            .build();
        return sqlClient.save(finalInput).getModifiedEntity();
    }
}
```

### 6. 异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(SaveException.class)
    public ResponseEntity<ErrorResponse> handleSaveException(SaveException e) {
        // 保存失败（如违反唯一约束）
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("Save failed: " + e.getMessage()));
    }
    
    @ExceptionHandler(FetchByIdNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(FetchByIdNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("Entity not found"));
    }
}
```

---

## 总结

遵循这些最佳实践，可以：
- 减少重复代码
- 避免常见陷阱
- 提高代码可维护性
- 充分发挥 Jimmer 的优势
