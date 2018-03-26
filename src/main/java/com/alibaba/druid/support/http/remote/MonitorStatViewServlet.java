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

import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.dubbo.common.utils.StringUtils;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 多服务端状态监控-监控端Servlet，通过JMX方式进行监控
 *
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018年3月19日 10点31分
 */
public class MonitorStatViewServlet extends StatViewServlet {
    private final static Log LOG                     = LogFactory.getLog(StatViewServlet.class);

    private Map<String,ConnectionProperties> connectionPropertiesMap;

    private Map<String,MBeanServerConnection> managedBeanServerConnectionMap;

    public Map<String,ConnectionProperties> getConnectionPropertiesMap() {
        return connectionPropertiesMap;
    }

    public void setConnectionPropertiesMap(Map<String,ConnectionProperties> connectionPropertiesMap) {
        this.connectionPropertiesMap = connectionPropertiesMap;
    }

    @Override
    public void init() throws ServletException {
        super.init();

        managedBeanServerConnectionMap = new HashMap<String, MBeanServerConnection>();
        // 获取jmx的连接配置信息
        if (this.connectionPropertiesMap != null) {
            Set<String> remoteNameSet = connectionPropertiesMap.keySet();
            for(String remoteName : remoteNameSet){
                this.managedBeanServerConnectionMap.put(remoteName,this.initJmxConn(remoteName,this.connectionPropertiesMap.get(remoteName)));
            }
        }

    }

    /**
     * 根据 url 中携带 remote 的参数判断直接调用本地服务还是远程服务，remote 参数对应于 managedBeanServerConnectionMap 中的 key。
     * 在进行jmx通信，首先判断一下jmx连接是否已经建立成功，如果已经建立成功，则直接进行通信，如果之前没有成功建立，则会尝试重新建立一遍。
     *
     * @param fullUrl 要连接的服务地址,带参数
     * @return 调用服务后返回的json字符串
     */
    @Override
    protected String process(String fullUrl) {
        String response = null;
        Map<String,String> parameters = this.parseParameter(fullUrl);

        String url = this.urlWithOutRemote(fullUrl,parameters);

        if(parameters.containsKey("remote")){
            // 处理远程服务调用操作
            response = this.processRemote(parameters.get("remote"),url);
        }else
            response = this.processLocal(url);

        return response;
    }

    private String processLocal(String fullUrl){
        if (fullUrl.equals("/remote-info.json")) {
            Set<String> remoteNameSet = this.connectionPropertiesMap.keySet();

            return DruidStatService.returnJSONResult(DruidStatService.RESULT_CODE_SUCCESS, remoteNameSet);
        }
        else
            return super.process(fullUrl);
    }

    private String processRemote(String remoteName,String fullUrl){
        MBeanServerConnection connection = this.managedBeanServerConnectionMap.get(remoteName);

        if(connection == null){
            connection = this.initJmxConn(remoteName,this.connectionPropertiesMap.get(remoteName));
            if(connection == null)
                return DruidStatService.returnJSONResult(
                        DruidStatService.RESULT_CODE_ERROR,
                        "Cannot connect to remote service, initializing jmx connection failed."
                        );
        }

        // JMX 连接建立成功
        try {
            return super.getJmxResult(connection,fullUrl);
        } catch (Exception e) {
            LOG.error("get jmx data error", e);
            return DruidStatService.returnJSONResult(DruidStatService.RESULT_CODE_ERROR,
                    "get data error" + e.getMessage());
        }
    }

    private String urlWithOutRemote(String fullUrl,Map<String,String> parameters){
        if(parameters.size() == 0)
            return fullUrl;

        String[] urlSplittedArray = fullUrl.split("\\?");
        String path = urlSplittedArray[0];

        Set<String> paramNameSet = parameters.keySet();
        String queryString = "";
        for(String paramName : paramNameSet){
            if(paramName.equalsIgnoreCase("remote"))
                continue;

            queryString += paramName + "=" + parameters.get(paramName) + "&";
        }

        if(StringUtils.isEmpty(queryString))
            return path;
        else
            return path + "?" + queryString;

    }

    /**
     * 解析 URL 参数
     * @param fullUrl 带参数的请求地址
     * @return
     */
    private Map<String,String> parseParameter(String fullUrl){
        Map<String,String> parameters = new HashMap<String, String>();

        String[] urlSplittedArray = fullUrl.split("\\?");
        if(urlSplittedArray.length != 2)
            return parameters;

        String parameterString = urlSplittedArray[1];
        String[] keyValueArray = parameterString.split("&");
        for(String keyValueString : keyValueArray){
            if(StringUtils.isEmpty(keyValueString) || !keyValueString.contains("="))
                continue;
            String[] keyValue = keyValueString.split("=");
            if(keyValue.length != 2)
                continue;
            parameters.put(keyValue[0],keyValue[1]);
        }

        return parameters;

    }

    /**
     * 初始化jmx连接
     */
    private MBeanServerConnection initJmxConn(String remoteName,ConnectionProperties properties){
        try {
            if (properties.getJmxUrl() != null) {
                JMXServiceURL url = new JMXServiceURL(properties.getJmxUrl());
                Map<String, String[]> env = null;
                if (properties.getUsername() != null) {
                    env = new HashMap<String, String[]>();
                    String[] credentials = new String[] { properties.getUsername(), properties.getPassword()};
                    env.put(JMXConnector.CREDENTIALS, credentials);
                }
                JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
                return jmxc.getMBeanServerConnection();
            }
        } catch (IOException e) {
            LOG.error("init jmx connection error", e);
        }
        return null;
    }
}
