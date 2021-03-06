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
package org.apache.syncope.common.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.mod.AbstractAttributableMod;
import org.apache.syncope.common.lib.mod.AbstractSubjectMod;
import org.apache.syncope.common.lib.mod.AttrMod;
import org.apache.syncope.common.lib.mod.MembershipMod;
import org.apache.syncope.common.lib.mod.ReferenceMod;
import org.apache.syncope.common.lib.mod.RoleMod;
import org.apache.syncope.common.lib.mod.UserMod;
import org.apache.syncope.common.lib.to.AbstractAttributableTO;
import org.apache.syncope.common.lib.to.AbstractSubjectTO;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.MembershipTO;
import org.apache.syncope.common.lib.to.RoleTO;
import org.apache.syncope.common.lib.to.UserTO;

/**
 * Utility class for manipulating classes extending AbstractAttributableTO and AbstractAttributableMod.
 *
 * @see AbstractAttributableTO
 * @see AbstractAttributableMod
 */
public final class AttributableOperations {

    private AttributableOperations() {
        // empty constructor for static utility classes
    }

    private static void populate(final Map<String, AttrTO> updatedAttrs,
            final Map<String, AttrTO> originalAttrs, final AbstractAttributableMod result) {

        populate(updatedAttrs, originalAttrs, result, false);
    }

    private static void populate(final Map<String, AttrTO> updatedAttrs,
            final Map<String, AttrTO> originalAttrs, final AbstractAttributableMod result,
            final boolean virtuals) {

        for (Map.Entry<String, AttrTO> entry : updatedAttrs.entrySet()) {
            AttrMod mod = new AttrMod();
            mod.setSchema(entry.getKey());

            Set<String> updatedValues = new HashSet<String>(entry.getValue().getValues());

            Set<String> originalValues = originalAttrs.containsKey(entry.getKey())
                    ? new HashSet<String>(originalAttrs.get(entry.getKey()).getValues())
                    : Collections.<String>emptySet();

            if (!originalAttrs.containsKey(entry.getKey())) {
                // SYNCOPE-459: take care of user virtual attributes without any value
                updatedValues.remove("");
                mod.getValuesToBeAdded().addAll(new ArrayList<String>(updatedValues));

                if (virtuals) {
                    result.getVirAttrsToUpdate().add(mod);
                } else {
                    result.getPlainAttrsToUpdate().add(mod);
                }
            } else if (!updatedValues.equals(originalValues)) {
                // avoid unwanted inputs
                updatedValues.remove("");
                if (!entry.getValue().isReadonly()) {
                    mod.getValuesToBeAdded().addAll(updatedValues);

                    if (!mod.isEmpty()) {
                        if (virtuals) {
                            result.getVirAttrsToRemove().add(mod.getSchema());
                        } else {
                            result.getPlainAttrsToRemove().add(mod.getSchema());
                        }
                    }
                }

                mod.getValuesToBeRemoved().addAll(originalValues);

                if (!mod.isEmpty()) {
                    if (virtuals) {
                        result.getVirAttrsToUpdate().add(mod);
                    } else {
                        result.getPlainAttrsToUpdate().add(mod);
                    }
                }
            }
        }
    }

