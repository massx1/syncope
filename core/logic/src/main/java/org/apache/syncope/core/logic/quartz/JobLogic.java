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
package org.apache.syncope.core.logic.quartz;

import java.util.List;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.JobTO;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.syncope.core.persistence.api.dao.TaskDAO;
import org.apache.syncope.core.persistence.api.entity.task.Task;
import org.apache.syncope.core.provisioning.api.job.JobNamer;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobLogic {

    private static final Logger LOG = LoggerFactory.getLogger(JobLogic.class);

    @Autowired
    private QuartzClient client;

    @Autowired
    private TaskDAO taskDAO;

    public List<String> listAllJob() {
        List<String> list;
        try {
            list = client.jobsMap();
        } catch (final SchedulerException ex) {
            LOG.error("Impossible to list all Job");
            final SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Unknown);
            sce.getElements().add(ex.getMessage());
            throw sce;
        }
        return list;
    }

    public void pause(final Long jobKey) {
        LOG.debug("Pause task key {}", jobKey);

        final Task task = taskDAO.find(jobKey);

        try {
            client.checkIfJobExist(JobNamer.getJobName(task));
            LOG.debug("Job exists");
        } catch (final SchedulerException e) {
            LOG.error("Job does not exists");
            final SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.NotFound);
            sce.getElements().add("Job does not exists");
            throw sce;
        }

        final String triggerName = JobNamer.getTriggerName(JobNamer.getJobName(task));

        try {
            LOG.debug("Pause trigger {}", triggerName);
            client.pauseTrigger(triggerName);
        } catch (SchedulerException ex) {
            LOG.error("Pause job error", ex);
            final SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
            sce.getElements().add(ex.getMessage());
            throw sce;
        }
    }

    public void resume(final Long jobKey) {
        LOG.debug("Pause task key {}", jobKey);

        final Task task = taskDAO.find(jobKey);

        try {
            client.checkIfJobExist(JobNamer.getJobName(task));
            LOG.debug("Job exists");
        } catch (final SchedulerException e) {
            LOG.error("Job does not exists");
            final SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.NotFound);
            sce.getElements().add("Job does not exists");
            throw sce;
        }

        final String triggerName = JobNamer.getTriggerName(JobNamer.getJobName(task));

        try {
            LOG.debug("Pause trigger {}", triggerName);
            client.resumeTrigger(triggerName);
        } catch (final SchedulerException ex) {
            LOG.error("Pause job error", ex);
            final SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
            sce.getElements().add(ex.getMessage());
            throw sce;
        }
    }

    public JobTO status(final Long jobKey) {
        LOG.info("Status task key {}", jobKey);

        final Task task = taskDAO.find(jobKey);
        try {
            client.checkIfJobExist(JobNamer.getJobName(task));
            LOG.debug("Job exists");
        } catch (final SchedulerException e) {
            LOG.error("Job does not exists");
            final SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.NotFound);
            sce.getElements().add("Job does not exists");
            throw sce;
        }

        final String triggerName = JobNamer.getTriggerName(JobNamer.getJobName(task));
        System.out.println(">>>>> JOB NAME: " + JobNamer.getJobName(task));
        System.out.println(">>>>> TRIGGER NAME: " + triggerName);
        String triggerStatus;
        try {
            client.jobsTrigger(JobNamer.getJobName(task));
            LOG.debug("Pause trigger {}", triggerName);
            triggerStatus = client.triggerStatus(triggerName);
        } catch (final SchedulerException ex) {
            LOG.error("Pause job error", ex);
            final SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.Scheduling);
            sce.getElements().add(ex.getMessage());
            throw sce;
        }

        final JobTO jobTO = new JobTO(jobKey, JobNamer.getJobName(task), triggerName, triggerStatus);

        return jobTO;
    }

}
