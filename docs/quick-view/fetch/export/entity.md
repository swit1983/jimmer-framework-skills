---
title: '直接返回实体'
---
# 直接返回实体


## 启动Web API自动分析


警告

目前，这部分内容支持Spring，以后会支持更多Web框架。


为了导出客户端代码，需要先启用Web API分析能力。开发人员有两种选择


- 使用`@org.babyfish.jimmer.client.EnableImplicitApi`修饰RestController所属工程中的任何一个类。对于Spring Boot应用而言，Application类就是一个不错的选择。
- 使用`@org.babyfish.jimmer.client.Api`修饰所有需要导出的所有`RestController`类以及它们的`HTTP Mapping`方法。


第一种方法相对简单，所以，对Spring Boot Application类应用`@EnableImplicitApi`注解即可。由于代码过于简单，无需演示。


## 编写RestController


作为例子，没有复杂业务，我们忽略Service层，直接基于前文的`BookRepository`编写`BookController`，如下：


- Java
- Kotlin
BookController.java

```
@RestControllerpublic class BookController implements Fetchers {    private final BookRepository bookRepository;    public BookController(BookRepository bookRepository) {        this.bookRepository = bookRepository;    }    @Nullable    @GetMapping("/book/{id}")    public    @FetchBy("COMPLEX_BOOK") Book ❶    findBookById(@PathVariable("id") long id) {        return bookRepository.findBookById(            id,            COMPLEX_BOOK ❷        );    }    @GetMapping("/books")    public List<        @FetchBy("SIMPLE_BOOK") Book ❸    > findBooksByName(            @RequestParam(name = "name", required = false) String name    ) {        return bookRepository.findBooksByName(            name,            SIMPLE_BOOK ❹        );    }    /**     * Simple Book DTO which can only access `id` and `name` of `Book` itself     */    private static final Fetcher<Book> SIMPLE_BOOK = ❺            BOOK_FETCHER                    .name();    /**     * Complex Book DTO which can access not only properties of `Book` itself,     * but also associated `BookStore` and `Author` objects with names     */    private static final Fetcher<Book> COMPLEX_BOOK = ❻            BOOK_FETCHER                    .allScalarFields()                    .store(                            BOOK_STORE_FETCHER.name()                    )                    .authors(                            AUTHOR_FETCHER                                    .firstName()                                    .lastName()                    );}
```

BookController.kt

```
@RestControllerclass BookController(    private val bookRepository: BookRepository) {    @GetMapping("/book/{id}")    fun findBookById(        @PathVariable id: Long    ): @FetchBy("COMPLEX_BOOK") Book = ❶        bookRepository.findBookById(            id,            COMPLEX_BOOK ❷        )    @GetMapping("/books")    fun findBooksByName(            @RequestParam(required = false) name: String    ): List<        @FetchBy("SIMPLE_BOOK") Book ❸    > =        bookRepository.findBooksByName(            name,            SIMPLE_BOOK ❹        )    companion object {        /**         * Simple Book DTO which can only access `id` and `name` of `Book` itself         */        val SIMPLE_BOOK = ❺            newFetcher(Book::class).by {                name()            }        /**         * Complex Book DTO which can access not only properties of `Book` itself,         * but also associated `BookStore` and `Author` objects with names         */        private val COMPLEX_BOOK = ❻            newFetcher(Book::class).by {                allScalarFields()                store {                    name()                }                authors {                    firstName()                    lastName()                }            }    }}
```


Java代码中，`BookController`类实现了Jimmer在编译时自动生成的`Fetchers`接口，只是为了让方便引用`BOOK_FETCHER`、`BOOK_STORE_FETCHER`和`AUTHOR_FETCHER`。


重点在于6个编号


