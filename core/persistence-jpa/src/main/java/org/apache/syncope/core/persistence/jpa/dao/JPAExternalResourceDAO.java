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
package org.apache.syncope.core.persistence.jpa.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.apache.syncope.common.lib.types.IntMappingType;
import org.apache.syncope.common.lib.types.PolicyType;
import org.apache.syncope.common.lib.types.TaskType;
import org.apache.syncope.core.persistence.api.dao.ExternalResourceDAO;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.persistence.api.dao.PolicyDAO;
import org.apache.syncope.core.persistence.api.dao.RoleDAO;
import org.apache.syncope.core.persistence.api.dao.TaskDAO;
import org.apache.syncope.core.persistence.api.dao.UserDAO;
import org.apache.syncope.core.persistence.api.entity.AccountPolicy;
import org.apache.syncope.core.persistence.api.entity.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.Mapping;
import org.apache.syncope.core.persistence.api.entity.MappingItem;
import org.apache.syncope.core.persistence.api.entity.Policy;
import org.apache.syncope.core.persistence.api.entity.role.Role;
import org.apache.syncope.core.persistence.api.entity.task.PropagationTask;
import org.apache.syncope.core.persistence.api.entity.task.PushTask;
import org.apache.syncope.core.persistence.api.entity.task.SyncTask;
import org.apache.syncope.core.persistence.api.entity.user.UMappingItem;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.persistence.jpa.entity.AbstractMappingItem;
import org.apache.syncope.core.persistence.jpa.entity.JPAExternalResource;
import org.apache.syncope.core.persistence.jpa.entity.role.JPARMappingItem;
import org.apache.syncope.core.persistence.jpa.entity.user.JPAUMappingItem;
import org.apache.syncope.core.provisioning.api.ConnectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JPAExternalResourceDAO extends AbstractDAO<ExternalResource, String> implements ExternalResourceDAO {

    @Autowired
    private TaskDAO taskDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private PolicyDAO policyDAO;

    @Autowired
    private ConnectorRegistry connRegistry;

    @Override
    public ExternalResource find(final String name) {
        TypedQuery<ExternalResource> query = entityManager.createQuery("SELECT e FROM "
                + JPAExternalResource.class.getSimpleName() + " e WHERE e.name = :name", ExternalResource.class);
        query.setParameter("name", name);

        ExternalResource result = null;
        try {
            result = query.getSingleResult();
        } catch (NoResultException e) {
            LOG.debug("No resource found with name {}", name, e);
        }

        return result;
    }

    private StringBuilder getByPolicyQuery(final PolicyType type) {
        StringBuilder query = new StringBuilder("SELECT e FROM ").
                append(JPAExternalResource.class.getSimpleName()).
                append(" e WHERE e.");
        switch (type) {
            case ACCOUNT:
            case GLOBAL_ACCOUNT:
                query.append("accountPolicy");
                break;

            case PASSWORD:
            case GLOBAL_PASSWORD:
                query.append("passwordPolicy");
                break;

            case SYNC:
            case GLOBAL_SYNC:
                query.append("syncPolicy");
                break;

            default:
                break;
        }
        return query;
    }

    @Override
    public List<ExternalResource> findByPolicy(final Policy policy) {
        TypedQuery<ExternalResource> query = entityManager.createQuery(
                getByPolicyQuery(policy.getType()).append(" = :policy").toString(), ExternalResource.class);
        query.setParameter("policy", policy);
        return query.getResultList();
    }

    @Override
    public List<ExternalResource> findWithoutPolicy(final PolicyType type) {
        TypedQuery<ExternalResource> query = entityManager.createQuery(
                getByPolicyQuery(type).append(" IS NULL").toString(), ExternalResource.class);
        return query.getResultList();
    }

    @Override
    public List<ExternalResource> findAll() {
        TypedQuery<ExternalResource> query = entityManager.createQuery(
                "SELECT e FROM  " + JPAExternalResource.class.getSimpleName() + " e", ExternalResource.class);
        return query.getResultList();
    }

    @Override
    public List<ExternalResource> findAllByPriority() {
        TypedQuery<ExternalResource> query = entityManager.createQuery(
                "SELECT e FROM  " + JPAExternalResource.class.getSimpleName() + " e ORDER BY e.propagationPriority",
                ExternalResource.class);
        return query.getResultList();
    }

    /**
     * This method has an explicit Transactional annotation because it is called by SyncJob.
     *
     * @see org.apache.syncope.core.sync.impl.SyncJob
     *
     * @param resource entity to be merged
     * @return the same entity, updated
     */
    @Override
    @Transactional(rollbackFor = { Throwable.class })
    public ExternalResource save(final ExternalResource resource) {
        ExternalResource merged = entityManager.merge(resource);
        try {
            connRegistry.registerConnector(merged);
        } catch (NotFoundException e) {
            LOG.error("While registering connector for resource", e);
        }
        return merged;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends MappingItem> void deleteMapping(
            final String intAttrName, final IntMappingType intMappingType, final Class<T> reference) {

        if (IntMappingType.getEmbedded().contains(intMappingType)) {
            return;
        }

        Class<? extends AbstractMappingItem> jpaRef = reference.equals(UMappingItem.class)
                ? JPAUMappingItem.class
                : JPARMappingItem.class;

        TypedQuery<T> query = entityManager.createQuery("SELECT m FROM " + jpaRef.getSimpleName()
                + " m WHERE m.intAttrName=:intAttrName AND m.intMappingType=:intMappingType", reference);
        query.setParameter("intAttrName", intAttrName);
        query.setParameter("intMappingType", intMappingType);

        Set<Long> itemIds = new HashSet<>();
        for (T item : query.getResultList()) {
            itemIds.add(item.getKey());
        }
        Class<?> mappingRef = null;
        for (Long itemId : itemIds) {
            T item = (T) entityManager.find(jpaRef, itemId);
            if (item != null) {
                mappingRef = item.getMapping().getClass();

                ((Mapping<T>) item.getMapping()).removeItem(item);
                item.setMapping(null);

                entityManager.remove(item);
            }
        }

        // Make empty query cache for *MappingItem and related *Mapping
        entityManager.getEntityManagerFactory().getCache().evict(jpaRef);
        if (mappingRef != null) {
            entityManager.getEntityManagerFactory().getCache().evict(mappingRef);
        }
    }

    @Override
    public void delete(final String name) {
        ExternalResource resource = find(name);
        if (resource == null) {
            return;
        }

        taskDAO.deleteAll(resource, TaskType.PROPAGATION);
        taskDAO.deleteAll(resource, TaskType.SYNCHRONIZATION);
        taskDAO.deleteAll(resource, TaskType.PUSH);

        for (User user : userDAO.findByResource(resource)) {
            user.removeResource(resource);
        }
        for (Role role : roleDAO.findByResource(resource)) {
            role.removeResource(resource);
        }
        for (AccountPolicy policy : policyDAO.findByResource(resource)) {
            policy.removeResource(resource);
        }

        if (resource.getConnector() != null && resource.getConnector().getResources() != null
                && !resource.getConnector().getResources().isEmpty()) {

            resource.getConnector().getResources().remove(resource);
        }
        resource.setConnector(null);

        if (resource.getUmapping() != null) {
            for (MappingItem item : resource.getUmapping().getItems()) {
                item.setMapping(null);
            }
            resource.getUmapping().getItems().clear();
            resource.getUmapping().setResource(null);
            resource.setUmapping(null);
        }
        if (resource.getRmapping() != null) {
            for (MappingItem item : resource.getRmapping().getItems()) {
                item.setMapping(null);
            }
            resource.getRmapping().getItems().clear();
            resource.getRmapping().setResource(null);
            resource.setRmapping(null);
        }

        entityManager.remove(resource);
    }
}
