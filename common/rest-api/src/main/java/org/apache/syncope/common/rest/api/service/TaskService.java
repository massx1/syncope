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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.cxf.jaxrs.model.wadl.DocTarget;
import org.apache.syncope.common.lib.to.AbstractTaskTO;
import org.apache.syncope.common.lib.to.BulkAction;
import org.apache.syncope.common.lib.to.BulkActionResult;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.to.ReportExecTO;
import org.apache.syncope.common.lib.to.SchedTaskTO;
import org.apache.syncope.common.lib.to.TaskExecTO;
import org.apache.syncope.common.lib.types.TaskType;

/**
 * REST operations for tasks.
 */
@Path("tasks")
public interface TaskService extends JAXRSService {

    /**
     * Returns the task matching the given key.
     *
     * @param taskKey key of task to be read
     * @param <T> type of taskTO
     * @return task with matching id
     */
    @GET
    @Path("{taskKey}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    <T extends AbstractTaskTO> T read(@NotNull @PathParam("taskKey") Long taskKey);

    /**
     * Returns the task execution with the given id.
     *
     * @param executionKey key of task execution to be read
     * @return task execution with matching Id
     */
    @GET
    @Path("executions/{executionKey}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    TaskExecTO readExecution(@NotNull @PathParam("executionKey") Long executionKey);

    /**
     * Returns a list of tasks with matching type.
     *
     * @param taskType type of tasks to be listed
     * @param <T> type of taskTO
     * @return list of tasks with matching type
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    <T extends AbstractTaskTO> PagedResult<T> list(@NotNull @MatrixParam("type") TaskType taskType);

    /**
     * Returns a list of tasks with matching type.
     *
     * @param taskType type of tasks to be listed
     * @param orderBy list of ordering clauses, separated by comma
     * @param <T> type of taskTO
     * @return list of tasks with matching type
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    <T extends AbstractTaskTO> PagedResult<T> list(@NotNull @MatrixParam("type") TaskType taskType,
            @QueryParam(PARAM_ORDERBY) String orderBy);

    /**
     * Returns a paged list of existing tasks matching type and page/size conditions.
     *
     * @param taskType type of tasks to be listed
     * @param page page number of tasks in relation to page size
     * @param size number of tasks listed per page
     * @param orderBy list of ordering clauses, separated by comma
     * @param <T> type of taskTO
     * @return paged list of existing tasks matching type and page/size conditions
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    <T extends AbstractTaskTO> PagedResult<T> list(@NotNull @MatrixParam("type") TaskType taskType,
            @NotNull @Min(1) @QueryParam(PARAM_PAGE) @DefaultValue(DEFAULT_PARAM_PAGE) Integer page,
            @NotNull @Min(1) @QueryParam(PARAM_SIZE) @DefaultValue(DEFAULT_PARAM_SIZE) Integer size,
            @QueryParam(PARAM_ORDERBY) String orderBy);

    /**
     * Returns a paged list of existing tasks matching type and page/size conditions.
     *
     * @param taskType type of tasks to be listed
     * @param page page number of tasks in relation to page size
     * @param size number of tasks listed per page
     * @param <T> type of taskTO
     * @return paged list of existing tasks matching type and page/size conditions
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    <T extends AbstractTaskTO> PagedResult<T> list(@MatrixParam("type") TaskType taskType,
            @NotNull @Min(1) @QueryParam(PARAM_PAGE) @DefaultValue(DEFAULT_PARAM_PAGE) Integer page,
            @NotNull @Min(1) @QueryParam(PARAM_SIZE) @DefaultValue(DEFAULT_PARAM_SIZE) Integer size);

    /**
     * Creates a new task.
     *
     * @param taskTO task to be created
     * @param <T> type of taskTO
     * @return <tt>Response</tt> object featuring <tt>Location</tt> header of created task
     */
    @Descriptions({
        @Description(target = DocTarget.RESPONSE, value = "Featuring <tt>Location</tt> header of created task")
    })
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    <T extends SchedTaskTO> Response create(@NotNull T taskTO);

    /**
     * Updates the task matching the provided key.
     *
     * @param taskKey key of task to be updated
     * @param taskTO updated task to be stored
     */
    @PUT
    @Path("{taskKey}")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    void update(@NotNull @PathParam("taskKey") Long taskKey, @NotNull AbstractTaskTO taskTO);

    /**
     * Deletes the task matching the provided key.
     *
     * @param taskKey key of task to be deleted
     */
    @DELETE
    @Path("{taskKey}")
    void delete(@NotNull @PathParam("taskKey") Long taskKey);

    /**
     * Deletes the task execution matching the provided key.
     *
     * @param executionKey key of task execution to be deleted
     */
    @DELETE
    @Path("executions/{executionKey}")
    void deleteExecution(@NotNull @PathParam("executionKey") Long executionKey);

    /**
     * Executes the task matching the given id.
     *
     * @param taskKey key of task to be executed
     * @param dryRun if true, task will only be simulated
     * @return execution report for the task matching the given id
     */
    @POST
    @Path("{taskKey}/execute")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    TaskExecTO execute(@NotNull @PathParam("taskKey") Long taskKey,
            @QueryParam("dryRun") @DefaultValue("false") boolean dryRun);

    /**
     * Reports task execution result.
     *
     * @param executionKey key of task execution being reported
     * @param reportExec execution being reported
     */
    @POST
    @Path("executions/{executionKey}/report")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    void report(@NotNull @PathParam("executionKey") Long executionKey, @NotNull ReportExecTO reportExec);

    /**
     * Executes the provided bulk action.
     *
     * @param bulkAction list of task ids against which the bulk action will be performed.
     * @return Bulk action result
     */
    @POST
    @Path("bulk")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    BulkActionResult bulk(@NotNull BulkAction bulkAction);
}
