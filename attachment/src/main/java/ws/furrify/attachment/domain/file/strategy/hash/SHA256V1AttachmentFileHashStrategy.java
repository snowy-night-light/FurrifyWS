package ws.furrify.attachment.domain.file.strategy.hash;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.springframework.stereotype.Component;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHashType;

import java.io.File;
import java.io.IOException;

@Component
public class SHA256V1AttachmentFileHashStrategy implements AttachmentFileHashStrategy {
    @Override
    public String calculateHash(File file) {
        try {
            return Files.asByteSource(file).hash(Hashing.sha256()).toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean validateHash(String hash, File file) {
        return hash.equals(calculateHash(file));
    }

    @Override
    public AttachmentFileHashType getHashType() {
        return AttachmentFileHashType.SHA256;
    }
}
