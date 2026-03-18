#!/usr/bin/env python3
"""Parses a JaCoCo XML report and prints a formatted coverage summary to stdout."""

import sys
import xml.etree.ElementTree as ET

def pct(covered, missed):
    total = covered + missed
    if total == 0:
        return "n/a", 100
    return f"{covered}/{total} ({covered * 100 // total}%)", covered * 100 // total

def main(report_path):
    try:
        tree = ET.parse(report_path)
    except FileNotFoundError:
        print(f"\n  [Coverage] Report not found: {report_path}\n")
        sys.exit(0)

    root = tree.getroot()

    col_w = 44
    print()
    print("  " + "=" * 65)
    print("  MACRobo – Test Coverage Summary")
    print("  " + "=" * 65)
    header = f"  {'Class':<{col_w}} {'Lines':>12} {'Branches':>12}"
    print(header)
    print("  " + "-" * 65)

    for pkg in sorted(root.findall(".//package"), key=lambda p: p.get("name", "")):
        for cls in sorted(pkg.findall("class"), key=lambda c: c.get("sourcefilename", "")):
            name = cls.get("sourcefilename", cls.get("name", "?")).replace(".java", "")
            if "Test" in name:
                continue

            line_el   = cls.find("counter[@type='LINE']")
            branch_el = cls.find("counter[@type='BRANCH']")

            if line_el is not None:
                line_str, _ = pct(int(line_el.get("covered", 0)), int(line_el.get("missed", 0)))
            else:
                line_str = "n/a"

            if branch_el is not None:
                branch_str, _ = pct(int(branch_el.get("covered", 0)), int(branch_el.get("missed", 0)))
            else:
                branch_str = "n/a"

            short = name.split("/")[-1]
            print(f"  {short:<{col_w}} {line_str:>12} {branch_str:>12}")

    print("  " + "-" * 65)

    totals = {}
    for ctr in root.findall("counter"):
        t = ctr.get("type")
        totals[t] = (int(ctr.get("covered", 0)), int(ctr.get("missed", 0)))

    for label, ctype in [("Lines", "LINE"), ("Branches", "BRANCH"), ("Instructions", "INSTRUCTION")]:
        if ctype in totals:
            cov, mis = totals[ctype]
            s, p = pct(cov, mis)
            bar_len = p * 20 // 100
            bar = "█" * bar_len + "░" * (20 - bar_len)
            print(f"  {label + ' total':<{col_w}} {s:>12}  {bar}")

    print("  " + "=" * 65)
    print()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: coverage-report.py <path-to-jacoco.xml>")
        sys.exit(1)
    main(sys.argv[1])
