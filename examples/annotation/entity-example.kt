/**
 * Jimmer 实体定义示例 - Kotlin
 * 
 * Jimmer 实体声明为 interface，而非 class
 * 由预编译器自动生成实现
 */
package com.example.model

import org.babyfish.jimmer.Entity
import org.babyfish.jimmer.Id
import org.babyfish.jimmer.ManyToOne
import org.babyfish.jimmer.OneToMany
import org.babyfish.jimmer.ManyToMany
import org.babyfish.jimmer.GeneratedValue
import org.babyfish.jimmer.GenerationType
import java.math.BigDecimal

/**
 * 书籍实体
 */
@Entity
interface Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val name: String

    val edition: Int

    val price: BigDecimal

    /**
     * 多对一关联：一本书属于一个书店
     */
    @ManyToOne
    val store: BookStore?

    /**
     * 多对多关联：一本书有多个作者
     */
    @ManyToMany
    val authors: List<Author>
}

/**
 * 书店实体
 */
@Entity
interface BookStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val name: String

    val website: String

    /**
     * 一对多关联：一个书店有多本书
     */
    @OneToMany(mappedBy = "store")
    val books: List<Book>
}

/**
 * 作者实体
 */
@Entity
interface Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val firstName: String

    val lastName: String

    val gender: Gender

    /**
     * 多对多关联：一个作者有多本书
     */
    @ManyToMany(mappedBy = "authors")
    val books: List<Book>
}

/**
 * 性别枚举
 */
enum class Gender {
    MALE,
    FEMALE
}
