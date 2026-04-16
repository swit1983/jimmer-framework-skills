# DTO 语言

## 1. 概念

### 1.1 痛点

Jimmer 动态实体可以解决很大一部分 DTO 爆炸问题，一般情况下不需要定义输出型 DTO。

然而，并非所有 DTO 类型都能被消灭：**输入型 DTO** 很难去除（API 明确性和系统安全性要求静态输入类型）。

DTO 语言的目的：让无法消灭的 DTO 变得极其廉价。

### 1.2 能做什么

编译时根据 DTO 定义自动生成：

1. 静态 DTO 类型
2. DTO 和 Jimmer 动态实体之间的相互转换逻辑
3. 与 DTO 形状完全契合的对象抓取器

开发效率远高于手动编写，也远快于 MapStruct。

### 1.3 应用场景

| 场景 | 说明 | 是否必需 |
|------|------|----------|
| 查询返回结果 | 充当输出 DTO，生成的类型实现 `View<E>` | 可选，非必要不推荐（直接用动态实体即可） |
| 保存 API 输入 | 充当输入参数，生成的类型实现 `Input<E>` | **必需** |
| 超级 QBE 查询 | 作为查询参数，生成的类型实现 `Specification<E>` | 可选，推荐使用 |

## 2. 创建文件

DTO 语言文件扩展名为 `.dto`，放在 `src/main/dto` 目录下（不是 resources）。

编译完成后这些文件就没有价值了，不需要打包到最终产物。

### 2.1 两种创建文件的方法

假设存在 Jimmer 实体 `com.yourcompany.yourproject.Book`：

**方法一：不使用 export 语句**

dto 文件的目录需要和原始实体的包对应，名称需要和原始实体的名称对应：

1. 在 `src/main/dto` 目录下建立目录 `com/yourcompany/yourproject`
2. 在该目录中创建 `Book.dto` 文件

**方法二：使用 export 语句**

dto 文件目录和名称是随意的，在文件内部使用 `export` 语句定义对应关系：

```dto
export com.yourcompany.yourproject.Book
// 可选：自定义生成代码的包名
export com.yourcompany.yourproject.Book -> package com.yourcompany.yourproject.mydto
```

### 2.2 注意事项

