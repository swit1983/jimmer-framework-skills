# 保存前拦截器

> 来源: https://jimmer.deno.dev/zh/docs/mutation/draft-interceptor

* [修改篇](/zh/docs/mutation/)
* 保存前拦截器

本页总览

# 保存前拦截器

## 基本概念[​](#基本概念 "基本概念的直接链接")

任何实体对象在被[保存指令](/zh/docs/mutation/save-command)保存 *(无论插入还是更新)* 前，都会被拦截器拦截。

在此，用户有一次修改被保存数据的机会，尤其是为某些缺失的属性赋值。

提示

如果使用拦截器为缺失的属性赋值\*(这也是推荐用法)\*，就和数据库级别的默认值有点类似，但是存在如下差异

* 数据库默认值只能提供业务无关的默认值规则。
* 拦截器可以根据业务上下文相关信息提供默认值，比如，当前用户在权限系统中的身份信息。

  用户可以根据这类业务上下文信息提供和业务紧密结合的默认值，这是数据库级别默认值无法实现的。

## 定义被拦截数据格式[​](#定义被拦截数据格式 "定义被拦截数据格式的直接链接")

Draft拦截器和[Save指令](/zh/docs/mutation/save-command)配合使用，在对象被保存之前调整数据。

假如大部分实体表都具备created\_time、modified\_time、created\_by和modified\_by四个字段，可以提供如下超类

* Java
* Kotlin

```
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

```
@MappedSuperclass  
interface BaseEntity {  
  
    val createdTime: LocalDateTime  
  
    val modifiedTime: LocalDateTime  
  
    @ManyToOne  
    @OnDissociate(DissociateAction.SET_NULL)  
    val createdBy: User?  
  
