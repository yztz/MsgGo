#!/usr/bin/env python3
"""
Generate test ODS fixtures for MsgGo OdsSpreadsheetParser.

Dependencies:
    pip install odfpy faker

Usage:
    python3 tools/gen_test_ods.py [output_dir]

Output dir defaults to tools/ods_fixtures/.
Each fixture tests a distinct parser behaviour; the expected outcome is noted
in the fixture name or the comment next to its entry in FIXTURES.
"""

import os
import sys
from datetime import date

from faker import Faker
from odf.opendocument import OpenDocumentSpreadsheet
from odf.table import Table, TableRow, TableCell
from odf.text import P

fake = Faker("zh_CN")
Faker.seed(42)  # reproducible data across runs


# --------------------------------------------------------------------------- #
# Cell helpers
# --------------------------------------------------------------------------- #

def str_cell(value: str) -> TableCell:
    tc = TableCell(valuetype="string")
    tc.addElement(P(text=str(value)))
    return tc


def float_cell(value: float) -> TableCell:
    tc = TableCell(valuetype="float", value=str(value))
    tc.addElement(P(text=str(value)))
    return tc


def date_cell(d: date) -> TableCell:
    iso = d.isoformat()
    tc = TableCell(valuetype="date", datevalue=iso)
    tc.addElement(P(text=iso))
    return tc


def empty_cell() -> TableCell:
    return TableCell()


# --------------------------------------------------------------------------- #
# Row / table helpers
# --------------------------------------------------------------------------- #

def make_row(*cells: TableCell) -> TableRow:
    tr = TableRow()
    for c in cells:
        tr.addElement(c)
    return tr


def make_doc(*rows: TableRow) -> OpenDocumentSpreadsheet:
    doc = OpenDocumentSpreadsheet()
    table = Table(name="Sheet1")
    doc.spreadsheet.addElement(table)
    for r in rows:
        table.addElement(r)
    return doc


def save(doc: OpenDocumentSpreadsheet, path: str) -> None:
    os.makedirs(os.path.dirname(path), exist_ok=True)
    doc.save(path)
    size = os.path.getsize(path)
    print(f"  {os.path.basename(path):35s} {size:>8,} bytes")


# --------------------------------------------------------------------------- #
# Fixtures
# --------------------------------------------------------------------------- #

def gen_basic(path: str) -> None:
    """Standard contact list: name / phone / city — all string cells."""
    header = make_row(str_cell("姓名"), str_cell("电话"), str_cell("城市"))
    rows = [header] + [
        make_row(str_cell(fake.name()), str_cell(fake.phone_number()), str_cell(fake.city()))
        for _ in range(20)
    ]
    save(make_doc(*rows), path)


def gen_mixed_types(path: str) -> None:
    """Mix of string, float, and date cells — exercises all three cell readers."""
    header = make_row(str_cell("姓名"), str_cell("电话"), str_cell("年龄"), str_cell("生日"))
    rows = [header] + [
        make_row(
            str_cell(fake.name()),
            str_cell(fake.phone_number()),
            float_cell(float(fake.random_int(18, 70))),
            date_cell(fake.date_of_birth(minimum_age=18, maximum_age=70)),
        )
        for _ in range(15)
    ]
    save(make_doc(*rows), path)


def gen_empty_rows_in_middle(path: str) -> None:
    """Blank rows scattered between data — parser must skip them."""
    header = make_row(str_cell("姓名"), str_cell("电话"))
    rows = [header]
    for i in range(10):
        rows.append(make_row(str_cell(fake.name()), str_cell(fake.phone_number())))
        if i % 3 == 2:
            rows.append(make_row(empty_cell(), empty_cell()))  # blank row
    save(make_doc(*rows), path)


def gen_single_column(path: str) -> None:
    """Only one data column — edge case for column count detection."""
    rows = [make_row(str_cell("电话"))] + [
        make_row(str_cell(fake.phone_number())) for _ in range(10)
    ]
    save(make_doc(*rows), path)


def gen_large(path: str, n: int = 1000) -> None:
    """Large file with many rows — stress test."""
    header = make_row(str_cell("姓名"), str_cell("电话"), str_cell("城市"), str_cell("地址"))
    rows = [header] + [
        make_row(
            str_cell(fake.name()),
            str_cell(fake.phone_number()),
            str_cell(fake.city()),
            str_cell(fake.address().replace("\n", " ")),
        )
        for _ in range(n)
    ]
    save(make_doc(*rows), path)


def gen_unicode_heavy(path: str) -> None:
    """Emoji and multi-script content — exercises UTF-8 handling."""
    header = make_row(str_cell("姓名"), str_cell("电话"), str_cell("备注"))
    notes = [
        "🎉重要客户🎉", "VIP⭐⭐⭐", "备注：café & naïve",
        "한국어混合English", "日本語テスト", "مرحبا", "Ünïcödé tëst",
    ]
    rows = [header] + [
        make_row(str_cell(fake.name()), str_cell(fake.phone_number()), str_cell(notes[i % len(notes)]))
        for i in range(len(notes))
    ]
    save(make_doc(*rows), path)


def gen_long_content(path: str) -> None:
    """Very long cell values — exercises buffer handling."""
    header = make_row(str_cell("姓名"), str_cell("电话"), str_cell("详细地址"))
    rows = [header] + [
        make_row(
            str_cell(fake.name()),
            str_cell(fake.phone_number()),
            str_cell("".join(fake.address() for _ in range(5)).replace("\n", "")),
        )
        for _ in range(10)
    ]
    save(make_doc(*rows), path)


# --- Error cases (parser should raise an exception) ---

def gen_header_only(path: str) -> None:
    """Header row but no data — expect error_empty_content."""
    save(make_doc(make_row(str_cell("姓名"), str_cell("电话"))), path)


def gen_empty_sheet(path: str) -> None:
    """Completely empty sheet — expect error_no_header."""
    save(make_doc(), path)


# --------------------------------------------------------------------------- #
# Main
# --------------------------------------------------------------------------- #

FIXTURES = [
    # (filename,                     generator,                  note)
    ("basic.ods",                    gen_basic,                  ""),
    ("mixed_types.ods",              gen_mixed_types,            ""),
    ("empty_rows_in_middle.ods",     gen_empty_rows_in_middle,   ""),
    ("single_column.ods",            gen_single_column,          ""),
    ("large.ods",                    gen_large,                  ""),
    ("unicode_heavy.ods",            gen_unicode_heavy,          ""),
    ("long_content.ods",             gen_long_content,           ""),
    ("header_only.ods",              gen_header_only,            "expect: error_empty_content"),
    ("empty_sheet.ods",              gen_empty_sheet,            "expect: error_no_header"),
]

if __name__ == "__main__":
    out_dir = sys.argv[1] if len(sys.argv) > 1 else "tools/ods_fixtures"
    print(f"Generating {len(FIXTURES)} ODS fixtures → {out_dir}/\n")
    for filename, fn, note in FIXTURES:
        suffix = f"  [{note}]" if note else ""
        fn(os.path.join(out_dir, filename))
        if note:
            print(f"    ^ {note}")
    print("\nDone.")
