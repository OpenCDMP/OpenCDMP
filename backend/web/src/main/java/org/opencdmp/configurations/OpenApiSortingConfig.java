package org.opencdmp.configurations;


import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class OpenApiSortingConfig {

    private static class OperationHolder {
        private String path;
        private PathItem.HttpMethod method;
        private Operation operation;

        OperationHolder(String path, PathItem.HttpMethod method, Operation operation) {
            this.path = path;
            this.method = method;
            this.operation = operation;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public PathItem.HttpMethod getMethod() {
            return method;
        }

        public void setMethod(PathItem.HttpMethod method) {
            this.method = method;
        }

        public Operation getOperation() {
            return operation;
        }

        public void setOperation(Operation operation) {
            this.operation = operation;
        }
    }

    @Bean
    public OpenApiCustomizer sortOperations() {

        return openApi -> {
            if (openApi.getPaths() == null) return;

            // operators by tag (each tag is rest controller)
            Map<String, List<OperationHolder>> opsByTag = new HashMap<>();

            // collect
            for (Map.Entry<String, PathItem> pathEntry : openApi.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();

                pathItem.readOperationsMap().forEach((method, operation) -> {
                    String tag = getPrimaryTag(operation);
                    opsByTag
                            .computeIfAbsent(tag, k -> new ArrayList<>())
                            .add(new OperationHolder(path, method, operation));
                });
            }

            Paths newPaths = new Paths();

            // for each tag sort operators
            for (Map.Entry<String, List<OperationHolder>> entry : opsByTag.entrySet()) {
                List<OperationHolder> operations = entry.getValue();

                operations.sort(Comparator.comparingInt(op -> getXOrder(op.operation)));

                for (OperationHolder holder : operations) {
                    PathItem pathItem = newPaths.containsKey(holder.path)
                            ? newPaths.get(holder.path)
                            : new PathItem();

                    pathItem.operation(holder.method, holder.operation);
                    newPaths.addPathItem(holder.path, pathItem);
                }
            }

            openApi.setPaths(newPaths);
        };
    }

    private String getPrimaryTag(Operation operation) {
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            return operation.getTags().getFirst(); // get controller
        }
        return "default";
    }

    private int getXOrder(Operation operation) {
        if (operation.getExtensions() != null && operation.getExtensions().containsKey("x-order")) {
            Object xOrder = operation.getExtensions().get("x-order");
            if (xOrder instanceof Map<?, ?> extProps) {
                Object value = extProps.get("value");
                if (value instanceof String s) {
                    try {
                        return Integer.parseInt(s);
                    } catch (Exception e) {}
                }
            } else if (xOrder instanceof String s) {
                try {
                    return Integer.parseInt(s);
                } catch (Exception e) {}
            }
        }
        return Integer.MAX_VALUE;
    }


    @Bean
    public OpenApiCustomizer sortTags() {
        return openApi -> {
            if (openApi.getTags() == null) return;

            List<Tag> sortedTags = openApi.getTags().stream()
                    .sorted(Comparator.comparingInt(tag -> {
                        if (tag.getExtensions() != null && tag.getExtensions().containsKey("x-order")) {
                            Object xOrder = tag.getExtensions().get("x-order");
                            if (xOrder instanceof Map<?, ?> extProps) {
                                Object value = extProps.get("value");
                                if (value instanceof String s) {
                                    try {
                                        return Integer.parseInt(s);
                                    } catch (Exception e) {}
                                }
                            } else if (xOrder instanceof String s) {
                                try {
                                    return Integer.parseInt(s);
                                } catch (Exception e) {}
                            }
                        }
                        return Integer.MAX_VALUE;
                    }))
                    .toList();

            openApi.setTags(sortedTags);
        };
    }

}
