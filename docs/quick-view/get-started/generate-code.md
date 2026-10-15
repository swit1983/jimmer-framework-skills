---
title: 生成代码
source: https://babyfish-ct.github.io/jimmer-doc/zh/docs/quick-view/get-started/generate-code
---

# 生成代码

## 代码生成

和JPA2.0 Criteria、QueryDsl、Fluent MyBatis等框架类似，Jimmer是让开发人员使用强类型DSL进行开发，尽可能在编译时发现开发人员犯的错误，而非运行时。

因此，编译时需要根据用户编写的实体类型，生成一些代码。

- Java用户使用Annotation Processor
- Kotlin用户使用KSP

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

完成这样的配置后，使用maven或gradle编译项目，即可根据用户编写的实体类型自动生成更多的源代码。

:::caution
注意，上面的代码，仅仅针对初次入门时项目结构没有切分的场景。

如果把实体的定义剥离成一个独立项目,和其他数据层业务层开，这套代码生成器需要配置到实体定义项目中。
为了保证实体定义项目的简单，原则上讲应该只添加一个最简单的的依赖：`jimmer-core`。

Jimmer定义数据类型需要如下4个注解之一
-   org.babyfish.jimmer.Immutable
-   org.babyfish.jimmer.sql.Entity
-   org.babyfish.jimmer.sql.MappedSuperclass
-   org.babyfish.jimmer.sql.Embeddable

截止到目前为止，我们只介绍了`@Entity`，其他的东西会在其他后续文档中说明，这里先忽略。这四个注解中，第一个是ORM无关的，后面三个是ORM相关的。

一旦实体项目中有数据类型使用了后面三种注解中的任何，就会导致一个问题：用户编写的实体代码本身只需要依赖`jimmer-core`，但是代码生成器生成的代码需要依赖`jimmer-sql`。

为解决这个问题，请如此配置依赖

> 📊 此处官方页面包含交互式图示/演示：`VersionBadgeAdmonition`。
> 可访问 https://babyfish-ct.github.io/jimmer-doc/zh/docs/quick-view/get-started/generate-code 查看。

**Maven**

```xml title="pom.xml"
...省略其他代码...

<dependencies>
    <dependency>
        <groupId>org.babyfish.jimmer</groupId>
        <!-- 用户编写的实体代码所需的依赖 -->
        <artifactId>jimmer-core</artifactId>
        <version>${jimmer.version}</version>
    </dependency>
    <dependency>
        <groupId>org.babyfish.jimmer</groupId>
        <!-- 生成的代码所需的依赖 -->
        <artifactId>jimmer-sql</artifactId>
        <version>${jimmer.version}</version>
        <!--其他项目必然导入包含此依赖-->

        <scope>provided</scope>
    </dependency>

    ...省略其他依赖...
</dependencies>

...省略其他代码...
```

**Java(Gradle)**

```groovy title="build.gradle"
...省略其他代码...

dependencies {

    ...省略其他依赖...

    // 用户编写的实体代码所需的依赖
    implementation "org.babyfish.jimmer:jimmer-core:${jimmerVersion}"

    // 生成的代码所需的依赖，其他项目必然导入包含此依赖

    compileOnly "org.babyfish.jimmer:jimmer-sql:${jimmerVersion}"
}

...省略其他代码...
```

**Kotlin(Gradle.kts)**

```kotlin title="build.gradle.kts"
...省略其他代码...

dependencies {

    ...省略其他依赖...

    // 用户编写的实体代码所需的依赖
    implementation("org.babyfish.jimmer:jimmer-core:${jimmerVersion}")

    // 生成的代码所需的依赖，其他项目必然导入包含此依赖

    compileOnly("org.babyfish.jimmer:jimmer-sql:${jimmerVersion}")
}

...省略其他代码...
```

**Java(Gradle插件)**

```groovy title="build.gradle"
jimmer {
    // 使 jimmer-sql 仅在生成和编译代码时被依赖
    ormCompileOnly = true

    ...省略其它配置...
}

...省略其他代码...
```

**Kotlin(Gradle插件)**

```kotlin title="build.gradle.kts"
jimmer {
    // 使 jimmer-sql-kotlin 仅在生成和编译代码时被依赖
    ormCompileOnly = true

    ...省略其它配置...
}

...省略其他代码...
```

:::

## 确认代码生成成功

如果代码生成成功，开发人员在编译输出目录中应该看到如下文件结构

|Java用户应该看到的目录结构|Kotlin用户应该看到的目录结构|
|---|---|
|`GeneratedJava`（官方页面含交互演示）|`GeneratedKt`（官方页面含交互演示）|

:::note
这里的截图来自gradle项目。

maven项目大同小异，顶级目录应该是target，而非build
:::

:::caution
正常情况下，自动生产的代码目录（java/kt包所在的父目录）会被Intellij标记上特殊图标`Generated`（官方页面含交互演示）。

然而，Intellij有一定的概率不对此目录进行标记，这样会导致生成的代码无法被用于后续开发。

如果遇到此问题，请在此目录上右键弹出菜单，选择`Mark Directory As` -> `Generated Sources Root`。
:::

这些生成的代码大致功能如下

<table>
    <thead>
        <tr>
            <th>Java文件</th>
            <th>Kotlin文件</th>
            <th>功能描述</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>XXXDraft.java</td>
            <td>XXXDraft.kt</td>
            <td>不可变对象的可修改代理接口，对象的实现，创建和“修改”对象的方法</td>
        </tr>
        <tr>
            <td>XXXProps.java</td>
            <td rowspan="3">XXXProps.kt</td>
            <td rowspan="3">强类型SQL DSL</td>
        </tr>
        <tr>
            <td>XXXTable.java</td>
        </tr>
        <tr>
            <td>XXXTableEx.java</td>
        </tr>
        <tr>
            <td>XXXFetcher.java</td>
            <td>XXXFetcher.kt</td>
            <td>对象抓取器DSL。Jimmer支持一句话查询任意复杂的数据结构，对象抓取器用于定义该数据结构的形状。</td>
        </tr>
    </tbody>
</table>
