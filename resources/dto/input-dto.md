# Input DTO

Input DTO 用于保存（Save）操作，解决前后端数据传输和空值处理的问题。

## 问题背景

直接用 `@RequestBody` 接受 Jimmer 动态对象作为 API 入参存在两个问题：

1. **安全性**：客户端可以传入任意字段，包括不应被修改的敏感字段
2. **API 模糊性**：调用者无法从方法签名知道需要提交哪些字段

因此，必须定义静态的 Input DTO 类型作为 API 入参。

## Jimmer 的方案

Jimmer 提供 **DTO 语言**，通过 `.dto` 文件自动生成 Input DTO 类及与动态实体的互转逻辑：

1. 创建 `src/main/dto/Book.dto` 文件
2. 用 DTO 语言定义 Input 形状
3. 编译后自动生成 Java/Kotlin 类
4. 生成的 Input DTO 可直接用于 `save` 指令

## 定义 Input DTO

### 文件创建

假设实体为 `com.yourcompany.yourproject.Book`：

1. 创建 `src/main/dto/com/yourcompany/yourproject/Book.dto`
2. 或使用 `export` 语句：

```dto
export com.yourcompany.yourproject.Book

input BookInput {
    #allScalars(Book)
    id(store)
    authors {
        #allScalars(Author)
        -id
    }
}
```

编译后自动生成 `BookInput` 类，实现 `org.babyfish.jimmer.Input<Book>` 接口。

### 基本用法

```java
@PostMapping("/books")
public Book saveBook(@RequestBody BookInput input) {
    // Input DTO 可直接用于 save
    return sqlClient.save(input).getModifiedEntity();
}
```

## Input DTO 的字段状态

每个字段都有三种状态：

| 状态 | JSON 表现 | Save 行为 |
|------|----------|----------|
| 未设置 | 字段不出现在 JSON 中 | 不修改该字段 |
| null | `"price": null` | 清除该字段（设为 null）|
| 有值 | `"price": 99.5` | 更新为该值 |

```json
// price 未传 → 不修改 price
{ "id": 1, "name": "Updated Name" }

// price 显式传 null → 清除 price
{ "id": 1, "price": null }
```

生成的 Input DTO 提供 `isDefined()` 和 `isPresent()` 方法检查字段状态：

```java
BookInput input = ...;

if (input.getName().isDefined()) {
    // name 被设置了（可能是值或 null）
}

if (input.getName().isPresent()) {
    // name 有具体值（不是 null）
}
```

## 关联对象的 Input

### 内嵌匿名类型

关联对象使用内嵌块定义（不是 `: Type` 语法）：

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

### id 函数

只需关联 id 而不需要完整对象时，使用 `id()` 函数：

```dto
input BookInput {
    #allScalars(Book)
    id(store)         // 得到 storeId
    id(authors) as authorIds  // 得到 authorIds
}
```

### flat 函数

将关联属性平摊到当前 DTO：

```dto
input FlatBookInput {
    #allScalars(Book)
    flat(store) {
        as(^ -> store) {
            #allScalars(BookStore)
        }
    }
}
// 得到：id, name, price, storeId, storeName
```

> `flat` 对 view/input 只能用于一对一/多对一关联，不能用于集合关联。

## 空值处理

### `?` 修饰符

让原本不可 null 的属性变为可 null：

```dto
input UpdateBookInput {
    #allScalars?  // 所有自动映射的非关联属性全部可 null
}
```

### `!` 修饰符

让原本可 null 的属性变为非 null：

```dto
input BookUpdateInfo {
    #allScalars
    id!  // 覆盖自动增长策略导致的默认 nullable
}
```

### `unsafe input`

允许将关联属性从 nullable 改为非 null：

```dto
unsafe input BookUpdateInfo {
    #allScalars
    store! {
        #allScalars(BookStore)
    }
}
```

> 如果实体对象中关联属性值为 null，转化为该 Input DTO 时会抛出异常。

## 生成接口

Input DTO 自动实现 `org.babyfish.jimmer.Input<E>` 接口：

```java
public interface Input<E> {
    E toEntity();
}
```

实现此接口的 POJO 可以直接传给 `save` 方法：

```java
// 底层 API
sqlClient.save(input);

// Spring Data
bookRepository.save(input);
```

## 与 MapStruct 配合使用

如果已有静态 POJO 类型（非 DTO 语言生成），Jimmer 扩展了 MapStruct 支持静态 POJO 与动态实体的互转。需要引入 `jimmer-mapstruct-apt`（Java 包含在 `jimmer-apt` 中，Kotlin 需额外添加）。

详见 [MapStruct 文档](mapstruct.md)。

## 最佳实践

| 实践 | 说明 |
|------|------|
| 优先使用 DTO 语言 | 比手写 DTO + MapStruct 更高效 |
| `#allScalars` 按需排除 | 用 `-field` 去掉不允许前端修改的字段 |
| 关联使用 `id()` | 只需关联 id 时用 `id(store)` 而非完整嵌套对象 |
| 明确字段状态 | 前端利用 "未设置" vs "null" 区分部分更新与清除 |
| Input 与 View 分离 | Input DTO 用于保存，View DTO 用于查询展示 |
