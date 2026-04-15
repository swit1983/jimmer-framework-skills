---
title: '生成客户端异常'
---
# 生成客户端异常


这部分功能其实和ORM没有关系。


然而，既然提供了[前后端API免对接](client/api)，那么就必须提供这部分功能，否则就不是一套完整的方案。


## 异常簇


我们需要定义特别的Java/Kotlin业务异常，它们能够被直接翻译成客户端可以理解的数据。


该如何定义这种特别的业务异常类型呢？有两种极端的选择


- 整个应用程序共享一个业务异常类，并内置客户端可以理解的error code
- 每个业务错误都定义一个业务异常类


很明显，上述两种方案都不合适。第一种方案粒度太粗，全局的error code难以维护；第二种方案粒度太细，需要定义的异常类是在太多了。


所以，Jimmer选择折中方案，把业务异常分为多个簇，每个簇用一套Error Code。


用户可以采用两种方法定义异常簇


- 自动生成
- 纯手写


### 自动生成异常簇


Jimmer采用枚举来定义异常簇，枚举也是这簇异常的错误码，例如


#### 定义异常簇


- Java
- Kotlin
UserInfoErrorCode.java

```
package com.example.business.error;import org.babyfish.jimmer.error.ErrorFamily;@ErrorFamilypublic enum UserInfoErrorCode {    ILLEGAL_USER_NAME,    PASSWORD_TOO_SHORT,    PASSWORDS_NOT_SAME}
```

UserInfoErrorCode.kt

```
package com.example.business.errorimport org.babyfish.jimmer.error.ErrorFamily@ErrorFamilyenum class UserInfoErrorCode {    ILLEGAL_USER_NAME,    PASSWORD_TOO_SHORT,    PASSWORDS_NOT_SAME}
```


`@org.babyfish.jimmer.error.ErrorFamily`表示该枚举表示一簇业务异常，该枚举也是这簇异常的ErrorCode.


注解`@ErrorFamily`会被Jimmer的预编译器处理


信息

这里的预编译器，对于Java而言就是Annotation Processor；对于Kotlin而言就是KSP。


这部分类型已经在[概述/快速上手/生成代码](quick-view/get-started/generate-code)一节中讨论过，此处不在重复。


预编译器会会根据此枚举生成如下异常类


信息

用作声明异常簇的枚举可以选择用`ErrorCode`或`Error`结尾。


- 如果有这样的特殊结尾，异常类名 = 枚举名去掉这样的结尾并加 + `Exception`
- 否则，异常类名 = 枚举名 + `Exception`


所以，这里生成的异常名为`UserInfoException`


经过Jimmer的编译，将会生成如下的异常类。


- Java
- Kotlin
UserInfoException.java

```
public abstract class UserInfoException     extends CodeBasedRuntimeException { ❶    private UserInfoException(String message, Throwable cause) {        super(message, cause);    }    @Override    public abstract UserInfoErrorCode getCode(); ❷    public static IllegalUserName illegalUserName(@NotNull String message) { ❸        return new IllegalUserName(            message,            null        );    }    public static IllegalUserName illegalUserName( ❹            @NotNull String message,            @Nullable Throwable cause    ) {        return new IllegalUserName(            message,            cause        );    }    public static PasswordTooShort passwordTooShort(@NotNull String message) { ❺        return new PasswordTooShort(            message,            null        );    }    public static PasswordTooShort passwordTooShort( ❻            @NotNull String message,            @Nullable Throwable cause    ) {        return new PasswordTooShort(            message,            cause        );    }    public static PasswordsNotSame passwordsNotSame(@NotNull String message) { ❼        return new PasswordsNotSame(            message,            null        );    }    public static passwordsNotSame passwordsNotSame( ❽            @NotNull String message,            @Nullable Throwable cause    ) {        return new PasswordsNotSame(            message,            cause        );    }    public static class IllegalUserName extends UserInfoException {        public IllegalUserName(String message, Throwable cause) {            super(message, cause);        }        @Override        public UserInfoErrorCode getCode() {            return UserInfoErrorCode.ILLEGAL_USER_NAME; ❾        }        @Override        public Map<String, Object> getFields() {            return Collections.emptyMap();        }    }    public static class PasswordTooShort extends UserInfoException {        public PasswordTooShort(String message, Throwable cause) {            super(message, cause);        }        @Override        public UserInfoErrorCode getCode() {            return UserInfoErrorCode.PASSWORD_TOO_SHORT; ❿        }        @Override        public Map<String, Object> getFields() {            return Collections.emptyMap();        }    }    public static class PasswordsNotSame extends UserInfoException {        public PasswordsNotSame(String message, Throwable cause) {            super(message, cause);        }        @Override        public UserInfoErrorCode getCode() {            return UserInfoErrorCode.PASSWORDS_NOT_SAME; ⓫        }        @Override        public Map<String, Object> getFields() {            return Collections.emptyMap();        }    }}
```

