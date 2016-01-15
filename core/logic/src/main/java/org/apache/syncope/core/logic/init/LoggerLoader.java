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
package org.apache.syncope.core.logic.init;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.db.jdbc.ColumnConfig;
import org.apache.logging.log4j.core.appender.db.jdbc.ConnectionSource;
import org.apache.logging.log4j.core.appender.db.jdbc.JdbcAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.syncope.core.misc.AuditManager;
import org.apache.syncope.core.misc.security.AuthContextUtils;
import org.apache.syncope.core.persistence.api.DomainsHolder;
import org.apache.syncope.core.persistence.api.SyncopeLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

@Component
public class LoggerLoader implements SyncopeLoader {

    @Autowired
    private DomainsHolder domainsHolder;

    @Autowired
    private LoggerAccessor loggerAccessor;

    @Override
    public Integer getPriority() {
        return 300;
    }

    @Override
    public void load() {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

        // Audit table and DataSource for each configured domain
        ColumnConfig[] columns = {
            ColumnConfig.createColumnConfig(ctx.getConfiguration(), "EVENT_DATE", null, null, "true", null, null),
            ColumnConfig.createColumnConfig(ctx.getConfiguration(), "LOGGER_LEVEL", "%level", null, null, null, null),
            ColumnConfig.createColumnConfig(ctx.getConfiguration(), "LOGGER", "%logger", null, null, null, null),
            ColumnConfig.createColumnConfig(ctx.getConfiguration(), "MESSAGE", "%message", null, null, null, null),
            ColumnConfig.createColumnConfig(ctx.getConfiguration(), "THROWABLE", "%ex{full}", null, null, null, null)
        };
        for (Map.Entry<String, DataSource> entry : domainsHolder.getDomains().entrySet()) {
            Appender appender = ctx.getConfiguration().getAppender("audit_for_" + entry.getKey());
            if (appender == null) {
                appender = JdbcAppender.createAppender(
                        "audit_for_" + entry.getKey(),
                        "false",
                        null,
                        new DataSourceConnectionSource(entry.getValue()),
                        "0",
                        "SYNCOPEAUDIT",
                        columns);
                appender.start();
                ctx.getConfiguration().addAppender(appender);
            }

            LoggerConfig logConf = new LoggerConfig(AuditManager.getDomainAuditLoggerName(entry.getKey()), null, false);
            logConf.addAppender(appender, Level.DEBUG, null);
            ctx.getConfiguration().addLogger(AuditManager.getDomainAuditLoggerName(entry.getKey()), logConf);

            AuthContextUtils.execWithAuthContext(entry.getKey(), new AuthContextUtils.Executable<Void>() {

                @Override
                public Void exec() {
                    loggerAccessor.synchronizeLog4J(ctx);
                    return null;
                }
            });
        }

        ctx.updateLoggers();
    }

    private static class DataSourceConnectionSource implements ConnectionSource {

        private final DataSource dataSource;

        DataSourceConnectionSource(final DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return DataSourceUtils.getConnection(dataSource);
        }

    }
}
