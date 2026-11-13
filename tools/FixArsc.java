import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Repackages an APK preserving entries, but forcing resources.arsc to method STORED. */
public class FixArsc {
    static int u16(RandomAccessFile r) throws IOException { return Short.toUnsignedInt(Short.reverseBytes(r.readShort())); }
    static long u32(RandomAccessFile r) throws IOException { return Integer.toUnsignedLong(Integer.reverseBytes(r.readInt())); }

    static void rebuild(Path input, Path output) throws Exception {
        if (input.toRealPath().equals(output.toAbsolutePath().normalize())) throw new IllegalArgumentException("Input and output must differ");
        boolean found = false; int copied = 0;
        try (ZipFile original = new ZipFile(input.toFile());
             ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(output)))) {
            out.setLevel(6);
            Enumeration<? extends ZipEntry> entries = original.entries();
            byte[] buffer = new byte[1024 * 1024];
            while (entries.hasMoreElements()) {
                ZipEntry src = entries.nextElement();
                String name = src.getName();
                // Re-sign from scratch; old JAR/V1 signatures must not survive.
                if (name.matches("(?i)^META-INF/[^/]+\\.(SF|RSA|DSA|EC)$") ||
                    name.equalsIgnoreCase("META-INF/MANIFEST.MF")) continue;
                ZipEntry dest = new ZipEntry(name);
                dest.setTime(src.getTime());
                boolean stored = name.equals("resources.arsc") || src.getMethod() == ZipEntry.STORED || src.isDirectory();
                if (name.equals("resources.arsc")) found = true;
                if (stored) {
                    if (src.getSize() < 0 || src.getCrc() < 0)
                        throw new IOException("Unknown ZIP size/CRC for " + name);
                    dest.setMethod(ZipEntry.STORED);
                    dest.setSize(src.getSize());
                    dest.setCompressedSize(src.getSize());
                    dest.setCrc(src.getCrc());
                } else {
                    dest.setMethod(ZipEntry.DEFLATED);
                }
                out.putNextEntry(dest);
                if (!src.isDirectory()) {
                    try (InputStream in = new BufferedInputStream(original.getInputStream(src))) {
                        for (int count; (count = in.read(buffer)) != -1;) out.write(buffer, 0, count);
                    }
                }
                out.closeEntry();
                copied++;
            }
        }
        if (!found) {
            Files.deleteIfExists(output);
            throw new IOException("resources.arsc not found: cannot repair this APK");
        }
        System.out.println("Repacked " + copied + " entries; resources.arsc is STORED.");
    }

    static void verify(Path apk) throws Exception {
        try (ZipFile z = new ZipFile(apk.toFile())) {
            ZipEntry e = z.getEntry("resources.arsc");
            if (e == null) throw new IOException("resources.arsc missing");
            if (e.getMethod() != ZipEntry.STORED) throw new IOException("resources.arsc still compressed");
        }
        try (RandomAccessFile r = new RandomAccessFile(apk.toFile(), "r")) {
            long length = r.length(), end = Math.max(0, length - 65557);
            long eocd = -1;
            for (long pos = length - 22; pos >= end; pos--) {
                r.seek(pos);
                if (u32(r) == 0x06054b50L) { eocd = pos; break; }
            }
            if (eocd < 0) throw new IOException("ZIP end-of-central-directory not found");
            r.seek(eocd + 16);
            long centralOffset = u32(r);
            r.seek(eocd + 10);
            int count = u16(r);
            r.seek(centralOffset);
            for (int i = 0; i < count; i++) {
                long pos = r.getFilePointer();
                if (u32(r) != 0x02014b50L) throw new IOException("Bad central directory record");
                r.seek(pos + 28);
                int nameLength = u16(r), extraLength = u16(r), commentLength = u16(r);
                r.seek(pos + 42);
                long localOffset = u32(r);
                byte[] nameBytes = new byte[nameLength];
                r.seek(pos + 46);
                r.readFully(nameBytes);
                if (new String(nameBytes, java.nio.charset.StandardCharsets.UTF_8).equals("resources.arsc")) {
                    r.seek(localOffset);
                    if (u32(r) != 0x04034b50L) throw new IOException("Bad local ZIP header");
                    r.seek(localOffset + 26);
                    int lfNameLen = u16(r), lfExtraLen = u16(r);
                    long offset = localOffset + 30L + lfNameLen + lfExtraLen;
                    if (offset % 4 != 0) throw new IOException("resources.arsc NOT 4-byte aligned (offset=" + offset + ")");
                    System.out.println("VERIFIED: resources.arsc stored, offset " + offset + ", 4-byte aligned.");
                    return;
                }
                r.seek(pos + 46L + nameLength + extraLength + commentLength);
            }
            throw new IOException("resources.arsc not found in central directory");
        }
    }

    public static void main(String[] a) throws Exception {
        if (a.length == 2 && a[0].equals("--verify")) verify(Paths.get(a[1]));
        else if (a.length == 2) rebuild(Paths.get(a[0]), Paths.get(a[1]));
        else throw new IllegalArgumentException("Usage: java FixArsc input.apk output.apk  OR --verify apk");
    }
}
