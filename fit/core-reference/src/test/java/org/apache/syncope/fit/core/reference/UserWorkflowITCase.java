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
package org.apache.syncope.fit.core.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.mod.UserMod;
import org.apache.syncope.common.lib.to.MembershipTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.to.WorkflowFormPropertyTO;
import org.apache.syncope.common.lib.to.WorkflowFormTO;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.syncope.common.rest.api.service.UserWorkflowService;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@FixMethodOrder(MethodSorters.JVM)
public class UserWorkflowITCase extends AbstractITCase {

    @Test
    public void createWithReject() {
        Assume.assumeTrue(ActivitiDetector.isActivitiEnabledForUsers(syncopeService));

        UserTO userTO = UserITCase.getUniqueSampleTO("createWithReject@syncope.apache.org");
        userTO.getResources().add(RESOURCE_NAME_TESTDB);

        // User with role 9 are defined in workflow as subject to approval
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(9L);
        userTO.getMemberships().add(membershipTO);

        // 1. create user with role 9
        userTO = createUser(userTO);
        assertNotNull(userTO);
        assertEquals(1, userTO.getMemberships().size());
        assertEquals(9, userTO.getMemberships().get(0).getRoleId());
        assertEquals("createApproval", userTO.getStatus());

        // 2. request if there is any pending task for user just created
        WorkflowFormTO form = userWorkflowService.getFormForUser(userTO.getKey());
        assertNotNull(form);
        assertNotNull(form.getUserKey());
        assertEquals(userTO.getKey(), form.getUserKey());
        assertNotNull(form.getTaskId());
        assertNull(form.getOwner());

        // 3. claim task from rossini, not in role 7 (designated for approval in workflow definition): fail
        UserWorkflowService userService2 = clientFactory.create(
                "rossini", ADMIN_PWD).getService(UserWorkflowService.class);

        try {
            userService2.claimForm(form.getTaskId());
            fail();
        } catch (SyncopeClientException e) {
            assertEquals(ClientExceptionType.Workflow, e.getType());
        }

        // 4. claim task from bellini, in role 7
        UserWorkflowService userService3 = clientFactory.create(
                "bellini", ADMIN_PWD).getService(UserWorkflowService.class);

        form = userService3.claimForm(form.getTaskId());
        assertNotNull(form);
        assertNotNull(form.getTaskId());
        assertNotNull(form.getOwner());

        // 5. reject user
        Map<String, WorkflowFormPropertyTO> props = form.getPropertyMap();
        props.get("approve").setValue(Boolean.FALSE.toString());
        props.get("rejectReason").setValue("I don't like him.");
        form.getProperties().clear();
        form.getProperties().addAll(props.values());
        userTO = userService3.submitForm(form);
        assertNotNull(userTO);
        assertEquals("rejected", userTO.getStatus());

        // 6. check that rejected user was not propagated to external resource (SYNCOPE-364)
        JdbcTemplate jdbcTemplate = new JdbcTemplate(testDataSource);
        Exception exception = null;
        try {
            jdbcTemplate.queryForObject("SELECT id FROM test WHERE id=?",
                    new String[] { userTO.getUsername() }, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    public void createWithApproval() {
        Assume.assumeTrue(ActivitiDetector.isActivitiEnabledForUsers(syncopeService));

        // read forms *before* any operation
        List<WorkflowFormTO> forms = userWorkflowService.getForms();
        assertNotNull(forms);
        int preForms = forms.size();

        UserTO userTO = UserITCase.getUniqueSampleTO("createWithApproval@syncope.apache.org");
        userTO.getResources().add(RESOURCE_NAME_TESTDB);

        // User with role 9 are defined in workflow as subject to approval
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(9L);
        userTO.getMemberships().add(membershipTO);

        // 1. create user with role 9 (and verify that no propagation occurred)
        userTO = createUser(userTO);
        assertNotNull(userTO);
        assertEquals(1, userTO.getMemberships().size());
        assertEquals(9, userTO.getMemberships().get(0).getRoleId());
        assertEquals("createApproval", userTO.getStatus());
        assertEquals(Collections.singleton(RESOURCE_NAME_TESTDB), userTO.getResources());

        assertTrue(userTO.getPropagationStatusTOs().isEmpty());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(testDataSource);

        Exception exception = null;
        try {
            jdbcTemplate.queryForObject("SELECT id FROM test WHERE id=?",
                    new String[] { userTO.getUsername() }, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            exception = e;
        }
        assertNotNull(exception);

        // 2. request if there is any pending form for user just created
        forms = userWorkflowService.getForms();
        assertNotNull(forms);
        assertEquals(preForms + 1, forms.size());

        WorkflowFormTO form = userWorkflowService.getFormForUser(userTO.getKey());
        assertNotNull(form);
        assertNotNull(form.getTaskId());
        assertNull(form.getOwner());

        // 4. claim task (from admin)
        form = userWorkflowService.claimForm(form.getTaskId());
        assertNotNull(form);
        assertNotNull(form.getTaskId());
        assertNotNull(form.getOwner());

        // 5. approve user (and verify that propagation occurred)
        Map<String, WorkflowFormPropertyTO> props = form.getPropertyMap();
        props.get("approve").setValue(Boolean.TRUE.toString());
        form.getProperties().clear();
        form.getProperties().addAll(props.values());
        userTO = userWorkflowService.submitForm(form);
        assertNotNull(userTO);
        assertEquals("active", userTO.getStatus());
        assertEquals(Collections.singleton(RESOURCE_NAME_TESTDB), userTO.getResources());

        exception = null;
        try {
            final String username = jdbcTemplate.queryForObject("SELECT id FROM test WHERE id=?", String.class,
                    userTO.getUsername());
            assertEquals(userTO.getUsername(), username);
        } catch (EmptyResultDataAccessException e) {
            exception = e;
        }
        assertNull(exception);

        // 6. update user
        UserMod userMod = new UserMod();
        userMod.setKey(userTO.getKey());
        userMod.setPassword("anotherPassword123");

        userTO = updateUser(userMod);
        assertNotNull(userTO);
    }

    @Test
    public void issueSYNCOPE15() {
        Assume.assumeTrue(ActivitiDetector.isActivitiEnabledForUsers(syncopeService));

        // read forms *before* any operation
        List<WorkflowFormTO> forms = userWorkflowService.getForms();
        assertNotNull(forms);
        int preForms = forms.size();

        UserTO userTO = UserITCase.getUniqueSampleTO("issueSYNCOPE15@syncope.apache.org");
        userTO.getResources().clear();
        userTO.getVirAttrs().clear();
        userTO.getDerAttrs().clear();
        userTO.getMemberships().clear();

        // User with role 9 are defined in workflow as subject to approval
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(9L);
        userTO.getMemberships().add(membershipTO);

        // 1. create user with role 9 (and verify that no propagation occurred)
        userTO = createUser(userTO);
        assertNotNull(userTO);
        assertNotEquals(0L, userTO.getKey());
        assertNotNull(userTO.getCreationDate());
        assertNotNull(userTO.getCreator());
        assertNotNull(userTO.getLastChangeDate());
        assertNotNull(userTO.getLastModifier());
        assertEquals(userTO.getCreationDate(), userTO.getLastChangeDate());

        // 2. request if there is any pending form for user just created
        forms = userWorkflowService.getForms();
        assertEquals(preForms + 1, forms.size());

        WorkflowFormTO form = userWorkflowService.getFormForUser(userTO.getKey());
        assertNotNull(form);

        // 3. first claim ny bellini ....
        UserWorkflowService userService3 = clientFactory.create(
                "bellini", ADMIN_PWD).getService(UserWorkflowService.class);

        form = userService3.claimForm(form.getTaskId());
        assertNotNull(form);
        assertNotNull(form.getTaskId());
        assertNotNull(form.getOwner());

        // 4. second claim task by admin
        form = userWorkflowService.claimForm(form.getTaskId());
        assertNotNull(form);

        // 5. approve user
        final Map<String, WorkflowFormPropertyTO> props = form.getPropertyMap();
        props.get("approve").setValue(Boolean.TRUE.toString());
        form.getProperties().clear();
        form.getProperties().addAll(props.values());

        // 6. submit approve
        userTO = userWorkflowService.submitForm(form);
        assertNotNull(userTO);
        assertEquals(preForms, userWorkflowService.getForms().size());
        assertNull(userWorkflowService.getFormForUser(userTO.getKey()));

        // 7. search approval into the history as well
        forms = userWorkflowService.getFormsByName(userTO.getKey(), "Create approval");
        assertFalse(forms.isEmpty());

        int count = 0;
        for (WorkflowFormTO hform : forms) {
            if (form.getTaskId().equals(hform.getTaskId())) {
                count++;

                assertEquals("createApproval", hform.getKey());
                assertNotNull(hform.getCreateTime());
                assertNotNull(hform.getDueDate());
                assertTrue(Boolean.parseBoolean(hform.getPropertyMap().get("approve").getValue()));
                assertNull(hform.getPropertyMap().get("rejectReason").getValue());
            }
        }
        assertEquals(1, count);

        userService.delete(userTO.getKey());

        try {
            userService.read(userTO.getKey());
            fail();
        } catch (Exception ignore) {
            assertNotNull(ignore);
        }

        try {
            userWorkflowService.getFormsByName(userTO.getKey(), "Create approval");
            fail();
        } catch (Exception ignore) {
            assertNotNull(ignore);
        }
    }

}
