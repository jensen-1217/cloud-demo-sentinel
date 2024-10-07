package cn.itcast.feign.config;

import cn.itcast.feign.fallback.UserClientFallback;
import feign.Logger;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfiguration  {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.BASIC; // 日志级别为BASIC
    }
    @Bean
    public UserClientFallback userClientFallbackFactory(){
        return new UserClientFallback();
    }
}