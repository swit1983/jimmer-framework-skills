/**
 * Jimmer 实体定义示例 - Java
 * 
 * Jimmer 实体声明为 interface，而非 class
 * 由预编译器自动生成实现
 */
package com.example.model;

import org.babyfish.jimmer.Entity;
import org.babyfish.jimmer.Id;
import org.babyfish.jimmer.ManyToOne;
import org.babyfish.jimmer.OneToMany;
import org.babyfish.jimmer.ManyToMany;
import org.babyfish.jimmer.GeneratedValue;
import org.babyfish.jimmer.GenerationType;

import java.math.BigDecimal;
import java.util.List;

/**
 * 书籍实体
 */
@Entity
public interface Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    String name();

    int edition();

    BigDecimal price();

    /**
     * 多对一关联：一本书属于一个书店
     */
    @ManyToOne
    BookStore store();

    /**
     * 多对多关联：一本书有多个作者
     */
    @ManyToMany
    List<Author> authors();
}

/**
 * 书店实体
 */
@Entity
public interface BookStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    String name();

    String website();

    /**
     * 一对多关联：一个书店有多本书
     */
    @OneToMany(mappedBy = "store")
    List<Book> books();
}

/**
 * 作者实体
 */
@Entity
public interface Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();

    String firstName();

    String lastName();

    Gender gender();

    /**
     * 多对多关联：一个作者有多本书
     */
    @ManyToMany(mappedBy = "authors")
    List<Book> books();
}

/**
 * 性别枚举
 */
enum Gender {
    MALE,
    FEMALE
}
