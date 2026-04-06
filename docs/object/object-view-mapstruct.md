# MapStruct

> 来源: https://jimmer.deno.dev/zh/docs/object/view/mapstruct

- [对象篇](/zh/docs/object/)
- [DTO转换](/zh/docs/object/view/)
- MapStruct

本页总览

# MapStruct

## 简介[​](#简介 "简介的直接链接")

Jimmer拓展了[MapStruct](https://mapstruct.org/)，支持使用mapstruct来完成Jimmer动态实体对象和静态DTO对象之间的相互转化。

### 注意事项[​](#注意事项 "注意事项的直接链接")

Jimmer的实体对象是动态的 *(和Hibernate3引入的标量属性惰性化比较类似)*，这是较早的[MapStruct](https://mapstruct.org/)未曾考虑过的模式。

和[MapStruct](https://mapstruct.org/)交流后，[MapStruct](https://mapstruct.org/)会从`1.6.0`开始支持这种行为。

警告

因此，请尽可能使用`1.6.0`或更高版本的[MapStruct](https://mapstruct.org/)。

### 优点[​](#优点 "优点的直接链接")

- 和追求快速开发但支持固定转化逻辑的[DTO语言](/zh/docs/object/view/dto-language)不同，[mapstruct](https://mapstruct.org/)可以实现任意复杂的转化逻辑。
- 和[DTO语言](/zh/docs/object/view/dto-language)直接生成全新的DTO类型不同，[mapstruct](https://mapstruct.org/)可以整合现有的DTO类型。

### 缺点[​](#缺点 "缺点的直接链接")

更推荐使用[DTO语言](/zh/docs/object/view/dto-language)，原因如下

- 不可忽略的开发成本

  [DTO语言](/zh/docs/object/view/dto-language)为Jimmer量身定制的方案，开发效率是结合其他任何技术方案无法比拟的。
- 不太适合Output DTO

  DTO语言自动生成的DTO类型具备内置的[对象抓取器](/zh/docs/query/object-fetcher)，因此可以作为查询的输出类型 *(虽然不推荐)*，请参见：

  - [对象抓取器/DTO查询](/zh/docs/query/object-fetcher/dto)
  - [Spring Data/查询DTO](/zh/docs/spring/repository/dto)

  然而，手动定义的DTO类型没有对应的[对象抓取器](/zh/docs/query/object-fetcher)定义，只支持和动态实体相互转化。
  虽然可以为此手动定义[对象抓取器](/zh/docs/query/object-fetcher)，但是存在DTO和[对象抓取器](/zh/docs/query/object-fetcher)形状不一致的风险。
  所以，不适合作为Output DTO。
- Kotlin风险

  - [mapstruct](https://mapstruct.org/)是基于`apt`*(Annotation Processor)* 的。

    因此，这需要在kotlin中使用`kapt`，这会明显降低kotlin项目的编译速度。
  - Kotlin已经废弃了[kapt](https://kotlinlang.org/docs/kapt.html)，而主张使用[ksp](https://kotlinlang.org/docs/ksp-overview.html)。

    因此，随着Kotlin的演化，未来使用`kapt`可能会遇到问题。

## 依赖和预编译器[​](#依赖和预编译器 "依赖和预编译器的直接链接")

对于将静态POJO转化为Jimmer动态对象而言，MapStruct并不知道该如何构建Jimmer对象。因此

- Jimmer本身的预编译器 *(Java的`jimmer-apt`或Kotlin的`jimmer-ksp`)* 在Draft中生成了一个一些面向MapStruct的代码，让MapStruct可以通过其[Builder](https://mapstruct.org/documentation/stable/reference/html/#mapping-with-builders)模式构建Jimmer对象。
- Jimmer扩展了MapStruct的Annotation Processor，该扩展让MapStruct利用生成的Draft中为MapStruct预留的能力构建Jimmer对象。

  这个扩展叫做`jimmer-mapstruct-apt`

  - 对于Java而言，`jimmer-mapstruct-apt`被`jimmer-apt`所包含
  - 对于Kotlin而言，需同时在maven或gradle配置文件中使用`jimmer-ksp`和`jimmer-mapstruct-apt`。

你既可以使用Jimmer标准的构建方式，也可以采用社区提供的插件

- 用法一：使用Jimmer标准的构建方式

  - Java(Maven)
  - Java(Gradle)
  - Kotlin(Gradle.kts)

  pom.xml

  ```...省略其他代码...
  <build>
      <dependencies>
          <dependency> ➀
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
              <version>${lombok.version}</version>
          </dependency>
          <dependency> ➊
              <groupId>org.mapstruct</groupId>
              <artifactId>mapstruct</artifactId>
              <version>${mapstruct.version}</version>
          </dependency>
          ...省略其他依赖...
      </dependencies>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-compiler-plugin</artifactId>
              <version>3.10.1</version>
              <configuration>
                  <annotationProcessorPaths>
                      <path> ➁
                          <groupId>org.projectlombok</groupId>
                          <artifactId>lombok</artifactId>
                          <version>${lombok.version}</version>
                      </path>
                      <path> ➋
                          <groupId>org.babyfish.jimmer</groupId>
                          <artifactId>jimmer-apt</artifactId>
                          <version>${jimmer.version}</version>
                      </path>
                      <path> ➌
                          <groupId>org.mapstruct</groupId>
                          <artifactId>mapstruct-processor</artifactId>
                          <version>${mapstruct.version}</version>
                      </path>
                  </annotationProcessorPaths>
              </configuration>
          </plugin>
      </plugins>
  </build>

  ...省略其他代码...

```

  build.gradle

  ```dependencies {
      implementation "org.projectlombok:lombok:${lombok.version}" ➀
      implementation "org.mapstruct:mapstruct:${mapstructVersion}" ➊

      annotationProcessor "org.projectlombok:lombok:${lombok.version}" ➁
      annotationProcessor "org.babyfish.jimmer:jimmer-apt:${jimmerVersion}" ➋
      annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}" ➌

      ...省略其他依赖...
  }

```

  build.gradle.kts

  ```plugins {
      id("com.google.devtools.ksp") version "1.7.10-1.0.6"
      kotlin("kapt") version "1.7.10"

      ...省略其他插件...
  }

  dependencies {

      implementation("org.mapstruct:mapstruct:${mapstructVersion}") ➊

      ksp("org.babyfish.jimmer:jimmer-ksp:${jimmerVersion}") ➋
      kapt("org.mapstruct:mapstruct-processor:${mapstructVersion}") ➌
      kapt("org.babyfish.jimmer:jimmer-mapstruct-apt:${jimmerVersion}") ⓐ

      ...省略其他依赖...
  }

  kotlin {
      sourceSets.main {
          kotlin.srcDir("build/generated/ksp/main/kotlin")
      }
  }

```

- 用法二：使用社区提供的插件

  <https://github.com/ArgonarioD/gradle-plugin-jimmer>

  - Java(Gradle插件)
  - Kotlin(Gradle插件)

  build.gradle

  ```plugins {
      id "tech.argonariod.gradle-plugin-jimmer" version "latest.release"

      ...省略其他插件...
  }

  jimmer {
      version = "${jimmerVersion}"

      ...省略其他配置...
  }

  dependencies {

      implementation "org.projectlombok:lombok:${lombok.version}" ➀
      implementation "org.mapstruct:mapstruct:${mapstructVersion}" ➊

      annotationProcessor "org.projectlombok:lombok:${lombok.version}" ➁
      annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}" ➍

      // 不需要手动添加 org.babyfish.jimmer:jimmer-apt 的依赖
      // 检测到 mapstruct-processor 时，插件会自动添加该依赖

      ...省略其他依赖...
  }

```

  build.gradle.kts

  ```plugins {
      id("tech.argonariod.gradle-plugin-jimmer") version "latest.release"
      id("com.google.devtools.ksp") version "1.7.10+"
      kotlin("kapt") version "1.7.10"

      ...省略其他插件...
  }

  jimmer {
      version = "${jimmerVersion}"

      ...省略其他配置...
  }

  dependencies {

      implementation("org.mapstruct:mapstruct:${mapstructVersion}") ➊
      kapt("org.mapstruct:mapstruct-processor:${mapstructVersion}") ➌

      ...省略其他依赖...
  }

```

  信息

  当你引入了MapStruct的kapt依赖时，插件会自动引入`jimmer-mapstruct-apt`的依赖。

这个例子中，我们假设Java中基于[lombok](https://projectlombok.org/)编写静态POJO。

| 语言          | 位置                                                                 | 描述                                  |
|-------------|--------------------------------------------------------------------|-------------------------------------|
| ---         | ---                                                                | ---                                 |
| Java和Kotlin | ➊                                                                  | 引入mapstruct依赖，让用户代码可以使用mapstruct的注解 |
| ➋           | 使用Jimmer的预编译器为动态类型生成相关的源代码，Java使用`jimmer-apt`，Kotlin使用`jimmer-ksp` |                                     |
| ➌           | 使用mapstruct的annotation processor生成源代码 *(后文会介绍)*                    |                                     |
| 仅Java       | ➀                                                                  | 引入lombok的依赖，让用户代码可以使用lombok的注解      |
| ➁           | 使用Lombok的预编译器更改静态POJO类的代码，比如添加getter, setter                       |                                     |
| 仅Kotlin     | ⓐ                                                                  | 使用`jimmer-mapstruct-apt`拓展➌         |

## 定义POJO[​](#定义pojo "定义POJO的直接链接")

- Java
- Kotlin

BookInput.java

```@data
public class BookInput {

    @Nullable
    private Long id;

    private String name;

    private int edition;

    private BigDecimal price;

    @Nullable
    private Long storeId;

    private List<Long> authorIds;
}

```

BookInput.java

```data class bookinput(
    val id: Long? = null,
    val name: String,
    val edition: Int,
    val price: BigDecimal,
    val storeId: Long?,
    val authorIds: List<Long>
)

```

备注

Java POJO代码中采用了`@Nullable`注解，仅为提高可读性，无实质性功能

该POJO有三个属性，需要说明一下

- `BookInput.id`

  - 这里，`BookInput.id`是允许为null的。这是必要的，比如，插入数据不需要指定id。
  - 实体对象动态属性`Book.id`不允许为null *(Jimmer禁止id可以为null，请参见[映射篇/基础映射/简单映射#@Id](/zh/docs/mapping/base/basic#id))*

  二者彼此矛盾，那么，`BookInput`怎么转化为`Book`呢？

  提示

  Jimmer约定，如果POJO的属性可为null而动态对象上对应的属性不能为null，那么动态对象的对应属性不会被赋值，保持缺失的状态。
- `BookInput.storeId`

  很明显，这是关联id，对实体 对象动态属性`Book.store`。

  这种动态对象属性被定义为关联对象，而POJO中却定义为关联id，叫做

  短关联
- `BookInput.authorIds`

  很明显，这是关联id集合，对实体对象动态属性`Book.authors`。

  这种动态对象属性被定义为关联对象，而POJO中却定义为关联id，叫做

  短关联

其他属性和原始实体的定义完全一样，无需说明

## 定义Mapper[​](#定义mapper "定义Mapper的直接链接")

使用mapstruct最重要的事是定义Mapper，如下

- Java
- Kotlin

BookInputMapper.java

```@mapper
public interface BookInputMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Book toBook(BookInput input);
}

```

BookInputMapper.java

```@mapper
interface BookInputMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    fun toBook(input: BookInput): Book
}

```

该Mapper提供一个`toBook`方法，用于把`BookInput`对象转化为`Book`对象。

`BookInput.id`、`BookInput.id`、`BookInput.name`和`BookInput.price`都是非关联属性，mapstruct能很好地处理它们。

备注

其中，`BookInput.id`可以为null, 而`Book.id`不能为null的问题，前面已经讨论过了，这里不再赘述。

关键是`BookInput.storeId`和`BookInput.authorIds`应该如何映射，这分为两种情况了。

- 实体定义了@IdView属性
- 实体未定义@IdView属性

### 如果实体定义了@IdView属性[​](#如果实体定义了idview属性 "如果实体定义了@IdView属性的直接链接")

如果实体类型定义了`@IdView`属性，例如

- Java
- Kotlin

Book.java

```package com.example.model;
import org.babyfish.jimmer.sql.*;
import org.jetbrains.annotations.Nullable;

@Entity
public interface Book {

    ...省略其他属性...

    @ManyToOne
    @Nullable
    BookStore store();

    @ManyToMany
    @JoinTable(
        name = "BOOK_AUTHOR_MAPPING",
        joinColumnName = "BOOK_ID",
        inverseJoinColumnName = "AUTHOR_id"
    )
    List<Author> authors();

    @IdView // 关联对象store的id的视图
    Long storeId();

    // 关联对象集合authors中所有对象的id的视图
    @IdView("authors")
    List<Long> authorIds();
}

```

Book.kt

```package com.example.model
import org.babyfish.jimmer.sql.*

@Entity
interface Book {

    ...省略其他属性...

    @ManyToOne
    val store: BookStore?

    @ManyToMany
    @JoinTable(
        name = "BOOK_AUTHOR_MAPPING",
        joinColumnName = "BOOK_ID",
        inverseJoinColumnName = "AUTHOR_id"
    )
    val authors: List<Author>

    @IdView // 关联对象store的id的视图
    val storeId: Long?

    // 关联对象集合authors中所有对象的id的视图
    @IdView("authors")
    val authorIds: List<Long>
}

```

这种情况下，实体对象和POJO完全对应，Mapper无需做任何修改。

### 如果实体未定义@IdView属性[​](#如果实体未定义idview属性 "如果实体未定义@IdView属性的直接链接")

如果实体类型并为定义`@IdView`属性，需要修改Mapper

- 将`BookInput.storeId`转化为只有id的`BookStore`对象，再赋给`Book.store`
- 将`BookInput.authorIds`转化为只有id的`Author`对象的集合，再赋给`Book.authors`

- Java
- Kotlin

BookInputMapper.java

```@mapper
public interface BookInputMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "store", source = "storeId")
    @Mapping(target = "authors", source = "authorIds")
    Book toBook(BookInput input);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = ".")
    BookStore toBookStore(Long id);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = ".")
    Author toAuthor(Long id);
}

```

BookInputMapper.java

```@mapper
interface BookInputMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "store", source = "storeId")
    @Mapping(target = "authors", source = "authorIds")
    fun toBook(input: BookInput): Book

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = ".")
    fun toBookStore(id: Long?): BookStore

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = ".")
    fun toAuthor(id: Long?): Author
}

```

由于mapstruct还支持`@Mapping(target = "store.id", source = "storeId")`的写法，也可以用下面的写法来简化代码

- Java
- Kotlin

BookInputMapper.java

```@mapper
public interface BookInputMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "store.id", source = "storeId")
    @Mapping(target = "authors", source = "authorIds")
    Book toBook(BookInput input);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = ".")
    Author toAuthor(Long id);
}

```

BookInputMapper.java

```@mapper
interface BookInputMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "store.id", source = "storeId")
    @Mapping(target = "authors", source = "authorIds")
    fun toBook(input: BookInput): Book

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = ".")
    fun toAuthor(id: Long?): Author
}

```

## 使用[​](#使用 "使用的直接链接")

现在，我们就可以把`BookInput`转化为`Book`了

- Java
- Kotlin

```bookinput input = ...省略...;
BookInputMapper mapper = Mappers.getMapper(BookInputMapper.class);
Book book = mapper.toBook(input);

```

```val input: bookinput = ...省略...
val mapper = Mappers.getMapper(BookInputMapper::class.java)
val book = mapper.toBook(input)

```

## 让POJO实现Input接口[​](#让pojo实现input接口 "让POJO实现Input接口的直接链接")

Jimmer提供了一个简单接口，`org.babyfish.jimmer.Input<E>`

```public interface input<e> {
    E toEntity();
}

```

动态对象永远不会实现此结构，该接口应该由用户自定义的静态POJO类来实现。其功能非常简单，就是把当前静态POJO转化为动态对象。

该接口可以提供语法层面的便利，无论是底层的[保存指令](/zh/docs/mutation/save-command)还是上层的spring-data基接口`JRepository/KRepository`，其`sava`方法都直接接受`Input`参数，而无需用户调用Mapper完成转化。

如果想要这个语法层面的便利，你可以选择让POJO实现该接口，修改`BookInput`的代码，如下

- Java
- Kotlin

BookInput.java

```@data
public class BookInput implements Input<Book> { ❶

    private static final Converter CONVERTER =
        Mappers.getMapper(Converter.class);

    ...省略私有字段...

    @Override
    public Book toEntity() { ❷
        return CONVERTER.toBook(this);
    }

    @Mapper
    interface Converter {

        @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
        Book toBook(BookInput input);

        ...省略其他mapstruct配置...
    }
}

```

BookInput.kt

```data class bookinput(
    ...略...
): Input<Book> { ❶

    override fun toEntity(): Book = ❷
        CONVERTER.toBook(this)

    @Mapper
    internal interface Converter {

        @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
        fun toBook(input: BookInput): Book

        ...省略其他mapstruct配置...
    }

    companion object {

        @JvmStatic
        private val CONVERTER =
            Mappers.getMapper(Converter::class.java)
    }
}

```

- ❶ `BookInput`类实现了接口`org.babyfish.jimmer.Input`
- ❷ 实现`Input.toEntity`方法，利用MapStruct把当前静态的`Input DTO`对象转化为动态的`Book`实体对象。这是这个类唯一的功能

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/object/view/mapstruct.mdx)

最后于 **2025年9月16日** 更新