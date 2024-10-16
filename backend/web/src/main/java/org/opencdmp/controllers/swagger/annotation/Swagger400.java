package org.opencdmp.controllers.swagger.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.*;

import static org.opencdmp.controllers.swagger.SwaggerHelpers.Errors.message_400;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@ApiResponse(
        description = "This is generally the response you should expect when you don't provide correct information and a validation error occurs.",
        responseCode = "400",
        content = {
                @Content(
                        examples = {
                                @ExampleObject(
                                        name = "400 example response",
                                        description = "This could be a response in case of a 400 bad request error. " +
                                                "It should contain a list of all the data requirements that are not fulfilled. " +
                                                "In our example, the property 'name' is mandatory and was not provided in the request body, resulting in the validation error above.",
                                        value = message_400
                                )
                        }
                )
        }
)
public @interface Swagger400 {

}
