package org.opencdmp.configurations;

import org.opencdmp.interceptors.tenant.TenantInterceptor;
import org.opencdmp.interceptors.tenant.TenantScopeClaimInterceptor;
import org.opencdmp.interceptors.tenant.TenantScopeHeaderInterceptor;
import org.opencdmp.interceptors.user.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@EnableAsync
@Configuration
@EnableScheduling
public class WebMVCConfiguration implements WebMvcConfigurer {
	private final TenantInterceptor tenantInterceptor;

	private final UserInterceptor userInterceptor;
	private final TenantScopeHeaderInterceptor scopeHeaderInterceptor;
	private final TenantScopeClaimInterceptor scopeClaimInterceptor;
	@Autowired 
	public WebMVCConfiguration(TenantInterceptor tenantInterceptor, UserInterceptor userInterceptor, TenantScopeHeaderInterceptor scopeHeaderInterceptor, TenantScopeClaimInterceptor scopeClaimInterceptor) {
		this.tenantInterceptor = tenantInterceptor;
		this.userInterceptor = userInterceptor;
		this.scopeHeaderInterceptor = scopeHeaderInterceptor;
		this.scopeClaimInterceptor = scopeClaimInterceptor;
	}

	@Autowired
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		int order = 1;
		registry.addWebRequestInterceptor(this.scopeHeaderInterceptor).order(order);
		order++;
		registry.addWebRequestInterceptor(this.scopeClaimInterceptor).order(order);
		order++;
		registry.addWebRequestInterceptor(this.userInterceptor).order(order);
		order++;
		registry.addWebRequestInterceptor(this.tenantInterceptor).order(order);
		order++;
	}
}
