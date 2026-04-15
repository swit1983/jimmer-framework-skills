---
title: '使用DTO语言'
---
# 使用DTO语言


Jimmer提供了[DTO语言](object/view/dto-language)。


开发人员可以使用此语言快速定义要保存的Input DTO的形状，编译后


- 相关的Java/Kotlin Input DTO类将会被自动生成
- Input DTO和实体之间的彼此转化逻辑会被自动生成
- 与之形状吻合的对象抓取器会被自动生成。*(这个功能和input DTO无关，所以本文不做论述)*


开发人员用自动生成的Input DTO作为API的入参，一行代码保存它即可 *(内部逻辑：调用自动生成的转化逻辑把Input DTO转化为动态实体对象，再用保存指令直接保存)*。


信息

这是消除Input DTO爆炸带来的痛苦的最高效方案


## 定义DTO的形状


本文侧重于讲解如何保存静态DTO类型，并非系统性介绍DTO语言，请参考[对象篇/DTO转换/DTO语言](object/view/dto-language)以了解完整的DTO语言。


假如`Book`类的全名为`com.yourcompany.yourproject.model.Book`，你可以


1. **在实体定义所在项目中**，建立目录`src/main/dto`
2. 在`src/main/dto`下，按实体类型所处的包路径建立子目录`com/yourcompany/yourproject/model`
3. 在上一步建立的目录下，建立文件`Book.dto`，文件必须和实体类同名，扩展名必须为`dto`
4. 编辑此文件，利用DTO语言，定义Book实体的各种DTO形状


Book.dto

```
input BookInput {        #allScalars(Book)        id(store)    authors {        #allScalars(Author)        -id    }}input SimpleBookInput { ...略... }...省略其他Input DTO形状定义...
```


信息

用作输入参数的Input DTO的形状定义必须用`input`修饰符修饰。


具体原因已经在[对象篇/DTO转换/DTO语言](object/view/dto-language)中有详细的阐述，本文不再赘述


## 自动生成DTO类型


Jimmer负责编译dto文件，自动生成符合这些形状的DTO类型。


警告

如果除了dto文件外还有其他Java/Kotlin原代码文件被修改了，直接点击IDE中运行按钮可以导致dto文件的重新编译


但是，如果除了dto文件外没有其他Java/Kotlin文件被修改，简单地点击IDE中运行按钮并不会导致dto文件被重新编译，除非显式地rebuild！


如果你使用的构建工具是Gradle，也可以使用社区提供的第三方Gradle插件来解决这个问题: [jimmer-gradle](https://github.com/Enaium/jimmer-gradle)


以上面代码中的`BookInput`为例，此dto文件被Jimmer成功编译后，会自动生成如下DTO类型


- Java
- Kotlin
BookInput.java

```
package com.yourcompany.yourproject.model.dto;import com.yourcompany.yourproject.model.Book;import org.babyfish.jimmer.Input;@GeneratedBy(file = "<your_project>/src/main/dto/Book.dto")public class BookInput implements Input<Book> {    @Nullable    private Long id; ❶    private String name;    private int edition;    private BigDecimal price;    @Nullable    private Long storeId; ❷    private List<TargetOf_authors> authors; ❸    public BookInput(Book book) { ❹        ...略...    }    @Override    public Book toEntity() { ❺        ...略...    }    @Data    public static class TargetOf_authors {        private String firstName;                private String lastName;                private Gender gender;        ...省略其他成员...    }    ...省略其他成员...}
```

BookInput.kt

```
package com.yourcompany.yourproject.model.dtoimport com.yourcompany.yourproject.model.Bookimport org.babyfish.jimmer.Input@GeneratedBy(file = "<your_project>/src/main/dto/Book.dto")data class BookInput(    val id: Long? = null, ❶    val name: String = "",     val edition: Int = 0,    val price: BigDecimal,     val storeId: Long? = null, ❷    val authors: List<TargetOf_authors> = emptyList() ❸): Input<Book> {    constructor(book: Book) : this(...略...) ❹    override fun toEntity(): Book = ...略... ❺    data class TargetOf_authors(        val firstName: String,        val lastName: String,        val gender: Gender    ) {        ...省略其他成员...    }    ...省略其他成员...}
```


- ❶ 如果id被指定了自动生成策略，则id不是必须的。这也是保存指令的一个特性，具体细节请参考[保存模式](mutation/save-command/save-mode)


信息
- 对于Jimmer实体而言，`id`不可能为null，靠id属性的缺失 *(即，不赋值)* 来表达对象没有id的情况。
- 对于Input DTO而言，静态的POJO类型没有属性缺失的概念，靠null来表达没有id的情况。


二者看似矛盾，但其实可以简单地处理：如果Input DTO的id为null，转化后的实体对象无id *(虽然id不允许被赋为null，但动态对象可以不对属性赋值)*
- ❷ 明确指定此InputDTO想以

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}短关联@media print{.css-1k371a6{position:absolute!important;}}的方式编辑实体的多对一关联`Book.store`。其中，
- ❸ 明确指定此InputDTO想以

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}长关联@media print{.css-1k371a6{position:absolute!important;}}的方式编辑实体的多对过关联`Book.authors`，
关联对象的类型也被内嵌的InputDTO类型`BookInput.TargetOf_authors`固化。
- ❹ 将动态实体转化为静态InputDTO
- ❺ 将静态InputDTO转化为动态实体


