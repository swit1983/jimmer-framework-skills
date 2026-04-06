# DTO 语言示例

Jimmer 提供原生 DTO 语言，可以极低成本生成 DTO 类型。

## 基本用法

在 `src/main/dto` 目录创建 `.dto` 文件：

```dto
// com/example/dto/BookDTO.dto
package com.example.dto

import com.example.model.Book

dto BookDTO {
    // 选择实体的所有简单属性
    allScalarFields(Book)
    
    // 选择关联的 store，只需要 id 和 name
    store {
        id
        name
    }
    
    // 选择关联的 authors，只需要 id 和 fullName
    authors {
        id
        fullName
    }
}
```

编译后会自动生成 Java/Kotlin 代码，可以和 Jimmer 实体自动互相转换。

## 根据实体自动推导

```dto
package com.example.dto

import com.example.model.TreeNode

// 递归树
dto TreeNodeDTO {
    allScalarFields(TreeNode)
    children recursive
}
```

## 与实体转换

```java
// 实体 -> DTO
Book book = ...;
BookDTO dto = new BookDTO(book);

// DTO -> 实体
BookDTO dto = ...;
Book book = dto.toEntity();
```

## 优点对比传统方式

| 特性 | Jimmer DTO | 手工编写 | MapStruct |
|--------|-----------|----------|-----------|
| 代码量 | 极少 | 多 | 中 |
| 编译时检查 | 支持 | 支持 | 支持 |
| 自动关联 | 支持 | 不支持 | 不支持 |
| 递归支持 | 原生支持 | 需要手工 | 需要手工 |

## ⚠️ 注意事项

- DTO 文件放在 `src/main/dto` 目录
- 编译时会自动生成 Java/Kotlin 代码到 `target/generated-sources/`
- 可以和 Jimmer 的 Fetch 语法结合使用

