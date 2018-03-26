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
package com.alibaba.druid.support.http.remote;

import com.alibaba.druid.support.http.remote.condition.MonitorCondition;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * 通过JMX暴露此类，供客户端自动报告其连接属性
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018年3月26日 14点21分
 */
@Conditional(value = MonitorCondition.class)
public class ClientConnectionHolder {
    private final static Log LOG = LogFactory.getLog(ClientConnectionHolder.class);

    public final static String MBEAN_NAME = "com.alibaba.druid:type=ClientConnectionHolder";
    public final static String MBEAN_METHOD = "put";

    private Map<String,ConnectionProperties> clientConnectionProperties;

    @Autowired
    public ClientConnectionHolder(Map<String,ConnectionProperties> clientConnectionProperties){
        this.registerMBean();
        this.clientConnectionProperties = clientConnectionProperties;
    }

    public synchronized boolean put(String clientName, ConnectionProperties connectionProperties){
        if(StringUtils.isEmpty(clientName) || connectionProperties == null)
            return false;

        this.clientConnectionProperties.put(clientName,connectionProperties);

        return true;
    }

    private void registerMBean() {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {

            ObjectName objectName = new ObjectName(MBEAN_NAME);
            if (!mbeanServer.isRegistered(objectName)) {
                mbeanServer.registerMBean(this, objectName);
            }
        } catch (JMException ex) {
            LOG.error("register mbean error", ex);
        }
    }



}
