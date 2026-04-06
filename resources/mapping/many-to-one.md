# 多对一关联

使用 `@org.babyfish.jimmer.sql.ManyToOne` 注解声明多对一关联。

有两种实现方式：基于外键和基于中间表。

## 1. 基于外键（推荐）

```java
// Java
@Entity
public interface Book {
    @ManyToOne
    BookStore store();
    
    // ...
}
```

如果不指定 `@JoinColumn`，Jimmer 会根据命名策略自动推导外键列名。默认策略下，`store` → `STORE_ID`，所以等价于：

```java
@Entity
public interface Book {
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    BookStore store();
    
    // ...
}
```

数据库约束：
```sql
ALTER TABLE BOOK
ADD CONSTRAINT FK_BOOK__BOOK_STORE
FOREIGN KEY(STORE_ID) REFERENCES BOOK_STORE(ID);
```

## 2. 基于中间表

用于兼容已有数据库设计：

```java
@Entity
public interface Book {
    @Nullable
    @ManyToOne
    @JoinTable
    BookStore store();
    
    // ...
}
```

如果不指定属性，Jimmer 自动推导：
- 中间表名：`BOOK_BOOK_STORE_MAPPING`
- 当前实体外键列：`BOOK_ID`
- 关联实体外键列：`STORE_ID`

等价于完整写法：

```java
@Entity
public interface Book {
    @Nullable
    @ManyToOne
    @JoinTable(
        name = "BOOK_BOOK_STORE_MAPPING",
        joinColumnName = "BOOK_ID",
        inverseJoinColumnName = "STORE_ID"
    )
    BookStore store();
    
    // ...
}
```

中间表定义：

```sql
CREATE TABLE BOOK_BOOK_STORE_MAPPING(
    BOOK_ID bigint NOT NULL,
    STORE_ID bigint NOT NULL,
    PRIMARY KEY (BOOK_ID, STORE_ID),
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK(ID),
    FOREIGN KEY (STORE_ID) REFERENCES BOOK_STORE(ID),
    -- 这个唯一约束非常重要，否则就是多对多了
    UNIQUE (BOOK_ID)
);
```

⚠️ **注意事项**：
- 除非兼容已有设计，否则多对一建议直接使用外键，不建议使用中间表
- 使用中间表映射多对一，关联属性必须可空（因为不能保证每个实体在中间表一定有对应数据）

## 总结

| 方式 | 使用场景 |
|------|----------|
| 外键 | 新建项目，推荐 |
| 中间表 | 兼容已有数据库设计 |
