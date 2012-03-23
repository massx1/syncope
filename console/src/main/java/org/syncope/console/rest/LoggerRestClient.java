/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.syncope.console.rest;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.syncope.client.to.LoggerTO;
import org.syncope.types.AuditElements;
import org.syncope.types.AuditElements.Category;
import org.syncope.types.AuditLoggerName;
import org.syncope.types.SyncopeLoggerLevel;

@Component
public class LoggerRestClient extends AbstractBaseRestClient {

    public List<LoggerTO> listLogs() {
        return Arrays.asList(restTemplate.getForObject(baseURL + "logger/log/list", LoggerTO[].class));
    }

    public List<AuditLoggerName> listAudits() {
        return Arrays.asList(restTemplate.getForObject(baseURL + "logger/audit/list", AuditLoggerName[].class));
    }

    public Map<AuditElements.Category, Set<AuditLoggerName>> listAuditsByCategory() {
        Map<Category, Set<AuditLoggerName>> result = new EnumMap<Category, Set<AuditLoggerName>>(Category.class);
        for (AuditLoggerName auditLoggerName : listAudits()) {
            if (!result.containsKey(auditLoggerName.getCategory())) {
                result.put(auditLoggerName.getCategory(), new HashSet<AuditLoggerName>());
            }

            result.get(auditLoggerName.getCategory()).add(auditLoggerName);
        }

        return result;
    }

    public void setLogLevel(final String name, final SyncopeLoggerLevel level) {
        restTemplate.postForObject(baseURL + "logger/log/{name}/{level}", null, LoggerTO.class, name, level);
    }

    public void enableAudit(final AuditLoggerName auditLoggerName) {
        restTemplate.put(baseURL + "logger/audit/enable", auditLoggerName);
    }

    public void deleteLog(final String name) {
        restTemplate.delete(baseURL + "logger/log/delete/{name}", name);
    }

    public void disableAudit(final AuditLoggerName auditLoggerName) {
        restTemplate.put(baseURL + "logger/audit/disable", auditLoggerName);
    }
}