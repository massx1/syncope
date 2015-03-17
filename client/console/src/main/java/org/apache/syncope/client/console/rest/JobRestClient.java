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
package org.apache.syncope.client.console.rest;

import org.apache.syncope.common.lib.to.JobTO;
import org.apache.syncope.common.rest.api.service.JobService;
import org.springframework.stereotype.Component;

@Component
public class JobRestClient extends BaseRestClient {

    private static final long serialVersionUID = -2390607159429227214L;

    public void stop(final Long id) {
        getService(JobService.class).stop(id);
    }
    
    public void pause(final Long id) {
        getService(JobService.class).pause(id);
    }
    
    public void resume(final Long id) {
        getService(JobService.class).resume(id);
    }
    
    public JobTO status(final Long id) {
        return getService(JobService.class).status(id);
    }

}
