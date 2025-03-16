package com.vtp.vipo.seller.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.flipkart.zjsonpatch.JsonDiff;
import com.vtp.vipo.seller.common.dto.response.merchant.MerchantLogAttributeChangeInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonMapperUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMapperUtils.class);

    public static <O> String writeValueAsString(O o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

    public static <T> T convertJsonToObject(String jsonStr, final Class<T> clazz) {
        try {
            return mapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T convertJsonToObject(String jsonStr, final TypeReference<T> reference) {
        try {
            return mapper.readValue(jsonStr, reference);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }


    /**
     * Deserializes a JSON string into an object of the specified type, supporting generic types.
     *
     * <p>This method converts a JSON-formatted {@code String} into an instance of the desired
     * Java type {@code T}. It is capable of handling generic types by accepting a parent type
     * and its parameter classes, allowing for the deserialization of complex nested structures.
     *
     * <p><strong>Example Usage:</strong>
     * <pre>{@code
     * // Deserializing a simple object
     * User user = JsonUtils.fromJson(jsonString, User.class);
     *
     * // Deserializing a generic type, e.g., List<User>
     * List<User> users = JsonUtils.fromJson(jsonString, List.class, User.class);
     * }</pre>
     *
     * @param json              the JSON string to deserialize
     * @param parentType        the parent class type (e.g., List.class for List<User>)
     * @param parameterClasses  the parameter classes for generic types (e.g., User.class for List<User>)
     * @param <T>               the type of the object to return
     * @return the deserialized object of type {@code T}, or {@code null} if the input {@code json} is {@code null} or deserialization fails
     */
    public static <T> T fromJson(String json, Class<?> parentType, Class<?>... parameterClasses) {
        // Return null immediately if the input JSON string is null
        if (json == null) {
            return null;
        }

        T object = null;
        try {
            // Construct the JavaType for the desired object, supporting generic types
            JavaType type = mapper.getTypeFactory().constructParametricType(parentType, parameterClasses);

            // Deserialize the JSON string into an object of the specified JavaType
            object = mapper.readValue(json, type);
        } catch (IOException e) {
            // Log the error message if deserialization fails
            log.error("Failed to deserialize JSON to type {}: {}", parentType.getName(), e.getMessage());
        }

        // Return the deserialized object, or null if deserialization failed
        return object;
    }

    /**
     * Compares two JSON strings and maps the differences to a list of MerchantLogAttributeChangeInfo.
     *
     * @param originalJson the original JSON string
     * @param updatedJson  the updated JSON string
     * @return a List of MerchantLogAttributeChangeInfo representing the differences.
     * @throws Exception if JSON parsing fails.
     */
    public static List<MerchantLogAttributeChangeInfo> mapJsonDiffToAttributeChangeInfo(
            String originalJson, String updatedJson
    ) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // Parse JSON strings into JsonNode trees.
        JsonNode originalNode = mapper.readTree(originalJson);
        JsonNode updatedNode = mapper.readTree(updatedJson);

        // Generate the JSON patch that represents differences between original and updated JSON.
        JsonNode patchNode = JsonDiff.asJson(originalNode, updatedNode);

        List<MerchantLogAttributeChangeInfo> changes = new ArrayList<>();

        // Iterate over each patch operation.
        for (JsonNode operation : patchNode) {
            String op = operation.get("op").asText();
            String path = operation.get("path").asText();

            // Convert JSON pointer (e.g., "/status" or "/address/street") to a field name.
            // Here, we simply remove the leading "/" and replace subsequent "/" with a dot.
            String field = path.startsWith("/") ? path.substring(1).replace("/", ".") : path;
            String from = null;
            String to = null;

            switch (op) {
                case "replace":
                    // For replace, retrieve the original value using the JSON pointer.
                    JsonNode originalValue = originalNode.at(path);
                    from = getValueAsString(originalValue);

                    // The patch contains the new value.
                    JsonNode newValue = operation.get("value");
                    to = getValueAsString(newValue);
                    break;

                case "add":
                    // For add, there is no original value.
                    from = null;
                    newValue = operation.get("value");
                    to = getValueAsString(newValue);
                    break;

                case "remove":
                    // For remove, the new value is null.
                    JsonNode removedValue = originalNode.at(path);
                    from = getValueAsString(removedValue);
                    to = null;
                    break;

                default:
                    continue;
            }

            MerchantLogAttributeChangeInfo info = MerchantLogAttributeChangeInfo.builder()
                    .field(field)
                    .from(from)
                    .to(to)
                    .build();

            changes.add(info);
        }

        return changes;
    }

    /**
     * Converts a JsonNode to a String. If the node is a value node, returns its text;
     * otherwise, returns its JSON string representation.
     *
     * @param node the JsonNode
     * @return a String representation or null if node is missing/null.
     */
    private static String getValueAsString(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        return node.isValueNode() ? node.asText() : node.toString();
    }

}