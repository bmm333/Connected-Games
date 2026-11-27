package com.connectedgames.stats.adapters.in.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Checks a JSON body against the schemas of contracts/openapi/stats.yaml: the code cannot drift from the contract. */
final class OpenApiContract {

    private static final Pattern UUID = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final Map<String, Object> root;

    private OpenApiContract(Map<String, Object> root) {
        this.root = root;
    }

    static OpenApiContract load() {
        try (InputStream in = Files.newInputStream(Path.of("../../contracts/openapi/stats.yaml"))) {
            return new OpenApiContract(new Yaml().load(in));
        } catch (IOException e) {
            throw new IllegalStateException("The contract is not readable from " + Path.of("").toAbsolutePath(), e);
        }
    }

    boolean declares(String path, int status) {
        return map(map(map(root.get("paths")).get(path)).get("get")).get("responses") instanceof Map<?, ?> responses
                && responses.containsKey(String.valueOf(status));
    }

    List<String> validateResponse(String path, int status, String body) {
        Object response = map(map(map(map(root.get("paths")).get(path)).get("get")).get("responses"))
                .get(String.valueOf(status));
        if (response == null) {
            return List.of("The contract has no " + status + " response for " + path);
        }
        Map<String, Object> resolved = resolve(map(response));
        Object content = map(resolved.get("content"));
        Map<String, Object> media = map(content);
        Object schema = media.values().stream().findFirst().map(m -> map(m).get("schema")).orElse(null);
        List<String> errors = new ArrayList<>();
        validate(schema, JSON.readTree(body), "$", errors);
        return errors;
    }

    private void validate(Object schemaOrRef, JsonNode value, String at, List<String> errors) {
        Map<String, Object> schema = flatten(schemaOrRef);
        if (value.isNull()) {
            errors.add(at + " is null, and the contract has no nullable fields");
            return;
        }
        Object type = schema.get("type");
        if ("object".equals(type)) {
            validateObject(schema, value, at, errors);
        } else if ("array".equals(type)) {
            if (!value.isArray()) {
                errors.add(at + " must be an array");
                return;
            }
            Object min = schema.get("minItems");
            if (min instanceof Number n && value.size() < n.intValue()) {
                errors.add(at + " has fewer than " + n + " items");
            }
            for (int i = 0; i < value.size(); i++) {
                validate(schema.get("items"), value.get(i), at + "[" + i + "]", errors);
            }
        } else if ("string".equals(type)) {
            validateString(schema, value, at, errors);
        } else if ("integer".equals(type)) {
            if (!value.isIntegralNumber()) {
                errors.add(at + " must be an integer");
            } else if (schema.get("minimum") instanceof Number min && value.asLong() < min.longValue()) {
                errors.add(at + " is below the minimum " + min);
            }
        } else {
            errors.add(at + " has a type the checker does not know: " + type);
        }
    }

    private void validateObject(Map<String, Object> schema, JsonNode value, String at, List<String> errors) {
        if (!value.isObject()) {
            errors.add(at + " must be an object");
            return;
        }
        Map<String, Object> properties = map(schema.get("properties"));
        Set<String> present = new LinkedHashSet<>();
        value.propertyNames().forEach(present::add);
        for (Object required : list(schema.get("required"))) {
            if (!present.contains(required)) {
                errors.add(at + "." + required + " is required and missing");
            }
        }
        for (String name : present) {
            if (!properties.containsKey(name)) {
                errors.add(at + "." + name + " is not in the contract");
            } else {
                validate(properties.get(name), value.get(name), at + "." + name, errors);
            }
        }
    }

    private void validateString(Map<String, Object> schema, JsonNode value, String at, List<String> errors) {
        if (!value.isString()) {
            errors.add(at + " must be a string");
            return;
        }
        String text = value.asString();
        if (schema.get("enum") instanceof List<?> allowed && !allowed.contains(text)) {
            errors.add(at + " is " + text + ", not one of " + allowed);
        }
        if ("uuid".equals(schema.get("format")) && !UUID.matcher(text).matches()) {
            errors.add(at + " is not a uuid: " + text);
        }
        if ("date-time".equals(schema.get("format"))) {
            try {
                OffsetDateTime.parse(text);
            } catch (RuntimeException e) {
                errors.add(at + " is not a date-time: " + text);
            }
        }
    }

    private Map<String, Object> flatten(Object schemaOrRef) {
        Map<String, Object> schema = resolve(map(schemaOrRef));
        Map<String, Object> merged = new LinkedHashMap<>(schema);
        merged.remove("allOf");
        for (Object part : list(schema.get("allOf"))) {
            Map<String, Object> flat = flatten(part);
            Map<String, Object> properties = new LinkedHashMap<>(map(merged.get("properties")));
            properties.putAll(map(flat.get("properties")));
            List<Object> required = new ArrayList<>(list(merged.get("required")));
            required.addAll(list(flat.get("required")));
            merged.putAll(flat);
            merged.put("properties", properties);
            merged.put("required", required);
        }
        return merged;
    }

    private Map<String, Object> resolve(Map<String, Object> node) {
        Object ref = node.get("$ref");
        if (ref == null) {
            return node;
        }
        Object target = root;
        for (String part : ((String) ref).substring(2).split("/")) {
            target = map(target).get(part);
        }
        return resolve(map(target));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    private static List<Object> list(Object value) {
        return value instanceof List<?> l ? new ArrayList<>(l) : List.of();
    }
}
