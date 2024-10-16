package org.opencdmp.controllers.swagger.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Swagger500
@Swagger403
public @interface SwaggerCommonErrorResponses {

}
