import binascii
KEY=lambda i: ((0x5A+i*0x37)^(i<<4))&255
def encode(payload):
    assert len(payload)==24 and payload[:5]==b'BOLA\x01'
    raw=bytes(b^KEY(i) for i,b in enumerate(payload))
    return raw+binascii.crc32(raw).to_bytes(4,'little')
def decode(data):
    assert len(data)==28
    assert binascii.crc32(data[:24])==int.from_bytes(data[24:],'little')
    p=bytes(b^KEY(i) for i,b in enumerate(data[:24]))
    assert p[:5]==b'BOLA\x01'
    return p
p=bytearray(24)
p[:5]=b'BOLA\x01'
p[5]=3
p[7]=3
d=encode(p)
assert decode(d)==p
assert d[:12].hex()=='18cea48e773ec4a892d92007'
print('PASS: MinDo BOLA payload roundtrip')
