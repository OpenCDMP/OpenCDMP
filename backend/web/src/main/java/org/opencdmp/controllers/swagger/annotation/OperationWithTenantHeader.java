package org.opencdmp.controllers.swagger.annotation;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Operation
public @interface OperationWithTenantHeader {
    @AliasFor(annotation = Operation.class, attribute = "method") String method() default "";
    @AliasFor(annotation = Operation.class, attribute = "tags") String[] tags() default {};
    @AliasFor(annotation = Operation.class, attribute = "summary") String summary() default "";
    @AliasFor(annotation = Operation.class, attribute = "description") String description() default "";
    @AliasFor(annotation = Operation.class, attribute = "requestBody") RequestBody requestBody() default @RequestBody;
    @AliasFor(annotation = Operation.class, attribute = "externalDocs") ExternalDocumentation externalDocs() default @ExternalDocumentation;
    @AliasFor(annotation = Operation.class, attribute = "operationId") String operationId() default "";
    @AliasFor(annotation = Operation.class, attribute = "parameters") Parameter[] parameters() default {
        @Parameter(
                name = "x-tenant",
                description = "This is a header containing the tenant scope of the request. " +
                        "It is required on every authenticated request. " +
                        "If the request does not target a specific tenant resource, this header should be set to the value 'default'.",
                required = true,
                in = ParameterIn.HEADER,
                schema = @Schema(implementation = String.class),
                example = "default"
        )
    };
    @AliasFor(annotation = Operation.class, attribute = "responses") ApiResponse[] responses() default {};
    @AliasFor(annotation = Operation.class, attribute = "deprecated") boolean deprecated() default false;
    @AliasFor(annotation = Operation.class, attribute = "security") SecurityRequirement[] security() default {};
    @AliasFor(annotation = Operation.class, attribute = "servers") Server[] servers() default {};
    @AliasFor(annotation = Operation.class, attribute = "extensions") Extension[] extensions() default {};
    @AliasFor(annotation = Operation.class, attribute = "hidden") boolean hidden() default false;
    @AliasFor(annotation = Operation.class, attribute = "ignoreJsonView") boolean ignoreJsonView() default false;
}
