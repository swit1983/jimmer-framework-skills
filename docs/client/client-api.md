# 生成客户端API

> 来源: https://jimmer.deno.dev/zh/docs/client/api

* [客户端篇](/zh/docs/client/)
* 生成客户端API

本页总览

# 生成客户端API

## 启用客户端能力[​](#启用客户端能力 "启用客户端能力的直接链接")

默认情况下，自动生成客户端的能力是关闭的。要启用这种个功能，有两种选择

1. 采用`@EnableImplicitApi`修饰项目中任何一个类。

   对于Spring Boot应用而言，Application是一个不错的选择。由于过于简单，无需示范。
2. 为每一个Controller和内部的HTTP放方法加上@Api

   * Java
   * Kotlin

   HelloWorldController.java

   ```
   @Api  
   @RestController  
   public class HelloWorldController {  
         
       @Api  
       @GetMapping("/helloworld")  
       public String helloworld() {  
           return "hello world"  
       }  
   }
   ```

   HelloWorldController.kt

   ```
   @Api  
   @RestController  
   class HelloWorldController {  
         
       @Api  
       @GetMapping("/helloworld")  
       fun helloworld() = "hello world"  
   }
   ```

为什么要如此设计呢？让我们来看一个Controller

* Java
* Kotlin

XController.java

```
@RestController  
public class XController {  
  
    @GetMapping("/clientFriendlyData")   
    SomePojo clientFriendlyData() { ❶  
        ...略...  
    }  
  
    @GetMapping("/clientUnfriendlyData")   
    Object clientUnfriendlyData() { ❷  
        ...略...  
    }  
}
```

XController.kt

```
@RestController  
class XController() {  
  
    @GetMapping("/clientFriendlyData")  
    fun clientFriendlyData(): SomePojo = ❶  
        ...略..  
  
    @GetMapping("/clientUnfriendlyData")   
    fun clientUnfriendlyData(): Any = ❷  
        ...略...  
}
```

* ❶ 精确的Api定义，对客户端友好
* ❷ 非常模糊的Api定义，对客户端不友好，甚至可以说是远程API的不良设计。

当然，导致客户端不友好的原因很多，这里只是列举一种最简单的案例。

如果要求Jimmer为客户端不友好Api生成客户端代码，将会导致编译错误。所以，我们需要有选择性地对一部分Api生成客户端代码，而非盲目地处理所有Api。

* 如果大部分Api都是客户端不友好的，只有个别Api才是友好的 *（这种项目处理的大部分信息都非结构化，结构化Api很少）*，建议选择显式地为Controller类和HTTP方法添加@Api注解。由于这种做法已经示范过，不再重复。
* 如果大部分Api都是客户端友好的，只有个别Api才是不友好的，推荐

  1. 先用`@EnableImplicitApi`修饰任何一个类，比如SpringBoot的主类。由于过于简单，不必示范。
  2. 再用`@ApiIgnore`修饰无法支持的类或方法，比如

     + Java
     + Kotlin

     XController.java

     ```
     @RestController  
     public class XController {  
       
         @GetMapping("/clientFriendlyData")   
         SomePojo clientFriendlyData() {  
             ...略...  
         }  
       
         @ApiIgnore  
         @GetMapping("/clientUnfriendlyData")   
         Object clientUnfriendlyData() {  
             ...略...  
         }  
     }
     ```

     XController.kt

     ```
     @RestController  
     class XController() {  
       
         @GetMapping("/clientFriendlyData")  
         fun clientFriendlyData(): SomePojo =   
             ...略..  
       
         @ApiIgnore  
         @GetMapping("/clientUnfriendlyData")   
         fun clientUnfriendlyData(): Any =   
             ...略...  
     }
     ```

提示

`@ApiIgnore`还有另外一个重要作用，比如Spring安全相关的编程中，Java/Kotlin方法经常通过参数注入一些安全上下文相关的东西，比如`javax.security.Principal`类型的参数，这类参数只是spring运行所需，并非Api契约的一部分，可以为这类参数添加`@ApiIgnore`。

## 开发Web服务[​](#开发web服务 "开发Web服务的直接链接")

### 声明@FetchBy[​](#声明fetchby "声明@FetchBy的直接链接")

