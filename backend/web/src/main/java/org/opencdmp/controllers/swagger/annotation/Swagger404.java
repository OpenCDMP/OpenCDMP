package org.opencdmp.controllers.swagger.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

import static org.opencdmp.controllers.swagger.SwaggerHelpers.Errors.message_404;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@ApiResponse(
        description = "This is generally the response you should expect when an entity is not found or you don't have sufficient permissions to view it.",
        responseCode = "404",
        content = {
                @Content(
                        examples = {
                                @ExampleObject(
                                        name = "404 response",
                                        description = "This is the response in case of a 404 error where the first placeholder {0} will be replaced by the item id and the second one {1} by the item type.",
                                        value = message_404
                                )
                        }
                )
        }
)
public @interface Swagger404 {

}
