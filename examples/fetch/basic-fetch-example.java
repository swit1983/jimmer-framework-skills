/**
 * Jimmer Fetcher 基础示例 - Java
 *
 * Fetcher 用于描述查询返回的数据结构（对象图）形状。
 *
 * 关键约定（务必记住）：
 *  1. Fetcher 不能直接传给 select，必须包装成 Selection：
 *         .select(table.fetch(fetcher))
 *  2. View DTO 查询用：
 *         .select(table.fetch(SomeDto.class))
 *  3. 递归抓取用编译时生成的 recursiveXxx() 方法。
 *
 * API 已对照 jimmer v0.12.4 源码与官方文档校验。
 */
package com.example.service;

import com.example.dto.BookStoreDto;
import com.example.model.Book;
import com.example.model.BookTable;
import com.example.model.Category;
import com.example.model.CategoryTable;
import com.example.model.BookStoreTable;
import com.example.model.Fetchers;
import com.example.model.BookFetcher;
import com.example.model.BookStoreFetcher;
import com.example.model.AuthorFetcher;
import com.example.model.CategoryFetcher;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 不同形状的查询示例
 */
@Service
public class BookService {

    private final JSqlClient jSqlClient;

    public BookService(JSqlClient jSqlClient) {
        this.jSqlClient = jSqlClient;
    }

    /**
     * 示例 1：只查询书籍的基本属性，不查关联
     * 返回 "残缺对象"，只包含 Fetcher 指定的属性。
     */
    public List<Book> findSimpleBooks() {
        Fetcher<Book> simpleBookFetcher = Fetchers.BOOK_FETCHER
                .id()
                .name()
                .edition()
                .price();

        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .where(table.name().ilike("GraphQL"))
                .select(table.fetch(simpleBookFetcher))
                .execute();
    }

    /**
     * 示例 2：一级关联嵌套
     */
    public List<Book> findBooksWithStore() {
        Fetcher<Book> bookWithStoreFetcher = Fetchers.BOOK_FETCHER
                .id()
                .name()
                .edition()
                .price()
                .store(BookStoreFetcher.$
                        .id()
                        .name()
                        .website()
                );

        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .select(table.fetch(bookWithStoreFetcher))
                .execute();
    }

    /**
     * 示例 3：深度嵌套抓取
     * allScalarFields() 一次性抓取所有标量属性。
     */
    public List<Book> findBooksWithAll() {
        Fetcher<Book> bookFullFetcher = Fetchers.BOOK_FETCHER
                .allScalarFields()
                .store(BookStoreFetcher.$.allScalarFields())
                .authors(AuthorFetcher.$.allScalarFields());

        BookTable table = BookTable.$;
        return jSqlClient
                .createQuery(table)
                .select(table.fetch(bookFullFetcher))
                .execute();
    }

    /**
     * 示例 4：无限递归抓取（自关联）
     *
     * 编译时 Jimmer 会为自关联属性生成 recursiveXxx() 方法，
     * 递归抓取无需再描述子对象形状，子对象形状即自身。
     */
    public List<Category> findCategoryTree() {
        Fetcher<Category> recursiveFetcher = Fetchers.CATEGORY_FETCHER
                .id()
                .name()
                .recursiveChildNodes();

        CategoryTable table = CategoryTable.$;
        return jSqlClient
                .createQuery(table)
                .where(table.parent().isNull())   // 只查根节点
                .select(table.fetch(recursiveFetcher))
                .execute();
    }

    /**
     * 示例 5：限制递归深度（抓取两层）
     */
    public List<Category> findCategoryWithMaxDepth() {
        Fetcher<Category> limitedRecursion = Fetchers.CATEGORY_FETCHER
                .id()
                .name()
                .recursiveChildNodes(it -> it.depth(2));

        CategoryTable table = CategoryTable.$;
        return jSqlClient
                .createQuery(table)
                .where(table.parent().isNull())
                .select(table.fetch(limitedRecursion))
                .execute();
    }

    /**
     * 示例 6：直接使用 View DTO 查询
     * DTO 定义本身描述了要抓取哪些字段，
     * 用 table.fetch(DTO.class) 即可，无需自己拼 Fetcher。
     */
    public List<BookStoreDto> findBookStoreDtos() {
        BookStoreTable table = BookStoreTable.$;
        return jSqlClient
                .createQuery(table)
                .select(table.fetch(BookStoreDto.class))
                .execute();
    }
}
