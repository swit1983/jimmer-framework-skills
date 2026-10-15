---
title: APT/KSP
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/overview/apt-ksp
---

# APT/KSP

## 基本概念

Jimmer高度依赖于JVM生态的预编译技术：

-   对于Java而言，就是APT，即[Annotation Processor Tool](https://www.jetbrains.com/help/idea/annotation-processors-support.html)

-   Kotlin而言，就是KSP，即[Kotlin Symbol Processing](https://kotlinlang.org/docs/ksp-overview.html)

:::info
使用APT/KSP自动生成的一些代码，是使用Jimmer所必须的。

因此，如果使用Intellij打开[官方例子](https://github.com/babyfish-ct/jimmer-examples)中的任何一个Java/Kotlin项目，都会发现一些本该被自动生成代码并不存在的问题。对此，可以选择以下任何一种方法：

-   先用命令行在要打开的项目目录下执行`./mvnw install`*(仅Java例子提供)*或`./gradlew build`命令完成代码生成，再用Intellij打开项目。

-   直接用Intellij打开项目，暂时无视IDE的错误，依赖下载完毕后，直接运行项目的main方法或单元测试 *(save-command/save-command-kt以单元测试演示功能)*，所有IDE错误将会自动消失，应用也会被正确启动。
:::

## 如何使用

你既可以使用Jimmer标准的构建方式，也可以采用社区提供的插件

-   用法一：使用Jimmer标准的构建方式

    > 📊 此处官方页面包含交互式图示/演示：`VersionBadgeAdmonition`。
> 可访问 https://babyfish-ct.github.io/jimmer-doc/zh/docs/_shared/precompiler 查看。

**Java(Maven)**

```xml title="pom.xml"
...省略其他代码...

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
            <configuration>

                <annotationProcessorPaths>
                    <path>
                        <groupId>org.babyfish.jimmer</groupId>

                        <artifactId>jimmer-apt</artifactId>
                        <version>${jimmer.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>

...省略其他代码...
```

**Java(Gradle)**

```groovy title="build.gradle"
dependencies {

    ...省略其他依赖...

    annotationProcessor "org.babyfish.jimmer:jimmer-apt:${jimmerVersion}"
}
```

**Kotlin(Gradle.kts)**

对于 Kotlin/JVM 项目，请使用与项目 Kotlin 版本兼容的 KSP2 版本。
在定义实体或 DTO 文件的每个模块中添加以下配置：

```kotlin title="build.gradle.kts"
import com.google.devtools.ksp.gradle.KspAATask

plugins {
    // 添加ksp插件
    id("com.google.devtools.ksp") version "<ksp-version>"

    ...省略其他插件...
}
dependencies {

    // 应用jimmer的ksp代码生成器

    ksp("org.babyfish.jimmer:jimmer-ksp:${jimmerVersion}")

    ...省略其他依赖...
}

// 仅修改 .dto 文件时也重新生成 DTO。
tasks.withType<KspAATask>().configureEach {
    val dtoDirectory = if (name == "kspKotlin") "src/main/dto" else "src/test/dto"
    inputs.files(fileTree(dtoDirectory) { include("**/*.dto") })
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

// 将生成的代码添加到编译路径中。
// 没有这个配置，gradle命令仍然可以正常执行，
// 但是, Intellij无法找到生成的源码。
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
```

如果配置了 `jimmer.dto.dirs` 或 `jimmer.dto.testDirs`，请使用实际配置的目录替换上述默认目录。
文件树会跟踪 DTO 文件的修改、新增和删除；输入未变化时，任务仍可保持 `UP-TO-DATE`。
修改 DTO 后请执行 Gradle 构建；不通过 Gradle 执行的 IDE 构建不使用上述配置。

-   用法二：使用社区提供的出插件

    <details>
    <summary>https://github.com/ArgonarioD/gradle-plugin-jimmer</summary>

**Java(Gradle插件)**

```groovy title="build.gradle"
plugins {
    // 从 Gradle 7.0 开始，可以使用 "latest.release" 代替具体的版本号，代表使用最新版本
    // 也可以使用 '+' 字符代表从 '+' 字符开始匹配最新的版本号
    id "tech.argonariod.gradle-plugin-jimmer" version "latest.release"

    ... 省略其它插件 ...
}

jimmer {
    // 设定 jimmer 依赖版本，此处也可以使用 "latest.release" 或 "0.+" 等版本范围表达式
    version = "${jimmerVersion}"
}
```

**Kotlin(插件)**

```kotlin title="build.gradle.kts"
plugins {
    // 从 Gradle 7.0 开始，可以使用 "latest.release" 代替具体的版本号，代表使用最新版本
    id("tech.argonariod.gradle-plugin-jimmer") version "latest.release"
    // 也可以使用 '+' 字符代表从 '+' 字符开始匹配最新的版本号
    // 添加ksp插件
    id("com.google.devtools.ksp") version "<ksp-version>"

    ... 省略其它插件 ...
}

jimmer {
    // 设定 jimmer 依赖版本，此处也可以使用 "latest.release" 或 "0.+" 等版本范围表达式
    version = "${jimmerVersion}"
}
```

    </details>



:::note
[KSP](https://kotlinlang.org/docs/ksp-overview.html)官方只支持gradle，
经过实践验证，KSP的第三方Maven插件支持跟不上`kotlin/KSP`本身的版本迭代，往往在升级过程中遇到很多问题。

最终Jimmer放弃了对Kotlin的Maven支持，请Kotlin开发人员使用Gradle.
:::

对于[官方例子](https://github.com/babyfish-ct/jimmer-examples)中的所有项目而言

-   所有Java例子都具备`pom.xml`和`build.gradle`，即，maven/gradle双支持。

    首次打开这种项目时，Intellij会询问以何种方式打开，做出选择即可。

    如果要切换打开方式，退出Intellij，删除项目下的隐藏目录`.idea`，再用Intellij打开，重新选择即可。

-   所有的Kotlin例子，只具备`build.gradle.kts`文件，即，只支持gradle。前面已经解释原因。

Intellij对通过maven引入的annotation processor的整合存在一些轻率的过度优化措施，导致gradle和IDE配合的开发体验优于maven。

## 在哪使用

业务项目很少是一个单项目，更多时候是借助于Maven和Gradle这类构建工具分割成多个子项目。

那么？我们应在哪些子项目中使用前面讲过的配置呢？

|子项目类型|使用目的|注意事项|
|--------|-------|-----------------|
|定义实体的项目|根据实体定义生成必要的代码，例如Draft，SQL DSL, Fetcher||
|在`src/main/dto`目录下定义DTO文件的项目|根据DTO语言的代码生成DTO类型|对于Java而言，除非当前子项目有实体定义，否则需要随便找个类用`@EnableDtoGeneration`修饰|
|使用Spring Web注解的项目|自动生成Openapi文档和TypeScript代码，将Web Api的Java/Kotlin文档注释写入文档和客户端代码中；支持远程异常||

## 注意事项

APT 和 KSP 在编译时执行。修改 Java 或 Kotlin 源码后，通过已配置注解处理器的 IDE
构建或运行项目，通常会自动更新生成的代码和资源。

修改 DTO 文件也需要触发代码生成。对于通过 Gradle 构建的 Kotlin/JVM 项目，
请使用上方 Kotlin 配置中的 DTO 任务输入设置，并在修改 `.dto` 文件后执行 Gradle 构建。
如果没有注册这些输入，Gradle 可能将 KSP 任务视为 `UP-TO-DATE`，保留旧的 DTO 代码。

对于其他构建方式，仅修改 DTO 文件时，可以使用配套的 DTO 插件或执行 clean 后重新构建。
clean 构建也有助于排查生成代码未更新的问题；配置 Gradle 输入后，无需每次修改 DTO 都执行 clean。

## Java代码的两种风格

和Kotlin API不同，Java API做不到自动生成的类型不出现在用户代码中。请看如下对比

<table>
<thead>
<th>功能</th>
<th>Java</th>
<th>Kotlin</th>
</thead>
<tbody>
<tr>
<td rowspan="2">Draft</td>
<td>使用生成的类型`BookDraft`</td>
<td>使用原实体类型`Book`</td>
</tr>
<tr>
<td>

```java
Book book =

    BookDraft.$.produce(b -> {
        b.setName("SQL");
        b.addIntoAuthors(a -> {
            a.setName("Jessica");
        });
        b.addIntoAuthors(a -> {
            a.setName("Bob");
        });
    });
```

</td>
<td>

```kotlin
val book =

    Book {
        name = "SQL in Action"
        authors().addBy {
            name = "Jessica"
        }
        authors().addBy {
            name = "Bob"
        }
    }
```

</td>
</tr>
<tr>
<td rowspan="2">SQL DSL</td>
<td>使用生成的类型`BookTable`</td>
<td>使用原实体类型`Book`</td>
</tr>
<tr>
<td>

```java

BookTable table = BookTable.$;

List<Book> books = sqlClient
    .createQuery(table)
    .where(table.storeId().isNull())
    .orderBy(table.name())
    .select(table)
    .execute();
```

</td>
<td>

```kotlin
List<Book> books = sqlClient

    .createQuery(Book::class) {
        where(table.storeId.isNull())
        orderBy(table.name)
        select(table)
    }
    .execute()
```

</td>
</tr>
<tr>
<td rowspan="2">Fetcher</td>
<td>使用生成的类型`BookFetcher`, `BookStoreFetcher`和`AuthorFetcher`</td>
<td>使用原实体类型`Book`</td>
</tr>
<tr>
<td>

```java
Fetcher<Book> fetcher =

    BookFetcher.$
        .allScalarFields()
        .store(

            BookStoreFetcher.$
                .allScalarFields()
        )
        .authors(

            AuthorFetcher.$
                .allScalarFields()
        )
```

</td>
<td>

```kotlin
val fetcher =

    newFetcher(Book::class).by {
        allScalarFields()
        store {
            allScalarFields()
        }
        authors {
            allScalarFields()
        }
    }
```

</td>
</tr>
</tbody>
</table>

可以看到，Java和Kotlin的抽象能力不同，导致API设计能达到的效果不同：

-   对于Kotlin而言，无论何种场景，都只需使用原实体类型`Book`。

-   对于Java而言，不得不使用由Annotation Processor自动生成的类型，例如`BookDraft`, `BookTable`, `BookFetcher`等。

    上述Java代码中频繁出现`.$`，`$`这些类的静态只读字段。

其实，使用`$`是最简单的使用方式。然而，考虑到部分Java开发人员对`$`存在主观偏见，对定义实体类型的Java子项目而言，
Jimmer的APT还是生成4个汇总类型:

-   `Objects`类

-   `Tables`接口

-   `TableExes`接口

-   `Fetchers`接口

这4个类型所在的包，为所有实体的公共包。

这4个类型通过定义静态常量为Java代码提供另外一种代码风格，两种风格对比如下

|接受`$`的风格|不接受`$`的风格|
|---|----|
|BookDraft.$.produce|Immutables.createBook|
|BookTable.$|Tables.BOOK_TABLE|
|BookTableEx.$|TableExes.BOOK_TABLE_EX|
|BookFetcher.$|Fetchers.BOOK_FETCHER|

另外，`Tables`, `TableExes`和`Fetchers`是接口，可以使用implements语句来进一步简化代码 *(由于Intellij对静态导入支持不友好，实现定义常量的接口仍然是值得推荐的技巧)*，例如

```java
public interface FetcherConstants

implements Fetchers {

    Fetcher<Book> BOOK_DETAIL_FETCHER =
        BOOK_FETCHER
            .allScalarFields()
            .store(
                BOOK_STORE_FETCHER
                    .allScalarFields()
            )
            .authors {
                AUTHOR_FETCHER
                    .allScalarFields()
            };
}
```

## 和Lombok配合

Java项目常常和Lombok配合使用使用。

默认情况下，如果项目除了lombok外没有其他APT，只需导入lombok的依赖即可。

然而，一旦引入了其他APT配置*(不一定是Jimmer的APT，任何其他APT)*，则必须明确配置lombok的APT。

**Java(Maven)**

```xml title="pom.xml"
...省略其他代码...

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
            <configuration>
                <annotationProcessorPaths>

                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>

                    <path>
                        <groupId>org.babyfish.jimmer</groupId>
                        <artifactId>jimmer-apt</artifactId>
                        <version>${jimmer.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>

...省略其他代码...
```

**Java(Gradle)**

```groovy title="build.gradle"
dependencies {

    ...省略其他依赖...

    annotationProcessor "org.projectlombok:lombok:${lombokVersion}"
    annotationProcessor "org.babyfish.jimmer:jimmer-apt:${jimmerVersion}"
}
```

**Java(Gradle插件)**

```groovy title="build.gradle"
dependencies {

    ...省略其他依赖...

    annotationProcessor "org.projectlombok:lombok:${lombokVersion}"
}
```
