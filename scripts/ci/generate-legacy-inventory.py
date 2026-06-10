#!/usr/bin/env python3
"""Generate the Phase 0 legacy workflow inventory.

The inventory is intentionally conservative. It records legacy artifacts,
assigns an initial owner category, and names the deletion proof required before
any file or route can be removed.
"""

from __future__ import annotations

import argparse
import csv
import re
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable

ROOT = Path.cwd()
DEFAULT_OUTPUT_DIR = Path("build/reports/legacy")


@dataclass(frozen=True)
class Artifact:
    artifact_id: str
    artifact_type: str
    path: str
    symbol: str
    route_or_mapping: str
    classification: str
    phase_slice: str
    deletion_gate: str
    notes: str


def rel(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="ignore")


def java_class_name(path: Path, text: str) -> str:
    package = ""
    package_match = re.search(r"^\s*package\s+([\w.]+);", text, re.MULTILINE)
    if package_match:
        package = package_match.group(1)
    class_match = re.search(r"\b(?:class|interface|enum)\s+(\w+)", text)
    name = class_match.group(1) if class_match else path.stem
    return f"{package}.{name}" if package else name


def extract_web_xml_mappings() -> tuple[dict[str, str], dict[str, list[str]]]:
    web_xml = ROOT / "web/src/main/webapp/WEB-INF/web.xml"
    if not web_xml.exists():
        return {}, {}

    root = ET.parse(web_xml).getroot()

    def child_text(node: ET.Element, local_name: str) -> str | None:
        for child in list(node):
            if child.tag.split("}")[-1] == local_name:
                return (child.text or "").strip()
        return None

    servlet_classes: dict[str, str] = {}
    servlet_mappings: dict[str, list[str]] = defaultdict(list)
    for node in root.iter():
        tag = node.tag.split("}")[-1]
        if tag == "servlet":
            name = child_text(node, "servlet-name")
            cls = child_text(node, "servlet-class")
            if name and cls:
                servlet_classes[name] = cls
        elif tag == "servlet-mapping":
            name = child_text(node, "servlet-name")
            pattern = child_text(node, "url-pattern")
            if name and pattern:
                servlet_mappings[name].append(pattern)

    class_to_routes: dict[str, list[str]] = defaultdict(list)
    for name, cls in servlet_classes.items():
        class_to_routes[cls].extend(servlet_mappings.get(name, []))
    return servlet_classes, class_to_routes


def first_request_mapping(line: str) -> str:
    stripped = line.strip()
    if stripped.startswith("//") or stripped.startswith("/*") or stripped.startswith("*"):
        return ""
    if "@RequestMapping" not in stripped:
        return ""
    for pattern in (
        r"value\s*=\s*\{\s*\"([^\"]+)\"",
        r"value\s*=\s*\"([^\"]+)\"",
        r"path\s*=\s*\"([^\"]+)\"",
        r"@RequestMapping\(\s*\"([^\"]+)\"",
    ):
        match = re.search(pattern, stripped)
        if match:
            return match.group(1)
    return stripped


