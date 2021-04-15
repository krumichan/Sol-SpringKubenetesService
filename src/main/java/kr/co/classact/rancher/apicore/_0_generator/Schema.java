package kr.co.classact.rancher.apicore._0_generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;


public class Schema {
    public enum TYPE {
        GLOBAL("global")
        , CLUSTER("cluster")
        , PROJECT("project")
        ,;

        private final String type;
        TYPE(String type) { this.type = type; }
        public String get() { return type; }
    }

    private static final String PATH_PREFIX = "src/main/java/kr/co/classact/rancher/apicore/_0_generator/schema/";
    private static final String PATH_SUFFIX = "-schema.txt";

    private static ObjectMapper mapper;
    private static Map<TYPE, JsonNode> schemaMap;

    public static JsonNode schema(TYPE schemaType) throws Exception {
        return Schema.map().get(schemaType);
    }

    public static Map<TYPE, JsonNode> map() throws Exception {
        if (schemaMap == null) {
            schemaMap = new HashMap<>();
            try {
                mapper = new ObjectMapper();
                List<TYPE> types =
                        Arrays.stream(TYPE.values())
                                .collect(Collectors.toList());
                Schema.generate(schemaMap, types);
            } catch (Exception e) {
                throw new Exception("Failed to get schemas map.", e);
            }
        }

        return Schema.schemaMap;
    }

    private static void generate(Map<TYPE, JsonNode> schemaMap, List<TYPE> types) throws Exception {
        for (TYPE type : types) {
            generate(schemaMap, type);
        }
    }

    private static void generate(Map<TYPE, JsonNode> schemaMap, TYPE type) throws Exception {
        schemaMap.put(
                type
                , genCore(fixGen(type.get()))
        );
    }

    private static JsonNode genCore(String path) throws Exception {
        File file = toFile(path);
        String content = Files.readString(file.toPath(), StandardCharsets.US_ASCII);

        try {
            return mapper.readTree(content);
        } catch (IOException e) {
            throw new Exception("[" + path + "]'s content is not json string.", e);
        }
    }

    private static String fixGen(String type) {
        return PATH_PREFIX + type + PATH_SUFFIX;
    }

    private static File toFile(String path) throws Exception {
        File file = new File(path);

        if (!file.exists() || !file.isFile()) {
            throw new Exception("[" + path + "] is not exists or file.");
        }

        return file;
    }
}
