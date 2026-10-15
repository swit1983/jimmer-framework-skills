/**
 * Jimmer Save Command 示例 - Java
 *
 * 只需一个方法调用，保存任意形状的数据结构
 * Jimmer 自动对比差异，执行相应的 insert/update/delete
 *
 * API 已对照 jimmer v0.12.4 源码校验。
 */
package com.example.service;

import com.example.dto.BookInput;
import com.example.model.*;
import com.example.model.Immutables;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SimpleSaveResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
        Book book = Immutables.createBook(draft -> {
            draft.setName("Learning GraphQL");
            draft.setEdition(1);
            draft.setPrice(new BigDecimal("49.99"));
        });

        // save 返回 SimpleSaveResult，取回实体需调用 getModifiedEntity()
        SimpleSaveResult<Book> result = jSqlClient.save(book);
        return result.getModifiedEntity().id();
    }

    /**
     * 示例 2：保存带一级关联的对象
     * 同时保存书籍和书店
     */
    @Transactional
    public long saveBookWithStore() {
        Book book = Immutables.createBook(draft -> {
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

        SimpleSaveResult<BookStore> result = jSqlClient.save(store);
        return result.getModifiedEntity().id();
    }

    /**
     * 示例 4：更新已有对象
     * 只修改传入的属性，保持其他属性不变（"不完整对象"更新）
     */
    @Transactional
    public void updateBookPrice(long bookId, BigDecimal newPrice) {
        Book book = Immutables.createBook(draft -> {
            draft.setId(bookId);
            draft.setPrice(newPrice);
        });

        // 默认 SaveMode.UPSERT：带 id 时按 id 更新，且只更新已赋值的列
        jSqlClient.save(book);

        // 若确定只更新、不新增，可显式限制
        jSqlClient.saveCommand(book)
                .setMode(SaveMode.UPDATE_ONLY)
                .execute();
    }

    /**
     * 示例 5：通过 DTO 保存
     * 从 Input DTO 转换为实体对象保存
     */
    @Transactional
    public long saveFromDto(BookInput input) {
        SimpleSaveResult<Book> result = jSqlClient.save(input.toEntity());
        return result.getModifiedEntity().id();
    }

    /**
     * 示例 6：修改关联关系（短关联保存）
     * 只改变书籍和书店的关联，不修改书店本身
     */
    @Transactional
    public void changeBookStore(long bookId, Long newStoreId) {
        Book book = Immutables.createBook(draft -> {
            draft.setId(bookId);
            draft.setStoreId(newStoreId);
        });

        jSqlClient.save(book);
    }

    /**
     * 示例 7：递归保存树形结构
     */
    @Transactional
    public long saveCategoryTree() {
        Category root = Immutables.createCategory(draft -> {
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

        SimpleSaveResult<Category> result = jSqlClient.save(root);
        return result.getModifiedEntity().id();
    }

    /**
     * 示例 8：设置保存选项
     * 注意：保存指令对「关联」的默认模式是 REPLACE，
     * 只想追加而不想删除旧关联时，必须显式改为 APPEND / MERGE。
     */
    @Transactional
    public void saveWithOptions(Book book) {
        jSqlClient
                .saveCommand(book)
                .setAssociatedModeAll(AssociatedSaveMode.APPEND)
                .execute();
    }

    /**
     * 示例 9：根据 ID 删除
     */
    @Transactional
    public void deleteBook(long bookId) {
        jSqlClient.deleteById(Book.class, bookId);
    }

    /**
     * 示例 10：批量删除
     */
    @Transactional
    public void deleteBooks(List<Long> bookIds) {
        jSqlClient.deleteByIds(Book.class, bookIds);
    }
}
