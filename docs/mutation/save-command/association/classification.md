---
title: '关联分类'
---
# 关联分类


## 基本概念


可以从两种角度对关联对象进行分类，从每个角度看都有两种关联类型，共计4种


- 按照关联对象形状分类


- **短关联**


只修改当前对象和其他对象之间的关联关系，不会进一步保存关联对象。


> 递归保存行为被终止，不会继续深入。
- **长关联**


不但可以修改当前对象和其他对象之间的关联关系，还会进一步保存关联对象。


> 递归保存行为不会被终止，会继续深入。
- 按照保存顺序分类


- **前置关联**


关联对象比当前对象更早保存，其实就是基于于外键 *(无论真伪)* 的关联。


例如：本教程中的`Book.store`。
- **后置关联**


关联对象比当前对象更晚保存，包括两种情况


- 前置关联的逆关联。


例如：本教程中的`BookStore.books`
- 基于中间表的关联。


例如：本教程中的`Book.authors`和`Author.books`


## 1. 按照关联对象形状分类


### 1.1. 短关联


所谓短关联，表示仅修改当前对象和其他对象之间的关联本身，对关联对象的修改没兴趣。


通常，UI设计会采用单选框 *(关联引用)* 或多选框 *(关联集合)*。


.css-vuzb25{background-color:#fff;color:rgba(0, 0, 0, 0.87);-webkit-transition:box-shadow 300ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:box-shadow 300ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border-radius:4px;box-shadow:0px 3px 3px -2px rgba(0,0,0,0.2),0px 3px 4px 0px rgba(0,0,0,0.14),0px 1px 8px 0px rgba(0,0,0,0.12);}.css-1ov46kg{display:-webkit-box;display:-webkit-flex;display:-ms-flexbox;display:flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;}.css-1ov46kg>:not(style):not(style){margin:0;}.css-1ov46kg>:not(style)~:not(style){margin-top:16px;}

# Book Form

.css-i44wyl{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;position:relative;min-width:0;padding:0;margin:0;border:0;vertical-align:top;}.css-1jeas20{display:block;transform-origin:top left;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:calc(133% - 32px);position:absolute;left:0;top:0;-webkit-transform:translate(14px, -9px) scale(0.75);-moz-transform:translate(14px, -9px) scale(0.75);-ms-transform:translate(14px, -9px) scale(0.75);transform:translate(14px, -9px) scale(0.75);-webkit-transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,-webkit-transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;z-index:1;pointer-events:auto;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;}.css-1ald77x{color:rgba(0, 0, 0, 0.6);font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.4375em;letter-spacing:0.00938em;padding:0;position:relative;display:block;transform-origin:top left;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:calc(133% - 32px);position:absolute;left:0;top:0;-webkit-transform:translate(14px, -9px) scale(0.75);-moz-transform:translate(14px, -9px) scale(0.75);-ms-transform:translate(14px, -9px) scale(0.75);transform:translate(14px, -9px) scale(0.75);-webkit-transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,-webkit-transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;z-index:1;pointer-events:auto;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;}.css-1ald77x.Mui-focused{color:#1976d2;}.css-1ald77x.Mui-disabled{color:rgba(0, 0, 0, 0.38);}.css-1ald77x.Mui-error{color:#d32f2f;}Name@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}.css-1v4ccyo{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.4375em;letter-spacing:0.00938em;color:rgba(0, 0, 0, 0.87);box-sizing:border-box;position:relative;cursor:text;display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;position:relative;border-radius:4px;}.css-1v4ccyo.Mui-disabled{color:rgba(0, 0, 0, 0.38);cursor:default;}.css-1v4ccyo:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.87);}@media (hover: none){.css-1v4ccyo:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.23);}}.css-1v4ccyo.Mui-focused .MuiOutlinedInput-notchedOutline{border-color:#1976d2;border-width:2px;}.css-1v4ccyo.Mui-error .MuiOutlinedInput-notchedOutline{border-color:#d32f2f;}.css-1v4ccyo.Mui-disabled .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.26);}.css-1x5jdmq{font:inherit;letter-spacing:inherit;color:currentColor;padding:4px 0 5px;border:0;box-sizing:content-box;background:none;height:1.4375em;margin:0;-webkit-tap-highlight-color:transparent;display:block;min-width:0;width:100%;-webkit-animation-name:mui-auto-fill-cancel;animation-name:mui-auto-fill-cancel;-webkit-animation-duration:10ms;animation-duration:10ms;padding:16.5px 14px;}.css-1x5jdmq::-webkit-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq::-moz-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq:-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq::-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq:focus{outline:0;}.css-1x5jdmq:invalid{box-shadow:none;}.css-1x5jdmq::-webkit-search-decoration{-webkit-appearance:none;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-webkit-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-moz-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-webkit-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-moz-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus:-ms-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-ms-input-placeholder{opacity:0.42;}.css-1x5jdmq.Mui-disabled{opacity:1;-webkit-text-fill-color:rgba(0, 0, 0, 0.38);}.css-1x5jdmq:-webkit-autofill{-webkit-animation-duration:5000s;animation-duration:5000s;-webkit-animation-name:mui-auto-fill;animation-name:mui-auto-fill;}.css-1x5jdmq:-webkit-autofill{border-radius:inherit;}.css-19w1uun{border-color:rgba(0, 0, 0, 0.23);}.css-igs3ac{text-align:left;position:absolute;bottom:0;right:0;top:-5px;left:0;margin:0;padding:0 8px;pointer-events:none;border-radius:inherit;border-style:solid;border-width:1px;overflow:hidden;min-width:0%;border-color:rgba(0, 0, 0, 0.23);}.css-14lo706{float:unset;width:auto;overflow:hidden;display:block;padding:0;height:11px;font-size:0.75em;visibility:hidden;max-width:100%;-webkit-transition:max-width 100ms cubic-bezier(0.0, 0, 0.2, 1) 50ms;transition:max-width 100ms cubic-bezier(0.0, 0, 0.2, 1) 50ms;white-space:nowrap;}.css-14lo706>span{padding-left:5px;padding-right:5px;display:inline-block;opacity:0;visibility:visible;}NameEdition@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}EditionPrice@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}Price.css-tzsjye{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;position:relative;min-width:0;padding:0;margin:0;border:0;vertical-align:top;width:100%;}Store@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}.css-fvipm8{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.4375em;letter-spacing:0.00938em;color:rgba(0, 0, 0, 0.87);box-sizing:border-box;position:relative;cursor:text;display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;position:relative;border-radius:4px;}.css-fvipm8.Mui-disabled{color:rgba(0, 0, 0, 0.38);cursor:default;}.css-fvipm8:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.87);}@media (hover: none){.css-fvipm8:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.23);}}.css-fvipm8.Mui-focused .MuiOutlinedInput-notchedOutline{border-color:#1976d2;border-width:2px;}.css-fvipm8.Mui-error .MuiOutlinedInput-notchedOutline{border-color:#d32f2f;}.css-fvipm8.Mui-disabled .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.26);}.css-qiwgdb{-moz-appearance:none;-webkit-appearance:none;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;border-radius:4px;cursor:pointer;font:inherit;letter-spacing:inherit;color:currentColor;padding:4px 0 5px;border:0;box-sizing:content-box;background:none;height:1.4375em;margin:0;-webkit-tap-highlight-color:transparent;display:block;min-width:0;width:100%;-webkit-animation-name:mui-auto-fill-cancel;animation-name:mui-auto-fill-cancel;-webkit-animation-duration:10ms;animation-duration:10ms;padding:16.5px 14px;}.css-qiwgdb:focus{border-radius:4px;}.css-qiwgdb::-ms-expand{display:none;}.css-qiwgdb.Mui-disabled{cursor:default;}.css-qiwgdb[multiple]{height:auto;}.css-qiwgdb:not([multiple]) option,.css-qiwgdb:not([multiple]) optgroup{background-color:#fff;}.css-qiwgdb.css-qiwgdb.css-qiwgdb{padding-right:32px;}.css-qiwgdb.MuiSelect-select{height:auto;min-height:1.4375em;text-overflow:ellipsis;white-space:nowrap;overflow:hidden;}.css-qiwgdb::-webkit-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb::-moz-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb:-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb::-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb:focus{outline:0;}.css-qiwgdb:invalid{box-shadow:none;}.css-qiwgdb::-webkit-search-decoration{-webkit-appearance:none;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-webkit-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-moz-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-webkit-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-moz-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus:-ms-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-ms-input-placeholder{opacity:0.42;}.css-qiwgdb.Mui-disabled{opacity:1;-webkit-text-fill-color:rgba(0, 0, 0, 0.38);}.css-qiwgdb:-webkit-autofill{-webkit-animation-duration:5000s;animation-duration:5000s;-webkit-animation-name:mui-auto-fill;animation-name:mui-auto-fill;}.css-qiwgdb:-webkit-autofill{border-radius:inherit;}O'REILLY.css-1k3x8v3{bottom:0;left:0;position:absolute;opacity:0;pointer-events:none;width:100%;box-sizing:border-box;}.css-bi4s6q{position:absolute;right:7px;top:calc(50% - .5em);pointer-events:none;color:rgba(0, 0, 0, 0.54);}.css-bi4s6q.Mui-disabled{color:rgba(0, 0, 0, 0.26);}.css-1636szt{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;width:1em;height:1em;display:inline-block;fill:currentColor;-webkit-flex-shrink:0;-ms-flex-negative:0;flex-shrink:0;-webkit-transition:fill 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:fill 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;font-size:1.5rem;position:absolute;right:7px;top:calc(50% - .5em);pointer-events:none;color:rgba(0, 0, 0, 0.54);}.css-1636szt.Mui-disabled{color:rgba(0, 0, 0, 0.26);}AuthorsAuthors@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}Eve Procello ,  Alex BanksAuthors.css-13sljp9{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;position:relative;min-width:0;padding:0;margin:0;border:0;vertical-align:top;}.css-1hxq67s{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.875rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:6px 16px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;color:#fff;background-color:#1976d2;box-shadow:0px 3px 1px -2px rgba(0,0,0,0.2),0px 2px 2px 0px rgba(0,0,0,0.14),0px 1px 5px 0px rgba(0,0,0,0.12);}.css-1hxq67s:hover{-webkit-text-decoration:none;text-decoration:none;background-color:#1565c0;box-shadow:0px 2px 4px -1px rgba(0,0,0,0.2),0px 4px 5px 0px rgba(0,0,0,0.14),0px 1px 10px 0px rgba(0,0,0,0.12);}@media (hover: none){.css-1hxq67s:hover{background-color:#1976d2;}}.css-1hxq67s:active{box-shadow:0px 5px 5px -3px rgba(0,0,0,0.2),0px 8px 10px 1px rgba(0,0,0,0.14),0px 3px 14px 2px rgba(0,0,0,0.12);}.css-1hxq67s.Mui-focusVisible{box-shadow:0px 3px 5px -1px rgba(0,0,0,0.2),0px 6px 10px 0px rgba(0,0,0,0.14),0px 1px 18px 0px rgba(0,0,0,0.12);}.css-1hxq67s.Mui-disabled{color:rgba(0, 0, 0, 0.26);box-shadow:none;background-color:rgba(0, 0, 0, 0.12);}.css-1hw9j7s{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.875rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:6px 16px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;color:#fff;background-color:#1976d2;box-shadow:0px 3px 1px -2px rgba(0,0,0,0.2),0px 2px 2px 0px rgba(0,0,0,0.14),0px 1px 5px 0px rgba(0,0,0,0.12);}.css-1hw9j7s::-moz-focus-inner{border-style:none;}.css-1hw9j7s.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-1hw9j7s{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-1hw9j7s:hover{-webkit-text-decoration:none;text-decoration:none;background-color:#1565c0;box-shadow:0px 2px 4px -1px rgba(0,0,0,0.2),0px 4px 5px 0px rgba(0,0,0,0.14),0px 1px 10px 0px rgba(0,0,0,0.12);}@media (hover: none){.css-1hw9j7s:hover{background-color:#1976d2;}}.css-1hw9j7s:active{box-shadow:0px 5px 5px -3px rgba(0,0,0,0.2),0px 8px 10px 1px rgba(0,0,0,0.14),0px 3px 14px 2px rgba(0,0,0,0.12);}.css-1hw9j7s.Mui-focusVisible{box-shadow:0px 3px 5px -1px rgba(0,0,0,0.2),0px 6px 10px 0px rgba(0,0,0,0.14),0px 1px 18px 0px rgba(0,0,0,0.12);}.css-1hw9j7s.Mui-disabled{color:rgba(0, 0, 0, 0.26);box-shadow:none;background-color:rgba(0, 0, 0, 0.12);}提交@media print{.css-1k371a6{position:absolute!important;}}


其中


- 单选框对应多对一关联`Book.store`
- 多选框对应多对多关联`Book.authors`

备注

实际项目中，待选数据可能很多，并不适合设计为下拉UI。这时，可以使用具备筛选条件和分页功能的对象框来代替下拉框，这是一种常见的变通方式。


由于用户只想修改当前对象和其他对象的关联，不想进一步修改关联对象，所以UI不可能出现多层关联嵌套。这就是称其为 **短关联** 的原因。


为save指令传递任意形状的数据结构作为参数时，指定短关联有两种方法


- 将Id-Only对象作为关联对象
- 在启用专用配置的前提下，将Key-Only对象作为关联对象


#### 1.1.1. 将Id-Only对象作为关联对象


让关联对象只有id属性


- Java
- Kotlin


```
Book book = Immutables.createBook(draft -> {    draft.setName("SQL in Action");    draft.setEdition(1);    draft.setPrice(new BigDecimal("39.9"));        // 关联对象只有id属性    draft.setStoreId(2L);    draft.addIntoAuthors(author -> {        // 关联对象只有id属性        author.setId(4L);    });    draft.addIntoAuthors(author -> {        // 关联对象只有id属性        author.setId(5L);    });});sqlClient.save(book);
```


```
val book = Book {    name = "SQL in Action"    edition = 1    price = BigDecimal("39.9")    // 关联对象只有id属性    storeId = 2L    authors().addBy {        // 关联对象只有id属性        id = 4L    }    authors().addBy {        // 关联对象只有id属性        id = 5L    }}sqlClient.save(book)
```


备注

这里对被保存数据结构进行硬编码仅为示范，实际项目中被保存的数据结构由前端界面提交。


当然，如果用户按照[映射篇/进阶映射/视图属性/IdView](mapping/advanced/view/id-view)一文的方法定义了`authorIds`属性，上述代码可以被简化，例如：


- Java
- Kotlin


```
Book book = ImmutableObjects.createBook(draft -> {    draft.setAuthorIds(Arrays.asList(4L, 5L));});
```


```
val book = Book {    authorIds = listOf(4L, 5L)}
```


但这并不是必须的，为了让例子更具普适性，这里并不假设用户为实体类型定义了[IdView](mapping/advanced/view/id-view)属性。后续所有文档都如此，不再提醒。


生成如下SQL


1. 保存聚合根。


- H2
- Mysql
- Postgres


```
merge into BOOK(    NAME, EDITION, PRICE, STORE_ID) key(    NAME, EDITION) values(    ? /* SQL in Action */,     ? /* 1 */,     ? /* 39.9 */,     ? /* 2 */)
```


```
insert into BOOK(    NAME, EDITION, PRICE, STORE_ID) values(    ? /* SQL in Action */,     ? /* 1 */,     ? /* 39.9 */,     ? /* 2 */) on duplicate key update    /* fake update to return all ids */ ID = last_insert_id(ID),     PRICE = values(PRICE),     STORE_ID = values(STORE_ID)
```


```
into into into BOOK(    NAME, EDITION, PRICE, STORE_ID) values(    ? /* SQL in Action */,     ? /* 1 */,     ? /* 39.9 */,     ? /* 2 */) on conflict(    NAME, EDITION) do update set    PRICE = excluded.PRICE,     STORE_ID = excluded.STORE_IDreturning ID
```


由于`Book.store`是直接基于外键 *(STORE_ID)* 的多对一关系，当前对象和`BookStore(2)`对象的关联将会因这条SQL的执行而被自动创建。
2. 如果和当前对象 *(新插入的数据为`Book(100)`)* 关联的`Author`对象不仅仅只有`Author(4)`和`Author(5)`，切断和其他对象啊的关联。


- H2
- Mysql
- Postgres


```
delete from BOOK_AUTHOR_MAPPINGwhere     BOOK_ID = ? /* 100 */and    not (        AUTHOR_ID = any(? /* [4, 5] */)    )
```


```
delete from BOOK_AUTHOR_MAPPINGwhere     BOOK_ID = ? /* 100 */and    AUTHOR_ID not in(        ? /* 4 */,         ? /* 5 */    )
```


```
delete from BOOK_AUTHOR_MAPPINGwhere     BOOK_ID = ? /* 100 */and    not (        AUTHOR_ID = any(? /* [4, 5] */)    )
```


信息

这个步骤叫做[脱勾操作](mutation/save-command/association/dissociation)，后续文档会给予介绍，这里请读者先行忽略
3. 建立对象 *(新插入的数据为`Book(100)`)* 和`Author(4)`和`Author(5)`两个对象之间的关联


- H2
- Mysql
- Postgres


```
merge into BOOK_AUTHOR_MAPPING tb_1_ using(values(?, ?)) tb_2_(    BOOK_ID, AUTHOR_ID) on     tb_1_.BOOK_ID = tb_2_.BOOK_IDand    tb_1_.AUTHOR_ID = tb_2_.AUTHOR_ID when not matched     then insert(BOOK_ID, AUTHOR_ID)    values(tb_2_.BOOK_ID, tb_2_.AUTHOR_ID)/* batch-0: [100, 4] *//* batch-1: [100, 5] */
```

警告

默认情况下，MySQL的批量操作不会被采用，而采用多条SQL。具体细节请参考[MySQL的问题](mutation/save-command/mysql)


1. ```
insert ignoreinto BOOK_AUTHOR_MAPPING(    BOOK_ID, AUTHOR_ID) values(    ? /* 100 */, ? /* 4 */)
```
2. ```
insert ignoreinto BOOK_AUTHOR_MAPPING(    BOOK_ID, AUTHOR_ID) values(    ? /* 100 */, ? /* 5 */)
```


```
insert into BOOK_AUTHOR_MAPPING(    BOOK_ID, AUTHOR_ID) values(    ?, ?) on conflict(    BOOK_ID, AUTHOR_ID)do nothing/* batch-0: [100, 4] *//* batch-1: [100, 5] */
```

信息

通过此例，不难发现，短关联只会创建或销毁当前对象和其他对象之间的关联关系，不会进一步保存关联对象。


短关联总是假设被引用的对象是存在的，如果所引用的对象 *(这个例子中的`BookStore(2)`, `Author(4)`和`Author(5)`)* 不存在，会导致异常！


#### 1.1.2. 在启用专用配置的前提下，将Key-Only对象作为关联对象


下面代码，假设


- `BookStore`类型的key属性是`name`


.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}查看@media print{.css-1k371a6{position:absolute!important;}}
- `Author`类型的key属性是`firstName`和`lastName`


> 实际业务中，这个唯一性约束未必合理，这里为简化例子，姑且这样假设。


.css-1cp83dk{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-1cp83dk:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-1cp83dk:hover{background-color:transparent;}}.css-1cp83dk.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}.css-k58djc{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.8125rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:3px 9px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border:1px solid rgba(25, 118, 210, 0.5);color:#1976d2;}.css-k58djc::-moz-focus-inner{border-style:none;}.css-k58djc.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-k58djc{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-k58djc:hover{-webkit-text-decoration:none;text-decoration:none;background-color:rgba(25, 118, 210, 0.04);border:1px solid #1976d2;}@media (hover: none){.css-k58djc:hover{background-color:transparent;}}.css-k58djc.Mui-disabled{color:rgba(0, 0, 0, 0.26);border:1px solid rgba(0, 0, 0, 0.12);}查看@media print{.css-1k371a6{position:absolute!important;}}


- Java
- Kotlin


```
Book book = Immutables.createBook(draft -> {    draft.setName("SQL in Action");    draft.setEdition(1);    draft.setPrice(new BigDecimal("39.9"));    draft.applyStore(store -> {        // 关联对象只有key属性，即`BookStore.name`        store.setName("MANNING");    });    draft.addIntoAuthors(author -> {        // 关联对象只有key属性，即`Author.firstName`和`Author.lastName`        author.setFirstName("Boris").setLastName("Cherny");    });    draft.addIntoAuthors(author -> {        // 关联对象只有key属性，即`Author.firstName`和`Author.lastName`        author.setFirstName("Samer").setLastName("Buna");    });});sqlClient    .saveCommand(book)    .setKeyOnlyAsReference(BookProps.STORE)    .setKeyOnlyAsReference(BookProps.AUTHORS)    .execute();
```


```
val book = Book {    name = "SQL in Action"    edition = 1    price = BigDecimal("39.9")    store {        // 关联对象只有key属性，即`BookStore.name`        name = "MANNING"    }    authors().addBy {        // 关联对象只有key属性，即`Author.firstName`和`Author.lastName`        firstName = "Boris"        lastName = "Cherny"    }    authors().addBy {        // 关联对象只有key属性，即`Author.firstName`和`Author.lastName`        firstName = "Samer"        lastName = "Buna"    }}sqlClient.save(book) {    setKeyOnlyAsReference(Book::store)    setKeyOnlyAsReference(Book::authors)}
```


信息

**默认情况下key-only关联对象被视为长关联**


然而，开发人员可以通过调用`setKeyOnlyAsReference`方法将key-only关联对象视为短关联。


- 这里，两次调用`setKeyOnlyAsReference`方法，明确地配置关联`Book.store`和`Book.authors`。


事实上，你也可以调用一次`setKeyOnlyAsReferenceAll`方法，盲目地配置所有关联关系。
- 和Kotlin相比，Java API对保存指令的高级配置的便捷性稍低。


先调用`saveCommand`方法创建一个保存指令但不立即执行，完成高级配置后  ，再调用`execute`方法真正执行。


### 1.2. 长关联


所谓长关联，表示除了要修改当前对象和其他对象之间的关联本身外，还要进一步修改关联对象。


通常，订单和订单明细是这类场景的最佳示范，UI设计会采用内嵌表格，例如


.css-vuzb25{background-color:#fff;color:rgba(0, 0, 0, 0.87);-webkit-transition:box-shadow 300ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:box-shadow 300ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border-radius:4px;box-shadow:0px 3px 3px -2px rgba(0,0,0,0.2),0px 3px 4px 0px rgba(0,0,0,0.14),0px 1px 8px 0px rgba(0,0,0,0.12);}.css-isbt42{box-sizing:border-box;display:-webkit-box;display:-webkit-flex;display:-ms-flexbox;display:flex;-webkit-box-flex-wrap:wrap;-webkit-flex-wrap:wrap;-ms-flex-wrap:wrap;flex-wrap:wrap;width:100%;-webkit-flex-direction:row;-ms-flex-direction:row;flex-direction:row;margin-top:-16px;width:calc(100% + 16px);margin-left:-16px;}.css-isbt42>.MuiGrid-item{padding-top:16px;}.css-isbt42>.MuiGrid-item{padding-left:16px;}.css-1udb513{box-sizing:border-box;margin:0;-webkit-flex-direction:row;-ms-flex-direction:row;flex-direction:row;-webkit-flex-basis:33.333333%;-ms-flex-preferred-size:33.333333%;flex-basis:33.333333%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:33.333333%;}@media (min-width:600px){.css-1udb513{-webkit-flex-basis:33.333333%;-ms-flex-preferred-size:33.333333%;flex-basis:33.333333%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:33.333333%;}}@media (min-width:900px){.css-1udb513{-webkit-flex-basis:33.333333%;-ms-flex-preferred-size:33.333333%;flex-basis:33.333333%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:33.333333%;}}@media (min-width:1200px){.css-1udb513{-webkit-flex-basis:33.333333%;-ms-flex-preferred-size:33.333333%;flex-basis:33.333333%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:33.333333%;}}@media (min-width:1536px){.css-1udb513{-webkit-flex-basis:33.333333%;-ms-flex-preferred-size:33.333333%;flex-basis:33.333333%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:33.333333%;}}.css-feqhe6{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-flex-direction:column;-ms-flex-direction:column;flex-direction:column;position:relative;min-width:0;padding:0;margin:0;border:0;vertical-align:top;width:100%;}.css-1jeas20{display:block;transform-origin:top left;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:calc(133% - 32px);position:absolute;left:0;top:0;-webkit-transform:translate(14px, -9px) scale(0.75);-moz-transform:translate(14px, -9px) scale(0.75);-ms-transform:translate(14px, -9px) scale(0.75);transform:translate(14px, -9px) scale(0.75);-webkit-transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,-webkit-transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;z-index:1;pointer-events:auto;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;}.css-1ald77x{color:rgba(0, 0, 0, 0.6);font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.4375em;letter-spacing:0.00938em;padding:0;position:relative;display:block;transform-origin:top left;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:calc(133% - 32px);position:absolute;left:0;top:0;-webkit-transform:translate(14px, -9px) scale(0.75);-moz-transform:translate(14px, -9px) scale(0.75);-ms-transform:translate(14px, -9px) scale(0.75);transform:translate(14px, -9px) scale(0.75);-webkit-transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,-webkit-transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;transition:color 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,transform 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms,max-width 200ms cubic-bezier(0.0, 0, 0.2, 1) 0ms;z-index:1;pointer-events:auto;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;}.css-1ald77x.Mui-focused{color:#1976d2;}.css-1ald77x.Mui-disabled{color:rgba(0, 0, 0, 0.38);}.css-1ald77x.Mui-error{color:#d32f2f;}购买人@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}.css-1bp1ao6{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:1rem;line-height:1.4375em;letter-spacing:0.00938em;color:rgba(0, 0, 0, 0.87);box-sizing:border-box;position:relative;cursor:text;display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;width:100%;position:relative;border-radius:4px;}.css-1bp1ao6.Mui-disabled{color:rgba(0, 0, 0, 0.38);cursor:default;}.css-1bp1ao6:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.87);}@media (hover: none){.css-1bp1ao6:hover .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.23);}}.css-1bp1ao6.Mui-focused .MuiOutlinedInput-notchedOutline{border-color:#1976d2;border-width:2px;}.css-1bp1ao6.Mui-error .MuiOutlinedInput-notchedOutline{border-color:#d32f2f;}.css-1bp1ao6.Mui-disabled .MuiOutlinedInput-notchedOutline{border-color:rgba(0, 0, 0, 0.26);}.css-1x5jdmq{font:inherit;letter-spacing:inherit;color:currentColor;padding:4px 0 5px;border:0;box-sizing:content-box;background:none;height:1.4375em;margin:0;-webkit-tap-highlight-color:transparent;display:block;min-width:0;width:100%;-webkit-animation-name:mui-auto-fill-cancel;animation-name:mui-auto-fill-cancel;-webkit-animation-duration:10ms;animation-duration:10ms;padding:16.5px 14px;}.css-1x5jdmq::-webkit-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq::-moz-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq:-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq::-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-1x5jdmq:focus{outline:0;}.css-1x5jdmq:invalid{box-shadow:none;}.css-1x5jdmq::-webkit-search-decoration{-webkit-appearance:none;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-webkit-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-moz-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq::-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-webkit-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-moz-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus:-ms-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-1x5jdmq:focus::-ms-input-placeholder{opacity:0.42;}.css-1x5jdmq.Mui-disabled{opacity:1;-webkit-text-fill-color:rgba(0, 0, 0, 0.38);}.css-1x5jdmq:-webkit-autofill{-webkit-animation-duration:5000s;animation-duration:5000s;-webkit-animation-name:mui-auto-fill;animation-name:mui-auto-fill;}.css-1x5jdmq:-webkit-autofill{border-radius:inherit;}.css-qiwgdb{-moz-appearance:none;-webkit-appearance:none;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;border-radius:4px;cursor:pointer;font:inherit;letter-spacing:inherit;color:currentColor;padding:4px 0 5px;border:0;box-sizing:content-box;background:none;height:1.4375em;margin:0;-webkit-tap-highlight-color:transparent;display:block;min-width:0;width:100%;-webkit-animation-name:mui-auto-fill-cancel;animation-name:mui-auto-fill-cancel;-webkit-animation-duration:10ms;animation-duration:10ms;padding:16.5px 14px;}.css-qiwgdb:focus{border-radius:4px;}.css-qiwgdb::-ms-expand{display:none;}.css-qiwgdb.Mui-disabled{cursor:default;}.css-qiwgdb[multiple]{height:auto;}.css-qiwgdb:not([multiple]) option,.css-qiwgdb:not([multiple]) optgroup{background-color:#fff;}.css-qiwgdb.css-qiwgdb.css-qiwgdb{padding-right:32px;}.css-qiwgdb.MuiSelect-select{height:auto;min-height:1.4375em;text-overflow:ellipsis;white-space:nowrap;overflow:hidden;}.css-qiwgdb::-webkit-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb::-moz-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb:-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb::-ms-input-placeholder{color:currentColor;opacity:0.42;-webkit-transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:opacity 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;}.css-qiwgdb:focus{outline:0;}.css-qiwgdb:invalid{box-shadow:none;}.css-qiwgdb::-webkit-search-decoration{-webkit-appearance:none;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-webkit-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-moz-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb::-ms-input-placeholder{opacity:0!important;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-webkit-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-moz-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus:-ms-input-placeholder{opacity:0.42;}label[data-shrink=false]+.MuiInputBase-formControl .css-qiwgdb:focus::-ms-input-placeholder{opacity:0.42;}.css-qiwgdb.Mui-disabled{opacity:1;-webkit-text-fill-color:rgba(0, 0, 0, 0.38);}.css-qiwgdb:-webkit-autofill{-webkit-animation-duration:5000s;animation-duration:5000s;-webkit-animation-name:mui-auto-fill;animation-name:mui-auto-fill;}.css-qiwgdb:-webkit-autofill{border-radius:inherit;}皮皮鲁.css-1k3x8v3{bottom:0;left:0;position:absolute;opacity:0;pointer-events:none;width:100%;box-sizing:border-box;}.css-bi4s6q{position:absolute;right:7px;top:calc(50% - .5em);pointer-events:none;color:rgba(0, 0, 0, 0.54);}.css-bi4s6q.Mui-disabled{color:rgba(0, 0, 0, 0.26);}.css-1636szt{-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;width:1em;height:1em;display:inline-block;fill:currentColor;-webkit-flex-shrink:0;-ms-flex-negative:0;flex-shrink:0;-webkit-transition:fill 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:fill 200ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;font-size:1.5rem;position:absolute;right:7px;top:calc(50% - .5em);pointer-events:none;color:rgba(0, 0, 0, 0.54);}.css-1636szt.Mui-disabled{color:rgba(0, 0, 0, 0.26);}.css-19w1uun{border-color:rgba(0, 0, 0, 0.23);}.css-igs3ac{text-align:left;position:absolute;bottom:0;right:0;top:-5px;left:0;margin:0;padding:0 8px;pointer-events:none;border-radius:inherit;border-style:solid;border-width:1px;overflow:hidden;min-width:0%;border-color:rgba(0, 0, 0, 0.23);}.css-14lo706{float:unset;width:auto;overflow:hidden;display:block;padding:0;height:11px;font-size:0.75em;visibility:hidden;max-width:100%;-webkit-transition:max-width 100ms cubic-bezier(0.0, 0, 0.2, 1) 50ms;transition:max-width 100ms cubic-bezier(0.0, 0, 0.2, 1) 50ms;white-space:nowrap;}.css-14lo706>span{padding-left:5px;padding-right:5px;display:inline-block;opacity:0;visibility:visible;}购买人省份@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}省份城市@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}城市.css-15j76c0{box-sizing:border-box;margin:0;-webkit-flex-direction:row;-ms-flex-direction:row;flex-direction:row;-webkit-flex-basis:100%;-ms-flex-preferred-size:100%;flex-basis:100%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:100%;}@media (min-width:600px){.css-15j76c0{-webkit-flex-basis:100%;-ms-flex-preferred-size:100%;flex-basis:100%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:100%;}}@media (min-width:900px){.css-15j76c0{-webkit-flex-basis:100%;-ms-flex-preferred-size:100%;flex-basis:100%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:100%;}}@media (min-width:1200px){.css-15j76c0{-webkit-flex-basis:100%;-ms-flex-preferred-size:100%;flex-basis:100%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:100%;}}@media (min-width:1536px){.css-15j76c0{-webkit-flex-basis:100%;-ms-flex-preferred-size:100%;flex-basis:100%;-webkit-box-flex:0;-webkit-flex-grow:0;-ms-flex-positive:0;flex-grow:0;max-width:100%;}}地址@-webkit-keyframes mui-auto-fill{from{display:block;}}@keyframes mui-auto-fill{from{display:block;}}@-webkit-keyframes mui-auto-fill-cancel{from{display:block;}}@keyframes mui-auto-fill-cancel{from{display:block;}}地址.css-1key733{margin:0;-webkit-flex-shrink:0;-ms-flex-negative:0;flex-shrink:0;border-width:0;border-style:solid;border-color:rgba(0, 0, 0, 0.12);border-bottom-width:thin;display:-webkit-box;display:-webkit-flex;display:-ms-flexbox;display:flex;white-space:nowrap;text-align:center;border:0;border-top-style:solid;border-left-style:solid;}.css-1key733::before,.css-1key733::after{content:"";-webkit-align-self:center;-ms-flex-item-align:center;align-self:center;}.css-1key733::before,.css-1key733::after{width:100%;border-top:thin solid rgba(0, 0, 0, 0.12);border-top-style:inherit;}.css-1key733::before{width:10%;}.css-1key733::after{width:90%;}.css-c1ovea{display:inline-block;padding-left:calc(8px * 1.2);padding-right:calc(8px * 1.2);}订单明细.css-kge0eu{width:100%;overflow-x:auto;}.css-13xy2my{background-color:#fff;color:rgba(0, 0, 0, 0.87);-webkit-transition:box-shadow 300ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:box-shadow 300ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;border-radius:4px;box-shadow:0px 2px 1px -1px rgba(0,0,0,0.2),0px 1px 1px 0px rgba(0,0,0,0.14),0px 1px 3px 0px rgba(0,0,0,0.12);width:100%;overflow-x:auto;}.css-1owb465{display:table;width:100%;border-collapse:collapse;border-spacing:0;}.css-1owb465 caption{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:400;font-size:0.875rem;line-height:1.43;letter-spacing:0.01071em;padding:16px;color:rgba(0, 0, 0, 0.6);text-align:left;caption-side:bottom;}

| 商品 | 数量 | 单价 | 明细价 | 删除 |
| --- | --- | --- | --- | --- |
| zippo夜光流沙打火机 |  | 268 | 536 |  |
| 憨憨宠猫爬架 |  | 238 | 238 |  |
| 添加 |  |  |  |  |

总额 : 774.css-1hxq67s{font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.875rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:6px 16px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;color:#fff;background-color:#1976d2;box-shadow:0px 3px 1px -2px rgba(0,0,0,0.2),0px 2px 2px 0px rgba(0,0,0,0.14),0px 1px 5px 0px rgba(0,0,0,0.12);}.css-1hxq67s:hover{-webkit-text-decoration:none;text-decoration:none;background-color:#1565c0;box-shadow:0px 2px 4px -1px rgba(0,0,0,0.2),0px 4px 5px 0px rgba(0,0,0,0.14),0px 1px 10px 0px rgba(0,0,0,0.12);}@media (hover: none){.css-1hxq67s:hover{background-color:#1976d2;}}.css-1hxq67s:active{box-shadow:0px 5px 5px -3px rgba(0,0,0,0.2),0px 8px 10px 1px rgba(0,0,0,0.14),0px 3px 14px 2px rgba(0,0,0,0.12);}.css-1hxq67s.Mui-focusVisible{box-shadow:0px 3px 5px -1px rgba(0,0,0,0.2),0px 6px 10px 0px rgba(0,0,0,0.14),0px 1px 18px 0px rgba(0,0,0,0.12);}.css-1hxq67s.Mui-disabled{color:rgba(0, 0, 0, 0.26);box-shadow:none;background-color:rgba(0, 0, 0, 0.12);}.css-1hw9j7s{display:-webkit-inline-box;display:-webkit-inline-flex;display:-ms-inline-flexbox;display:inline-flex;-webkit-align-items:center;-webkit-box-align:center;-ms-flex-align:center;align-items:center;-webkit-box-pack:center;-ms-flex-pack:center;-webkit-justify-content:center;justify-content:center;position:relative;box-sizing:border-box;-webkit-tap-highlight-color:transparent;background-color:transparent;outline:0;border:0;margin:0;border-radius:0;padding:0;cursor:pointer;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none;vertical-align:middle;-moz-appearance:none;-webkit-appearance:none;-webkit-text-decoration:none;text-decoration:none;color:inherit;font-family:"Roboto","Helvetica","Arial",sans-serif;font-weight:500;font-size:0.875rem;line-height:1.75;letter-spacing:0.02857em;text-transform:uppercase;min-width:64px;padding:6px 16px;border-radius:4px;-webkit-transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;transition:background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms;color:#fff;background-color:#1976d2;box-shadow:0px 3px 1px -2px rgba(0,0,0,0.2),0px 2px 2px 0px rgba(0,0,0,0.14),0px 1px 5px 0px rgba(0,0,0,0.12);}.css-1hw9j7s::-moz-focus-inner{border-style:none;}.css-1hw9j7s.Mui-disabled{pointer-events:none;cursor:default;}@media print{.css-1hw9j7s{-webkit-print-color-adjust:exact;color-adjust:exact;}}.css-1hw9j7s:hover{-webkit-text-decoration:none;text-decoration:none;background-color:#1565c0;box-shadow:0px 2px 4px -1px rgba(0,0,0,0.2),0px 4px 5px 0px rgba(0,0,0,0.14),0px 1px 10px 0px rgba(0,0,0,0.12);}@media (hover: none){.css-1hw9j7s:hover{background-color:#1976d2;}}.css-1hw9j7s:active{box-shadow:0px 5px 5px -3px rgba(0,0,0,0.2),0px 8px 10px 1px rgba(0,0,0,0.14),0px 3px 14px 2px rgba(0,0,0,0.12);}.css-1hw9j7s.Mui-focusVisible{box-shadow:0px 3px 5px -1px rgba(0,0,0,0.2),0px 6px 10px 0px rgba(0,0,0,0.14),0px 1px 18px 0px rgba(0,0,0,0.12);}.css-1hw9j7s.Mui-disabled{color:rgba(0, 0, 0, 0.26);box-shadow:none;background-color:rgba(0, 0, 0, 0.12);}提交@media print{.css-1k371a6{position:absolute!important;}}


由于用户不但要修改当前对象和其他对象的关联，还要进一步修改关联对象，而关联对象可以包含更深的关联，所以，理论上讲，UI可出多层关联嵌套。这就是称其为 **长关联** 的原因。


备注

虽然设计人员为了保证UI的简洁性会刻意避免在内嵌表格中嵌套更深的内嵌表格，但是实际项目中仍然存在需要在UI上维护多层嵌套关联的场景，比如：


- 表单本身是一颗树结构，编辑好了后，作为一个整体保存。
- 可视化UI设计，因为UI组件本身就是树形结构，用户进行一系列可视化拖拉拽的设计后，把UI组件树作为一个整体保存。


Jimmer可以直接保存任意形状的长关联数据结构，如果把深度未知的长关联数据结构称为复杂表单，**保存指令就是为复杂表单而设计。**


例子如下


- Java
- Kotlin


```
Order order = Immutables.createOrder(draft -> {    draft.setCustomerId(1L);    draft.setProvince("四川");    draft.setCity("成都");    draft.setAddress("龙泉驿区洪玉路与十洪路交叉口");    draft.addIntoItems(item -> {        item.setProductId(8L);        item.setQuantity(2);    });    draft.addIntoItems(item -> {        item.setProductId(9L);        item.setQuantity(1);    });});sqlClient.insert(order);
```


```
val order = Order {    customerId = 1L    province = "四川"    city = "成都"    address = "龙泉驿区洪玉路与十洪路交叉口"    items().addBy {        productId = 8L        quantity = 2    }    items().addBy {        productId = 9L        quantity = 1    }}sqlClient.insert(order)
```


在这个例子中，我们可以看到很多短关联，例如`Order.customer`, `OrderItem.product`，但是，们并非这里应该关注的重点。


在这里，我们应该关注关联`Order.items`，很明显，它是一个长关联。


此操作会生成两条SQL


1. 插入根对象`Order`


```
insert into order_(    PROVINCE, CITY, ADDRESS, CUSTOMER_ID) values(    ? /* 四川 */,     ? /* 成都 */,     ? /* 龙泉驿区洪玉路与十洪路交叉口 */,     ? /* 1 */)
```
2. 插入所有子对象`OrderItem`


- 绝大部分数据库
- Mysql


```
insert into ORDER_ITEM(    ORDER_ID,     PRODUCT_ID,     QUANTITY) values(?, ?, ?)/* batch-0: [100, 8, 2] *//* batch-1: [100, 9, 1] */
```

警告

默认情况下，MySQL的批量操作不会被采用，而采用多条SQL。具体细 节请参考[MySQL的问题](mutation/save-command/mysql)


1. ```
insert into ORDER_ITEM(    ORDER_ID,     PRODUCT_ID,     QUANTITY) values(    ? /* 100 */,     ? /* 8 */,     ? /* 2 */)
```
2. ```
insert into ORDER_ITEM(    ORDER_ID,     PRODUCT_ID,     QUANTITY) values(    ? /* 100 */,     ? /* 9 */,     ? /* 1 */)
```

信息

由此可见，长关联不仅能修改当前对象和其他对象的关联关系，还是会导致关联对象被保存。


如果关联对象也具备长关联，将会递归保存，直到没有更多关联属性或遇到短关联为止。


## 2. 按照保存顺序分类


### 2.1. 前置关联


前置关联就是基于外键 *(无论真伪)* 的关联，其工作模式为，先保存关联对象，再保存根对象。


- Java
- Kotlin


```
Book book = Immutables.createBook(draft -> {    draft.setName("SQL in Action");    draft.setEdition(1);    draft.setPrice(new BigDecimal("49.9"));    draft.applyStore(store -> {        store.setName("TURING");        store.setWebsite("https://www.turing.com");    });});sqlClient.save(book);
```


```
val book = Book {    name = "SQL in Action"    edition = 1    price = BigDecimal("49.9")    store {        name = "TURING"        website = "https://www.turing.com"    }}sqlClient.save(book)
```


以H2为例，生成两条SQL


1. 先保存关联对象`BookStore`


- H2
- Mysql
- Postgres


```
merge into BOOK_STORE(    NAME, WEBSITE) key(    NAME) values(    ? /* TURING */,     ? /* https://www.turing.com */)
```


```
insert into BOOK_STORE(    NAME, WEBSITE) values(    ? /* TURING */,     ? /* https://www.turing.com */) on duplcate update    /* fake update to return all ids */ ID = last_insert_id(ID),    WEBSITE = VALUES(WEBSITE)
```


```
insert into BOOK_STORE(    NAME, WEBSITE) values(    ? /* TURING */,     ? /* https://www.turing.com */) on conflict(    NAME, WEBSITE) do update set    WEBSITE = excluded.WEBSITE,return ID
```
2. 后保存当前对象`Book` *(假设上个操作返回的id为`100`)*


- H2
- Mysql
- Postgres


```
merge into BOOK(    NAME, EDITION, PRICE, STORE_ID) key(    NAME, EDITION) values(    ? /* SQL in Action */,     ? /* 1 */,     ? /* 49.9 */,     ? /* 100 */)
```


```
insert into BOOK(    NAME, EDITION, PRICE, STORE_ID) values(    ? /* SQL in Action */,     ? /* 1 */,     ? /* 49.9 */,     ? /* 100 */) on duplcate key update    /* fake update to return all ids */ ID = last_insert_id(ID),     PRICE = values(PRICE),     STORE_ID = values(STORE_ID)
```


```
insert into BOOK(    NAME, EDITION, PRICE, STORE_ID) values(    ? /* SQL in Action */,     ? /* 1 */,     ? /* 49.9 */,     ? /* 100 */) on conflict(    NAME, EDITION) do update set    PRICE = values(PRICE),     STORE_ID = values(STORE_ID)returning ID
```

警告

在工作交流中，面对前置关联时，建议用"当前对象/关联对象"这样的方式来表达，而不是"父对象/子对象"这种表达方式。


因为，对于前置关联而言，ORM层面的父子关系和数据库建模层面的父子关系完全相反，非常容易引起混淆和误会。


### 2.2. 后置关联


其他关联，例如


- 前置关联的逆关联，*(本教程中的`BookStore.books`)*
- 基于中间表的关联，*(本教程中的`Book.authors`和`Author.books`)*


都可以归为后置关联，是一种更常见的场景。


后置关联的工作模式更容易理解，先保存当前对象，再保存关联对象。


- Java
- Kotlin


```
BookStore store = Immutables.createBookStore(draft -> {    draft.setName("TURING");    draft.setWebsite("https://www.turing.com");    draft.addIntoBooks(book -> {        book.setName("SQL in Action");        book.setEdition(1);        book.setPrice(new BigDecimal("49.9"));    });    draft.addIntoBooks(book -> {        book.setName("RUST programming");        book.setEdition(2);        book.setPrice(new BigDecimal("39.9"));    });});sqlClient    .saveCommand(store)    // 请读者先行忽略此配置    .setTargetTransferModeAll(TargetTransferMode.ALLOWED)    .execute();
```


```
val store = BookStore {    name = "TURING"    website = "https://www.turing.com"    books().addBy {        name = "SQL in Action"        edition = 1        price = BigDecimal("49.9")    }    books().addBy {        name = "RUST programming"        edition = 2        price = BigDecimal("39.9")    }}sqlClient.save(store) {    // 请读者先行忽略此配置    setTargetTransferModeAll(TargetTransferMode.ALLOWED)}
```


以H2为例，生成三条SQL


1. 先保存当前对象`BookStore`


- H2
- Mysql
- Postgres


```
merge into BOOK_STORE(    NAME, WEBSITE) key(    NAME) values(    ? /* TURING */,     ? /* https://www.turing.com */)
```


```
insert into BOOK_STORE(    NAME, WEBSITE) values(    ? /* TURING */,     ? /* https://www.turing.com */) on duplcate update    /* fake update to return all ids */ ID = last_insert_id(ID),    WEBSITE = VALUES(WEBSITE)
```


```
insert into BOOK_STORE(    NAME, WEBSITE) values(    ? /* TURING */,     ? /* https://www.turing.com */) on conflict(    NAME, WEBSITE) do update set    WEBSITE = excluded.WEBSITE,return ID
```
2. 后保存关联对象`Book` *(假设上个操作返回的id为`100`)*


- H2
- Mysql
- Postgres


```
merge into BOOK(    NAME, EDITION, PRICE, STORE_ID) key(    NAME, EDITION) values(?, ?, ?, ?)/* batch-0: [SQL in Action, 1, 49.9, 100] *//* batch-1: [RUST programming, 2, 39.9, 100] */
```

警告

默认情况下，MySQL的批量操作不会被采用，而采用多条SQL。具体细节请参考[MySQL的问题](mutation/save-command/mysql)


1. ```
insert into BOOK(    NAME, EDITION, PRICE, STORE_ID) values(    ? /* SQL in Action */,     ? /* 1 */,     ? /* 49.9 */,     ? /* 100 */) on duplcate update     /* fake update to return all ids */ ID = last_insert_id(ID),     PRICE = VALUES(PRICE),    STORE_ID = VALUES(STORE_ID)
```
2. ```
insert into BOOK(    NAME, EDITION, PRICE, STORE_ID) values(    ? /* RUST programming */,     ? /* 2 */,     ? /* 39.9 */,     ? /* 100 */) on duplcate update     /* fake update to return all ids */ ID = last_insert_id(ID),     PRICE = VALUES(PRICE),    STORE_ID = VALUES(STORE_ID)
```


```
insert into BOOK(    NAME, EDITION, PRICE, STORE_ID) values(    ?, ?, ?, ?) on conflict(    NAME, EDITION) do update set    PRICE = excluded.PRICE,    STORE_ID = excluded.STORE_ID/* batch-0: [SQL in Action, 1, 49.9, 100] *//* batch-1: [RUST programming, 2, 39.9, 100] */
```
3. 第三条SQL和这里讨论的话题无关，省略

信息

后置关联的功能比前置关联丰  富，本教程讲重点讨论后置关联

[编辑此页](https://github.com/babyfish-ct/jimmer-doc/edit/main/i18n/zh/docusaurus-plugin-content-docs/current/mutation/save-command/association/classification.mdx)最后 于 **2025年9月16日**  更新
- [基本概念](#基本概念)
- [1. 按照关联对象形状分类](#1-按照关联对象形状分类)
- [1.1. 短关联](#11-短关联)
- [1.2. 长关联](#12-长关联)
- [2. 按照保存顺序分类](#2-按照保存顺序分类)
- [2.1. 前置关联](#21-前置关联)
- [2.2. 后置关联](#22-后置关联)