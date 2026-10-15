---
title: 合并冲突连接
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/dynamic-join/merge
---

# 合并冲突连接

## 使用方法

在目前其他操作SQL的技术方案，无论ORM还是非ORM，都有存在一个空白领域：只考虑到了动态`where`，没有考虑`动态join`。

`动态JOIN`定义：如果某些动态查询条件针对其他表而非当前表。这意味着

-   条件满足时：先通过关联属性join到其他表，再对join得到的表添加动态where条件

-   条件不满足时：不能通过关联属性join其他表

## 场景-1

让我们先来看第一个场景，这里用面向原生SQL的MyBatis为例

-   定义MyBatis Mapper接口

```java
@Mapper
public interface BookMapper {

    List<Book> findBooks(
        @Nullable String name,
        @Nullable String storeName,
        @Nullable String storeWebsite
    );
}
```

    这里，所有查询参数都可能为null，很明显，这是一个动态查询。

    后面两个查询参数：`storeName`和`storeWebsite`，其过滤条件并不施加在当前表`BOOK`上，而是施加在父表`BOOK_STORE上`。
    即，当这两个参数中的任何一个非null时，都会生成对`BOOK_STORE`表的JOIN，这种由参数值动态决定是否添加的表连接，在本文中被称为`动态JOIN`。

-   定义MyBatis的SQL映射XML

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="somepackage.BookMapper">
    <select id="findBooks" resultType="somepackage.Book">
        select * from BOOK as book

        <if test="storeName != null or storeWebsite != null"> ❶
            inner join BOOK_STORE as store
                on book.STORE_ID = store.ID
        </if>
        <where>
            <if test="name != null">
                and book.NAME = #{name}
            </if>
            <if test="storeName != null"> ❷
                and store.NAME = #{storeName}
            </if>
            <if test="storeWebsite != null">
                and store.WEBSITE = #{storeWebsite} ❸
            </if>
        </where>
    </select>
</mapper>
```

    其中，❶就是动态JOIN。然而对开发人员而言，❷和❸才是目的，❶是为支持❷和❸而不得不做的工作，其判断条件是一种负担。

    也许你已经注意到了，❶的判断条件使用了`or`，这也不难理解。

    **然而，这仅仅是最简单的两表之间动态连接，对于更深的多表连接操作而言，动态连接的复杂度会急剧上升！**

## 场景-2

让我们再来看第二个场景，有了前面的例子作为基础，这个例子就不同任何业务耦合了。

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="somepackage.AMapper">
    <select id="findAObjects" resultType="somepackage.A">
        select distinct A.id
        from A

        <if test="bId != null or cId != null or dId != null or eId != null">
            inner join B on A.ID = B.A_ID
        </if>
        <if test="cId != null or dId != null or eId != null">
            inner join C on B.ID = C.B_ID
        </if>
        <if test="dId != null or eId != null">
            inner join D on C.ID = D.C_ID
        </if>
        <if test="eId != null">
            inner join E on D.ID = E.D_ID
        </if>

        <where>
            <if test="aId != null">
                and A.ID = #{aId}
            </if>
            <if test="bId != null">
                and B.ID = #{bId}
            </if>
            <if test="cId != null">
                and C.ID = #{cId}
            </if>
            <if test="dId != null">
                and D.ID = #{dId}
            </if>
            <if test="eId != null">
                and E.ID = #{eId}
            </if>
        </where>
    </select>
</mapper>
```

这个例子逻辑很简单，A、B、C、D和E这五张表形成了一个JOIN链，每张表都一个动态查询条件。然而，正如你所见，动态JOIN的复杂度已经变得不可接受了。中，我们列举了两个场景，本文讨论是否如何解决这两个场景的问题。

:::note
这个问题有两种解决方啊，本文演示第一种解决办法
:::

-   解决场景1的问题

**Java**

```java
List<Book> findBooks(
        @Nullable String name,
        @Nullable String storeName,
        @Nullable String storeWebsite
) {
    BookTable table = Tables.BOOK_TABLE;

    return sqlClient
        .createQuery(table)
        .whereIf(
            name != null,
            () -> book.name().like(name)
        )
        .whereIf(
            storeName != null,
            () -> table
                .store() ❶
                .name()
                .like(storeName)
        )
        .whereIf(
            storeWebsite != null,
            () -> table
                .store() ❷
                .website()
                .like(storeWebsite)
        )
        .select(book)
        .execute();
}
```

**Kotlin**

```kotlin
fun findBooks(
    name: String? = null,
    storeName: String? = null,
    storeWebsite: String? = null
): List<Book> =
    sqlClient
        .createQuery(Book::class) {
            name?.let {
                where(table.name like it)
            }
            storeName?.let {
                where(
                    table
                        .store ❶
                        .name like it
                )
            }
            storeWebsite?.let {
                where(
                    table
                        .store ❷
                        .website like it
                )
            }
            select(table)
        }
        .execute()
```

    这是一个典型的动态查询，三个查询参数都允许为null。

    -   指定`name`，但`storeName`和`storeWebsite`仍然为null。

        这时，❶和❷两处的代码都不会执行，最终生成的SQL不会包含任何join。

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_
    where tb_1_.NAME = ?
