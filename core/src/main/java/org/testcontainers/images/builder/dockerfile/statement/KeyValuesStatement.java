package org.testcontainers.images.builder.dockerfile.statement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class KeyValuesStatement extends Statement {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected final Map<String, String> entries;

    public KeyValuesStatement(String type, Map<String, String> entries) {
        super(type);
        this.entries = entries;
    }

    @Override
    @SneakyThrows(JsonProcessingException.class)
    public void appendArguments(StringBuilder dockerfileStringBuilder) {
        Set<Map.Entry<String, String>> entries = this.entries.entrySet();

        Iterator<Map.Entry<String, String>> iterator = entries.iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();

            dockerfileStringBuilder.append(objectMapper.writeValueAsString(entry.getKey()));
            dockerfileStringBuilder.append("=");
            dockerfileStringBuilder.append(objectMapper.writeValueAsString(entry.getValue()));

            if (iterator.hasNext()) {
                dockerfileStringBuilder.append(" \\\n\t");
            }
        }
    }
}
