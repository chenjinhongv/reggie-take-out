package com.hippo.reggietakeout.config;

import com.hippo.reggietakeout.common.JacksonObjectMapper;
import com.hippo.reggietakeout.interceptor.LoginCheckInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport { // 继承WebMvcConfigurationSupport类
    /**
     * 重写addResourceHandlers方法以配置静态资源映射
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // addResourceHandler添加需要映射的请求路由，addResourceLocations配置对应的资源路径
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
        log.info("静态资源映射完成...");
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor()).addPathPatterns("/**").excludePathPatterns(
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        );
        log.info("登录状态拦截器添加完成...");
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将自定义的转换器对象添加的原转换器列表中
        converters.add(0, messageConverter);
//        super.extendMessageConverters(converters);
    }
}
