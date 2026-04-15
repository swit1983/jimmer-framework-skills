---
title: '比较'
---


# 比较


二者对比如下


|  | 直接返回实体 | 使用DTO语言 |
| --- | --- | --- |
| 相同点 | 都能为客户端开发人员呈现每个API的返回类型的DTO类型定义 |  |
| 都能生成OpenApi在线文档和TypeScript RPC代码 |  |  |
| 都能把代码中的文档注释的信息展现给客户端 |  |  |
| 不同点 | 直接返回实体(易) | 需要使用DTO语言快速生成DTO类型 |
| 需要在RestController中使用@FetchBy注解为实体类型补充类型信息 | 直接基于生成的DTO类型开发RestController(易) |  |
| 更偏向对外暴露Api | 对外暴露和内部使用皆可 |  |


提示

无论用户如何选择，Jimmer都能以JVM生态中其他技术栈难以想象的低开发成本优雅地解决

.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}DTO爆炸问题@media print{.css-1k371a6{position:absolute!important;}}


[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/quick-view/fetch/export/comparison.mdx)最后 于 **2025年9月16日**  更新