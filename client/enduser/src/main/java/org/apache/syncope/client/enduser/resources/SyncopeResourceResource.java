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
package org.apache.syncope.client.enduser.resources;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.syncope.client.enduser.SyncopeEnduserSession;
import org.apache.syncope.client.enduser.adapters.ResourceTOAdapter;
import org.apache.syncope.common.lib.to.ResourceTO;
import org.apache.syncope.common.rest.api.service.ResourceService;
import org.apache.syncope.core.misc.serialization.POJOHelper;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncopeResourceResource extends AbstractBaseResource {

    private static final long serialVersionUID = 7475706378304995200L;

    private static final Logger LOG = LoggerFactory.getLogger(SyncopeResourceResource.class);

    private final ResourceService resourceService;

    private final ResourceTOAdapter resourceTOAdapter;

    public SyncopeResourceResource() {
        resourceTOAdapter = new ResourceTOAdapter();
        resourceService = SyncopeEnduserSession.get().getService(ResourceService.class);
    }

    @Override
    protected AbstractResource.ResourceResponse newResourceResponse(final IResource.Attributes attributes) {

        LOG.debug("Search all available resources");

        AbstractResource.ResourceResponse response = new AbstractResource.ResourceResponse();

        try {

            HttpServletRequest request = (HttpServletRequest) attributes.getRequest().getContainerRequest();
            if (!xsrfCheck(request)) {
                LOG.error("XSRF TOKEN does not match");
                response.setError(Response.Status.BAD_REQUEST.getStatusCode(), "XSRF TOKEN does not match");
                return response;
            }

            final List<ResourceTO> resourceTOs = resourceService.list();

            response.setWriteCallback(new AbstractResource.WriteCallback() {

                @Override
                public void writeData(final IResource.Attributes attributes) throws IOException {
                    attributes.getResponse().write(POJOHelper.serialize(resourceTOAdapter.fromResourceTO(resourceTOs)));
                }
            });
            response.setStatusCode(Response.Status.OK.getStatusCode());
        } catch (Exception e) {
            LOG.error("Error retrieving available resources", e);
            response.setError(Response.Status.BAD_REQUEST.getStatusCode(), new StringBuilder()
                    .append("ErrorMessage{{ ")
                    .append(e.getMessage())
                    .append(" }}")
                    .toString());
        }
        return response;
    }

}
