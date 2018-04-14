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
package com.alibaba.druid.support.http.remote;

import com.alibaba.druid.stat.ClientConnectionHolder;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 自动向监控服务器Monitor报告自己的 IP 和 JMX 端口
 * 只有当 ClientStatViewServlet bean 存在时才创建该bean
 *
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018年3月23日 16点45分
 */
public class ClientAutoReport {
    private final static Log LOG = LogFactory.getLog(ClientAutoReport.class);

    private MonitorConnectionProperties connectionProperties;

    private JMXConnector jmxConnector;

    @Value("${spring.application.name}")
    private String applicationName = UUID.randomUUID().toString();

    public ClientAutoReport(MonitorConnectionProperties connectionProperties){
        this.connectionProperties = connectionProperties;
    }

    @Scheduled(cron = "${spring.datasource.druid.monitor.properties.cron}")
    public void doReport(){
        if(this.initJmxConnection() == null)
            return ;

        this.reportToMonitor();
    }

    /**
     * 向远程监控发送信息
     */
    private void reportToMonitor(){
        try{
            if(LOG.isDebugEnabled()){
                String logString = "Preparing report to monitor[%s] with properties[jmxUrl=%s, username=%s, password=%s]";
                LOG.debug(String.format(logString,
                        this.connectionProperties.getJmxUrl(),
                        this.connectionProperties.getMyJmxUrl(),
                        this.connectionProperties.getMyUsername(),
                        this.connectionProperties.getMyPassword()
                ));
            }

            ConnectionProperties connectionProperties = new ConnectionProperties();
            connectionProperties.setJmxUrl(this.connectionProperties.getMyJmxUrl());
            connectionProperties.setUsername(this.connectionProperties.getMyUsername());
            connectionProperties.setPassword(this.connectionProperties.getMyPassword());

            ObjectName objectName = new ObjectName(ClientConnectionHolder.MBEAN_NAME);
            MBeanServerConnection connection = this.initJmxConnection();
            Boolean result = (Boolean)connection.invoke(objectName, ClientConnectionHolder.MBEAN_METHOD,
                    new Object[] { this.applicationName, connectionProperties },
                    new String[] { String.class.getName(), ConnectionProperties.class.getName() });

            LOG.info(String.format("Report to monitor result: %s",result));

        }catch (Exception e){
            LOG.error(e.getMessage(),e);
            try {
                this.jmxConnector.close();
            } catch (IOException e1) {
                LOG.error(e1.getMessage(),e1);
            }
            this.jmxConnector = null;
        }
    }

    /**
     * 初始化远程JMX连接
     * @return Boolean
     */
    private MBeanServerConnection initJmxConnection(){
        try {
            if(this.jmxConnector != null){
                if(LOG.isDebugEnabled())
                    LOG.debug("Connected to monitor with jmx: " + this.connectionProperties.getJmxUrl());

                return this.jmxConnector.getMBeanServerConnection();
            }

            if(StringUtils.isEmpty(this.connectionProperties.getJmxUrl())){
                LOG.warn("Remote monitor jmx url is empty");
                return null;
            }


            JMXServiceURL url = new JMXServiceURL(this.connectionProperties.getJmxUrl());
            Map<String, String[]> env = null;
            if (this.connectionProperties.getUsername() != null) {
                env = new HashMap<String, String[]>();
                String[] credentials = new String[] { this.connectionProperties.getUsername(), this.connectionProperties.getPassword()};
                env.put(JMXConnector.CREDENTIALS, credentials);
            }
            this.jmxConnector = JMXConnectorFactory.connect(url, env);
            return this.jmxConnector.getMBeanServerConnection();

        } catch (IOException e) {
            LOG.error("init jmx connection error", e);
        }

        return null;

    }

}
