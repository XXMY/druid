/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
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
package com.alibaba.druid.stat;

import com.alibaba.druid.support.http.remote.ConnectionProperties;
import com.alibaba.druid.support.http.remote.condition.MonitorCondition;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.StringUtils;
import org.springframework.context.annotation.Conditional;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.rmi.RMIConnector;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 通过JMX暴露此类，供客户端自动报告其连接属性
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018年3月26日 14点21分
 */
@Conditional(value = MonitorCondition.class)
public class ClientConnectionHolder implements ClientConnectionHolderMBean{
    private final static Log LOG = LogFactory.getLog(ClientConnectionHolder.class);

    public final static String MBEAN_NAME = "com.alibaba.druid:type=ClientConnectionHolder";
    public final static String MBEAN_METHOD = "put";

    private Map<String,ConnectionProperties> clientConnectionProperties;
    private Map<String,JMXConnector> clientConnectors;
    private int expireSeconds;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private Map<String,Future> scheduledFutures = new HashMap<String, Future>();

    public ClientConnectionHolder(Map<String,ConnectionProperties> clientConnectionProperties,Map<String,JMXConnector> clientConnectors,int expireSeconds){
        this.registerMBean();
        this.clientConnectionProperties = clientConnectionProperties;
        this.clientConnectors = clientConnectors;

        if(expireSeconds > 0)
            this.expireSeconds = expireSeconds;
        else
            this.expireSeconds = 300;

    }

    @Override
    public synchronized boolean put(final String clientName, final ConnectionProperties connectionProperties){
        if(StringUtils.isEmpty(clientName) || connectionProperties == null)
            return false;
        if(LOG.isDebugEnabled()){
            LOG.debug(String.format("Received clientName: %s with properties: %s",clientName,connectionProperties.toString()));
        }

        this.clientConnectionProperties.put(clientName,connectionProperties);

        if(LOG.isDebugEnabled()){
            LOG.debug(String.format("Size of clientConnectionProperties is %d, and contains %s",this.clientConnectionProperties.size(),this.clientConnectionProperties.keySet()));
        }

        cancelTask(clientName);

        newExpireTask(clientName);

        if(LOG.isDebugEnabled()){
            LOG.debug(String.format("Size of scheduledFutures is %d, and contains %s",this.scheduledFutures.size(),this.scheduledFutures.keySet()));
        }
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

    private void closeJmxConnection(String clientName){
        try{
            RMIConnector connector = (RMIConnector) clientConnectors.get(clientName);
            if(connector != null)
                connector.close();
        }catch (Exception e){
            LOG.error(String.format("Close connection of %s occurred an exception, %s",clientName,e.getMessage()));
        }

        clientConnectors.remove(clientName);
    }

    private void cancelTask(String clientName){
        // 若有对应的任务则先取消任务
        if(scheduledFutures.containsKey(clientName)){
            if(LOG.isDebugEnabled()){
                LOG.debug(String.format("Preparing cancel %s and remove it from scheduledFutures.",clientName));
            }
            closeJmxConnection(clientName);
            scheduledFutures.get(clientName).cancel(false);
            scheduledFutures.remove(clientName);
        }
    }

    private void newExpireTask(final String clientName){
        // 向任务池中添加任务
        ScheduledFuture future = executorService.schedule(new Runnable() {
            @Override
            public void run() {
                if(LOG.isDebugEnabled()){
                    LOG.debug(String.format("Auto remove %s from clientConnectionProperties and scheduledFutures.",clientName));
                }

                closeJmxConnection(clientName);

                clientConnectionProperties.remove(clientName);
                scheduledFutures.remove(clientName);

            }
        },this.expireSeconds, TimeUnit.SECONDS);

        scheduledFutures.put(clientName,future);
    }
}
