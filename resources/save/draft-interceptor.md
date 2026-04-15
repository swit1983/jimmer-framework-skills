# 保存前拦截器 (Draft Interceptor)

> 本文档介绍如何使用 Jimmer 的保存前拦截器，在实体被保存前自动修改数据。

---

## 基本概念

任何实体对象在被[保存指令](./save-command.md)保存（无论插入还是更新）前，都会被拦截器拦截。

在此，用户有一次修改被保存数据的机会，尤其是为某些缺失的属性赋值。

### 与数据库默认值的区别

| 特性 | 数据库默认值 | 拦截器默认值 |
|------|-------------|-------------|
| 业务关联 | 无业务含义 | 可结合业务上下文（如当前用户信息） |
| 灵活性 | 固定值 | 动态计算 |
| 使用场景 | 简单初始值 | 复杂业务逻辑 |

---

## 定义被拦截数据格式

Draft 拦截器和保存指令配合使用，在对象被保存之前调整数据。

### 示例：基础实体超类

假设大部分实体表都具备 `created_time`、`modified_time`、`created_by` 和 `modified_by` 四个字段，可以提供如下超类：

```java
@MappedSuperclass
public interface BaseEntity {
    
    LocalDateTime createdTime();
    
    LocalDateTime modifiedTime();
    
    @Nullable
    @ManyToOne
    @OnDissociate(DissociateAction.SET_NULL)
    User creator();
    
    @Nullable
    @ManyToOne
    @OnDissociate(DissociateAction.SET_NULL)
    User editor();
}
```

所有需要这些字段的实体都从此超类派生即可。

> **注意**：这里的 `@OnDissociate(DissociateAction.SET_NULL)` 是为了防止因这两个外键导致相关 User 数据的删除操作被阻止。当相关 User 被删除后，这两个外键自动清空。

### 拦截抽象类型 vs 实体类型

用户可以直接拦截实体类型（被 `@Entity` 修饰），而非抽象类型（被 `@MappedSuperclass` 修饰）。

然而，如果选择拦截抽象类型，那么所有派生实体类型的保存操作都将会被拦截，这可以极大地提高系统的灵活性，尤其是抽象类型支持多继承时。

所以，本文的例子选择拦截抽象类型，而非实体类型。

---

## 定义拦截器

假设有一个叫做 `UserService` 的服务类，其 Java 方法 `getCurrentUserId()` 或 Kotlin 属性 `currentUserId` 返回当前登录用户的 id。

拦截器需要实现 `org.babyfish.jimmer.sql.DraftInterceptor` 接口。

### 示例代码

```java
@Component
public class BaseEntityDraftInterceptor 
implements DraftInterceptor<BaseEntity, BaseEntityDraft> {
    
    private final UserService userService;
    
    public BaseEntityDraftInterceptor(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    public void beforeSave(BaseEntityDraft draft, @Nullable BaseEntity original) {
        // 修改时间：总是更新
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.MODIFIED_TIME)) {
            draft.setModifiedTime(LocalDateTime.now());
        }
        
        // 修改人：总是更新
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.EDITOR)) {
            draft.applyModifiedBy(user -> {
                user.setId(userService.getCurrentUserId());
            });
        }
        
        // 新增操作（original == null 表示 INSERT）
        if (original == null) {
            // 创建时间
            if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATED_TIME)) {
                draft.setCreatedTime(LocalDateTime.now());
            }
            
            // 创建人
            if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATOR)) {
                draft.applyCreatedBy(user -> {
                    user.setId(userService.getCurrentUserId());
                });
            }
        }
    }
}
```

### 方法参数说明

`beforeSave` 方法在某个对象被保存之前被调用，用户可以对即将保存的数据 `draft` 做出最后调整。

| 参数 | 说明 |
|------|------|
| `draft` | 即将被保存的对象，你可以修改它 |
| `original` | 如果非 null，则表示数据库中现有的数据，只可读取，不可修改 |

**操作类型判断**：
- `original == null`：INSERT 操作
- `original != null`：UPDATE 操作

