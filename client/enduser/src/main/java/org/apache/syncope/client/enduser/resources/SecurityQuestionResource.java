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
import org.apache.syncope.common.lib.to.SecurityQuestionTO;
import org.apache.syncope.common.rest.api.service.SecurityQuestionService;
import org.apache.syncope.core.misc.serialization.POJOHelper;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityQuestionResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityQuestionResource.class);

    private static final long serialVersionUID = 6453101466981543020L;

    private final SecurityQuestionService securityQuestionService;

    public SecurityQuestionResource() {
        securityQuestionService = SyncopeEnduserSession.get().getService(SecurityQuestionService.class);
    }

    @Override
    protected AbstractResource.ResourceResponse newResourceResponse(final IResource.Attributes attributes) {

        LOG.debug("List available security questions");

        AbstractResource.ResourceResponse response = new AbstractResource.ResourceResponse();

        try {

            HttpServletRequest request = (HttpServletRequest) attributes.getRequest().getContainerRequest();

            if (!xsrfCheck(request)) {
                LOG.error("XSRF TOKEN does not match");
                response.setError(Response.Status.BAD_REQUEST.getStatusCode(), "XSRF TOKEN does not match");
                return response;
            }

            PageParameters parameters = attributes.getParameters();
            StringValue username = parameters.get("username");
            //if the username is defined then retrieve its security questions, otherwise retrieve all security questions
            if (!username.isEmpty()) {
                final SecurityQuestionTO securityQuestionTO = securityQuestionService.readByUser(username.toString());

                response.setWriteCallback(new AbstractResource.WriteCallback() {

                    @Override
                    public void writeData(final IResource.Attributes attributes) throws IOException {
                        attributes.getResponse().write(POJOHelper.serialize(securityQuestionTO));
                    }
                });
            } else {
                final List<SecurityQuestionTO> securityQuestionTOs = securityQuestionService.list();

                response.setWriteCallback(new AbstractResource.WriteCallback() {

                    @Override
                    public void writeData(final IResource.Attributes attributes) throws IOException {
                        attributes.getResponse().write(POJOHelper.serialize(securityQuestionTOs));
                    }
                });
            }

            response.setStatusCode(Response.Status.OK.getStatusCode());
        } catch (Exception e) {
            LOG.error("Error retrieving security questions", e);
            response.setError(Response.Status.BAD_REQUEST.getStatusCode(), new StringBuilder()
                    .append("ErrorMessage{{ ")
                    .append(e.getMessage())
                    .append(" }}")
                    .toString());
        }

        return response;
    }

}
