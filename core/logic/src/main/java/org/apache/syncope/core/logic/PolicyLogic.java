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
package org.apache.syncope.core.logic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.syncope.common.lib.to.AbstractPolicyTO;
import org.apache.syncope.common.lib.to.AccountPolicyTO;
import org.apache.syncope.common.lib.to.PasswordPolicyTO;
import org.apache.syncope.common.lib.to.SyncPolicyTO;
import org.apache.syncope.common.lib.types.PolicyType;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.persistence.api.dao.PolicyDAO;
import org.apache.syncope.core.persistence.api.entity.AccountPolicy;
import org.apache.syncope.core.persistence.api.entity.PasswordPolicy;
import org.apache.syncope.core.persistence.api.entity.Policy;
import org.apache.syncope.core.persistence.api.entity.SyncPolicy;
import org.apache.syncope.core.provisioning.api.data.PolicyDataBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class PolicyLogic extends AbstractTransactionalLogic<AbstractPolicyTO> {

    @Autowired
    private PolicyDAO policyDAO;

    @Autowired
    private PolicyDataBinder binder;

    @PreAuthorize("hasRole('POLICY_CREATE')")
    public <T extends AbstractPolicyTO> T create(final T policyTO) {
        return binder.getPolicyTO(policyDAO.save(binder.getPolicy(null, policyTO)));
    }

    private <T extends AbstractPolicyTO, K extends Policy> T update(final T policyTO, final K policy) {
        binder.getPolicy(policy, policyTO);
        K savedPolicy = policyDAO.save(policy);
        return binder.getPolicyTO(savedPolicy);
    }

    @PreAuthorize("hasRole('POLICY_UPDATE')")
    public PasswordPolicyTO update(final PasswordPolicyTO policyTO) {
        Policy policy = policyDAO.find(policyTO.getKey());
        if (!(policy instanceof PasswordPolicy)) {
            throw new NotFoundException("PasswordPolicy with id " + policyTO.getKey());
        }

        return update(policyTO, policy);
    }

    @PreAuthorize("hasRole('POLICY_UPDATE')")
    public AccountPolicyTO update(final AccountPolicyTO policyTO) {
        Policy policy = policyDAO.find(policyTO.getKey());
        if (!(policy instanceof AccountPolicy)) {
            throw new NotFoundException("AccountPolicy with id " + policyTO.getKey());
        }

        return update(policyTO, policy);
    }

    @PreAuthorize("hasRole('POLICY_UPDATE')")
    public SyncPolicyTO update(final SyncPolicyTO policyTO) {
        Policy policy = policyDAO.find(policyTO.getKey());
        if (!(policy instanceof SyncPolicy)) {
            throw new NotFoundException("SyncPolicy with id " + policyTO.getKey());
        }

        return update(policyTO, policy);
    }

    @PreAuthorize("hasRole('POLICY_LIST')")
    @SuppressWarnings("unchecked")
    public <T extends AbstractPolicyTO> List<T> list(final PolicyType type) {

        List<? extends Policy> policies = policyDAO.find(type);

        final List<T> policyTOs = new ArrayList<T>();
        for (Policy policy : policies) {
            policyTOs.add((T) binder.getPolicyTO(policy));
        }

        return policyTOs;
    }

    @PreAuthorize("hasRole('POLICY_READ')")
    public PasswordPolicyTO getGlobalPasswordPolicy() {
        PasswordPolicy policy = policyDAO.getGlobalPasswordPolicy();
        if (policy == null) {
            throw new NotFoundException("No password policy found");
        }

        return (PasswordPolicyTO) binder.getPolicyTO(policy);
    }

    @PreAuthorize("hasRole('POLICY_READ')")
    public AccountPolicyTO getGlobalAccountPolicy() {
        AccountPolicy policy = policyDAO.getGlobalAccountPolicy();
        if (policy == null) {
            throw new NotFoundException("No account policy found");
        }

        return (AccountPolicyTO) binder.getPolicyTO(policy);
    }

    @PreAuthorize("hasRole('POLICY_READ')")
    public SyncPolicyTO getGlobalSyncPolicy() {
        SyncPolicy policy = policyDAO.getGlobalSyncPolicy();
        if (policy == null) {
            throw new NotFoundException("No sync policy found");
        }

        return (SyncPolicyTO) binder.getPolicyTO(policy);
    }

    @PreAuthorize("hasRole('POLICY_READ')")
    public <T extends AbstractPolicyTO> T read(final Long id) {
        Policy policy = policyDAO.find(id);
        if (policy == null) {
            throw new NotFoundException("Policy " + id + " not found");
        }

        return binder.getPolicyTO(policy);
    }

    @PreAuthorize("hasRole('POLICY_DELETE')")
    public <T extends AbstractPolicyTO> T delete(final Long id) {
        Policy policy = policyDAO.find(id);
        if (policy == null) {
            throw new NotFoundException("Policy " + id + " not found");
        }

        T policyToDelete = binder.getPolicyTO(policy);
        policyDAO.delete(policy);

        return policyToDelete;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractPolicyTO resolveReference(final Method method, final Object... args)
            throws UnresolvedReferenceException {
        Long id = null;

        if (ArrayUtils.isNotEmpty(args)) {
            for (int i = 0; id == null && i < args.length; i++) {
                if (args[i] instanceof Long) {
                    id = (Long) args[i];
                } else if (args[i] instanceof AbstractPolicyTO) {
                    id = ((AbstractPolicyTO) args[i]).getKey();
                }
            }
        }

        if ((id != null) && !id.equals(0l)) {
            try {
                return binder.getPolicyTO(policyDAO.find(id));
            } catch (Throwable ignore) {
                LOG.debug("Unresolved reference", ignore);
                throw new UnresolvedReferenceException(ignore);
            }
        }

        throw new UnresolvedReferenceException();
    }
}
