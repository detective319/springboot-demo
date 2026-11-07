package com.example.demo.config;

import com.example.demo.wicket.WicketApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WicketConfig {
    @Bean
    public FilterRegistrationBean<WicketFilter> wicketFilter() {
        WicketFilter filter = new WicketFilter(new WicketApplication());
        FilterRegistrationBean<WicketFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("wicket");
        registration.addUrlPatterns("/ui/*");
        registration.addInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/ui/*");
        registration.addInitParameter("configuration", "deployment");
        return registration;
    }
}
