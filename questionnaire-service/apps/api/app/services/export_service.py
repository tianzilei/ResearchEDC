import csv
import io
import json

import pandas as pd


def export_long(responses: list[dict], fmt: str) -> io.BytesIO:
    rows = []
    for r in responses:
        raw = r.get("raw_response_json", {})
        if isinstance(raw, str):
            raw = json.loads(raw)
        if isinstance(raw, dict):
            for item_code, value in raw.items():
                rows.append({
                    "subject_id": r["subject_id"],
                    "item_code": item_code,
                    "item_value": value,
                    "submitted_at": r.get("submitted_at"),
                })
    df = pd.DataFrame(rows)
    return _df_to_buffer(df, fmt)


def export_wide(responses: list[dict], fmt: str) -> io.BytesIO:
    rows = []
    for r in responses:
        raw = r.get("raw_response_json", {})
        if isinstance(raw, str):
            raw = json.loads(raw)
        row = {"subject_id": r["subject_id"]}
        if r.get("score_json"):
            scores = r["score_json"]
            if isinstance(scores, str):
                scores = json.loads(scores)
            if isinstance(scores, dict):
                row["total_score"] = scores.get("total_score")
        if isinstance(raw, dict):
            row.update(raw)
        rows.append(row)
    df = pd.DataFrame(rows)
    return _df_to_buffer(df, fmt)


def export_score(responses: list[dict], fmt: str) -> io.BytesIO:
    rows = []
    for r in responses:
        scores = r.get("score_json")
        if isinstance(scores, str):
            scores = json.loads(scores)
        if isinstance(scores, dict):
            rows.append({
                "subject_id": r["subject_id"],
                "score_code": scores.get("score_code"),
                "total_score": scores.get("total_score"),
                "severity": scores.get("severity"),
            })
    df = pd.DataFrame(rows)
    return _df_to_buffer(df, fmt)


def _df_to_buffer(df: pd.DataFrame, fmt: str) -> io.BytesIO:
    buffer = io.BytesIO()
    if fmt == "csv":
        df.to_csv(buffer, index=False, encoding="utf-8-sig")
    elif fmt == "json":
        buffer.write(df.to_json(orient="records", force_ascii=False).encode())
    else:
        df.to_excel(buffer, index=False, engine="openpyxl")
    buffer.seek(0)
    return buffer


def save_to_file(buffer: io.BytesIO, file_path: str) -> None:
    import os
    os.makedirs(os.path.dirname(file_path), exist_ok=True)
    with open(file_path, "wb") as f:
        f.write(buffer.getvalue())
