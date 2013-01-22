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
package org.apache.syncope.core.services;

import java.net.URI;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.syncope.common.services.SchemaService;
import org.apache.syncope.common.to.AbstractSchemaTO;
import org.apache.syncope.common.to.DerivedSchemaTO;
import org.apache.syncope.common.to.SchemaTO;
import org.apache.syncope.common.to.VirtualSchemaTO;
import org.apache.syncope.common.types.AttributableType;
import org.apache.syncope.core.rest.controller.DerivedSchemaController;
import org.apache.syncope.core.rest.controller.SchemaController;
import org.apache.syncope.core.rest.controller.VirtualSchemaController;
import org.apache.syncope.core.util.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaServiceImpl implements SchemaService, ContextAware {

    @Autowired
    private SchemaController normalSchemaController;

    @Autowired
    private DerivedSchemaController derivedSchemaController;

    @Autowired
    private VirtualSchemaController virtualSchemaController;

    private UriInfo uriInfo;

    @Override
    public <T extends AbstractSchemaTO> Response create(final AttributableType kind, final SchemaType type,
            final T schemaTO) {
        AbstractSchemaTO response;
        switch (type) {
            case NORMAL:
                response = normalSchemaController.create(new DummyHTTPServletResponse(), (SchemaTO) schemaTO,
                        kind.toString());
                break;

            case DERIVED:
                response = derivedSchemaController.create(new DummyHTTPServletResponse(), (DerivedSchemaTO) schemaTO,
                        kind.toString());
                break;

            case VIRTUAL:
                response = virtualSchemaController.create(new DummyHTTPServletResponse(), (VirtualSchemaTO) schemaTO,
                        kind.toString());
                break;

            default:
                throw new BadRequestException();
        }
        URI location = uriInfo.getAbsolutePathBuilder().path(response.getName()).build();
        return Response.created(location).build();
    }

    @Override
    public void delete(final AttributableType kind, final SchemaType type, final String schemaName) {
        try {
            switch (type) {
                case NORMAL:
                    normalSchemaController.delete(kind.toString(), schemaName);

                case DERIVED:
                    derivedSchemaController.delete(kind.toString(), schemaName);

                case VIRTUAL:
                    virtualSchemaController.delete(kind.toString(), schemaName);

                default:
                    throw new BadRequestException();
            }
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractSchemaTO> List<T> list(final AttributableType kind, final SchemaType type) {
        switch (type) {
            case NORMAL:
                return (List<T>) normalSchemaController.list(kind.toString());

            case DERIVED:
                return (List<T>) derivedSchemaController.list(kind.toString());

            case VIRTUAL:
                return (List<T>) virtualSchemaController.list(kind.toString());

            default:
                throw new BadRequestException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractSchemaTO> T read(final AttributableType kind, final SchemaType type,
            final String schemaName) {
        try {
            switch (type) {
                case NORMAL:
                    return (T) normalSchemaController.read(kind.toString(), schemaName);

                case DERIVED:
                    return (T) derivedSchemaController.read(kind.toString(), schemaName);

                case VIRTUAL:
                    return (T) virtualSchemaController.read(kind.toString(), schemaName);

                default:
                    throw new BadRequestException();
            }
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @Override
    public  <T extends AbstractSchemaTO> void update(final AttributableType kind, final SchemaType type,
            final String schemaName, final T schemaTO) {
        try {
            switch (type) {
                case NORMAL:
                    normalSchemaController.update((SchemaTO) schemaTO, kind.toString());

                case DERIVED:
                    derivedSchemaController.update((DerivedSchemaTO) schemaTO, kind.toString());

                case VIRTUAL:
                    virtualSchemaController.update((VirtualSchemaTO) schemaTO, kind.toString());

                default:
                    throw new BadRequestException();
            }
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @Override
    public void setUriInfo(final UriInfo ui) {
        this.uriInfo = ui;
    }
}