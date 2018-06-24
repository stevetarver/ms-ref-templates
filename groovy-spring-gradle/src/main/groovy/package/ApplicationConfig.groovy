package ~~ROOT_PACKAGE~~

import ~~ROOT_PACKAGE~~.common.interceptor.RequestInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Configuration
class ApplicationConfig extends WebMvcConfigurerAdapter {

    @Override
    void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor())
    }
}
