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
package org.apache.syncope.common.lib.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.syncope.common.lib.to.AbstractAttributableTO;
import org.apache.syncope.common.lib.to.RoleTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.SubjectType;

public class SearchableFields {

    protected static final String[] ATTRIBUTES_NOTINCLUDED = {
        "serialVersionUID", "password"
    };

    public static final List<String> get(final SubjectType subjectType) {
        return get(subjectType == SubjectType.USER
                ? UserTO.class
                : RoleTO.class);
    }

    public static final List<String> get(final Class<? extends AbstractAttributableTO> attributableRef) {
        final List<String> fieldNames = new ArrayList<>();

        // loop on class and all superclasses searching for field
        Class<?> clazz = attributableRef;
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!ArrayUtils.contains(ATTRIBUTES_NOTINCLUDED, field.getName())
                        && !Collection.class.isAssignableFrom(field.getType())
                        && !Map.class.isAssignableFrom(field.getType())) {

                    fieldNames.add(field.getName());
                }
            }
            clazz = clazz.getSuperclass();
        }

        Collections.reverse(fieldNames);
        return fieldNames;
    }

    private SearchableFields() {
        // empty constructor for static utility class
    }
}
