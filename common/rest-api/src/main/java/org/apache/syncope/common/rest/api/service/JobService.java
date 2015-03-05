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
package org.apache.syncope.common.rest.api.service;


import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("jobs")
public interface JobService extends JAXRSService {

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    List<String> getJobs();

    @POST
    @Path("{jobKey}/pause")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    void pause(@NotNull @PathParam("jobKey") Long jobKey);

    @POST
    @Path("{jobKey}/resume")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    void resume(@NotNull @PathParam("jobKey") Long jobKey);

    @POST
    @Path("{jobKey}/stop")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    void stop(@NotNull @PathParam("jobKey") Long jobKey);

    @GET
    @Path("status/{jobKey}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    String status(@NotNull @PathParam("jobKey") Long jobKey);

}
