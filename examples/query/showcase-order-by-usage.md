# 5.1 基本用法

> 来源: https://jimmer.deno.dev/zh/docs/showcase/order-by/usage

* [案例展示 ★](/zh/docs/showcase/)
* [5. 排序](/zh/docs/showcase/order-by/)
* 5.1 基本用法

本页总览

# 5.1 基本用法

## 多个orderBy[​](#多个orderby "多个orderBy的直接链接")

* Java
* Kotlin

```
BookTable table = BookTable.$;  
  
List<Book> books = sqlClient  
    .createQuery(table)  
    .orderBy(table.name().asc())  
    .orderBy(table.edition().desc())  
    .select(table)  
    .execute();
```

```
val books = sqlClient  
    .createQuery(Book::class) {  
        orderBy(table.name().asc())  
        orderBy(table.edition().desc())  
        select(table)  
    }  
    .execute()
```

## orderBy带多个参数[​](#orderby带多个参数 "orderBy带多个参数的直接链接")

还有另外一种写法与上述代码等价

* Java
* Kotlin

```
BookTable table = BookTable.$;  
  
List<Book> books = sqlClient  
    .createQuery(table)  
    .orderBy(  
        table.name().asc(),   
        table.edition().desc()  
    )  
    .select(table)  
    .execute();
```

```
val books = sqlClient  
    .createQuery(Book::class) {  
        orderBy(  
            table.name.asc(),   
            table.edition.desc()  
        )  
        select(table)  
    }  
    .execute()
```

然而，之前的写法更利于代码结构组织，故而更推荐之前的写法。

## 基于子查询的排序[​](#基于子查询的排序 "基于子查询的排序的直接链接")

* Java
* Kotlin

```
BookTable table = BookTable.$;  
AuthorTableEx author = AuthorTableEx.$;  
  
List<Book> books = sqlClient  
    .createQuery(table)  
    .orderBy(  
        sqlClient  
            .createSubQuery(author)  
            .where(author.books().eq(table))  
            .select(Expression.rowCount())  
            .desc()  
    )  
    .select(table)  
    .execute();
```

```
val books = sqlClient  
    .createQuery(Book::class) {  
        orderBy(  
            subQuery(Author::class) {  
                where(table.books eq parentTable)  
                select(rowCount())  
            }  
            .desc()  
        )  
        select(table)  
    }  
    .execute()
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/order-by/usage.mdx)

最后于 **2025年9月16日** 更新