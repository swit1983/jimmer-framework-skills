# DTO语言

> 来源: https://jimmer.deno.dev/zh/docs/object/view/dto-language

* [对象篇](/zh/docs/object/)
* [DTO转换](/zh/docs/object/view/)
* DTO语言

本页总览

# DTO语言

## 1. 概念[​](#1-概念 "1. 概念的直接链接")

### 1.1. 痛点[​](#11-痛点 "1.1. 痛点的直接链接")

Jimmer提供动态实体，可以很好地解决很大一部分DTO爆炸问题。所以，一般情况下不需要定义输出型的DTO类型来表达查询返回结果。

然而，并非所有DTO类型都可以被消灭，其中，输入型的DTO对象很难去除。

> 以GraphQL为例，虽然从output的角度讲，为客户端返回动态的`GraphQLObject`数据；但是，从input的角度讲，接受客户端提交的静态的`GraphQLInput`数据。
>
> GraphQL协议为什么将`GraphQLInput`定义为静态类型呢？是因为API的明确性和系统的安全性是非常重要需求，请参考[动态对象作为输入参数的问题](/zh/docs/mutation/save-command/input-dto/problem)。
>
> GraphQL协议面对的问题，Jimmer也同样需要面对，必须给出完整的解决方案。

提示

DTO语言是为了无法被消灭的那部分DTO类型而设计，目的是为了它们变得极其廉价。

### 1.2. 方案[​](#12-方案 "1.2. 方案的直接链接")

作为一个综合性解决方案，Jimmer不局限于ORM本身，而是为整个项目的考虑，为解决此问题，提供了DTO语言。

DTO语言是Jimmer提供的一个非常强大的功能，是一个和[对象抓取器](/zh/docs/query/object-fetcher)高度类似的概念，但在编译过程中位于更早的阶段。

DTO语言用于快速定义数据结构的形状，根据这些形状，DTO可以在编译时

* 生成特定数据结构所对应的静态DTO类型
* 生成DTO静态对象和Jimmer动态对象之间的相互转换逻辑
* 生成与DTO形状定义完全契合的[对象抓取器](/zh/docs/query/object-fetcher)

使用DTO语言快速构建DTO类型，是为Jimmer量身设计的方案，开发效率极高，远快于使用[mapstruct](/zh/docs/object/view/mapstruct)，是优先推荐的方式。

### 1.3. 应用场景[​](#13-应用场景 "1.3. 应用场景的直接链接")

DTO语言的应用场景

* [在查询API中充当返回结果 *(可选，不推荐，非必要不用)*](/zh/docs/query/object-fetcher/dto)
* [在保存API中充当输入参数 *(必须)*](/zh/docs/mutation/save-command/input-dto/dto-lang)
* [在超级QBE查询中作为输入参数 *(可选，建议使用)*](/zh/docs/query/super_qbe)

## 2. 创建文件[​](#2-创建文件 "2. 创建文件的直接链接")

DTO语言的代码体现为扩展名为`dto`的文件，一旦编译完成，这些文件就没有价值了。所以，这些文件不能放到`src/main/resources`中打包，而是放到`src/main/dto`目录中。

因此，第一件事，是在`src/main`下建立`dto`子目录。

提示

Jimmer不要求`src/main/dto`目录必须在实体类型所在的项目中。事实上，你可以在任何能合法访问实体的项目中定义创建此目录。

Jimmer只要求在包含dto文件的项目中使用`jimmer-apt`或`jimmer-ksp`，它们负责DTO文件的编译和相关代码的生成。

