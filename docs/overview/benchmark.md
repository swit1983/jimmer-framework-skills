---
title: 'Benchmark报告'
---
# Benchmark报告


**Jimmer不仅可以为你带来强大的功能，还可以为你带来极致的性能。**


基准测试的源代码[在此](https://github.com/babyfish-ct/jimmer/tree/main/benchmark)，使用H2的内存数据库，无需任何环境准备即可直接运行。


## 报告


### 每秒操作次数


- 横坐标表示每次从数据库中查询到的数据对象的数量。
- 纵坐标表示每秒操作次数。


- 图表
- 数据
.css-1h7anqn{display:-webkit-box;display:-webkit-flex;display:-ms-flexbox;display:flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;-webkit-box-flex-wrap:wrap;-webkit-flex-wrap:wrap;-ms-flex-wrap:wrap;flex-wrap:wrap;}.css-1jaw3da{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;cursor:pointer;vertical-align:middle;-webkit-tap-highlight-color:transparent;margin-left:-11px;margin-right:16px;}.css-1jaw3da.Mui-disabled{cursor:default;}.css-1jaw3da .MuiFormControlLabel-label.Mui-disabled{color:rgba(0, 0, 0, 0.38);}.css-f43kvs{color:rgba(0, 0, 0, 0.6);}.css-f43kvs:hover{background-color:rgba(25, 118, 210, 0.04);}@media (hover: none){.css-f43kvs:hover{background-color:transparent;}}.css-f43kvs.Mui-checked,.css-f43kvs.MuiCheckbox-indeterminate{color:#1976d2;}.css-f43kvs.Mui-disabled{color:rgba(0, 0, 0, 0.26);}.css-nnbnb7{padding:9px;border-radius:50%;color:rgba(0, 0, 0, 0.6);}.css-nnbnb7:hover{background-color:rgba(25, 118, 210, 0.04);}@media (hover: none){.css-nnbnb7:hover{background-color:transparent;}}.css-nnbnb7.Mui-checked,.css-nnbnb7.MuiCheckbox-indeterminate{color:#1976d2;}.css-nnbnb7.Mui-disabled{color:rgba(0, 0, 0, 0.26);}.css-zun73v{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;padding:9px;border-radius:50%;color:rgba(0, 0, 0, 0.6);}.css-zun73v::-moz-focus-inner{border-style:none;}.css-zun73v.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-zun73v{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-zun73v:hover{background-color:rgba(25, 118, 210, 0.04);}@media (hover: none){.css-zun73v:hover{background-color:transparent;}}.css-zun73v.Mui-checked,.css-zun73v.MuiCheckbox-indeterminate{color:#1976d2;}.css-zun73v.Mui-disabled{color:rgba(0, 0, 0, 0.26);}.css-1m9pwf3{cursor:inherit;position:absolute;opacity:0;width:100%;height:100%;top:0;left:0;margin:0;padding:0;z-index:1;}.css-vubbuv{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;width:1em;height:1em;display:inline-block;fill:currentColor;-webkit-flex-shrink:0;-ms-flex-negative:0;flex-shrink:0;-webkit-transition:fill 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:fill 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;font-size:1.5rem;}.css-9l3uo3{margin:0;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.5;letter-spacing:0.00938em;}显示原生JDBC指标.css-39bbo6{margin:0;-webkit-flex-shrink:0;-ms-flex-negative:0;flex-shrink:0;border-width:0;border-style:solid;border-color:rgba(0, 0, 0, 0.12);border-bottom-width:thin;}.css-1owb465{display:table;width:100%;border-collapse:collapse;border-spacing:0;}.css-1owb465 caption{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:0.875rem;line-height:1.43;letter-spacing:0.01071em;padding:16px;color:rgba(0, 0, 0, 0.6);text-align:left;caption-side:bottom;}

| Framework | Data count | Ops/s |
| --- | --- | --- |
| JDBC(ColIndex) | 10 | 662071 |
| JDBC(ColName) | 10 | 345169 |
| Jimmer(Java) | 10 | 315312 |
| Jimmer(Kotlin) | 10 | 309029 |
| EasyQuery | 10 | 206634 |
| MyBatis | 10 | 68743 |
| Exposed | 10 | 92494 |
| JPA(Hibernate) | 10 | 90542 |
| JPA(EclipseLink) | 10 | 64230 |
| JOOQ | 10 | 69408 |
| Nutz | 10 | 76904 |
| ObjectiveSQL | 10 | 56245 |
| Spring Data JDBC | 10 | 20494 |
| Ktorm | 10 | 19152 |
| JDBC(ColIndex) | 20 | 453226 |
| JDBC(ColName) | 20 | 238107 |
| Jimmer(Java) | 20 | 224179 |
| Jimmer(Kotlin) | 20 | 230622 |
| EasyQuery | 20 | 135063 |
| MyBatis | 20 | 43940 |
| Exposed | 20 | 66934 |
| JPA(Hibernate) | 20 | 59926 |
| JPA(EclipseLink) | 20 | 33421 |
| JOOQ | 20 | 39737 |
| Nutz | 20 | 39501 |
| ObjectiveSQL | 20 | 29975 |
| Spring Data JDBC | 20 | 10704 |
| Ktorm | 20 | 9520 |
| JDBC(ColIndex) | 50 | 259484 |
| JDBC(ColName) | 50 | 125589 |
| Jimmer(Java) | 50 | 132673 |
| Jimmer(Kotlin) | 50 | 128315 |
| EasyQuery | 50 | 92230 |
| MyBatis | 50 | 20050 |
| Exposed | 50 | 26693 |
| JPA(Hibernate) | 50 | 25893 |
| JPA(EclipseLink) | 50 | 13634 |
| JOOQ | 50 | 18373 |
| Nutz | 50 | 16639 |
| ObjectiveSQL | 50 | 12455 |
| Spring Data JDBC | 50 | 4146 |
| Ktorm | 50 | 4385 |
| JDBC(ColIndex) | 100 | 130991 |
| JDBC(ColName) | 100 | 72424 |
| Jimmer(Java) | 100 | 77044 |
| Jimmer(Kotlin) | 100 | 74499 |
| EasyQuery | 100 | 45502 |
| MyBatis | 100 | 10541 |
| Exposed | 100 | 19483 |
| JPA(Hibernate) | 100 | 13096 |
| JPA(EclipseLink) | 100 | 6802 |
| JOOQ | 100 | 8145 |
| Nutz | 100 | 8903 |
| ObjectiveSQL | 100 | 6251 |
| Spring Data JDBC | 100 | 2229 |
| Ktorm | 100 | 2091 |
| JDBC(ColIndex) | 200 | 77725 |
| JDBC(ColName) | 200 | 33068 |
| Jimmer(Java) | 200 | 41474 |
| Jimmer(Kotlin) | 200 | 36656 |
| EasyQuery | 200 | 28085 |
| MyBatis | 200 | 5310 |
| Exposed | 200 | 10008 |
| JPA(Hibernate) | 200 | 6900 |
| JPA(EclipseLink) | 200 | 3238 |
| JOOQ | 200 | 4186 |
| Nutz | 200 | 4374 |
| ObjectiveSQL | 200 | 3470 |
| Spring Data JDBC | 200 | 1025 |
| Ktorm | 200 | 1063 |
| JDBC(ColIndex) | 500 | 32109 |
| JDBC(ColName) | 500 | 16234 |
| Jimmer(Java) | 500 | 16371 |
| Jimmer(Kotlin) | 500 | 16870 |
| EasyQuery | 500 | 11666 |
| MyBatis | 500 | 2137 |
| Exposed | 500 | 3894 |
| JPA(Hibernate) | 500 | 2491 |
| JPA(EclipseLink) | 500 | 1361 |
| JOOQ | 500 | 1707 |
| Nutz | 500 | 2040 |
| ObjectiveSQL | 500 | 1259 |
| Spring Data JDBC | 500 | 447 |
| Ktorm | 500 | 427 |
| JDBC(ColIndex) | 1000 | 16188 |
| JDBC(ColName) | 1000 | 8346 |
| Jimmer(Java) | 1000 | 8831 |
| Jimmer(Kotlin) | 1000 | 7932 |
| EasyQuery | 1000 | 4427 |
| MyBatis | 1000 | 1048 |
| Exposed | 1000 | 2082 |
| JPA(Hibernate) | 1000 | 1207 |
| JPA(EclipseLink) | 1000 | 650 |
| JOOQ | 1000 | 832 |
| Nutz | 1000 | 957 |
| ObjectiveSQL | 1000 | 559 |
| Spring Data JDBC | 1000 | 197 |
| Ktorm | 1000 | 168 |


### 每次操作耗时


- 横坐标表示每次从数据库中查询到的数据对象的数量。
- 纵坐标表示每次操作耗时(微秒)。


- 图表
- 数据
.css-1owb465{display:table;width:100%;border-collapse:collapse;border-spacing:0;}.css-1owb465 caption{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:0.875rem;line-height:1.43;letter-spacing:0.01071em;padding:16px;color:rgba(0, 0, 0, 0.6);text-align:left;caption-side:bottom;}

| Framework | Data count | Time(μs) |
| --- | --- | --- |
| JDBC(ColIndex) | 10 | 2 |
| JDBC(ColName) | 10 | 3 |
| Jimmer(Java) | 10 | 3 |
| Jimmer(Kotlin) | 10 | 3 |
| EasyQuery | 10 | 5 |
| MyBatis | 10 | 15 |
| Exposed | 10 | 11 |
| JPA(Hibernate) | 10 | 11 |
| JPA(EclipseLink) | 10 | 16 |
| JOOQ | 10 | 14 |
| Nutz | 10 | 13 |
| ObjectiveSQL | 10 | 18 |
| Spring Data JDBC | 10 | 49 |
| Ktorm | 10 | 52 |
| JDBC(ColIndex) | 20 | 2 |
| JDBC(ColName) | 20 | 4 |
| Jimmer(Java) | 20 | 4 |
| Jimmer(Kotlin) | 20 | 4 |
| EasyQuery | 20 | 7 |
| MyBatis | 20 | 23 |
| Exposed | 20 | 15 |
| JPA(Hibernate) | 20 | 17 |
| JPA(EclipseLink) | 20 | 30 |
| JOOQ | 20 | 25 |
| Nutz | 20 | 25 |
| ObjectiveSQL | 20 | 33 |
| Spring Data JDBC | 20 | 93 |
| Ktorm | 20 | 105 |
| JDBC(ColIndex) | 50 | 4 |
| JDBC(ColName) | 50 | 8 |
| Jimmer(Java) | 50 | 8 |
| Jimmer(Kotlin) | 50 | 8 |
| EasyQuery | 50 | 11 |
| MyBatis | 50 | 50 |
| Exposed | 50 | 37 |
| JPA(Hibernate) | 50 | 39 |
| JPA(EclipseLink) | 50 | 73 |
| JOOQ | 50 | 54 |
| Nutz | 50 | 60 |
| ObjectiveSQL | 50 | 80 |
| Spring Data JDBC | 50 | 241 |
| Ktorm | 50 | 228 |
| JDBC(ColIndex) | 100 | 8 |
| JDBC(ColName) | 100 | 14 |
| Jimmer(Java) | 100 | 13 |
| Jimmer(Kotlin) | 100 | 13 |
| EasyQuery | 100 | 22 |
| MyBatis | 100 | 95 |
| Exposed | 100 | 51 |
| JPA(Hibernate) | 100 | 76 |
| JPA(EclipseLink) | 100 | 147 |
| JOOQ | 100 | 123 |
| Nutz | 100 | 112 |
| ObjectiveSQL | 100 | 160 |
| Spring Data JDBC | 100 | 449 |
| Ktorm | 100 | 478 |
| JDBC(ColIndex) | 200 | 13 |
| JDBC(ColName) | 200 | 30 |
| Jimmer(Java) | 200 | 24 |
| Jimmer(Kotlin) | 200 | 27 |
| EasyQuery | 200 | 36 |
| MyBatis | 200 | 188 |
| Exposed | 200 | 100 |
| JPA(Hibernate) | 200 | 145 |
| JPA(EclipseLink) | 200 | 309 |
| JOOQ | 200 | 239 |
| Nutz | 200 | 229 |
| ObjectiveSQL | 200 | 288 |
| Spring Data JDBC | 200 | 976 |
| Ktorm | 200 | 941 |
| JDBC(ColIndex) | 500 | 31 |
| JDBC(ColName) | 500 | 62 |
| Jimmer(Java) | 500 | 61 |
| Jimmer(Kotlin) | 500 | 59 |
| EasyQuery | 500 | 86 |
| MyBatis | 500 | 468 |
| Exposed | 500 | 257 |
| JPA(Hibernate) | 500 | 401 |
| JPA(EclipseLink) | 500 | 735 |
| JOOQ | 500 | 586 |
| Nutz | 500 | 490 |
| ObjectiveSQL | 500 | 794 |
| Spring Data JDBC | 500 | 2237 |
| Ktorm | 500 | 2342 |
| JDBC(ColIndex) | 1000 | 62 |
| JDBC(ColName) | 1000 | 120 |
| Jimmer(Java) | 1000 | 113 |
| Jimmer(Kotlin) | 1000 | 126 |
| EasyQuery | 1000 | 226 |
| MyBatis | 1000 | 954 |
| Exposed | 1000 | 480 |
| JPA(Hibernate) | 1000 | 829 |
| JPA(EclipseLink) | 1000 | 1538 |
| JOOQ | 1000 | 1202 |
| Nutz | 1000 | 1045 |
| ObjectiveSQL | 1000 | 1789 |
| Spring Data JDBC | 1000 | 5076 |
| Ktorm | 1000 | 5952 |


备注

由于Spring移除了对OpenJPA的支持，本基准测试不包含`JPA(OpenJPA)`


## 实现原则


1. 所有框架禁用缓存
2. 所有框架关闭日志
3. 所有框架每次都打开和关闭连接/会话，不做共享；靠连接池保证性能。
4. 接入Spring的连接管理机制。因不同框架API不同，实现方法略有不同。


- 有的使用[DataSourceUtils](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/DataSourceUtils.html)的getConnection和releaseConnection
- 有的使用[TransactionAwareDataSourceProxy](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jdbc/datasource/TransactionAwareDataSourceProxy.html)


但最终效果都一样
5. 不使用事务


`Exposed`比较特殊，其API强制要求事务，给予假的实现。
6. 使用嵌入的H2内数据库，尽可能压缩数据库的消耗，凸显ORM本身的性能，即映射的性能。


## 价值


一种常见的观点：ORM本身的性能不重要，实际项目中，数据库并非有内嵌内存数据库，所以ORM本身耗时相对于数据库耗时可忽略不计。


反驳：Java19发布后，支持虚拟线程。ORM能尽快完成映射，可以让JVM 去调度更多的虚拟线程，可以提高系统的吞吐量。


## 为什么如此快?


JDBC有两种编程风格用于从`java.sql.ResultSet`中读取值


- `JDBC(ColIndex)`，即按照列索引读取，`rs.getString(1)`
- `JDBC(ColName)`，按照列名称读取：`rs.getString("VALUE_1")`


`JDBC(ColIndex)`比`JDBC(ColName)`性能高，因此，Jimmer本身也将`JDBC(ColIndex)`作为底层的访问`ResultSet`的手段，为性能指标超越`JDBC(ColName)`提供了可能性。


现在要我们重点讨论图表 *(选中`JDBC原生JDBC指标`)* 中的两个案例


- 每次查询10条数据，`JDBC(ColName)`和`Jimmer(Java)`的OPS接近。


这种场景下，Jimmer和`JDBC(ColName)`性能相似。`JDBC(ColName)`的测试代码直接硬编码SQL，而Jimmer采用DSL机制需要动态构建SQL，从这个角度讲Jimmer应该更慢。然而Jimmer把`ResultSet`转化为对象的的过程比`JDBC(ColName)`快，二者抵消。所以性能相近。


当然Benchmark中采用的SQL语句不复杂，如果换用更复杂的SQL，直接硬编码SQL的`JDBC(ColName)`会更快，这也是Jimmer后续版本可以优化的空间。
- 每次查询1000条数据，Jimmer的ops明显高于`JDBC(ColName)`的OPS。


在这种情况下，Jimmer把`ResultSet`转化为对象的性能优势得到了充分体现，即便Jimmer使用DSL临时生产SQL拖慢了速度，但整体结果仍然比`JDBC(ColName)`快。


Jimmer把`ResultSet`转化为对象的  性能非高，主要是因为如下两个原因


- 底层使用`JDBC(ColIndex)`
- 不使用Java的反射机制为对象动态设置各属性。


在编译时，Jimmer为每一个不可变的实体类型生称一个可修改的`DraftImpl`类，提供了一个通用的`__set(PropId propId, Object value)`方法完成和Java反射类似的动态设置对象属性的功能。


同时，与编译器每个属性分配一个整数作为id，`DraftImpl`类的的`__set`方法内部使用`switch`语句实现了对传入的属性id进行鉴别。以生成的Java代码为例


```
@Overridepublic void __set(PropId prop, Object value) {    int __propIndex = prop.asIndex();    switch (__propIndex) {        case -1:            __set(prop.asName(), value);            return;        case 0:            setId((Long)value);            break;        case 1:            setName((String)value);            break;        case 2:            setEdition((Integer)value);            break;        ...略...    }
```


现代编译器对这种基于整数的`switch`语句有充分优化，可以认为Jimmer对实体对象的动态赋值不会比JDBC测试代码中的硬编码慢多少。


以上两个原因，是Jimmer的对象映射性能如此高的最重要的原因。
[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/overview/benchmark.mdx)最后 于 **2025年9月16日**  更新
- [报告](#报告)
- [每秒操作次数](#每秒操作次数)
- [每次操作耗时](#每次操作耗时)
- [实现原则](#实现原则)
- [价值](#价值)
- [为什么如此快?](#为什么如此快)