package org.opencdmp.controllers.swagger.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

import static org.opencdmp.controllers.swagger.SwaggerHelpers.Errors.message_403;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@ApiResponse(
        description = "This is generally the response you should expect when you don't have sufficient permissions to perform an action.",
        responseCode = "403",
        content = {
                @Content(
                        examples = {
                                @ExampleObject(
                                        name = "403 response",
                                        description = "This is the response in case of a 403 error.",
                                        value = message_403
                                )
                        }
                )
        }
)
public @interface Swagger403 {

}