def classify(artifact_type: str, path: str, symbol: str, route_or_mapping: str) -> tuple[str, str, str, str]:
    haystack = " ".join([artifact_type, path, symbol, route_or_mapping]).lower()

    if artifact_type == "soap-endpoint":
        return (
            "keep compatibility",
            "phase-2-soap-retirement",
            "Identify external consumers, add replacement REST/module contract or formal deprecation, then remove endpoint registration.",
            "SOAP compatibility endpoint; do not delete without consumer proof.",
        )
    if artifact_type == "dao-spi":
        return (
            "keep compatibility",
            "phase-3-dao-implementation-deletion",
            "Keep until callers depend on module-owned ports instead of legacy SPI names.",
            "SPI bridge surface; deletion waits for caller migration.",
        )
    if artifact_type in {"dao-implementation", "dao-support"}:
        return (
            "replace",
            "phase-3-dao-implementation-deletion",
            "Every provided method must be module-backed or proven unused; no Spring bean, factory, inheritance, or runtime reference may remain.",
            "Legacy DAO surface.",
        )
    if artifact_type in {"shared-service", "scheduled-job"}:
        return (
            "replace" if "job" not in haystack else "unknown",
            "phase-4-shared-service-deletion",
            "Move required behavior to module-owned services or retire caller; prove no servlet/SOAP/DAO/runtime references remain.",
            "Shared legacy service/job surface.",
        )

    if any(token in haystack for token in ("odm", "openrosa", "clinicaldata", "metadata", "download", "print", "import")):
        return (
            "keep compatibility",
            "phase-1-import-export-compatibility",
            "Preserve until import/export/ODM/OpenRosa contract tests and versioned replacements prove compatibility.",
            "Compatibility-sensitive import/export or ODM path.",
        )
    if any(token in haystack for token in ("healthcheck", "systemstatus", "systemcontroller", "audit", "viewlogmessage", "logmessage", "techadmin", "scheduler", "viewjob", "viewalljobs", "viewsinglejob")):
        return (
            "replace",
            "phase-1-admin-read-only",
            "Prove SPA/API route parity, SecureController permission parity, audit visibility, route removal, and targeted tests.",
            "Candidate for first low-risk admin read-only slice.",
        )
    if any(token in haystack for token in ("configure", "passwordrequirements", "createjob", "updatejob", "pausejob")):
        return (
            "replace",
            "phase-1-admin-write",
            "Prove write validation, permission parity, audit output, rollback behavior, and route removal before deleting.",
            "Admin write workflow; not first deletion candidate.",
        )
    if any(token in haystack for token in ("login", "logout", "password", "profile", "requestaccount", "requeststudy", "contact", "changestudy", "enterprise")):
        return (
            "replace",
            "phase-1-login-profile",
            "Prove Spring Security/session behavior and SPA/default route parity before deleting JSP/servlet path.",
            "Login/profile flow.",
        )
    if any(token in haystack for token in ("crf", "form", "section", "item")):
        return (
            "replace",
            "phase-1-crf-metadata",
            "Prove CRF metadata SPA/API parity, validation, permissions, audit, and import/export side effects.",
            "CRF/form metadata workflow.",
        )
    if any(token in haystack for token in ("dataset", "filter", "extract", "export")):
        return (
            "replace",
            "phase-1-export-dataset-filter",
            "Prove export/dataset/filter API parity, generated file compatibility, permissions, audit, and historical behavior.",
            "Dataset/filter/export workflow.",
        )
    if any(token in haystack for token in ("dataentry", "discrepancy", "note", "submit", "sdv")):
        return (
            "replace",
            "phase-1-data-entry-discrepancy",
            "Prove data entry, discrepancy note, SDV, validation, audit, and rollback behavior before deleting.",
            "Data capture/discrepancy workflow.",
        )
    if any(token in haystack for token in ("study", "subject", "event", "site", "userrole", "useraccount", "account")):
        return (
            "replace",
            "phase-1-study-subject-event",
            "Prove study/subject/event management parity, permissions, audit, and route removal before deleting.",
            "Study/subject/event workflow.",
        )
    if artifact_type == "jsp-view" and "menu.jsp" in path:
        return (
            "keep compatibility",
            "phase-1-layout-common",
            "Main landing page and permission-error redirect target (Page.MENU/MENU_SERVLET used by 10+ servlets); redirect to SPA /app/dashboard before deletion.",
            "Main menu page; includes sideAlert.jsp and sideInfo.jsp.",
        )
    if artifact_type == "jsp-view" and "/include/" in path:
        if any(f in path for f in ("sideAlert.jsp", "sideInfo.jsp")):
            return (
                "keep compatibility",
                "phase-1-layout-common",
                "Shared sidebar panel; delete last after ALL JSP pages are migrated. Used by menu.jsp, index.jsp, data-entry, and import JSPs.",
                "Sidebar layout fragment used across data-entry, study, and import workflows.",
            )
        if "workflow.jsp" in path:
            return (
                "replace",
                "phase-1-study-subject-event",
                "Breadcrumb workflow box; delete when managestudy/index.jsp (sole consumer) is migrated.",
                "Workflow breadcrumb display included only by index.jsp.",
            )
        return (
            "replace",
            "phase-1-layout-common",
            "Layout fragment; delete after all referring JSPs are migrated.",
            "Popup/message JS initialization included by shared headers.",
        )

    return (
        "unknown",
        "phase-0-inventory-and-gates",
        "Assign owner category, replacement status, route/API usage, and deletion gate before changing code.",
        "Needs manual classification.",
    )


def artifact(artifact_type: str, path: str, symbol: str, route_or_mapping: str) -> Artifact:
    classification, phase_slice, deletion_gate, notes = classify(artifact_type, path, symbol, route_or_mapping)
    artifact_id = f"{artifact_type}:{path}:{symbol}:{route_or_mapping}"
    return Artifact(artifact_id, artifact_type, path, symbol, route_or_mapping, classification, phase_slice, deletion_gate, notes)


