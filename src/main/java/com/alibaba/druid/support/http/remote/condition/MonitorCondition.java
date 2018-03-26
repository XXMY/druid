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
package com.alibaba.druid.support.http.remote.condition;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.remote.MonitorStatViewServlet;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * 判断是否为监控端的条件
 * @author Fangwei Cai[cfw892@gmail.com]
 * @since 2018年3月26日 14点25分
 */
public class MonitorCondition implements Condition {
    private final static Log LOG = LogFactory.getLog(StatViewServlet.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try{
            Map<String,MonitorStatViewServlet> beansOfType = context.getBeanFactory().getBeansOfType(MonitorStatViewServlet.class);
            return beansOfType != null && beansOfType.size() > 0 ;
        }catch (Exception e){
            LOG.error(e.getMessage(),e);
        }

        return false;
    }
}
