# 关联分类

> 来源: https://jimmer.deno.dev/zh/docs/mutation/save-command/association/classification

- [修改篇](/zh/docs/mutation/)
- [保存指令](/zh/docs/mutation/save-command/)
- [关联对象](/zh/docs/mutation/save-command/association/)
- 关联分类

本页总览

# 关联分类

## 基本概念[​](#基本概念 "基本概念的直接链接")

可以从两种角度对关联对象进行分类，从每个角度看都有两种关联类型，共计4种

- 按照关联对象形状分类

  - **短关联**

    只修改当前对象和其他对象之间的关联关系，不会进一步保存关联对象。

    > 递归保存行为被终止，不会继续深入。
  - **长关联**

    不但可以修改当前对象和其他对象之间的关联关系，还会进一步保存关联对象。

    > 递归保存行为不会被终止，会继续深入。
- 按照保存顺序分类

  - **前置关联**

    关联对象比当前对象更早保存，其实就是基于于外键 *(无论真伪)* 的关联。

    例如：本教程中的`Book.store`。
  - **后置关联**

    关联对象比当前对象更晚保存，包括两种情况

    - 前置关联的逆关联。

      例如：本教程中的`BookStore.books`
    - 基于中间表的关联。

      例如：本教程中的`Book.authors`和`Author.books`

## 1. 按照关联对象形状分类[​](#1-按照关联对象形状分类 "1. 按照关联对象形状分类的直接链接")

### 1.1. 短关联[​](#11-短关联 "1.1. 短关联的直接链接")

所谓短关联，表示仅修改当前对象和其他对象之间的关联本身，对关联对象的修改没兴趣。

通常，UI设计会采用单选框 *(关联引用)* 或多选框 *(关联集合)*。

# Book Form

Name

Name

Edition

Edition

Price

Price

Store

O'REILLY

Authors

Authors

Eve Procello, Alex Banks

Authors

提交

其中

- 单选框对应多对一关联`Book.store`
- 多选框对应多对多关联`Book.authors`

备注

实际项目中，待选数据可能很多，并不适合设计为下拉UI。这时，可以使用具备筛选条件和分页功能的对象框来代替下拉框，这是一种常见的变通方式。

由于用户只想修改当前对象和其他对象的关联，不想进一步修改关联对象，所以UI不可能出现多层关联嵌套。这就是称其为 **短关联** 的原因。

为save指令传递任意形状的数据结构作为参数时，指定短关联有两种方法

- 将Id-Only对象作为关联对象
- 在启用专用配置的前提下，将Key-Only对象作为关联对象

#### 1.1.1. 将Id-Only对象作为关联对象[​](#111-将id-only对象作为关联对象 "1.1.1. 将Id-Only对象作为关联对象的直接链接")

让关联对象只有id属性

- Java
- Kotlin

```book book = immutables.createbook(draft -> {
    draft.setName("SQL in Action");
    draft.setEdition(1);
    draft.setPrice(new BigDecimal("39.9"));

    // 关联对象只有id属性
    draft.setStoreId(2L);

    draft.addIntoAuthors(author -> {
        // 关联对象只有id属性
        author.setId(4L);
    });
    draft.addIntoAuthors(author -> {
        // 关联对象只有id属性
        author.setId(5L);
    });
});
sqlClient.save(book);

```

```val book = book {
    name = "SQL in Action"
    edition = 1
    price = BigDecimal("39.9")

    // 关联对象只有id属性
    storeId = 2L

    authors().addBy {
        // 关联对象只有id属性
        id = 4L
    }
    authors().addBy {
        // 关联对象只有id属性
        id = 5L
    }
}
sqlClient.save(book)

```

备注

这里对被保存数据结构进行硬编码仅为示范，实际项目中被保存的数据结构由前端界面提交。

当然，如果用户按照[映射篇/进阶映射/视图属性/IdView](/zh/docs/mapping/advanced/view/id-view)一文的方法定义了`authorIds`属性，上述代码可以被简化，例如：

- Java
- Kotlin

```book book = immutableobjects.createbook(draft -> {
    draft.setAuthorIds(Arrays.asList(4L, 5L));
});

```

```val book = book {
    authorIds = listOf(4L, 5L)
}

```

但这并不是必须的，为了让例子更具普适性，这里并不假设用户为实体类型定义了[IdView](/zh/docs/mapping/advanced/view/id-view)属性。后续所有文档都如此，不再提醒。

生成如下SQL