def collect_secure_controllers(class_to_routes: dict[str, list[str]]) -> Iterable[Artifact]:
    base = ROOT / "web/src/main/java"
    if not base.exists():
        return []
    items: list[Artifact] = []
    for path in sorted(base.rglob("*.java")):
        text = read_text(path)
        match = re.search(r"\bclass\s+(\w+)\b[^\n{]*\bextends\s+(SecureController|CoreSecureController)\b", text)
        if not match:
            continue
        fqcn = java_class_name(path, text)
        routes = sorted(set(class_to_routes.get(fqcn, [])))
        items.append(artifact("legacy-servlet", rel(path), fqcn, ";".join(routes) if routes else "(no web.xml mapping found)"))
    return items


def collect_spring_routes() -> Iterable[Artifact]:
    base = ROOT / "web/src/main/java"
    if not base.exists():
        return []
    items: list[Artifact] = []
    for path in sorted(base.rglob("*.java")):
        text = read_text(path)
        if "@RequestMapping" not in text:
            continue
        fqcn = java_class_name(path, text)
        for lineno, line in enumerate(text.splitlines(), start=1):
            mapping = first_request_mapping(line)
            if mapping:
                items.append(artifact("spring-mvc-route", rel(path), f"{fqcn}:{lineno}", mapping))
    return items


def collect_jsps() -> Iterable[Artifact]:
    base = ROOT / "web/src/main/webapp"
    if not base.exists():
        return []
    return [artifact("jsp-view", rel(path), path.name, "/" + rel(path).removeprefix("web/src/main/webapp/")) for path in sorted(base.rglob("*.jsp"))]


def collect_soap_endpoints() -> Iterable[Artifact]:
    base = ROOT / "ws/src/main/java"
    if not base.exists():
        return []
    items: list[Artifact] = []
    for path in sorted(base.rglob("*.java")):
        text = read_text(path)
        if "@Endpoint" not in text:
            continue
        fqcn = java_class_name(path, text)
        roots = re.findall(r"@PayloadRoot\([^)]*localPart\s*=\s*\"([^\"]+)\"", text, flags=re.DOTALL)
        route = "/ws/*" + (" localPart=" + ";".join(sorted(set(roots))) if roots else "")
        items.append(artifact("soap-endpoint", rel(path), fqcn, route))
    return items


def collect_dao_files() -> Iterable[Artifact]:
    base = ROOT / "shared/src/main/java/org/researchedc/dao"
    if not base.exists():
        return []
    items: list[Artifact] = []
    for path in sorted(base.rglob("*.java")):
        text = read_text(path)
        fqcn = java_class_name(path, text)
        p = rel(path)
        if "/spi/" in p:
            artifact_type = "dao-spi"
        elif re.search(r"\bclass\s+\w+", text):
            artifact_type = "dao-implementation"
        else:
            artifact_type = "dao-support"
        items.append(artifact(artifact_type, p, fqcn, "(dao file)"))
    return items


def collect_shared_services() -> Iterable[Artifact]:
    base = ROOT / "shared/src/main/java/org/researchedc/service"
    if not base.exists():
        return []
    return [artifact("shared-service", rel(path), java_class_name(path, read_text(path)), "(shared service)") for path in sorted(base.rglob("*.java"))]


def collect_jobs() -> Iterable[Artifact]:
    roots = [ROOT / "web/src/main/java", ROOT / "ws/src/main/java", ROOT / "shared/src/main/java", ROOT / "app/src/main/java"]
    items: list[Artifact] = []
    for base in roots:
        if not base.exists():
            continue
        for path in sorted(base.rglob("*.java")):
            text = read_text(path)
            if "QuartzJobBean" not in text and not re.search(r"\bimplements\s+Job\b", text):
                continue
            items.append(artifact("scheduled-job", rel(path), java_class_name(path, text), "(quartz/job class)"))
    return items


def build_inventory() -> list[Artifact]:
    _, class_to_routes = extract_web_xml_mappings()
    items: list[Artifact] = []
    for collector in (
        lambda: collect_secure_controllers(class_to_routes),
        collect_spring_routes,
        collect_jsps,
        collect_soap_endpoints,
        collect_dao_files,
        collect_shared_services,
        collect_jobs,
    ):
        items.extend(collector())

    seen: set[str] = set()
    unique: list[Artifact] = []
    for item in sorted(items, key=lambda a: (a.artifact_type, a.path, a.symbol, a.route_or_mapping)):
        if item.artifact_id in seen:
            continue
        seen.add(item.artifact_id)
        unique.append(item)
    return unique


