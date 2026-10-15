"""Inspect overlay integration and patch consistency."""
from pathlib import Path
from xml.etree import ElementTree as ET
p=Path(__file__).resolve().parent.parent
src=(p/"MinDoOverlay.c").read_text()
assert "Documents/Assembly-CSharp-patch.bytes" in src
assert "Documents/bolacfg.bin" in src
assert "if(!gCfgValid && !cfgInitDefaults())" in src\nassert "if(!cfgWrite())" in src
assert "if(!gCfgValid)return 0;" in src
root=ET.parse(p/"MinDoOverlay.plist").getroot()
ids=[v.text for v in root.iter("string")]
assert {"com.dts.freefireth","com.dts.freefiremax"} <= set(ids)
assert "Do NOT disable an otherwise valid configuration" in src
print("PASS: bundle filters, config check and rollback compatibility")
