package com.nashtech.rookies.oam.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    public static <T> T getBean(Class<T> requiredType) {
        return context.getBean(requiredType);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        SpringContext.context = ctx;
    }
}
