# 创建项目

> 来源: https://jimmer.deno.dev/zh/docs/quick-view/get-started/create-project

* [快速预览 ★](/zh/docs/quick-view/)
* [快速上手](/zh/docs/quick-view/get-started/)
* 创建项目

本页总览

# 创建项目

## 创建SpringBoot项目[​](#创建springboot项目 "创建SpringBoot项目的直接链接")

使用<https://start.spring.io/>创建一个SpringBoot项目，你需要选择你喜欢的语言：Java或Kotlin。

项目的语言的选择很重要，Jimmer支持两套不同的API。

* Java API: 保证尽可能多的开发人员可以使用
* Kotlin API: 专为kotlin语言设计的API，尽可能利用kotlin的语言特性优化开发体验

## 加入依赖[​](#加入依赖 "加入依赖的直接链接")

Jimmer本身是高度中立的，可以脱离spring-boot使用；但同时也提供了良好的spring-boot集成

用户既可以选择和spring-boot结合使用，也可以单独使用。不同的选择需要添加的依赖不同。

* groupId:
  org.babyfish.jimmer
* artifactId:

  |  | 和SpringBoot结合使用 | 单独使用 |
  | --- | --- | --- |
  | Java | jimmer-spring-boot-starter | jimmer-sql |
  | Kotlin | jimmer-sql-kotlin |

相比于独立使用而言，和Spring-Boot结合使用更简单，因此本教程一律使用`jimmer-spring-boot-starter`进行演示。

修改gradle或maven文件，加入依赖

信息

请把下面代码中的`TODO: latest.version`替换为真正的版本号。

请参见<https://github.com/babyfish-ct/jimmer/releases>
，从中找到最新的release，去掉前面的`v`就得到了真正的版本号。
例如，此页面中最后一个release为`v0.9.64`, 那么真正的版本号就是`0.9.64`。

* Maven
* Gradle
* Gradle(kts)

pom.xml

```
<properties>  
    <jimmer.version>TODO: latest.version</jimmer.version>  
</properties>  
  
<dependencies>  
    <dependency>  
        <groupId>org.babyfish.jimmer</groupId>  
        <artifactId>jimmer-spring-boot-starter</artifactId>  
        <version>${jimmer.version}</version>  
    </dependency>  
    ...省略其他依赖...  
</dependencies>
```

build.gradle

```
ext {  
    jimmerVersion = "TODO: latest.version"  
}  
  
dependencies {  
      
    implementation "org.babyfish.jimmer:jimmer-spring-boot-starter:${jimmerVersion}"  
  
    ...省略其他依赖...  
}
```

build.gradle.kts

```
val jimmerVersion = "TODO: latest.version"  
  
dependencies {  
      
    implementation "org.babyfish.jimmer:jimmer-spring-boot-starter:${jimmerVersion}"  
  
    ...省略其他依赖...  
}
```

信息

作为手把手的例子，这里仅展示Jimmer标准的构建方式。

社区也提供了gradle插件，请参考[APT/KSP](/zh/docs/overview/apt-ksp)

## kotlin需要的额外配置[​](#kotlin需要的额外配置 "kotlin需要的额外配置的直接链接")

如果开发人员选择了kotlin，需要修改spring-boot的配置文件，这点非常重要。

注意

Kotlin项目必须配置`jimmer.language`

* application.properties
* application.yml

```
jimmer.language = kotlin
```

```
jimmer:  
    language: kotlin
```

## 其他有用的配置[​](#其他有用的配置 "其他有用的配置的直接链接")

除了kotlin项目必需的`jimmer.language`外，还有其他配置。

这里再介绍几个很用的基本配置，对Java和Kotlin都有效

| 属性名 | 类型 | 默认值 | 描述 |
| --- | --- | --- | --- |
| jimmer.dialect | string | org.babyfish.jimmer.sql.dialect.DefaultDialect | 数据库方言类名 |
| jimmer.show-sql | boolean | false | 如果为true，自动打印被执行的SQL |
| jimmer.pretty-sql | boolean | false | E确保打印的sql是格式良好的 *(默认是紧凑的)* |
| jimmer.database-validation-mode | NONE|WARNING|ERROR | NONE | 如果非NONE，验证数据库结构和代码实体类型结构的一致性，如果不一致，WARNING导致日志告警，ERROR导致报错 |

完整的配置，请参考[Spring篇/附录](/zh/docs/spring/appendix)

* application.properties
* application.yml

```
jimmer.dialect = org.babyfish.jimmer.sql.dialect.MySqlDialect  
jimmer.show-sql = true  
jimmer.pretty-sql = true  
jimmer.database-validation-mode = ERROR  
...省略其他配置...
```

```
jimmer:  
    dialect: org.babyfish.jimmer.sql.dialect.MySqlDialect  
    show-sql: true  
    pretty-sql: true  
    database-validation-mode: ERROR  
    ...省略其他配置...
```

## 添加除Jimmer外的其他必要依赖[​](#添加除jimmer外的其他必要依赖 "添加除Jimmer外的其他必要依赖的直接链接")

除了Jimmer外，还需要一些其他必要的依赖，比如spring-web，JDBC驱动。

修改gradle或maven文件，加入依赖

* Maven
* Gradle
* Gradle(kts)

pom.xml

```
<dependencies>  
    <dependency>  
        <groupId>org.springframework.boot</groupId>  
        <artifactId>spring-boot-starter-web</artifactId>  
    </dependency>  
    <dependency>  
        <groupId>mysql</groupId>  
        <artifactId>mysql-connector-java</artifactId>  
        <version>8.0.30</version>  
        <scope>runtime</scope>  
    </dependency>  
    ...省略其他依赖...  
</dependencies>
```

build.gradle

```
dependencies {  
      
    implementation 'org.springframework.boot:spring-boot-starter-web'  
    runtimeOnly 'mysql:mysql-connector-java:8.0.30'  
  
    ...省略其他依赖...  
}
```

build.gradle.kts

```
dependencies {  
      
    implementation("org.springframework.boot:spring-boot-starter-web")  
    runtimeOnly("mysql:mysql-connector-java:8.0.30")  
  
    ...省略其他依赖...  
}
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/get-started/create-project.mdx)

最后于 **2025年9月16日** 更新