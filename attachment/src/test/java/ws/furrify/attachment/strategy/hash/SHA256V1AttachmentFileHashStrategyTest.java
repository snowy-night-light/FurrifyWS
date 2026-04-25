package ws.furrify.attachment.strategy.hash;

import org.junit.jupiter.api.Test;
import ws.furrify.attachment.domain.file.strategy.hash.SHA256V1AttachmentFileHashStrategy;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHashType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SHA256V1AttachmentFileHashStrategyTest extends AttachmentFileHashStrategyTest {
    public SHA256V1AttachmentFileHashStrategyTest() {
        super(new SHA256V1AttachmentFileHashStrategy());
    }

    private final String VALID_HASH = "aad96d410d92b5589d41e8462507e3af57682022db3d3711a236c0245fcf296e";

    @Test
    @Override
    void testHash() {
        String hash = getHashForExampleFile();

        assertEquals(VALID_HASH, hash);
    }

    @Test
    @Override
    void validateHash() {
        assertTrue(validateHashForExampleFile(VALID_HASH));
    }

    @Test
    @Override
    void validateHashType() {
        assertEquals(AttachmentFileHashType.SHA256, strategy.getHashType());
    }
}
