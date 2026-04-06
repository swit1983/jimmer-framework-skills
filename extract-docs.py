#!/usr/bin/env python3
import os
import requests
from bs4 import BeautifulSoup
import markdownify
import time

BASE_URL = "https://jimmer.deno.dev"

DOCS = [
# 初识Jimmer ★
("/zh/docs/overview/", "overview"),
("/zh/docs/overview/welcome", "overview-welcome"),
("/zh/docs/overview/key-features", "overview-key-features"),
("/zh/docs/overview/introduction", "overview-introduction"),
("/zh/docs/overview/apt-ksp", "overview-apt-ksp"),
("/zh/docs/overview/benchmark", "overview-benchmark"),

# 快速预览 ★
("/zh/docs/quick-view/", "quick-view"),
("/zh/docs/quick-view/precondition", "quick-view-precondition"),
("/zh/docs/quick-view/fetch/", "quick-view-fetch"),
("/zh/docs/quick-view/fetch/feature", "quick-view-fetch-feature"),
("/zh/docs/quick-view/fetch/export/entity", "quick-view-fetch-export-entity"),
("/zh/docs/quick-view/fetch/export/dto", "quick-view-fetch-export-dto"),
("/zh/docs/quick-view/fetch/export/comparison", "quick-view-fetch-export-comparison"),
("/zh/docs/quick-view/save/", "quick-view-save"),
("/zh/docs/quick-view/save/feature", "quick-view-save-feature"),
("/zh/docs/quick-view/save/export/root", "quick-view-save-export-root"),
("/zh/docs/quick-view/save/export/short", "quick-view-save-export-short"),
("/zh/docs/quick-view/save/export/long", "quick-view-save-export-long"),
("/zh/docs/quick-view/dsl/", "quick-view-dsl"),
("/zh/docs/quick-view/dsl/feature", "quick-view-dsl-feature"),
("/zh/docs/quick-view/dsl/super_qbe", "quick-view-dsl-super_qbe"),
("/zh/docs/quick-view/get-started/", "quick-view-get-started"),
("/zh/docs/quick-view/get-started/create-project", "quick-view-get-started-create-project"),
("/zh/docs/quick-view/get-started/create-database", "quick-view-get-started-create-database"),
("/zh/docs/quick-view/get-started/define-entity", "quick-view-get-started-define-entity"),
("/zh/docs/quick-view/get-started/generate-code", "quick-view-get-started-generate-code"),
("/zh/docs/quick-view/get-started/usage", "quick-view-get-started-usage"),
("/zh/docs/quick-view/standard-demo", "quick-view-standard-demo"),

# 案例展示 ★
("/zh/docs/showcase/", "showcase"),
("/zh/docs/showcase/base", "showcase-base"),
("/zh/docs/showcase/fetch-association/associated-object", "showcase-fetch-association-associated-object"),
("/zh/docs/showcase/fetch-association/more-association", "showcase-fetch-association-more-association"),
("/zh/docs/showcase/fetch-association/associated-id", "showcase-fetch-association-associated-id"),
("/zh/docs/showcase/fetch-association/deeper-association", "showcase-fetch-association-deeper-association"),
("/zh/docs/showcase/fetch-association/join-fetch", "showcase-fetch-association-join-fetch"),
("/zh/docs/showcase/fetch-association/prop-filter", "showcase-fetch-association-prop-filter"),
("/zh/docs/showcase/recursive-query/usage", "showcase-recursive-query-usage"),
("/zh/docs/showcase/recursive-query/depth", "showcase-recursive-query-depth"),
("/zh/docs/showcase/recursive-query/node-control", "showcase-recursive-query-node-control"),
("/zh/docs/showcase/recursive-query/multiple-props", "showcase-recursive-query-multiple-props"),
("/zh/docs/showcase/where/usage", "showcase-where-usage"),
("/zh/docs/showcase/where/dynamic-where", "showcase-where-dynamic-where"),
("/zh/docs/showcase/where/associated-id", "showcase-where-associated-id"),
("/zh/docs/showcase/where/dynamic-join", "showcase-where-dynamic-join"),
("/zh/docs/showcase/where/implicit-subquery", "showcase-where-implicit-subquery"),
("/zh/docs/showcase/order-by/usage", "showcase-order-by-usage"),
("/zh/docs/showcase/order-by/dynamic", "showcase-order-by-dynamic"),
("/zh/docs/showcase/page", "showcase-page"),
("/zh/docs/showcase/comprehensive-query", "showcase-comprehensive-query"),
("/zh/docs/showcase/other-query", "showcase-other-query"),
("/zh/docs/showcase/update-statement", "showcase-update-statement"),
("/zh/docs/showcase/delete-statement", "showcase-delete-statement"),
("/zh/docs/showcase/to-be-conitnued", "showcase-to-be-conitnued"),

# 映射篇
("/zh/docs/mapping/", "mapping"),
("/zh/docs/mapping/base/nullity", "mapping-base-nullity"),
("/zh/docs/mapping/base/basic", "mapping-base-basic"),
("/zh/docs/mapping/base/association/one-to-one", "mapping-base-association-one-to-one"),
("/zh/docs/mapping/base/association/many-to-one", "mapping-base-association-many-to-one"),
("/zh/docs/mapping/base/association/one-to-many", "mapping-base-association-one-to-many"),
("/zh/docs/mapping/base/association/many-to-many", "mapping-base-association-many-to-many"),
("/zh/docs/mapping/base/naming-strategy", "mapping-base-naming-strategy"),
("/zh/docs/mapping/base/foreignkey", "mapping-base-foreignkey"),
("/zh/docs/mapping/base/json-converter", "mapping-base-json-converter"),
("/zh/docs/mapping/base/more-type", "mapping-base-more-type"),
("/zh/docs/mapping/advanced/embedded", "mapping-advanced-embedded"),
("/zh/docs/mapping/advanced/mapped-super-class", "mapping-advanced-mapped-super-class"),
("/zh/docs/mapping/advanced/logical-deleted/entity", "mapping-advanced-logical-deleted-entity"),
("/zh/docs/mapping/advanced/logical-deleted/join-table", "mapping-advanced-logical-deleted-join-table"),
("/zh/docs/mapping/advanced/view/id-view", "mapping-advanced-view-id-view"),
("/zh/docs/mapping/advanced/view/many-to-many-view", "mapping-advanced-view-many-to-many-view"),
("/zh/docs/mapping/advanced/calculated/formula", "mapping-advanced-calculated-formula"),
("/zh/docs/mapping/advanced/calculated/transient", "mapping-advanced-calculated-transient"),
("/zh/docs/mapping/advanced/enum", "mapping-advanced-enum"),
("/zh/docs/mapping/advanced/json", "mapping-advanced-json"),
("/zh/docs/mapping/advanced/join-sql", "mapping-advanced-join-sql"),
("/zh/docs/mapping/advanced/join-table-filter", "mapping-advanced-join-table-filter"),
("/zh/docs/mapping/advanced/key", "mapping-advanced-key"),
("/zh/docs/mapping/advanced/remote", "mapping-advanced-remote"),
("/zh/docs/mapping/advanced/on-dissociate", "mapping-advanced-on-dissociate"),

# 查询篇
("/zh/docs/query/", "query"),
("/zh/docs/query/usage", "query-usage"),
("/zh/docs/query/object-fetcher/usage", "query-object-fetcher-usage"),
("/zh/docs/query/object-fetcher/props", "query-object-fetcher-props"),
("/zh/docs/query/object-fetcher/association", "query-object-fetcher-association"),
("/zh/docs/query/object-fetcher/recursive", "query-object-fetcher-recursive"),
("/zh/docs/query/object-fetcher/view", "query-object-fetcher-view"),
("/zh/docs/query/object-fetcher/dto", "query-object-fetcher-dto"),
("/zh/docs/query/object-fetcher/spring-data", "query-object-fetcher-spring-data"),
("/zh/docs/query/dynamic-where", "query-dynamic-where"),
("/zh/docs/query/dynamic-join/problem", "query-dynamic-join-problem"),
("/zh/docs/query/dynamic-join/chain-style", "query-dynamic-join-chain-style"),
("/zh/docs/query/dynamic-join/merge", "query-dynamic-join-merge"),
("/zh/docs/query/dynamic-join/optimization", "query-dynamic-join-optimization"),
("/zh/docs/query/dynamic-join/table-ex", "query-dynamic-join-table-ex"),
("/zh/docs/query/dynamic-join/weak-join", "query-dynamic-join-weak-join"),
("/zh/docs/query/dynamic-join/kotlin-join", "query-dynamic-join-kotlin-join"),
("/zh/docs/query/implicit-subquery", "query-implicit-subquery"),
("/zh/docs/query/dynamic-order", "query-dynamic-order"),
("/zh/docs/query/group", "query-group"),
("/zh/docs/query/paging/usage", "query-paging-usage"),
("/zh/docs/query/paging/unnecessary-join", "query-paging-unnecessary-join"),
("/zh/docs/query/paging/reverse-sorting", "query-paging-reverse-sorting"),
("/zh/docs/query/paging/deep-optimization", "query-paging-deep-optimization"),
("/zh/docs/query/expression", "query-expression"),
("/zh/docs/query/native-sql", "query-native-sql"),
("/zh/docs/query/base-query", "query-base-query"),
("/zh/docs/query/global-filter/user-filter", "query-global-filter-user-filter"),
("/zh/docs/query/global-filter/logical-deleted", "query-global-filter-logical-deleted"),
("/zh/docs/query/sub-query", "query-sub-query"),
("/zh/docs/query/associations", "query-associations"),
("/zh/docs/query/super_qbe", "query-super_qbe"),

# 修改篇
("/zh/docs/mutation/", "mutation"),
("/zh/docs/mutation/update-statement", "mutation-update-statement"),
("/zh/docs/mutation/delete-statement", "mutation-delete-statement"),
("/zh/docs/mutation/save-command/usage", "mutation-save-command-usage"),
("/zh/docs/mutation/save-command/data-classification", "mutation-save-command-data-classification"),
("/zh/docs/mutation/save-command/save-mode", "mutation-save-command-save-mode"),
("/zh/docs/mutation/save-command/association/classification", "mutation-save-command-association-classification"),
("/zh/docs/mutation/save-command/association/owner", "mutation-save-command-association-owner"),
("/zh/docs/mutation/save-command/association/associated-save-mode", "mutation-save-command-association-associated-save-mode"),
("/zh/docs/mutation/save-command/association/dissociation", "mutation-save-command-association-dissociation"),
("/zh/docs/mutation/save-command/investigation", "mutation-save-command-investigation"),
("/zh/docs/mutation/save-command/optimistic-locking", "mutation-save-command-optimistic-locking"),
("/zh/docs/mutation/save-command/pessimistic-locking", "mutation-save-command-pessimistic-locking"),
("/zh/docs/mutation/save-command/mysql", "mutation-save-command-mysql"),
("/zh/docs/mutation/save-command/input-dto/problem", "mutation-save-command-input-dto-problem"),
("/zh/docs/mutation/save-command/input-dto/lonely", "mutation-save-command-input-dto-lonely"),
("/zh/docs/mutation/save-command/input-dto/dto-lang", "mutation-save-command-input-dto-dto-lang"),
("/zh/docs/mutation/save-command/input-dto/null-handling", "mutation-save-command-input-dto-null-handling"),
("/zh/docs/mutation/save-command/input-dto/mapstruct", "mutation-save-command-input-dto-mapstruct"),
("/zh/docs/mutation/delete-command", "mutation-delete-command"),
("/zh/docs/mutation/associations", "mutation-associations"),
("/zh/docs/mutation/draft-interceptor", "mutation-draft-interceptor"),
("/zh/docs/mutation/trigger", "mutation-trigger"),

# 缓存篇
("/zh/docs/cache/", "cache"),
("/zh/docs/cache/enable-cache", "cache-enable-cache"),
("/zh/docs/cache/cache-type/object", "cache-cache-type-object"),
("/zh/docs/cache/cache-type/association", "cache-cache-type-association"),
("/zh/docs/cache/cache-type/calculation", "cache-cache-type-calculation"),
("/zh/docs/cache/consistency", "cache-consistency"),
("/zh/docs/cache/multiview-cache/concept", "cache-multiview-cache-concept"),
("/zh/docs/cache/multiview-cache/user-filter", "cache-multiview-cache-user-filter"),
("/zh/docs/cache/multiview-cache/advanced", "cache-multiview-cache-advanced"),
("/zh/docs/cache/multiview-cache/abandoned-callback", "cache-multiview-cache-abandoned-callback"),

# 客户端篇
("/zh/docs/client/", "client"),
("/zh/docs/client/api", "client-api"),
("/zh/docs/client/error", "client-error"),

# Spring篇
("/zh/docs/spring/", "spring"),
("/zh/docs/spring/transaction", "spring-transaction"),
("/zh/docs/spring/repository/concept", "spring-repository-concept"),
("/zh/docs/spring/repository/abstract", "spring-repository-abstract"),
("/zh/docs/spring/repository/default", "spring-repository-default"),
("/zh/docs/spring/repository/dto", "spring-repository-dto"),
("/zh/docs/spring/spring-cloud", "spring-spring-cloud"),
("/zh/docs/spring/appendix", "spring-appendix"),

# 配置篇
("/zh/docs/configuration/", "configuration"),
("/zh/docs/configuration/dialect", "configuration-dialect"),
("/zh/docs/configuration/connection-manager", "configuration-connection-manager"),
("/zh/docs/configuration/multi-datasources", "configuration-multi-datasources"),
("/zh/docs/configuration/batch-size", "configuration-batch-size"),
("/zh/docs/configuration/sql-log", "configuration-sql-log"),
("/zh/docs/configuration/default-enum-strategy", "configuration-default-enum-strategy"),
("/zh/docs/configuration/default-database-stragegy", "configuration-default-database-stragegy"),
("/zh/docs/configuration/trigger-type", "configuration-trigger-type"),
("/zh/docs/configuration/database-validation", "configuration-database-validation"),
("/zh/docs/configuration/micro-service", "configuration-micro-service"),
("/zh/docs/configuration/scala-provider", "configuration-scala-provider"),
("/zh/docs/configuration/cache-abandoned", "configuration-cache-abandoned"),
("/zh/docs/configuration/save-command-pessimistic-lock", "configuration-save-command-pessimistic-lock"),
("/zh/docs/configuration/id-only-target-checking-level", "configuration-id-only-target-checking-level"),
("/zh/docs/configuration/dissociate-action-checking", "configuration-dissociate-action-checking"),
("/zh/docs/configuration/in-list-optimization", "configuration-in-list-optimization"),

# GraphQL篇
("/zh/docs/graphql/", "graphql"),
("/zh/docs/graphql/concept", "graphql-concept"),
("/zh/docs/graphql/query", "graphql-query"),
("/zh/docs/graphql/mutation", "graphql-mutation"),

# 对象篇
("/zh/docs/object/", "object"),
("/zh/docs/object/dynamic", "object-dynamic"),
("/zh/docs/object/immutable/reason", "object-immutable-reason"),
("/zh/docs/object/immutable/current-situation", "object-immutable-current-situation"),
("/zh/docs/object/immutable/solution", "object-immutable-solution"),
("/zh/docs/object/draft", "object-draft"),
("/zh/docs/object/jackson", "object-jackson"),
("/zh/docs/object/view/dto-language", "object-view-dto-language"),
("/zh/docs/object/view/mapstruct", "object-view-mapstruct"),
("/zh/docs/object/visibility", "object-visibility"),
("/zh/docs/object/tool", "object-tool"),

# 资源篇
("/zh/docs/resource/", "resource"),
("/zh/docs/resource/ecosystem", "resource-ecosystem"),
("/zh/docs/resource/video", "resource-video"),
("/zh/docs/resource/discuss", "resource-discuss"),
]

