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

import com.alibaba.druid.support.http.remote.JmxConnectionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018.3.20 14:29
 */
@ConfigurationProperties("spring.datasource.druid.multiConnection")
public class DruidMultiConnectionProperties {

    // <Remote module name, Jmx connection property>
    private Map<String,JmxConnectionProperties> properties;

    public Map<String, JmxConnectionProperties> getMultiConnection() {
        return properties;
    }

    public void setMultiConnection(Map<String, JmxConnectionProperties> multiConnection) {
        this.properties = multiConnection;
    }

    public Map<String, JmxConnectionProperties> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, JmxConnectionProperties> properties) {
        this.properties = properties;
    }
}