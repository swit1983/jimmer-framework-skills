---
title: 'IdView'
---
# IdView


## 基本概念：短关联


在介绍Id视图之前，我们要先介绍一个概念：短关联。


在介绍短关联之前，我们先看一看普通关联


- Java
- Kotlin


```
Book book = bookRepository.findNullable(    1L,    Fetchers.BOOK_FETCHER        .allScalarFields()        .store(            Fetchers.BOOK_STORE_FETCHER                .allScalarFields()        )        .authors(            Fetchers.AUTHOR_FETCHER                .firstName()                .lastName()        ));System.out.println(book);
```


```
val book = bookRepository.findNullable(    1L,    newFetcher(Book::class).by {        allScalarFields()        store {            allScalarFields()        }        authors {            firstName()            lastName()        }    });System.out.println(book);
```


代码中


- 通过关联属性`Book.store`关联抓取关联对象`BookStore`，并期望获得关联对象的所有非关联属性
- 通过关联属性`Book.authors`关联抓取关联对象`Author`，并期望获得关联对象的的`id`(隐含+强制)、`firstName`和`lastName`


输出结果为


```
{    "id":1,    "name":"Learning GraphQL",    "edition":1,    "price":45,    "store":{        "id":1,        "name":"O'REILLY",        "website":null    },    "authors":[        {            "id":2,            "firstName":"Alex",            "lastName":"Banks"        },        {            "id":1,            "firstName":"Eve",            "lastName":"Procello"        }    ]}
```


这里，聚合根对象`Book`上的关联对象，`BookStore`和`Author`，具备除了id以外的其他属性，具有相对完善的信息。


更重要的是，非`id`属性当然也包括关联属性，所以此数据结构可以多层嵌套甚至递归，这种关联也可以叫做“长关联”。


信息

然而，并非所有时候都需要层次很深的数据结构。实际项目中，有时需要的只是一种非常简单的界面，如下


