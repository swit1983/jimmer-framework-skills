# 保存长关联

> 来源: https://jimmer.deno.dev/zh/docs/quick-view/save/export/long

- [快速预览 ★](/zh/docs/quick-view/)
- [2. 保存任意形状](/zh/docs/quick-view/save/)
- [暴露功能](/zh/docs/quick-view/save/export/)
- 保存长关联

本页总览

# 保存长关联

## 何谓长关联[​](#何谓长关联 "何谓长关联的直接链接")

所谓长关联，指不仅要改变当前对象和其他对象之间的关联关系，还要进一步修改关联对象。

对于UI界面而言，通常表现为父子表单的嵌套 *(甚至树形递归)*。形式多样，以表单内嵌子表颇为常见，如下：

购买人

皮皮鲁

购买人

省份

省份

城市

城市

地址

地址

订单明细

| 商品           | 数量  | 单价  | 明细价 | 删除  |
|--------------|-----|-----|-----|-----|
| zippo夜光流沙打火机 |     | 268 | 536 |     |
| 憨憨宠猫爬架       |     | 238 | 238 |     |
|              |     |     |     |     |
| ---          | --- | --- | --- | --- |
| 添加           |     |     |     |     |

总额:774

提交

信息

只要开发人员愿意，这种父子表单嵌套结构的深度可以不只两层，从理论上讲，深度可以无限，这就是称其为长关联的原因。

## 编写DTO[​](#编写dto "编写DTO的直接链接")

在当前例子的在[实体定义](/zh/docs/quick-view/precondition#book)中，比较适合作为长关联的例子是保存`BookStore`以及它的`books`关联集合。

1. 安装DTO语言Intellij插件：<https://github.com/ClearPlume/jimmer-dto> *（此过程不是必须的，但非常推荐）*
2. 新建目录`src/main/dto`
3. 在`src/main/dto`下建立一个文件`BookStore.dto`，编写代码如下

   BookStore.dto

   ```export org.doc.j.model.bookstore
       - > package org.doc.j.model.dto

   input BookStoreWithLongAssociation {
       #allScalars(this)
       books { // LongAssociation
           #allScalars(this)
           - id
           id(authors) as authorIds
       }
   }

```

## 生成的代码[​](#生成的代码 "生成的代码的直接链接")

编译，Jimmer会生成如下代码

- Java
- Kotlin

BookStoreWithLongAssociation.java

```@generatedby(
        file = "<yourproject>/src/main/dto/BookStore.dto"
)
public class BookStoreWithLongAssociation implements Input<BookStore> {

    @Nullable
    private Long id;

    @NotNull
    private String name;

    @Nullable
    private String website;

    @NotNull
    private List<TargetOf_books> books;

    ...省略其他方法...

    public static class TargetOf_books implements Input<Book> {

        @NotNull
        private String name;

        private int edition;

        @NotNull
        private BigDecimal price;

        @NotNull
        private List<Long> authorIds;

        ...省略其他方法...
    }
}

```

BookStoreWithLongAssociation.kt

```@generatedby(
        file = "<yourproject>/src/main/dto/BookStore.dto"
)
data class BookStoreWithLongAssociation(
    val id: Long?
    val name: String,
    val website: String?,
    val books: List<TargetOf_books>
) : Input<BookStore> {

    ...省略其他方法...

    data class TargetOf_authors(
        val name: String,
        val edition: Int,
        val price: BigDecimal,
        val authorIds: List<Long>
    )
}

```

## 编写HTTP服务[​](#编写http服务 "编写HTTP服务的直接链接")

- Java
- Kotlin

BookStoreController.java

```@restcontroller
public class BookStoreController {

    private final JSqlClient sqlClient;

    public BookController(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    @PutMapping("/bookStore")
    pubic int saveBookStoreWithLongAssociation(
        @RequestBody BookStoreWithLongAssociation input
    ) {
        return sqlClient
            .save(input)
            .getTotalAffectedRowCount();
    }
}

```

BookStoreController.java

```class bookstorecontroller(
    private val sqlClient: KSqlClient
) {

    @PutMapping("/bookStore")
    fun saveBookStoreWithLongAssociation(
        @RequestBody input: BookStoreWithLongAssociation
    ): Int =
        sqlClient
            .save(input)
            .totalAffectedRowCount
}

```

可见，无论Input DTO如何改变，Jimmer仍然只需一个方法调用即可完成数据保存。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/save/export/long.mdx)

最后于 **2025年9月16日** 更新