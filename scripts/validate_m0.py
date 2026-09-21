#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Offline M0 contract checks. Install tests/m0/requirements.txt; Node is required."""

import copy
import hashlib
import json
import re
import subprocess
import sys
from pathlib import Path
from urllib.parse import unquote, urlsplit

from jsonschema import Draft202012Validator, FormatChecker

ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "tests/m0/fixtures"


def require(condition, message):
    if not condition:
        raise ValueError(message)


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        require(key not in result, f"Duplicate JSON key: {key!r}")
        result[key] = value
    return result


def reject_constant(value):
    raise ValueError(f"Non-JSON numeric constant: {value}")


def parse(text):
    return json.loads(text, object_pairs_hook=unique_object, parse_constant=reject_constant)


def read_json(path):
    try:
        return parse(path.read_text(encoding="utf-8"))
    except ValueError as error:
        raise ValueError(f"{path.relative_to(ROOT)}: {error}") from error


def at(value, path):
    for key in path:
        value = value[key]
    return value


def patterns(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key == "pattern":
                yield child
            elif key == "$ref":
                require(child.startswith("#/"), "M0 tests must not retrieve remote references")
            else:
                yield from patterns(child)
    elif isinstance(value, list):
        for child in value:
            yield from patterns(child)


def check_instances(schemas, checker):
    total = 0
    failures = []
    for name, schema in sorted(schemas.items()):
        fixture = read_json(FIXTURES / f"{name}.json")
        require(set(fixture) == {"schema", "base", "cases"}, f"Invalid fixture fields: {name}")
        require(fixture["schema"] == name, f"Wrong fixture schema: {name}")
        validator = Draft202012Validator(schema, format_checker=checker)
        validator.validate(fixture["base"])
        require(fixture["cases"], f"Empty fixture suite: {name}")
        names = set()
        for case in fixture["cases"]:
            require(set(case) <= {"name", "valid", "set", "remove"}, f"Unknown case fields: {name}")
            require(isinstance(case["valid"], bool), f"Expected boolean validity: {name}")
            require(case["name"] not in names, f"Duplicate case name: {case['name']}")
            names.add(case["name"])
            instance = copy.deepcopy(fixture["base"])
            for edit in case.get("set", []):
                require(set(edit) == {"path", "value"} and edit["path"], "Invalid fixture edit")
                at(instance, edit["path"][:-1])[edit["path"][-1]] = copy.deepcopy(edit["value"])
            for path in case.get("remove", []):
                del at(instance, path[:-1])[path[-1]]
            errors = list(validator.iter_errors(instance))
            if (not errors) != case["valid"]:
                detail = "; ".join(error.message for error in errors[:2]) or "unexpectedly accepted"
                failures.append(f"{name}/{case['name']}: {detail}")
            total += 1
        print(f"Checked {name}: {len(names)} fixtures")
    require(not failures, "Fixture failures:\n" + "\n".join(failures))
    print(f"PASS schema instances: {total}; four valid base documents")


def check_parsing():
    paths = sorted((ROOT / "schemas").glob("*.json"))
    paths += sorted((ROOT / "runtime/manifests").glob("*.json"))
    paths += sorted(FIXTURES.glob("*.json"))
    for path in paths:
        read_json(path)
    cases = read_json(FIXTURES / "json-parsing.json")
    for text in cases["accept"]:
        parse(text)
    for text in cases["reject"]:
        try:
            parse(text)
        except ValueError:
            continue
        raise ValueError(f"JSON parser unexpectedly accepted {text!r}")
    print(f"PASS strict JSON: {len(paths)} documents; "
          f"{sum(map(len, cases.values()))} duplicate-key/numeric parsing fixtures")


def check_regex(schemas):
    compiled = [p for schema in schemas.values() for p in patterns(schema)]
    groups = []
    for group in read_json(FIXTURES / "regex.json"):
        keys = [key.replace("~1", "/").replace("~0", "~") for key in group["pointer"].split("/")[1:]]
        value = schemas[group["schema"]]
        for key in keys:
            value = value[int(key)] if isinstance(value, list) else value[key]
        require(isinstance(value, str), "Regex fixture pointer must resolve to a pattern")
        groups.append({"name": group["schema"] + group["pointer"], "pattern": value,
                       "accept": group["accept"], "reject": group["reject"]})
    # No shell or network; Node failure/missing Node fails the suite rather than silently skipping.
    subprocess.run(["node", str(ROOT / "scripts/check_m0_regex.cjs")],
                   input=json.dumps({"patterns": compiled, "cases": groups}),
                   text=True, check=True, timeout=30)


def heading_ids(text):
    seen = {}
    result = set()
    for title in re.findall(r"^#{1,6}\s+(.+?)\s*#*\s*$", text, re.MULTILINE):
        slug = re.sub(r"[^\w\- ]", "", title.lower()).replace(" ", "-")
        number = seen.get(slug, 0)
        seen[slug] = number + 1
        result.add(slug + (f"-{number}" if number else ""))
    return result


def check_markdown():
    count = 0
    for path in [ROOT / "README.md", *sorted((ROOT / "docs").rglob("*.md"))]:
        text = path.read_text(encoding="utf-8")
        # Repository docs use inline Markdown links; exclude fenced code examples.
        text = re.sub(r"^```.*?^```\s*$", "", text, flags=re.MULTILINE | re.DOTALL)
        for raw in re.findall(r"\[[^\]]+\]\(([^)]+)\)", text):
            link = urlsplit(raw.strip("<>"))
            if link.scheme or link.netloc:
                continue
            target = (path.parent / unquote(link.path)).resolve() if link.path else path
            require(target.is_relative_to(ROOT), f"Local link escapes checkout: {path}: {raw}")
            require(target.exists(), f"Broken local link: {path}: {raw}")
            if link.fragment and target.suffix == ".md":
                require(unquote(link.fragment) in heading_ids(target.read_text(encoding="utf-8")),
                        f"Missing Markdown heading: {path}: {raw}")
            count += 1
    print(f"PASS local Markdown links: {count}")


def check_metadata():
    policy = read_json(ROOT / "runtime/manifests/compatibility.json")
    sources = read_json(ROOT / "runtime/manifests/sources.lock.json")
    require(policy["state"] == "unqualified" and not policy["qualifiedConfigurations"], "M0 qualification drift")
    presentation = policy["presentation"]
    require(presentation["backend"] == "native-surface" and presentation["transport"] == "unresolved"
            and presentation["qualificationMilestone"] == "M6" and presentation["owner"] == "StudioDroid",
            "Presentation ownership/transport drift")
    require(not any(presentation[k] for k in ("termuxX11RuntimeDependency", "externalXServerDependency",
                                            "productionXvfbCpuBitmapCopy")), "Forbidden presentation dependency")
    require(policy["profiles"]["lowMemoryBelowPhysicalBytes"] > 4 * 1024**3
            and policy["profiles"]["unknownProfile"] == "LOW_MEMORY", "Low-memory defaults changed")
    require(all(v["provider"] == "system" and not v["qualified"] for v in policy["gpuDefaults"]),
            "Unqualified custom driver selected")
    require(sources["state"] == "research-only" and sources["buildable"] is False
            and not sources["artifacts"], "M0 sources must remain non-buildable")
    ids = set()
    for source in sources["sources"]:
        require(source["id"] not in ids and re.fullmatch(r"[a-f0-9]{40}", source["revision"])
                and source["url"].startswith("https://github.com/") and source["incorporated"] is False,
                "Invalid research source identity")
        ids.add(source["id"])
    require(hashlib.sha256((ROOT / "LICENSE").read_bytes()).hexdigest()
            == "fb981668c18a279e285fc4d83fba1e836cc84dd4daa73c9697d3cfd2d8aca6e0", "License integrity mismatch")
    print("PASS M0 metadata invariants and license digest")


def main():
    checker = FormatChecker()
    for fmt in ("uuid", "uri", "date-time"):
        require(fmt in checker.checkers, f"Missing {fmt} checker; install tests/m0/requirements.txt")
    schemas = {p.name.removesuffix(".schema.json"): read_json(p)
               for p in sorted((ROOT / "schemas").glob("*.schema.json"))}
    require(len(schemas) == 4, "Expected all four M0 schemas")
    for schema in schemas.values():
        list(patterns(schema))  # Enforce local references before constructing validators.
        Draft202012Validator.check_schema(schema)
    print("PASS Draft 2020-12 meta-validation: 4 schemas")
    check_parsing()
    check_instances(schemas, checker)
    check_regex(schemas)
    check_markdown()
    check_metadata()
    subprocess.run(["git", "diff", "--check"], cwd=ROOT, check=True, timeout=30)
    # git diff excludes new files: check the permanent harness/fixture files too.
    for folder in (ROOT / "scripts", ROOT / "tests/m0"):
        for path in folder.rglob("*"):
            if path.is_file() and path.suffix in {".py", ".cjs", ".json", ".txt"}:
                for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
                    require(line == line.rstrip(), f"Trailing whitespace: {path}:{number}")
    print("PASS git diff --check and validation-file whitespace")
    print("M0.1 contract validation passed; no Android/runtime qualification implied.")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, KeyError, TypeError, subprocess.SubprocessError) as error:
        print(f"FAIL: {error}", file=sys.stderr)
        sys.exit(1)
