/**
 * Jimmer Fetcher 基础示例 - Java
 * 
 * Fetcher 用于控制查询返回的数据结构形状
 */
package com.example.service;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.fetcher.DtoFetcher;
import com.example.model.*;
import com.example.dto.*;
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
     * 示例 1：只查询书籍的基本属性，不查询关联
     * 返回残缺对象，只包含指定属性
     */
    public List<Book> findSimpleBooks() {
        // 定义 Fetcher: 只获取 id, name, edition, price
        Fetcher<Book> simpleBookFetcher = BookFetcher.$
                .id()
                .name()
                .edition()
                .price();

        // 执行查询
        return jSqlClient
                .createQuery(BookTable.$)
                .where(BookTable.$.name().ilike("GraphQL"))
                .select(simpleBookFetcher)
                .execute();
    }

    /**
     * 示例 2：查询书籍及其书店信息
     * 一级关联嵌套
     */
    public List<Book> findBooksWithStore() {
        Fetcher<Book> bookWithStoreFetcher = BookFetcher.$
                .id()
                .name()
                .edition()
                .price()
                .store(store -> store
                        .id()
                        .name()
                        .website()
                );

        return jSqlClient
                .createQuery(BookTable.$)
                .select(bookWithStoreFetcher)
                .execute();
    }

    /**
     * 示例 3：深度嵌套查询
     * 书籍 -> 书店 + 作者，完整树形结构
     */
    public List<Book> findBooksWithAll() {
        Fetcher<Book> bookFullFetcher = BookFetcher.$
                .allScalarFields() // 包含所有标量属性
                .store(BookStoreFetcher.$
                        .allScalarFields()
                )
                .authors(AuthorFetcher.$
                        .allScalarFields()
                );

        return jSqlClient
                .createQuery(BookTable.$)
                .select(bookFullFetcher)
                .execute();
    }

    /**
     * 示例 4：递归查询自关联
     * 适用于分类、评论等树形结构
     */
    public List<Category> findCategoryTree() {
        // 递归定义：查询子类直到最深层次
        Fetcher<Category> recursiveFetcher = CategoryFetcher.$
                .id()
                .name()
                .children(child -> child
                        .id()
                        .name()
                        .children(/* 递归引用 */ this)
                );

        return jSqlClient
                .createQuery(CategoryTable.$)
                .where(CategoryTable.$.parent().isNull()) // 查询根节点
                .select(recursiveFetcher)
                .execute();
    }

    /**
     * 示例 5：限制递归深度
     * 避免无限递归
     */
    public List<Category> findCategoryWithMaxDepth() {
        // 最多递归 3 层
        Fetcher<Category> limitedRecursion = CategoryFetcher.$
                .id()
                .name()
                .children(3, child -> child
                        .id()
                        .name()
                );

        return jSqlClient
                .createQuery(CategoryTable.$)
                .where(CategoryTable.$.parent().isNull())
                .select(limitedRecursion)
                .execute();
    }

    /**
     * 示例 6：直接使用 DTO 进行查询
     * 基于 DTO 定义自动生成 Fetcher
     */
    public List<BookStoreDto> findBookStoreDtos() {
        // DTO 定义已经说明了需要哪些字段
        // 直接使用 DtoFetcher 转换
        DtoFetcher<BookStore, BookStoreDto> fetcher = 
                BookStoreDto.dtoFetcher();
        
        return jSqlClient
                .createQuery(BookStoreTable.$)
                .select(fetcher)
                .execute();
    }
}