> **注意**：请不要在 `beforeSave` 方法中修改被 `@Id` 或 `@Key` 修饰的属性。

---

## 控制 original 参数的格式

上文谈到，如果当前操作为 UPDATE，`beforeSave` 方法的 `original` 参数非 null，表示数据库中的旧值。

`original` 是 Jimmer 动态对象，默认情况下，只有 id 和 key 属性是已加载和可访问的。然而，是否能够控制 `original` 对象的格式让更多的属性可以被访问呢？

### 使用 dependencies() 方法

`DraftInterceptor` 接口提供了另外一个 `default` 方法 `dependencies`，返回一个属性集合，以表示除了 id 属性和 key 属性外，`original` 对象还有哪些属性需要被加载。

```java
@Component
public class BaseEntityDraftInterceptor
implements DraftInterceptor<BaseEntity, BaseEntityDraft> {
    
    @Override
    public void beforeSave(
        BaseEntityDraft draft,
        @Nullable BaseEntity original
    ) {
        // ...implementation is omitted...
    }
    
    @Override
    public Collection<TypedProp<BaseEntity, ?>> dependencies() {
        return Arrays.asList(
            BaseEntityProps.CREATED_BY,
            BaseEntityProps.MODIFIED_BY
        );
    }
}
```

> **提示**：返回的属性集合无需包含 id 属性和 key 属性，因为它们总是被加载。

---

## 应用拦截器

### 使用 Jimmer Spring Starter

上文中，我们定义的类 `BaseEntityDraftInterceptor` 被 `@Component` 修饰，这是一个 Spring 托管对象。

如果使用 Spring Boot Starter 且保证拦截器被 Spring 托管，那么 Jimmer 就会自动将它注册，**无需额外的配置**。

### 不使用 Jimmer Spring Starter

未使用 Spring Boot 时，将拦截器挂接到 `SqlClient` 对象上，即可生效：

```java
@Bean
public JSqlClient sqlClient(
    List<DraftInterceptor<?>> interceptors,
    // ...省略其他参数...
) {
    return JSqlClient
        .newBuilder()
        .addDraftInterceptors(interceptors)
        // ...省略其他配置...
        .build();
}
```

> **提示**：虽然在本文仅示范了一个 `DraftInterceptor`，实际项目中可能有很多个。所以，这里使用集合，让 Spring 注入所有的 `DraftInterceptor`。

---

## 最终使用

假如 `Book` 继承了 `BaseEntity`，则可以这样使用：

```java
Book book = Immutables.createBook(draft -> {
    draft.setName("SQL in Action");
    draft.setEdition(1);
    draft.setPrice(new BigDecimal("59"));
    draft.applyStore(store -> store.setId(2L));
});

sqlClient.getEntities().save(book);
```

### 生成的 SQL

**如果上面的保存指令最终导致了 INSERT 操作**，生成的 SQL 如下：

```sql
INSERT INTO BOOK(
    CREATED_TIME, 
    MODIFIED_TIME, 
    CREATED_BY, 
    MODIFIED_BY, 
    NAME, 
    EDITION, 
    PRICE, 
    STORE_ID
) VALUES(?, ?, ?, ?, ?, ?, ?, ?)
```

其中，为 `CREATED_TIME`、`MODIFIED_TIME`、`CREATED_BY` 和 `MODIFIED_BY` 赋值的行为由**拦截器自动添加**。

**如果上面的保存指令最终导致了 UPDATE 操作**，生成的 SQL 如下：

```sql
UPDATE BOOK SET 
    MODIFIED_TIME = ?, 
    MODIFIED_BY = ?, 
    PRICE = ?, 
    STORE_ID = ? 
WHERE ID = ?
```

其中，为 `MODIFIED_TIME` 和 `MODIFIED_BY` 赋值的行为由**拦截器自动添加**。

---

## 参考

- [Jimmer 官方文档 - 保存前拦截器](https://jimmer.deno.dev/zh/docs/mutation/draft-interceptor)
- [保存指令文档](./save-command.md)
- [官方示例代码](https://github.com/babyfish-ct/jimmer-examples)
