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
package org.apache.syncope.common.lib.to;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.syncope.common.lib.AbstractBaseBean;

@XmlRootElement(name = "job")
@XmlType
public class JobTO extends AbstractBaseBean {

    private static final long serialVersionUID = -7254450981751326711L;

    private Long taskKey;

    private String jobName;

    private String triggerName;

    private String triggerStatus;

    public JobTO() {
    }

    public JobTO(final Long taskKey, final String jobName, final String triggerName, final String triggerStatus) {
        this.taskKey = taskKey;
        this.jobName = jobName;
        this.triggerName = triggerName;
        this.triggerStatus = triggerStatus;
    }

    public Long getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(final Long taskKey) {
        this.taskKey = taskKey;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(final String jobName) {
        this.jobName = jobName;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(final String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerStatus() {
        return triggerStatus;
    }

    public void setTriggerStatus(final String triggerStatus) {
        this.triggerStatus = triggerStatus;
    }

    @Override
    public String toString() {
        return "JobTO{" + "taskKey=" + taskKey + ", jobName=" + jobName + ", triggerName=" + triggerName
                + ", triggerStatus=" + triggerStatus + '}';
    }
}
