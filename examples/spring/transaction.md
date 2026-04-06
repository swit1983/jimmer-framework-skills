# Spring 事务整合示例

Jimmer 天然支持 Spring 事务整合。

## 基本用法

```java
@Service
public class BookService {
    
    @Autowired
    private JSqlClient sqlClient;
    
    @Transactional
    public Book saveBook(Book book) {
        // Jimmer 自动参与 Spring 事务
        return sqlClient.save(book);
    }
}
```

## 事务模板

如果你需要编程式事务：

```java
@Service
public class BookService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    public Book saveBook(Book book) {
        return transactionTemplate.execute(status -> {
            return sqlClient.save(book);
        });
    }
}
```

## 要点

- Jimmer 利用 Spring 的 Connection 管理
- 自动参与当前事务，无需额外配置
- 事务回滚自动生效
- 支持 Propagation 嵌套事务

## 缓存一致性

如果你使用 **Transaction Trigger**，缓存一致性会自动在事务提交后维护：

```yaml
jimmer:
  cache:
    enabled: true
    transaction-trigger:
      enabled: true
```

这样任何通过 Jimmer 的修改都会自动更新缓存。

