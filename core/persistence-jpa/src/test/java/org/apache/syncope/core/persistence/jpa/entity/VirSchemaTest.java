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
package org.apache.syncope.core.persistence.jpa.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import org.apache.syncope.common.lib.types.AttributableType;
import org.apache.syncope.common.lib.types.EntityViolationType;
import org.apache.syncope.core.persistence.api.attrvalue.validation.InvalidEntityException;
import org.apache.syncope.core.persistence.api.dao.VirSchemaDAO;
import org.apache.syncope.core.persistence.api.entity.VirSchema;
import org.apache.syncope.core.persistence.api.entity.role.RVirSchema;
import org.apache.syncope.core.persistence.api.entity.user.UVirSchema;
import org.apache.syncope.core.persistence.jpa.AbstractTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class VirSchemaTest extends AbstractTest {

    @Autowired
    private VirSchemaDAO virSchemaDAO;

    @Test
    public void findAll() {
        List<UVirSchema> list = virSchemaDAO.findAll(UVirSchema.class);
        assertEquals(2, list.size());
    }

    @Test
    public void findByName() {
        UVirSchema attributeSchema = virSchemaDAO.find("virtualdata", UVirSchema.class);
        assertNotNull("did not find expected virtual attribute schema", attributeSchema);
    }

    @Test
    public void save() {
        UVirSchema virtualAttributeSchema = entityFactory.newEntity(UVirSchema.class);
        virtualAttributeSchema.setKey("virtual");
        virtualAttributeSchema.setReadonly(true);

        virSchemaDAO.save(virtualAttributeSchema);

        UVirSchema actual = virSchemaDAO.find("virtual", UVirSchema.class);
        assertNotNull("expected save to work", actual);
        assertTrue(actual.isReadonly());
    }

    @Test
    public void delete() {
        UVirSchema virtualdata = virSchemaDAO.find("virtualdata", UVirSchema.class);

        virSchemaDAO.delete(virtualdata.getKey(), attrUtilFactory.getInstance(AttributableType.USER));

        VirSchema actual = virSchemaDAO.find("virtualdata", UVirSchema.class);
        assertNull("delete did not work", actual);

        // ------------- //
        RVirSchema rvirtualdata = virSchemaDAO.find("rvirtualdata", RVirSchema.class);
        assertNotNull(rvirtualdata);

        virSchemaDAO.delete(rvirtualdata.getKey(), attrUtilFactory.getInstance(AttributableType.ROLE));

        actual = virSchemaDAO.find("rvirtualdata", RVirSchema.class);
        assertNull("delete did not work", actual);
    }

    @Test
    public void issueSYNCOPE418() {
        UVirSchema schema = entityFactory.newEntity(UVirSchema.class);
        schema.setKey("http://schemas.examples.org/security/authorization/organizationUnit");

        try {
            virSchemaDAO.save(schema);
            fail();
        } catch (InvalidEntityException e) {
            assertTrue(e.hasViolation(EntityViolationType.InvalidName));
        }
    }
}
