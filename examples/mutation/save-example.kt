/**
 * Jimmer Save Command 示例 - Kotlin
 * 
 * 只需一个方法调用，保存任意形状的数据结构
 * Jimmer 自动对比差异，执行相应的 insert/update/delete
 */
package com.example.service

import org.babyfish.jimmer.sql.JSqlClient
import org.babyfish.jimmer.sql.SaveCommand
import org.babyfish.jimmer.sql.SaveResult
import com.example.model.*
import com.example.dto.BookInput
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * 保存操作示例
 */
@Service
class BookSaveService(
    private val jSqlClient: JSqlClient
) {

    /**
     * 示例 1：保存单个简单对象
     * 只设置需要保存的属性，未设置的属性不会被修改
     */
    @Transactional
    fun saveSimpleBook(): Long {
        // 使用 Draft API 构建动态对象
        val book = newBook {
            name = "Learning GraphQL"
            edition = 1
            price = BigDecimal("49.99")
        }

        // 一个方法保存完成
        val result = jSqlClient.save(book)
        return result.modifiedEntity.id
    }

    /**
     * 示例 2：保存带一级关联的对象
     * 同时保存书籍和书店
     */
    @Transactional
    fun saveBookWithStore(): Long {
        val book = newBook {
            name = "Learning GraphQL"
            edition = 1
            price = BigDecimal("49.99")
            
            // 直接嵌套设置关联对象
            store {
                name = "O'Reilly"
                website = "https://www.oreilly.com/"
            }
        }

        return jSqlClient.save(book).modifiedEntity.id
    }

    /**
     * 示例 3：保存完整嵌套结构
     * 书籍 -> 书店 + 多位作者
     * 一次保存整个树形结构
     */
    @Transactional
    fun saveFullNested(): Long {
        val store = newBookStore {
            name = "O'Reilly"
            website = "https://www.oreilly.com/"
            
            books {
                add {
                    name = "Learning GraphQL"
                    edition = 1
                    price = BigDecimal("49.99")
                    
                    authors {
                        add {
                            firstName = "Eve"
                            lastName = "Procello"
                            gender = Gender.FEMALE
                        }
                        add {
                            firstName = "Alex"
                            lastName = "Banks"
                            gender = Gender.MALE
                        }
                    }
                }
            }
        }

        // 一次性保存整个嵌套结构
        val result = jSqlClient.save(store)
        return result.modifiedEntity.id
    }

    /**
     * 示例 4：更新已有对象
     * 只修改传入的属性，保持其他属性不变
     */
    @Transactional
    fun updateBookPrice(bookId: Long, newPrice: BigDecimal) {
        val book = newBook {
            id = bookId  // 指定 ID 表示更新
            price = newPrice  // 只修改价格
            // 其他属性不设置，保持原有值不变
        }
        
        jSqlClient.save(book)
    }

    /**
     * 示例 5：通过 DTO 保存
     * 从 DTO 转换为实体对象保存
     */
    @Transactional
    fun saveFromDto(input: BookInput): Long {
        // 将 Input DTO 转换为实体对象
        val book = input.toEntity()
        val result = jSqlClient.save(book)
        return result.modifiedEntity.id
    }

    /**
     * 示例 6：修改关联关系
     * 只改变书籍和书店的关联，不修改书店本身
     * 
     * 这就是"短关联"保存
     */
    @Transactional
    fun changeBookStore(bookId: Long, newStoreId: Long) {
        val book = newBook {
            id = bookId
            // 只设置关联 ID，Jimmer 只会修改关联关系
            // 不会修改关联对象本身
            storeId = newStoreId
        }
        
        jSqlClient.save(book)
    }

    /**
     * 示例 7：递归保存树形结构
     * 保存分类树，支持任意深度
     */
    @Transactional
    fun saveCategoryTree(): Long {
        val root = newCategory {
            name = "Technology"
            children {
                add {
                    name = "Programming"
                    children {
                        add {
                            name = "Java"
                        }
                        add {
                            name = "Kotlin"
                        }
                    }
                }
            }
        }
        
        val result = jSqlClient.save(root)
        return result.modifiedEntity.id
    }

    /**
     * 示例 8：设置保存选项
     * 自定义保存行为
     */
    @Transactional
    fun saveWithOptions(book: Book) {
        jSqlClient
            .saveCommand(book)
            // 设置关联保存模式：追加而不是替换
            .setAssociatedSaveMode(SaveCommand.AssociatedSaveMode.APPEND)
            .execute()
    }

    /**
     * 示例 9：删除数据
     * 根据 ID 删除
     */
    @Transactional
    fun deleteBook(bookId: Long) {
        jSqlClient.delete(Book::class, bookId)
    }

    /**
     * 示例 10：批量删除
     */
    @Transactional
    fun deleteBooks(bookIds: List<Long>) {
        jSqlClient.deleteAll(Book::class, bookIds)
    }
}