.css-vuzb25{background-color:#fff;color:rgba(0, 0, 0, 0.87);-webkit-transition:box-shadow 300ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:box-shadow 300ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border-radius:4px;box-shadow:0px 3px 3px -2px rgba(0,0,0,0.2),0px 3px 4px 0px rgba(0,0,0,0.14),0px 1px 8px 0px rgba(0,0,0,0.12);}.css-1ov46kg{display:-webkit-box;display:-webkit-flex;display:-ms-flexbox;display:flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;}.css-1ov46kg>:not(style):not(style){margin:0;}.css-1ov46kg>:not(style)~:not(style){margin-top:16px;}

# Book Form

.css-i44wyl{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;position:relative;min-width:0;padding:0;margin:0;border:0;vertical-align:top;}.css-1jeas20{display:block;transform-origin:top left;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:calc(133% - 32px);position:absolute;left:0;top:0;-webkit-transform:translate(14px, -9px) scale(0.75);-moz-transform:translate(14px, -9px) scale(0.75);-ms-transform:translate(14px, -9px) scale(0.75);transform:translate(14px, -9px) scale(0.75);-webkit-transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,-webkit-transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;z-index:1;pointer-events:auto;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;}.css-1ald77x{color:rgba(0, 0, 0, 0.6);font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.4375em;letter-spacing:0.00938em;padding:0;position:relative;display:block;transform-origin:top left;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:calc(133% - 32px);position:absolute;left:0;top:0;-webkit-transform:translate(14px, -9px) scale(0.75);-moz-transform:translate(14px, -9px) scale(0.75);-ms-transform:translate(14px, -9px) scale(0.75);transform:translate(14px, -9px) scale(0.75);-webkit-transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,-webkit-transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;z-index:1;pointer-events:auto;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;}.css-1ald77x.Mui-focused{color:#1976d2;}.css-1ald77x.Mui-disabled{color:rgba(0, 0, 0, 0.38);}.css-1ald77x.Mui-error{color:#d32f2f;}Name@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}.css-1v4ccyo{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.4375em;letter-spacing:0.00938em;color:rgba(0, 0, 0, 0.87);box-sizing:border-box;position:relative;cursor:text;display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;position:relative;border-radius:4px;}.css-1v4ccyo.Mui-disabled{color:rgba(0, 0, 0, 0.38);cursor:default;}.css-1v4ccyo:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.87);}@media (hover: none){.css-1v4ccyo:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.23);}}.css-1v4ccyo.Mui-focused .MuiOutlinedInput-notchedOutline{border-color:#1976d2;border-width:2px;}.css-1v4ccyo.Mui-error .MuiOutlinedInput-notchedOutline{border-color:#d32f2f;}.css-1v4ccyo.Mui-disabled .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.26);}.css-1x5jdmq{font:inherit;letter-spacing:inherit;color:currentColor;padding:4px 0 5px;border:0;box-sizing:content-box;background:none;height:1.4375em;margin:0;-webkit-tap-highlight-color:transparent;display:block;min-width:0;width:100%;-webkit-animation-name:mui-auto-fill-cancel;animation-name:mui-auto-fill-cancel;-webkit-animation-duration:10ms;animation-duration:10ms;padding:16.5px 14px;}.css-1x5jdmq::-webkit-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq::-moz-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq:-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq::-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq:focus{outline:0;}.css-1x5jdmq:invalid{box-shadow:none;}.css-1x5jdmq::-webkit-search-decoration{-webkit-appearance:none;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-webkit-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-moz-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-webkit-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-moz-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus:-ms-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-ms-input-placeholder{opacity:0.42;}.css-1x5jdmq.Mui-disabled{opacity:1;-webkit-text-fill-color:rgba(0, 0, 0, 0.38);}.css-1x5jdmq:-webkit-autofill{-webkit-animation-duration:5000s;animation-duration:5000s;-webkit-animation-name:mui-auto-fill;animation-name:mui-auto-fill;}.css-1x5jdmq:-webkit-autofill{border-radius:inherit;}.css-19w1uun{border-color:rgba(0, 0, 0, 0.23);}.css-igs3ac{text-align:left;position:absolute;bottom:0;right:0;top:-5px;left:0;margin:0;padding:0 8px;pointer-events:none;border-radius:inherit;border-style:solid;border-width:1px;overflow:hidden;min-width:0%;border-color:rgba(0, 0, 0, 0.23);}.css-14lo706{float:unset;width:auto;overflow:hidden;display:block;padding:0;height:11px;font-size:0.75em;visibility:hidden;max-width:100%;-webkit-transition:max-width 100ms cubic-bezier(0.0, 0, 0.2, 1) 50ms;transition:max-width 100ms cubic-bezier(0.0, 0, 0.2, 1) 50ms;white-space:nowrap;}.css-14lo706>span{padding-left:5px;padding-right:5px;display:inline-block;opacity:0;visibility:visible;}NameEdition@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}EditionPrice@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}Price.css-tzsjye{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;position:relative;min-width:0;padding:0;margin:0;border:0;vertical-align:top;width:100%;}Store@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}.css-fvipm8{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.4375em;letter-spacing:0.00938em;color:rgba(0, 0, 0, 0.87);box-sizing:border-box;position:relative;cursor:text;display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;position:relative;border-radius:4px;}.css-fvipm8.Mui-disabled{color:rgba(0, 0, 0, 0.38);cursor:default;}.css-fvipm8:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.87);}@media (hover: none){.css-fvipm8:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.23);}}.css-fvipm8.Mui-focused .MuiOutlinedInput-notchedOutline{border-color:#1976d2;border-width:2px;}.css-fvipm8.Mui-error .MuiOutlinedInput-notchedOutline{border-color:#d32f2f;}.css-fvipm8.Mui-disabled .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.26);}.css-qiwgdb{-moz-appearance:none;-webkit-appearance:none;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;border-radius:4px;cursor:pointer;font:inherit;letter-spacing:inherit;color:currentColor;padding:4px 0 5px;border:0;box-sizing:content-box;background:none;height:1.4375em;margin:0;-webkit-tap-highlight-color:transparent;display:block;min-width:0;width:100%;-webkit-animation-name:mui-auto-fill-cancel;animation-name:mui-auto-fill-cancel;-webkit-animation-duration:10ms;animation-duration:10ms;padding:16.5px 14px;}.css-qiwgdb:focus{border-radius:4px;}.css-qiwgdb::-ms-expand{display:none;}.css-qiwgdb.Mui-disabled{cursor:default;}.css-qiwgdb[multiple]{height:auto;}.css-qiwgdb:not([multiple]) option,.css-qiwgdb:not([multiple]) optgroup{background-color:#fff;}.css-qiwgdb.css-qiwgdb.css-qiwgdb{padding-right:32px;}.css-qiwgdb.MuiSelect-select{height:auto;min-height:1.4375em;text-overflow:ellipsis;white-space:nowrap;overflow:hidden;}.css-qiwgdb::-webkit-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb::-moz-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb:-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb::-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb:focus{outline:0;}.css-qiwgdb:invalid{box-shadow:none;}.css-qiwgdb::-webkit-search-decoration{-webkit-appearance:none;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-webkit-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-moz-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-webkit-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-moz-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus:-ms-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-ms-input-placeholder{opacity:0.42;}.css-qiwgdb.Mui-disabled{opacity:1;-webkit-text-fill-color:rgba(0, 0, 0, 0.38);}.css-qiwgdb:-webkit-autofill{-webkit-animation-duration:5000s;animation-duration:5000s;-webkit-animation-name:mui-auto-fill;animation-name:mui-auto-fill;}.css-qiwgdb:-webkit-autofill{border-radius:inherit;}O'REILLY.css-1k3x8v3{bottom:0;left:0;position:absolute;opacity:0;pointer-events:none;width:100%;box-sizing:border-box;}.css-bi4s6q{position:absolute;right:7px;top:calc(50% - .5em);pointer-events:none;color:rgba(0, 0, 0, 0.54);}.css-bi4s6q.Mui-disabled{color:rgba(0, 0, 0, 0.26);}.css-1636szt{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;width:1em;height:1em;display:inline-block;fill:currentColor;-webkit-flex-shrink:0;-ms-flex-negative:0;flex-shrink:0;-webkit-transition:fill 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:fill 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;font-size:1.5rem;position:absolute;right:7px;top:calc(50% - .5em);pointer-events:none;color:rgba(0, 0, 0, 0.54);}.css-1636szt.Mui-disabled{color:rgba(0, 0, 0, 0.26);}AuthorsAuthors@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}Eve Procello ,  Alex BanksAuthors.css-13sljp9{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;position:relative;min-width:0;padding:0;margin:0;border:0;vertical-align:top;}.css-1hxq67s{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.875rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:6px 16px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;color:#fff;background-color:#1976d2;box-shadow:0px 3px 1px -2px rgba(0,0,0,0.2),0px 2px 2px 0px rgba(0,0,0,0.14),0px 1px 5px 0px rgba(0,0,0,0.12);}.css-1hxq67s:hover{-webkit-text-decoration:none;text-decoration:none;background-color:#1565c0;box-shadow:0px 2px 4px -1px rgba(0,0,0,0.2),0px 4px 5px 0px rgba(0,0,0,0.14),0px 1px 10px 0px rgba(0,0,0,0.12);}@media (hover: none){.css-1hxq67s:hover{background-color:#1976d2;}}.css-1hxq67s:active{box-shadow:0px 5px 5px -3px rgba(0,0,0,0.2),0px 8px 10px 1px rgba(0,0,0,0.14),0px 3px 14px 2px rgba(0,0,0,0.12);}.css-1hxq67s.Mui-focusVisible{box-shadow:0px 3px 5px -1px rgba(0,0,0,0.2),0px 6px 10px 0px rgba(0,0,0,0.14),0px 1px 18px 0px rgba(0,0,0,0.12);}.css-1hxq67s.Mui-disabled{color:rgba(0, 0, 0, 0.26);box-shadow:none;background-color:rgba(0, 0, 0, 0.12);}.css-1hw9j7s{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.875rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:6px 16px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;color:#fff;background-color:#1976d2;box-shadow:0px 3px 1px -2px rgba(0,0,0,0.2),0px 2px 2px 0px rgba(0,0,0,0.14),0px 1px 5px 0px rgba(0,0,0,0.12);}.css-1hw9j7s::-moz-focus-inner{border-style:none;}.css-1hw9j7s.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-1hw9j7s{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-1hw9j7s:hover{-webkit-text-decoration:none;text-decoration:none;background-color:#1565c0;box-shadow:0px 2px 4px -1px rgba(0,0,0,0.2),0px 4px 5px 0px rgba(0,0,0,0.14),0px 1px 10px 0px rgba(0,0,0,0.12);}@media (hover: none){.css-1hw9j7s:hover{background-color:#1976d2;}}.css-1hw9j7s:active{box-shadow:0px 5px 5px -3px rgba(0,0,0,0.2),0px 8px 10px 1px rgba(0,0,0,0.14),0px 3px 14px 2px rgba(0,0,0,0.12);}.css-1hw9j7s.Mui-focusVisible{box-shadow:0px 3px 5px -1px rgba(0,0,0,0.2),0px 6px 10px 0px rgba(0,0,0,0.14),0px 1px 18px 0px rgba(0,0,0,0.12);}.css-1hw9j7s.Mui-disabled{color:rgba(0, 0, 0, 0.26);box-shadow:none;background-color:rgba(0, 0, 0, 0.12);}提交@media print{.css-1k371a6{position:absolute!important;}}


在这个界面中


- `Book.store`是多对一关联，在界面上体现为单选下拉框
- `Book.authors`是多对多关联，在界面上体现为多选下拉框


> *当然，如果候选数据很多，下拉列表不再是合理的设计，这时，改进为弹出对话框并在对话框中使用分页。但，这些UI细节不重要，和现有话题无关。*


很明显，这时，用户只关注关联对象对象的`id`，对关联对象的其他属性没有兴趣。


**即, 希望关联对象只有id属性**


为了让聚合根挂上一些只有id的的关联对象，我们可以改进代码。


- Java
- Kotlin


```
Book book = bookRepository.findNullable(    1L,    Fetchers.BOOK_FETCHER        .allScalarFields()        .store() //无参数表示id only        .authors() //无参数表示id only);System.out.println(book);
```


```
val book = bookRepository.findNullable(    1L,    newFetcher(Book::class).by {        allScalarFields()        store() //无参数表示id only        authors() //无参数表示id only    });System.out.println(book);
```


这次，我们得到了这样的数据结构


```
{    "id":1,    "name":"Learning GraphQL",    "edition":1,    "price":45,    "store":{        // 只有id属性        "id":1    },    "authors":[        {            // 只有id属性            "id":1        },        {            // 只有id属性            "id":2        }    ]}
```


备注

在Hibernate中，这种只有id属性的对象被称为代理对象。


但是，只有id的关联对象，并没有关联id那么简单。让同样的的数据用关联id而非关联对象来表达的样子。


```
{    "id":1,    "name":"Learning GraphQL",    "edition":1,    "price":45,    "storeId": 1,    "authorIds":[1, 2]}
```


很明显，对于短关联业务而言，关联id或其集合比只有id的关联对象或其集合简单。


## Microsoft的解决方案


`ADO.NET EF Core`是Microsoft的ORM  ，让我们来看看其设计: [https://learn.microsoft.com/en-us/ef/core/modeling/relationships?tabs=fluent-api%2Cfluent-api-simple-key%2Csimple-key](https://learn.microsoft.com/en-us/ef/core/modeling/relationships?tabs=fluent-api%2Cfluent-api-simple-key%2Csimple-key)


这段C#代码从上面的链接的页面复制

```
public class Post{    public int PostId { get; set; }    public string Title { get; set; }    public string Content { get; set; }    public int BlogId { get; set; }    public Blog Blog { get; set; }}
```


不难发现


- 关联对象: `public Blog Blog { get; set; }`
- 关联id: `public int BlogId { get; set; }`


二者并存。


Jimmer借鉴`ADO.NET EF Core`这种设计，提供了`@IdView`属性。


## IdView属性


### 声明视图属性


IdView属性由`@org.babyfish.jimmer.sql.IdView`声明


- Java
- Kotlin
Book.java

```
package com.example.model;import org.babyfish.jimmer.sql.*;import org.jetbrains.annotations.Nullable;@Entitypublic interface Book {    ...省略其他属性...    @ManyToOne    @Nullable    BookStore store();    @ManyToMany    @JoinTable(        name = "BOOK_AUTHOR_MAPPING",        joinColumnName = "BOOK_ID",        inverseJoinColumnName = "AUTHOR_id"    )    List<Author> authors();    @IdView // 关联对象store的id的视图    Long storeId();    // 关联对象集合authors中所有对象的id的视图    @IdView("authors")     List<Long> authorIds();}
```

Book.kt

```
package com.example.modelimport org.babyfish.jimmer.sql.*@Entityinterface Book {    ...省略其他属性...    @ManyToOne    val store: BookStore?    @ManyToMany    @JoinTable(        name = "BOOK_AUTHOR_MAPPING",        joinColumnName = "BOOK_ID",        inverseJoinColumnName = "AUTHOR_id"    )    val authors: List<Author>    @IdView // 关联对象store的id的视图    val storeId: Long?    // 关联对象集合authors中所有对象的id的视图    @IdView("authors")     val authorIds: List<Long>}
```


其中：


- `Book.storeId`: 关联`Book.store`对象的id的视图。


- `storeId`本身以`Id`结尾，这种情况下，可以不指定`@IdView`注解的参数，Jimmer认为该视图属性的原始关联属性为`Book.store`。
- 原始关联属性和IdView属性的可空性必须一致。


在这个例子中，`Book.store`属性可以为null，即，Java版本被`@Nullable修饰`，Kotlin版本返回`BookStore?`。


因此，`Book.storeId`也必须可以为null，即，Java版本返回必须返回`Long`而非`long`，Kotlin版本必须返回`Long?`而非`Long`。


否则，会导致编译错误。
- `Book.authorIds`: 关联`Book.authors`对象集合中，所有Author对象的id形成的视图。


`authorIds`本身不以`Id`结尾，必须指定`@IdView`注解的参数，明确表示其原始关联为`Book.authors`。


> 这种情况下，需要这样做的原因是英文存在不规则名词复数变形的问题。


### 视图的本质


上文反复强调`视图`二字是有原因的。IdView属性并没有自己的数据，它只是原始关联属性的视图。


信息

IdView属性和原始关联属性是联动的，设置一个，必然影响另外一个。


- 设置视图属性，影响原始属性:


- Java
- Kotlin


```
// 设置视图属性Book book = Immutables.createBook(draft -> {    draft.setStoreId(10L);    draft.setAuthorIds(Arrays.asList(100L, 101L));});// 打印原始属性System.out.println("Store: " + book.store());System.out.println("Authors:" + book.authors());
```


```
// 设置视图属性val book = Book {    storeId = 10L    authorIds = listOf(100L, 101L)}// 打印原始属性println("Store: $book.store}")println("Authors: ${book.authors}")
```


打印结果：


```
Store: {"id":10}Authors: [{"id":100},{"id":101}]
```
- 设置原始属性，影响视图属性:


- Java
- Kotlin


```
// 设置原始属性Book book = Immutables.createBook(draft -> {    draft.applyStore(store -> {        store.setId(10L).storeName("TURING")    });    draft.addIntoAuthors(author -> {        author.setId(101L);        author.setFirstName("Fabrice");        author.setLastName("Marguerie");    });    draft.addIntoAuthors(author -> {        author.setId(101L);        author.setFirstName("Steve");        author.setLastName("Eichert");     });});// 打印视图属性System.out.println("StoreId: " + book.storeId());System.out.println("AuthorIds:" + book.authorIds());
```


```
// 设置原始属性val book = Book {    store {        id = 10L        name = "TURING"    }    authors().addBy {        id = 100L;        firstName = "Fabrice"        lastName = "Marguerie"    }    authors().addBy {        id = 101L        firstName = "Steve"        lastName = "Eichert"     }}// 打印视图属性println("Store: $book.storeId}")println("Authors: ${book.authorIds}")
```


打印结果：


```
StoreId: 10AuthorIds: [100, 101]
```

提示

这说明视图属性和原始属性是高度统一的，Jimmer仍然是以关联对象为核心的ORM框架，视图属性仅仅是一种语法糖。


除了接下来要讲解的视图属性对[对象抓取器](query/object-fetcher)的影响外，视图属性对ORM和核心逻辑不会造成任何影响。


## 抓取IdView属性


- Java
- Kotlin


```
Book book = bookRepository.findNullable(    1L,    Fetchers.BOOK_FETCHER        .allScalarFields()        .storeId()        .authorIds());System.out.println(book);
```


```
val book = bookRepository.findNullable(    1L,    newFetcher(Book::class).by {        allScalarFields()        storeId()        authorIds()    });System.out.println(book);
```


打印结果为


```
{    "id":1,    "name":"Learning GraphQL",    "edition":1,    "price":45,    "storeId": 1,    "authorIds":[1, 2]}
```


提示

对Jimmer动态实体而言，原始关联属性和视图属性绝对一致，  要么都可以访问，要么都缺失。


无论选择抓取原始关联属性，还是选择抓取IdView视图属性，都不会影响Jimmer底层执行逻辑，当然包括最终生成的SQL。


不同选择带来差异只有一个，原始关联属性和视图属性的Jackson[可见性标志](object/visibility)不同。


即，使用Jackson序列化时，被直接抓取的属性会被序列化，未被直接抓取的属性会被忽略。


## 请勿滥用


警告

不借助DTO，希望实体本身能表达关联id，是唯一适合采用`@IdView`的场景。


其他功能并不对关联属性是否有对应的`@IdView`属性做任何假设。


- 在SQL DSL中使用关联id


即使实体的某个一对一或多对一关联属性没有对应的`@IdView`属性，也可以在SQL DSL中使用关联id表达式，例如


- Java
- Kotlin


```
where(table.storeId().eq(2L));
```


```
where(table.storeId eq 2L)
```


> 当然，如果你对SQL DSL自动生成的关联id名称 *(比如，这里的`storeId`)* 并不满意，就可以提供`@IdView`属性改变其名称。
- 在DTO语言中使用关联id


DTO语言根本不需要`@IdView`属性。即使实体的某个关联属性已经具备了对应的`@IdView`属性，也不建议在DTO语言中使用它，因为这是一个脆弱的假设，一旦那个`@IdView`属性被删除，DTO代码在同步修改前无法被正确编译。


DTO语言应该直接使用关联属性，例如


```
export yourpackage.Book    -> package yourpackage.dtoinput BookInput {    allScalarFields()    id(store) // as storeId    id(authors) as authorIds}specification BookSpecification {    like/i(name)    associatedIdIn(store) as storeIds    associatedIdNotIn(store) as excludedStoreIds}
```
[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mapping/advanced/view/id-view.mdx)最后 于 **2025年9月16日**  更新
- [基本概念：短关联](#基本概念短关联)
- [Microsoft的解决方案](#microsoft的解决方案)
- [IdView属性](#idview属性)
- [声明视图属性](#声明视图属性)
- [视图的本质](#视图的本质)
- [抓取IdView属性](#抓取idview属性)
- [请勿滥用](#请勿滥用)