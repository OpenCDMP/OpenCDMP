package org.opencdmp.controllers.swagger.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

import static org.opencdmp.controllers.swagger.SwaggerHelpers.Errors.message_500;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@ApiResponse(
        description = "This is the response you should expect when an unexpected exception has occurred.",
        responseCode = "500",
        content = {
                @Content(
                        examples = {
                                @ExampleObject(
                                        name = "500 response",
                                        description = "This is the response in case of an internal server error.",
                                        value = message_500
                                )
                        }
                )
        }
)
public @interface Swagger500 {

}