- **Java 项目**：如果当前项目并非定义实体的项目，需要在当前项目找一个类用 `@org.babyfish.jimmer.sql.EnableDtoGeneration` 修饰，否则 DTO 文件不会被编译
- dto 文件由 Jimmer 的 APT（Java）或 KSP（Kotlin）编译。如果只修改了 dto 文件而没有修改其他 Java/Kotlin 文件，需要显式 **rebuild** 才能触发重新编译
- Gradle 用户可使用社区插件 [jimmer-gradle](https://github.com/Enaium/jimmer-gradle) 简化此问题

## 3. view、input 和 specification

```dto
// Output DTO - 实现 View<E>
BookView {
    #allScalars
}

// Input DTO - 实现 Input<E>
input BookInput {
    #allScalars
}

// Specification DTO - 实现 Specification<E>，用于超级 QBE 查询
specification BookSpecification {
    #allScalars
}
```

### 3.1 view 和 input 共有的功能

- 以原始实体为参数的构造方法：实体 → DTO
- `toEntity()`：DTO → 实体

```java
Book entity = ...;
BookView dto = new BookView(entity);   // 实体 -> DTO
Book anotherEntity = dto.toEntity();   // DTO -> 实体
```

### 3.2 input 特有功能

- 如果实体 id 配置了自动增长策略，input DTO 中的 id 属性默认可 null
- 可通过 `id!` 强制非 null，或通过 `-id` 去掉 id 属性
- 只能定义可保存的属性（简单属性、普通 ORM 关联、id-view），不能定义 transient、公式属性、计算属性、远程关联
- 对 nullable 属性有强大的全面支持

### 3.3 specification 特有功能

- 不提供与实体互转的能力
- 所有属性默认可 null
- 用于支持[超级 QBE 查询](../../query/super_qbe)

## 4. 简单属性

```dto
BookView {
    id
    name
    edition
}
```

## 5. `#allScalars` 宏

自动映射标量属性（包括继承的）：

```dto
// 映射所有标量属性（含继承的）
BookView {
    #allScalars
}

// 只映射当前类型定义的标量，不包含继承的
BookView {
    #allScalars(this)
}

// 映射特定超类型的属性
BookView {
    #allScalars(TenantAware)
}

// 多个类型组合
BookView {
    #allScalars(this, TenantAware, CommonEntity)
}
```

| 宏表达式 | 自动映射的属性 |
|---------|---------------|
| `#allScalars` | createdTime, modifiedTime, tenant, id, name, edition, price |
| `#allScalars(this)` | id, name, edition, price |
| `#allScalars(TenantAware)` | tenant |
| `#allScalars(CommonEntity)` | createdTime, modifiedTime |
| `#allScalars(this, TenantAware)` | tenant, id, name, edition, price |

> `#allScalars` 必须是第一个属性，否则编译报错。每个参数必须是当前实体或其基类型。

## 6. 负属性

负属性可以去掉某些属性：

```dto
BookView {
    #allScalars
    -tenant  // 去掉 tenant
}
```

## 7. 可空性

默认：

- `specification` 所有属性默认可 null
- 其他：可空性和实体原始属性一致

### 7.1 `?` 修饰符

让属性可 null（原属性非 null 才能用）：

```dto
input UpdateBookInput {
    #allScalars
    price?  // 让 price 可 null
}

// 让所有自动映射的非关联属性全部可 null
input UpdateBookInput {
    #allScalars?
}
```

> Specification 类型不允许使用 `?`。

### 7.2 `!` 修饰符

让属性非 null（原属性可 null 才能用）：

```dto
// 场景 1：input 中修饰 id 属性（覆盖自动增长的默认 nullable）
input BookUpdateInfo {
    #allScalars
    id!
}

// 场景 2：unsafe input 中修饰关联属性
unsafe input BookUpdateInfo {
    #allScalars
    store! {
        ...
    }
}

// 场景 3：specification 中让可 null 属性变为非 null
specification BookSpecification {
    #allScalars
    edition!
}
```

## 8. 重命名

### 8.1 单个属性重命名

```dto
BookView {
    name as bookName
}
```

### 8.2 多个属性重命名（as 块）

```dto
RobotView {
    // 添加前缀：^ 表示起始位置
    as(^ -> oldSystem) {
        prop1
        prop2
    }
    // 添加后缀：$ 表示结尾位置
    as($ -> ForNewSystem) {
        prop3
        prop4
    }
}
```

`as()` 块完整用法：

| 代码 | 作用 |
|------|------|
| `as(^ -> prefix)` | 添加前缀 |
| `as(^prefix ->)` | 删除前缀 |
| `as(^prefix -> newPrefix)` | 替换前缀 |
| `as($ -> suffix)` | 添加后缀 |
| `as($suffix ->)` | 删除后缀 |
| `as($suffix -> newSuffix)` | 替换后缀 |
| `as(infix -> )` | 删除任何位置的内容 |
| `as(infix -> newInfix)` | 替换任何位置的内容 |

> `^` 和 `$` 不能同时出现。`as()` 块中可以定义 `#allScalars` 等绝大部分属性。

### 8.3 关联属性重命名

`as` 子句应在关联定义块**之前**：

```dto
input CompositeBookInput {
    authors as authorList {
        #allScalars(Author)
    }
}
```

## 9. 枚举映射

```dto
// 映射为数字
AuthorView {
    #allScalars
    gender -> { MALE: 100, FEMALE: 101 }
}

// 映射为字符串
AuthorView {
    #allScalars
    gender -> { MALE: "Male", FEMALE: "Female" }
}
```

## 10. 关联属性

### 10.1 普通关联

```dto
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

### 10.2 递归关联（自关联树形结构）

```dto
TreeNodeView {
    #allScalars
    childNodes*  // 星号表示递归
}
```

### 10.3 id 函数

只获取关联 id，不需要完整对象：

```dto
BookView {
    id(store)         // 得到 storeId
    id(authors) as authorIds  // 得到 authorIds（重命名）
}
```

> 这是比假设实体声明了 `@IdView` 属性更好的方案。

### 10.4 flat 函数

把关联属性平摊到当前 DTO，得到不含关联的平坦对象。

**限制**：
- 对 view 和 input：只能用于引用关联（一对一/多对一），**不能**用于集合关联（一对多/多对多）
- 对 specification：无此限制，可用于集合关联

```dto
FlatTreeNodeView {
    #allScalars(TreeNode)  // id, name
    flat(parent) {
        as(^ -> parent) {  // 用 as 避免 id/name 冲突
            #allScalars(TreeNode)  // parentId, parentName
        }
    }
}
// 得到：id, name, parentId, parentName
```

可嵌套使用：

```dto
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
// 得到：id, name, parentId, parentName, grandParentId, grandParentName
```

> 对 output DTO 而言，`flat` 方案有争议，不推荐盲目采用。

## 11. 自定义字段

自定义属性需要指定类型：

```dto
BookInput {
    #allScalars(Author)? -id
    remark: String  // 自定义字段
}
```

### 11.1 内置类型

| DTO 语言类型 | 生成的 Java 类型 | 生成的 Kotlin 类型 |
|-------------|-----------------|-------------------|
| `Boolean` / `Boolean?` | `boolean` / `Boolean` | `Boolean` / `Boolean?` |
| `Int` / `Int?` | `int` / `Integer` | `Int` / `Int?` |
| `Long` / `Long?` | `long` / `Long` | `Long` / `Long?` |
| `String` | `String` | `String` |
| `Any` | `Object` | `Any` |
| `Array<T>` | `T[]` | `TArray` |
| `List<E>` | `List<? extends E>` | `List<E>` |
| `Set<E>` | `Set<? extends E>` | `Set<E>` |
| `Map<K, V>` | `Map<? extends K, ? extends V>` | `Map<K, V>` |

### 11.2 范型参数修饰

| DTO 语言类型 | 生成的 Java 类型 | 生成的 Kotlin 类型 |
|-------------|-----------------|-------------------|
| `UserType1<UserType2>` | `UserType1<UserType2>` | `UserType1<UserType2>` |
| `UserType1<out UserType2>` | `UserType1<? extends UserType2>` | `UserType1<out UserType2>` |
| `UserType1<in UserType2>` | `UserType1<? super UserType2>` | `UserType1<in UserType2>` |

### 11.3 导入语句

```dto
// 单类导入
import java.time.LocalDateTime

// 单类重命名导入
import java.time.LocalDateTime as LDT

// 多类导入
import java.time.{
    LocalDateTime,
    OffsetDateTime,
    ZonedDateTime
}

// 多类重命名导入
import java.time.{
    LocalDateTime as LDT,
    OffsetDateTime as ODT,
    ZonedDateTime as ZDT
}
```

## 12. 注解

可以给 DTO 类型、映射属性、自定义属性添加注解：

```dto
import javax.validation.constraints.Size

@Doc("Book input")
BookInput {
    #allScalars

    @Size(min = 4, max = 20)
    name

    @Size(min = 10, max = 50)
    remark: String
}
```

- 非 Jimmer 注解会自动从实体属性复制到 DTO 属性
- 注解中的 `value` 参数可以简写
- 混合 Java 和 Kotlin 的注解语法（`{}` 和 `[]` 数组语法都支持）

## 13. 实现接口

DTO 类型可以实现已有的 Java/Kotlin 接口（前提：接口的抽象属性是 DTO 属性的子集）：

```dto
export com.yourcompany.yourproject.Book -> com.yourcompany.yourproject.model.dto
import com.yourcompany.yourcompany.common.{Shape1, Shape2}

BookView implements Shape1<String>, Shape2 {
    #allScalars
    store implements Shape3<String>, Shape4 {
        #allScalars
    }
}
```

编译后生成的类型会实现指定接口，覆盖的属性会加 `@Override`（Java）或 `override`（Kotlin）。

## 14. 相关链接

- [在 Input DTO 中处理空值](../../api/save-command.md)
- [超级 QBE 查询](../../query/super_qbe.md)
