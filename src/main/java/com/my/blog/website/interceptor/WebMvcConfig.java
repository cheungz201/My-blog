package com.my.blog.website.interceptor;


import com.my.blog.website.utils.TaleUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 向mvc中添加自定义组件
 *
 * @author Zhang Zhe
 * @date 2021/5/29
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private BaseInterceptor baseInterceptor;

    @Resource
    private BlackInterceptor blackInterceptor;

    /**
     * @Time:  2021/5/29 10:01
     * @Params:
     * @Return: NULL
     * @Description: 添加拦截器
     **/
    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(blackInterceptor);
        interceptorRegistry.addInterceptor(baseInterceptor)
                .excludePathPatterns("/js/**","/css/**","/img/**","/imgs/**","js/**","css/**","img/**","imgs/**");
    }


    /**
     * 添加静态资源文件，外部可以直接访问地址
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + TaleUtils.getUploadFilePath() + "upload/");

    }


    @Override
    public void configurePathMatch(PathMatchConfigurer pathMatchConfigurer) {}

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer contentNegotiationConfigurer) {}

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer asyncSupportConfigurer) {}

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer defaultServletHandlerConfigurer) {}

    @Override
    public void addFormatters(FormatterRegistry formatterRegistry) {}

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {}

    @Override
    public void addViewControllers(ViewControllerRegistry viewControllerRegistry) {}

    @Override
    public void configureViewResolvers(ViewResolverRegistry viewResolverRegistry) {}

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> list) {}

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> list) {}

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> list) {}

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> list) {}

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> list) {}

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> list) {

    }

    @Override
    public Validator getValidator() {
        return null;
    }

    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        return null;
    }
}