    private static void diff(
            final AbstractAttributableTO updated,
            final AbstractAttributableTO original,
            final AbstractAttributableMod result,
            final boolean incremental) {

        // 1. check same id
        if (updated.getKey() != original.getKey()) {
            throw new IllegalArgumentException("AttributableTO's id must be the same");
        }
        result.setKey(updated.getKey());

        // 2. attributes
        Map<String, AttrTO> updatedAttrs = new HashMap<>(updated.getPlainAttrMap());
        Map<String, AttrTO> originalAttrs = new HashMap<>(original.getPlainAttrMap());

        Set<String> originalAttrNames = new HashSet<>(originalAttrs.keySet());
        originalAttrNames.removeAll(updatedAttrs.keySet());

        if (!incremental) {
            result.getPlainAttrsToRemove().clear();
            result.getPlainAttrsToRemove().addAll(originalAttrNames);
        }

        Set<String> emptyUpdatedAttrs = new HashSet<>();
        for (Map.Entry<String, AttrTO> entry : updatedAttrs.entrySet()) {
            if (entry.getValue().getValues() == null || entry.getValue().getValues().isEmpty()) {

                emptyUpdatedAttrs.add(entry.getKey());
            }
        }
        for (String emptyUpdatedAttr : emptyUpdatedAttrs) {
            updatedAttrs.remove(emptyUpdatedAttr);
            result.getPlainAttrsToRemove().add(emptyUpdatedAttr);
        }

        populate(updatedAttrs, originalAttrs, result);

        // 3. derived attributes
        updatedAttrs = updated.getDerAttrMap();
        originalAttrs = original.getDerAttrMap();

        originalAttrNames = new HashSet<>(originalAttrs.keySet());
        originalAttrNames.removeAll(updatedAttrs.keySet());

        if (!incremental) {
            result.getDerAttrsToRemove().clear();
            result.getDerAttrsToRemove().addAll(originalAttrNames);
        }

        Set<String> updatedAttrNames = new HashSet<>(updatedAttrs.keySet());
        updatedAttrNames.removeAll(originalAttrs.keySet());
        result.getDerAttrsToAdd().clear();
        result.getDerAttrsToAdd().addAll(updatedAttrNames);

        // 4. virtual attributes
        updatedAttrs = updated.getVirAttrMap();
        originalAttrs = original.getVirAttrMap();

        originalAttrNames = new HashSet<>(originalAttrs.keySet());
        originalAttrNames.removeAll(updatedAttrs.keySet());

        if (!incremental) {
            result.getVirAttrsToRemove().clear();
            result.getVirAttrsToRemove().addAll(originalAttrNames);
        }

        populate(updatedAttrs, originalAttrs, result, true);

        // 5. resources
        if (original instanceof AbstractSubjectTO && updated instanceof AbstractSubjectTO
                && result instanceof AbstractSubjectMod) {

            Set<String> updatedRes = new HashSet<>(((AbstractSubjectTO) updated).getResources());
            Set<String> originalRes = new HashSet<>(((AbstractSubjectTO) original).getResources());

            updatedRes.removeAll(originalRes);
            ((AbstractSubjectMod) result).getResourcesToAdd().clear();
            ((AbstractSubjectMod) result).getResourcesToAdd().addAll(updatedRes);

            originalRes.removeAll(((AbstractSubjectTO) updated).getResources());

            if (!incremental) {
                ((AbstractSubjectMod) result).getResourcesToRemove().clear();
                ((AbstractSubjectMod) result).getResourcesToRemove().addAll(originalRes);
            }
        }
    }

    /**
     * Calculate modifications needed by first in order to be equal to second.
     *
     * @param updated updated UserTO
     * @param original original UserTO
     * @return UserMod containing differences
     */
    public static UserMod diff(final UserTO updated, final UserTO original) {
        return diff(updated, original, false);
    }

    /**
     * Calculate modifications needed by first in order to be equal to second.
     *
     * @param updated updated UserTO
     * @param original original UserTO
     * @param incremental perform incremental diff (without removing existing info)
     * @return UserMod containing differences
     */
    public static UserMod diff(final UserTO updated, final UserTO original, final boolean incremental) {
        UserMod result = new UserMod();

        diff(updated, original, result, incremental);

        // 1. password
        if (updated.getPassword() != null && (original.getPassword() == null
                || !original.getPassword().equals(updated.getPassword()))) {

            result.setPassword(updated.getPassword());
        }

        // 2. username
        if (original.getUsername() != null && !original.getUsername().equals(updated.getUsername())) {
            result.setUsername(updated.getUsername());
        }

        // 3. security question / answer
        if (updated.getSecurityQuestion() == null) {
            result.setSecurityQuestion(null);
            result.setSecurityAnswer(null);
        } else if (!updated.getSecurityQuestion().equals(original.getSecurityQuestion())
                || StringUtils.isNotBlank(updated.getSecurityAnswer())) {

            result.setSecurityQuestion(updated.getSecurityQuestion());
            result.setSecurityAnswer(updated.getSecurityAnswer());
        }

        // 4. memberships
        Map<Long, MembershipTO> updatedMembs = updated.getMembershipMap();
        Map<Long, MembershipTO> originalMembs = original.getMembershipMap();

        for (Map.Entry<Long, MembershipTO> entry : updatedMembs.entrySet()) {
            MembershipMod membMod = new MembershipMod();
            membMod.setRole(entry.getValue().getRoleId());

            if (originalMembs.containsKey(entry.getKey())) {
                // if memberships are actually same, just make the isEmpty() call below succeed
                if (entry.getValue().equals(originalMembs.get(entry.getKey()))) {
                    membMod.setRole(0);
                } else {
                    diff(entry.getValue(), originalMembs.get(entry.getKey()), membMod, false);
                }
            } else {
                for (AttrTO attr : entry.getValue().getPlainAttrs()) {
                    AttrMod attrMod = new AttrMod();
                    attrMod.setSchema(attr.getSchema());
                    attrMod.getValuesToBeAdded().addAll(attr.getValues());

                    if (!attrMod.isEmpty()) {
                        membMod.getPlainAttrsToUpdate().add(attrMod);
                        membMod.getPlainAttrsToRemove().add(attrMod.getSchema());
                    }
                }
                for (AttrTO attr : entry.getValue().getDerAttrs()) {
                    membMod.getDerAttrsToAdd().add(attr.getSchema());
                }
                for (AttrTO attr : entry.getValue().getVirAttrs()) {
                    AttrMod attrMod = new AttrMod();
                    attrMod.setSchema(attr.getSchema());
                    attrMod.getValuesToBeAdded().addAll(attr.getValues());

                    if (!attrMod.isEmpty()) {
                        membMod.getVirAttrsToUpdate().add(attrMod);
                        membMod.getPlainAttrsToRemove().add(attrMod.getSchema());
                    }
                }
            }

            if (!membMod.isEmpty()) {
                result.getMembershipsToAdd().add(membMod);
            }
        }

        if (!incremental) {
            Set<Long> originalRoles = new HashSet<>(originalMembs.keySet());
            originalRoles.removeAll(updatedMembs.keySet());
            for (Long roleId : originalRoles) {
                result.getMembershipsToRemove().add(originalMembs.get(roleId).getKey());
            }
        }

        return result;
    }