    @ManyToOne  
    @OnDissociate(DissociateAction.SET_NULL)  
    val modifiedBy: User?  
}
```

所有需要这些字段的实体都从此超类派生即可。

备注

这里的`@OnDissociate(DissociateAction.SET_NULL)`是为了防止因这两个外键导致相关`User`数据的删除操作被阻止。当相关`User`被删除后，这两个外键自动清空。

提示

当然，用户可以直接拦截实体类型 *(被@Entity修饰)*，而非抽象类型 *(被@MappedSupperClass)* 修饰。

然而，如果选择拦截抽象类型，那么所有派生实体类型的保存操作都将会被拦截，这可以极大地提高系统的灵活性，尤其是抽象类型支持多继承时。

所以，本文的例子选择拦截抽象类型，而非实体类型。

## 定义拦截器[​](#定义拦截器 "定义拦截器的直接链接")

假设有一个叫做`UserService`的服务类，其java方法`getCurrentUserId()`或kotlin属性`currentUserId`返回当前登录用户的id。

拦截器需要实现`org.babyfish.jimmer.sql.DraftInterceptor`接口。

如果使用Spring托管 *(下文会介绍两种使用拦截器的方式)*，请用`@Component`修饰拦截器实现类，代码代码如下：

* Java
* Kotlin

```
@Component  
public class BaseEntityDraftInterceptor   
implements DraftInterceptor<BaseEntity, BaseEntityDraft> {  
  
    private final UserService userService;  
  
    public BaseEntityDraftInterceptor(UserService userService) {  
        this.userService = userService;  
    }  
  
    @Override  
    public void beforeSave(BaseEntityDraft draft, @Nullable BaseEntity original) {  
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.MODIFIED_TIME)) {  
            draft.setModifiedTime(LocalDateTime.now());  
        }  
        if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.EDITOR)) {  
            draft.applyModifiedBy(user - > {  
                user.setId(userService.getCurrentUserId());  
            });  
        }  
        if (original == null) {  
            if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATED_TIME)) {  
                draft.setCreatedTime(LocalDateTime.now());  
            }  
            if (!ImmutableObjects.isLoaded(draft, BaseEntityProps.CREATOR)) {  
                draft.applyCreatedBy(user - > {  
                    user.setId(userService.getCurrentUserId());  
                });  
            }     
        }  
    }  
}
```

```
@Component  
class BaseEntityDraftInterceptor(  
    private val userService: UserService  
) : DraftInterceptor<BaseEntity, BaseEntityDraft> {  
  
    override fun beforeSave(draft: BaseEntityDraft, original: BaseEntity?) {  
        if (!isLoaded(draft, BaseEntity::modifiedTime)) {  
            draft.modifiedTime = LocalDateTime.now()  
        }  
  
        if (!isLoaded(draft, BaseEntity::modifiedBy)) {  
            draft.modifiedBy {  
                id = userService.currentUserId  
            }  
        }  
  
        if (original === null) {  
            if (!isLoaded(draft, BaseEntity::createdTime)) {  
                draft.createdTime = LocalDateTime.now()  
            }  
  
            if (!isLoaded(draft, BaseEntity::createdBy)) {  
                draft.createdBy {  
                    id = userService.currentUserId  
                }  
            }  
        }  
    }  
}
```

其中，`beforeSave`方法在某个对象被保存之前被调用，用户可以对即将保存的数据`draft`做出最后调整。该方法有两个参数

* `draft`: 即将被保存的对象，你可以修改它
* `original`: 如果非null，则表示数据库中现有的数据，只可读取，不可修改

  + 对于INSERT操作而言，`original`为null
  + 对于UPDATE操作而言，`original`非null

  所以，可以通过`original`是否为null判断当前操作是INSERT还是UPDATE。

  `original`对象是一个Jimmer动态对象，其哪些些属性就绪可以访问而哪些缺失不可访问，受到另外一个方法`dependencies`的控制。

注意

请不要在`beforeSave`方法中，修改被`@Id`或`@Key`修饰的属性。

## 控制original参数的格式[​](#控制original参数的格式 "控制original参数的格式的直接链接")

上文谈到，如果当前操作为`UPDATE`，`beforeSave`方法的`original`参数非null，表示数据库中的旧值。

`original`是Jimmer动态对象，默认情况下，只有`id`和`key`属性是已加载和可访问的。然而，是否能够控制`original`对象的格式让更多的属性可以被访问呢？

`DraftInterceptor`接口提供了另外一个default方法`dependencies`，返回一个属性集合，以表示除了id属性和key属性外，`original`对象还有那些属性需要被加载。

* Java
* Kotlin

```
@Component  
public class BaseEntityDraftInterceptor   
implements DraftInterceptor<BaseEntity, BaseEntityDraft> {  
  
    @Override  
    public void beforeSave(  
        BaseEntityDraft draft,   
          
        // The format of `original` is controlled by `dependencies()`  
        @Nullable BaseEntity original  
    ) {  
        ...implementation is omitted...  
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

```
@Component  
class BaseEntityDraftInterceptor(  
    private val userService: UserService  
) : DraftInterceptor<BaseEntity, BaseEntityDraft> {  
  
    override fun beforeSave(  
        draft: BaseEntityDraft,   
  
        // The format of `original` is controlled by `dependencies()`  
        original: BaseEntity?  
    ) {  
        ...implementation is omitted...  
    }  
  
    override fun dependencies(): Collection<TypedProp<BaseEntity, *>> =   
        listOf(  
            BaseEntityProps.CREATED_BY,   
            BaseEntityProps.MODIFIED_BY  
        )  
}
```

提示

返回的属性集合无需包含`id`属性和`key`属性，因为它们总是被加载。

## 应用拦截器[​](#应用拦截器 "应用拦截器的直接链接")

### 使用Jimmer Spring Starter[​](#使用jimmer-spring-starter "使用Jimmer Spring Starter的直接链接")

上文中，我们定义的类`BaseEntityDraftInterceptor`被`@Component`修饰，很明显这是一个Spring托管对象。

信息

如果使用SpringBoot Starter且保证拦截器被Spring托管，那么Jimmer就会将注册它，无需额外的配置。

否则，必需手动注册

### 不使用Jimmer Spring Starter[​](#不使用jimmer-spring-starter "不使用Jimmer Spring Starter的直接链接")

未使用SpringBoot时，将拦截器挂接到SqlClient对象上，即可生效

* Java
* Kotlin

```
@Bean  
public JSqlClient sqlClient(  
    List<DraftInterceptor<?>> interceptors,  
    ...省略其他参数...  
) {  
    return JSqlClient  
        .newBuilder()  
        .addDraftInterceptors(interceptors)  
        ...省略其他配置...  
        .build();  
}
```

```
@Bean  
fun sqlClient(  
    interceptors: List<DraftInterceptor<?>>,  
    ...省略其他参数...  
): KSqlClient =  
    newKSqlClient {  
        addDraftInterceptors(interceptors)  
        ...省略其他配置...  
    }
```

提示

虽然在本文仅示范了一个`DraftInterceptor`，实际项目中可能有很多个。

所以，这里使用集合，让Spring注入所有的`DraftInterceptor`。

## 最终使用[​](#最终使用 "最终使用的直接链接")

假如`Book`继承了`BaseEntity`，则可以这么使用

* Java
* Kotlin

```
Book book = Immutables.createBook(draft -> {  
    draft.setName("SQL in Action");  
    draft.setEdition(1);  
    draft.setPrice(new BigDecimal("59"));  
    draft.applyStore(store -> store.setId(2L));  
});  
sqlClient.getEntities().save(book);
```

```
val book = Book {  
    name = "SQL in Action"  
    edition = 1  
    price = BigDecimal("59")  
    store().id = 2  
}  
sqlClient.entities.save(book)
```

* 如果上面的保存指令最终导致了insert操作，生成的SQL如下

  ```
  insert into BOOK(  
      CREATED_TIME,  
      MODIFIED_TIME,  
      CREATED_BY,  
      MODIFIED_BY,  
      NAME,   
      EDITION,   
      PRICE,   
      STORE_ID  
  ) values(  
      ?, ?, ?, ?,  
      ?, ?, ?, ?  
  )
  ```

  其中，为`CREATED_TIME`、 `MODIFIED_TIME`、`CREATED_BY`和`MODIFIED_BY`赋值的行为由拦截器自动添加
* 如果上面的保存指令最终导致了update操作，生成的SQL如下

  ```
  update BOOK set   
      MODIFIED_TIME = ?,  
      MODIFIED_TIME,  
      PRICE = ?,   
      STORE_ID = ?   
  where ID = ?
  ```

其中，为`MODIFIED_TIME`和`MODIFIED_BY`赋值的行为由拦截器自动添加

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mutation/draft-interceptor.mdx)

最后于 **2025年9月16日** 更新