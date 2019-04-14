package spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:foghandoffConfig")
@ComponentScan(basePackages = "foghandoff.fog")
public class SpringConfig {
}