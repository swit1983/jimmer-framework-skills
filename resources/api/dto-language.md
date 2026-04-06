# DTO 语言

## 概念

DTO 语言是 Jimmer 提供的强大功能，用于快速定义 DTO 数据结构，编译时自动生成 Java/Kotlin 代码。

### 解决的痛点

Jimmer 动态实体可以解决很大一部分 DTO 爆炸问题，一般情况下不需要定义输出型 DTO。

然而，并非所有 DTO 类型都能被消灭：
- **输入型 DTO** 很难去除，API 明确性和系统安全性要求静态输入类型

DTO 语言的目的：让无法消灭的 DTO 变得极其廉价。

### 能做什么

编译时根据 DTO 定义自动生成：
1. 静态 DTO 类型
2. DTO 和 Jimmer 动态对象之间的相互转换逻辑
3. 与 DTO 形状完全契合的对象抓取器

开发效率远高于手动编写，也远快于 mapstruct。

### 应用场景

| 场景 | 说明 | 是否必需 |
|------|------|----------|
| 查询返回结果 | 充当输出 DTO | 可选，非必要不推荐（直接用动态实体即可） |
| 保存 API 输入 | 充当输入参数 | **必需** |
| 超级 QBE 查询 | 作为查询参数 | 可选，推荐使用 |

## 文件位置

DTO 语言文件扩展名为 `.dto`，放在 `src/main/dto` 目录下（不是 resources）。

不需要打包到最终产物，编译完成后就没用了。

## DTO 类型

DTO 语言可以定义三种 DTO：

| 类型 | 关键字 | 用途 | 生成接口 |
|------|--------|------|----------|
| View | (默认) | 查询输出 | `View<Entity>` |
| Input | `input` | 保存指令输入 | `Input<Entity>` |
| Specification | `specification` | 超级 QBE 查询参数 | `Specification<Entity>` |

命名建议：
- View 以 `*View` 结尾
- Input 以 `*Input` 结尾
- Specification 以 `*Specification` 结尾

## 基础语法

### 简单属性

```dto
// Book.dto
BookView {
    id
    name
    edition
    price
}
```

### `#allScalars` 宏

自动映射所有标量属性（包括继承的）：

```dto
// 映射所有标量属性
BookView {
    #allScalars
}

// 只映射当前类型定义的标量，不包含继承的
BookView {
    #allScalars(this)
}

// 排除某个属性
BookView {
    #allScalars
    -tenant  // 负属性去掉 tenant
}

// 多个父类型
BookView {
    #allScalars(this, TenantAware, CommonEntity)
}
```

> 注意：`#allScalars` 必须是第一个属性，否则编译报错。

### 可空性

默认：
- `specification` 所有属性默认可 null
- 其他：可空性和实体原始属性一致

修改可空性：
- `price?` → 设为可 null（原属性非 null 才能用）
- `id!` → 设为非 null（原属性可 null 才能用，主要用于 id）

### 重命名

单个属性重命名：
```dto
BookView {
    name as bookName
}
```

批量重命名（添加前缀/后缀）：
```dto
RobotView {
    // ^ 表示开头，添加前缀
    as(^ -> oldSystem) {
        prop1
        prop2
    }
    // $ 表示结尾，添加后缀
    as($ -> ForNewSystem) {
        prop3
        prop4
    }
}
```

支持替换前缀/后缀：
- `as(^prefix -> newPrefix)` → 替换前缀
- `as($suffix -> newSuffix)` → 替换后缀

### 枚举映射

默认还是枚举类型，可以映射为数字或字符串：

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

### 关联属性

**普通关联**：
```dto
CompositeBookInput {
    #allScalars(this) -id
    store {
        #allScalars(BookStore) -id
    }
    authors {
        #allScalars(Author) -id
    }
}
```

**递归关联**（自关联树形结构）：
```dto
TreeNodeView {
    #allScalars
    childNodes*  // 星号表示递归
}
```

**id 函数**（只获取关联 id，不需要完整对象）：
```dto
BookView {
    id(store)         // 得到 storeId
    id(authors) as authorIds  // 得到 authorIds，重命名
}
```

**flat 函数**（把关联属性平摊到当前 DTO）：
```dto
FlatTreeNodeView {
    #allScalars(TreeNode)
    flat(parent) {
        as(^ -> parent) {
            #allScalars(TreeNode)
        }
    }
}
// 得到：id, name, parentId, parentName
```

### 自定义字段

支持自定义非映射属性：

```dto
BookInput {
    #allScalars -id
    remark: String  // 自定义字段需要指定类型
}
```

支持内置类型：
- 原生类型：boolean, char, byte, short, int, long, float, double 等
- String, Any
- 数组：`Type[]`
- 集合：List, Set, Map, Collection 等

### 注解

可以给 DTO 类型、属性添加注解：

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

非 Jimmer 注解会自动从实体属性复制到 DTO 属性。

## 代码示例

- [DTO 基础示例](../../examples/dto/basic.dto)

## 相关链接

- [保存指令 -> Input DTO](../../api/save-command.md)
- [查询 -> 超级 QBE](super-qbe.md)
