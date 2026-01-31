package ws.furrify.storage.domain.file.strategy.hash;

import ws.furrify.core.model.StrategyIntf;
import ws.furrify.storage.domain.file.File;
import ws.furrify.storage.domain.source.strategy.SourceStrategy;

public interface FileHashStrategy extends StrategyIntf {

    String calculateHash(File file);

    boolean validateHash(String hash, File file);
}
