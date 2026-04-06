# 4.5 隐式子查询

> 来源: https://jimmer.deno.dev/zh/docs/showcase/where/implicit-subquery

* [案例展示 ★](/zh/docs/showcase/)
* [4. 条件](/zh/docs/showcase/where/)
* 4.5 隐式子查询

本页总览

# 4.5 隐式子查询

隐式子查询仅可用于集合关联，本文通过多对多`Book.authors`来讨论隐式子查询。

## 用法[​](#用法 "用法的直接链接")

* Java
* Kotlin

```
@Nullable String authorName = ...略...;  
@Nullable Gender authorGender = ...略...;  
  
BookTable table = BookTable.$;  
List<Book> books = sqlClient  
    .createQuery(table)  
    .where(  
        table.authors(author -> ❶  
            Predicate.or(  
                author.firstName().ilikeIf(authorName),  
                author.lastName().ilikeIf(authorName)  
            )  
        )   
    )   
    .where(  
        table.authors(author -> ❷  
            author.gender().eqIf(authorGender)  
        )  
    )   
    .select(table)  
    .execute();
```

```
val authorName: String? = ...略...  
val authorGender: Gender? = ...略...  
  
val books = sqlClient  
    .createQuery(Book::class) {  
        where += table.authors { ❶  
            or(  
                firstName `ilike?` authorName,  
                lastName `ilike?` authorName  
            )  
        }  
        where += table.authors { ❷  
            gender `eq?` authorGender  
        }  
        select(table)  
    }  
    .execute()
```

其实，上面的代码中的两个隐式子查询可以合并成一个，但是为了后文更好的展示，故意写成了两个。

## 各种情况[​](#各种情况 "各种情况的直接链接")

### 所有子查询都不生效[​](#所有子查询都不生效 "所有子查询都不生效的直接链接")

如果`authorName`和`authorGender`都为null，会导致❶和❷两处创建的子查询都失效，不会渲染任何真实的SQL子查询。

这时，生成如下SQL:

```
select  
    tb_1_.ID,  
    tb_1_.NAME,  
    tb_1_.EDITION,  
    tb_1_.PRICE,  
    tb_1_.STORE_ID  
from BOOK tb_1_
```

### 部分子查询生效[​](#部分子查询生效 "部分子查询生效的直接链接")

将`authorName`指定为非null，而`authorGender`仍然为null，❶处的子查询生效，而❷的子查询被忽略。

这时，生成如下SQL:

```
select  
    tb_1_.ID,  
    tb_1_.NAME,  
    tb_1_.EDITION,  
    tb_1_.PRICE,  
    tb_1_.STORE_ID  
from BOOK tb_1_  
where  
    exists(  
        select  
            1  
        from AUTHOR tb_2_  
        inner join BOOK_AUTHOR_MAPPING tb_3_  
            on tb_2_.ID = tb_3_.AUTHOR_ID  
        where  
                tb_3_.BOOK_ID = tb_1_.ID  
            and  
                (  
                    lower(tb_2_.FIRST_NAME) like ? /* %a% */  
                or  
                    lower(tb_2_.LAST_NAME) like ? /* %a% */  
                )  
    )
```

### 所有子查询都生效[​](#所有子查询都生效 "所有子查询都生效的直接链接")

如果`authorName`和`authorGender`都被指定为非null，会导致❶和❷两处创建的隐式子查询都生效。

提示

Jimmer能自动合并冲突的隐式子查询，两个隐式子查询会被合并为一个隐式子查询，最终，只有一个SQL子查询被渲染

```
select  
    tb_1_.ID,  
    tb_1_.NAME,  
    tb_1_.EDITION,  
    tb_1_.PRICE,  
    tb_1_.STORE_ID  
from BOOK tb_1_  
where  
    exists(  
        select  
            1  
        from AUTHOR tb_2_  
        inner join BOOK_AUTHOR_MAPPING tb_3_  
            on tb_2_.ID = tb_3_.AUTHOR_ID  
        where  
                tb_3_.BOOK_ID = tb_1_.ID  
            and  
                (  
                    lower(tb_2_.FIRST_NAME) like ? /* %a% */  
                or  
                    lower(tb_2_.LAST_NAME) like ? /* %a% */  
                )  
            and  
                tb_2_.GENDER = ? /* M */  
    )
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/where/implicit-subquery.mdx)

最后于 **2025年9月16日** 更新