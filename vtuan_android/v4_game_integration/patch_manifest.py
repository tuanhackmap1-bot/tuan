#!/usr/bin/env python3
"""Patch the *decoded* V4 AndroidManifest for a VTUAN key-entry launcher.

Preserves native .so, game assets and original game activity. It does not
change, bypass or replace the native V4 authentication inside libysmteam.so.
"""
import argparse
from pathlib import Path
import xml.etree.ElementTree as ET

ANDROID="http://schemas.android.com/apk/res/android"
A=lambda key:"{%s}%s"%(ANDROID,key)
ET.register_namespace("android",ANDROID)
GAME="com.garena.game.kgtw.SGameActivity"
ENTRY="com.vtuan.v4integration.V4EntryActivity"
PACKAGE="com.garena.game.kgvn1"

def main(filename):
    path=Path(filename)
    tree=ET.parse(path)
    root=tree.getroot()
    if root.tag!="manifest" or root.get("package")!=PACKAGE:
        raise SystemExit("REFUSED: unexpected original APK package")
    app=root.find("application")
    if app is None:
        raise SystemExit("REFUSED: manifest has no application")
    found_game=False
    removed=0
    for activity in app.findall("activity")+app.findall("activity-alias"):
        name=activity.get(A("name"),"")
        if name==GAME:
            found_game=True
        for intent in list(activity.findall("intent-filter")):
            actions={node.get(A("name")) for node in intent.findall("action")}
            categories={node.get(A("name")) for node in intent.findall("category")}
            if ("android.intent.action.MAIN" in actions and
                 "android.intent.category.LAUNCHER" in categories):
                activity.remove(intent)
                removed+=1
    if not found_game or removed!=1:
        raise SystemExit("REFUSED: expected original game activity and 1 launcher; "
                         "found_game=%s removed=%s"%(found_game,removed))
    if app.find("./activity[@"+A("name")+"='"+ENTRY+"']") is not None:
        raise SystemExit("REFUSED: gate already installed")
    activity=ET.SubElement(app,"activity",{
        A("name"):ENTRY,
        A("exported"):"true",
        A("theme"):"@android:style/Theme.Material.Light.NoActionBar",
        A("label"):"VTUAN V4 Key"
    })
    filt=ET.SubElement(activity,"intent-filter")
    ET.SubElement(filt,"action",{A("name"):"android.intent.action.MAIN"})
    ET.SubElement(filt,"category",{A("name"):"android.intent.category.LAUNCHER"})
    perms={x.get(A("name")) for x in root.findall("uses-permission")}
    if "android.permission.INTERNET" not in perms:
        ET.SubElement(root,"uses-permission",{A("name"):"android.permission.INTERNET"})
    provider=ET.SubElement(app,"provider",{
        A("name"):"com.vtuan.v4integration.V4NativeStartupProvider",
        A("authorities"):PACKAGE+".vtuan.native.bootstrap",
        A("exported"):"false",
        A("initOrder"):"100",
        A("enabled"):"true",
    })
    tree.write(path,encoding="utf-8",xml_declaration=True)
    print("PASS: one launcher rerouted to VTUAN, original SGameActivity retained")

if __name__=="__main__":
    parser=argparse.ArgumentParser()
    parser.add_argument("manifest_xml")
    a=parser.parse_args()
    main(a.manifest_xml)
