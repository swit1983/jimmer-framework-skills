# 基础映射示例

包含简单类型、关联关系（一对一/多对一/一对多/多对多）的基础映射示例。

## 定义实体

Jimmer 使用 interface 定义实体，而不是 class。

### 简单实体示例

#### Java
```java
package com.example.model;

import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

@Entity
public interface BookStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    @Key
    String name();
    
    @Nullable
    String website();
}
```

#### Kotlin
```kotlin
package com.example.model

import org.babyfish.jimmer.sql.*

@Entity
interface BookStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
    
    @Key
    val name: String
    
    val website: String?
}
```

## 关联映射

### 多对一

#### Java
```java
@Entity
public interface Book {
    // ... 其他属性
    
    @ManyToOne
    @Nullable
    BookStore store();
}
```

#### Kotlin
```kotlin
@Entity
interface Book {
    // ... 其他属性
    
    @ManyToOne
    val store: BookStore?
}
```

### 一对多（反向）

#### Java
```java
@Entity
public interface BookStore {
    // ... 其他属性
    
    @OneToMany(mappedBy = "store")
    List<Book> books();
}
```

#### Kotlin
```kotlin
@Entity
interface BookStore {
    // ... 其他属性
    
    @OneToMany(mappedBy = "store")
    val books: List<Book>
}
```

### 多对多

#### 主动端
```java
@Entity
public interface Book {
    // ... 其他属性
    
    @ManyToMany
    @JoinTable(
        name = "BOOK_AUTHOR_MAPPING",
        joinColumnName = "BOOK_ID",
        inverseJoinColumnName = "AUTHOR_ID"
    )
    List<Author> authors();
}
```

#### 镜像端
```java
@Entity
public interface Author {
    // ... 其他属性
    
    @ManyToMany(mappedBy = "authors")
    List<Book> books();
}
```

### 树形递归（自关联）

```java
@Entity
public interface TreeNode {
    @Id
    @Column(name = "NODE_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    String name();
    
    @ManyToOne
    @Nullable
    TreeNode parent();
    
    @OneToMany(mappedBy = "parent")
    List<TreeNode> childNodes();
}
```

## 枚举映射

### 字符串映射（默认）

```java
package com.example.model;

import org.babyfish.jimmer.sql.EnumItem;

@EnumType(EnumType.Strategy.NAME)
public enum Gender {
    @EnumItem(name = "M")
    MALE,
    
    @EnumItem(name = "F")
    FEMALE
}
```

### 整数映射

```java
@EnumType(EnumType.Strategy.ORDINAL)
public enum Gender {
    @EnumItem(ordinal = 100)
    MALE,
    
    @EnumItem(ordinal = 200)
    FEMALE
}
```

## 注意要点

- Jimmer 实体使用 **interface**，原因：动态性 + 不可变性
- **未设置属性** vs **属性为 null** 是不同的概念
- 关联关系必须指定 `mappedBy` 表示镜像端
- 多对多必须一端主动一端镜像

