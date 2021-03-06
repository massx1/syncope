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

import java.util.List;
import org.apache.syncope.core.persistence.api.dao.EntitlementDAO;
import org.apache.syncope.core.persistence.api.entity.Entitlement;
import org.apache.syncope.core.persistence.jpa.AbstractTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class EntitlementTest extends AbstractTest {

    @Autowired
    private EntitlementDAO entitlementDAO;

    @Test
    public void findAll() {
        List<Entitlement> list = entitlementDAO.findAll();
        assertEquals("did not get expected number of entitlements ", 86, list.size());
    }

    @Test
    public void findByName() {
        Entitlement entitlement = entitlementDAO.find("base");
        assertNotNull("did not find expected entitlement", entitlement);
    }

    @Test
    public void save() {
        Entitlement entitlement = entityFactory.newEntity(Entitlement.class);
        entitlement.setKey("another");

        entitlementDAO.save(entitlement);

        Entitlement actual = entitlementDAO.find("another");
        assertNotNull("expected save to work", actual);
        assertEquals(entitlement, actual);
    }

    @Test
    public void delete() {
        Entitlement entitlement = entitlementDAO.find("base");
        assertNotNull("did not find expected entitlement", entitlement);

        entitlementDAO.delete("base");

        assertNull(entitlementDAO.find("base"));
    }
}