UserInfoException.kt

```
public abstract class UserInfoException private constructor(    message: String,    cause: Throwable? = null,) : CodeBasedRuntimeException(message, cause) { ❶    public abstract override val code: UserInfoErrorCode ❷    public companion object {        @JvmStatic        public fun illegalUserName( ❸❹            message: String,             cause: Throwable? = null        ): IllegalUserName =            IllegalUserName(                message,                cause            )        @JvmStatic        public fun passwordTooShort( ❺❻            message: String,             cause: Throwable? = null        ): PasswordTooShort =            PasswordTooShort(                message,                cause            )        @JvmStatic        public fun passwordsNotSame( ❼❽            message: String,             cause: Throwable? = null            ): PasswordsNotSame =                PasswordsNotSame(                    message,                    cause                )    }    public class IllegalUserName(        message: String,        cause: Throwable? = null,    ) : UserInfoException(message, cause) {        public override val code: UserInfoErrorCode            get() = UserInfoErrorCode.ILLEGAL_USER_NAME ❾        public override val fields: Map<String, Any?>            get() = emptyMap()    }    public class PasswordTooShort(        message: String,        cause: Throwable? = null,    ) : UserInfoException(message, cause) {        public override val code: UserInfoErrorCode            get() = UserInfoErrorCode.PASSWORD_TOO_SHORT ❿        public override val fields: Map<String, Any?>            get() = emptyMap()    }    public class PasswordsNotSame(        message: String,        cause: Throwable? = null,    ) : UserInfoException(message, cause) {        public override val code: UserInfoErrorCode            get() = UserInfoErrorCode.PASSWORDS_NOT_SAME ⓫        public override val fields: Map<String, Any?>            get() = emptyMap()    }}
```


- ❶ 基于枚举错误码的异常必然继承`org.babyfish.jimmer.error.CodeBasedRuntimeException`
- ❷ 这一簇异常的错误码的类型为`UserInfoErrorCode`
- ❸❹ 构建错误码为`ILLEGAL_USER_NAME`的异常的静态方法
- ❺❻ 构建错误码为`PASSWORD_TOO_SHORT`的异常的静态方法
- ❼❽ 构建错误码为`PASSWORDS_NOT_SAME`的异常的静态方法
- ❾ 异常类`UserInfoException.IllegalUserName`的错误码为`ILLEGAL_USER_NAME`
- ❿ 异常类`UserInfoException.PasswordTooShort`的错误码为`PASSWORD_TOO_SHORT`
- ⓫ 异常类`UserInfoException.PasswordsNotSame`的错误码为`PASSWORDS_NOT_SAME`


#### 为异常码添加字段


可以为任何一个错误码添加附加字段。


比如，`ILLEGAL_USER_NAME`表示非法的用户名，即用户名包含了非法字符。我们可以为其添加字段`illegalChars`


- Java
- Kotlin
UserInfoErrorCode.java

```
@ErrorFamilypublic enum UserInfoErrorCode {    @ErrorField(name = "illegalChars", type = Character.class, list = true)    ILLEGAL_USER_NAME,        PASSWORD_TOO_SHORT,    PASSWORDS_NOT_SAME}
```

UserInfoErrorCode.kt

```
@ErrorFamilyenum class UserInfoErrorCode {        @ErrorField(name = "illegalChars", type = Char::class, list = true)    ILLEGAL_USER_NAME,    PASSWORD_TOO_SHORT,        PASSWORDS_NOT_SAME}
```


生成的代码的如下


- Java
- Kotlin
UserInfoException.java

