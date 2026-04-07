# Input DTO

Input DTO 用于保存（Save）操作，解决前后端数据传输和空值处理的问题。

## 问题背景

传统 DTO 保存的问题：
1. **孤独表单**：前端只传部分字段，后端不知道哪些字段未设置 vs 设置为 null
2. **空值歧义**：`null` 表示清除字段还是不修改字段？
3. **DTO 转换**：需要手动编写大量 MapStruct 转换代码

## Jimmer 的解决方案

Jimmer 提供：
1. **DTO 语言**：自动生成 Input DTO 类
2. **动态字段**：区分 "未设置" 和 "设置为 null"
3. **原生支持**：Input DTO 可直接用于 Save 指令

## 定义 Input DTO

创建 `.dto` 文件（如 `Book.dto`）：

```dto
input BookInput {
    id
    name
    price
    storeId
    
    // 关联对象的 Input
    authors: AuthorInput[]
}

input AuthorInput {
    id
    firstName
    lastName
}
```

编译后自动生成 Java/Kotlin 类。

## 使用 Input DTO

### 作为保存参数

```java
@PostMapping("/books")
public Book saveBook(@RequestBody BookInput input) {
    // Input DTO 可直接用于 save
    return sqlClient.save(input).getModifiedEntity();
}
```

### Input DTO 的字段状态

每个字段都有三种状态：

| 状态 | 含义 | Save 行为 |
|------|------|----------|
| 未设置 | 字段未被赋值 | 不修改该字段 |
| null | 字段被显式设为 null | 清除该字段（设为 null）|
| 有值 | 字段有具体值 | 更新为该值 |

```java
// 示例
BookInput input = BookInput.builder()
    .id(1L)           // 有值 → 更新 ID=1 的对象
    .name("New Name") // 有值 → 更新 name
    // price 未设置 → 不修改 price
    .price(null)      // 显式 null → 清除 price（设为 null）
    .build();
```

## 关联对象的 Input

### 保存嵌套关联

```dto
input BookInput {
    id
    name
    storeId
    
    // 使用 - 表示排除某些字段
    store: BookStoreInput {
        id
        name
        // website 被排除，保存时不会修改
    }
    
    // 集合关联
    authors: AuthorInput[] {
        id
        firstName
        lastName
    }
}
```

### 指定关联保存模式

```dto
input BookInput {
    id
    name
    
    // static 表示只保存 Book，不级联 store
    static store: BookStoreInput
    
    // flat 表示展开关联的字段到当前对象
    flat store {
        storeId: id
        storeName: name
    }
}
```

| 修饰符 | 说明 |
|--------|------|
| `static` | 只保存当前对象，不级联关联对象 |
| `flat` | 将关联对象的字段平铺到当前 DTO |
| 默认 | 级联保存关联对象 |

## 空值处理详解

### 前端不传字段

```json
{
    "id": 1,
    "name": "Updated Name"
    // price 未传
}
```

后端表现：`price` 处于 "未设置" 状态，**不会修改**原有值。

### 前端传 null

```json
{
    "id": 1,
    "price": null
}
```

后端表现：`price` 被显式设为 null，**会清除**原有值。

### 区别未设置和 null

Java 代码：

```java
BookInput input = BookInput.builder()
    .id(1L)
    .build();

// 检查字段是否设置
if (input.getName().isDefined()) {
    // name 被设置了（可能是值或 null）
}

if (input.getName().isPresent()) {
    // name 有具体值（不是 null）
}
```

## 使用 MapStruct

如果需要手动转换，Jimmer 与 MapStruct 兼容：

```java
@Mapper
public interface BookConverter {
    
    default Book toBook(BookInput input) {
        return BookDraft.$.produce(book -> {
            if (input.getId().isPresent()) {
                book.setId(input.getId().get());
            }
            if (input.getName().isPresent()) {
                book.setName(input.getName().get());
            }
            // ...
        });
    }
}
```

## 最佳实践

| 实践 | 说明 |
|------|------|
| Input 与 View 分离 | Input DTO 用于保存，View DTO 用于查询展示 |
| 按需包含字段 | 只包含允许前端修改的字段 |
| 使用 flat 展开 | 避免嵌套太深，前端更容易处理 |
| 明确修饰符 | 对关联使用 `static` 或 `flat`，避免意外级联 |
