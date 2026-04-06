/**
 * Jimmer SQL DSL 查询示例 - Java
 * 
 * Jimmer 支持强类型动态 SQL 查询，天生适合复杂动态查询场景
 */
package com.example.service;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import com.example.model.*;
import org.babyfish.jimmer.sql.ast.query.specification.JimmerSpecification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 动态查询示例
 */
@Service
public class BookQueryService {

    private final JSqlClient jSqlClient;

    public BookQueryService(JSqlClient jSqlClient) {
        this.jSqlClient = jSqlClient;
    }

    /**
     * 示例 1：简单条件查询
     */
    public List<Book> simpleQuery() {
        return jSqlClient
                .createQuery(BookTable.$)
                .where(BookTable.$.price().gt(BigDecimal.valueOf(50)))
                .where(BookTable.$.name().ilike("Spring"))
                .orderBy(BookTable.$.price().desc())
                .select(BookFetcher.$
                        .allScalarFields()
                        .store(it -> it.name())
                )
                .execute();
    }

    /**
     * 示例 2：动态条件查询
     * 根据参数决定是否添加某个条件
     */
    public List<Book> dynamicQuery(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long storeId
    ) {
        return jSqlClient
                .createQuery(BookTable.$)
                // 只有当参数不为空时才添加条件
                .whereIf(StringUtils.hasText(name), () -> 
                    BookTable.$.name().ilike(name)
                )
                .whereIf(minPrice != null, () -> 
                    BookTable.$.price().ge(minPrice)
                )
                .whereIf(maxPrice != null, () -> 
                    BookTable.$.price().le(maxPrice)
                )
                .whereIf(storeId != null, () -> 
                    BookTable.$.store().id().eq(storeId)
                )
                .orderBy(BookTable.$.name().asc())
                .select(Fetcher.all(Book.class))
                .execute();
    }

    /**
     * 示例 3：动态表连接
     * 根据查询条件动态添加表连接
     * 
     * 查询某个作者写的书，可以根据作者名字过滤
     * 如果不提供作者名字，则不需要连接 author 表
     */
    public List<Book> dynamicJoinQuery(String authorName) {
        return jSqlClient
                .createQuery(BookTable.$)
                // 只有当提供了作者名称才需要连接关联表
                .joinIf(StringUtils.hasText(authorName), 
                        BookTable.$.authors(), 
                        (book, author) -> 
                            author.firstName().ilike(authorName)
                )
                .select(BookFetcher.$.allScalarFields())
                .execute();
    }

    /**
     * 示例 4：隐式子查询
     * 查询价格大于平均价格的所有书籍
     */
    public List<Book> implicitSubQuery() {
        BookTable book = BookTable.$;
        return jSqlClient
                .createQuery(book)
                // 子查询直接基于关联属性，写法非常简洁
                .where(book.price().gt(
                    book.store().books().price().avg()
                ))
                .select(book)
                .execute();
    }

    /**
     * 示例 5：分页查询
     * Jimmer 自动生成 count 查询，无需手动编写
     */
    public List<Book> pagedQuery(int page, int pageSize) {
        return jSqlClient
                .createQuery(BookTable.$)
                .orderBy(BookTable.$.id().asc())
                .limit(pageSize, (page - 1) * pageSize)
                .select(BookFetcher.$.allScalarFields())
                .execute();
    }

    /**
     * 示例 6：使用原生 SQL 片段
     * 当需要使用数据库特有功能时，可以嵌入原生 SQL
     */
    public List<Book> nativeSqlExample() {
        BookTable book = BookTable.$;
        return jSqlClient
                .createQuery(book)
                // 使用原生 SQL 表达式作为条件
                .where(book.price().gt(
                    new BigDecimal("100")
                ).and(
                    org.babyfish.jimmer.sql.ast.query.Expression.nativeSql(
                        Boolean.class,
                        "JSON_CONTAINS({}, {})",
                        book.tags(),
                        "'tech'"
                    )
                ))
                .select(book)
                .execute();
    }

    /**
     * 示例 7：超级 QBE (Query By Example)
     * 使用 DTO 自动生成查询条件
     */
    public List<Book> superQbe(BookQueryParams params) {
        // 定义 Specification DTO，Jimmer 自动生成查询条件
        JimmerSpecification<Book> spec = 
            BookTable.$.createSpecification(params);
        
        return jSqlClient
                .createQuery(BookTable.$)
                .where(spec)
                .select(Book.class)
                .execute();
    }

    /**
     * 示例 8：全局过滤器
     * 全局过滤器会自动应用到所有查询，比如软删除过滤
     * 
     * 默认已经自动应用逻辑删除过滤器，不需要手动添加
     */
    public List<Book> withGlobalFilter() {
        // 逻辑删除的实体已经被自动过滤掉了
        return jSqlClient
                .createQuery(BookTable.$)
                .select(Book.class)
                .execute();
        
        // 如果需要禁用某个过滤器：
        // return jSqlClient
        //         .createQuery(BookTable.$)
        //         .disableFilter(LogicalDeletedFilter.class)
        //         .select(Book.class)
        //         .execute();
    }
}
