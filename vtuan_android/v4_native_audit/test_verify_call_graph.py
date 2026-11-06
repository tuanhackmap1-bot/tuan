#!/usr/bin/env python3
"""Deterministic regression tests for VTUAN's read-only V4 call-graph evidence."""
import copy
import json
import unittest
from pathlib import Path

from verify_call_graph import run

REPORT = Path(__file__).parent / "report" / "v4_calls.json"

class CallGraphAuditTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.original = json.loads(REPORT.read_text(encoding="utf-8"))

    def check_rejected(self, mutate, expected):
        changed = copy.deepcopy(self.original)
        mutate(changed)
        messages = run(changed)
        self.assertTrue(any(expected in msg for msg in messages), messages)

    def test_original_evidence_is_consistent(self):
        self.assertEqual(run(self.original), [])

    def test_rejects_other_library(self):
        self.check_rejected(lambda d: d.update(sha256="0" * 64), "SHA-256 mismatch")

    def test_rejects_wrong_fde_start(self):
        self.check_rejected(
            lambda d: d["targets"]["JNI rendering frame"].update(enclosing_fde_start_rva="0x1234"),
            "Wrong RVA: JNI rendering frame")

    def test_rejects_missing_call(self):
        def remove_edge(data):
            data["edges_between_candidates"] = [
                e for e in data["edges_between_candidates"]
                if not (e["source"] == "JNI rendering frame" and
                        e["destination"] == "Aim configuration / ImGui UI")]
        self.check_rejected(remove_edge, "Missing verified edge")

    def test_rejects_fde_mismatch(self):
        self.check_rejected(
            lambda d: d["targets"]["Aim hero-choice UI"].update(start_matches_candidate=False),
            "FDE boundary mismatch")

    def test_auth_candidates_are_inside_larger_fde(self):
        auth = self.original["targets"]["Old auth-branch candidate"]
        success = self.original["targets"]["Old auth-success candidate"]
        self.assertEqual(int(auth["enclosing_fde_start_rva"], 0), 0x1DD280)
        self.assertEqual(int(success["enclosing_fde_start_rva"], 0), 0x19D1C4)
        self.assertFalse(auth["start_matches_candidate"])
        self.assertFalse(success["start_matches_candidate"])

if __name__ == "__main__":
    unittest.main()
