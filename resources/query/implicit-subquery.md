# 隐式子查询

**隐式子查询** 对集合关联（一对多或多对多）的子查询给予了极大简化。

Jimmer 自动为集合关联生成特殊的 DSL 方法，用户只需要提供关联对象的过滤条件即可，Jimmer 自动生成 exists 子查询和关联连接条件。

## 示例

查询包含指定作者名字的书籍：

```java
// Java
public List<Book> findBooks(@Nullable String authorName) {
    return sqlClient
            .createQuery(table)
            .whereIf(
                authorName != null && !authorName.isEmpty(),
                table.authors(author -> {
                    return Predicate.or(
                        author.firstName().ilike(authorName),
                        author.lastName().ilike(authorName)
                    );
                })
            )
            .select(table)
            .execute();
}
```

生成的 SQL：

```sql
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK tb_1_
where
    exists(
        select 1
        from AUTHOR tb_2_
        inner join BOOK_AUTHOR_MAPPING tb_3_
        on tb_2_.ID = tb_3_.AUTHOR_ID
        where
            tb_3_.BOOK_ID = tb_1_.ID
            and (
                lower(tb_2_.FIRST_NAME) like '%alex%'
                or
                lower(tb_2_.LAST_NAME) like '%alex%'
            )
    )
```

## 和普通子查询的区别

| 对比 | 隐式子查询 | 普通子查询 |
|------|-------------|-------------|
| 父子关联条件 | 自动生成 | 需要手动编写 |
| 代码简洁度 | 非常简洁，只需要关注过滤条件 | 需要完整编写 |

## 自动合并

同一个 `and` / `or` / `not` 内部，针对**相同关联**的多个隐式子查询会被自动合并。

示例：

```java
public List<Book> findBooks(
        @Nullable String authorName,
        @Nullable Gender authorGender
) {
    return sqlClient
            .createQuery(table)
            .whereIf(
                authorName != null && !authorName.isEmpty(),
                table.authors(author -> Predicate.or(
                    author.firstName().ilike(authorName),
                    author.lastName().ilike(authorName)
                ))
            )
            .whereIf(
                authorGender != null,
                table.authors(author -> author.gender().eq(authorGender))
            )
            .select(table)
            .execute();
}
```

即使写了两个 `table.authors(...)` 隐式子查询，最终 SQL 只会合并为一个 exists 子查询：

```sql
where
    exists(
        select 1
        from AUTHOR tb_2_
        inner join BOOK_AUTHOR_MAPPING tb_3_
        on tb_2_.ID = tb_3_.AUTHOR_ID
        where
            tb_3_.BOOK_ID = tb_1_.ID
            and (
                lower(tb_2_.FIRST_NAME) like '%alex%'
                or
                lower(tb_2_.LAST_NAME) like '%alex%'
            )
            and
                tb_2_.GENDER = ? /* M */
    )
```

> 合并只发生在同一个 and/or/not 内部。
