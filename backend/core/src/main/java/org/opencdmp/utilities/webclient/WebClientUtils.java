package org.opencdmp.utilities.webclient;

import org.springframework.web.util.UriBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class WebClientUtils {

    public static URI buildParameters(UriBuilder builder, Object data) {
        List<Field> fields = List.of(data.getClass().getDeclaredFields());
        List<Method> getters = Stream.of(data.getClass().getDeclaredMethods()).filter(method -> method.getName().startsWith("get")).toList();
        fields.forEach(field -> {
            Method getter = getters.stream().filter(method -> method.getName().equals(makeGetterMethodName(field.getName()))).findFirst().orElse(null);
            if (getter != null && getter.canAccess(data)) {
                try {
                    registerParameter(builder, getter.invoke(data), field);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else if (field.canAccess(data)) {
                try {
                    registerParameter(builder, field.get(data), field);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Field " + field.getName() + " is not public and has no public getter Method");
            }
        });
        return builder.build();
    }

    private static void registerParameter(UriBuilder builder, Object value, Field field) {
        if (value != null) {
            builder.queryParam(getParameterName(field), value);
        }
    }

    private static String makeGetterMethodName(String fieldName) {
        return "get" + fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
    }

    private static String getParameterName(Field field) {
        return field.getName();
    }
}
