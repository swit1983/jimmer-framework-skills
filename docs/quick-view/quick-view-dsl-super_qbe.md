# 超级QBE

> 来源: https://jimmer.deno.dev/zh/docs/quick-view/dsl/super_qbe

- [快速预览 ★](/zh/docs/quick-view/)
- [3. 任意动态查询](/zh/docs/quick-view/dsl/)
- 超级QBE

本页总览

# 超级QBE

## 能否更简单[​](#能否更简单 "能否更简单的直接链接")

通过[上一篇文章](/zh/docs/quick-view/dsl/feature)，我们知道Jimmer SQL DSL天生就是为任意复杂的动态查询而设计，和其他框架的SQL DSL仅仅为了带来强类型体验完全不同。

但是有两个问题

1. 伴随着[上一篇文章](/zh/docs/quick-view/dsl/feature)的讲解，查询方法的参数越来越多。这对Java语言不友好，我们迫切需要将所有查询参数封装成一个对象。
2. 我真的很懒，我想实现[上一篇文章](/zh/docs/quick-view/dsl/feature)中的所有功能，但我不想写那些代码，我只想写一行代码。

Jimmer自带的[DTO语言](/zh/docs/object/view/dto-language)可以快速解决以上两个问题。

## 定义Specification DTO[​](#定义specification-dto "定义Specification DTO的直接链接")

由于在[查询任意形状/暴露功能/返回输出DTO](/zh/docs/quick-view/fetch/export/dto)一文中，为讲解Output DTO，我们已经对DTO语言有了一定了解，本文不做重复性阐述。

1. 安装DTO语言Intellij插件：<https://github.com/ClearPlume/jimmer-dto> *（此过程不是必须的，但非常推荐）*
2. 新建目录`src/main/dto`
3. 在`src/main/dto`下建立一个文件`Book.dto`，编写代码如下

   Book.dto

   ```export com.yourcompany.yourproject.model.book
       - > package com.yourcompany.yourproject.model.dto

   specification BookSpecification {
       like/i(name)
       ge(price) // Default alias: minPrice
       le(price) // Default alias: maxPrice
       flat(store) {
           as(^ -> store) {
               like/i(name)
               like/i(website)
           }
       }
       flat(authors) {
           like/i(firstName, lastName) as authorName
           gender as authorGender
       }
   }
   ...省略其他DTO类型定义...

```

   信息

   和之前讨论过的Output/Input DTO不同，这里查询规格DTO采用`specification`修饰符。

   这个Specification DTO内部使用了大量的QBE函数，其意义不难理解，本文属于快速预览部分，不做过多解释。

## 生成的代码[​](#生成的代码 "生成的代码的直接链接")

编译项目，Jimmer将会自动生成如下代码

- Java
- Kotlin

BookSpecification.java

```@generatedby( ❶
        file = "<yourproject>/src/main/dto/Book.dto"
)
public class BookSpecification
implements JSpecification<Book, BookTable> {  ❷

    @Nullable
    private String name;

    @Nullable
    private BigDecimal minPrice;

    @Nullable
    private BigDecimal maxPrice;

    @Nullable
    private String storeName;

    @Nullable
    private String storeWebsite;

    @Nullable
    private String authorName;

    @Nullable
    private Gender authorGender;

    @Override
    public void applyTo(SpecificationArgs<Book, BookTable> args) { ❸
        ...Omit complex dynamic query logic...
    }

    ...Omit getters, setters, hashCode, equals, toString...
}

```

BookSpecification.kt

```@generatedby( ❶
        file = "<yourproject>/src/main/dto/Book.dto"
)
data class BookSpecification(
    val name: String? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val storeName: String? = null,
    val storeWebsite: String? = null,
    val authorName: String? = null,
    val authorGender: Gender? = null
) : KSpecification<Book> { ❷

    override applyTo(args: KSpecificationArgs<Book>) { ❸
        ...Omit complex dynamic query logic...
    }
}

```

- ❶ 提醒开发人员这个类是在编译时被Jimmer自动生成的
- ❷ Specification DTO实现的接口
- ❸ 这个类知道如何生成SQL条件

## 使用[​](#使用 "使用的直接链接")

- Java
- Kotlin

BookRepository.java

```@repository
public class BookRepository {

    private final JSqlClient sqlClient;

    public BookRepository(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    List<Book> findBooks(
        BookSpecification specification,
        @Nullable Fetcher<Book> fetcher
    ) {
        BookTable table = Tables.BOOK_TABLE;

        return sqlClient
            .createQuery(table)
            .where(specification)
            .select(table.fetch(fetcher))
            .execute();
    }
}

```

BookRepository.kt

```@repository
class BookRepository(
    private val sqlClient: KSqlClient
) {

    fun findBooks(
        specification: BookSpecification,
        fetcher: Fetcher<Book>? = null
    ): List<Book> =
        sqlClient
            .createQuery(Book::class) {
                where(specification)
                select(table.fetch(table))
            }
            .execute()
}

```

我们看到，支持，我们只需一行代码即可实现复杂的动态查询。

其功能和[上一篇文章](/zh/docs/quick-view/dsl/feature)中最后一个例子完全一样，无需重复。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/dsl/super_qbe.mdx)

最后于 **2025年9月16日** 更新