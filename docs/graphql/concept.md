---
title: '基本概念'
---
# 基本概念


## 概念


在[Spring篇](spring)，我们讨论了如何利用Jimmer实现REST服务，并为之自动生成客户端所需的代码，比如TypeScript。


不仅如此，Jimmer还支持另外一种开发模式，构建[GraphQL](https://graphql.org/)服务。这是本系列文章要讨论的内容。


警告

截止到目前为止，GraphQL协议并不支持自关联属性的递归查询。


因此，无法通过GraphQL暴露类似于[对象抓取器的递归查询](query/object-fetcher/recursive)的功能，这是目前使用GraphQL必须接受的功能牺牲。


Jimmer对GraphQL的支持基于[Spring GraphQL](https://spring.io/projects/spring-graphql)实现的。
所以，项目需要同时导入Jimmer和Spring GraphQL的Spring Boot Starter，比如


- Maven
- Gradle
pom.xml

```
...省略其他代码...<build>    <dependencies>        <dependency>            <groupId>org.babyfish.jimmer</groupId>            <artifactId>jimmer-spring-boot-starter</artifactId>            <version>${jimmer.version}</version>        </dependency>        <dependency>            <groupId>org.springframework.boot</groupId>            <artifactId>spring-boot-starter-graphql</artifactId>            <version>${spring.boot.version}</version>        </dependency>        ...省略其他依赖...    </dependencies></build>...省略其他代码...
```

build.gradle

```
dependencies {    implementation "org.babyfish.jimmer:jimmer-spring-boot-starter:${jimmerVersion}"    implementation 'org.springframework.boot:spring-boot-starter-graphql'    ...省略其他依赖...}
```


信息

如果Jimmer实体类型也定义在GraphQL项目中，而并未独立形成另外一个项目，那么预编译器 *(对Java而言，是Annotation Processor；对Kotlin而言，是KSP)* 也配置在构建脚本中。


这部分内容已经在[生成代码](quick-view/get-started/generate-code)一文中被详细论述过，本文不再重复。


[Spring GraphQL](https://spring.io/projects/spring-graphql)是Schema-First方案，而非Code-First方案，
因此，开发人员需要为项目创建文件`src/main/resources/graphql/schema.graphqls`，并在其中定义GraphQL Schema。


这个文件是[Spring GraphQL](https://spring.io/projects/spring-graphql)的要求，其内容GraphQL schema是一种标准的语言，它们都和Jimmer无关，本文不做解释，可参考[附带例子的GraphQL Schema](https://github.com/babyfish-ct/jimmer-examples/blob/main/java/jimmer-sql-graphql/service/src/main/resources/graphql/schema.graphqls)。


## 注意事项


警告

目前版本存在限制，在提供GraphQL服务时，请勿使用[基于SQL的简单计算属性](mapping/advanced/calculated/formula#2-%E5%9F%BA%E4%BA%8Esql%E7%9A%84%E8%AE%A1%E7%AE%97)

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/graphql/concept.mdx)最后 于 **2025年9月16日**  更新
- [概念](#概念)
- [注意事项](#注意事项)