前面讨论了，使用Jimmer构建REST服务并由服务端罗列客户端所需对象的所有形状是本文要讨论的话题。

要使用这种开发方式，需要在REST API中使用注解`@org.babyfish.jimmer.client.FetchBy`修饰返回类型中的动态实体类型，为客户端标注动态对象的具体形状。

提示

`@FetchBy`并不是简单地修饰REST API的返回值，而是用于修饰类型引用，其声明代码如下

```
package org.babyfish.jimmer.client;  
  
import java.lang.annotation.*;  
  
@Documented  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.TYPE_USE)  
public @interface FetchBy {  
  
    ...略...  
}
```

因此，REST API的返回类型非常灵活，你可以在任何地方 *(包括范型参数)* 使用它修饰Jimmer实体类型，例如

* `@FetchBy("...") Book`
* `List<@FetchBy("...") Book>`
* `Page<@FetchBy("...") Book>`
* `Tuple2<@FetchBy("...") BookStore, @FetchBy("...") Author>`
* `Map<String, Map<String, @FetchBy("...") Book>>`

* Java
* Kotlin

BookController.java

```
@GetMapping("/books")  
public Page<  
    @FetchBy("SIMPLE_BOOK") Book ❶  
> findBookById(  
    @RequestParam(defaultValue = "0") int pageIndex,  
    @RequestParam(defaultValue = "5") int pageSize,  
    @RequestParam(defaultValue = "name asc, edition desc") String sortCode  
) {  
    return bookRepository.findBooks(  
            PageRequest.of(pageIndex, pageSize, SortUtils.toSort(sortCode)),  
            SIMPLE_BOOK ❷  
    );  
}  
  
@GetMapping("book/{id}")  
@Nullable  
public   
    @FetchBy("COMPLEX_BOOK") Book ❸  
findComplexBook(  
        @PathVariable("id") long id  
) {  
    return bookRepository.findNullable(  
        id,   
        COMPLEX_BOOK ❹  
    );  
}  
  
private static final Fetcher<Book> SIMPLE_BOOK = ❺  
    Fetchers.BOOK_BOOK  
        .name();  
  
private static final Fetcher<Book> COMPLEX_BOOK = ❻  
    Fetchers.BOOK_BOOK  
        .allScalarFields()  
        .store(  
            Fetchers.BOOK_STORE_BOOK  
                .name()  
        )  
        .authors(  
            Fetchers.AUTHOR_BOOK  
                .firstName()  
                .lastName()  
        );
```

BookController.kt

```
@GetMapping("/books")  
fun findBooks(  
    @RequestParam(defaultValue = "0") pageIndex: Int,  
    @RequestParam(defaultValue = "5") pageSize: Int,  
    @RequestParam(defaultValue = "name asc, edition desc") sortCode: String  
): Page<  
    @FetchBy("SIMPLE_BOOK") Book ❶  
> =  
    bookRepository.findBooks(  
        PageRequest.of(pageIndex, pageSize, SortUtils.toSort(sortCode)),  
        name,  
        storeName,  
        authorName,  
        SIMPLE_BOOK ❷  
    )  
  
@GetMapping("/book/{id}")  
fun findBookById(  
    @PathVariable id: Long,  
): @FetchBy("COMPLEX_BOOK") Book? = ❸  
    bookRepository.findNullable(  
        id,   
        COMPLEX_BOOK ❹  
    )  
  
companion object {  
  
    private val SIMPLE_BOOK = ❺  
        newFetcher(Book::class).by {  
            name()  
        }  
  
    private val COMPLEX_BOOK = ❻  
        newFetcher(Book::class).by {  
  
            allScalarFields()  
  
            store {  
                name()  
            }  
              
            authors {  
                firstName()  
                lastName()  
            }  
        }  
}
```

* ❶ 对外承诺，`GET /books`返回的分页对象中的每一个`Book`对象的形状为静态常量`SIMPLE_BOOK`所表达的形状
* ❷ 内部实现，`GET /books`内部使用静态常量`SIMPLE_BOOK`查询数据

  警告

  作为对外承诺的❶和作为内部实现的❷必须一致
