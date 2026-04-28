# Jimmer 快速开始

5 分钟上手 Jimmer ORM。

## 1. 添加依赖

### Maven

```xml
<dependency>
    <groupId>org.babyfish.jimmer</groupId>
    <artifactId>jimmer-spring-boot-starter</artifactId>
    <version>0.8.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'org.babyfish.jimmer:jimmer-spring-boot-starter:0.8.0'
```

## 2. 配置数据库

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

jimmer:
  dialect: org.babyfish.jimmer.sql.dialect.MySqlDialect
  show-sql: true
```

## 3. 定义实体

```java
package com.example.model;

import org.babyfish.jimmer.sql.*;

@Entity
public interface Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    String name();
    
    BigDecimal price();
    
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    BookStore store();
}
```

```java
package com.example.model;

import org.babyfish.jimmer.sql.*;
import java.util.List;

@Entity
public interface BookStore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    String name();
    
    @OneToMany(mappedBy = "store")
    List<Book> books();
}
```

## 4. 使用 Repository

```java
package com.example.repository;

import com.example.model.Book;
import org.babyfish.jimmer.spring.repository.JRepository;

public interface BookRepository extends JRepository<Book, Long> {
    // 基础 CRUD 已内置，无需编写
}
```

## 5. 查询数据

```java
@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private JSqlClient sqlClient;
    
    // 1. 根据 ID 查询
    public Book findById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }
    
    // 2. 使用 Fetcher 抓取指定字段
    public Book findByIdWithStore(Long id) {
        return bookRepository.findById(
            id, 
            BookFetcher.$
                .name()
                .price()
                .store(BookStoreFetcher.$.name())
        ).orElse(null);
    }
    
    // 3. 动态查询
    public List<Book> search(String name, BigDecimal minPrice) {
        BookTable table = BookTable.$;
        return sqlClient
            .createQuery(table)
            .whereIf(
                name != null && !name.isEmpty(),
                table.name().like(name)
            )
            .whereIf(
                minPrice != null,
                table.price().ge(minPrice)
            )
            .orderBy(table.name())
            .select(table)
            .execute();
    }
    
    // 4. 分页查询
    public Page<Book> findPage(int pageIndex, int pageSize) {
        return bookRepository.findAll(
            Pageable.ofSize(pageSize).withPage(pageIndex),
            BookFetcher.$.allScalarFields()
        );
    }
}
```

## 6. 保存数据

```java
@Service
public class BookService {
    
    @Autowired
    private JSqlClient sqlClient;
    
    // 1. 简单保存
    public Book saveBook(String name, BigDecimal price) {
        Book book = BookDraft.$.produce(draft -> {
            draft.setName(name);
            draft.setPrice(price);
        });
        return sqlClient.save(book).getModifiedEntity();
    }
    
    // 2. 更新指定字段
    public Book updateBookPrice(Long id, BigDecimal newPrice) {
        Book book = BookDraft.$.produce(draft -> {
            draft.setId(id);  // 有 ID 表示更新
            draft.setPrice(newPrice);
            // name 未设置，不会被修改
        });
        return sqlClient.save(book).getModifiedEntity();
    }
    
    // 3. 级联保存（书 + 作者）
    public Book saveBookWithAuthors(
            String bookName, 
            List<AuthorInput> authorInputs) {
        
        Book book = BookDraft.$.produce(draft -> {
            draft.setName(bookName);
            
            for (AuthorInput authorInput : authorInputs) {
                draft.addIntoAuthors(author -> {
                    author.setFirstName(authorInput.getFirstName());
                    author.setLastName(authorInput.getLastName());
                });
            }
        });
        
        return sqlClient
            .saveCommand(book)
            .setAssociatedMode(BookProps.AUTHORS, AssociatedSaveMode.MERGE)
            .execute()
            .getModifiedEntity();
    }
}
```

## 7. 删除数据

```java
@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private JSqlClient sqlClient;
    
    // 1. 根据 ID 删除
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }
    
    // 2. 批量删除
    public void deleteByIds(List<Long> ids) {
        bookRepository.deleteAllById(ids);
    }
    
    // 3. 级联删除（删除书籍时同时删除关联的中间表记录）
    public void deleteBookWithAuthors(Long bookId) {
        // 使用 save 指令的脱钩功能
        Book book = BookDraft.$.produce(draft -> {
            draft.setId(bookId);
            draft.setAuthors(Collections.emptyList());  // 清空关联
        });
        
        sqlClient
            .saveCommand(book)
            .setDissociateAction(BookProps.AUTHORS, DissociateAction.DELETE)
            .execute();
        
        // 再删除书籍本身
        sqlClient.deleteById(Book.class, bookId);
    }
}
```

## 下一步

- 深入学习 [对象抓取器](../query/fetcher.md)
- 了解 [保存指令详解](../save/save-command.md)
- 查看 [动态 JOIN](../query/dynamic-join.md)
