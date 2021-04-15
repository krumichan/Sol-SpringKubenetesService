package kr.co.classact.rancher.apicore._0_generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import kr.co.classact.rancher.apicore._0_generator.utility.FileUtility;
import kr.co.classact.rancher.apicore._0_generator.utility.StringUtility;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
public class Generator {

    private final StringUtility stringUtility = StringUtility.ins();
    private final FileUtility fileUtility = FileUtility.ins();
    private final Convertor convertor = new Convertor();

    private final String SOURCE_OUTPUT_DIR = "src/main/java/kr/co/classact/rancher/apicore";

    private final String TYPE_SOURCE_OUTPUT_DIR = SOURCE_OUTPUT_DIR + "/type/";
    private final String SERVICE_SOURCE_OUTPUT_DIR = SOURCE_OUTPUT_DIR + "/service/";

    private final String CURRENT_DIR = SOURCE_OUTPUT_DIR + "/_0_generator/";
    private final String TYPE_PACKAGE_PREFIX = "kr.co.classact.rancher.apicore.type.";

    private final String TYPE_SUFFIX = ".java";
    private final String SERVICE_SUFFIX = "Service.java";

    private final Map<String, Boolean> blackListTypes;
    private final Map<String, Boolean> blackListActions;
    private final Map<String, Boolean> noConversionTypes;
    private final Map<String, Boolean> schemaExists;

    private final ObjectMapper om;

    public Generator() {
        om = new ObjectMapper();
        blackListTypes = new HashMap<>() {{
            put("schema", true);
            put("resource", true);
            put("collection", true);
        }};

        blackListActions = new HashMap<>() {{
            put("create", true);
            put("update", true);
        }};

        noConversionTypes = new HashMap<>() {{
            put("string", true);
        }};

        schemaExists = new HashMap<>();
    }

    public static void main(String[] args) {
        Generator generator = new Generator();

        try {
            List<Schema.TYPE> schemasTypeList = Arrays
                    .stream(Schema.TYPE.values())
                    .collect(Collectors.toList());

            for (Schema.TYPE schemasType : schemasTypeList) {
                generator.generateFiles(schemasType);
            }
        } catch (Exception e) {
            log.fatal("Failed to generate Rancher Type and Service.", e);
            System.exit(1);
        }
    }

    public void generateFiles(Schema.TYPE schemaType) throws Exception {
        ArrayNode schemas =
                getSchemasArrayNode(
                        getSchemasNode(
                                Schema.schema(schemaType)
                        )
                );

        for (JsonNode schema : schemas) {
            if (containedBlackListTypes(getSchemaId(schema))) {
                continue;
            }

            schemaExists.put(getSchemaId(schema), true);

            generateType(schema);
            generateService(schema);
        }
    }

    private ArrayNode getSchemasArrayNode(JsonNode dataNode) throws Exception {
        if (!dataNode.isArray()) {
            throw new Exception("['schema.data'] attribute is not array data." + "\n" + "schema: " + dataNode);
        }
        return (ArrayNode) dataNode;
    }

    private JsonNode getSchemasNode(JsonNode schema) throws Exception {
        if (!schema.has("data")) {
            throw new Exception("Failed to get ['data'] attribute in schema json node." + "\n" + "schema: " + schema);
        }
        return schema.get("data");
    }

    private String getSchemaId(JsonNode dataNode) throws Exception {
        if (!dataNode.has("id")) {
            throw new Exception("Failed to get ['id'] attribute in schema data json node." + "\n" + "data: " + dataNode);
        }
        return dataNode.get("id").textValue();
    }

    private boolean containedBlackListTypes(String type) {
        return blackListTypes.containsKey(type) && blackListTypes.get(type);
    }

    private void generateType(JsonNode schema) throws Exception {
        generateTemplate(
                schema
                , TYPE_SOURCE_OUTPUT_DIR
                , TYPE_SUFFIX
                , "type.vm"
                , false);
    }

    private void generateService(JsonNode schema) throws Exception {
        generateTemplate(
                schema
                , SERVICE_SOURCE_OUTPUT_DIR
                , SERVICE_SUFFIX
                , "service.vm"
                , false);
    }