- ❶ 声明`findBookById`方法返回的`Book`对象的精确形状由静态变量`COMPLEX_BOOK`定义
- ❷ `findBookById`方法的内部实现需要与❶处的对外声明一致，查询形状为`COMPLEX_BOOK`的`Book`对象
- ❸ 声明`findBooksByName`方法返回的`List`中的每一个`Book`对象的精确形状由静态变量`SIMPLE_BOOK`定义
- ❹ `findBooksByName`方法的内部实现需要与❸处的对外声明一致，查询形状为`SIMPLE_BOOK`的`Book`对象
- ❺ `SIMPLE_BOOK`形状的定义，既在❸处使用作为对外API声明的一部分，又在❹处使用以控制返回数据结构的形状
- ❻ `COMPLEX_BOOK`形状的定义，既在❶处使用作为对外API声明的一部分，又在❷处使用以控制返回数据结构的形状


## 查看Api文档


为了识别`@FetchBy`这个Jimmer特有的注解，Jimmer对OpenAPI/Swagger给予了一套极具特色的实现。


无需使用JVM生态中任何其他关于自动生成OpenAPI/Swagger的框架，只需修改`application.yml`*(或`application.properties`)*，如下


application.yml

```
jimmer:    ...省略其他配置...    client:        openapi:            path: /openapi.yml            ui-path: /openapi.html            properties:                info:                    title: My Web Service                    description: |                        Restore the DTO explosion that was                        eliminated by server-side developers                    version: 1.0
```


启动Web项目，使用浏览器访问`http://localhost:8080/openapi.html`，则可见


