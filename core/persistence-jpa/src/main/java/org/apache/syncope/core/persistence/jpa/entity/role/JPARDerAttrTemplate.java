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
package org.apache.syncope.core.persistence.jpa.entity.role;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.syncope.core.persistence.api.entity.role.RDerAttrTemplate;
import org.apache.syncope.core.persistence.api.entity.role.RDerSchema;
import org.apache.syncope.core.persistence.api.entity.role.Role;
import org.apache.syncope.core.persistence.jpa.entity.AbstractDerAttrTemplate;

@Entity
@Table(name = JPARDerAttrTemplate.TABLE)
public class JPARDerAttrTemplate extends AbstractDerAttrTemplate<RDerSchema> implements RDerAttrTemplate {

    private static final long serialVersionUID = 624868884107016649L;

    public static final String TABLE = "RDerAttrTemplate";

    @ManyToOne
    private JPARole owner;

    @ManyToOne
    @JoinColumn(name = "schema_name")
    private JPARDerSchema schema;

    @Override
    public RDerSchema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(final RDerSchema schema) {
        checkType(schema, JPARDerSchema.class);
        this.schema = (JPARDerSchema) schema;
    }

    @Override
    public Role getOwner() {
        return owner;
    }

    @Override
    public void setOwner(final Role owner) {
        checkType(owner, JPARole.class);
        this.owner = (JPARole) owner;
    }
}
