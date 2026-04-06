# 中间表

> 来源: https://jimmer.deno.dev/zh/docs/mapping/advanced/logical-deleted/join-table

- [映射篇](/zh/docs/mapping/)
- [进阶映射](/zh/docs/mapping/advanced/)
- [逻辑删除](/zh/docs/mapping/advanced/logical-deleted/)
- 中间表

本页总览

# 中间表

想让中间表支持逻辑删除，需要为注解`org.babyfish.jimmer.sql.JoinTable`指定属性，以表示该数据是正常的还是已经被删除。

- 一旦为中间表配置了逻辑删除属性，当任何一端的实体被逻辑删除时，所有相关的中间表记录将会被逻辑删除。
- 所有针对当前关联的JOIN操作都会被自动加上 `and 软删除标志 <> 已经被删除`的条件，从而营造出某些关联已经被删除的假象。

## 用法[​](#用法 "用法的直接链接")

逻辑删除标志属性可以是如下类型之一

- boolean：必须非null
- int：必须非null
- 枚举：必须非null
- long/Long：null或非null即可
- UUID: 必须非null
- 日期：必须可null

| 类型                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | 代码                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | 删除状态    | 初始状态        |
|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|-------------|
| ---                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       | ---                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | ---     | ---         |
| boolean                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "DELETED",             type = boolean.class,             value = "true"         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "DELETED",             type = boolean.class,             value = "true"         ) ) val authors: List<Author> ```                                                                                           | true    | false       |
| * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "ACTIVE",             type = boolean.class,             value = "false"         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "ACTIVE",             type = boolean.class,             value = "false"         ) ) val authors: List<Author> ```                                                                                 | false                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | true    |             |
| int                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       | * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "STATE",             type = int.class,             value = "1",             initializedValue = "0"         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "STATE",             type = int.class,             value = "1",             initializedValue = "0"         ) ) val authors: List<Author> ```                                     | 1       | 0           |
| 枚举                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "STATE",             type = State.class,             value = "DELETED",             initializedValue = "INITIALIZED"         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "STATE",             type = State.class,             value = "DELETED",             initializedValue = "INITIALIZED"         ) ) val authors: List<Author> ``` | DELETED | INITIALIZED |
| ✩  long                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "DELETED_MILLIS",             type = long.class         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "DELETED_MILLIS",             type = long.class         ) ) val authors: List<Author> ```                                                                                                                                           | 当前时钟毫秒数 | 0L          |
| ✩  Nullable Long                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "DELETED_MILLIS",             type = Long.class,             nullable = true         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "DELETED_MILLIS",             type = Long.class,             nullable = true         ) ) val authors: List<Author> ```                                                                                 | 当前时钟毫秒数 | null        |
| ✩  UUID                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "DELETED_DATA",             type = UUID.class         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "DELETED_DATA",             type = UUID.class         ) ) val authors: List<Author> ```                                                                                                                                              | 随机UUID  | 所有字节为0的UUID |
| ✩  Nullable UUID                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "DELETED_DATA",             type = UUID.class,             nullable = true         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "DELETED_DATA",             type = UUID.class,             nullable = true         ) ) val authors: List<Author> ```                                                                                     | 随机UUID  | null        |
| Nullable LocalDateTime                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | ✩  * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "DELETED_TIME",             type = LocalDateTime.class,             nullable = true,             value = "now"         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "DELETED_TIME",             type = LocalDateTime.class,             nullable = true,             value = "now"         ) ) val authors: List<Author> ```          | 当前时间    | null        |
| * Java * Kotlin   ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         @JoinTable.LogicalDeletedFilter(             columnName = "CREATED_TIME",             type = LocalDateTime.class,             nullable = true,             value = "null"         ) ) List<Author> authors(); ```  ``` @ManyToMany @JoinTable(     ...省略其他属性...,     logicalDeletedFilter =         JoinTable.LogicalDeletedFilter(             columnName = "CREATED_TIME",             type = LocalDateTime.class,             nullable = true,             value = "null"         ) ) val authors: List<Author> ``` | null                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | 当前时间    |             |

其中

- 第一列或第二列中的 ✩ 表示当前配置方法支持下一节即将讨论的多版本数据。

  信息

  支持逻辑删除却不考虑多版本数据的问题，是一种成熟度不高的考虑，因此建议使用支持多版本的逻辑删除配置。
- `当前时钟毫秒数`，默认行为是`System.currentMillis()`，即是默认的`org.babyfish.jimmer.sql.meta.LogicalDeletedLongGenerator`的行为。

  如果对此行为不满意，可以自定义类实现`LogicalDeletedValueGenerator<Long>`接口，并用如下配置

  - `@JoinTable.LogicalDeletedFilter(generatedType = YourGenerator.class)`
  - `@JoinTable.LogicalDeletedFilter(generatedRef = YourGenerator.class)`，这里`generatorRef`表示对象在IOC容器管理框架中的名称
- `随机UUD`，默认行为是`UUID.randomUUID()`，即是默认的`org.babyfish.jimmer.sql.meta.LogicalDeletedUUIDGenerator`的行为。

  如果对此行为不满意，可以自定义类实现`LogicalDeletedValueGenerator<UUID>`接口，并用如下配置

  - `@JoinTable.LogicalDeletedFilter(generatedType = YourGenerator.class)`
  - `@JoinTable.LogicalDeletedFilter(generatedRef = YourGenerator.class)`，这里`generatorRef`表示对象在IOC容器管理框架中的名称

## 多版本数据[​](#多版本数据 "多版本数据的直接链接")

逻辑删除并不会导致数据被真正删除，只会导致数据被隐藏，这代表着数据出现多版本问题。

以支持多版本数据的配置

- Java
- Kotlin

```@manytomany
@JoinTable(
    name = "BOOK_AUTHOR_MAPPING",
    joinColumnName = "BOOK_ID",
    inverseJoinColumnName = "AUTHOR_ID",
    logicalDeletedFilter =
        @JoinTable.LogicalDeletedFilter(
            columnName = "DELETED_MILLIS",
            type = long.class
        )
)
List<Author> authors();

```

```@manytomany
@JoinTable(
    name = "BOOK_AUTHOR_MAPPING",
    joinColumnName = "BOOK_ID",
| inverseJoinColumnName = "AUTHOR_ID", |
|--------------------------------------|
    logicalDeletedFilter =
        @JoinTable.LogicalDeletedFilter(
            columnName = "DELETED_MILLIS",
            type = long.class
        )
)
val authors: List<Author>

```

为例，`BOOK_AUTHOR_MAPPING`表有三列，全部作为主键的组成部分

```alter table book_author_mapping
    add pk_BOOK_AUTHOR_MAPPING
        primary key(
            BOOK_ID,
            AUTHOR_ID,
            DELETED_MILLIS
        );

```

假如表格输入如下

| BOOK\_ID | AUTHOR\_ID | DELETED\_MILLIS |
|----------|------------|-----------------|
| ---      | ---        | ---             |
| 97       | 23         | 0               |
| 97       | 23         | 1708796420956   |
| 97       | 23         | 1708234681901   |
| 249      | 11         | 0               |
| 249      | 11         | 1708722582793   |
| 249      | 11         | 1708664484823   |

其中有4条关联数据被隐藏，有效数据只有两条

| BOOK\_ID | AUTHOR\_ID | DELETED\_MILLIS |
|----------|------------|-----------------|
| ---      | ---        | ---             |
| 97       | 23         | 0               |
| 249      | 11         | 0               |

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mapping/advanced/logical-deleted/join-table.mdx)

最后于 **2025年9月16日** 更新