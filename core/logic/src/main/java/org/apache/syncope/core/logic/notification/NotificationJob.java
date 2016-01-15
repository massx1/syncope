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
package org.apache.syncope.core.logic.notification;

import org.apache.syncope.core.misc.security.AuthContextUtils;
import org.apache.syncope.core.persistence.api.DomainsHolder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Periodically checks for notification to send.
 *
 * @see org.apache.syncope.core.persistence.api.entity.task.NotificationTask
 */
@Component
@DisallowConcurrentExecution
public class NotificationJob implements Job {

    public enum Status {

        SENT,
        NOT_SENT

    }

    public static final String DEFAULT_CRON_EXP = "0 0/5 * * * ?";

    private static final Logger LOG = LoggerFactory.getLogger(NotificationJob.class);

    @Autowired
    private DomainsHolder domainsHolder;

    @Autowired
    private NotificationJobDelegate delegate;

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        LOG.debug("Waking up...");

        for (String domain : domainsHolder.getDomains().keySet()) {
            try {
                AuthContextUtils.execWithAuthContext(domain, new AuthContextUtils.Executable<Void>() {

                    @Override
                    public Void exec() {
                        try {
                            delegate.execute();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        return null;
                    }
                });
            } catch (RuntimeException e) {
                throw new JobExecutionException(e.getCause());
            }
        }

        LOG.debug("Sleeping again...");
    }

}
