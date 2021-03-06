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
package org.apache.syncope.core.persistence.jpa.entity.membership;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.syncope.core.persistence.api.entity.membership.MPlainAttrTemplate;
import org.apache.syncope.core.persistence.api.entity.membership.MPlainSchema;
import org.apache.syncope.core.persistence.api.entity.role.Role;
import org.apache.syncope.core.persistence.jpa.entity.AbstractPlainAttrTemplate;
import org.apache.syncope.core.persistence.jpa.entity.role.JPARole;

@Entity
@Table(name = JPAMPlainAttrTemplate.TABLE)
public class JPAMPlainAttrTemplate extends AbstractPlainAttrTemplate<MPlainSchema> implements MPlainAttrTemplate {

    private static final long serialVersionUID = -8768086609963244514L;

    public static final String TABLE = "MPlainAttrTemplate";

    @Id
    private Long id;

    @ManyToOne
    private JPARole owner;

    @ManyToOne
    @JoinColumn(name = "schema_name")
    private JPAMPlainSchema schema;

    @Override
    public Long getKey() {
        return id;
    }

    @Override
    public MPlainSchema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(final MPlainSchema schema) {
        checkType(schema, JPAMPlainSchema.class);
        this.schema = (JPAMPlainSchema) schema;
    }

    @Override
    public JPARole getOwner() {
        return owner;
    }

    @Override
    public void setOwner(final Role owner) {
        checkType(owner, JPARole.class);
        this.owner = (JPARole) owner;
    }

}
