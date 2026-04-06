#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Markdown文档格式化工具
统一规范Jimmer框架文档的Markdown格式
"""

import os
import re
import sys
from pathlib import Path
from typing import List, Tuple


class MarkdownFormatter:
    """Markdown格式化处理类"""

    def __init__(self, file_path: str):
        self.file_path = file_path
        self.original_content = ""
        self.formatted_content = ""
        self.fixes = []

    def read_file(self) -> bool:
        """读取文件内容"""
        try:
            with open(self.file_path, 'r', encoding='utf-8') as f:
                self.original_content = f.read()
            return True
        except Exception as e:
            print(f"  错误: 无法读取文件 {self.file_path}: {e}")
            return False

    def format_content(self) -> str:
        """格式化Markdown内容"""
        content = self.original_content

        # 1. 规范化换行符
        content = self._normalize_line_endings(content)

        # 2. 规范化标题层级
        content = self._normalize_headings(content)

        # 3. 规范化代码块
        content = self._normalize_code_blocks(content)

        # 4. 修复表格格式
        content = self._normalize_tables(content)

        # 5. 修复列表缩进
        content = self._normalize_lists(content)

        # 6. 规范化链接格式
        content = self._normalize_links(content)

        # 7. 规范化分隔线
        content = self._normalize_hr(content)

        # 8. 修复空行问题
        content = self._normalize_empty_lines(content)

        self.formatted_content = content
        return content

    def _normalize_line_endings(self, content: str) -> str:
        """规范化换行符为LF"""
        # 将CRLF转换为LF
        return content.replace('\r\n', '\n')

    def _normalize_headings(self, content: str) -> str:
        """规范化标题层级"""
        lines = content.split('\n')
        result = []

        for i, line in enumerate(lines):
            # 修复行首的标题标记（去除多余空格）
            if re.match(r'^#+\s', line):
                # 规范化标题格式：确保#后只有一个空格
                line = re.sub(r'^(#+)\s*', r'\1 ', line)

                # 检查标题层级是否连续（可选的警告）
                heading_level = len(re.match(r'^(#+)', line).group(1))
                if heading_level > 6:
                    self.fixes.append(f"标题层级过深（{heading_level}级）: {line[:50]}")

            result.append(line)

        return '\n'.join(result)

    def _normalize_code_blocks(self, content: str) -> str:
        """规范化代码块格式"""
        # 匹配代码块（支持带语言标识的和不带的）
        def replace_code_block(match):
            backticks = match.group(1)
            # 尝试获取语言标识，可能为空
            lang_info = match.group(2) if len(match.groups()) > 1 else ''
            code = match.group(3) if len(match.groups()) > 2 else ''

            # 如果lang_info包含换行符，说明匹配方式不对，需要重新解析
            if lang_info and '\n' in lang_info:
                # 重新解析: lang_info实际上是代码内容
                parts = lang_info.split('\n', 1)
                if len(parts) == 2:
                    lang = parts[0].strip()
                    code = parts[1] + '\n' + code if code else parts[1]
                else:
                    lang = ''
                    code = lang_info + '\n' + code if code else lang_info
            else:
                lang = lang_info.strip() if lang_info else ''

            # 规范化语言标识
            lang = lang.lower() if lang else ''
            lang_map = {
                'js': 'javascript',
                'ts': 'typescript',
                'py': 'python',
                'sh': 'bash',
                'shell': 'bash',
                'kt': 'kotlin',
            }
            lang = lang_map.get(lang, lang)

            # 去除代码块内部的尾随空白行
            if code:
                lines = code.rstrip().split('\n')
                # 去除代码块开头的空行（保留缩进）
                while lines and not lines[0].strip():
                    lines.pop(0)
                code = '\n'.join(lines)

            if lang:
                return f'```{lang}\n{code}\n```' if code else f'```{lang}\n```'
            else:
                return f'```\n{code}\n```' if code else '```\n```'

        # 匹配 ```lang\ncode\n``` 格式
        # 使用更宽松的模式
        pattern = r'```([\w]*)\n(.*?)```'
        content = re.sub(pattern, replace_code_block, content, flags=re.DOTALL)

        return content

    def _normalize_tables(self, content: str) -> str:
        """修复表格格式"""
        lines = content.split('\n')
        result = []
        in_table = False
        table_lines = []

        for line in lines:
            # 检测表格行（包含 | 的行）
            if '|' in line:
                in_table = True
                table_lines.append(line)
            else:
                if in_table:
                    # 处理收集到的表格行
                    formatted_table = self._format_table_rows(table_lines)
                    result.extend(formatted_table)
                    table_lines = []
                    in_table = False
                result.append(line)

        # 处理文件末尾的表格
        if table_lines:
            formatted_table = self._format_table_rows(table_lines)
            result.extend(formatted_table)

        return '\n'.join(result)

    def _format_table_rows(self, table_lines: List[str]) -> List[str]:
        """格式化表格行"""
        if not table_lines:
            return []

        # 解析表格内容
        rows = []
        for line in table_lines:
            # 分割单元格，保留空单元格
            cells = [cell.strip() for cell in line.split('|')]
            # 去除首尾空单元格（由行首行尾的|产生）
            if cells and not cells[0]:
                cells = cells[1:]
            if cells and not cells[-1]:
                cells = cells[:-1]
            rows.append(cells)

        if not rows:
            return table_lines

        # 计算每列的最大宽度
        num_cols = max(len(row) for row in rows)
        col_widths = [0] * num_cols
        for row in rows:
            for i, cell in enumerate(row):
                col_widths[i] = max(col_widths[i], len(cell))

        # 格式化每一行
        formatted = []
        for row_idx, row in enumerate(rows):
            # 补齐单元格
            row = row + [''] * (num_cols - len(row))
            # 格式化行
            formatted_row = '| ' + ' | '.join(
                cell.ljust(col_widths[i]) for i, cell in enumerate(row)
            ) + ' |'
            formatted.append(formatted_row)

            # 在表头后添加分隔行
            if row_idx == 0:
                separator = '|' + '|'.join(
                    '-' * (col_widths[i] + 2) for i in range(num_cols)
                ) + '|'
                formatted.append(separator)

        return formatted

    def _normalize_lists(self, content: str) -> str:
        """修复列表缩进"""
        lines = content.split('\n')
        result = []

        for line in lines:
            # 检测无序列表项
            match = re.match(r'^(\s*)[-*+](\s*)(.+)$', line)
            if match:
                indent = match.group(1)
                space = match.group(2)
                content_text = match.group(3)
                # 确保列表标记后有一个空格
                if not space:
                    line = f"{indent}- {content_text}"
                else:
                    line = f"{indent}- {content_text}"

            # 检测有序列表项
            match = re.match(r'^(\s*)(\d+)[\.\)](\s*)(.+)$', line)
            if match:
                indent = match.group(1)
                number = match.group(2)
                space = match.group(3)
                content_text = match.group(4)
                # 确保列表标记后有一个空格
                line = f"{indent}{number}. {content_text}"

            result.append(line)

        return '\n'.join(result)

    def _normalize_links(self, content: str) -> str:
        """规范化链接格式"""
        # 修复链接格式 [text](url)
        # 确保链接文本和URL之间没有多余空格
        def fix_link(match):
            text = match.group(1).strip()
            url = match.group(2).strip()
            return f'[{text}]({url})'

        content = re.sub(r'\[([^\]]+)\]\s*\(\s*([^)]+)\s*\)', fix_link, content)

        # 修复图片格式 ![alt](url)
        def fix_image(match):
            alt = match.group(1).strip()
            url = match.group(2).strip()
            return f'![{alt}]({url})'

        content = re.sub(r'!\[([^\]]*)\]\s*\(\s*([^)]+)\s*\)', fix_image, content)

        return content

    def _normalize_hr(self, content: str) -> str:
        """规范化分隔线"""
        # 统一分隔线为 ---
        lines = content.split('\n')
        result = []

        for line in lines:
            stripped = line.strip()
            # 匹配各种分隔线格式
            if re.match(r'^[\*\-_]{3,}$', stripped):
                line = '---'
            result.append(line)

        return '\n'.join(result)

    def _normalize_empty_lines(self, content: str) -> str:
        """规范化空行"""
        # 去除行尾空白字符
        lines = content.split('\n')
        result = []

        for line in lines:
            # 去除行尾空白
            line = line.rstrip()
            result.append(line)

        content = '\n'.join(result)

        # 规范化空行数量
        # 确保标题后有适当的空行
        content = re.sub(r'^(#{1,6}\s.+?)\n([^\n])', r'\1\n\n\2', content, flags=re.MULTILINE)

        # 确保代码块前后有适当的空行
        content = re.sub(r'([^\n])\n(```)', r'\1\n\n\2', content)
        content = re.sub(r'(```)\n([^\n])', r'\1\n\n\2', content)

        # 合并过多的连续空行（最多保留2个）
        content = re.sub(r'\n{4,}', '\n\n\n', content)

        return content

    def write_file(self) -> bool:
        """写入格式化后的内容"""
        try:
            with open(self.file_path, 'w', encoding='utf-8') as f:
                f.write(self.formatted_content)
            return True
        except Exception as e:
            print(f"  错误: 无法写入文件 {self.file_path}: {e}")
            return False

    def has_changes(self) -> bool:
        """检查是否有修改"""
        return self.original_content != self.formatted_content


def find_markdown_files(directory: str) -> List[str]:
    """查找所有Markdown文件"""
    md_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.md'):
                md_files.append(os.path.join(root, file))
    return md_files


def main():
    docs_dir = '/root/.openclaw/workspace/.agents/skills/jimmer-framework/docs'

    print("=" * 70)
    print("Markdown文档格式化工具")
    print("=" * 70)
    print()

    # 查找所有Markdown文件
    print("正在查找Markdown文件...")
    md_files = find_markdown_files(docs_dir)
    print(f"找到 {len(md_files)} 个Markdown文件")
    print()

    # 统计信息
    processed_count = 0
    modified_count = 0
    error_count = 0
    fix_summary = {
        'line_endings': 0,
        'headings': 0,
        'code_blocks': 0,
        'tables': 0,
        'lists': 0,
        'links': 0,
        'horizontal_rules': 0,
        'empty_lines': 0,
    }

    # 处理每个文件
    for idx, file_path in enumerate(md_files, 1):
        rel_path = os.path.relpath(file_path, docs_dir)
        print(f"[{idx}/{len(md_files)}] 处理: {rel_path}")

        formatter = MarkdownFormatter(file_path)

        if not formatter.read_file():
            error_count += 1
            continue

        try:
            formatter.format_content()
        except Exception as e:
            print(f"  错误: 格式化失败: {e}")
            error_count += 1
            continue

        if formatter.has_changes():
            if formatter.write_file():
                modified_count += 1
                print(f"  ✓ 已修复")
            else:
                error_count += 1
        else:
            print(f"  - 无需修改")

        processed_count += 1

    print()
    print("=" * 70)
    print("处理完成")
    print("=" * 70)
    print(f"总文件数: {len(md_files)}")
    print(f"成功处理: {processed_count}")
    print(f"修改文件: {modified_count}")
    print(f"错误数量: {error_count}")
    print()
    print("主要修复问题:")
    print("  1. 规范化换行符（统一为LF）")
    print("  2. 统一标题层级格式")
    print("  3. 规范代码块格式（统一使用 ```language 格式）")
    print("  4. 修复表格对齐和格式")
    print("  5. 统一列表缩进")
    print("  6. 规范化链接和图片格式")
    print("  7. 统一分隔线格式")
    print("  8. 规范化空行和段落间距")
    print()


if __name__ == '__main__':
    main()
