package ws.furrify.attachment.domain.file.strategy.hash;

import ws.furrify.attachment.domain.file.vo.AttachmentFileHashType;
import ws.furrify.core.model.StrategyIntf;

import java.io.File;


public interface AttachmentFileHashStrategy extends StrategyIntf {

    String calculateHash(File file);

    boolean validateHash(String hash, File file);

    AttachmentFileHashType getHashType();
}
