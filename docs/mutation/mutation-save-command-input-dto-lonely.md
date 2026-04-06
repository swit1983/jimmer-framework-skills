# 孤单表单

> 来源: https://jimmer.deno.dev/zh/docs/mutation/save-command/input-dto/lonely

- [修改篇](/zh/docs/mutation/)
- [保存指令](/zh/docs/mutation/save-command/)
- [Input DTO](/zh/docs/mutation/save-command/input-dto/)
- 孤单表单

# 孤单表单

警告

这是一个非常简陋的方案，仅供学习或非常简单的项目使用。

限制：只能保存单表数据，无法实现复杂数据结构的保存

功能：解决安全性问题 *(该问题在[上一篇文档](/zh/docs/mutation/save-command/input-dto/problem)中讨论过)*

优点：无需定义Input DTO

缺点：不解决API模糊性问题 *(该问题在[上一篇文档](/zh/docs/mutation/save-command/input-dto/problem)中讨论过)*

- Java
- Kotlin

```@putmapping("/book")
public void saveBook(
    @RequestBody Book book
) {
    if (!ImmutableObjects.isLonely(book)) {
        throw new IllegalArgumentException("The input object is too complex");
    }
    bookRepository.save(book);
}

```

```@putmapping("/book")
fun saveBook(
    @RequestBody book: Book
) {
    if (!isLonely(book)) {
        throw IllegalArgumentException("The input object is too complex")
    }
    bookRepository.save(input)
}

```

这个例子直接采用Jimmer动态对象作为入参，不限制传入的数据结构的复杂度。但是，我们加了一个验证，如果参数不是孤单对象，抛出异常以保证安全性。

`isLonely`用于判断动态对象是否只是孤单对象，孤单对象只能存在两种属性

- 标量属性
- 基于外键的的关联属性，但只能被赋为null或只有id属性的关联对象。

不难看出，上面代码中验证可以保证所有的`insert`或`update`操作只能针对一张表。

如果开发人员认为

- 将功能限制为只能修改一张表就达到自己需要的安全限制
- 另外一个API模糊化问题是可以接受的。

那么这的确是一个非常简陋但可用的方案。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mutation/save-command/input-dto/lonely.mdx)

最后于 **2025年9月16日** 更新