package ws.furrify.core.shared;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Standard implementation of MultipartFile for programmatic file creation.
 */
public class StandardMultipartFile implements MultipartFile {
    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;
    private final File file;

    public StandardMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
        this.file = null;
    }

    public StandardMultipartFile(String name, String originalFilename, String contentType, File file) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = null;
        this.file = file;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        if (file != null) return !file.exists() || file.length() == 0;
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        if (file != null) return file.length();
        return content != null ? content.length : 0;
    }

    @Override
    public byte[] getBytes() throws IOException {
        if (file != null) return Files.readAllBytes(file.toPath());
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (file != null) return new FileInputStream(file);
        return new ByteArrayInputStream(content != null ? content : new byte[0]);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        if (file != null) {
            try (FileInputStream in = new FileInputStream(file);
                 FileOutputStream out = new FileOutputStream(dest)) {
                in.transferTo(out);
            }
        } else if (content != null) {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}
