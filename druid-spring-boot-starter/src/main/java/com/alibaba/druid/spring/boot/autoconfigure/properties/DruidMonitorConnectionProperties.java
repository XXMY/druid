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

import com.alibaba.druid.support.http.remote.MonitorConnectionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Used by druid client to specify monitor properties.
 *
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018年3月26日 13点52分
 */
@ConfigurationProperties("spring.datasource.druid.monitor.properties")
public class DruidMonitorConnectionProperties extends MonitorConnectionProperties{
}