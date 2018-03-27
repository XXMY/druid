package com.alibaba.druid.spring.boot.autoconfigure.stat;

import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidClientConnectionProperties;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.support.http.remote.MonitorStatViewServlet;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.druid.monitor-stat-view-servlet.enabled", havingValue = "true")
@EnableConfigurationProperties({DruidStatProperties.class,DruidClientConnectionProperties.class})
public class DruidMonitorStatViewServletConfiguration extends DruidStatViewServletConfiguration {

    @Bean("monitorStatViewServlet")
    @ConditionalOnMissingBean(name = {"monitorStatViewServlet"})
    public ServletRegistrationBean statViewServletRegistrationBean(DruidStatProperties properties,DruidClientConnectionProperties clientConnectionProperties) {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean();
        MonitorStatViewServlet monitorStatViewServlet = new MonitorStatViewServlet();
        monitorStatViewServlet.setConnectionPropertiesMap(clientConnectionProperties.getProperties());

        registrationBean.setServlet(monitorStatViewServlet);

        super.configStatViewServlet(registrationBean,properties);
        return registrationBean;
    }
}
