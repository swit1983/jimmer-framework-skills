---
title: 'OnDissociate'
---
# OnDissociate


## 概念


@`org.babyfish.jimmer.sql.OnDissociate`用于和[修改篇/保存指令](mutation/save-command)和[修改篇/删除指令](mutation/delete-command)配合


- [修改篇/保存指令](mutation/save-command)


| 数据库已有数据结构 | 用户期望保存的数据结构 |
| --- | --- |
| +-BookStore(id=2)|+-----Book(id=10)|+-----Book(id=11)|\-----Book(id=12) | +-BookStore(id=2)|+-----Book(id=10)|||||\-----Book(id=9) |


这表示


- `BookStore-2`和`Book-10`之间的关联不变
- `BookStore-2`和`Book-9`之间需要新建关联
- `BookStore-2`需要和`Book-11`、`Book-12`脱勾。
- [修改篇/删除指令](mutation/delete-command)


这很好理解，在删除父对象之前，首先需要对子对象进行脱勾处理。


`@OnDissociate`只能用在基于外键映射的多对一关联上，比如


- Java
- Kotlin
Book.java

```
@Entitypublic interface Book {    @Null    @ManyToOne    @OnDissociate(DissociateAction.SET_NULL)    BookStore store();    ...}
```

Book.kt

```
@Entityinterface Book {    @ManyToOne    @OnDissociate(DissociateAction.SET_NULL)    val store: BookStore?    ...}
```


信息

虽然子对象脱勾是由于一对多关联 *(或逆向inverse一对一)* 导致的 *(即，父对象遗弃某些子对象，本例的一对多关联为`BookStore.books`)*，但是脱勾模式的配置针对逆向的多对一关联 *(本例为`Book.store`)*，这样设计的目的是为了保持和数据库DDL外键的级联特性配置的相似性。


对于Jimmer而言，一对多关联一定是双向关联，知道某个一对多关联，一定知道与其互为镜像的多对一关联。所以，此设计没有任何问题。


## 脱钩模式


上面代码中`OnDissociate`注解的参数，被称为脱钩模式


子对象脱勾操作有5种模式


| 模式 | 描述 |
| --- | --- |
| NONE(默认) | 视全局配置jimmer.default-dissociate-action-checking而定如果jimmer.default-dissociate-action-checking为true(默认)或 当前关联所基于的外键是真的(数据库中存在相应的外键约束，请参见真假外键)，视为CHECK。如果jimmer.default-dissociate-action-checking为false且当前关联所基于的外键是假的(数据库中没有相应的外键约束，请参见真假外键)，视为LAX。 |
| LAX | 脱钩操作不执行任何动作。如果外键是真的(请参见真假外键)，当父对象被删除时如果为数据库中的外键配置了级联删除行为(on cascade set null或on delete delete)，由数据库来自动清空被脱钩的子对象的外键，或自动删除被脱钩的子对象虽然数据库层面的级联修改行为比ORM层面的级联修改行为性能高，但ORM对此毫不知情，在需要缓存一致性的项目中，请慎用否则，数据库会报告错误，保存指令被终止如果外键是假的(请参见真假外键)，当父对象被删除时，不会有任何附加行为发生，放任子对象外键的值出现悬挂问题即使假外键的值是非法的悬挂值，jimmer的查询也不会出错，查询系统会得到父对象为null的结果，而非因父对象不存在而报错 |
| CHECK | 不支持脱钩操作，如果数据库中当前父对象拥有需要脱钩的子对象，则抛出异常阻止操作。 |
| SET_NULL | 把被脱勾的子对象的外键设置为null。使用此模式的前提是子对象的外键关联属性是nullnullable的；否则尝试此配置将会导致异常。 |
| DELETE | 将被脱勾的子对象删除。 |


本文只介绍OnDissociate的配置，至于如何进一步使用，请参见[保存指定/脱钩操作](mutation/save-command/association/dissociation)和[删除指令](mutation/delete-command)。


## 动态覆盖


借助于实体中`OnDissociate`注解的脱钩配置，叫做静态配置。


有的时候，不同的业务可能对脱钩操作有不同的要求，因此，脱钩配置可以在运行时被动态覆盖。


- [修改篇/保存指令](mutation/save-command)


- Java
- Kotlin


```
sqlClient    .getEntities()    .saveCommand(book)    .setDissociateAction(BookProps.STORE, DissociateAction.SET_NULL)    .execute();
```


```
sqlClient.save(book) {    .setDissociateAction(Book::store, DissociateAction.SET_NULL)}
```
- [修改篇/删除指令](mutation/delete-command)


- Java
- Kotlin


```
DeleteResult result = sqlClient    .getEntities()    .deleteCommand(BookStore.class, 1L)    .configure(it ->            it                .setDissociateAction(                    BookProps.STORE,                    DissociateAction.SET_NULL                )    )    .execute();
```


```
val result = sqlClient    .entities    .delete(BookStore::class, 1L) {        setDissociateAction(            Book::store,             DissociateAction.SET_NULL        )    }
```
[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mapping/advanced/on-dissociate.mdx)最后 于 **2025年9月16日**  更新
- [概念](#概念)
- [脱钩模式](#脱钩模式)
- [动态覆盖](#动态覆盖)