/**
 * Jimmer SQL DSL 查询示例 - Kotlin
 *
 * Jimmer 支持强类型动态 SQL 查询，天生适合复杂动态查询场景。
 *
 * 写法取自官方 Kotlin DSL：`createQuery(Book::class) { ... }`，
 * 块内使用 where(...) / orderBy(...) / select(table)。
 * 抓取任意形状的对象图请用 table.fetch(fetcher)，见 examples/fetch/basic-fetch-example.kt。
 */
package com.example.service

import com.example.dto.BookSpecification
import com.example.model.Book
import com.example.model.BookTable
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * 动态查询示例
 */
@Service
class BookQueryService(
    private val sqlClient: KSqlClient
) {

    /**
     * 示例 1：简单条件查询
     */
    fun simpleQuery(): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                where(table.price gt BigDecimal.valueOf(50))
                where(table.name ilike "Spring")
                orderBy(table.price.desc())
                select(table)
            }
            .execute()

    /**
     * 示例 2：动态条件查询
     * 参数为 null 时对应条件不会进入 SQL。
     */
    fun dynamicQuery(
        name: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        storeId: Long?
    ): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                name?.takeIf { it.isNotBlank() }?.let {
                    where(table.name ilike it)
                }
                minPrice?.let {
                    where(table.price ge it)
                }
                maxPrice?.let {
                    where(table.price le it)
                }
                storeId?.let {
                    where(table.store.id eq it)
                }
                orderBy(table.name.asc())
                select(table)
            }
            .execute()

    /**
     * 示例 3：动态 JOIN（隐式子查询）
     *
     * Jimmer 没有 joinIf：集合关联属性本身即可作为 exists 条件，
     * 条件为空时不产生 JOIN。
     */
    fun dynamicJoinQuery(authorName: String?): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                authorName?.takeIf { it.isNotBlank() }?.let {
                    where(
                        table.authors {
                            or(
                                firstName ilike it,
                                lastName ilike it
                            )
                        }
                    )
                }
                select(table)
            }
            .execute()

    /**
     * 示例 4：隐式子查询
     * 关联路径上的聚合值可直接参与比较。
     */
    fun implicitSubQuery(): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                where(table.price gt table.store.books.price.avg())
                select(table)
            }
            .execute()

    /**
     * 示例 5：分页查询
     */
    fun pagedQuery(page: Int, pageSize: Int): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                orderBy(table.id.asc())
                select(table)
            }
            .limit(pageSize, (page - 1) * pageSize.toLong())
            .execute()

    /**
     * 示例 6：超级 QBE（Query By Example）
     * specification 类型的 DTO 由 DTO 语言生成。
     */
    fun superQbe(specification: BookSpecification): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                where(specification)
                select(table)
            }
            .execute()

    /**
     * 示例 7：全局过滤器
     * 逻辑删除等内置过滤器默认自动生效；
     * 需要临时关闭时用 sqlClient.filters { ... } 派生临时 SqlClient。
     */
    fun withGlobalFilter(): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                select(table)
            }
            .execute()

    fun withFilterDisabled(filterType: Class<*>): List<Book> {
        val tmp = sqlClient.filters {
            disableByTypes(filterType.kotlin)
        }
        return tmp.createQuery(Book::class) {
            select(table)
        }.execute()
    }
}
