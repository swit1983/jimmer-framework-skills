/**
 * Jimmer Fetcher 基础示例 - Kotlin
 * 
 * Fetcher 用于控制查询返回的数据结构形状
 */
package com.example.service

import org.babyfish.jimmer.sql.JSqlClient
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.fetcher.dtoFetcher
import com.example.model.*
import com.example.dto.BookStoreDto
import org.springframework.stereotype.Service
import kotlin.streams.toList

/**
 * 不同形状的查询示例
 */
@Service
class BookService(
    private val jSqlClient: JSqlClient
) {

    /**
     * 示例 1：只查询书籍的基本属性，不查询关联
     * 返回残缺对象，只包含指定属性
     */
    fun findSimpleBooks(): List<Book> {
        // 定义 Fetcher: 只获取 id, name, edition, price
        val simpleBookFetcher = BookFetcher {
            it.id()
            it.name()
            it.edition()
            it.price()
        }

        // 执行查询
        return jSqlClient
            .createQuery(Book) {
                where += table.name ilike "GraphQL"
            }
            .select(simpleBookFetcher)
            .execute()
    }

    /**
     * 示例 2：查询书籍及其书店信息
     * 一级关联嵌套
     */
    fun findBooksWithStore(): List<Book> {
        val bookWithStoreFetcher = BookFetcher {
            it.id()
            it.name()
            it.edition()
            it.price()
            it.store { store ->
                store.id()
                store.name()
                store.website()
            }
        }

        return jSqlClient
            .createQuery(Book)
            .select(bookWithStoreFetcher)
            .execute()
    }

    /**
     * 示例 3：深度嵌套查询
     * 书籍 -> 书店 + 作者，完整树形结构
     */
    fun findBooksWithAll(): List<Book> {
        val bookFullFetcher = BookFetcher {
            it.allScalarFields() // 包含所有标量属性
            it.store {
                it.allScalarFields()
            }
            it.authors {
                it.allScalarFields()
            }
        }

        return jSqlClient
            .createQuery(Book)
            .select(bookFullFetcher)
            .execute()
    }

    /**
     * 示例 4：递归查询自关联
     * 适用于分类、评论等树形结构
     */
    fun findCategoryTree(): List<Category> {
        // 递归定义：查询子类直到最深层次
        lateinit var recursiveFetcher: Fetcher<Category>
        recursiveFetcher = CategoryFetcher {
            it.id()
            it.name()
            it.children { child ->
                child.id()
                child.name()
                child.children(recursiveFetcher)
            }
        }

        return jSqlClient
            .createQuery(Category) {
                where += table.parent.isNull() // 查询根节点
            }
            .select(recursiveFetcher)
            .execute()
    }

    /**
     * 示例 5：限制递归深度
     * 避免无限递归
     */
    fun findCategoryWithMaxDepth(): List<Category> {
        // 最多递归 3 层
        val limitedRecursion = CategoryFetcher {
            it.id()
            it.name()
            it.children(depth = 3) { child ->
                child.id()
                child.name()
            }
        }

        return jSqlClient
            .createQuery(Category) {
                where += table.parent.isNull()
            }
            .select(limitedRecursion)
            .execute()
    }

    /**
     * 示例 6：直接使用 DTO 进行查询
     * 基于 DTO 定义自动生成 Fetcher
     */
    fun findBookStoreDtos(): List<BookStoreDto> {
        // DTO 定义已经说明了需要哪些字段
        // 直接使用 dtoFetcher() 获取
        val fetcher = BookStoreDto.dtoFetcher()

        return jSqlClient
            .createQuery(BookStore)
            .select(fetcher)
            .execute()
    }
}
