# 多对多关联

使用 `@org.babyfish.jimmer.sql.ManyToMany` 注解声明多对多关联。

多对多支持双向关联：
- **主动方**（必须）：真正的数据库映射，实现单向多对多
- **从动方**（可选）：作为主动方的镜像，形成双向关联

和 JPA/Hibernate 不同：**主动方和从动方都可以用于保存关联**。

## 示例：Book ↔ Author 双向多对多

### 主动方：Book.authors

```java
@Entity
public interface Book {
    @ManyToMany
    List<Author> authors();
    
    // ...
}
```

如果不指定 `@JoinTable`，Jimmer 根据命名策略自动推导：
- 中间表名：`BOOK_AUTHOR_MAPPING`
- 当前实体外键：`BOOK_ID`
- 关联实体外键：`AUTHOR_ID`

等价于完整写法：

```java
@Entity
public interface Book {
    @ManyToMany
    @JoinTable(
        name = "BOOK_AUTHOR_MAPPING",
        joinColumnName = "BOOK_ID",
        inverseJoinColumnName = "AUTHOR_ID"
    )
    List<Author> authors();
    
    // ...
}
```

中间表定义：

```sql
CREATE TABLE BOOK_AUTHOR_MAPPING (
    BOOK_ID bigint NOT NULL,
    AUTHOR_ID bigint NOT NULL,
    PRIMARY KEY (BOOK_ID, AUTHOR_ID),
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID),
    FOREIGN KEY (AUTHOR_ID) REFERENCES AUTHOR(ID)
);
```

### 从动方：Author.books

使用 `mappedBy` 指定它是对方的镜像：

```java
@Entity
public interface Author {
    // `mappedBy = "authors"` 表示 Author.books 是 Book.authors 的镜像
    @ManyToMany(mappedBy = "authors")
    List<Book> books();
    
    // ...
}
```

## 重要规则

| 规则 | 说明 |
|------|------|
| `@ManyToMany` 关联必须非 null | 如果没有关联，返回空集合（长度 0），不是 null |
| 从动方使用 `mappedBy` | 不能再使用 `@JoinTable` |
| 中间表不能有额外业务字段 | 如果需要额外业务字段，请参见 `ManyToManyView` |

## 中间表添加业务字段

如果需要在多对多中间表中定义其他业务字段（比如排序号、创建时间），请参考：
[ManyToManyView →](../mapping/advanced/view/many-to-many-view.md)
