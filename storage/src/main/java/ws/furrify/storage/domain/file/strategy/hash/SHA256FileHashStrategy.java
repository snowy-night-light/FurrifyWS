package ws.furrify.storage.domain.file.strategy.hash;

import ws.furrify.storage.domain.file.File;

public class SHA256FileHashStrategy implements FileHashStrategy {
    @Override
    public String calculateHash(File file) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean validateHash(String hash, File file) {
        throw new RuntimeException("Not implemented yet");
    }
}
