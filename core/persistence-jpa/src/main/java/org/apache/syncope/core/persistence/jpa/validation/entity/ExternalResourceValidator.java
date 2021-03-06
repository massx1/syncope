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
package org.apache.syncope.core.persistence.jpa.validation.entity;

import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.types.AttributableType;
import org.apache.syncope.common.lib.types.EntityViolationType;
import org.apache.syncope.core.persistence.api.entity.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.Mapping;
import org.apache.syncope.core.persistence.api.entity.MappingItem;
import org.apache.syncope.core.provisioning.api.propagation.PropagationActions;
import org.apache.syncope.core.persistence.api.entity.user.UMapping;

public class ExternalResourceValidator extends AbstractValidator<ExternalResourceCheck, ExternalResource> {

    private boolean isValid(final MappingItem item, final ConstraintValidatorContext context) {
        if (StringUtils.isBlank(item.getExtAttrName())) {
            context.buildConstraintViolationWithTemplate(
                    getTemplate(EntityViolationType.InvalidMapping, item + ".extAttrName is null")).
                    addPropertyNode("extAttrName").addConstraintViolation();

            return false;
        }

        if (StringUtils.isBlank(item.getIntAttrName())) {
            context.buildConstraintViolationWithTemplate(
                    getTemplate(EntityViolationType.InvalidMapping, item + ".intAttrName is null")).
                    addPropertyNode("intAttrName").addConstraintViolation();

            return false;
        }

        if (item.getPurpose() == null) {
            context.buildConstraintViolationWithTemplate(
                    getTemplate(EntityViolationType.InvalidMapping, item + ".purpose is null")).
                    addPropertyNode("purpose").addConstraintViolation();

            return false;
        }

        return true;
    }

    private boolean isValid(final Mapping<?> mapping, final ConstraintValidatorContext context) {
        if (mapping == null) {
            return true;
        }

        int accountIds = 0;
        for (MappingItem item : mapping.getItems()) {
            if (item.isAccountid()) {
                accountIds++;
            }
        }
        if (accountIds != 1) {
            context.buildConstraintViolationWithTemplate(
                    getTemplate(EntityViolationType.InvalidMapping, "One and only one accountId mapping is needed")).
                    addPropertyNode("accountId.size").addConstraintViolation();
            return false;
        }

        final MappingItem accountId = mapping.getAccountIdItem();
        if (mapping instanceof UMapping
                && AttributableType.ROLE == accountId.getIntMappingType().getAttributableType()) {
            context.buildConstraintViolationWithTemplate(
                    getTemplate(EntityViolationType.InvalidMapping,
                            "Role attribute as accountId is not permitted")).
                    addPropertyNode("attributableType").addConstraintViolation();
            return false;
        }

        boolean isValid = true;

        int passwords = 0;
        for (MappingItem item : mapping.getItems()) {
            isValid &= isValid(item, context);

            if (item.isPassword()) {
                passwords++;
            }
        }
        if (passwords > 1) {
            context.buildConstraintViolationWithTemplate(
                    getTemplate(EntityViolationType.InvalidMapping, "One and only one password mapping is allowed")).
                    addPropertyNode("password.size").addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }

    @Override
    public boolean isValid(final ExternalResource resource, final ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (!NAME_PATTERN.matcher(resource.getKey()).matches()) {
            context.buildConstraintViolationWithTemplate(
                    getTemplate(EntityViolationType.InvalidName, "Invalid Resource name")).
                    addPropertyNode("name").addConstraintViolation();
            return false;
        }

        if (!resource.getPropagationActionsClassNames().isEmpty()) {
            for (String className : resource.getPropagationActionsClassNames()) {
                Class<?> actionsClass = null;
                boolean isAssignable = false;
                try {
                    actionsClass = Class.forName(className);
                    isAssignable = PropagationActions.class.isAssignableFrom(actionsClass);
                } catch (Exception e) {
                    LOG.error("Invalid PropagationActions specified: {}", className, e);
                }

                if (actionsClass == null || !isAssignable) {
                    context.buildConstraintViolationWithTemplate(
                            getTemplate(EntityViolationType.InvalidResource, "Invalid actions class name")).
                            addPropertyNode("actionsClassName").addConstraintViolation();
                    return false;
                }
            }
        }

        return isValid(resource.getUmapping(), context) && isValid(resource.getRmapping(), context);
    }
}
