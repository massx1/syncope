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

import java.util.Collections;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.apache.syncope.core.persistence.api.dao.ReportDAO;
import org.apache.syncope.core.persistence.api.dao.search.OrderByClause;
import org.apache.syncope.core.persistence.api.attrvalue.validation.InvalidEntityException;
import org.apache.syncope.core.persistence.api.entity.Report;
import org.apache.syncope.core.persistence.jpa.entity.JPAReport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JPAReportDAO extends AbstractDAO<Report, Long> implements ReportDAO {

    @Override
    @Transactional(readOnly = true)
    public Report find(final Long key) {
        return entityManager.find(JPAReport.class, key);
    }

    @Override
    public List<Report> findAll() {
        return findAll(-1, -1, Collections.<OrderByClause>emptyList());
    }

    @Override
    public List<Report> findAll(final int page, final int itemsPerPage, final List<OrderByClause> orderByClauses) {
        final TypedQuery<Report> query = entityManager.createQuery(
                "SELECT e FROM " + JPAReport.class.getSimpleName() + " e "
                + toOrderByStatement(Report.class, "e", orderByClauses), Report.class);

        query.setFirstResult(itemsPerPage * (page <= 0
                ? 0
                : page - 1));

        if (itemsPerPage > 0) {
            query.setMaxResults(itemsPerPage);
        }

        return query.getResultList();
    }

    @Override
    public int count() {
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(id) FROM " + JPAReport.TABLE);
        return ((Number) countQuery.getSingleResult()).intValue();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Report save(final Report report) throws InvalidEntityException {
        return entityManager.merge(report);
    }

    @Override
    public void delete(final Long key) {
        Report report = find(key);
        if (report == null) {
            return;
        }

        delete(report);
    }

    @Override
    public void delete(final Report report) {
        entityManager.remove(report);
    }
}