1. 保存聚合根。

   - H2
   - Mysql
   - Postgres

   ```merge into book(
       NAME, EDITION, PRICE, STORE_ID
   ) key(
       NAME, EDITION
   ) values(
       ? /* SQL in Action */,
       ? /* 1 */,
       ? /* 39.9 */,
       ? /* 2 */
   )

```

   ```insert into book(
       NAME, EDITION, PRICE, STORE_ID
   ) values(
       ? /* SQL in Action */,
       ? /* 1 */,
       ? /* 39.9 */,
       ? /* 2 */
   ) on duplicate key update
       /* fake update to return all ids */ ID = last_insert_id(ID),
       PRICE = values(PRICE),
       STORE_ID = values(STORE_ID)

```

   ```into into into book(
       NAME, EDITION, PRICE, STORE_ID
   ) values(
       ? /* SQL in Action */,
       ? /* 1 */,
       ? /* 39.9 */,
       ? /* 2 */
   ) on conflict(
       NAME, EDITION
   ) do update set
       PRICE = excluded.PRICE,
       STORE_ID = excluded.STORE_ID
   returning ID

```

   由于`Book.store`是直接基于外键 *(STORE\_ID)* 的多对一关系，当前对象和`BookStore(2)`对象的关联将会因这条SQL的执行而被自动创建。
2. 如果和当前对象 *(新插入的数据为`Book(100)`)* 关联的`Author`对象不仅仅只有`Author(4)`和`Author(5)`，切断和其他对象啊的关联。

   - H2
   - Mysql
   - Postgres

   ```delete from book_author_mapping
   where
       BOOK_ID = ? /* 100 */
   and
       not (
           AUTHOR_ID = any(? /* [4, 5] */)
       )

```

   ```delete from book_author_mapping
   where
       BOOK_ID = ? /* 100 */
   and
       AUTHOR_ID not in(
           ? /* 4 */,
           ? /* 5 */
       )

```

   ```delete from book_author_mapping
   where
       BOOK_ID = ? /* 100 */
   and
       not (
           AUTHOR_ID = any(? /* [4, 5] */)
       )

```

   信息

   这个步骤叫做[脱勾操作](/zh/docs/mutation/save-command/association/dissociation)，后续文档会给予介绍，这里请读者先行忽略
3. 建立对象 *(新插入的数据为`Book(100)`)* 和`Author(4)`和`Author(5)`两个对象之间的关联

   - H2
   - Mysql
   - Postgres

   ```merge into book_author_mapping tb_1_
   using(values(?, ?)) tb_2_(
       BOOK_ID, AUTHOR_ID
   )
   on
       tb_1_.BOOK_ID = tb_2_.BOOK_ID
   and
       tb_1_.AUTHOR_ID = tb_2_.AUTHOR_ID
   when not matched
       then insert(BOOK_ID, AUTHOR_ID)
       values(tb_2_.BOOK_ID, tb_2_.AUTHOR_ID)
   /* batch-0: [100, 4] */
   /* batch-1: [100, 5] */

```

   警告

   默认情况下，MySQL的批量操作不会被采用，而采用多条SQL。具体细节请参考[MySQL的问题](/zh/docs/mutation/save-command/mysql)

   1. ```insert
      ignore
      into BOOK_AUTHOR_MAPPING(
          BOOK_ID, AUTHOR_ID
      ) values(
          ? /* 100 */, ? /* 4 */
      )

```

   2. ```insert
      ignore
      into BOOK_AUTHOR_MAPPING(
          BOOK_ID, AUTHOR_ID
      ) values(
          ? /* 100 */, ? /* 5 */
      )

```

   ```insert into book_author_mapping(
       BOOK_ID, AUTHOR_ID
   ) values(
       ?, ?
   ) on conflict(
       BOOK_ID, AUTHOR_ID
   )
   do nothing
   /* batch-0: [100, 4] */
   /* batch-1: [100, 5] */

```

信息

通过此例，不难发现，短关联只会创建或销毁当前对象和其他对象之间的关联关系，不会进一步保存关联对象。

短关联总是假设被引用的对象是存在的，如果所引用的对象 *(这个例子中的`BookStore(2)`, `Author(4)`和`Author(5)`)* 不存在，会导致异常！

#### 1.1.2. 在启用专用配置的前提下，将Key-Only对象作为关联对象[​](#112-在启用专用配置的前提下将key-only对象作为关联对象 "1.1.2. 在启用专用配置的前提下，将Key-Only对象作为关联对象的直接链接")

下面代码，假设

- `BookStore`类型的key属性是`name`

  查看
- `Author`类型的key属性是`firstName`和`lastName`

  > 实际业务中，这个唯一性约束未必合理，这里为简化例子，姑且这样假设。

  查看

