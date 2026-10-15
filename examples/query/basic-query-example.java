/**
 * Jimmer SQL DSL 查询示例 - Java
 *
 * Jimmer 支持强类型动态 SQL 查询，天生适合复杂动态查询场景。
 *
 * 全部 API 已对照 jimmer v0.12.4 源码与官方文档校验。
 */
package com.example.service;

import com.example.dto.BookSpecification;
import com.example.model.*;
import org.babyfish.jimmer.Specification;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.Expression;
import org.babyfish.jimmer.sql.ast.Predicate;
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
     * 示例 1：抓取受限形状的对象图
     *
     * ⚠️ select 需要的是 Selection，不能直接传 Fetcher，
     * 必须用 table.fetch(fetcher) 包装。
     */
    public List<Book> fetchPartialShape() {
        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .where(table.price().gt(BigDecimal.valueOf(50)))
                .where(table.name().ilike("Spring"))
                .orderBy(table.price().desc())
                .select(
                        table.fetch(
                                BookFetcher.$
                                        .allScalarFields()
                                        .store(it -> it.name())
                        )
                )
                .execute();
    }

    /**
     * 示例 2：动态条件查询
     * 条件不满足时，对应的 where/if 分支不会进入 SQL。
     */
    public List<Book> dynamicQuery(
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long storeId
    ) {
        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .whereIf(StringUtils.hasText(name),
                        () -> table.name().ilike(name))
                .whereIf(minPrice != null,
                        () -> table.price().ge(minPrice))
                .whereIf(maxPrice != null,
                        () -> table.price().le(maxPrice))
                .whereIf(storeId != null,
                        () -> table.store().id().eq(storeId))
                .orderBy(table.name().asc())
                .select(table)
                .execute();
    }

    /**
     * 示例 3：动态 JOIN（链式关联路径 + 隐式子查询）
     *
     * Jimmer 没有 joinIf；关联属性本身就是 JOIN：
     * 集合属性调用会生成 exists 子查询，条件为空时不会加入 SQL，因此自动省去 JOIN。
     */
    public List<Book> dynamicJoinQuery(String authorName) {
        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .whereIf(
                        StringUtils.hasText(authorName),
                        table.authors(author ->
                                Predicate.or(
                                        author.firstName().ilike(authorName),
                                        author.lastName().ilike(authorName)
                                )
                        )
                )
                .select(table)
                .execute();
    }

    /**
     * 示例 4：隐式子查询
     * 关联路径的价格平均值可直接作为表达式参与比较。
     */
    public List<Book> implicitSubQuery() {
        BookTable book = BookTable.$;
        return jSqlClient
                .createQuery(book)
                .where(book.price().gt(
                        book.store().books().price().avg()
                ))
                .select(book)
                .execute();
    }

    /**
     * 示例 5：分页查询
     * 配合 PageFactory / repository 时 count 查询会自动生成。
     */
    public List<Book> pagedQuery(int page, int pageSize) {
        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .orderBy(table.id().asc())
                .limit(pageSize, (page - 1) * pageSize)
                .select(table)
                .execute();
    }

     * 示例 6：嵌入原生 SQL 片段
     *
     * - 做 where 条件：`Predicate.sql("... %e ...", args)`
     * - 做 select 表达式：`Expression.numeric().sql(Type.class, "... %e ...", args)`
     * 占位符统一为 %e（不是 {}，也不是 ?）。
     */
    public List<Book> nativeSqlExample() {
        BookTable book = BookTable.$;
        return jSqlClient
                .createQuery(book)
                .where(
                        Predicate.and(
                                book.price().gt(new BigDecimal("100")),
                                Predicate.sql(
                                        "JSON_CONTAINS(%e, %e)",
                                        book.tags(),
                                        Expression.value("tech")
                                )
                        )
                )
                .select(book)
                .execute();
    }

    /**
     * 示例 7：超级 QBE（Query By Example）
     *
     * specification 类型的 DTO 由 DTO 语言生成，实现 org.babyfish.jimmer.Specification，
     * 直接传给 where(...) 即可。
     */
    public List<Book> superQbe(BookSpecification spec) {
        Specification<Book> specification = spec;
        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .where(specification)
                .select(table)
                .execute();
    }

    /**
     * 示例 8：全局过滤器
     * 逻辑删除等内置过滤器默认自动生效。
     * 需要临时关闭时，用 sqlClient.filters(...) 派生一个临时 SqlClient。
     */
    public List<Book> withGlobalFilter() {
        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .select(table)
                .execute();

        // 临时禁用某个过滤器：
        // JSqlClient tmp = jSqlClient.filters(cfg -> cfg.disable(myFilter));
        // return tmp.createQuery(table).select(table).execute();
    }
}