    /**
     * Calculate modifications needed by first in order to be equal to second.
     *
     * @param updated updated RoleTO
     * @param original original RoleTO
     * @return RoleMod containing differences
     */
    public static RoleMod diff(final RoleTO updated, final RoleTO original) {
        return diff(updated, original, false);
    }

    /**
     * Calculate modifications needed by first in order to be equal to second.
     *
     * @param updated updated RoleTO
     * @param original original RoleTO
     * @param incremental perform incremental diff (without removing existing info)
     * @return RoleMod containing differences
     */
    public static RoleMod diff(final RoleTO updated, final RoleTO original, final boolean incremental) {
        RoleMod result = new RoleMod();

        diff(updated, original, result, incremental);

        // 1. inheritance
        result.setInheritOwner(updated.isInheritOwner());
        result.setInheritTemplates(updated.isInheritTemplates());
        result.setInheritAccountPolicy(updated.isInheritAccountPolicy());
        result.setInheritPasswordPolicy(updated.isInheritPasswordPolicy());
        result.setInheritPlainAttrs(updated.isInheritPlainAttrs());
        result.setInheritDerAttrs(updated.isInheritDerAttrs());
        result.setInheritVirAttrs(updated.isInheritVirAttrs());

        // 2. policies
        result.setAccountPolicy(new ReferenceMod(updated.getAccountPolicy()));
        result.setPasswordPolicy(new ReferenceMod(updated.getPasswordPolicy()));

        // 3. name
        if (!original.getName().equals(updated.getName())) {
            result.setName(updated.getName());
        }

        // 4. entitlements
        Set<String> updatedEnts = new HashSet<>(updated.getEntitlements());
        Set<String> originalEnts = new HashSet<>(original.getEntitlements());
        if (updatedEnts.equals(originalEnts)) {
            result.setModEntitlements(false);
            result.getEntitlements().clear();
        } else {
            result.setModEntitlements(true);
            result.getEntitlements().addAll(updated.getEntitlements());
        }

        // 5. templates
        Set<String> updatedTemplates = new HashSet<>(updated.getRPlainAttrTemplates());
        Set<String> originalTemplates = new HashSet<>(original.getRPlainAttrTemplates());
        if (updatedTemplates.equals(originalTemplates)) {
            result.setModRAttrTemplates(false);
            result.getRPlainAttrTemplates().clear();
        } else {
            result.setModRAttrTemplates(true);
            result.getRPlainAttrTemplates().addAll(updated.getRPlainAttrTemplates());
        }
        updatedTemplates = new HashSet<>(updated.getRDerAttrTemplates());
        originalTemplates = new HashSet<>(original.getRDerAttrTemplates());
        if (updatedTemplates.equals(originalTemplates)) {
            result.setModRDerAttrTemplates(false);
            result.getRDerAttrTemplates().clear();
        } else {
            result.setModRDerAttrTemplates(true);
            result.getRDerAttrTemplates().addAll(updated.getRDerAttrTemplates());
        }
        updatedTemplates = new HashSet<>(updated.getRVirAttrTemplates());
        originalTemplates = new HashSet<>(original.getRVirAttrTemplates());
        if (updatedTemplates.equals(originalTemplates)) {
            result.setModRVirAttrTemplates(false);
            result.getRVirAttrTemplates().clear();
        } else {
            result.setModRVirAttrTemplates(true);
            result.getRVirAttrTemplates().addAll(updated.getRVirAttrTemplates());
        }
        updatedTemplates = new HashSet<>(updated.getMPlainAttrTemplates());
        originalTemplates = new HashSet<>(original.getMPlainAttrTemplates());
        if (updatedTemplates.equals(originalTemplates)) {
            result.setModMAttrTemplates(false);
            result.getMPlainAttrTemplates().clear();
        } else {
            result.setModMAttrTemplates(true);
            result.getMPlainAttrTemplates().addAll(updated.getMPlainAttrTemplates());
        }
        updatedTemplates = new HashSet<>(updated.getMDerAttrTemplates());
        originalTemplates = new HashSet<>(original.getMDerAttrTemplates());
        if (updatedTemplates.equals(originalTemplates)) {
            result.setModMDerAttrTemplates(false);
            result.getMDerAttrTemplates().clear();
        } else {
            result.setModMDerAttrTemplates(true);
            result.getMDerAttrTemplates().addAll(updated.getMDerAttrTemplates());
        }
        updatedTemplates = new HashSet<>(updated.getMVirAttrTemplates());
        originalTemplates = new HashSet<>(original.getMVirAttrTemplates());
        if (updatedTemplates.equals(originalTemplates)) {
            result.setModMVirAttrTemplates(false);
            result.getMVirAttrTemplates().clear();
        } else {
            result.setModMVirAttrTemplates(true);
            result.getMVirAttrTemplates().addAll(updated.getMVirAttrTemplates());
        }

        // 6. owner
        result.setUserOwner(new ReferenceMod(updated.getUserOwner()));
        result.setRoleOwner(new ReferenceMod(updated.getRoleOwner()));

        return result;
    }

