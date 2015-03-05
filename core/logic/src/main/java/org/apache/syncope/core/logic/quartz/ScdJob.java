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

import java.io.Serializable;
import java.util.Date;

public class ScdJob implements Serializable {

    private static final long serialVersionUID = 2476938795547653558L;

    private final String jobName;

    private TriggerState state;

    private String cronExpr;

    private Date startTime;

    private Date endTime;

    private Date nextFireTime;

    private Date previousFireTime;

    private String description;

    public TriggerState getState() {
        return state;
    }

    public void setState(final TriggerState state) {
        this.state = state;
    }

    public void setCronExpr(final String cronExpr) {
        this.cronExpr = cronExpr;
    }

    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(final Date endTime) {
        this.endTime = endTime;
    }

    public void setNextFireTime(final Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public void setPreviousFireTime(final Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    public ScdJob(final String jobName) {
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }

    public String getCronExpr() {
        return cronExpr;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum TriggerState {

        NONE("no state"),
        NORMAL("state normal"),
        PAUSED("state paused"),
        COMPLETE("state complete"),
        ERROR("state error"),
        BLOCKED("state blocked");

        private final String state;

        private TriggerState(final String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }
    }
}