- Java
- Kotlin

```book book = immutables.createbook(draft -> {
    draft.setName("SQL in Action");
    draft.setEdition(1);
    draft.setPrice(new BigDecimal("39.9"));
    draft.applyStore(store -> {
        // 关联对象只有key属性，即`BookStore.name`
        store.setName("MANNING");
    });
    draft.addIntoAuthors(author -> {
        // 关联对象只有key属性，即`Author.firstName`和`Author.lastName`
        author.setFirstName("Boris").setLastName("Cherny");
    });
    draft.addIntoAuthors(author -> {
        // 关联对象只有key属性，即`Author.firstName`和`Author.lastName`
        author.setFirstName("Samer").setLastName("Buna");
    });
});
sqlClient
    .saveCommand(book)
    .setKeyOnlyAsReference(BookProps.STORE)
    .setKeyOnlyAsReference(BookProps.AUTHORS)
    .execute();

```

```val book = book {
    name = "SQL in Action"
    edition = 1
    price = BigDecimal("39.9")
    store {
        // 关联对象只有key属性，即`BookStore.name`
        name = "MANNING"
    }
    authors().addBy {
        // 关联对象只有key属性，即`Author.firstName`和`Author.lastName`
        firstName = "Boris"
        lastName = "Cherny"
    }
    authors().addBy {
        // 关联对象只有key属性，即`Author.firstName`和`Author.lastName`
        firstName = "Samer"
        lastName = "Buna"
    }
}
sqlClient.save(book) {
    setKeyOnlyAsReference(Book::store)
    setKeyOnlyAsReference(Book::authors)
}

```

信息

- *默认情况下key-only关联对象被视为长关联**

然而，开发人员可以通过调用`setKeyOnlyAsReference`方法将key-only关联对象视为短关联。

- 这里，两次调用`setKeyOnlyAsReference`方法，明确地配置关联`Book.store`和`Book.authors`。

  事实上，你也可以调用一次`setKeyOnlyAsReferenceAll`方法，盲目地配置所有关联关系。
- 和Kotlin相比，Java API对保存指令的高级配置的便捷性稍低。

  先调用`saveCommand`方法创建一个保存指令但不立即执行，完成高级配置后  ，再调用`execute`方法真正执行。

### 1.2. 长关联[​](#12-长关联 "1.2. 长关联的直接链接")

所谓长关联，表示除了要修改当前对象和其他对象之间的关联本身外，还要进一步修改关联对象。

通常，订单和订单明细是这类场景的最佳示范，UI设计会采用内嵌表格，例如

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

由于用户不但要修改当前对象和其他对象的关联，还要进一步修改关联对象，而关联对象可以包含更深的关联，所以，理论上讲，UI可出多层关联嵌套。这就是称其为 **长关联** 的原因。

备注

虽然设计人员为了保证UI的简洁性会刻意避免在内嵌表格中嵌套更深的内嵌表格，但是实际项目中仍然存在需要在UI上维护多层嵌套关联的场景，比如：

- 表单本身是一颗树结构，编辑好了后，作为一个整体保存。
- 可视化UI设计，因为UI组件本身就是树形结构，用户进行一系列可视化拖拉拽的设计后，把UI组件树作为一个整体保存。

Jimmer可以直接保存任意形状的长关联数据结构，如果把深度未知的长关联数据结构称为复杂表单，**保存指令就是为复杂表单而设计。**

例子如下

- Java
- Kotlin

```order order = immutables.createorder(draft -> {
    draft.setCustomerId(1L);
    draft.setProvince("四川");
    draft.setCity("成都");
    draft.setAddress("龙泉驿区洪玉路与十洪路交叉口");
    draft.addIntoItems(item -> {
        item.setProductId(8L);
        item.setQuantity(2);
    });
    draft.addIntoItems(item -> {
        item.setProductId(9L);
        item.setQuantity(1);
    });
});
sqlClient.insert(order);

```

```val order = order {
    customerId = 1L
    province = "四川"
    city = "成都"
    address = "龙泉驿区洪玉路与十洪路交叉口"
    items().addBy {
        productId = 8L
        quantity = 2
    }
    items().addBy {
        productId = 9L
        quantity = 1
    }
}
sqlClient.insert(order)

```

在这个例子中，我们可以看到很多短关联，例如`Order.customer`, `OrderItem.product`，但是，们并非这里应该关注的重点。

在这里，我们应该关注关联`Order.items`，很明显，它是一个长关联。

此操作会生成两条SQL

