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
package org.apache.syncope.core.persistence.jpa.entity.user;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import org.apache.syncope.core.persistence.api.entity.Attributable;
import org.apache.syncope.core.persistence.api.entity.PlainAttrUniqueValue;
import org.apache.syncope.core.persistence.api.entity.PlainAttrValue;
import org.apache.syncope.core.persistence.api.entity.PlainSchema;
import org.apache.syncope.core.persistence.api.entity.user.UPlainAttr;
import org.apache.syncope.core.persistence.api.entity.user.UPlainAttrUniqueValue;
import org.apache.syncope.core.persistence.api.entity.user.UPlainAttrValue;
import org.apache.syncope.core.persistence.api.entity.user.UPlainSchema;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.persistence.jpa.entity.AbstractPlainAttr;

@Entity
@Table(name = JPAUPlainAttr.TABLE)
public class JPAUPlainAttr extends AbstractPlainAttr implements UPlainAttr {

    private static final long serialVersionUID = 6333601983691157406L;

    public static final String TABLE = "UPlainAttr";

    /**
     * Auto-generated id for this table.
     */
    @Id
    private Long id;

    /**
     * The owner of this attribute.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private JPAUser owner;

    /**
     * The schema of this attribute.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schema_name")
    private JPAUPlainSchema schema;

    /**
     * Values of this attribute (if schema is not UNIQUE).
     */
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, mappedBy = "attribute")
    @Valid
    private List<JPAUPlainAttrValue> values;

    /**
     * Value of this attribute (if schema is UNIQUE).
     */
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "attribute")
    @Valid
    private JPAUPlainAttrUniqueValue uniqueValue;

    /**
     * Default constructor.
     */
    public JPAUPlainAttr() {
        super();
        values = new ArrayList<>();
    }

    @Override
    public Long getKey() {
        return id;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public void setOwner(final Attributable<?, ?, ?> owner) {
        checkType(owner, JPAUser.class);
        this.owner = (JPAUser) owner;
    }

    @Override
    public UPlainSchema getSchema() {
        return schema;
    }

    @Override
    public void setSchema(final PlainSchema schema) {
        checkType(schema, JPAUPlainSchema.class);
        this.schema = (JPAUPlainSchema) schema;
    }

    @Override
    protected boolean addValue(final PlainAttrValue attrValue) {
        checkType(attrValue, JPAUPlainAttrValue.class);
        return values.add((JPAUPlainAttrValue) attrValue);
    }

    @Override
    public boolean removeValue(final PlainAttrValue attrValue) {
        checkType(attrValue, JPAUPlainAttrValue.class);
        return values.remove((JPAUPlainAttrValue) attrValue);
    }

    @Override
    public List<? extends UPlainAttrValue> getValues() {
        return values;
    }

    @Override
    public UPlainAttrUniqueValue getUniqueValue() {
        return uniqueValue;
    }

    @Override
    public void setUniqueValue(final PlainAttrUniqueValue uniqueValue) {
        checkType(uniqueValue, JPAUPlainAttrUniqueValue.class);
        this.uniqueValue = (JPAUPlainAttrUniqueValue) uniqueValue;
    }
}
