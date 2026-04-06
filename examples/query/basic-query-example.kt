/**
 * Jimmer SQL DSL 查询示例 - Kotlin
 * 
 * Jimmer 支持强类型动态 SQL 查询，天生适合复杂动态查询场景
 */
package com.example.service

import org.babyfish.jimmer.sql.JSqlClient
import org.babyfish.jimmer.sql.ast.query.specification.createSpecification
import com.example.model.*
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import java.math.BigDecimal

/**
 * 动态查询示例
 */
@Service
class BookQueryService(
    private val jSqlClient: JSqlClient
) {

    /**
     * 示例 1：简单条件查询
     */
    fun simpleQuery(): List<Book> {
        return jSqlClient
            .createQuery(Book) {
                where += table.price gt BigDecimal.valueOf(50)
                where += table.name ilike "Spring"
                orderBy += table.price.desc()
            }
            .select(BookFetcher {
                it.allScalarFields()
                it.store { store ->
                    store.name()
                }
            })
            .execute()
    }

    /**
     * 示例 2：动态条件查询
     * 根据参数决定是否添加某个条件
     */
    fun dynamicQuery(
        name: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        storeId: Long?
    ): List<Book> {
        return jSqlClient
            .createQuery(Book) {
                // 只有当参数不为空时才添加条件
                name.takeIf { StringUtils.hasText(it) }?.let {
                    where += table.name ilike it
                }
                minPrice?.let {
                    where += table.price ge it
                }
                maxPrice?.let {
                    where += table.price le it
                }
                storeId?.let {
                    where += table.store.id eq it
                }
                orderBy += table.name.asc()
            }
            .select(Fetcher.all(Book::class))
            .execute()
    }

    /**
     * 示例 3：动态表连接
     * 根据查询条件动态添加表连接
     * 
     * 查询某个作者写的书，可以根据作者名字过滤
     * 如果不提供作者名字，则不需要连接 author 表
     */
    fun dynamicJoinQuery(authorName: String?): List<Book> {
        return jSqlClient
            .createQuery(Book) {
                // 只有当提供了作者名称才需要连接关联表
                authorName?.takeIf { StringUtils.hasText(it) }?.let {
                    join(table.authors) { author ->
                        where += author.firstName ilike it
                    }
                }
            }
            .select(BookFetcher.$.allScalarFields())
            .execute()
    }

    /**
     * 示例 4：隐式子查询
     * 查询价格大于所在书店平均价格的所有书籍
     */
    fun implicitSubQuery(): List<Book> {
        return jSqlClient
            .createQuery(Book) {
                // 子查询直接基于关联属性，写法非常简洁
                where += table.price gt table.store.books.price.avg()
            }
            .select(table)
            .execute()
    }

    /**
     * 示例 5：分页查询
     * Jimmer 自动生成 count 查询，无需手动编写
     */
    fun pagedQuery(page: Int, pageSize: Int): List<Book> {
        return jSqlClient
            .createQuery(Book) {
                orderBy += table.id.asc()
            }
            .limit(pageSize, (page - 1) * pageSize)
            .select(BookFetcher { it.allScalarFields() })
            .execute()
    }

    /**
     * 示例 6：使用原生 SQL 片段
     * 当需要使用数据库特有功能时，可以嵌入原生 SQL
     */
    fun nativeSqlExample(): List<Book> {
        return jSqlClient
            .createQuery(Book) {
                where += table.price gt BigDecimal("100")
                // 使用原生 SQL 表达式作为条件
                where += org.babyfish.jimmer.sql.ast.query.Expression.nativeSql(
                    Boolean::class,
                    "JSON_CONTAINS({}, '\"tech\"')",
                    table.tags
                )
            }
            .select(table)
            .execute()
    }

    /**
     * 示例 7：超级 QBE (Query By Example)
     * 使用 DTO 自动生成查询条件
     */
    fun superQbe(params: BookQueryParams): List<Book> {
        // 定义 Specification DTO，Jimmer 自动生成查询条件
        val spec = BookTable.createSpecification(params)
        
        return jSqlClient
            .createQuery(Book) {
                where += spec
            }
            .select(table)
            .execute()
    }

    /**
     * 示例 8：全局过滤器
     * 全局过滤器会自动应用到所有查询，比如软删除过滤
     * 
     * 默认已经自动应用逻辑删除过滤器，不需要手动添加
     */
    fun withGlobalFilter(): List<Book> {
        // 逻辑删除的实体已经被自动过滤掉了
        return jSqlClient
            .createQuery(Book)
            .select(table)
            .execute()

        // 如果需要禁用某个过滤器：
        // return jSqlClient
        //         .createQuery(Book)
        //         .disableFilter(LogicalDeletedFilter::class.java)
        //         .select(table)
        //         .execute()
    }
}
