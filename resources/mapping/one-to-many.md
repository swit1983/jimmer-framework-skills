# 一对多关联

和 JPA 类似但有区别：**Jimmer 不支持单向一对多关联，一对多关联只能作为多对一关联的镜像**。

也就是说，一对多关联必然意味着双向关联。

## 示例

多对一（Book → BookStore）：
```java
@Entity
public interface Book {
    @ManyToOne
    @JoinColumn(name = "STORE_ID")
    BookStore store();
    
    // ...
}
```

一对多（BookStore → Book），作为 `Book.store` 的镜像：

```java
@Entity
public interface BookStore {
    // `mappedBy` 表示 `books` 是 `Book.store` 的镜像
    @OneToMany(mappedBy = "store")
    List<Book> books();
    
    // ...
}
```

## 重要规则

1. **@OneToMany 必须使用 `mappedBy`**，指向对方的多对一关联属性
2. **不得使用 @JoinColumn 或 @JoinTable**，因为一对多只是镜像，真正的外键在对方那边
3. **@OneToMany 关联属性必须非 null**
   - 如果查询父对象并要求抓取一对多关联，但父对象没有子对象，该属性是空集合（长度为 0），不是 null

## 总结

| 特性 | 说明 |
|------|------|
| 支持单向一对多 | ❌ 不支持 |
| 必须双向 | ✅ 一对多必须是多对一的镜像 |
| 必须用 mappedBy | ✅ 强制要求 |
| 禁止 @JoinColumn | ✅ 禁止 |
| 属性必须非空 | ✅ 空集合表示没有子对象 |
