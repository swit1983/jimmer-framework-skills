---
title: 保存短关联
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/quick-view/save/export/short
---

# 保存短关联

## 何谓短关联

所谓短关联，指只改变当前对象和其他对象之间的关联关系，不进一步修改关联对象。

对于UI界面而言

-   引用关联 *(一对一和多对一)* 表现为单选菜单

-   集合关联 *(一对多和多对多)* 表现为多选菜单

例如：
> 📊 此处官方页面包含交互式图示/演示：`ShortAssociation`。
> 可访问 https://babyfish-ct.github.io/jimmer-doc/zh/docs/quick-view/save/export/short 查看。

## 定义Input DTO

1.  安装DTO语言Intellij插件：https://github.com/ClearPlume/jimmer-dto *（此过程不是必须的，但非常推荐）*

2.  新建目录`src/main/dto`

3.  在`src/main/dto`下建立一个文件`Book.dto`，编写代码如下

```sh title="Book.dto"
input BookInputWithShortAssociations {
    #allScalars(this)

    id(store) //默认别名storeId

    id(authors) as authorIds
}
```

## 生成的代码

**Java**

```java title="BookInputWithShortAssociations.java"
@GeneratedBy(
        file = "<yourproject>/src/main/dto/Book.dto"
)
public class BookInputWithShortAssociations implements Input<Book> {

    @Nullable
    private Long id;

    @NotNull
    private String name;

    private int edition;

    @NotNull
    private BigDecimal price;

    @Nullable

    private Long storeId;

    @NotNull

    private List<Long> authorIds;

    ...省略其他方法...
}
```

**Kotlin**

```kotlin title="BookInputWithShortAssociations.kt"
@GeneratedBy(
        file = "<yourproject>/src/main/dto/Book.dto"
)
data class BookInputWithShortAssociations(
    val id: Long?,
    val name: String,
    val edition: Int,
    val price: BigDecimal,

    val storeId: Long?

    val authorIds: List<Long>
) : Input<Book> {
    ...省略其他方法...
}
```

## 编写HTTP服务

**Java**

```java title="BookController.java"
@RestController
public class BookController {

    private final JSqlClient sqlClient;

    public BookController(JSqlClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    @PutMapping("/book")
    pubic int saveBookInputWithShortAssociations(

        @RequestBody BookInputWithShortAssociations input
    ) {
        return sqlClient
            .save(input)
            .getTotalAffectedRowCount();
    }
}
```

**Kotlin**

```kotlin title="BookController.java"
class BookController(
    private val sqlClient: KSqlClient
) {

    @PutMapping("/book")
    fun saveBookInputWithShortAssociations(

        @RequestBody input: BookInputWithShortAssociations
    ): Int =
        sqlClient
            .save(input)
            .totalAffectedRowCount
}
```

可见，无论Input DTO如何改变，Jimmer仍然只需一个方法调用即可完成数据保存。
