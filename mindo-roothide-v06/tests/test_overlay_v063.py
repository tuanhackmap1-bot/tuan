from pathlib import Path
from xml.etree import ElementTree as ET
p=Path(__file__).resolve().parent.parent
s=(p/"MinDoOverlay.c").read_text()
assert 'cfgInitDefaults' in s
assert 'Documents/bolacfg.bin' in s
assert 'if(!gCfgValid && !cfgInitDefaults())' in s
assert 'fileExistsAtPath:' in s
assert "gCfg[19]=30;" in s
assert "gCfg[20]=100;" in s
ids={v.text for v in ET.parse(p/"MinDoOverlay.plist").getroot().iter("string")}
assert {'com.dts.freefireth','com.dts.freefiremax'} <= ids
print('PASS: compact overlay configuration checks')
