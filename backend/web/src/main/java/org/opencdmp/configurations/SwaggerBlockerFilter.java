package org.opencdmp.configurations;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.opencdmp.configurations.swagger.SwaggerConfigProperties;
import org.opencdmp.configurations.swagger.SwaggerSpringDocProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SwaggerBlockerFilter implements Filter {

    private final SwaggerConfigProperties swaggerConfigProperties;
    private final SwaggerSpringDocProperties springDocProperties;

    public SwaggerBlockerFilter(SwaggerConfigProperties swaggerConfigProperties, SwaggerSpringDocProperties springDocProperties) {
        this.swaggerConfigProperties = swaggerConfigProperties;
        this.springDocProperties = springDocProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (!this.swaggerConfigProperties.isEnabled() && (
                req.getRequestURI().startsWith(this.springDocProperties.getSwaggerUi().getPath()) ||
                        req.getRequestURI().startsWith(this.springDocProperties.getApiDocs().getPath())
        )) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        chain.doFilter(request, response);
    }
}