    private void generateTemplate(
            JsonNode schema
            , String outputPath
            , String fileSuffix
            , String templateName
            , boolean removeOption) throws Exception {

        fileUtility.setupDirectory(outputPath, removeOption);
        Map<String, Object> dataMap = getDataMap(schema);

        convertor.execute(
                dataMap
                , CURRENT_DIR + templateName
                , outputPath
                        + stringUtility.capitalize.apply(getSchemaId(schema)) + fileSuffix
                , removeOption
        );
    }

    private Map<String, Object> getDataMap(JsonNode schema) throws Exception {
        TypeMap typeMap = getSchemaTypeMap(schema);

        return new HashMap<>() {{
            put("schema", schema);
            put("class", stringUtility.capitalize.apply(getSchemaId(schema)));
            put("collection", getSchemaId(schema) + "Collection");
            put("structFields", typeMap.result);
            put("resourceActions", getResourceActions(schema, typeMap.meta));
            put("type", getSchemaId(schema));
            put("importList", typeMap.meta.listImports());
            put("importActionList", typeMap.meta.listActionImports());
            put("function", customFunctionMap());
        }};
    }

    private TypeMap getSchemaTypeMap(JsonNode schema) throws Exception {
        return getTypeMap(schema);
    }

    private TypeMap getTypeMap(JsonNode schema) {
        TypeMap typeMap = new TypeMap();
        if (!schema.has("resourceFields")) {
            log.warn("Current data has not ['resourceFields'] field." + "\n" + "current: " + schema);
            return typeMap;
        }

        Map<String, Map<String, Object>> resourceFields
                = om.convertValue(schema.get("resourceFields"), new TypeReference<>() {});

        resourceFields.forEach((name, fields) -> {
            String fieldName = stringUtility.capitalize.apply(name);

            if (!fields.containsKey("type")) {
                log.warn("['" + fieldName + "'] field has not ['type'] field.");
                return;
            }
            String type = (String) fields.get("type");

            if (stringUtility.startsWith(type, "reference", "data", "enum", "dnsLabel", "hostname")) {
                typeMap.result.put(fieldName, "String");
            } else if (stringUtility.startsWith(type, "array[reference[")) {
                typeMap.result.put(fieldName, "List<String>");
                typeMap.meta.importClass("java.util.List");
            } else if (stringUtility.startsWith(type, "array[array[")) {
                typeMap.meta.importClass("java.util.List");

                String innerType = stringUtility.trimSuffix(
                        stringUtility.trimPrefix(type, "array["), "]");

                switch (innerType) {
                    case "array[reference]":
                    case "array[data]":
                    case "array[enum]":
                    case "array[string]":
                        typeMap.result.put(fieldName, "List<List<String>>");
                        break;

                    case "array[int]":
                        typeMap.result.put(fieldName, "List<List<Integer>>");
                        break;

                    case "array[float64]":
                    case "array[float]":
                        typeMap.result.put(fieldName, "List<List<Float>>");
                        break;

                    case "array[json]":
                        typeMap.result.put(fieldName, "List<List<Map<String, Object>>>");
                        break;

                    default:
                        String fieldType = stringUtility.trimSuffix(
                                stringUtility.trimPrefix(type, "array["), "]");
                        String className = stringUtility.capitalize.apply(fieldType);
                        typeMap.result.put(fieldName, "List<List<" + className + ">>");
                }

            }  else if (stringUtility.startsWith(type, "array")) {
                typeMap.meta.importClass("java.util.List");

                switch (type) {
                    case "array[reference]":
                    case "array[data]":
                    case "array[enum]":
                    case "array[string]":
                        typeMap.result.put(fieldName, "List<String>");
                        break;

                    case "array[int]":
                        typeMap.result.put(fieldName, "List<Integer>");
                        break;

                    case "array[float64]":
                    case "array[float]":
                        typeMap.result.put(fieldName, "List<Float>");
                        break;

                    case "array[json]":
                        typeMap.result.put(fieldName, "List<Map<String, Object>>");
                        break;

                    default:
                        String fieldType = stringUtility.trimSuffix(
                                stringUtility.trimPrefix(type, "array["), "]");
                        String className = stringUtility.capitalize.apply(fieldType);
                        typeMap.result.put(fieldName, "List<" + className + ">");
                }
            } else if (stringUtility.startsWith(type, "map", "json")) {
                typeMap.result.put(fieldName, "Map<String, Object>");
                typeMap.meta.importClass("java.util.Map");
            } else if (stringUtility.startsWith(type, "boolean")) {
                typeMap.result.put(fieldName, "Boolean");
            } else if (stringUtility.startsWith(type, "extensionPoint")) {
                typeMap.result.put(fieldName, "Object");
            } else if (stringUtility.startsWith(type, "float")) {
                typeMap.result.put(fieldName, "Float");
            } else if (stringUtility.startsWith(type, "int")) {
                typeMap.result.put(fieldName, "Integer");
            } else if (stringUtility.startsWith(type, "base64")) {
                typeMap.result.put(fieldName, "Byte[]");
            } else if (stringUtility.startsWith(type, "date")) {
               typeMap.result.put(fieldName, "Date");
               typeMap.meta.importClass("java.util.Date");
            } else {
                typeMap.result.put(fieldName, stringUtility.capitalize.apply(type));
            }
        });

        return typeMap;
    }