```
public abstract class UserInfoException extends CodeBasedRuntimeException {    public static IllegalUserName illegalUserName(        @NotNull String message,        @NotNull List<Character> illegalChars    ) {        ...省略代码...    }    public static IllegalUserName illegalUserName(        @NotNull String message,        @Nullable Throwable cause,         @NotNull List<Character> illegalChars    ) {        ...省略代码...    }    public static class IllegalUserName extends UserInfoException {        @NotNull        private final List<Character> illegalChars;        public IllegalUserName(            String message,             Throwable cause,            @NotNull List<Character> illegalChars        ) {            super(message, cause);            this.illegalChars = illegalChars;        }        @Override        public Map<String, Object> getFields() {            return Collections.singletonMap("illegalChars", illegalChars);        }        @NotNull        public List<Character> getIllegalChars() {            return illegalChars;        }        ...省略其他代码...    }    ...省略其他代码...}
```

UserInfoException.kt

```
public abstract class UserInfoException private constructor(    message: String,    cause: Throwable? = null,) : CodeBasedRuntimeException(message, cause) { ❶    public abstract override val code: UserInfoErrorCode ❷    public companion object {                @JvmStatic        public fun illegalUserName(            message: String,            cause: Throwable? = null,            illegalChars: List<Char>,        ): IllegalUserName =             ...省略代码...        ...省略其他代码...    }    public class IllegalUserName(        message: String,        cause: Throwable? = null,        public val illegalChars: List<Char>,    ) : UserInfoException(message, cause) {        public override val fields: Map<String, Any?>            get() = mapOf(                "illegalChars" to illegalChars            )        ...省略其他代码...    }}
```


### 手写异常簇


【TODO】


## 为REST API声明异常


### 允许抛出整个异常簇


所谓抛出异常簇中部分异常，就是抛出抽象的异常类型 *(例如：throws UserInfoException)*。


Jimmer允许任何HTTP服务方法抛出异常


- Java： 使用throws语法
- Kotlin：使用`@kotlin.Throws`注解


- Java
- Kotlin
UserController.java

```
package com.example.service;import org.babyfish.jimmer.client.ThrowsAll;@PostMapping("/user")public void registerUser(    @RequestBody RegisterUserInput input) throws UserInfoException {    ...省略代码...}
```

UserController.kt

```
package com.example.serviceimport import org.babyfish.jimmer.client.ThrowsAll@PostMapping("/user")@Throws(UserInfoException::class)fun registerUser(@RequestBody input: RegisterUserInput) {    ...省略代码...}
```


### 允许抛出异常簇中部分异常


所谓抛出异常簇中部分异常，就是抛出具体的异常类型 *(例如：throws UserInfoException.PasswordTooShort)*。


- Java
- Kotlin
UserController.java

```
@PostMapping("/user")public void registerUser(    @RequestBody RegisterUserInput input) throws UserInfoException.PasswordTooShort {    ...省略代码...}
```

UserController.kt

```
@PostMapping("/user")@Throws(UserInfoException.PasswordsNotSame::class)fun registerUser(@RequestBody input: RegisterUserInput) {    ...省略代码...}
```


### 比较和建议


提示

其实允许抛出整个异常簇就是抛出簇内所有异常。


即，抛出整个异常簇`throws UserInfoException`，其实和抛出具体异常类型列表`throws UserInfoException.IllegalUserName, UserInfoException.PasswordTooShort, UserInfoException.PasswordsNotSame`等价。


建议尽量抛出具体的异常类型列表，以减少客户端需要处理的异常的种类。


## 导出服务端异常


前面的工作只是声明REST API有可能抛出何种异常。接下来我们讨论在服务端真正抛出异常


### 抛出异常


- Java
- Kotlin
UserController.java

```
@PostMapping("/user")public void registerUser(    @RequestBody RegisterUserInput input) throws UserInfoException.IllegalUserName {    if (...某些条件...) {        List<Character> illegalChars = ...略...        throw UserInfoException.illegalUserName(            "The user name is invalid",             illegalChars        );    }    ...省略其他代码...}
```

UserController.kt

