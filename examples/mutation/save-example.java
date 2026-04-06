/**
 * Jimmer Save Command 示例 - Java
 * 
 * 只需一个方法调用，保存任意形状的数据结构
 * Jimmer 自动对比差异，执行相应的 insert/update/delete
 */
package com.example.service;

import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.SaveCommand;
import org.babyfish.jimmer.sql.SaveResult;
import com.example.model.*;
import com.example.dto.BookInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 保存操作示例
 */
@Service
public class BookSaveService {

    private final JSqlClient jSqlClient;

    public BookSaveService(JSqlClient jSqlClient) {
        this.jSqlClient = jSqlClient;
    }

    /**
     * 示例 1：保存单个简单对象
     * 只设置需要保存的属性，未设置的属性不会被修改
     */
    @Transactional
    public long saveSimpleBook() {
        // 使用 Draft API 构建动态对象
        Book book = Books.createBook(draft -> {
            draft.setName("Learning GraphQL");
            draft.setEdition(1);
            draft.setPrice(new BigDecimal("49.99"));
        });

        // 一个方法保存完成
        SaveResult<Book> result = jSqlClient.save(book);
        return result.getModifiedEntity().id();
    }

    /**
     * 示例 2：保存带一级关联的对象
     * 同时保存书籍和书店
     */
    @Transactional
    public long saveBookWithStore() {
        Book book = Books.createBook(draft -> {
            draft.setName("Learning GraphQL");
            draft.setEdition(1);
            draft.setPrice(new BigDecimal("49.99"));
            
            // 直接嵌套设置关联对象
            draft.applyStore(store -> {
                store.setName("O'Reilly");
                store.setWebsite("https://www.oreilly.com/");
            });
        });

        return jSqlClient.save(book).getModifiedEntity().id();
    }

    /**
     * 示例 3：保存完整嵌套结构
     * 书籍 -> 书店 + 多位作者
     * 一次保存整个树形结构
     */
    @Transactional
    public long saveFullNested() {
        BookStore store = Immutables.createBookStore(draft -> {
            draft.setName("O'Reilly");
            draft.setWebsite("https://www.oreilly.com/");
            
            draft.addIntoBooks(book -> {
                book.setName("Learning GraphQL");
                book.setEdition(1);
                book.setPrice(new BigDecimal("49.99"));
                
                book.addIntoAuthors(author -> {
                    author.setFirstName("Eve");
                    author.setLastName("Procello");
                    author.setGender(Gender.FEMALE);
                });
                
                book.addIntoAuthors(author -> {
                    author.setFirstName("Alex");
                    author.setLastName("Banks");
                    author.setGender(Gender.MALE);
                });
            });
        });

        // 一次性保存整个嵌套结构
        SaveResult<BookStore> result = jSqlClient.save(store);
        return result.getModifiedEntity().id();
    }

    /**
     * 示例 4：更新已有对象
     * 只修改传入的属性，保持其他属性不变
     */
    @Transactional
    public void updateBookPrice(long bookId, BigDecimal newPrice) {
        Book book = Books.createBook(draft -> {
            draft.setId(bookId);  // 指定 ID 表示更新
            draft.setPrice(newPrice);  // 只修改价格
            // 其他属性不设置，保持原有值不变
        });
        
        jSqlClient.save(book);
    }

    /**
     * 示例 5：通过 DTO 保存
     * 从 DTO 转换为实体对象保存
     */
    @Transactional
    public long saveFromDto(BookInput input) {
        // 将 Input DTO 转换为实体对象
        Book book = input.toEntity();
        SaveResult<Book> result = jSqlClient.save(book);
        return result.getModifiedEntity().id();
    }

    /**
     * 示例 6：修改关联关系
     * 只改变书籍和书店的关联，不修改书店本身
     * 
     * 这就是"短关联"保存
     */
    @Transactional
    public void changeBookStore(long bookId, Long newStoreId) {
        Book book = Books.createBook(draft -> {
            draft.setId(bookId);
            // 只设置关联 ID，Jimmer 只会修改关联关系
            // 不会修改关联对象本身
            draft.setStoreId(newStoreId);
        });
        
        jSqlClient.save(book);
    }

    /**
     * 示例 7：递归保存树形结构
     * 保存分类树，支持任意深度
     */
    @Transactional
    public long saveCategoryTree() {
        Category root = Categories.createCategory(draft -> {
            draft.setName("Technology");
            draft.addIntoChildren(programming -> {
                programming.setName("Programming");
                programming.addIntoChildren(java -> {
                    java.setName("Java");
                });
                programming.addIntoChildren(kotlin -> {
                    kotlin.setName("Kotlin");
                });
            });
        });
        
        SaveResult<Category> result = jSqlClient.save(root);
        return result.getModifiedEntity().id();
    }

    /**
     * 示例 8：设置保存选项
     * 自定义保存行为
     */
    @Transactional
    public void saveWithOptions(Book book) {
        jSqlClient
                .saveCommand(book)
                // 设置关联保存模式：追加而不是替换
                .setAssociatedSaveMode(SaveCommand.AssociatedSaveMode.APPEND)
                .execute();
    }

    /**
     * 示例 9：删除数据
     * 根据 ID 删除
     */
    @Transactional
    public void deleteBook(long bookId) {
        jSqlClient.delete(Book.class, bookId);
    }

    /**
     * 示例 10：批量删除
     */
    @Transactional
    public void deleteBooks(List<Long> bookIds) {
        jSqlClient.deleteAll(Book.class, bookIds);
    }
}