* ❸ 对外承诺，如果`GET /book/{id}`返回非null, 其形状为静态常量`COMPLEX_BOOK`所表达的形状
* ❹ 内部实现，`GET /book/{id}`内部使用静态常量`COMPLEX_BOOK`查询数据

  警告

  作为对外承诺的❸和作为内部实现的❹必须一致
* ❺和❻，以静态常量的方式声明对象的形状。

通过@FetchBy的修饰，Jimmer就明白每个对象对外返回的数据的具体形状了，它就可以为客户端生成代码了，包括TypeScript。

### @DefaultFetcherOwner[​](#defaultfetcherowner "@DefaultFetcherOwner的直接链接")

在上个例子中，使用注解`@FetchBy`的类和为各种形状声明`Fetcher`类型静态常量的类是同一个类 *(BookController)*。

若非如此，需要为`@FetchBy`注解指定`ownerType`参数，例如

`@FetchBy(value = "COMPLEX_BOOK", ownerType = FetcherConstants.class)`

然而，为每个`@FetchBy`都配置`ownerType`比较繁琐，因此Jimmer支持`@DefaultFetcherOwner`

* Java
* Kotlin

BookController

```
@RestController  
@DefaultFetcherOwner(FetcherConstants.class)  
public class BookController {  
  
    public List<@FetchBy("SIMPLE_BOOK") Book> getSimpleBooks(...略...) {  
        ...略...  
    }  
  
    public List<@FetchBy("DEFAULT_BOOK") Book> getDefaultBooks(...略...) {  
        ...略...  
    }  
  
    @Nullable  
    public @FetchBy("COMPLEX_BOOK") Book findComplexBookById(long id) {  
        ...略...  
    }  
}
```

BookController

```
@RestController  
@DefaultFetcherOwner(FetcherConstants.class)  
class BookController {  
  
    fun getSimpleBooks(...略...): List<@FetchBy("SIMPLE_BOOK") Book> =  
        ...略...  
  
    fun getDefaultBooks(...略...): List<@FetchBy("DEFAULT_BOOK") Book> =  
        ...略...  
  
    fun findComplexBookById(long id): @FetchBy("COMPLEX_BOOK") Book? =  
        ...略...  
}
```

在类级别使用`@DefaultFetcherOwner`可以一次性调整所有`@FetchBy`的`ownerType`属性，不必为每个`@FetchBy`配置`ownerType`了。

## 查看Api文档[​](#查看api文档 "查看Api文档的直接链接")

为了识别`@FetchBy`等Jimmer特有的注解，Jimmer对OpenAPI/Swagger给予了一套极具特色的实现。

无需使用JVM生态中任何其他关于自动生成OpenAPI/Swagger的框架，只需对`application.yml`\*(或`application.properties`)\*进行修改如下即可

application.yml

```
jimmer:  
    ...省略其他配置...  
    client:  
        openapi:  
            path: /openapi.yml  
            ui-path: /openapi.html  
            properties:  
                info:  
                    title: My Web Service  
                    description: |  
                        Restore the DTO explosion that was   
                        eliminated by server-side developers  
                    version: 1.0
```

启动Web项目，使用浏览器访问其`/openapi.html`，则可见

![openapi](/zh/assets/images/openapi-e3db15b0d20a1e447225e55a4983c253.webp)

* 展开`/books`，可以看到返回的集合中，每一个元素都是一个相对简单的DTO对象

  ![openapi-simple](data:image/webp;base64,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)
* 展开`/books/{id}`，可以看到返回类型是一个相对复杂的DTO类型

  ![openapi-complex](/zh/assets/images/complex-9425049a58775c669ef7f9c8826e0b2a.webp)

## 开发Web客户端[​](#开发web客户端 "开发Web客户端的直接链接")

### 生成TypeScript代码[​](#生成typescript代码 "生成TypeScript代码的直接链接")

可以在`application.yml`或`application.properties`中声明如下配置，用于下载相关的客户端代码

```
jimmer:  
    ...省略其他配置...  
    client:  
        ts:  
            path: /ts.zip ❶
```

目前，Jimmer支持生成两种客户端代码，TypeScript和Spring Cloud所需的Java Feign Client代码