![openapi](https://jimmer.deno.dev/zh/assets/images/openapi-e3db15b0d20a1e447225e55a4983c253.webp)


- 展开`/books`，可以看到返回的集合中，每一个元素都是一个相对简单的DTO对象


![openapi-simple](data:image/webp;base64,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)
- 展开`/books/{id}`，可以看到返回类型是一个相对复杂的DTO类型


![openapi-complex](https://jimmer.deno.dev/zh/assets/images/complex-9425049a58775c669ef7f9c8826e0b2a.webp)


## 生成TypeScript


修改`application.yml`*（或`application.properties`）*，添加对TypeScript的支持


application.yml

```
jimmer:    ...省略其他配置...    client:        openapi:            ...省略openapi相关配置...        ts:            path: /ts.zip
```


启动Web项目，下载`http://localhost:8080/ts.zip`，解压，可以看到TypeScript客户端代码中`BookController`定义如下：


services/BookController.ts

```
import type {Executor} from '../';import type {BookDto} from '../model/dto/';export class BookController {    constructor(private executor: Executor) {}    async findBookById(options: BookControllerOptions['findBookById']): Promise<        BookDto['BookController/COMPLEX_BOOK']    > {        ...省略具体逻辑...    }    async findBooksByName(options: BookControllerOptions['findBooksByName']): Promise<        ReadonlyArray<            BookDto['BookController/SIMPLE_BOOK']        >    > {        ...省略具体逻辑...    }}export type BookControllerOptions = {    'findBookById': {        readonly id: number    },    'findBooksByName': {        readonly name?: string | undefined    }}
```


其中，`BookDto['BookController/COMPLEX_BOOK']`和`BookDto['BookController/SIMPLE_BOOK']`就是Jimmer生成的TypeScript客户端代码中被恢复的DTO类型，可以打开`model/dto/BookDto.ts`文件查看它们的定义，如下：


model/dto/BookDto.ts

```
export type BookDto = {    /**     * Complex Book DTO which can access not only properties of `Book` itself,     * but also associated `BookStore` and `Author` objects with names     */    'BookController/COMPLEX_BOOK': {        readonly id: number;        readonly name: string;        readonly edition: number;        readonly price: number;        readonly store?: {            readonly id: number;            readonly name: string;        } | null | undefined;        readonly authors: ReadonlyArray<{            readonly id: number;            readonly firstName: string;            readonly lastName: string;        }>;    }    /**     * Simple Book DTO which can only access `id` and `name` of `Book` itself     */    'BookController/SIMPLE_BOOK': {        readonly id: number;        readonly name: string;    }}
```


## 文档注释


通过上面的展示，我们看到服务端无需定义DTO相关的Java/Kotlin类型，而客户端却看到每个具体业务API都自动定义精确的DTO返回类型。这样服务端和客户端都得到了各自期望的编程模型。


本文聚焦于演示这个强大功能，没有对如何为Api的各部分*(例如：类型，Api方法，Api参数，对象属性)* 添加文字描述的问题加以讨论。


提示

Jimmer对这类问题的提供了最简单的解决方案，无需使用任何注解，Java/Kotlin开发人员只编写最基本的文档注释即可，所有文档注释就自动复制到客户端Api中。


这个功能很简单，读者可以自行实验，这里不再阐述。


## Flat关联ID


如果关联对象只有`id`属性，那么关联Id会比关联对象更好用，例如


- 使用关联对象，会导致大量的只有id属性的对象，结果稍显冗余


```
{    "id" : 1,    "name" : "Learning GraphQL",    "edition" : 1,    "price" : 50.00,    "store" : {        "id" : 1    },    "authors" : [{        "id" : 1    }, {        "id" : 2    }]}
```
- 使用关联Id，结果相对简练


```
{    "id" : 1,    "name" : "Learning GraphQL",    "edition" : 1,    "price" : 50.00,    "storeId" : 1,    "authorIds" : [1, 2]}
```


如果选择直接返回实体 *(而非[下一篇文章](quick-view/fetch/export/dto)中的返回DTO)*，且想要使用关联id，需先为实体添加@IdView属性


- Java
- Kotlin


```
@Entitypublic interface Book {    @Nullable    @ManyToOne    BookStore store();    @ManyToMany    List<Author> authors();    @Nullable    @IdView    Long storeId();    @IdView("authors")    List<Long> authorIds();    ...省略其他成员...}
```


```
@Entityinterface Book {    @ManyToOne    val store: BookStore?    @ManyToMany    val authors: List<Author>    @IdView    val storeId: Long?    @IdView("authors")    val authorIds: List<Long>    ...省略其他成员...}
```


上例中


- `storeId`属性并非全新属性，它只是`store`属性的视图，获取`store`属性所表示的关联对象的`id`属性 *(或null)*。`storeId`和`store`共享相同的数据。
- `authorIds`属性并非全新属性，它只是`authors`属性的视图，获取`authors`属性所表示的所有关联对象的`id`属性列表。`authorIds`和`authors`共享相同的数据。


现在，如此编写REST Controller即可


- Java
- Kotlin
BookController.java

```
@RestControllerpublic class BookController implements Fetchers {    private final BookRepository bookRepository;    public BookController(BookRepository bookRepository) {        this.bookRepository = bookRepository;    }    @Nullable    @GetMapping("/book")    public @FetchBy("SHALLOW_BOOK") Book findBookById(        @PathVariable("id") long id    ) {        return bookRepository.findBookById(id, SHALLOW_BOOK);    }    /**     * Shallow Book DTO which can access     * 1. All scalar properties of `Book` itself     * 2. All associated ids, not associated objects.     */    private static final Fetcher<Book> SIMPLE_BOOK =            SHALLOW_BOOK                    .allScalarFields()                    .storeId()                    .authorIds();    ...省略其他成员...}
```

BookController.kt

```
@RestControllerclass BookController(    private val bookRepository: BookRepository) {    @GetMapping("/book/{id}")    fun findBookById(        @PathVariable id: Long    ): @FetchBy("SHALLOW_BOOK") Book =        bookRepository.findBookById(id, SHALLOW_BOOK)    ...省略其他成员...    companion object {        /**         * Shallow Book DTO which can access         * 1. All scalar properties of `Book` itself         * 2. All associated ids, not associated objects.         */        val SHALLOW_BOOK =            newFetcher(Book::class).by {                allScalarFields()                storeId()                authorIds()            }        ...省略其他形状定义...    }}
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/fetch/export/entity.mdx)最后 于 **2025年9月16日**  更新
- [启动Web API自动分析](#启动web-api自动分析)
- [编写RestController](#编写restcontroller)
- [查看Api文档](#查看api文档)
- [生成TypeScript](#生成typescript)
- [文档注释](#文档注释)
- [Flat关联ID](#flat关联id)