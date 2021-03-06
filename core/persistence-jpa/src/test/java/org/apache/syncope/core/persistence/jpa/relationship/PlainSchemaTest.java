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
package org.apache.syncope.core.persistence.jpa.relationship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.apache.syncope.common.lib.types.AttributableType;
import org.apache.syncope.core.persistence.api.dao.ExternalResourceDAO;
import org.apache.syncope.core.persistence.api.dao.PlainAttrDAO;
import org.apache.syncope.core.persistence.api.dao.PlainSchemaDAO;
import org.apache.syncope.core.persistence.api.dao.UserDAO;
import org.apache.syncope.core.persistence.api.entity.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.MappingItem;
import org.apache.syncope.core.persistence.api.entity.user.UPlainAttr;
import org.apache.syncope.core.persistence.api.entity.user.UPlainSchema;
import org.apache.syncope.core.persistence.jpa.AbstractTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class PlainSchemaTest extends AbstractTest {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PlainSchemaDAO plainSchemaDAO;

    @Autowired
    private PlainAttrDAO plainAttrDAO;

    @Autowired
    private ExternalResourceDAO resourceDAO;

    @Test
    public void deleteFullname() {
        // fullname is mapped as AccountId for ws-target-resource-2, need to swap it otherwise validation errors 
        // will be raised
        for (MappingItem item : resourceDAO.find("ws-target-resource-2").getUmapping().getItems()) {
            if ("fullname".equals(item.getIntAttrName())) {
                item.setAccountid(false);
            } else if ("surname".equals(item.getIntAttrName())) {
                item.setAccountid(true);
            }
        }

        // search for user schema fullname
        UPlainSchema schema = plainSchemaDAO.find("fullname", UPlainSchema.class);
        assertNotNull(schema);

        // check for associated mappings
        Set<MappingItem> mapItems = new HashSet<>();
        for (ExternalResource resource : resourceDAO.findAll()) {
            if (resource.getUmapping() != null) {
                for (MappingItem mapItem : resource.getUmapping().getItems()) {
                    if (schema.getKey().equals(mapItem.getIntAttrName())) {
                        mapItems.add(mapItem);
                    }
                }
            }
        }
        assertFalse(mapItems.isEmpty());

        // delete user schema fullname
        plainSchemaDAO.delete("fullname", attrUtilFactory.getInstance(AttributableType.USER));

        plainSchemaDAO.flush();

        // check for schema deletion
        schema = plainSchemaDAO.find("fullname", UPlainSchema.class);
        assertNull(schema);

        plainSchemaDAO.clear();

        // check for mappings deletion
        mapItems = new HashSet<>();
        for (ExternalResource resource : resourceDAO.findAll()) {
            if (resource.getUmapping() != null) {
                for (MappingItem mapItem : resource.getUmapping().getItems()) {
                    if ("fullname".equals(mapItem.getIntAttrName())) {
                        mapItems.add(mapItem);
                    }
                }
            }
        }
        assertTrue(mapItems.isEmpty());

        assertNull(plainAttrDAO.find(100L, UPlainAttr.class));
        assertNull(plainAttrDAO.find(300L, UPlainAttr.class));
        assertNull(userDAO.find(1L).getPlainAttr("fullname"));
        assertNull(userDAO.find(3L).getPlainAttr("fullname"));
    }

    @Test
    public void deleteSurname() {
        // search for user schema fullname
        UPlainSchema schema = plainSchemaDAO.find("surname", UPlainSchema.class);
        assertNotNull(schema);

        // check for associated mappings
        Set<MappingItem> mappings = new HashSet<>();
        for (ExternalResource resource : resourceDAO.findAll()) {
            if (resource.getUmapping() != null) {
                for (MappingItem mapItem : resource.getUmapping().getItems()) {
                    if (schema.getKey().equals(mapItem.getIntAttrName())) {
                        mappings.add(mapItem);
                    }
                }
            }
        }
        assertFalse(mappings.isEmpty());

        // delete user schema fullname
        plainSchemaDAO.delete("surname", attrUtilFactory.getInstance(AttributableType.USER));

        plainSchemaDAO.flush();

        // check for schema deletion
        schema = plainSchemaDAO.find("surname", UPlainSchema.class);
        assertNull(schema);
    }

    @Test
    public void deleteALong() {
        assertEquals(6, resourceDAO.find("resource-db-sync").getUmapping().getItems().size());

        plainSchemaDAO.delete("aLong", attrUtilFactory.getInstance(AttributableType.USER));
        assertNull(plainSchemaDAO.find("aLong", UPlainSchema.class));

        plainSchemaDAO.flush();

        assertEquals(5, resourceDAO.find("resource-db-sync").getUmapping().getItems().size());
    }
}
