# MapStruct

Jimmer 扩展了 [MapStruct](https://mapstruct.org/)，支持将静态 POJO 与 Jimmer 动态实体相互转换。

## 注意事项

Jimmer 的实体对象是动态的（惰性属性），这是早期 MapStruct 未曾考虑的模式。MapStruct 从 `1.6.0` 开始支持此行为，请尽可能使用 1.6.0 或更高版本。

## 优缺点

### 优点

- 可实现任意复杂的转换逻辑（DTO 语言适合固定转换逻辑）
- 可整合现有的 DTO 类型（DTO 语言生成全新类型）

### 缺点

- **开发成本高**：DTO 语言为 Jimmer 量身定制，开发效率更高
- **不适合 Output DTO**：DTO 语言自动生成的类型具备内置对象抓取器，手动定义的 DTO 没有
- **Kotlin 风险**：MapStruct 基于 `apt`，Kotlin 需使用 `kapt`（已废弃，Kotlin 主张用 `ksp`）

## 依赖配置

### Java（Maven）

```xml
<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
</dependencies>

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
                <path>
                    <groupId>org.mapstruct</groupId>
                    <artifactId>mapstruct-processor</artifactId>
                    <version>${mapstruct.version}</version>
                </path>
            </annotationProcessorPaths>
        </configuration>
    </plugin>
</plugins>
```

### Java（Gradle）

```groovy
dependencies {
    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.babyfish.jimmer:jimmer-apt:${jimmerVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
}
```

### Kotlin（Gradle）

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    kotlin("kapt") version "1.7.10"
}

dependencies {
    implementation("org.mapstruct:mapstruct:${mapstructVersion}")
    ksp("org.babyfish.jimmer:jimmer-ksp:${jimmerVersion}")
    kapt("org.mapstruct:mapstruct-processor:${mapstructVersion}")
    kapt("org.babyfish.jimmer:jimmer-mapstruct-apt:${jimmerVersion}")
}
```

> Java 中 `jimmer-mapstruct-apt` 已包含在 `jimmer-apt` 中，Kotlin 需额外添加。

## 定义 POJO

```java
@Data
public class BookInput {
    @Nullable
    private Long id;           // 可 null（插入时不需要 id）
    private String name;
    private int edition;
    private BigDecimal price;
    @Nullable
    private Long storeId;      // 短关联 id（对应 Book.store）
    private List<Long> authorIds;  // 短关联 id 集合（对应 Book.authors）
}
```

## 定义 Mapper

### 实体已定义 `@IdView` 属性

如果实体中已定义了 `@IdView` 属性（如 `storeId()`、`authorIds()`），POJO 与实体完全对应：

```java
@Mapper
public interface BookInputMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Book toBook(BookInput input);
}
```

### 实体未定义 `@IdView` 属性

需要额外定义 id → 实体的转换：

```java
@Mapper
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

## 使用

```java
BookInput input = ...;
BookInputMapper mapper = Mappers.getMapper(BookInputMapper.class);
Book book = mapper.toBook(input);
```

## 实现 `Input<E>` 接口

让 POJO 实现 `org.babyfish.jimmer.Input<E>` 接口，可利用语法层面的便利（`save` 方法直接接受 `Input` 参数）：

```java
@Data
public class BookInput implements Input<Book> {
    private static final Converter CONVERTER =
        Mappers.getMapper(Converter.class);

    @Nullable
    private Long id;
    private String name;
    // ... 其他字段 ...

    @Override
    public Book toEntity() {
        return CONVERTER.toBook(this);
    }

    @Mapper
    interface Converter {
        @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
        Book toBook(BookInput input);
    }
}
```

之后可直接：

```java
sqlClient.save(input);  // 内部自动调用 input.toEntity()
```
