/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.spring.boot.autoconfigure.stat;

import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidClientConnectionProperties;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidMonitorConnectionProperties;
import com.alibaba.druid.stat.ClientConnectionHolder;
import com.alibaba.druid.support.http.remote.ClientAutoReport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018年3月23日 16点45分
 */
@Configuration
@EnableConfigurationProperties({DruidClientConnectionProperties.class})
public class DruidRemoteConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.druid.monitor.properties", name = "jmxUrl")
    @ConditionalOnMissingBean
    public DruidMonitorConnectionProperties monitorConnectionProperties(){
        return new DruidMonitorConnectionProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.druid.monitor.properties", name = "auto-report", havingValue = "true")
    @ConditionalOnMissingBean
    public ClientAutoReport clientAutoReport(DruidMonitorConnectionProperties connectionProperties){
        return new ClientAutoReport(connectionProperties);
    }

    @Bean
    @ConditionalOnBean(name = {"monitorStatViewServlet"})
    //@Conditional(MonitorCondition.class)
    @ConditionalOnMissingBean
    public ClientConnectionHolder clientConnectionHolder(DruidClientConnectionProperties clientConnectionProperties){
        return new ClientConnectionHolder(clientConnectionProperties.getProperties());
    }

}