* ❶ 可通过 <http://localhost:8080/ts.zip> 下载Web客户端所需的TypeScript代码
* ❷ 可通过 <http://localhost:8080/java-feign.zip> 下载Spring Cloud所需的Java Feign Client代码

接下来，我们讨论TypeScript代码。

启动服务，下载[http://localhost:8080/ts.zip，解压缩。设解压缩后的根目录为`${ts\_root}`](http://localhost:8080/ts.zip%EF%BC%8C%E8%A7%A3%E5%8E%8B%E7%BC%A9%E3%80%82%E8%AE%BE%E8%A7%A3%E5%8E%8B%E7%BC%A9%E5%90%8E%E7%9A%84%E6%A0%B9%E7%9B%AE%E5%BD%95%E4%B8%BA%60$%7Bts_root%7D%60):

让我们先看`${ts_root}/model/dto/BookDto.ts`

BookDto.ts

```
export type BookDto = {  
    'BookController/SIMPLE_BOOK': {  
        readonly id: number,   
        readonly name: string  
    },   
    'BookController/COMPLEX_BOOK': {  
        readonly id: number,   
        readonly name: string,   
        readonly edition: number,   
        readonly price: number,   
        readonly store?: {  
            readonly id: number,   
            readonly name: string  
        },   
        readonly authors: ReadonlyArray<{  
            readonly id: number,   
            readonly firstName: string,   
            readonly lastName: string  
        }>  
    }  
}
```

信息

很明显，在服务端被消灭掉的DTO爆炸，在客户端被恢复了。

让我们再看看`${ts_root}/services/BookService.ts`

```
import type { BookDto } from '../model/dto';  
import type { Page } from '../model/static';  
  
export class BookService {  
      
    async findBooks(  
        options: BookServiceOptions['findBooks']  
    ): Promise<  
        Page<  
            BookDto['BookService/SIMPLE_BOOK']  
        >  
    > {  
        ...省略代码...  
    }  
  
    async findBookById(  
        options: BookServiceOptions['findBookById']  
    ): Promise<  
        BookDto['BookService/COMPLEX_BOOK'] |   
        undefined  
    > {  
        ...省略代码...  
    }  
  
    ...省略其他代码...  
}  
  
export type BookServiceOptions = {  
    'findBooks': {  
        readonly pageIndex: number,   
        readonly pageSize: number,   
        readonly sortCode: string  
    },  
    'findBookById': {  
        readonly id: number  
    }  
}
```

信息

很明显，每个业务场景的返回类型都得到了精确的定义。

### 使用生成的TypeScript代码[​](#使用生成的typescript代码 "使用生成的TypeScript代码的直接链接")

1. 创建React项目

   首先创建一个基于typescript的react项目

   ```
   yarn create react-app my-web-app --template typescript
   ```
2. 自动生成客户端代码

   很显然，不可能在每次服务端发生变化的时候，都要求客户端开发人员都需要手动下载服务端代码，解压，并替换本地代码。

   所以，我们需要编写一个小脚本，自动完成最新TypeScript代码的下载、解压和替换。

   在项目根目录下添加文件夹`scripts`，在其下添加文件`generate-api.js`，该文件由nodejs执行，是开发工具的代码，不是客户端本身的代码。

   scripts/generate-api.js

   ```
   const http = require('http');   
   const fs = require('fs');  
   const fse = require('fs-extra');  
   const uuid = require('uuid');  
   const tempDir = require('temp-dir');  
   const AdmZip = require('adm-zip');  
     
   const sourceUrl = "http://localhost:8080/ts.zip";  
   const tmpFilePath = tempDir + "/" + uuid.v4() + ".zip";  
   const generatePath = "src/__generated";  
     
   console.log("Downloading " + sourceUrl + "...");  
     
   const tmpFile = fs.createWriteStream(tmpFilePath);  
     
   const request = http.get(sourceUrl, (response) => {  
       response.pipe(tmpFile);  
       tmpFile.on("finish", () => {  
           tmpFile.close();  
           console.log("File save success: ", tmpFilePath);  
     
           // Remove generatePath if it exists  
           if (fs.existsSync(generatePath)) {  
               console.log("Removing existing generatePath...");  
               fse.removeSync(generatePath);  
               console.log("Existing generatePath removed.");  
           }  
     
           // Unzip the file using adm-zip  
           console.log("Unzipping the file...");  
           const zip = new AdmZip(tmpFilePath);  
           zip.extractAllTo(generatePath, true);  
           console.log("File unzipped successfully.");  
     
           // Remove the temporary file  
           console.log("Removing temporary file...");  
           fs.unlink(tmpFilePath, (err) => {  
               if (err) {  
                   console.error("Error while removing temporary file:", err);  
               } else {  
                   console.log("Temporary file removed.");  
               }  
           });  
       });  
   });
   ```

   其中，`adm-zip`需要单独安装

   ```
   yarn add adm-zip --dev
   ```

   修改项目的`package.json`，在其"scripts"字段下添加如下代码

   ```
   {  
       ...省略其他代码...  
       "scripts": {  
           ...省略其他代码...  
           "api": "node scripts/generate-api.js"  
       }  
       ...省略其他代码...  
   }
   ```

   这样，每次服务端团队通知REST API发生变化时，都可以简单地执行`yarn api`刷新本地的TypeScript客户端代码

   警告

   这个方法仅仅使用规模很少的前端团队，如果前端对象人数较多，更推荐的做法是对CI环境实施二次开发，实现如下功能：

   每次服务端特定分支代码被提交后，由CI环境编译并启动后端服务，然后，下载ts代码，解压，并提交到git中。最后，前端工程师统一从git拉取最新代码即可。
3.  创建全局API对象

   生成的TypeScript代码中，有一个`__generated/Api.ts`文件，需要用该类实例化一个全局变量并完成必要的配置。

   在`src`下创建`ApiInstance.ts`，定义并导出全部变量`api`

   src/ApiInstance.ts

   ```
   import { Api } from "../__generated";  
     
   const BASE_URL = "http://localhost:8080";  
     
   // 导出全局变量`api`  
   export const api = new Api(async({uri, method, headers, body}) => {  
       const tenant = (window as any).__tenant as string | undefined;  
       const response = await fetch(`${BASE_URL}${uri}`, {  
           method,  
           body: body !== undefined ? JSON.stringify(body) : undefined,  
           headers: {  
               'content-type': 'application/json;charset=UTF-8',  
               ...headers,  
               ...(tenant !== undefined && tenant !== "" ? {tenant} : {})  
           }  
       });  
       if (response.status !== 200) {  
           throw response.json();  
       }  
       const text = await response.text();  
       if (text.length === 0) {  
           return null;  
       }  
       return JSON.parse(text);  
   });
   ```
4. 调用REST API

   现在，我们就可以基于全局变量`api`调用REST API了。

   信息

   下面的例子，基于[use-immer](https://github.com/immerjs/use-immer)和[TanStack/Query](https://github.com/TanStack/query)。

   熟练使用或快熟掌握各种远程请求库，是web前端工程师的基本素养，所以，这里不再细致交代。

   * 体验`api.bookService.findBooks`

     ```
     const [options, setOptions] = useImmer<  
         // RequestOf是Jimmer生成的TypeScript辅助类，  
         // 用于提取任何REST API的参数类型  
         RequestOf<  
             typeof api.bookService.findBooks  
         >  
     >(() => {  
         return {  
             pageIndex: 0,  
             pageSize: 10,  
             sortCode: "name asc"  
         };  
     });  
       
     const {   
         isLoading,   
         // 如果`data`非`undefined`, 则其类型必为  
         // `Page<BookDto['BookService/SIMPLE_BOOK']>`  
         data,   
         error,   
         refetch   
     } = useQuery({  
         queryKey: ["Books", options],  
         // `data`的类型由此决定  
         queryFn: () => api.bookService.findBooks(options)   
     });
     ```

     如果请求成功，`data`的类型为`Page<BookDto['BookService/SIMPLE_BOOK']>`。

     其中, `BookDto['BookService/SIMPLE_BOOK']`的定义为

     ```
     {  
         readonly id: number,   
         readonly name: string,   
         readonly edition: number,  
         readonly price: number,  
     }
     ```
   * 体验`api.bookService.findBookById`

     在下面的代码中，假设`id`为当前React主键的参数

     ```
     const {   
         // 如果`data`非`undefined`, 则其类型必为  
         // `BookDto['BookService/COMPLEX_BOOK']`  
         data,   
         isLoading,   
         error   
     } = useQuery({  
         queryKey: ["book/detail", id],  
         queryFn: () => api.bookService.findBookById({id: id!}),  
         enabled: id !== undefined  
     });
     ```

     如果请求成功且`data`非null, 其类型为`BookDto['BookService/COMPLEX_BOOK']`。该类型定义如下

     ```
     {  
         readonly id: number,   
         readonly name: string,   
         readonly edition: number,   
         readonly price: number,   
         readonly store?: {  
             readonly id: number,   
             readonly name: string,   
             readonly website?: string,   
             readonly avgPrice: number  
         },   
         readonly authors: ReadonlyArray<{  
             readonly id: number,   
             readonly firstName: string,   
             readonly lastName: string,   
             readonly gender: Gender  
         }>  
     }
     ```

   提示

   可见，任何REST API调用都会返回严格的数据类型定义，这些严格的类型定义也会影响`tsx`文件中React UI模板代码。

   这会充分发挥TypeScript的优势，让前端项目具备良好的IDE智能提示，并保证在编译时发现所有问题，具备良好的开发体验。

## 和自定义数据结合[​](#和自定义数据结合 "和自定义数据结合的直接链接")

虽然`@FetchBy`和Jimmer动态实体相结合能够在客户端代码中还原DTO类型定义，但仍然需要认真考虑一种情况：返回的数据类型和底层实体模型差异很大。例如

* Java
* Kotlin

ActiveAuthorInfo.java

```
@lombok.Data  
public class ActiveAuthorInfo {  
  
    private Author raw;  
  
    private List<BookStore> stores;  
}
```

ActiveAuthorInfo.kt

```
data class ActiveAuthorInfo(  
    val raw: Author,  
    val stores: List<BookStore>  
)
```

在这个例子中，`ActiveAuthorInfo`表示活跃度很高的  作者，里面包含了作者的原始信息`raw`，以及所有售卖他/她的书籍的书店的集合。

对应的HTTP服务接口为

* Java
* Kotlin

AuthorController.java

```
@GetMapping("/authors/mostActive")  
public List<ActiveAuthorInfo> findMostActiveAuthorInfos(  
    @RequestParam(defaultValue = "10") int limit  
) {  
    ...略...  
}
```

AuthorController.kt

```
@GetMapping("/authors/mostActive")  
fun findMostActiveAuthorInfos(  
    @RequestParam(defaultValue = "10") limit: Int  
): List<ActiveAuthorInfo> {  
    ...略...  
}
```

很明显，这个数据结构和底层实体模型差异较大。在实体模型中，`BookStore`和`Book`之间存在关联，`Book`和`Author`之间也存在关联，但是，`BookStore`和`Author`之间并不存在关联。

提示

某些情况下，客户端需要的数据可能包含多种实体对象，它们之间并不存在直接的ORM关联，只是特定业务层面的一种的联系。

如果这种特定业务层面的联系毫无通用性，那么为实体定义[复杂计算属性](/zh/docs/mapping/advanced/calculated/transient)也并非一个好的选择。

这时，我们可以打破实体对象图的定式思维，用自定义数据表示返回结果，就如同这里的`ActiveAuthorInfo`

然而，`ActiveAuthorInfo`并不是纯粹的用户自定义数据类型，其内部混合使用了Jimmer实体。我们不妨称之为为混合类型。

可以用`@FetchBy`注解修饰这种混合类型的字段，比如

* Java
* Kotlin

ActiveAuthorInfo.java

```
@lombok.Data  
public class ActiveAuthorInfo {  
  
    private @FetchBy("AUTHOR_BOOK") Author raw;  
  
    private List<@FetchBy("STORE_BOOK") BookStore> stores;  
  
    private static final Fetcher<Author> AUTHOR_BOOK =   
        Fetchers.AUTHOR_BOOK  
            .firstName()  
            .lastName();  
  
    private static final Fetcher<BookStore> STORE_BOOK =   
        Fetchers.AUTHOR_BOOK.name();  
}
```

ActiveAuthorInfo.kt

```
data class ActiveAuthorInfo(  
      
    val raw: @FetchBy("AUTHOR_BOOK") Author,  
  
    val stores: List<@FetchBy("STORE_BOOK") BookStore>  
) {  
    companion object {  
  
        private val AUTHOR_FETCHER =  
            newFetcher(Author::class) {  
                firstName()  
                lastName()  
            }  
  
        private val STORE_BOOK =   
            new Fetcher(BookStore::class) {  
                name()  
            }  
    }  
}
```

最终，`ActiveAuthorInfo`所生成的TypeScript相关类型如下 *(为了方便，这里混合了多个TypeScript文件的代码)*

```
export interface ActiveAuthorInfo {  
  
    readonly raw: AuthorDto['ActiveAuthorInfo/AUTHOR_BOOK'];  
  
    readonly stores: ReadonlyArray<  
        BookStoreDto['ActiveAuthorInfo/STORE_BOOK']  
    >;  
}  
  
export type AuthorDto {  
    'ActiveAuthorInfo/AUTHOR_BOOK': {  
        readonly id: number,  
        readonly firstName: string,  
        readonly lastName: string  
    },  
    ...省略其他DTO类型定义...  
}  
  
export type BookStoreDto {  
    'ActiveAuthorInfo/STORE_BOOK': {  
        readonly id: number,  
        readonly name: string  
    },  
    ...省略其他DTO类型定义...  
}
```

## Api分组[​](#api分组 "Api分组的直接链接")

有两种 方式分组方式

* Controller级别

  + Java
  + Kotlin

  ```
  @Api("pc")  
  @RestController  
  public class BookPcController {...略...}  
    
  @Api("mobile")  
  @RestController  
  public class BookMobileController {...略...}
  ```

  ```
  @Api("pc")  
  @RestController  
  class PcBookController {...略...}  
    
  @Api("mobile")  
  @RestController  
  class MobileBookController {...略...}
  ```
* Api级别

  + Java
  + Kotlin

  BookController.java

  ```
  @RestController  
  public class BookController {  
        
      @Api("pc")  
      @GetMapping("/pc/books")  
      public List<@FetchBy("BOOK_FOR_PC")> findPcBooks(...略...) {  
          ...略...  
      }  
    
      @Api("mobile")  
      @GetMapping("/mobile/books")  
      public List<@FetchBy("BOOK_FOR_MOBILE")> findMobileBooks(...略...) {  
          ...略...  
      }  
  }
  ```

  BookController.kt

  ```
  @RestController  
  class BookController {  
        
      @Api("pc")  
      @GetMapping("/pc/books")  
      fun findBooks(...略...): List<@FetchBy("BOOK_FOR_PC")> =  
          ...略...  
    
      @Api("mobile")  
      @GetMapping("/mobile/books")  
      fun findBooks(...略...): List<@FetchBy("BOOK_FOR_MOBILE")>   
          ...略...  
  }
  ```

提示

@Api可以接受多个组名，比如`@Api({"group1", "group2", "groupN"})`。为了简化问题，上例未示范

警告

类级别和方法级别的分组可以混用，此时，方法级的任何组名都必须是类级组名之一，否则无法编译。

查看/下载客户端的URL如下:

* <http://localhost:8080/openapi.yml>
* <http://localhost:8080/openapi.html>
* <http://localhost:8080/ts.zip>

现在，可以通过如下方法只查看/下载PC相关的客户端

* <http://localhost:8080/openapi.yml?groups=pc>
* <http://localhost:8080/openapi.html?groups=pc>
* <http://localhost:8080/ts.zip?groups=pc>

同理，也可以只查看/下载Mobile相关的客户端

* <http://localhost:8080/openapi.yml?groups=mobile>
* <http://localhost:8080/openapi.html?groups=mobile>
* <http://localhost:8080/ts.zip?groups=mobile>

也可以附加多个参数 *(当然，对于本例而言，这样做和不指定参数效果一样)*

* <http://localhost:8080/openapi.yml?groups=pc,mobile>
* <http://localhost:8080/openapi.html?groups=pc,mobile>
* <http://localhost:8080/ts.zip?groups=pc,mobile>

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/client/api.mdx)

最后于 **2025年9月16日** 更新