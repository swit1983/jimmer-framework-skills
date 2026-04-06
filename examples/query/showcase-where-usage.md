# 4.1 基本用法

> 来源: https://jimmer.deno.dev/zh/docs/showcase/where/usage

* [案例展示 ★](/zh/docs/showcase/)
* [4. 条件](/zh/docs/showcase/where/)
* 4.1 基本用法

本页总览

# 4.1 基本用法

以下三种写法等价

## 使用多个where[​](#使用多个where "使用多个where的直接链接")

* Java
* Kotlin

```
String name = ...略...;  
int edition = ...略...;  
  
BookTable table = BookTable.$;  
List<Book> books = sqlClient  
    .createQuery(table)  
    .where(table.name().ilike(name))  
    .where(table.edition().eq(edition))  
    .select(table)  
    .execute();
```

```
val name: String = ...略...  
val edition: Int = ...略...  
  
val books = sqlClient  
    .createQuery(Book::class) {  
        where(table.name ilike name)  
        where(table.edition eq edition)  
        select(table)  
    }  
    .execute()
```

## where带多个参数[​](#where带多个参数 "where带多个参数的直接链接")

* Java
* Kotlin

```
String name = ...略...;  
int edition = ...略...;  
  
BookTable table = BookTable.$;  
List<Book> books = sqlClient  
    .createQuery(table)  
    .where(  
        table.name().ilike(name),  
        table.edition().eq(edition)  
    )  
    .select(table)  
    .execute();
```

```
val name: String = ...略...  
val edition: Int = ...略...  
  
val books = sqlClient  
    .createQuery(Book::class) {  
        where(  
            table.name ilike name,  
            table.edition eq edition  
        )  
        select(table)  
    }  
    .execute();
```

## 使用逻辑与[​](#使用逻辑与 "使用逻辑与的直接链接")

* Java
* Kotlin

```
String name = ...略...;  
int edition = ...略...;  
  
BookTable table = BookTable.$;  
List<Book> books = sqlClient  
    .createQuery(table)  
    .where(  
        Predicate.and(  
            table.name().ilike(name),  
            table.edition().eq(edition)  
        )  
    )  
    .select(table)  
    .execute();
```

```
val name: String = ...略...  
val edition: Int = ...略...  
  
val books = sqlClient  
    .createQuery(Book::class) {  
        where(  
            and(  
                table.name ilike name,  
                table.edition eq edition  
            )  
        )  
        select(table)  
    }  
    .execute()
```

## 建议[​](#建议 "建议的直接链接")

以上三种写法完全等价。然而，毫无疑问，第一种是最简单的，故而推荐。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/showcase/where/usage.mdx)

最后于 **2025年9月16日** 更新