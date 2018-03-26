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

/**
 * 客户端使用的配置监控端的连接属性
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018年3月26日 14点20分
 */
public class MonitorConnectionProperties extends ConnectionProperties {

    private boolean autoReport;
    private String myJmxUrl;
    private String myUsername;
    private String myPassword;
    private String cron;

    public boolean isAutoReport() {
        return autoReport;
    }

    public void setAutoReport(boolean autoReport) {
        this.autoReport = autoReport;
    }

    public String getMyJmxUrl() {
        return myJmxUrl;
    }

    public void setMyJmxUrl(String myJmxUrl) {
        this.myJmxUrl = myJmxUrl;
    }

    public String getMyUsername() {
        return myUsername;
    }

    public void setMyUsername(String myUsername) {
        this.myUsername = myUsername;
    }

    public String getMyPassword() {
        return myPassword;
    }

    public void setMyPassword(String myPassword) {
        this.myPassword = myPassword;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