1. 插入根对象`Order`

   ```insert into order_(
       PROVINCE, CITY, ADDRESS, CUSTOMER_ID
   ) values(
       ? /* 四川 */,
       ? /* 成都 */,
       ? /* 龙泉驿区洪玉路与十洪路交叉口 */,
       ? /* 1 */
   )

```

2. 插入所有子对象`OrderItem`

   - 绝大部分数据库
   - Mysql

   ```insert into order_item(
       ORDER_ID,
       PRODUCT_ID,
       QUANTITY
   ) values(?, ?, ?)
   /* batch-0: [100, 8, 2] */
   /* batch-1: [100, 9, 1] */

```

   警告

   默认情况下，MySQL的批量操作不会被采用，而采用多条SQL。具体细 节请参考[MySQL的问题](/zh/docs/mutation/save-command/mysql)

   1. ```insert into order_item(
          ORDER_ID,
          PRODUCT_ID,
          QUANTITY
      ) values(
          ? /* 100 */,
          ? /* 8 */,
          ? /* 2 */
      )

```

   2. ```insert into order_item(
          ORDER_ID,
          PRODUCT_ID,
          QUANTITY
      ) values(
          ? /* 100 */,
          ? /* 9 */,
          ? /* 1 */
      )

```

信息

由此可见，长关联不仅能修改当前对象和其他对象的关联关系，还是会导致关联对象被保存。

如果关联对象也具备长关联，将会递归保存，直到没有更多关联属性或遇到短关联为止。

## 2. 按照保存顺序分类[​](#2-按照保存顺序分类 "2. 按照保存顺序分类的直接链接")

### 2.1. 前置关联[​](#21-前置关联 "2.1. 前置关联的直接链接")

前置关联就是基于外键 *(无论真伪)* 的关联，其工作模式为，先保存关联对象，再保存根对象。

- Java
- Kotlin

```book book = immutables.createbook(draft -> {
    draft.setName("SQL in Action");
    draft.setEdition(1);
    draft.setPrice(new BigDecimal("49.9"));
    draft.applyStore(store -> {
        store.setName("TURING");
        store.setWebsite("https://www.turing.com");
    });
});
sqlClient.save(book);

```

```val book = book {
    name = "SQL in Action"
    edition = 1
    price = BigDecimal("49.9")
    store {
        name = "TURING"
        website = "https://www.turing.com"
    }
}
sqlClient.save(book)

```

以H2为例，生成两条SQL

1. 先保存关联对象`BookStore`

   - H2
   - Mysql
   - Postgres

   ```merge into book_store(
       NAME, WEBSITE
   ) key(
       NAME
   ) values(
       ? /* TURING */,
       ? /* https://www.turing.com */
   )

```

   ```insert into book_store(
       NAME, WEBSITE
   ) values(
       ? /* TURING */,
       ? /* https://www.turing.com */
   ) on duplcate update
       /* fake update to return all ids */ ID = last_insert_id(ID),
       WEBSITE = VALUES(WEBSITE)

```

   ```insert into book_store(
       NAME, WEBSITE
   ) values(
       ? /* TURING */,
       ? /* https://www.turing.com */
   ) on conflict(
       NAME, WEBSITE
   ) do update set
       WEBSITE = excluded.WEBSITE,
   return ID

```

2. 后保存当前对象`Book` *(假设上个操作返回的id为`100`)*

   - H2
   - Mysql
   - Postgres

   ```merge into book(
       NAME, EDITION, PRICE, STORE_ID
   ) key(
       NAME, EDITION
   ) values(
       ? /* SQL in Action */,
       ? /* 1 */,
       ? /* 49.9 */,
       ? /* 100 */
   )

```

   ```insert into book(
       NAME, EDITION, PRICE, STORE_ID
   ) values(
       ? /* SQL in Action */,
       ? /* 1 */,
       ? /* 49.9 */,
       ? /* 100 */
   ) on duplcate key update
       /* fake update to return all ids */ ID = last_insert_id(ID),
       PRICE = values(PRICE),
       STORE_ID = values(STORE_ID)

```

   ```insert into book(
       NAME, EDITION, PRICE, STORE_ID
   ) values(
       ? /* SQL in Action */,
       ? /* 1 */,
       ? /* 49.9 */,
       ? /* 100 */
   ) on conflict(
       NAME, EDITION
   ) do update set
       PRICE = values(PRICE),
       STORE_ID = values(STORE_ID)
   returning ID

```

警告

在工作交流中，面对前置关联时，建议用"当前对象/关联对象"这样的方式来表达，而不是"父对象/子对象"这种表达方式。

因为，对于前置关联而言，ORM层面的父子关系和数据库建模层面的父子关系完全相反，非常容易引起混淆和误会。

