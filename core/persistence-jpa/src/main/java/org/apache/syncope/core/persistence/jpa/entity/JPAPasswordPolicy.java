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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.apache.syncope.common.lib.types.PolicyType;
import org.apache.syncope.core.persistence.api.entity.PasswordPolicy;

@Entity
@DiscriminatorValue("PasswordPolicy")
public class JPAPasswordPolicy extends JPAPolicy implements PasswordPolicy {

    private static final long serialVersionUID = 9138550910385232849L;

    public JPAPasswordPolicy() {
        this(false);
    }

    public JPAPasswordPolicy(final boolean global) {
        super();

        this.type = global
                ? PolicyType.GLOBAL_PASSWORD
                : PolicyType.PASSWORD;
    }
}
