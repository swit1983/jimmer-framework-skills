# 定义Repository

> 来源: https://jimmer.deno.dev/zh/docs/spring/repository/concept

- [Spring篇](/zh/docs/spring/)
- [Spring Data风格](/zh/docs/spring/repository/)
- 定义Repository

# 定义Repository

Jimmer整合了spring data，为Java用户和Kotlin用户各自提供了一个Repository基接口。

|        |                                                          |
|--------|----------------------------------------------------------|
| ---    | ---                                                      |
| Java   | org.babyfish.jimmer.spring.repository.JRepository<E, ID> |
| Kotlin | org.babyfish.jimmer.spring.repository.KRepository<E, ID> |

该接口具备两个范型参数

- E: 实体类型
- ID: 实体ID类型

通过继承此接口，开发人员可以快速实现各种Repository

- BookStoreRepository

  - Java
  - Kotlin

  BookStoreRepository.java

  ```package com.example.repository;
  import com.example.model.BookStore;

  import org.babyfish.jimmer.spring.repository.JRepository;

  public interface BookStoreRepository extends JRepository<BookStore, Long> {}

```

  BookStoreRepository.kt

  ```package com.example.repository
  import com.example.model.BookStore

  import org.babyfish.jimmer.spring.repository.KRepository

  interface BookStoreRepository : KRepository<BookStore, Long>

```

- BookRepository

  - Java
  - Kotlin

  BookRepository.java

  ```package com.example.repository;
  import com.example.model.Book;

  import org.babyfish.jimmer.spring.repository.JRepository;

  public interface BookRepository extends JRepository<Book, Long> {}

```

  BookRepository.kt

  ```package com.example.repository
  import com.example.model.Book

  import org.babyfish.jimmer.spring.repository.KRepository

  interface BookRepository : KRepository<Book, Long>

```

- AuthorRepository

  - Java
  - Kotlin

  AuthorRepository.java

  ```package com.example.repository;
  import com.example.model.Author;

  import org.babyfish.jimmer.spring.repository.JRepository;

  public interface AuthorRepository extends JRepository<Author, Long> {}

```

  AuthorRepository.kt

  ```package com.example.repository
  import com.example.model.Author

  import org.babyfish.jimmer.spring.repository.KRepository

  interface AuthorRepository : KRepository<Author, Long>

```

- TreeNodeRepository

  - Java
  - Kotlin

  TreeNodeRepository.java

  ```package com.example.repository;
  import com.example.model.TreeNode;

  import org.babyfish.jimmer.spring.repository.JRepository;

  public interface TreeNodeRepository extends JRepository<TreeNode, Long> {}

```

  TreeNodeRepository.kt

  ```package com.example.repository
  import com.example.model.TreeNode

  import org.babyfish.jimmer.spring.repository.KRepository

  interface TreeNodeRepository : KRepository<TreeNode, Long>

```

备注

其他与spring-data相同，定义好接口即可，**无需**用`@org.springframework.stereotype.Repository`修饰。Jimmer会自动实现这些接口并注册到Spring中。

但是有一个注意事项：

- 默认情况下，自定义Repository所属的包必须是SpringBoot主类所在包或子包。
- 否则，需要用`@org.babyfish.jimmer.spring.repository.EnableJimmerRepositories`修饰SpringBoot主类或其他Spring配置类，明确指定自定义Repository接口所在的包。

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/spring/repository/concept.mdx)

最后于 **2025年9月16日** 更新