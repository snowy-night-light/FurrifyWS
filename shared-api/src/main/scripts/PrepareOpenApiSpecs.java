/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Prepares OpenAPI specifications for OpenAPI Generator by adding nullable support
 * to Patch request schemas without modifying the source Swagger-generated JSON files.
 */
/**
 * Adjusts the OpenAPI specs before the clients are generated from them. Run by the build with the Java source
 * launcher and Jackson on its class path (see the shared-api pom).
 */
public class PrepareOpenApiSpecs {

    /** Objects keep their key order; numbers keep their exact value (no rounding through double). */
    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java PrepareOpenApiSpecs.java <inputDir> <outputDir>");
            System.exit(1);
        }

        Path inputDir = Paths.get(args[0]);
        Path outputDir = Paths.get(args[1]);

        Files.createDirectories(outputDir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, "*.json")) {
            for (Path file : stream) {
                processFile(file, outputDir.resolve(file.getFileName()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void processFile(Path inputPath, Path outputPath) throws IOException {
        String content = Files.readString(inputPath, StandardCharsets.UTF_8);
        Object parsed = JSON.readValue(content, Object.class);

        if (parsed instanceof Map<?, ?> root) {
            Map<String, Object> rootMap = (Map<String, Object>) root;
            Object componentsObj = rootMap.get("components");
            if (componentsObj instanceof Map<?, ?> components) {
                Object schemasObj = ((Map<String, Object>) components).get("schemas");
                if (schemasObj instanceof Map<?, ?> schemas) {
                    for (Map.Entry<String, Object> entry : ((Map<String, Object>) schemas).entrySet()) {
                        String schemaName = entry.getKey();
                        if (schemaName.startsWith("Patch") && entry.getValue() instanceof Map<?, ?> schemaMap) {
                            Object propertiesObj = ((Map<String, Object>) schemaMap).get("properties");
                            if (propertiesObj instanceof Map<?, ?> properties) {
                                for (Map.Entry<String, Object> propEntry : ((Map<String, Object>) properties).entrySet()) {
                                    if (propEntry.getValue() instanceof Map<?, ?> propMap) {
                                        Map<String, Object> prop = (Map<String, Object>) propMap;
                                        if (prop.containsKey("$ref")) {
                                            Object refVal = prop.remove("$ref");
                                            Map<String, Object> refItem = new LinkedHashMap<>();
                                            refItem.put("$ref", refVal);
                                            prop.put("allOf", Collections.singletonList(refItem));
                                        }
                                        prop.put("nullable", true);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Ensure operations with multipart/form-data do not also have application/json,
            // which causes OpenAPI Generator to default to application/json.
            Object pathsObj = rootMap.get("paths");
            if (pathsObj instanceof Map<?, ?> paths) {
                for (Object pathItemObj : paths.values()) {
                    if (pathItemObj instanceof Map<?, ?> pathItem) {
                        for (Object opObj : pathItem.values()) {
                            if (opObj instanceof Map<?, ?> op) {
                                Object reqBodyObj = ((Map<String, Object>) op).get("requestBody");
                                if (reqBodyObj instanceof Map<?, ?> reqBody) {
                                    Object contentObj = ((Map<String, Object>) reqBody).get("content");
                                    if (contentObj instanceof Map<?, ?> contentMap) {
                                        Map<String, Object> contentMapTyped = (Map<String, Object>) contentMap;
                                        if (contentMapTyped.containsKey("multipart/form-data") && contentMapTyped.containsKey("application/json")) {
                                            contentMapTyped.remove("application/json");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        String outputJson = JSON.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);
        Files.writeString(outputPath, outputJson, StandardCharsets.UTF_8);
        System.out.println("Prepared OpenAPI spec: " + outputPath);
    }
}
