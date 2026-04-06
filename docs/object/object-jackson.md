# 和Jackson协同

> 来源: https://jimmer.deno.dev/zh/docs/object/jackson

- [对象篇](/zh/docs/object/)
- 和Jackson协同

# 和Jackson协同

Jimmer对象是动态的，并不是对象的所有属性都需要初始化，它允许缺少一些属性。

- 未指定的属性在直接被代码访问时会导致异常
- 未指定的属性在JSON序列化中自动忽略，不会异常

这里提到了JSON序列化，指[jackson](https://github.com/FasterXML/jackson)。

jimmer-core定义了一个jackson模块：`org.babyfish.jimmer.jackson.ImmutableModule`，利用该模块可以为jackson增加序列化/反序列化jimmer不可变对象的能力。

分两种情况

- 使用Spring Boot Starter

  在这种情况下，Jimmer已经为Spring注册了ImmutableModule，如下

  ```// jimmer内部代码，非用户代码
  @ConditionalOnMissingBean(ImmutableModule.class)
  @Bean
  public ImmutableModule immutableModule() {
      return new ImmutableModule();
  }

```

  因此，Spring默认的`ObjectMapper`已经可以序列化/反序列化Jimmer动态对象，而绝大部分对象的序列化/反序列化工作都是在HTTP交互时由Spring自动完成的，所以无需任何开发。

  警告

  Spring默认的Json处理库就是[jackson](https://github.com/FasterXML/jackson)，勿替换。
- 使用底层API

  - Java
  - Kotlin

  ```objectmapper mapper = new objectmapper()
      .registerModule(new ImmutableModule());

  TreeNode treeNode = Immutables.createTreeNode(
      draft -> draft.setName("Root Node")
  );

  // 系列化
  String json = mapper.writeValueAsString(treeNode);

  // 反序列化
  TreeNode deserializedTreeNode =
      mapper.readValue(json, TreeNode.class);

```

  ```val mapper = objectmapper()
      .registerModule(ImmutableModule())

  val treeNode = TreeNode {
      name = "Root Node"
  }

  // 序列化
  val json = mapper.writeValueAsString(treeNode);

  // 反序列化
  TreeNode deserializedTreeNode =
      mapper.readValue(json, TreeNode::class.java);

```

提示

- 对于序列化操作而言，有一个便捷方式，就是jimmer对象的`toString`方法。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/object/jackson.mdx)

最后于 **2025年9月16日** 更新