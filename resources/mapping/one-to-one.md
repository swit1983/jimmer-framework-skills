# 一对一关联

使用 `@org.babyfish.jimmer.sql.OneToOne` 注解声明一对一关联。

一对一支持双向关联：
- **主动方**（必须）：真正的数据库映射，实现单向一对一
- **从动方**（可选）：作为主动方的镜像，形成双向关联

和 JPA/Hibernate 不同：**主动方和从动方都可以用于保存关联**。

## 示例：Customer ↔ Address 双向一对一

### 主动方：Customer.address

有两种实现方式：基于外键和基于中间表。

#### 1. 基于外键（推荐）

```java
@Entity
public interface Customer {
    @OneToOne
    Address address();
    
    // ...
}
```

不指定 `@JoinColumn` 时，Jimmer 自动推导：`address` → `ADDRESS_ID`，等价于：

```java
@Entity
public interface Customer {
    @OneToOne
    @JoinColumn(name = "ADDRESS_ID")
    Address address();
    
    // ...
}
```

数据库约束：
```sql
ALTER TABLE CUSTOMER
ADD CONSTRAINT FK_CUSTOMER__ADDRESS
FOREIGN KEY (ADDRESS_ID) REFERENCES ADDRESS(ID);
```

#### 2. 基于中间表（兼容已有设计）

```java
@Entity
public interface Customer {
    @Nullable
    @OneToOne
    @JoinTable
    Address address();
    
    // ...
}
```

不指定属性时，自动推导：
- 中间表名：`CUSTOMER_ADDRESS_MAPPING`
- 当前实体外键：`CUSTOMER_ID`
- 关联实体外键：`ADDRESS_ID`

等价于完整写法：

```java
@Entity
public interface Customer {
    @Nullable
    @OneToOne
    @JoinTable(
        name = "CUSTOMER_ADDRESS_MAPPING",
        joinColumnName = "CUSTOMER_ID",
        inverseJoinColumnName = "ADDRESS_ID"
    )
    Address address();
    
    // ...
}
```

中间表定义：

```sql
CREATE TABLE CUSTOMER_ADDRESS_MAPPING (
    CUSTOMER_ID bigint NOT NULL,
    ADDRESS_ID bigint NOT NULL,
    PRIMARY KEY (CUSTOMER_ID, ADDRESS_ID),
    FOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMER(ID),
    FOREIGN KEY (ADDRESS_ID) REFERENCES ADDRESS(ID),
    -- 必须对两个外键都加唯一约束，否则就是多对多了
    UNIQUE (CUSTOMER_ID),
    UNIQUE (ADDRESS_ID)
);
```

⚠️ 注意：
- 除非兼容已有设计，否则建议直接使用外键
- 使用中间表时，关联属性必须可空（无法保证一定有对应数据）

### 从动方：Address.customer

使用 `mappedBy` 指定它是对方的镜像：

```java
@Entity
public interface Address {
    // `mappedBy = "address"` 表示 Address.customer 是 Customer.address 的镜像
    @OneToOne(mappedBy = "address")
    @Nullable
    Customer customer();
    
    // ...
}
```

## 重要规则

| 规则 | 说明 |
|------|------|
| 使用 `mappedBy` 后 | 不能再使用 `@JoinColumn` 或 `@JoinTable` |
| 从动方一对一 | 必须可空 |
| 中间表一对一 | 两个外键都必须加唯一约束 |

## 对比

| 方式 | 使用场景 |
|------|----------|
| 外键 | 新建项目，推荐 |
| 中间表 | 兼容已有数据库设计 |