## HTTP API


DTO语言生成了相对完备的代码，所以，我们可以快速对外暴露保存数据的HTTP API


- Java
- Kotlin


```
@PutMapping("/book")public void saveBook(    @RequestBody BookInput input) {    // `save(input)`等价于`save(input.toEntity())`    bookRepository.save(input);}
```


```
@PutMapping("/book")fun saveBook(    @RequestBody input: BookInput) {    // `save(input)`等价于`save(input.toEntity())`    bookRepository.save(input)}
```


## 最佳实践


在实际项目中，常常面临一个实际的问题，实体的属性可能非常多，而且


- 插入时需要指定的属性相对较多
- 修改时需要指定的属性相对较少


我们一致用作例子的`Book`等实体属性很少，不方便演示，因此，我虚构一个属性较多的实体类型：`Product`。


- Java
- Kotlin
Product.java

```
@Entitypublic interface Product {     ...省略实体属性...}
```

Product.kt

```
@Entityinterface Product {     ...省略实体属性...}
```


- 针对插入时需要指定的属性相对较多的情况，定义`CreateProductInput`
- 针对修改时需要指定的属性相对较少的情况，定义`UpdateProductInput`


所以，`Product.dto`的代码看起来应该如下


```
input CreateProductInput {    ...较多属性，略...}input UpdateProductInput {    ...较少属性，略...}
```


编译后，将会自动生成Java/Kotlin类`CreateProductInput`和`UpdateProductInput`。所以，我们可以快速实现如下两个HTTP API


- Java
- Kotlin


```
@PostMapping("/product")public void createProduct(    // `CreateProductInput`属性相对多    @RequestBody CreateProductInput input) {    productRepository.insert(input);}@PutMapping("/product")public void updateProduct(    // `UpdateProductInput`属性相对少    @RequestBody UpdateProductInput input) {    productRepository.update(input);}
```


```
@PostMapping("/product")fun createProduct(    // `CreateProductInput`属性相对多    @RequestBody input: CreateProductInput) {    productRepository.insert(input)}@PutMapping("/product")fun updateProduct(    // `UpdateProductInput`属性相对少    @RequestBody input: UpdateProductInput) {    productRepository.update(input)}
```


提示

由此可见，无论项目的业务特色决定需要为同一实体定义多少了不同的`Input DTO`类型。最终都是利用DTO语言快速生成Input DTO类型和相关数据转化逻辑，然后用一行代码调用保存指令即可。


哪怕项目的业务更复杂一些，比如不同身份的人可以编辑的  数据结构的形状不同，也可以不断套用此模式轻松应对。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mutation/save-command/input-dto/dto-lang.mdx)最后 于 **2025年9月16日**  更新
- [定义DTO的形状](#定义dto的形状)
- [自动生成DTO类型](#自动生成dto类型)
- [HTTP API](#http-api)
- [最佳实践](#最佳实践)