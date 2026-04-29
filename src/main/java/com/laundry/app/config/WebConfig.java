package com.laundry.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.convert.converter.Converter;
import com.laundry.app.model.OrderStatus;
import com.laundry.app.model.PaymentStatus;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, OrderStatus>() {
            @Override
            public OrderStatus convert(String source) {
                return OrderStatus.valueOf(source.toUpperCase());
            }
        });
        registry.addConverter(new Converter<String, PaymentStatus>() {
            @Override
            public PaymentStatus convert(String source) {
                return PaymentStatus.valueOf(source.toUpperCase());
            }
        });
    }
}