    private static List<AttrTO> getUpdateValues(final Map<String, AttrTO> attrs,
            final Set<String> attrsToBeRemoved, final Set<AttrMod> attrsToBeUpdated) {

        Map<String, AttrTO> rwattrs = new HashMap<>(attrs);
        for (String attrName : attrsToBeRemoved) {
            rwattrs.remove(attrName);
        }
        for (AttrMod attrMod : attrsToBeUpdated) {
            if (rwattrs.containsKey(attrMod.getSchema())) {
                AttrTO attrTO = rwattrs.get(attrMod.getSchema());
                attrTO.getValues().removeAll(attrMod.getValuesToBeRemoved());
                attrTO.getValues().addAll(attrMod.getValuesToBeAdded());
            } else {
                AttrTO attrTO = new AttrTO();
                attrTO.setSchema(attrMod.getSchema());
                attrTO.getValues().addAll(attrMod.getValuesToBeAdded());

                rwattrs.put(attrMod.getSchema(), attrTO);
            }
        }

        return new ArrayList<>(rwattrs.values());
    }

    private static <T extends AbstractAttributableTO, K extends AbstractAttributableMod> void apply(final T to,
            final K mod, final T result) {

        // 1. attributes
        result.getPlainAttrs().addAll(getUpdateValues(to.getPlainAttrMap(),
                mod.getPlainAttrsToRemove(), mod.getPlainAttrsToUpdate()));

        // 2. derived attributes
        Map<String, AttrTO> attrs = to.getDerAttrMap();
        for (String attrName : mod.getDerAttrsToRemove()) {
            attrs.remove(attrName);
        }
        for (String attrName : mod.getDerAttrsToAdd()) {
            AttrTO attrTO = new AttrTO();
            attrTO.setSchema(attrName);

            attrs.put(attrName, attrTO);
        }
        result.getDerAttrs().addAll(attrs.values());

        // 3. virtual attributes
        result.getVirAttrs().addAll(getUpdateValues(to.getVirAttrMap(),
                mod.getVirAttrsToRemove(), mod.getVirAttrsToUpdate()));

        // 4. resources
        if (result instanceof AbstractSubjectTO && mod instanceof AbstractSubjectMod) {
            ((AbstractSubjectTO) result).getResources().removeAll(((AbstractSubjectMod) mod).getResourcesToRemove());
            ((AbstractSubjectTO) result).getResources().addAll(((AbstractSubjectMod) mod).getResourcesToAdd());
        }
    }

    public static UserTO apply(final UserTO userTO, final UserMod userMod) {
        // 1. check same id
        if (userTO.getKey() != userMod.getKey()) {
            throw new IllegalArgumentException("UserTO and UserMod ids must be the same");
        }

        UserTO result = SerializationUtils.clone(userTO);
        apply(userTO, userMod, result);

        // 1. password
        result.setPassword(userMod.getPassword());

        // 2. username
        if (userMod.getUsername() != null) {
            result.setUsername(userMod.getUsername());
        }
        // 3. memberships
        Map<Long, MembershipTO> membs = result.getMembershipMap();
        for (Long membId : userMod.getMembershipsToRemove()) {
            result.getMemberships().remove(membs.get(membId));
        }
        for (MembershipMod membMod : userMod.getMembershipsToAdd()) {
            MembershipTO membTO = new MembershipTO();
            membTO.setRoleId(membMod.getRole());

            apply(membTO, membMod, membTO);
        }

        return result;
    }
}
