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

import java.util.ArrayList;
import java.util.List;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QuartzClient {

    @Autowired
    private Scheduler scheduler;

    public List<String> jobsMap() throws SchedulerException {
        final List<String> jobsName = new ArrayList<>();
        for (final String groupName : jobGroups()) {
            for (final JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                jobsName.add(jobKey.getName());
            }
        }
        return jobsName;
    }

    private List<String> jobGroups() throws SchedulerException {
        return scheduler.getJobGroupNames();
    }

    public boolean checkIfJobExist(final String jobName) throws SchedulerException {
        return scheduler.checkExists(JobKey.jobKey(jobName));
    }

    public void pauseTrigger(final String triggerName) throws SchedulerException {
        scheduler.pauseTrigger(TriggerKey.triggerKey(triggerName));
    }

    public void resumeTrigger(final String triggerName) throws SchedulerException {
        scheduler.resumeTrigger(TriggerKey.triggerKey(triggerName));
    }

    public void pauseJob(final String jobName) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(jobName));
    }

    public void resumeJob(final JobKey jobKey) throws SchedulerException {
        scheduler.resumeJob(jobKey);
    }

    public JobDetail jobDetail(final JobKey jobKey) throws SchedulerException {
        return scheduler.getJobDetail(jobKey);
    }

    public String triggerStatus(final String triggerName) throws SchedulerException {
        System.out.println("> > > > > > " + triggerName);
        System.out.println("> > > > > > " + TriggerKey.triggerKey(triggerName));
        return scheduler.getTriggerState(TriggerKey.triggerKey(triggerName)).toString();
    }

    public List<? extends Trigger> jobsTrigger(final String jobName) throws SchedulerException {
        return scheduler.getTriggersOfJob(JobKey.jobKey(jobName));
    }
//    public TriggerKey triggerName(final JobKey jobKey) throws SchedulerException {
//        final List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
//        return triggersOfJob.get(0).getKey(); 
//    }
//
//    public CronTrigger trigger(final TriggerKey triggerKey) throws SchedulerException {
//        return (CronTrigger) scheduler.getTrigger(triggerKey);
//    }

//    public void pauseAll() throws SchedulerException {
//        scheduler.pauseAll();
//    }
//    
//    public void startAll() throws SchedulerException{
//        scheduler.resumeAll();
//    }
//
//
//    public void start(final TriggerKey triggerKey) throws SchedulerException {
//        scheduler.resumeTrigger(triggerKey);
//    }
//
//    public void unscheduleJob(final TriggerKey triggerKey) throws SchedulerException {
//        scheduler.unscheduleJob(triggerKey);
//    }
//
//    
//    public void updateCronTrigger(final JobDetail jobDetail, final CronTrigger updateCronTrigger) throws SchedulerException {
//        scheduler.scheduleJob(jobDetail, updateCronTrigger);
//
//    }
//    
//
//    public boolean deleteJob(JobKey jobKey) throws SchedulerException {
//       return scheduler.deleteJob(jobKey);
//    }
//    
//    public Date scheduleNewJob(final JobDetail job, final Trigger trigger) throws SchedulerException {
//        return scheduler.scheduleJob(job, trigger);
//    }
//    
//    public void executeJob(final JobKey jobKey) throws SchedulerException {
//        scheduler.triggerJob(jobKey);
//    }
}
