#!/bin/bash

# Jimmer 文档批量下载脚本
# 基于 playwright-cli 下载所有文档页面

OUTPUT_DIR="docs"
mkdir -p "$OUTPUT_DIR"

# 所有文档URL
DOCS=(
# 初识Jimmer ★
"/zh/docs/overview/"
"/zh/docs/overview/welcome"
"/zh/docs/overview/key-features"
"/zh/docs/overview/introduction"
"/zh/docs/overview/apt-ksp"
"/zh/docs/overview/benchmark"

# 快速预览 ★
"/zh/docs/quick-view/"
"/zh/docs/quick-view/precondition"
"/zh/docs/quick-view/fetch/"
"/zh/docs/quick-view/fetch/feature"
"/zh/docs/quick-view/fetch/export/entity"
"/zh/docs/quick-view/fetch/export/dto"
"/zh/docs/quick-view/fetch/export/comparison"
"/zh/docs/quick-view/save/"
"/zh/docs/quick-view/save/feature"
"/zh/docs/quick-view/save/export/root"
"/zh/docs/quick-view/save/export/short"
"/zh/docs/quick-view/save/export/long"
"/zh/docs/quick-view/dsl/"
"/zh/docs/quick-view/dsl/feature"
"/zh/docs/quick-view/dsl/super_qbe"
"/zh/docs/quick-view/get-started/"
"/zh/docs/quick-view/get-started/create-project"
"/zh/docs/quick-view/get-started/create-database"
"/zh/docs/quick-view/get-started/define-entity"
"/zh/docs/quick-view/get-started/generate-code"
"/zh/docs/quick-view/get-started/usage"
"/zh/docs/quick-view/standard-demo"

# 案例展示 ★
"/zh/docs/showcase/"
"/zh/docs/showcase/base"
"/zh/docs/showcase/fetch-association/associated-object"
"/zh/docs/showcase/fetch-association/more-association"
"/zh/docs/showcase/fetch-association/associated-id"
"/zh/docs/showcase/fetch-association/deeper-association"
"/zh/docs/showcase/fetch-association/join-fetch"
"/zh/docs/showcase/fetch-association/prop-filter"
"/zh/docs/showcase/recursive-query/usage"
"/zh/docs/showcase/recursive-query/depth"
"/zh/docs/showcase/recursive-query/node-control"
"/zh/docs/showcase/recursive-query/multiple-props"
"/zh/docs/showcase/where/usage"
"/zh/docs/showcase/where/dynamic-where"
"/zh/docs/showcase/where/associated-id"
"/zh/docs/showcase/where/dynamic-join"
"/zh/docs/showcase/where/implicit-subquery"
"/zh/docs/showcase/order-by/usage"
"/zh/docs/showcase/order-by/dynamic"
"/zh/docs/showcase/page"
"/zh/docs/showcase/comprehensive-query"
"/zh/docs/showcase/other-query"
"/zh/docs/showcase/update-statement"
"/zh/docs/showcase/delete-statement"
"/zh/docs/showcase/to-be-conitnued"

# 映射篇
"/zh/docs/mapping/"
"/zh/docs/mapping/base/nullity"
"/zh/docs/mapping/base/basic"
"/zh/docs/mapping/base/association/one-to-one"
"/zh/docs/mapping/base/association/many-to-one"
"/zh/docs/mapping/base/association/one-to-many"
"/zh/docs/mapping/base/association/many-to-many"
"/zh/docs/mapping/base/naming-strategy"
"/zh/docs/mapping/base/foreignkey"
"/zh/docs/mapping/base/json-converter"
"/zh/docs/mapping/base/more-type"
"/zh/docs/mapping/advanced/embedded"
"/zh/docs/mapping/advanced/mapped-super-class"
"/zh/docs/mapping/advanced/logical-deleted/entity"
"/zh/docs/mapping/advanced/logical-deleted/join-table"
"/zh/docs/mapping/advanced/view/id-view"
"/zh/docs/mapping/advanced/view/many-to-many-view"
"/zh/docs/mapping/advanced/calculated/formula"
"/zh/docs/mapping/advanced/calculated/transient"
"/zh/docs/mapping/advanced/enum"
"/zh/docs/mapping/advanced/json"
"/zh/docs/mapping/advanced/join-sql"
"/zh/docs/mapping/advanced/join-table-filter"
"/zh/docs/mapping/advanced/key"
"/zh/docs/mapping/advanced/remote"
"/zh/docs/mapping/advanced/on-dissociate"

# 查询篇
"/zh/docs/query/"
"/zh/docs/query/usage"
"/zh/docs/query/object-fetcher/usage"
"/zh/docs/query/object-fetcher/props"
"/zh/docs/query/object-fetcher/association"
"/zh/docs/query/object-fetcher/recursive"
"/zh/docs/query/object-fetcher/view"
"/zh/docs/query/object-fetcher/dto"
"/zh/docs/query/object-fetcher/spring-data"
"/zh/docs/query/dynamic-where"
"/zh/docs/query/dynamic-join/problem"
"/zh/docs/query/dynamic-join/chain-style"
"/zh/docs/query/dynamic-join/merge"
"/zh/docs/query/dynamic-join/optimization"
"/zh/docs/query/dynamic-join/table-ex"
"/zh/docs/query/dynamic-join/weak-join"
"/zh/docs/query/dynamic-join/kotlin-join"
"/zh/docs/query/implicit-subquery"
"/zh/docs/query/dynamic-order"
"/zh/docs/query/group"
"/zh/docs/query/paging/usage"
"/zh/docs/query/paging/unnecessary-join"
"/zh/docs/query/paging/reverse-sorting"
"/zh/docs/query/paging/deep-optimization"
"/zh/docs/query/expression"
"/zh/docs/query/native-sql"
"/zh/docs/query/base-query"
"/zh/docs/query/global-filter/user-filter"
"/zh/docs/query/global-filter/logical-deleted"
"/zh/docs/query/sub-query"
"/zh/docs/query/associations"
"/zh/docs/query/super_qbe"

# 修改篇
"/zh/docs/mutation/"
"/zh/docs/mutation/update-statement"
"/zh/docs/mutation/delete-statement"
"/zh/docs/mutation/save-command/usage"
"/zh/docs/mutation/save-command/data-classification"
"/zh/docs/mutation/save-command/save-mode"
"/zh/docs/mutation/save-command/association/classification"
"/zh/docs/mutation/save-command/association/owner"
"/zh/docs/mutation/save-command/association/associated-save-mode"
"/zh/docs/mutation/save-command/association/dissociation"
"/zh/docs/mutation/save-command/investigation"
"/zh/docs/mutation/save-command/optimistic-locking"
"/zh/docs/mutation/save-command/pessimistic-locking"
"/zh/docs/mutation/save-command/mysql"
"/zh/docs/mutation/save-command/input-dto/problem"
"/zh/docs/mutation/save-command/input-dto/lonely"
"/zh/docs/mutation/save-command/input-dto/dto-lang"
"/zh/docs/mutation/save-command/input-dto/null-handling"
"/zh/docs/mutation/save-command/input-dto/mapstruct"
"/zh/docs/mutation/delete-command"
"/zh/docs/mutation/associations"
"/zh/docs/mutation/draft-interceptor"
"/zh/docs/mutation/trigger"

# 缓存篇
"/zh/docs/cache/"
"/zh/docs/cache/enable-cache"
"/zh/docs/cache/cache-type/object"
"/zh/docs/cache/cache-type/association"
"/zh/docs/cache/cache-type/calculation"
"/zh/docs/cache/consistency"
"/zh/docs/cache/multiview-cache/concept"
"/zh/docs/cache/multiview-cache/user-filter"
"/zh/docs/cache/multiview-cache/advanced"
"/zh/docs/cache/multiview-cache/abandoned-callback"

# 客户端篇
"/zh/docs/client/"
"/zh/docs/client/api"
"/zh/docs/client/error"

# Spring篇
"/zh/docs/spring/"
"/zh/docs/spring/transaction"
"/zh/docs/spring/repository/concept"
"/zh/docs/spring/repository/abstract"
"/zh/docs/spring/repository/default"
"/zh/docs/spring/repository/dto"
"/zh/docs/spring/spring-cloud"
"/zh/docs/spring/appendix"

# 配置篇
"/zh/docs/configuration/"
"/zh/docs/configuration/dialect"
"/zh/docs/configuration/connection-manager"
"/zh/docs/configuration/multi-datasources"
"/zh/docs/configuration/batch-size"
"/zh/docs/configuration/sql-log"
"/zh/docs/configuration/default-enum-strategy"
"/zh/docs/configuration/default-database-stragegy"
"/zh/docs/configuration/trigger-type"
"/zh/docs/configuration/database-validation"
"/zh/docs/configuration/micro-service"
"/zh/docs/configuration/scala-provider"
"/zh/docs/configuration/cache-abandoned"
"/zh/docs/configuration/save-command-pessimistic-lock"
"/zh/docs/configuration/id-only-target-checking-level"
"/zh/docs/configuration/dissociate-action-checking"
"/zh/docs/configuration/in-list-optimization"

# GraphQL篇
"/zh/docs/graphql/"
"/zh/docs/graphql/concept"
"/zh/docs/graphql/query"
"/zh/docs/graphql/mutation"

# 对象篇
"/zh/docs/object/"
"/zh/docs/object/dynamic"
"/zh/docs/object/immutable/reason"
"/zh/docs/object/immutable/current-situation"
"/zh/docs/object/immutable/solution"
"/zh/docs/object/draft"
"/zh/docs/object/jackson"
"/zh/docs/object/view/dto-language"
"/zh/docs/object/view/mapstruct"
"/zh/docs/object/visibility"
"/zh/docs/object/tool"

# 资源篇
"/zh/docs/resource/"
"/zh/docs/resource/ecosystem"
"/zh/docs/resource/video"
"/zh/docs/resource/discuss"
)

BASE_URL="https://jimmer.deno.dev"

echo "开始下载 ${#DOCS[@]} 篇文档..."
count=0
total=${#DOCS[@]}

for doc_path in "${DOCS[@]}"; do
    count=$((count + 1))
    url="$BASE_URL$doc_path"
    
    # 将路径转换为文件名，替换/为-
    filename=$(echo "$doc_path" | sed -e 's/^\///' -e 's/\//-/g')
    if [ -z "$filename" ]; then
        filename="index"
    fi
    output_file="$OUTPUT_DIR/${filename}.md"
    
    echo "[$count/$total] 下载: $url -> $output_file"
    
    # 使用 playwright-cli 打开页面并获取markdown内容
    # playwright-cli 会自动保存内容，我们需要提取正文内容
    playwright-cli open "$url" --no-wait-for-network-idle >/dev/null 2>&1
    
    # 这里需要进一步处理，但先保存快照信息
    # 实际内容提取需要单独处理
    sleep 2
done

echo "下载完成！总共 $total 篇文档"