```
@PostMapping("/user")@Throws(UserInfoException.IllegalUserName::class)fun registerUser(@RequestBody input: RegisterUserInput) {    if (...某些条件...) {        val illegalChars: List<Char> = ...略...        throw UserInfoException.illegalUserName(            "The user name is invalid",            illegalChars        )    }    ...省略其他代码...}
```


警告

内部代码抛出的异常的种类，不得超过对外声明的种类


### 将异常消息写入HTTP响应


提示

只要使用了Jimmer的Spring Boot Starter，无需任何工作，服务抛出任何继承自`CodeBasedRuntimeException`的异常都会被自动翻译。


翻译结果为


```
{    "family":"USER_INFO_ERROR_CODE",    "code":"ILLEGAL_USER_NAME",    "illegalChars": ["&", "`", "@"]}
```


为了方便开发和测试，可以在`application.yml`或`application.properties`中配置


```
jimmer:    error-translator:        debug-info-supported: true
```


此配置将会在HTTP返回中附带便于在开发和测试阶段定位问题的信息。由于内容较长，请点击下面的按钮查看结果。


.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}查看结果@media print{.css-1k371a6{position:absolute!important;}}
注意

此配置仅用于辅助开发和测试，绝不能在生产环境中开启此开关！


比如，请在`application-dev.yml`和`application-test.yml`中开启此开关，但绝对不能在`application-prod.yml`中开启。


## 客户端


### 生成的TypeScript代码


上文中我们已经定义了一个异常簇`USER_INFO_ERROR_CODE`，假如还有另外一个异常簇`PLATFORM_ERROR_CODE`，采用自动生成的方式，原始枚举类型为。


- Java
- Kotlin
PlatformErrorCode.java

```
@ErrorFamilypublic enum PlatformErrorCode {    PERMISSION_DENIED,    DATA_IS_FROZEN,    SERVICE_IS_SUSPENDED,}
```

PlatformErrorCode.kt

```
@ErrorFamilyenum class PlatformErrorCode {    PERMISSION_DENIED,    DATA_IS_FROZEN,    SERVICE_IS_SUSPENDED}
```


假设Controller代码如下


- Java
- Kotlin
UserController.kt

```
@RestControllerpublic class UserController {    @PutMapping("/user")    public void registerUser(        BookInput input    ) throws        UserInfoException,        PlatformException.ServiceIsSuspended {        ...略...    }    @DeleteMapping("/user")    public void deleteUser(        long id    ) throws        UserInfoException,        PlatformException.PermissionDenied {        ...略...    }}
```

UserController.kt

```
@RestControllerclass UserController {    @PutMapping("/user")    @Throws(        UserController::class        PlatformException.ServiceIsSuspended::class    )    fun registerUser(        input: BookInput    ) {        ...略...       }    @DeleteMapping("/user")    @Throws(        UserController::class        PlatformException.PermissionDenied::class    )    fun deleteUser(        id: Long    ) {        ...略...       }}
```


统计各种异常被Java Web API方法throws子句或Kotlin Web方法的`@Throws`注解使用的情况，不难发现


| USER_INFO_ERROR_CODE | PLATFORM_ERROR_CODE |  |  |
| --- | --- | --- | --- |
| ILLEGAL_USER_NAME | Used | PERMISSION_DENIED | Used |
| PASSWORD_TOO_SHORT | Used | DATA_IS_FROZEN | Not used! |
| PASSWORD_NOT_SAME | Used | SERVICE_IS_SUSPENDED | Used |


信息

请注意我们定义了6种具体的异常类型，但是Web Api只`throws`了5种，`PLATFORM_ERROR_CODE`簇中的`DATA_IS_FROZEN`从未被使用，后文会讨论。


生成的TypeScript代码如下


```
export type AllErrors = ❶    {        readonly family: "PLATFORM_ERROR_CODE",         readonly code: "SERVICE_IS_SUSPENDED"    } |    {        readonly family: "PLATFORM_ERROR_CODE",         readonly code: "PERMISSION_DENIED"    } |     {        readonly family: "USER_INFO_ERROR_CODE",        readonly code: "ILLEGAL_USER_NAME",        readonly "illegalChars": ReadonlyArray<number>    } |     {        readonly family: "USER_INFO_ERROR_CODE",        readonly code: "PASSWORDS_NOT_SAME"    } |     {        readonly family: "USER_INFO_ERROR_CODE",        readonly code: "PASSWORD_TOO_SHORT"    } |    ...省略其他代码...;export type ApiErrors = { ❷    "userController": {        "registerUser": AllErrors & (            {                readonly family: 'USER_INFO_ERROR_CODE',                readonly code: 'ILLEGAL_USER_NAME',                readonly [key:string]: any            } |             {                readonly family: 'USER_INFO_ERROR_CODE',                readonly code: 'PASSWORD_TOO_SHORT',                readonly [key:string]: any            } |             {                readonly family: 'USER_INFO_ERROR_CODE',                readonly code: 'PASSWORDS_NOT_SAME',                readonly [key:string]: any            } |             {                readonly family: 'PLATFORM_ERROR_CODE',                readonly code: 'SERVICE_IS_SUSPENDED',                readonly [key:string]: any            }        ),        "deleteUser": AllErrors & (            {                readonly family: 'USER_INFO_ERROR_CODE',                readonly code: 'ILLEGAL_USER_NAME',                readonly [key:string]: any            } |             {                readonly family: 'USER_INFO_ERROR_CODE',                readonly code: 'PASSWORD_TOO_SHORT',                readonly [key:string]: any            } |             {                readonly family: 'USER_INFO_ERROR_CODE',                readonly code: 'PASSWORDS_NOT_SAME',                readonly [key:string]: any            } |             {                readonly family: 'PLATFORM_ERROR_CODE',                readonly code: 'PERMISSION_DENIED',                readonly [key:string]: any            }        )     },    ...如果还有更多Controller有抛出客户端异常的行为，将会出现在这里...};
```


- ❶ `AllErrors`定义所有错误类型，包括`family`，`code`以及各异常的自定义字段 *(灰色北京一样)*


信息

前文我们讨论过，虽然我定义了6种异常，但是只有5种被Web Api使用。因此，TypeScript代码中只有5种错误定义。
- ❷ Web Api种HTTP各接口抛出的错误类型。


### TypeScript IDE的效果


上面的TypeScript代码较多，考虑到部分读者没有TypeScript的背景知识，这里罗列一些IDE的智能提示截图


- 全局类型`ApiErrors`下面有两个选项: `userController`和`bookController`


![](data:image/webp;base64,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)
- `ApiErrors["userController"]`下面有三个选项: `login`、`logout`和`registerUser`


![](https://jimmer.deno.dev/zh/assets/images/api-2a7966c9db85aec24ae0d1fe0cb62c17.webp)
- `ApiErrors["userController"]["registerUser"]`的`family`字段有两个选项: `USER_INFO_ERROR_CODE`和`PLATFORM_ERROR_CODE`


![](https://jimmer.deno.dev/zh/assets/images/family-b9c1857991f5f8d662444aa9f6e2b85f.webp)
- 一旦`family`被确定为`UserInfoErrorCode`，`code`字段有三个选项: `ILLEGAL_USER_NAME`、`PASSWORD_TOO_SHORT`和`PASSWORDS_NOT_SAME`


![](https://jimmer.deno.dev/zh/assets/images/code-bfdfb727fb3ccc54d12fe6a4c5ddc58f.webp)
- 一旦`code`被确定为`ILLEGAL_USER_NAME`，则可以使用`illegalChars`附加字段


![](https://jimmer.deno.dev/zh/assets/images/field-05834b18f87cfe96cfd1d1a72750e9e5.webp)
[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/client/error.mdx)最后 于 **2025年9月16日**  更新
- [异常簇](#异常簇)
- [自动生成异常簇](#自动生成异常簇)
- [手写异常簇](#手写异常簇)
- [为REST API声明异常](#为rest-api声明异常)
- [允许抛出整个异常簇](#允许抛出整个异常簇)
- [允许抛出异常簇中部分异常](#允许抛出异常簇中部分异常)
- [比较和建议](#比较和建议)
- [导出服务端异常](#导出服务端异常)
- [抛出异常](#抛出异常)
- [将异常消息写入HTTP响应](#将异常消息写入http响应)
- [客户端](#客户端)
- [生成的TypeScript代码](#生成的typescript代码)
- [TypeScript IDE的效果](#typescript-ide的效果)