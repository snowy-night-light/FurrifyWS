package ws.furrify.attachment.strategy.hash;

import lombok.SneakyThrows;
import ws.furrify.attachment.domain.file.strategy.hash.AttachmentFileHashStrategy;

import java.io.File;
import java.net.URL;

public abstract class AttachmentFileHashStrategyTest {

    protected final AttachmentFileHashStrategy strategy;

    public AttachmentFileHashStrategyTest(AttachmentFileHashStrategy strategy) {
        this.strategy = strategy;
    }

    @SneakyThrows
    private File getExampleFile() {
        URL resourceUrl = getClass().getClassLoader().getResource("files/example.png");
        assert resourceUrl != null;
        return new File(resourceUrl.toURI());
    }

    protected String getHashForExampleFile() {


        return strategy.calculateHash(getExampleFile());
    }

    protected boolean validateHashForExampleFile(String hash) {
        return strategy.validateHash(hash, getExampleFile());
    }

    abstract void testHash();
    abstract void validateHash();
    abstract void validateHashType();
}
