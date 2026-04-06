# 复合字段 (Embedded)

使用 `@Embeddable` 将多个数据库列映射为一个复合类型。

## 定义复合类型

```java
@Embeddable
public interface FullName {
    String firstName();
    String lastName();
}
```

```kotlin
@Embeddable
interface FullName {
    val firstName: String
    val lastName: String
}
```

⚠️ **限制**：`@Embeddable` 类型**不是实体**，不能声明 ID 和关联属性。

## 在实体中使用

```java
@Entity
public interface Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id();
    
    // 直接使用复合类型，无需 @Embedded
    FullName name();
}
```

```kotlin
@Entity
interface Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
    
    val name: FullName
}
```

对应表结构：

```sql
create table author(
    id bigint unsigned not null auto_increment primary key,
    first_name varchar(20) not null,
    last_name varchar(20) not null
) engine=innodb;
```

## 覆盖列名

使用 `@PropOverride` 覆盖复合类型中的列名：

```java
@Entity
public interface Transition {
    @Id
    long id();
    
    Rect source();
    
    @PropOverride(prop = "leftTop.x", columnName = "TARGET_LEFT")
    @PropOverride(prop = "leftTop.y", columnName = "TARGET_TOP")
    @PropOverride(prop = "rightBottom.x", columnName = "TARGET_RIGHT")
    @PropOverride(prop = "rightBottom.y", columnName = "TARGET_BOTTOM")
    Rect target();
}
```

多级复合类型也可以逐层覆盖。

## 复合类型作为主外键

⚠️ **不推荐**：除非必须兼容遗留系统，否则应避免复合主外键。

```java
@Embeddable
public interface UniqueId {
    @ColumnName("UNIQUE_ID_DAY_NO")
    int dayNo();
    
    @ColumnName("UNIQUE_ID_SEQ_NO")
    int sequenceNo();
}

@Entity
public interface Book {
    @Id
    UniqueId id();
    
    @ManyToMany
    @JoinTable(
        joinColumns = {
            @JoinColumn(
                name = "BOOK_ID_DAY_NO",
                referencedColumnName = "UNIQUE_ID_DAY_NO"
            ),
            @JoinColumn(
                name = "BOOK_ID_SEQ_NO",
                referencedColumnName = "UNIQUE_ID_SEQ_NO"
            )
        }
    )
    List<Author> authors();
}
```

复合外键需要显式指定 `referencedColumnName`。
