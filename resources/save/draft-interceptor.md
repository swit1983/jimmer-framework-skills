# APT/KSP 配置指南

> 本文档介绍 Jimmer 的预编译技术配置：Java 使用 APT，Kotlin 使用 KSP。

---

## 基本概念

Jimmer 高度依赖于 JVM 生态的预编译技术：

| 语言 | 技术 | 说明 |
|------|------|------|
| **Java** | **APT** (Annotation Processor Tool) | [IntelliJ IDEA 文档](https://www.jetbrains.com/help/idea/annotation-processors-support.html) |
| **Kotlin** | **KSP** (Kotlin Symbol Processing) | [Kotlin 官方文档](https://kotlinlang.org/docs/ksp-overview.html) |

### 为什么需要预编译？

使用 APT/KSP 自动生成的代码，是**使用 Jimmer 所必须的**。例如：

- `BookDraft` - 用于创建和修改实体
- `BookTable` / `BookTableEx` - 用于 SQL DSL 查询
- `BookFetcher` - 用于对象抓取器
- DTO 类 - 根据 `.dto` 文件生成

### 首次打开项目的注意事项

使用 IntelliJ IDEA 打开 Jimmer 项目时，可能会发现自动生成的代码不存在。解决方法：

**方法一**：先用命令行编译
```bash
# Maven
./mvnw install

# Gradle
./gradlew build
```
然后再用 IntelliJ 打开项目。

**方法二**：直接用 IntelliJ 打开，暂时无视 IDE 的错误，等待依赖下载完毕后，直接运行项目的 main 方法或单元测试。所有 IDE 错误将会自动消失。

---

## 如何使用

### 方法一：使用 Jimmer 标准构建方式

#### Java (Maven)

```xml
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
```

#### Java (Gradle)

```groovy
dependencies {
    annotationProcessor "org.babyfish.jimmer:jimmer-apt:${jimmerVersion}"
}
```

#### Kotlin (Gradle.kts)

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

dependencies {
    ksp("org.babyfish.jimmer:jimmer-ksp:${jimmerVersion}")
}
```

### 方法二：使用社区提供的 Gradle 插件

[gradle-plugin-jimmer](https://github.com/ArgonarioD/gradle-plugin-jimmer)

```groovy
plugins {
    id "tech.argonariod.gradle-plugin-jimmer" version "latest.release"
}

jimmer {
    version = "${jimmerVersion}"
}
```

---

## 在哪使用

业务项目通常是多模块结构，不同子项目的配置不同：

| 子项目类型 | 使用目的 | 注意事项 |
|-----------|----------|----------|
| **定义实体的项目** | 生成 Draft、SQL DSL、Fetcher | 必须配置 |
| **定义 DTO 的项目** | 根据 `.dto` 文件生成 DTO 类型 | Java 项目需要 `@EnableDtoGeneration` |
| **Spring Web 项目** | 生成 OpenAPI 文档、TypeScript 代码 | 支持远程异常 |

---

## Java 代码风格

Java 和 Kotlin 的抽象能力不同，导致 API 设计风格差异：

### 对比

| 功能 | Java | Kotlin |
|------|------|--------|
| **Draft** | 使用生成的类型 `BookDraft` | 使用原实体类型 `Book` |
| **SQL DSL** | 使用生成的类型 `BookTable` | 使用原实体类型 `Book` |
| **Fetcher** | 使用生成的类型 `BookFetcher` | 使用原实体类型 `Book` |

### Java 代码示例

```java
// Draft
Book book = BookDraft.$.produce(b -> {
    b.setName("SQL in Action");
    b.addIntoAuthors(a -> a.setName("Jessica"));
    b.addIntoAuthors(a -> a.setName("Bob"));
});

// SQL DSL
BookTable table = BookTable.$;
List<Book> books = sqlClient
    .createQuery(table)
    .where(table.storeId().isNull())
    .orderBy(table.name())
    .select(table)
    .execute();

// Fetcher
Fetcher<Book> fetcher = BookFetcher.$
    .allScalarFields()
    .store(BookStoreFetcher.$.allScalarFields())
    .authors(AuthorFetcher.$.allScalarFields());
```

### Kotlin 代码示例

```kotlin
// Draft
val book = Book {
    name = "SQL in Action"
    authors().addBy { name = "Jessica" }
    authors().addBy { name = "Bob" }
}

// SQL DSL
val books = sqlClient
    .createQuery(Book::class) {
        where(table.storeId.isNull())
        orderBy(table.name)
        select(table)
    }
    .execute()

// Fetcher
val fetcher = newFetcher(Book::class).by {
    allScalarFields()
    store { allScalarFields() }
    authors { allScalarFields() }
}
```

### 两种 Java 代码风格

考虑到部分 Java 开发人员对 `$` 存在主观偏见，Jimmer 的 APT 还生成 4 个汇总类型：

| 汇总类型 | 说明 |
|----------|------|
| `Objects` 类 | 创建 Draft 的另一种方式 |
| `Tables` 接口 | SQL DSL 表定义 |
| `TableExes` 接口 | 扩展表定义 |
| `Fetchers` 接口 | Fetcher 定义 |

#### 风格对比

| 接受 `$` 的风格 | 不接受 `$` 的风格 |
|-----------------|-------------------|
| `BookDraft.$.produce` | `Immutables.createBook` |
| `BookTable.$` | `Tables.BOOK_TABLE` |
| `BookTableEx.$` | `TableExes.BOOK_TABLE_EX` |
| `BookFetcher.$` | `Fetchers.BOOK_FETCHER` |

#### 使用接口简化代码

```java
public interface FetcherConstants implements Fetchers {
    Fetcher<Book> BOOK_DETAIL_FETCHER = BOOK_FETCHER
        .allScalarFields()
        .store(BOOK_STORE_FETCHER.allScalarFields())
        .authors(AUTHOR_FETCHER.allScalarFields());
}
```

---

## 与 Lombok 配合

Java 项目常常和 Lombok 配合使用。

### 默认情况

如果项目除了 Lombok 外没有其他 APT，只需导入 Lombok 的依赖即可。

### 引入其他 APT 时

一旦引入了其他 APT 配置（不一定是 Jimmer 的 APT，任何其他 APT），则**必须明确配置 Lombok 的 APT**。

#### Maven 配置

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
            <configuration>
                <annotationProcessorPaths>
                    <!-- 注意顺序：Lombok 在最前面 -->
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
```

#### Gradle 配置

```groovy
dependencies {
    annotationProcessor 'org.projectlombok:lombok:${lombokVersion}'
    annotationProcessor 'org.babyfish.jimmer:jimmer-apt:${jimmerVersion}'
}
```

**注意**：Gradle 不需要像 Maven 那样特别指定顺序。

---

## 常见问题

### Q: 为什么打开项目后看不到自动生成的代码？

**A**: 使用 IntelliJ IDEA 打开 Jimmer 项目时，可能会发现自动生成的代码不存在。解决方法：

**方法一**：先用命令行编译
```bash
# Maven
./mvnw install

# Gradle
./gradlew build
```
然后再用 IntelliJ 打开项目。

**方法二**：直接用 IntelliJ 打开，暂时无视 IDE 的错误，等待依赖下载完毕后，直接运行项目的 main 方法或单元测试。所有 IDE 错误将会自动消失。

### Q: Kotlin 为什么只能用 Gradle？

**A**: KSP 官方只支持 Gradle。虽然第三方提供了 KSP 的 Maven 插件，但版本迭代跟不上 Kotlin/KSP 本身的更新，经常遇到兼容性问题。因此 Jimmer 放弃了对 Kotlin 的 Maven 支持，请 Kotlin 开发者使用 Gradle。

### Q: Java 项目应该选择 Maven 还是 Gradle？

**A**: Jimmer 官方例子中的 Java 项目同时提供了 `pom.xml` 和 `build.gradle`，即 Maven/Gradle 双支持。可以根据团队习惯选择。

但 IntelliJ 对 Maven 引入的 annotation processor 的整合存在一些过度优化措施，导致 Gradle 和 IDE 配合的开发体验优于 Maven。

### Q: APT 和 KSP 有什么区别？

**A**: 

| 对比项 | APT | KSP |
|--------|-----|-----|
| 语言 | Java | Kotlin |
| 官方支持 | Java 标准 | Kotlin 官方 |
| 构建工具 | Maven/Gradle | 仅 Gradle |
| 速度 | 标准 | 更快（KSP 设计更高效） |

Jimmer 为 Java 用户提供 APT 支持，为 Kotlin 用户提供 KSP 支持。

---

## 参考

- [Jimmer 官方文档 - APT/KSP](https://jimmer.deno.dev/zh/docs/overview/apt-ksp)
- [IntelliJ IDEA - Annotation Processors](https://www.jetbrains.com/help/idea/annotation-processors-support.html)
- [Kotlin - KSP Overview](https://kotlinlang.org/docs/ksp-overview.html)