### 2.2. 后置关联[​](#22-后置关联 "2.2. 后置关联的直接链接")

其他关联，例如

- 前置关联的逆关联，*(本教程中的`BookStore.books`)*
- 基于中间表的关联，*(本教程中的`Book.authors`和`Author.books`)*

都可以归为后置关联，是一种更常见的场景。

后置关联的工作模式更容易理解，先保存当前对象，再保存关联对象。

- Java
- Kotlin

```bookstore store = immutables.createbookstore(draft -> {
    draft.setName("TURING");
    draft.setWebsite("https://www.turing.com");
    draft.addIntoBooks(book -> {
        book.setName("SQL in Action");
        book.setEdition(1);
        book.setPrice(new BigDecimal("49.9"));
    });
    draft.addIntoBooks(book -> {
        book.setName("RUST programming");
        book.setEdition(2);
        book.setPrice(new BigDecimal("39.9"));
    });
});
sqlClient
    .saveCommand(store)
    // 请读者先行忽略此配置
    .setTargetTransferModeAll(TargetTransferMode.ALLOWED)
    .execute();

```

```val store = bookstore {
    name = "TURING"
    website = "https://www.turing.com"
    books().addBy {
        name = "SQL in Action"
        edition = 1
        price = BigDecimal("49.9")
    }
    books().addBy {
        name = "RUST programming"
        edition = 2
        price = BigDecimal("39.9")
    }
}
sqlClient.save(store) {
    // 请读者先行忽略此配置
    setTargetTransferModeAll(TargetTransferMode.ALLOWED)
}

```

以H2为例，生成三条SQL

1. 先保存当前对象`BookStore`

   - H2
   - Mysql
   - Postgres

   ```merge into book_store(
       NAME, WEBSITE
   ) key(
       NAME
   ) values(
       ? /* TURING */,
       ? /* https://www.turing.com */
   )

```

   ```insert into book_store(
       NAME, WEBSITE
   ) values(
       ? /* TURING */,
       ? /* https://www.turing.com */
   ) on duplcate update
       /* fake update to return all ids */ ID = last_insert_id(ID),
       WEBSITE = VALUES(WEBSITE)

```

   ```insert into book_store(
       NAME, WEBSITE
   ) values(
       ? /* TURING */,
       ? /* https://www.turing.com */
   ) on conflict(
       NAME, WEBSITE
   ) do update set
       WEBSITE = excluded.WEBSITE,
   return ID

```

2. 后保存关联对象`Book` *(假设上个操作返回的id为`100`)*

   - H2
   - Mysql
   - Postgres

   ```merge into book(
       NAME, EDITION, PRICE, STORE_ID
   ) key(
       NAME, EDITION
   ) values(?, ?, ?, ?)
   /* batch-0: [SQL in Action, 1, 49.9, 100] */
   /* batch-1: [RUST programming, 2, 39.9, 100] */

```

   警告

   默认情况下，MySQL的批量操作不会被采用，而采用多条SQL。具体细节请参考[MySQL的问题](/zh/docs/mutation/save-command/mysql)

   1. ```insert into book(
          NAME, EDITION, PRICE, STORE_ID
      ) values(
          ? /* SQL in Action */,
          ? /* 1 */,
          ? /* 49.9 */,
          ? /* 100 */
      ) on duplcate update
          /* fake update to return all ids */ ID = last_insert_id(ID),
          PRICE = VALUES(PRICE),
          STORE_ID = VALUES(STORE_ID)

```

   2. ```insert into book(
          NAME, EDITION, PRICE, STORE_ID
      ) values(
          ? /* RUST programming */,
          ? /* 2 */,
          ? /* 39.9 */,
          ? /* 100 */
      ) on duplcate update
          /* fake update to return all ids */ ID = last_insert_id(ID),
          PRICE = VALUES(PRICE),
          STORE_ID = VALUES(STORE_ID)

```

   ```insert into book(
       NAME, EDITION, PRICE, STORE_ID
   ) values(
       ?, ?, ?, ?
   ) on conflict(
       NAME, EDITION
   ) do update set
       PRICE = excluded.PRICE,
       STORE_ID = excluded.STORE_ID
   /* batch-0: [SQL in Action, 1, 49.9, 100] */
   /* batch-1: [RUST programming, 2, 39.9, 100] */

```

3. 第三条SQL和这里讨论的话题无关，省略

信息

后置关联的功能比前置关联丰  富，本教程讲重点讨论后置关联

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mutation/save-command/association/classification.mdx)

最后于 **2025年9月16日** 更新