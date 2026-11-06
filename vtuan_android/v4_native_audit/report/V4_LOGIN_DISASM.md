# V4 ARM64 login and menu instructions

Native SHA-256: 752bcdc20d88fe535b4de156790e7468bf58c61b21dfd1eba5ee2fbda0facb38

All listed locations are native-library RVAs, not offsets inside the game.
These are instruction excerpts, NOT a working authentication patch.

## V4 input/login click candidate: 0x166404

~~~asm
0x00166404  bl       #0xce230
0x00166408  cbnz     w0, #0x167348
0x0016640c  add      x0, sp, #0x68
0x00166410  bl       #0xd2720
0x00166414  add      x0, sp, #0x68
0x00166418  bl       #0xd3b60
0x0016641c  ldrb     w8, [sp, #0xd0]
0x00166420  tbz      w8, #0, #0x16642c
0x00166424  ldr      x0, [sp, #0xe0]
0x00166428  bl       #0xcfc70
0x0016642c  ldrb     w8, [sp, #0x70]
0x00166430  tbz      w8, #0, #0x16643c
0x00166434  ldr      x0, [sp, #0x80]
0x00166438  bl       #0xcfc70
0x0016643c  bl       #0x1fee58
0x00166440  mov      w0, #4
0x00166444  bl       #0x1e1094
0x00166448  fmov     s0, wzr
0x0016644c  mov      v1.16b, v13.16b
0x00166450  bl       #0x200ee4
0x00166454  adrp     x8, #0x79d000
0x00166458  ldr      q0, [x8, #0xe70]
0x0016645c  mov      w9, #0x4a56
0x00166460  movk     w9, #0x3f3d, lsl #16
0x00166464  mov      w10, #0xad4d
0x00166468  mov      w22, #0x97e7
0x0016646c  mov      w19, #0x75f3
0x00166470  mov      w23, #0x4a9f
0x00166474  mov      w24, #0x1ad1
0x00166478  add      x8, sp, #0xd0
0x0016647c  mov      w25, #0x7d8d
0x00166480  mov      w20, #0x1574
0x00166484  mov      w29, #0x1df1
0x00166488  mov      w28, #0x4dbf
0x0016648c  stur     xzr, [sp, #0xe6]
0x00166490  strh     wzr, [sp, #0xfe]
0x00166494  add      x21, sp, #0xd0
0x00166498  movk     w22, #0x5e92, lsl #16
0x0016649c  movk     w19, #0xc206, lsl #16
0x001664a0  movk     w23, #0x9c73, lsl #16
0x001664a4  movk     w24, #0xdf4e, lsl #16
0x001664a8  str      w9, [sp, #0xe0]
0x001664ac  strh     w10, [sp, #0xe4]
0x001664b0  str      q0, [sp, #0xd0]
0x001664b4  stur     xzr, [x8, #0x26]
0x001664b8  stur     xzr, [x8, #0x1e]
0x001664bc  movk     w25, #0x8745, lsl #16
0x001664c0  movk     w20, #0xe5d8, lsl #16
0x001664c4  movk     w29, #0xcfb, lsl #16
0x001664c8  movk     w28, #0xca20, lsl #16
0x001664cc  bl       #0x256474
0x001664d0  ldr      x27, [x0]
0x001664d4  bl       #0x256480
0x001664d8  ldr      x10, [x0]
0x001664dc  mov      x8, xzr
0x001664e0  cmp      x27, x8
0x001664e4  b.ls     #0x166578
0x001664e8  b        #0x167154
0x001664ec  mov      w10, #0x6416
0x001664f0  movk     w10, #0x11dc, lsl #16
0x001664f4  add      w10, w8, w10
0x001664f8  add      w11, w10, w9
0x001664fc  and      w10, w10, w9
0x00166500  sub      w10, w11, w10, lsl #1
~~~

## V4 authentication function start: 0x19d1c4

~~~asm
0x0019d1c4  stp      x28, x27, [sp, #-0x60]!
0x0019d1c8  stp      x26, x25, [sp, #0x10]
0x0019d1cc  stp      x24, x23, [sp, #0x20]
0x0019d1d0  stp      x22, x21, [sp, #0x30]
0x0019d1d4  stp      x20, x19, [sp, #0x40]
0x0019d1d8  stp      x29, x30, [sp, #0x50]
0x0019d1dc  sub      sp, sp, #1, lsl #12
0x0019d1e0  sub      sp, sp, #0x7b0
0x0019d1e4  mrs      x20, tpidr_el0
0x0019d1e8  ldr      x8, [x20, #0x28]
0x0019d1ec  adrp     x19, #0x8a8000
0x0019d1f0  add      x27, sp, #0x3d0
0x0019d1f4  str      x8, [sp, #0x17a8]
0x0019d1f8  ldr      x8, [x19, #0xc00]
0x0019d1fc  str      x20, [sp, #0x30]
0x0019d200  cbz      x8, #0x19d298
0x0019d204  adrp     x8, #0x8a8000
0x0019d208  add      x8, x8, #0xc68
0x0019d20c  ldar     x8, [x8]
0x0019d210  mov      x21, x0
0x0019d214  cmn      x8, #1
0x0019d218  b.eq     #0x19d244
0x0019d21c  adrp     x0, #0x8a8000
0x0019d220  adrp     x2, #0x1dc000
0x0019d224  add      x8, sp, #0x400
0x0019d228  add      x9, sp, #0x420
0x0019d22c  add      x0, x0, #0xc68
0x0019d230  add      x2, x2, #0x5ec
0x0019d234  add      x1, sp, #0x3d0
0x0019d238  str      x8, [sp, #0x420]
0x0019d23c  str      x9, [sp, #0x3d0]
0x0019d240  bl       #0xd66e0
0x0019d244  ldr      x19, [x19, #0xc00]
0x0019d248  str      xzr, [sp, #0x250]
0x0019d24c  strb     wzr, [sp, #0x258]
0x0019d250  cbz      x19, #0x19e8e8
0x0019d254  ldr      x8, [x19]
0x0019d258  mov      w2, #6
0x0019d25c  add      x1, sp, #0x250
0x0019d260  movk     w2, #1, lsl #16
0x0019d264  ldr      x8, [x8, #0x30]
0x0019d268  mov      x0, x19
0x0019d26c  blr      x8
0x0019d270  cbz      w0, #0x19dca0
0x0019d274  ldr      x8, [x19]
0x0019d278  add      x1, sp, #0x250
0x0019d27c  mov      x0, x19
0x0019d280  mov      x2, xzr
0x0019d284  ldr      x8, [x8, #0x20]
0x0019d288  blr      x8
0x0019d28c  cbz      w0, #0x19dc98
0x0019d290  str      xzr, [sp, #0x250]
0x0019d294  b        #0x19e8e8
0x0019d298  adrp     x8, #0x79e000
0x0019d29c  ldr      q0, [x8, #0x180]
0x0019d2a0  adrp     x9, #0x79e000
0x0019d2a4  mov      w10, #0x9d02
0x0019d2a8  mov      w20, #0x2fc9
0x0019d2ac  str      q0, [x27, #0x50]
0x0019d2b0  ldr      d0, [x9, #0x600]
0x0019d2b4  mov      w21, #0xeb7d
0x0019d2b8  mov      w22, #0x9291
0x0019d2bc  mov      w23, #0x32ff
0x0019d2c0  mov      w28, #0x6a6
~~~

## Post-auth state path candidate: 0x1c23c4

~~~asm
0x001c23c4  adrp     x0, #0x8a8000
0x001c23c8  add      x0, x0, #0xcd8
0x001c23cc  bl       #0xe0cb8
0x001c23d0  mov      w20, #1
0x001c23d4  b        #0x1c2400
0x001c23d8  mov      w8, #0x6ae8
0x001c23dc  movk     w8, #0x15e, lsl #16
0x001c23e0  add      x9, sp, #0x420
0x001c23e4  add      x1, x9, x8
0x001c23e8  b        #0x1c23f0
0x001c23ec  add      x1, sp, #0x420
0x001c23f0  adrp     x0, #0x8a8000
0x001c23f4  add      x0, x0, #0xcd8
0x001c23f8  bl       #0xe0cb8
0x001c23fc  mov      w20, #3
0x001c2400  adrp     x0, #0x8a8000
0x001c2404  add      x0, x0, #0xd84
0x001c2408  bl       #0xd3d60
0x001c240c  ldrb     w8, [sp, #0x2e0]
0x001c2410  tbnz     w8, #0, #0x1c2420
0x001c2414  ldrb     w8, [sp, #0x300]
0x001c2418  tbz      w8, #0, #0x1be338
0x001c241c  b        #0x1c2430
0x001c2420  ldr      x0, [sp, #0x2f0]
0x001c2424  bl       #0xcfc70
0x001c2428  ldrb     w8, [sp, #0x300]
0x001c242c  tbz      w8, #0, #0x1be338
0x001c2430  ldr      x0, [sp, #0x310]
0x001c2434  bl       #0xcfc70
0x001c2438  ldrb     w8, [sp, #0x320]
0x001c243c  tbz      w8, #0, #0x1be340
0x001c2440  ldr      x0, [sp, #0x330]
0x001c2444  bl       #0xcfc70
0x001c2448  ldrb     w8, [sp, #0x338]
0x001c244c  tbz      w8, #0, #0x1be348
0x001c2450  ldr      x0, [sp, #0x348]
0x001c2454  bl       #0xcfc70
0x001c2458  ldrb     w8, [sp, #0x388]
0x001c245c  tbnz     w8, #0, #0x1be350
0x001c2460  b        #0x1be358
0x001c2464  bl       #0xcf080
0x001c2468  add      x0, sp, #0x3d0
0x001c246c  bl       #0xe0aa8
0x001c2470  add      x0, sp, #0x3d0
0x001c2474  bl       #0xe0aa8
0x001c2478  add      x0, sp, #0x3d0
0x001c247c  bl       #0xe0aa8
0x001c2480  add      x0, sp, #0x3d0
0x001c2484  bl       #0xe0aa8
0x001c2488  mov      w8, #0x7e98
0x001c248c  movk     w8, #0x146, lsl #16
0x001c2490  b        #0x1c249c
0x001c2494  mov      w8, #0x648a
0x001c2498  movk     w8, #0x58, lsl #16
0x001c249c  add      x9, sp, #0x420
0x001c24a0  add      x1, x9, x8
0x001c24a4  ldr      x0, [sp, #0x28]
0x001c24a8  bl       #0xd53b0
0x001c24ac  adrp     x1, #0x86b000
0x001c24b0  adrp     x2, #0x86b000
0x001c24b4  ldr      x1, [x1, #0xbc8]
0x001c24b8  ldr      x2, [x2, #0xe20]
0x001c24bc  ldr      x0, [sp, #0x28]
0x001c24c0  bl       #0xd2ae0
0x001c24c4  b        #0x1b3564
0x001c24c8  mov      w8, #0x2ac0
0x001c24cc  movk     w8, #0x15c, lsl #16
0x001c24d0  b        #0x1c24dc
0x001c24d4  mov      w8, #0x42d0
0x001c24d8  movk     w8, #0x125, lsl #16
0x001c24dc  add      x9, sp, #0x420
0x001c24e0  add      x1, x9, x8
0x001c24e4  ldr      x0, [sp, #0x28]
0x001c24e8  bl       #0xd53b0
0x001c24ec  adrp     x1, #0x86b000
0x001c24f0  adrp     x2, #0x86b000
~~~

## V4 outer login branch: 0x1dd280

~~~asm
0x001dd280  stp      x21, x20, [sp, #-0x20]!
0x001dd284  stp      x19, x30, [sp, #0x10]
0x001dd288  mov      x19, x0
0x001dd28c  bl       #0xd68f0
0x001dd290  ldr      x1, [x19]
0x001dd294  str      xzr, [x19]
0x001dd298  ldr      w0, [x0]
0x001dd29c  bl       #0xd2640
0x001dd2a0  mov      x20, x19
0x001dd2a4  ldrb     w8, [x20, #8]!
0x001dd2a8  ldr      x9, [x20, #0x10]
0x001dd2ac  tst      w8, #1
0x001dd2b0  csinc    x0, x9, x20, ne
0x001dd2b4  bl       #0x19d1c4
0x001dd2b8  cmp      w0, #1
0x001dd2bc  b.ne     #0x1dd2e8
0x001dd2c0  ldrb     w8, [x19, #0x20]
0x001dd2c4  cbz      w8, #0x1dd2f8
0x001dd2c8  adrp     x0, #0x8a8000
0x001dd2cc  add      x0, x0, #0xdf8
0x001dd2d0  mov      x1, x20
0x001dd2d4  bl       #0xe05e8
0x001dd2d8  adrp     x0, #0x8a8000
0x001dd2dc  add      x0, x0, #0xde0
0x001dd2e0  bl       #0x15b3fc
0x001dd2e4  b        #0x1dd2f8
0x001dd2e8  adrp     x0, #0x8a8000
0x001dd2ec  add      x0, x0, #0xde0
0x001dd2f0  strb     wzr, [x0]
0x001dd2f4  bl       #0x15b3fc
0x001dd2f8  adrp     x8, #0x8a8000
0x001dd2fc  add      x8, x8, #0xc14
0x001dd300  stlrb    wzr, [x8]
0x001dd304  ldrb     w8, [x20]
0x001dd308  tbz      w8, #0, #0x1dd314
0x001dd30c  ldr      x0, [x19, #0x18]
0x001dd310  bl       #0xcfc70
0x001dd314  ldr      x20, [x19]
0x001dd318  str      xzr, [x19]
0x001dd31c  cbz      x20, #0x1dd330
0x001dd320  mov      x0, x20
0x001dd324  bl       #0xd4cf0
0x001dd328  mov      x0, x20
0x001dd32c  bl       #0xcfc70
0x001dd330  mov      x0, x19
0x001dd334  bl       #0xcfc70
0x001dd338  ldp      x19, x30, [sp, #0x10]
0x001dd33c  mov      x0, xzr
0x001dd340  ldp      x21, x20, [sp], #0x20
0x001dd344  ret      
0x001dd348  mov      x20, x0
0x001dd34c  cbnz     x19, #0x1dd358
0x001dd350  b        #0x1dd38c
0x001dd354  mov      x20, x0
0x001dd358  ldrb     w8, [x19, #8]
0x001dd35c  tbz      w8, #0, #0x1dd368
0x001dd360  ldr      x0, [x19, #0x18]
0x001dd364  bl       #0xcfc70
0x001dd368  ldr      x21, [x19]
0x001dd36c  str      xzr, [x19]
0x001dd370  cbz      x21, #0x1dd384
0x001dd374  mov      x0, x21
0x001dd378  bl       #0xd4cf0
0x001dd37c  mov      x0, x21
0x001dd380  bl       #0xcfc70
0x001dd384  mov      x0, x19
0x001dd388  bl       #0xcfc70
0x001dd38c  mov      x0, x20
0x001dd390  bl       #0x3ca688
0x001dd394  str      d8, [sp, #-0x20]!
0x001dd398  str      x20, [sp, #8]
0x001dd39c  stp      x19, x30, [sp, #0x10]
0x001dd3a0  movi     d8, #0000000000000000
0x001dd3a4  mov      x19, x0
0x001dd3a8  add      x20, x0, #0x10c
0x001dd3ac  stur     d8, [x0, #0xc4]
~~~

## JNI menu frame: 0xe90e4

~~~asm
0x000e90e4  stp      d15, d14, [sp, #-0xa0]!
0x000e90e8  stp      d13, d12, [sp, #0x10]
0x000e90ec  stp      d11, d10, [sp, #0x20]
0x000e90f0  stp      d9, d8, [sp, #0x30]
0x000e90f4  stp      x28, x27, [sp, #0x40]
0x000e90f8  stp      x26, x25, [sp, #0x50]
0x000e90fc  stp      x24, x23, [sp, #0x60]
0x000e9100  stp      x22, x21, [sp, #0x70]
0x000e9104  stp      x20, x19, [sp, #0x80]
0x000e9108  stp      x29, x30, [sp, #0x90]
0x000e910c  sub      sp, sp, #0xfc0
0x000e9110  mrs      x24, tpidr_el0
0x000e9114  ldr      x8, [x24, #0x28]
0x000e9118  str      x8, [sp, #0xfb8]
0x000e911c  adrp     x8, #0x8a3000
0x000e9120  ldrb     w8, [x8, #0x868]
0x000e9124  cbz      w8, #0xe9318
0x000e9128  add      x1, sp, #0x38
0x000e912c  mov      w0, #1
0x000e9130  bl       #0xd8810
0x000e9134  ldp      d0, d1, [sp, #0x38]
0x000e9138  mov      x8, #0xcd6500000000
0x000e913c  movk     x8, #0x41cd, lsl #48
0x000e9140  fmov     d2, x8
0x000e9144  scvtf    d1, d1
0x000e9148  scvtf    d0, d0
0x000e914c  fdiv     d1, d1, d2
0x000e9150  fadd     d8, d1, d0
0x000e9154  bl       #0x1ed7a8
0x000e9158  adrp     x27, #0x8a3000
0x000e915c  adrp     x20, #0x8a3000
0x000e9160  ldr      w9, [x27, #0x850]
~~~

## Aim UI start: 0xf7444

~~~asm
0x000f7444  stp      d15, d14, [sp, #-0xa0]!
0x000f7448  stp      d13, d12, [sp, #0x10]
0x000f744c  stp      d11, d10, [sp, #0x20]
0x000f7450  stp      d9, d8, [sp, #0x30]
0x000f7454  stp      x28, x27, [sp, #0x40]
0x000f7458  stp      x26, x25, [sp, #0x50]
0x000f745c  stp      x24, x23, [sp, #0x60]
0x000f7460  stp      x22, x21, [sp, #0x70]
0x000f7464  stp      x20, x19, [sp, #0x80]
0x000f7468  stp      x29, x30, [sp, #0x90]
0x000f746c  sub      sp, sp, #0x210
0x000f7470  mrs      x8, tpidr_el0
0x000f7474  str      x8, [sp, #8]
0x000f7478  ldr      x8, [x8, #0x28]
0x000f747c  fmov     s9, #0.75000000
0x000f7480  mov      v0.16b, v9.16b
0x000f7484  str      x8, [sp, #0x208]
0x000f7488  bl       #0x1fe474
0x000f748c  bl       #0x1fe420
0x000f7490  mov      x19, x0
0x000f7494  bl       #0x1fe358
0x000f7498  mov      w8, #0x43700000
0x000f749c  mov      v11.16b, v1.16b
0x000f74a0  fmov     s1, w8
0x000f74a4  fadd     s0, s0, s1
0x000f74a8  fmov     s14, #16.00000000
0x000f74ac  fadd     s13, s0, s14
0x000f74b0  bl       #0x1fe330
0x000f74b4  mov      w8, #-0x3c900000
0x000f74b8  mov      w9, #-0x3e000000
0x000f74bc  fmov     s1, w8
0x000f74c0  fadd     s0, s0, s1
~~~

