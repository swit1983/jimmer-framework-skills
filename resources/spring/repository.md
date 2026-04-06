# Spring Data Repository 集成

Jimmer 整合了 Spring Data，为 Java 和 Kotlin 分别提供了 Repository 基接口。

## 定义 Repository

| 语言 | 基接口 |
|------|--------|
| Java | `org.babyfish.jimmer.spring.repository.JRepository<E, ID>` |
| Kotlin | `org.babyfish.jimmer.spring.repository.KRepository<E, ID>` |

范型参数：
- `E` - 实体类型
- `ID` - 实体 ID 类型

### 示例

```java
// Java
package com.example.repository;

import com.example.model.BookStore;
import org.babyfish.jimmer.spring.repository.JRepository;

public interface BookStoreRepository 
        extends JRepository<BookStore, Long> {
}

public interface BookRepository 
        extends JRepository<Book, Long> {
}

public interface AuthorRepository 
        extends JRepository<Author, Long> {
}
```

```kotlin
// Kotlin
package com.example.repository

import com.example.model.BookStore
import org.babyfish.jimmer.spring.repository.KRepository

interface BookStoreRepository : KRepository<BookStore, Long>
```

> 和 Spring Data 一样，定义好接口即可，无需实现，Jimmer 自动实现并注册到 Spring 容器中。
> 不需要 `@Repository` 注解。

## 包扫描

- 默认：自定义 Repository 所在包必须是 Spring Boot 主类所在包或子包
- 如果不在，可以使用 `@EnableJimmerRepositories` 指定包：

```java
@SpringBootApplication
@EnableJimmerRepositories("com.example.custom.repository")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

## 常用方法

Jimmer Repository 继承了常用的 CRUD 方法：

| 方法 | 说明 |
|------|------|
| `findById(ID id)` | 根据 ID 查询单个 |
| `findMapByIds(Iterable<ID> ids)` | 根据一批 ID 查询，返回 Map |
| `findAll()` | 查询所有 |
| `save(E entity)` | 保存实体（调用 Jimmer Save Command）|
| `save(Iterable<E> entities)` | 批量保存 |
| `delete(ID id)` | 根据 ID 删除 |
| `deleteAll(Iterable<ID> ids)` | 批量删除 |
| `existsById(ID id)` | 判断是否存在 |
| `count()` | 统计总数 |

## 配合 DTO 查询

```java
// 直接返回 DTO
List<BookStoreDto> findAll(
        Fetcher<BookStore> fetcher
);

// 分页返回 DTO
Page<BookStoreDto> findAll(
        Pageable pageable,
        Fetcher<BookStore> fetcher
);
```
