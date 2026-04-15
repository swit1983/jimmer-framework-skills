---
title: '工具方法'
---
# 工具方法


Jimmer提供了一系列静态工具方法，用于操作动态对象或其Draft。


信息

这些方法既有强类型重构版本，也有弱类型的重构版本。为节省篇幅，本文仅列举强类型用法。


## 操作不可变对象


### isLoaded


- 作用：判断动态对象的某个属性是否被加载


- Java
- Kotlin


```
if (ImmutableObjects.isLoaded(book, BookProps.AUTHORS)) {    ...}
```


```
if (isLoaded(book, Book::authors)) {    ...}
```


### get


- 作用：动态获取对象的某个属性
- 异常：对象的该属性处于未加载状态


- Java
- Kotlin


```
List<Author> authors =     ImmutableObjects.get(book, BookProps.AUTHORS);
```


```
val authors = get(book, Book::authors)
```


### isIdOnly


- 作用：是否是只被设置了id属性的动态对象
- 前提：对象类型被`@Entity`修饰，是ORM实体，具备id


- Java
- Kotlin


```
if (ImmutableObjects.isIdOnly(book)) {    ...}
```


```
if (isIdOnly(book)) {    ...}
```


### makeIdOnly


- 作用：用指定类型构建一个对象，并设置其id属性
- 前提：对象类型被`@Entity`修饰，是ORM实体，具备id


- Java
- Kotlin


```
Book book = ImmutableObjects.makeIdOnly(Book.class, 1L);
```


```
Book book = makeIdOnly(Book::class, 1L)
```


提示
- 对于kotlin而言，参数id不得为null，返回值也不为null。


如果要接受可能为null的id，并在id真为null时直接返回null，请调用`makeNullableIdOnly`
- Java的`makeIdOnly`，其实和Kotlin的`makeNullableIdOnly`等价。


### isLonely


- 作用：是否是只被设置了id属性的孤单对象。即，是否没有任何关联属性被设置为非null *(包含未设置和设置为null)*。


信息

如果对象是一个ORM实体，若直接基于外键的一对一或多对一属性被设置为只有id的关联对象，则例外。


- Java
- Kotlin


```
if (ImmutableObjects.isLonely(book)) {    ...}
```


```
if (isLonely(book)) {    ...}
```


### toLonely


- 作用：根据一个已有的对象，创建新对象。新对象从旧对象复制所有非关联属性，但所有关联属性保持未设置状态


信息

如果对象是一个ORM实体，对于直接外键的一对一或多对一属性而言，进行特殊处理，将其设置为只有id的关联对象或null


- Java
- Kotlin


```
Book lonelyBook = ImmutableObjects.toLonely(book);
```


```
val lonelyBook = toLonely(book)
```


### toIdOnly


- 作用：根据一个已有的对象，创建新对象。新对象仅从旧对象复制id属性
- 前提：对象类型被`@Entity`修饰，是ORM实体，具备id


- Java
- Kotlin


```
Book lonelyBook = ImmutableObjects.toIdOnly(book);
```


```
val lonelyBook = toIdOnly(book)
```


或


- Java
- Kotlin


```
List<Book> lonelyBooks = ImmutableObjects.toIdOnly(books);
```


```
val lonelyBooks = toIdOnly(books)
```


### fromString


作用：JSON反序列化的快捷方式


- Java
- Kotlin


```
Book book = ImmutableObjects.fromObject(    Book.class,    "{\"id\":1,\"name\":\"Learning GraphQL\",\"authorIds\":[2,1]}"  );
```


```
val book = fromString(    Book::class,    """{"id":1,"name":"Learning GraphQL","authorIds":[2,1]}""")
```


## 操作可变Draft


### set


作用：动态设置Draft属性


- Java
- Kotlin


```
Book newBook = Immutables.createBook(book, draft -> {    DraftObjects.set(draft, BookProps.AUTHOR_IDS, Arrays.asList(1L, 3L));});
```


```
val newBook = Book(book) {    set(draft, BookDraft::authorIds, listOf(1L, 3L))}
```


### unload


作用：卸载Draft属性，即，将某个属性标记成未加载状态


- Java
- Kotlin


```
Book newBook = Immutables.createBook(book, draft -> {    DraftObjects.unload(draft, BookProps.AUTHOR_IDS);});
```


```
val newBook = Book(book) {    unload(draft, BookDraft::authorIds)}
```


### show


作用：显示某属性


- Java
- Kotlin


```
Book newBook = Immutables.createBook(book, draft -> {    DraftObjects.show(draft, BookProps.AUTHOR_IDS);});
```


```
val newBook = Book(book) {    show(draft, BookDraft::authorIds)}
```


### hide


作用：隐藏某属性


- Java
- Kotlin


```
Book newBook = Immutables.createBook(book, draft -> {    DraftObjects.hide(draft, BookProps.AUTHOR_IDS);});
```


```
val newBook = Book(book) {    hide(draft, BookDraft::authorIds)}
```

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/object/tool.mdx)最后 于 **2025年9月16日**  更新
- [操作不可变对象](#操作不可变对象)
- [isLoaded](#isloaded)
- [get](#get)
- [isIdOnly](#isidonly)
- [makeIdOnly](#makeidonly)
- [isLonely](#islonely)
- [toLonely](#tolonely)
- [toIdOnly](#toidonly)
- [fromString](#fromstring)
- [操作可变Draft](#操作可变draft)
- [set](#set)
- [unload](#unload)
- [show](#show)
- [hide](#hide)