> 对于Java项目而言，除了`jimmer-apt`外，有可能还还需要多一个额外的配置，请参见[注意事项](#23-%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A1%B9)

### 2.1. 两种创建文件的方法[​](#21-两种创建文件的方法 "2.1. 两种创建文件的方法的直接链接")

`src/main/dto`目录下可以定义若干个dto文件，每一个文件和一个原始实体相互对应。

假设存在一个Jimmer实体类型，其完整的类型名为`com.yourcompany.yourproject.Book`，该类被`@org.babyfish.jimmer.sql.Entity`修饰 *(DTO语言只支持Entity类型)*，
有两种方法建立dto文件。

1. 不使用`export`语句

   这种情况下，dto文件的目录需要和原始实体的包对应，名称需要和原始实体的名称对应：

   1. 在`src/main/dto`目录下建立目录`com/yourcompany/yourproject`，即，建立和包名一致的目录
   2. 在上一步建立的目录中新文件`Book.dto`，该文件必须和类同名，且扩展名为`dto`
2. 使用`export`语句

   语句很重要，我们单独用一个小结来讲解。

### 2.2. export语句[​](#22-export语句 "2.2. export语句的直接链接")

这种情况下，dto文件目录和名称是随意的，因为我们会在文件内部使用`export`语句定义dto文件和哪个原始实体对应。

* 由于对dto文件的目录没有要求，建议直接在`src/main/dto`下建立dto文件
* 虽然对dto文件的名称也没有要求，但是为了项目的可维护性，仍然建议文件名采用原始实体的名称，这里，就是`Book.dto`
* dto文件的第一行代码为`export`语句

  ```
  export com.yourcompany.yourproject.Book  
    
  ...后续代码...
  ```

DTO文件被编译后，将自动生成更多的Java/Kotlin类型，它们默认的包名为：`实体包名` + `.dto`。

如果你使用了`export`语句，你可以进一步定义生成的代码所在的包，例如：

```
export com.yourcompany.yourproject.Book  
    -> package com.yourcompany.yourproject.mydto
```

用户可以编辑`Book.dto`文件，定义任意个以`Book`类型为聚合根的所有DTO类型。这里，我们先定一个DTO类型：

Book.dto

```
...省略export(如果有的话)...  
  
BookView {  
    ...略...  
}
```

信息

DTO文件的第一行代码可能是`export`语句，为简化本文，后文不再写出。

编译后会生成Java/Kotlin类型`BookView`，假设生成代码所在包的默认值没有别修改，生成代码如下

* Java
* Kotlin

BookView.java

```
package com.yourcompany.yourproject.dto;  
  
import com.yourcompany.yourproject.Book;  
import org.babyfish.jimmer.View;  
  
public class BookView implements View<Book> {  
    ...略...  
}
```

BookView.kt

```
package com.yourcompany.yourproject.dto  
  
import com.yourcompany.yourproject.Book  
import org.babyfish.jimmer.View  
  
open class BookView(  
    ...略...  
) : View<Book> {  
    ...略...  
}
```

### 2.3. 注意事项[​](#23-注意事项 "2.3. 注意事项的直接链接")

注意

* 对于Java项目而言 *(kotlin开发者请忽略)*：

  如果当前项目并非定义实体的项目，则需要在当前项目随意找一个类，用`@org.babyfish.jimmer.sql.EnableDtoGeneration`修饰。

  否则，DTO文件不会被编译。
* dto文件由Jimmer的Annotation Processor *(Java)* 或 Ksp *(Kotlin)* 编译。

  因此，如果正在使用诸如Intellij这样的IDE开发项目，那么

  + 如果除了dto文件外还有其他Java/Kotlin文件被修改了，直接点击IDE中运行按钮可以导致dto文件的重新编译
  + 但是，如果除了dto文件外没有其他Java/Kotlin文件被修改，简单地点击IDE中运行按钮并不会导致dto文件被重新编译，除非显式地rebuild！
  + 如果你使用的构建工具是Gradle，也可以使用社区提供的第三方Gradle插件来解决这个问题: [jimmer-gradle](https://github.com/Enaium/jimmer-gradle)

## 3. view、input和specification[​](#3-viewinput和specification "3. view、input和specification的直接链接")

前文提到，DTO语言有三种使用场景

* [在查询API中充当返回值 *(可选，不推荐，非必要不用)*](/zh/docs/query/object-fetcher/dto)
* [在保存API中充当输入参数 *(必须)*](/zh/docs/mutation/save-command/input-dto/dto-lang)
* [在超级QBE查询中作为输入参数 *(可选，建议使用)*](/zh/docs/query/super_qbe)

所以，DTO语言可以定义三种DTO

* view: 既不使用`input`关键字也不使用`specification`关键字，可以被理解为Output DTO。
* input: 使用`input`关键字声明，可以被理解为Input DTO。
* specification: 使用`specification`关键字声明，本身和DTO关系不够大，但可以用于做查询参数，支持[超级QBE查询](/zh/docs/query/super_qbe)。

Book.dto

```
BookView {  
    ...略...  
}  
  
AnotherBookView {  
    ...略...  
}  
  
input BookInput {  
    ...略...  
}  
  
input AnotherBookInput {  
    ...略...  
}  
  
specification BookSpecification {  
    ...略...  
}  
  
specification AnotherBookSpecification {  
    ...略...  
}
```

这表示

* `BookView`和`AnotherBookView`用作查询输出，生成的Java/Kotlin类型会实现`org.babyfish.jimmer.View<E>`接口

  备注

  建议输出DTO以`View`结尾
* `BookInput`和`AnotherBookInput`用作保存指令输入，生成的Java/Kotlin类型会实现`org.babyfish.jimmer.Input<E>`接口

  备注

  建议输入DTO以`Input`结尾
* `BookSpecification`和`AnotherBookSpecification`用作查询参数，生成的Java/Kotlin类型会实现`org.babyfish.jimmer.Specification<E>`接口

  备注

  建议查询参数DTO以`Specification`结尾

### 3.1 view和input共有的功能[​](#31-view和input共有的功能 "3.1 view和input共有的功能的直接链接")

对于view和input而言，其生成的Java/Kotlin类型可以和实体相互转化，具备如下功能

* 以原始实体类型为参数的构造方法：将Jimmer动态实体对象转化为静态DTO对象
* `toEntity()`：将静态DTO对象转化为Jimmer动态实体对象

以`BookView`为例

* Java
* Kotlin

```
Book entity = ...略...  
  
// 实体 -> DTO  
BookView dto = new BookView(entity);  
  
// DTO -> 实体  
Book anotherEntity = dto.toEntity();
```

```
val entity: Book = ...略...  
  
// 实体 -> DTO  
val dto = BookView(entity)  
  
// DTO -> 实体  
val anotherEntity: Book = dto.toEntity()
```

### 3.2 input特有功能[​](#32-input特有功能 "3.2 input特有功能的直接链接")

和Output DTO相比，Input DTO存在如下不同

* 如果实体id属性配置了自动增长策略，那么input DTO中的id属性是nullable的。

  信息

  如此设计的原因在于，当实体的id属性具备自动增长策略时，保存对象就不一定需要id属性。

  > 然而，这并非表示Jimmer会如同以JPA为代表的其他ORM一样，简单地认为认为没有id属性表示insert操作而有id属性表示update操作。
  >
  > Jimmer在这方面有更智能的策略，请参考[保存指令/保存模式](/zh/docs/mutation/save-command/save-mode)，本文不再赘述。

  如果不接受这种默认行为，开发人员也可以按照一下两种方  式之一编写DTO代码

  + 让DTO类型根本没有id属性

    ```
    input BookInput {  
        #allScalars(this)  
        -id  
    }
    ```
  + 让DTO类型的id属性不能为null

    ```
    input BookInput {  
        #allScalars(this)  
        id!  
    }
    ```
* input DTO中只能定义可以保存的属性，如简单属性、普通ORM关联属性和id-view属性。 不能定义无法保存的属性，如transient属性、公式属性、计算属性和远程关联，否则会导致编译错误。
* input DTO对nullable属性有强大的全面的支持

  提示

  对于原实体中允许为null的属性而言，如何通过Input DTO映射是一个复杂的话题，Jimmer提供全面和强大的支持。

  请参见[修改篇/保持指令/Input DTO/处理空值](/zh/docs/mutation/save-command/input-dto/null-handling)。

### 3.3 specification特有功能[​](#33-specification特有功能 "3.3 specification特有功能的直接链接")

`specification`和`input`的作用类似，用于修饰输入类型，但`specification`不提供和实体对象相互转化的能力，而是被用作支持[超级QBE查询](/zh/docs/query/super_qbe)。

提示

[超级QBE查询](/zh/docs/query/super_qbe)是Jimmer的一个非常强大的功能，本文不做阐述，请参见[相关章节](/zh/docs/query/super_qbe)。

## 4. 简单属性[​](#4-简单属性 "4. 简单属性的直接链接")

可以为DTO类型属性，用于映射原始实体类型中属性，例如

Book.dto

```
BookView {  
    id  
    name   
    edition  
}
```

这表示，DTO只映射实体中的三个属性：`id`、`name`和`edition`，如下

* Java
* Kotlin

BookView.java

```
public class BookView implements View<Book> {  
  
    private long id;  
    private String name;  
    private String edition;  
  
    public BookView(Book book) {  
        ...略...  
    }  
  
    @Override  
    public Book toEntity() {  
        ...略...  
    }  
  
    ...省略其他成员...  
}
```

BookView.kt

```
open class BookView(  
    val id: Long = 0,  
    val name: String = "",  
    val edition: Int = 0  
) : View<Book> {  
  
    constructor(book: Book): this(...略...)  
  
    override fun toEntity(): Book {  
        ...略...  
    }  
  
    ...省略其他成员...  
}
```

## 5. allScalars[​](#5-allscalars "5. allScalars的直接链接")

DTO语言支持一个特别的宏属性，`#allScalars`，表示映射实体中的所有标量属性

Book.dto

```
BookView {  
    #allScalars  
}
```

按照官方例子中`Book`类型的定义

* `Book`本身的标量属性有`id`、`name`、`edition`和`price`
* `Book`继承了`TenantAware`，`TenantAware`接口定义了属性`tenant`
* `Book`也继承了`CommonEntity` *(多继承)*，`CommonEntity`接口定义了属性`createdTime`和`modifiedTime`

`#allScalars`表示自动映射实体所有标量属性，包括继承的。

如果这不是你所要的行为，那么

* 可以使用`allScalars(Book)`，表示只映射Book类型本身定义的所有标量属性，不包含继承的属性。
* 也可以使用`allScalars(TenantAware)`和`allScalars(CommonEntity)`表示特定超类型的属性。

提示

对于当前类型`Book`而言，#allScalars(Book)也可以写作`#allScalars(this)`。

事实上，`#allScalars`支持多个参数，举例如下

| 宏表达式 | 自动映射的属性 |
| --- | --- |
| #allScalars | createdTime, modifiedTime, tenant, id, name, edition, price |
| #allScalars(this) | id, name, edition, price |
| #allScalars(TenantAware) | tenant |
| #allScalars(CommonEntity) | createdTime、modifiedTime |
| #allScalars(this, TenantAware) | tenant, id, name, edition, price |
| #allScalars(this, CommonEntity) | createdTime、modifiedTime, id, name, edition, price |
| #allScalars(TenantAware, CommonEntity) | createdTime、modifiedTime, tenant |
| #allScalars(this, TenantAware, CommonEntity) | createdTime, modifiedTime, tenant, id, name, edition, price |

信息

* 如果使用`#allScalars`宏，则它必须被定义成第一个属性，否则，否则会导致编译报错。
* 如果为`#allScalars`宏指定参数，则每个参数必须为当前实体或其基类型，否则会导致编译报错。

## 6. 负属性[​](#6-负属性 "6. 负属性的直接链接")

前文所讲的`#allScalars`宏，会批量化地让DTO映射多个属性。负属性可以去掉某些属性

Book.dto

```
BookView {  
    #allScalars  
    -tenant  
}
```

和上面一样，如果按官方例子中`Book`类型的继承关系来理解的话，`#allScalars`会加入属性`createdTime`、`modifiedTime`、`tenant`、`id`、`name`、`edition`和`price`。

而`-tenant`从中减去了`tenant`属性，最终加入属性`createdTime`、`modifiedTime`、`id`、`name`、`edition`和`price`。

信息

负属性指定的其实不是实体属性名，而是DTO中对应属性的名称，在这里二者相同，并无差异。

稍后的章节[重命名](#8-%E9%87%8D%E5%91%BD%E5%90%8D)会揭示二者区别。

不难发现，对于这个例子而言，还有一种与之等价的写法

Book.dto

```
BookView {  
    // 排除`TenantAware`  
    #allScalars(Book, CommonEntity)   
}
```

## 7. 可空性[​](#7-可空性 "7. 可空性的直接链接")

默认情况下，

* `specification`中所有属性都默认可null
* 否则，DTO属性的可空性和实体中原始属性的可空性一样。

我们可以采用问号`?`或`!`修饰DTO属性，改变DTO属性的可空性。

### 7.1. `?`[​](#71- "71-的直接链接")

我们可以采用`?`修饰DTO属性，让其可null。

 注意

如果实体中原属性已经可null，则会导致编译错误

例如

Book.dto

```
input UpdateBookInput {  
    #allScalars  
    price?  
}
```

你甚至可以对`allScalars`采用`?`，让所有自动映射的非关联属性全部可以为null

Book.dto

```
input UpdateBookInput {  
    #allScalars?  
}
```

* Specification类型不允许使用`?`

  注意

  由于`specification`默认所有属性可以为null，所以在specification内部为属性指定修饰符`?`将会导致编译错误。
* 当实体的原属性不允许为null时

  对于实体中的不允许为null原始属性而言，如果DTO对象的对应属性为null，那么将改DTO对象转化为实体对象后，实体对象中原始属性不会被赋值。
* 当实体的原属性允  许为null时

  提示

  对于原实体中允许为null的属性而言，如何通过Input DTO映射是一个复杂的话题，Jimmer提供全面和强大的支持。

  限于篇幅问题，这个问题单独形成一篇文章，请参见[修改篇/保持指令/Input DTO/处理空值](/zh/docs/mutation/save-command/input-dto/null-handling)。

### 7.2. `!`[​](#72- "72-的直接链接")

我们可以采用`!`修饰DTO属性，让其非null。

注意

如果实体中原属性已经非null，则会导致编译错误

其使用场景受限，只能用于以下三种情况。

* 在`input`类型中修饰id属性

  如果id属性被配置自动增长策略，那么对应的input DTO类型将会把id属性设置为可null。

  然而，这种行为并不总是符合用户预期，可以在`input`类型中使用`!`修饰id属性，让其非null，例如

  Book.dto

  ```
  input BookUpdateInfo {  
      #allScalars  
      id!  
  }
  ```

  注意

  如果用`?`修饰其它属性，将会导致编译错误
* 如果`input`被`unsafe`关键字修饰，则可以将任何可null的实体属性转化为非null，例如

  Book.dto

  ```
  unsafe input BookUpdateInfo {  
      #allScalars  
      store! {  
          ...略...  
      }  
  }
  ```

  信息

  对于一个实体对象而言，如果改属性对应的值为null，在使用Input DTO的构造方法将其转化为Input DTO对象是，由于DTO中对应的属性不允许为null，将会导致异常。

  这就是`unsafe`关键字的意义。
* 对于`specification`而言，由于所有属性都被默认为null，所以可以将任何可null的实体属性转化为非null，例如

  Book.dto

  ```
  specification BookSpecification {  
      #allScalars  
      edition!  
  }
  ```

## 8. 重命名[​](#8-重命名 "8. 重命名的直接链接")

可以使用`as`关键字为属性设置别名，让实体属性名和DTO属性名不同。

`as`有两种用法，既可以精确地为一个属性设置别名，也可以模糊地为多个属性设置别名

### 8.1. 重命名单个属性[​](#81-重命名单个属性 "8.1. 重命名单个属性的直接链接")

Book.dto

```
BookView {  
    name as bookName  
}
```

### 8.2. 重命名多个属性[​](#82-重命名多个属性 "8.2. 重命名多个属性的直接链接")

假设有一个实体类型叫做`Robot`

Robot.dto

```
OrderView {  
    as (^ -> oldSystem) { ❶   
        prop1  
        prop2  
    }  
    as ($ -> ForNewSystem) { ❷  
        prop3  
        prop4  
    }  
}
```

其中

* ❶ 为`prop1`和`prop2`添加前缀`oldSystem`，其中`^`表示起始位置
* ❷ 为`prop3`和`prop4`添加后缀`ForNewSystem`，其中`$`表示结尾位置

生成的`RobotView`类如下

* Java
* Kotlin

BookView.java

```
public class RobotView implements View<Robot> {  
  
    private String oldSystemProp1;  
    private String oldSystemProp2;  
  
    private String prop3ForNewSystem;  
    private String prop4ForNewSystem;  
  
    ...省略其他成员...  
}
```

BookView.kt

```
open class RobotView(  
      
    val oldSystemProp1 = "",  
    val oldSystemProp2 = "",  
  
    val prop3ForNewSystem = "",  
    val prop4ForNewSystem = ""  
) : View<Robot> {  
  
    ...省略其他成员...  
}
```

可以在`as() {...}`块中定义绝大部分属性，当然包括`#allScalars`，例如

Robot.dto

```
RobotView {  
    as(^ -> robot) {  
        #allScalars  
    }  
}
```

下面罗列`as() {...}`块的所有用法

| 代码示范 | 作用描述 |
| --- | --- |
| as(^ -> prefix) | 添加前缀 |
| as(^prefix ->) | 删除前缀 |
| as(^prefix -> newPrefix) | 替换前缀 |
| as($ -> suffix) | 添加后缀 |
| as($suffix ->) | 删除后缀 |
| as($suffix -> newSuffix) | 替换后缀 |
| as(infix -> ) | 删除任何位置的内容 |
| as(infix -> newInfix) | 替换任何位置的内容 |

警告

`^`和`$`不能同时出现，因为这表示精确重命名单个属性，这和已有的功能重复

## 9. 枚举映射[​](#9-枚举映射 "9. 枚举映射的直接链接")

默认情况下，实体的枚举类型属性被映射成DTO属性后仍然是枚举类型。

你可以将枚举类型映射为数字或字符串。

* 映射为数字

  ```
  AuthorView {  
      #allScalars  
      gender -> {  
          MALE: 100  
          FEMALE: 101  
      }  
  }
  ```

  生成如下代码

  + Java
  + Kotlin

  BookView.java

  ```
  public class AuthorView implements View<AuthorView> {  
    
      private int gender;  
    
      ...省略其他成员...  
  }
  ```

  BookView.kt

  ```
  open class RobotView(  
      val gender: Int,  
      ...省略其他成员...  
  ) : View<Robot> {  
    
      ...省略其他成员...  
  }
  ```
* 映射为字符串

  ```
  AuthorView {  
      #allScalars  
      gender -> {  
          MALE: "Male"  
          FEMALE: "Female"  
      }  
  }
  ```

  生成如下代码

  + Java
  + Kotlin

  BookView.java

  ```
  public class AuthorView implements View<AuthorView> {  
    
      private String gender;  
    
      ...省略其他成员...  
  }
  ```

  BookView.kt

  ```
  open class RobotView(  
      val gender: String,  
      ...省略其他成员...  
  ) : View<Robot> {  
    
      ...省略其他成员...  
  }
  ```

## 10. 关联属性[​](#10-关联属性 "10. 关联属性的直接链接")

对关联属性的处理是DTO语言一个非常强大的功能，存在三种用法

* 直接使用
* 递归关联
* 调用id函数
* 调用flat函数

### 10.1. 普通关联[​](#101-普通关联 "10.1. 普通关联的直接链接")

* 基本用法

  Robot.dto

  ```
  input CompositeBookInput {  
        
      #allScalars(Book)  
    
      store {  
          #allScalars(BookStore)  
          -id  
      }  
    
      authors {  
          #allScalars(Author)  
          -id  
      }  
  }
  ```

  生成的代码为

  + Java
  + Kotlin

  CompositeBookInput.java

  ```
  public class CompositeBookInput implements Input<Book> {  
    
      @Nullable  
      private Long id;  
    
      private String name;  
    
      private int edition;  
    
      private BigDecimal price;  
    
      private TargetOf_store store;  
    
      private List<TargetOf_authors> authors;  
    
      public static class TargetOf_store implements Input<BookStore> {  
        
          private String name;  
    
          @Nullable  
          private String website;  
    
          ...省略其他成员...  
      }  
    
      public static class TargetOf_authors implements Input<Author> {  
            
          private String firstName;  
    
          private String lastName;  
    
          private Gender gender;  
    
          ...省略其他成员...  
      }  
    
      ...省 略其他成员...  
  }
  ```

  CompositeBookInput.kt

  ```
  open class CompositeBookInput(  
      val id: Long? = null,  
      val name: String = "",  
      val edition: Int = 0,  
      val price: BigDecimal,  
    
      val store: TargetOf_store? = null,  
      val authors: List<TargetOf_authors> = emptyList(),  
  ) : Input<Book> {  
    
      open class TargetOf_store(  
          val name: String = "",  
          val website: String? = null,  
      ) : Input<BookStore> {  
          ...省略其他成员...  
      }  
    
      open class TargetOf_authors(  
          public val firstName: String = "",  
          public val lastName: String = "",  
          public val gender: Gender,  
      ) : Input<Author> {  
          ...省略其他成员...  
      }  
    
      ...省略其他成员...  
  }
  ```
* 重命名

  如果要对关联属性进行重命名，`as`子句应该在关联定义块之前，例如

  Book.dto

  ```
  input CompositeBookInput {  
        
      authors as authorList {  
          ...略...  
      }  
    
      ...略...  
  }
  ```

### 10.2 递归关联[​](#102-递归关联 "10.2 递归关联的直接链接")

对于实体中可递归的属性，比如，附带例子中的`TreeNode.parent`或`TreeNode.childNodes`，可以使用星号`*`将DTO对应的关联属性标记为递归属性。

TreeNode.dto

```
TreeNodeView {  
      
    #allScalars  
  
    childNodes*  
}
```

### 10.3. id函数[​](#103-id函数 "10.3. id函数的直接链接")

对于

短关联而言，
虽然我们当然选择在实体中定义[@IdView](/zh/docs/mapping/advanced/view/id-view)属性并在DTO中简单地引用它们，
但是，我们不能总是寄希望于实体类型总是声明了[@IdView](/zh/docs/mapping/advanced/view/id-view)属性，更好的方案不应该有此假设。

因此，DTO语言支持对关联属性调用`id`函数来达到同样目的：

Book.dto

```
BookView {  
  
    id(store)  
  
    id(authors) as authorIds  
  
    ...略...  
}
```

这段代码的功能和前面的例子完全一样，但不再假设实体类型总是声明了[@IdView](/zh/docs/mapping/advanced/view/id-view)属性，是更好的方案。

### 10.4. flat函数[​](#104-flat函数 "10.4. flat函数的直接链接")

* 对view和input而言，`flat`函数只能用于引用关联 *(一对一或多对一)*，不能用于集合关联 *(一对多或多对多)*，用于把关联DTO的属性提升到当前DTO类型，得到不含关联的平坦对象。
* 对于specification而言，`flat`函数没有上述限制，可以用于集合关联。请查看 [@Super QBE](/zh/docs/query/super_qbe)。

以官方例子中的`TreeNode`实体为例 *(事实上，这个例子是错误的，我们稍后会讨论这个问题)*

TreeNode.dto

```
FlatTreeNodeView {  
      
    #allScalars(TreeNode)  
  
    flat(parent) {  
        #allScalars(TreeNode)  
    }  
}
```

如上文所述，这个例子是错误的，因为聚合根和关联对象都有属性`id`和`name`，无条件把关联对象的属性往聚合根上提取，必然导致名字冲突，最终导致DTO语言编译报错。

正确的做法，是和用户多属性模糊重命名的`as(...) {}`块结合使用。让我们来看一个新的例子

TreeNode.dto

```
FlatTreeNodeView {  
      
    #allScalars(TreeNode) // id, name  
  
    flat(parent) {  
        as(^ -> parent) {  
            // parentId, parentName  
            #allScalars(TreeNode)   
        }  
    }  
}
```

这样，我们得到了一个平坦的DTO类型，具有4个属性：`id`、`name`、`parentId`和`parentName`。

甚至还可以嵌套使用`flat`函数，将来多级关联转化为平坦对象

TreeNode.dto

```
FlatTreeNodeView {  
      
    #allScalars(TreeNode)  
  
    flat(parent) {  
        as(^ -> parent) {  
            #allScalars(TreeNode)  
        }  
        flat(parent) {  
            as(^ -> grandParent) {  
                #allScalars(TreeNode)  
            }  
        }  
    }  
}
```

这样，我们得到了一个平坦的DTO类型，具有6个属性：`id`、`name`、`parentId`、`parentName`、`grandParentId`、`grandParentName`。

* Java
* Kotlin

```
TreeNode treeNode = Immutables.createTreeNode(cola -> {  
    cola.setId(4L);  
    cola.setName("Coca cola");  
    cola.applyParent(drinks -> {  
        drinks.setId(3L);  
        drinks.setName("Drinks");  
        drinks.applyParent(food -> {  
            food.setId(2L);  
            food.setName("Food");  
        })  
    })  
});  
  
// 将层次化的Entity转化为扁平的DTO  
FlatTreeNodeView view = new FlatTreeNodeView(treeNode);  
  
System.out.println(view);
```

```
val treeNode = TreeNode {  
    id = 4L  
    name = "Coca cola"  
    parent {  
        id = 3L  
        name = "Drinks"  
        parent {  
            id = 2L  
            name = "Food"  
        }     
    }  
}  
  
// 将层次化的Entity转化为扁平的DTO  
val view = FlatTreeNodeView(treeNode)  
  
println(view)
```

打印结果为 *(为了方便阅读，这里进行了格式化)*

```
com.yourcompany.yourproject.FlatTreeNodeView(  
    id = 4,  
    name = Coco cola,  
    parentId = 3,  
    parentName = Drinks,  
    grandParentId = 2,  
    grandParentName = "Food"  
)
```

`flat`方案也有缺点，对\*\*output \*\* DTO而言，`flat`方案是有争议的，并不推荐盲目采用。

请参见

Output DTO是否该用flat模式的争议

## 11. 自定义字段[​](#11-自定义字段 "11. 自定义字段的直接链接")

### 11.1 初识自定义字段[​](#111-初识自定义字段 "11.1 初识自定义字段的直接链接")

前面我们所讲的例子中，所有DTO属性都是由实体属性映射而来，成为映射属性。

除了映射属性外，DTO类型还支持映射自定义属性

BookInput.dto

```
BookInput {  
      
    #allScalars(Author)? - id  
  
    remark: String  
}
```

这里，BookInput具备一个自定义属性：`remark`。

信息

自定义属性和映射属性的区别在于需要类型定义。

### 11.2 内置类型[​](#112-内置类型 "11.2 内置类型的直接链接")

自定义属性的类型可以是内置类型。

所谓内置类型，就是DTO语言内置类型，无需采用`import`导入。

* 原生和装箱类型

  | DTO语言类型 | 生成的Java类型 | 生成的Kotlin类型 |
  | --- | --- | --- |
  | Boolean | boolean | Boolean |
  | Boolean? | Boolean | Boolean? |
  | Char | char | Char |
  | Char? | Character | Char? |
  | Byte | byte | Byte |
  | Byte? | Byte | Byte? |
  | Short | short | Short |
  | Short? | Short | Short? |
  | Int | int | Int |
  | Int? | Integer | Int? |
  | Long | long | Long |
  | Long? | Long | Long? |
  | Float | float | Float |
  | Float? | Float | Float? |
  | Double | double | Double |
  | Double? | Double | Double? |
* Any和String类型

  | DTO语言类型 | 生成的Java类型 | 生成的Kotlin类型 |
  | --- | --- | --- |
  | Any | Object | Any |
  | String | String | String |
* 数组类型

  | DTO语言类型 | 生成的Java类型 | 生成的Kotlin类型 |
  | --- | --- | --- |
  | Array<Boolean> | boolean[] | BooleanArray |
  | Array<Boolean?> | Boolean[] | Array<Boolean?> |
  | Array<Char> | char[] | CharArray |
  | Array<Char?> | Character[] | Array<Char?> |
  | Array<Byte> | byte[] | ByteArray |
  | Array<Byte?> | Byte[] | Array<Byte?> |
  | Array<Short> | short[] | ShortArray |
  | Array<Short?> | Short[] | Array<Short?> |
  | Array<Int> | int[] | IntArray |
  | Array<Int?> | Integer[] | Array<Int?> |
  | Array<Long> | long[] | LongArray |
  | Array<Long?> | Long[] | Array<Long?> |
  | Array<Float> | float[] | FloatArray |
  | Array<Float?> | Float[] | Array<Float?> |
  | Array<Double> | double[] | DoubleArray |
  | Array<Double?> | Double[] | Array<Double?> |
  | Array<UserType> | UserType[] | Array<UserType> |
  | Array<UserType?> | UserType[] | Array<UserType?> |
  | Array<\*> | Object[] | Array<\*> |
* 集合类型

  | DTO语言类型 | 生成的Java类型 | 生成的Kotlin类型 |
  | --- | --- | --- |
  | Iterable<E> | Iterable<? extends E> | Iterable<E> |
  | MutableIterable<E> | Iterable<E> | MutableIterable<E> |
  | Collection<E> | Collection<? extends E> | Collection<E> |
  | MutableCollection<E> | Collection<E> | MutableCollection<E> |
  | List<E> | List<? extends E> | List<E> |
  | MutableList<E> | List<E> | MutableList<E> |
  | Set<E> | Set<? extends E> | Set<E> |
  | MutableSet<E> | Set<E> | MutableSet<E> |
  | Map<K, V> | Map<? extends K, ? extends V> | Map<K, V> |
  | MutableMap<K, V> | Map<K, V> | Mutable<K, V> |

### 11.3 范型参数修饰[​](#113-范型参数修饰 "11.3 范型参数修饰的直接链接")

除了内置类型中的范型类 *(数组和集合)* 型外，其他范型类都支持使用`in`或`out`修饰范型参数，例如

| DTO语言类型 | 生成的Java类型 | 生成的Kotlin类型 |
| --- | --- | --- |
| UserType1<UserType2> | UserType1<UserType2> | UserType1<UserType2> |
| UserType1<out UserType2> | UserType1<? extends UserType2> | UserType1<out UserType2> |
| UserType1<in UserType2> | UserType1<? super UserType2> | UserType1<in UserType2> |

### 11.4 导入语句[​](#114-导入语句 "11.4 导入语句的直接链接")

除了内置类型外的其他类型外，其他类型在被引用时，要么书写全名，要么在文件开头使用import语句，否则，将会认为其同当前实体属于同一包。

DTO语言支持多种风格的import语句，例如

* 单类导入

  ```
  import java.time.LocalDateTime
  ```
* 单类重命名导入

  ```
  import java.time.LocalDateTime as LDT
  ```
* 多类导入

  ```
  import java.time.{   
      LocalDateTime,   
      OffsetDataTime,   
      ZonedDataTime   
  }
  ```
* 多类重命名导入

  ```
  import java.time.{   
      LocalDateTime as LDT,   
      OffsetDataTime as ODT,   
      ZonedDataTime as ZDT  
  }
  ```

## 12. 注解[​](#12-注解 "12. 注解的直接链接")

### 12.1 基本用法[​](#121-基本用法 "12.1 基本用法的直接链接")

你可以在DTO语言中使用注解修饰DTO类型、映射属性和自定义属性

Author.dto

```
import org.babyfish.jimmer.client.Doc   
import javax.validation.constraints.Size  
  
@Doc("BookInput without associations") ❶  
BookInput {  
      
    #allScalars  
  
    @Size(min = 4, max = 20) ❷  
    name  
  
    @Size(min = 10, max = 50) ❸  
    remark: String  
}
```

其中

* ❶ 修饰类型
* ❷ 修饰映射属性
* ❸ 修饰自定义属性

提示

有一个细节需要注意

BookView

```
...省略import...   
  
BookView {  
  
    ...省略其他属性...  
  
    @A authors @B {  
        ...省略关联对象属性...  
    }  
  
    ...省略其他DTO类型...  
}
```

其中

* `@A`修饰`authors`属性
* `@B`修饰匿名的关联类型

### 12.2 注解替换[​](#122-注解替换 "12.2 注解替换的直接链接")

* 如前文所述，DTO语言支持注解，直接控制被生成的DTO类型的注解
* 如果DTO属性没有被任何注解修饰，就会复制原实体中对应属性的注解 *(如果DTO属性是映射而来，而非自定义的)*。
  其中，除`org.babyfish.jimmer.client`包下的其他任何 **非** jimmer注解都会被复制到被生成的DTO类型中

### 12.3 value参数[​](#123-value参数 "12.3 value参数的直接链接")

注解中的`value`参数可以被简写，例如

```
@UserAnnotation(value = "a", value2 = "b", value3 = "c")
```

可以被简写为

```
@UserAnnotation("a", value2 = "b", value3 = "c")
```

和Java不同，无论注解有多少个参数，`value`参数都可以被缩写，只要保证被缩写的属性最先配置即可。

### 12.4 混合Java和Kotlin的语法[​](#124-混合java和kotlin的语法 "12.4 混合Java和Kotlin的语法的直接链接")

Java和Kotlin的注解语法存在一些微弱的差异，DTO语言混合了二者的特征，例如

* ```
  @UserAnnotation(  
      "key1",  
      items = {  
          @Item("subKey1", description = "Detail information for subKey1 ..."),  
          @Item("subKey2", description = "Detail information for subKey2 ..."),  
          @Item("subKey3", description = "Detail information for subKey3 ...")  
      }  
  )
  ```
* ```
  @UserAnnotation(  
      "key1",  
      items = [  
          @Item("subKey1", description = "Detail information for subKey1 ..."),  
          @Item("subKey2", description = "Detail information for subKey2 ..."),  
          @Item("subKey3", description = "Detail information for subKey3 ...")  
      ]  
  )
  ```
* ```
  @UserAnnotation(  
      "key1",  
      items = {  
          Item("subKey1", description = "Detail information or subKey1 ..."),  
          Item("subKey2", description = "Detail information for subKey2 ..."),  
          Item("subKey3", description = "Detail information for subKey3 ...")  
      }  
  )
  ```
* ```
  @UserAnnotation(  
      "key1",  
      items = [  
          Item("subKey1", description = "Detail information for subKey1 ..."),  
          Item("subKey2", description = "Detail information for subKey2 ..."),  
          Item("subKey3", description = "Detail information for subKey3 ...")  
      ]  
  )
  ```

以上四种写法是等价的。你可以随意选择你喜欢的系法。

## 13. 实现接口[​](#13-实现接口 "13. 实现接口的直接链接")

DTO类型是低价值信息，如果允许不同的DTO类型彼此引用，会引发一个问题：早期效果是方便，但后期效果是难以维护。
因此，Jimmer禁止用户在低价值的DTO类型中寻找可复用性。

然而，有的时候的确需要在不同的DTO类型之间寻找共性 *(尤其是DTO的内部类)*，以便于抽象和设计。
为此，DTO语言提供了一个折中方案：允许DTO类型实现已有的Java/Kotlin接口。前提是接口中的抽象属性是DTO中的属性的子集，能被DTO类型实现。

DTO代码如下

```
export com.yourcompany.yourproject.model.Book  
    -> com.yourcompany.yourproject.model.dto  
  
import com.yourcompany.yourcompany.common.{  
    Shape1, Shape2, Shape3, Shape4, Shape5, Shape6  
}  
  
BookView implements Shape1<String>, Shape2 {  
    #allScalars  
    store implements Shape3<String>, Shape4 {  
        #allScalars  
    }  
    authors implements Shape5<String>, Shape6 {  
        #allScalars  
    }  
}
```

编译，生成如下代码

* Java
* Kotlin

BookView.java

```
package com.yourcompany.yourproject.dto;  
  
import com.yourcompany.yourproject.Book;  
import org.babyfish.jimmer.View;  
  
public class BookView   
    implements View<Book>, Shape1<String>, Shape2 {  
      
    @Nullable  
    private TargetOf_store store;  
  
    private List<TargetOf_authors> authors;  
      
    ...省略其他成员...  
  
    public static class TargetOf_store  
        implements View<BookStore>, Shape3<String>, Shape4 {  
      
        ...省略成员...  
    }  
  
    public static class TargetOf_authors  
        implements View<Author>, Shape5<String>, Shape6 {  
      
        ...省略成员...  
    }  
}
```

BookView.kt

```
package com.yourcompany.yourproject.dto  
  
import com.yourcompany.yourproject.Book  
import org.babyfish.jimmer.View  
  
open class BookView(  
    ...省略其他属性...  
    store: TargetOf_store? = null,  
    authors: List<TargetOf_authors> = emptyList()  
) : View<Book>, Shape1<String>, Shape2 {  
      
    ...省略其他成员...  
  
    open class TargetOf_store(  
        ...略...  
    ): View<BookStore>, Shape3<String>, Shape4 {  
          
        ...略...  
    }  
  
    open class TargetOf_authors(  
        ...略...  
    ): View<Author>, Shape5<String>, Shape6 {  
          
        ...略...  
    }  
}
```

警告

如果DTO中的某个属性覆盖了接口的抽象属性，那么在自动生成的Java/Kotlin类型中，该属性会被添加`@Override`注解 *(Java)* 或`override`关键字 *(Kotlin)*。

除此之外，DTO语言对接口实现的验证并不多。如果用户犯了其他错误，将会导致生成错误的Java/Kotlin类型，由Java/Kotlin编译器负责处理。

## 14. 相关链接[​](#14-相关链接 "14. 相关链接的直接链接")

前文提到，DTO语言还有两个强大的功能，由于篇幅原因，未在本文中深入讨论，而是被独立成了其他文档。这里再次强调一次。

* [在Input DTO中处理空值](/zh/docs/mutation/save-command/input-dto/null-handling)
* [Specification DTO，超级QBE](/zh/docs/query/super_qbe)

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/object/view/dto-language.mdx)

最后于 **2025年9月16日** 更新