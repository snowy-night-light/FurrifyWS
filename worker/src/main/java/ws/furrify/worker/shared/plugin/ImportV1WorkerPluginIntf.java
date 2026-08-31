package ws.furrify.worker.shared.plugin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.File;
import java.util.UUID;

@Schema(type = "string")
public interface ImportV1WorkerPluginIntf extends WorkerPluginIntf {
    /**
     * @return Name used as a key for storing statistics tied to the specific source.
     */
    String getProviderName();

    /**
     * Validates the provided file against the plugin's required schema.
     * 
     * @param file The file to validate
     * @return true if the file is valid according to the schema, false otherwise
     */
    boolean validateSchema(File file);

    /**
     * Loads all data from selected schema and files into respective services.
     * 
     * @param file The archive file containing the data to load
     */
    void loadSchemaDataIntoLibrary(File file, UUID libraryId);
}
