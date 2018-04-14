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
package com.alibaba.druid.spring.boot.autoconfigure.properties;

import com.alibaba.druid.support.http.remote.ConnectionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.management.remote.JMXConnector;
import java.util.HashMap;
import java.util.Map;

/**
 * Used by druid monitor to specify client properties.
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018.3.20 14:29
 */
@ConfigurationProperties("spring.datasource.druid.client")
public class DruidClientConnectionProperties {

    // <Remote module name, Jmx connection property>
    private Map<String,ConnectionProperties> properties = new HashMap<>();

    private Map<String,JMXConnector> jmxConnectorMap = new HashMap<>();

    public Map<String, ConnectionProperties> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, ConnectionProperties> properties) {
        this.properties = properties;
    }

    public Map<String, JMXConnector> getJmxConnectorMap() {
        return jmxConnectorMap;
    }
}