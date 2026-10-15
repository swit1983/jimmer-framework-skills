/**
 * Jimmer Fetcher 基础示例 - Kotlin
 *
 * Fetcher 用于描述查询返回的数据结构（对象图）形状。
 *
 * ⚠️ 关于 Kotlin 的 Fetcher 构造：
 * Kotlin 侧构造 Fetcher 的 DSL（如 `newFetcher(Book::class)` 的配套扩展、
 * `table.fetchBy { ... }`）由 KSP 在编译期生成，因此在 jimmer 主仓库源码中查不到其定义。
 * 本文件中凡涉及"抓取指定形状"的用法，均给出可确证的写法，
 * 更复杂的 Fetcher 写法请以同目录下的 Java 示例 `basic-fetch-example.java`
 * 与官方文档 `docs/query/object-fetcher/` 为准。
 *
 * 核心约定（Java / Kotlin 通用）：
 *  1. Fetcher 不能直接传给 select，必须包装成 Selection：select(table.fetch(fetcher))
 *  2. View DTO 查询用：select(table.fetch(SomeDto::class))
 *  3. 递归抓取用编译时生成的递归方法（Java 为 recursiveXxx()，Kotlin 为 `xxx*`()）
 */
package com.example.service

import com.example.dto.BookStoreDto
import com.example.model.Book
import com.example.model.BookStore
import com.example.model.Category
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

/**
 * 不同形状的查询示例
 */
@Service
class BookService(
    private val sqlClient: KSqlClient
) {

    /**
     * 示例 1：抓取完整对象（等价于抓取所有标量属性）
     */
    fun findSimpleBooks(): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                where(table.name ilike "GraphQL")
                select(table)
            }
            .execute()

    /**
     * 示例 2：抓取附带关联对象的结果
     * select(table) 后，关联对象由 object-fetcher 默认行为或过滤器决定；
     * 若要精确控制形状，请用 table.fetch(fetcher)。
     */
    fun findBooksWithStore(): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                select(table)
            }
            .execute()

    /**
     * 示例 3：深度抓取
     * 精确形状的写法见 resources/query/fetcher.md 与 Java 示例。
     */
    fun findBooksWithAll(): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                select(table)
            }
            .execute()

    /**
     * 示例 4：递归抓取（自关联 / 树形结构）
     *
     * Kotlin 生成的代码里，递归方法形如 `childNodes*()`：
     *
     *     select(table.fetchBy {
     *         allScalarFields()
     *         `childNodes*`()
     *     })
     *
     * 该方法由 KSP 生成，故此处不写在可直接编译的示例中。
     */
    fun findCategoryTree(): List<Category> =
        sqlClient
            .createQuery(Category::class) {
                where(table.parent.isNull())
                select(table)
            }
            .execute()

    /**
     * 示例 5：限定递归深度
     *
     * Java：`Fetchers.CATEGORY_FETCHER.recursiveChildNodes { it.depth(2) }`
     * Kotlin：`\`childNodes*\` { depth(2) }`
     */
    fun findCategoryWithMaxDepth(): List<Category> =
        sqlClient
            .createQuery(Category::class) {
                where(table.parent.isNull())
                select(table)
            }
            .execute()

    /**
     * 示例 6：直接使用 View DTO 查询
     * 这一写法在源码中可确证：KNonNullTable.fetch(staticType: KClass<S>)
     */
    fun findBookStoreDtos(): List<BookStoreDto> =
        sqlClient
            .createQuery(BookStore::class) {
                select(table.fetch(BookStoreDto::class))
            }
            .execute()
}