def write_csv(items: list[Artifact], path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow([
            "artifact_type",
            "path",
            "symbol",
            "route_or_mapping",
            "classification",
            "phase_slice",
            "deletion_gate",
            "notes",
        ])
        for item in items:
            writer.writerow([
                item.artifact_type,
                item.path,
                item.symbol,
                item.route_or_mapping,
                item.classification,
                item.phase_slice,
                item.deletion_gate,
                item.notes,
            ])


def markdown_escape(value: str) -> str:
    return value.replace("|", "\\|").replace("\n", " ")


def write_markdown(items: list[Artifact], path: Path, csv_name: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    generated = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")
    by_type = Counter(item.artifact_type for item in items)
    by_class = Counter(item.classification for item in items)
    by_phase = Counter(item.phase_slice for item in items)

    phase_priority = [
        "phase-1-admin-read-only",
        "phase-1-login-profile",
        "phase-1-admin-write",
        "phase-1-crf-metadata",
        "phase-1-study-subject-event",
        "phase-1-export-dataset-filter",
        "phase-1-import-export-compatibility",
        "phase-1-data-entry-discrepancy",
    ]
    recommended_phase = next((phase for phase in phase_priority if by_phase.get(phase, 0)), "phase-0-inventory-and-gates")
    phase_candidates = [item for item in items if item.phase_slice == recommended_phase]
    unknown_items = [item for item in items if item.classification == "unknown"]

    lines: list[str] = []
    lines.append("# Legacy Workflow Inventory")
    lines.append("")
    lines.append(f"Generated: {generated}")
    lines.append("")
    lines.append(f"Full CSV inventory: `{csv_name}`")
    lines.append("")
    lines.append("## Summary By Type")
    lines.append("")
    lines.append("| Artifact type | Count |")
    lines.append("|---|---:|")
    for key, count in sorted(by_type.items()):
        lines.append(f"| `{key}` | {count} |")
    lines.append("")
    lines.append("## Summary By Classification")
    lines.append("")
    lines.append("| Classification | Count |")
    lines.append("|---|---:|")
    for key, count in sorted(by_class.items()):
        lines.append(f"| `{key}` | {count} |")
    lines.append("")
    lines.append("## Summary By Phase Slice")
    lines.append("")
    lines.append("| Phase slice | Count |")
    lines.append("|---|---:|")
    for key, count in sorted(by_phase.items()):
        lines.append(f"| `{key}` | {count} |")
    lines.append("")
    lines.append("## First Phase 1 Candidate Slice")
    lines.append("")
    lines.append(f"Recommended slice: `{recommended_phase}`.")
    lines.append("")
    lines.append("Deletion proof required before removing any candidate artifact:")
    lines.append("")
    lines.append("- SPA/API route is the default navigation path.")
    lines.append("- Legacy route either redirects to SPA or has no runtime registration.")
    lines.append("- Permissions match the relevant `SecureController.mayProceed()` behavior.")
    lines.append("- Audit/status/log output parity is captured by tests or explicit verification.")
    lines.append("- Servlet/JSP/helper references are gone before file deletion.")
    lines.append("")
    lines.append("Candidate artifacts:")
    lines.append("")
    lines.append("| Type | Path | Symbol | Route/mapping |")
    lines.append("|---|---|---|---|")
    for item in phase_candidates[:80]:
        lines.append(
            "| "
            + " | ".join(
                markdown_escape(value)
                for value in (item.artifact_type, item.path, item.symbol, item.route_or_mapping)
            )
            + " |"
        )
    if len(phase_candidates) > 80:
        lines.append(f"| ... | ... | ... | {len(phase_candidates) - 80} more candidates in CSV |")
    lines.append("")
    lines.append("## Unknown Items")
    lines.append("")
    lines.append("These require manual owner/category assignment before deletion work:")
    lines.append("")
    lines.append("| Type | Path | Symbol | Route/mapping |")
    lines.append("|---|---|---|---|")
    for item in unknown_items[:120]:
        lines.append(
            "| "
            + " | ".join(
                markdown_escape(value)
                for value in (item.artifact_type, item.path, item.symbol, item.route_or_mapping)
            )
            + " |"
        )
    if len(unknown_items) > 120:
        lines.append(f"| ... | ... | ... | {len(unknown_items) - 120} more unknown items in CSV |")
    lines.append("")
    lines.append("_Generated by `scripts/ci/generate-legacy-inventory.py`._")
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate legacy workflow inventory artifacts.")
    parser.add_argument("--output-dir", default=str(DEFAULT_OUTPUT_DIR), help="Directory for generated files.")
    parser.add_argument("--basename", default="legacy-workflow-inventory", help="Output basename without extension.")
    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    csv_path = output_dir / f"{args.basename}.csv"
    md_path = output_dir / f"{args.basename}.md"

    items = build_inventory()
    write_csv(items, csv_path)
    write_markdown(items, md_path, csv_path.name)

    print(f"Legacy workflow inventory: {len(items)} artifacts")
    print(f"CSV written to {csv_path}")
    print(f"Markdown written to {md_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