```

    -   指定`name`和`storeName`, 但`storeWebsite`仍然为null。

        这时，❶处的连接生效但❷处的代码不会被执行，最终生成的SQL如下。

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_

inner join BOOK_STORE as tb_2_
    on tb_1_.STORE_ID = tb_2_.ID

where
    tb_1_.NAME = ?
and
    tb_2_.NAME = ?
```

    -   指定所有参数，`name`, `storeName`和`storeWebsite`都非null。

        这时，❶、❷两处的连接都生效，这种情况叫连接冲突。

        :::tip
        这种冲突并不会导致任何问题，冲突的join对象会被合并成一个join对象。

        因此，在最终生成的SQL，不会导致重复的join操作。
        :::

```csharp
select
    tb_1_.ID,
    tb_1_.NAME,
    tb_1_.EDITION,
    tb_1_.PRICE,
    tb_1_.STORE_ID
from BOOK as tb_1_

    inner join BOOK_STORE as tb_2_ on
        tb_1_.STORE_ID = tb_2_.ID

where
    tb_1_.NAME = ?
and
    tb_2_.NAME = ?
and
    tb_2_.WEBSITE = ?
```

-   解决场景2的问题

**Java**

```java
List<Long> findDistinctIds(
    @Nullable Long aId,
    @Nullable Long bId,
    @Nullable Long cId,
    @Nullable Long dId,
    @Nullable Long eId
) {
    ATable table = Tables.A_TABLE;

    return sqlClient
        .createQuery(table)
        .whereIf(
            aId != null,
            () -> table.id().like(aId)
        )
        .whereIf(
            bId != null,
            () -> table.asTableEx().bs().id().like(bId)
        )
        .whereIf(
            cId != null,
            () -> table.asTableEx().bs().cs().id().like(cId)
        )
        .whereIf(
            dId != null,
            () -> table.asTableEx().bs().cs().ds().id().like(dId)
        )
        .whereIf(
            eId != null,
            () -> table.asTableEx().bs().cs().ds().es().id().like(eId)
        )
        .select(book.id())
        .distinct()
        .execute();
}
```

**Kotlin**

```kotlin
fun findDistinctIds(
    aId: Long? = null,
    bId: Long? = null,
    cId: Long? = null,
    dId: Long? = null,
    eId: Long? = null
): List<Long> =
    sqlClient
        .createQuery(A::class) {
            aId?.let {
                where(table.id eq it)
            }
            bId?.let {
                where(table.asTableEx().bs.id eq it)
            }
            cId?.let {
                where(table.asTableEx().bs.cs.id eq it)
            }
            dId?.let {
                where(table.asTableEx().bs.cs.ds.id eq it)
            }
            eId?.let {
                where(table.asTableEx().bs.cs.ds.es.id eq it)
            }
            select(table.id)
        }
        .distinct()
        .execute()
```

    :::info
    -   有了前面的基础，这里不再需要罗列不同的参数组合下会生成和何种SQL。明白无论如何最终SQL都不会包含重复的连接操作即可。

    -   这里的`asTableEx`是后续文档[分页安全性](./table-ex.md)要介绍的概念。这里，请读者先忽略它。
    :::

## 合并规则

假设有三个表连接路径，分别是

-   a -> b -> c -> d -> e -> f -> g
-   a -> b -> c -> h -> i -> j
-   a -> x -> y -> z -> a-> b -> c -> d

为了消除冲突，Jimmer会先把这些路径中的节点合并成一棵树

```
-+-a
 |
 +----+-b
 |    |
 |    \----+-c
 |         |
 |         +----+-d
 |         |    |
 |         |    \----+-e
 |         |         |
 |         |         \----+-f
 |         |              |
 |         |              \------g
 |         |
 |         \----+-h
 |              |
 |              \----+-i
 |                   |
 |                   \------j
 |
 \----+-x
      |
      \----+-y
           |
           \----+-z
                |
                \----+-a
                     |
                     \----+-b
                          |
                          \----+-c
                               |
                               \------d
```

然后根据这棵树来生成最终SQL中的join子句。

另外一个需要说明的规则，就是连接方式。创建join对象的方法具备参数，以指定连接方式，比如，左连接：

**Java**

```java
book.store(JoinType.LEFT)
```

**Kotlin**

```kotlin
book.`store?`
```

连接方式合并规则如下：

- 如果发生冲突的join节点的连接方式全部一样，合并后连接方式不变。
- 否则，合并后一定是内连接。