OUTPUT_DIR = "docs"
os.makedirs(OUTPUT_DIR, exist_ok=True)

headers = {
    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
}

def download_doc(url_path, filename):
    url = BASE_URL + url_path
    output_file = os.path.join(OUTPUT_DIR, f"{filename}.md")
    
    try:
        response = requests.get(url, headers=headers, timeout=30)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # 找到文章内容
        article = soup.find('article')
        if not article:
            print(f"⚠️  没有找到文章内容: {url}")
            return False
        
        # 转换为 markdown
        markdown_content = markdownify.markdownify(str(article), heading_style="ATX")
        
        # 添加标题和来源
        title_tag = soup.find('h1')
        title = title_tag.get_text().strip() if title_tag else filename
        
        full_content = f"# {title}\n\n> 来源: {url}\n\n{markdown_content}"
        
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(full_content)
        
        print(f"✅ 已保存: {output_file} ({len(full_content)} 字符)")
        return True
    except Exception as e:
        print(f"❌ 下载失败 {url}: {e}")
        return False

def main():
    total = len(DOCS)
    success = 0
    failed = 0
    
    print(f"开始下载 {total} 篇文档...\n")
    
    for i, (url_path, filename) in enumerate(DOCS, 1):
        print(f"[{i}/{total}] ", end='')
        if download_doc(url_path, filename):
            success += 1
        else:
            failed += 1
        time.sleep(1)  # 礼貌延迟
    
    print(f"\n下载完成！成功: {success}, 失败: {failed}")

if __name__ == "__main__":
    main()