    private Map<String, Map<String, String>> getResourceActions(
            JsonNode schema
            , Metadata metadata) throws Exception {

        Map<String, Map<String, String>> resourceActionMap = new HashMap<>();

        if (!schema.has("resourceActions")) {
            log.warn("Current data has not ['resourceActions'] field." + "\n" + "current: " + schema);
            return resourceActionMap;
        }

        Map<String, Map<String, String>> resourceActions
                = om.convertValue(schema.get("resourceActions"), new TypeReference<>() {});

        for (Map.Entry<String, Map<String, String>> resourceAction : resourceActions.entrySet()) {
            String name = resourceAction.getKey();
            Map<String, String> action = resourceAction.getValue();

            if (action.containsKey("output")) {
                if (schemaExists.containsKey(name)) {
                    continue;
                }

                String nodeId = getSchemaId(schema);
                String className = stringUtility.capitalize.apply(nodeId);

                if (action.containsKey("input") &&
                    !Objects.isNull(action.get("input")) &&
                    !stringUtility.capitalize.apply(String.valueOf(action.get("input"))).equals(className)) {
                    metadata.importActionClass(
                            TYPE_PACKAGE_PREFIX
                                    + stringUtility.capitalize.apply(String.valueOf(action.get("input")))
                    );
                }

                if (action.containsKey("output") &&
                    !Objects.isNull(action.get("output")) &&
                    !stringUtility.capitalize.apply(String.valueOf(action.get("output"))).equals(className)) {
                    metadata.importActionClass(
                            TYPE_PACKAGE_PREFIX
                                    + stringUtility.capitalize.apply(String.valueOf(action.get("output")))
                    );
                }

                resourceActionMap.put(name, action);
            }
        }

        return resourceActionMap;
    }

    private Map<String, Function<String, String>> customFunctionMap() {
        return new HashMap<>() {{
            put("toCamelCase", stringUtility.toLowerCamelCase);
            put("toLowerUnderscore", stringUtility.addUnderscore);
            put("dashToUnderscore", stringUtility.toUnderscore);
            put("capitalize", stringUtility.capitalize);
            put("upper", stringUtility.upper);
        }};
    }

    public static class Metadata {
        private final Map<String, Boolean> importTypes = new HashMap<>();
        private final Map<String, Boolean> actionImportTypes = new HashMap<>();

        public void importActionClass(String className) {
            actionImportTypes.put(className, true);
        }

        public void importClass(String className) {
            importTypes.put(className, true);
        }

        public List<String> listActionImports() {
            return toList(actionImportTypes);
        }

        public List<String> listImports() {
            return toList(importTypes);
        }

        private List<String> toList(Map<String, Boolean> inputMap) {
            return inputMap
                    .entrySet()
                    .stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
    }

    public static class TypeMap {
        public Map<String, String> result = new HashMap<>();
        public Metadata meta = new Metadata();
    }
}
