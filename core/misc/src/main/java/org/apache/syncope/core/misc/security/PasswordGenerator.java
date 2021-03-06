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
package org.apache.syncope.core.misc.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.types.PasswordPolicySpec;
import org.apache.syncope.core.persistence.api.dao.PolicyDAO;
import org.apache.syncope.core.persistence.api.entity.ExternalResource;
import org.apache.syncope.core.persistence.api.entity.PasswordPolicy;
import org.apache.syncope.core.persistence.api.entity.role.Role;
import org.apache.syncope.core.persistence.api.entity.user.User;
import org.apache.syncope.core.misc.policy.InvalidPasswordPolicySpecException;
import org.apache.syncope.core.misc.policy.PolicyPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generate random passwords according to given policies.
 *
 * @see PasswordPolicy
 */
@Component
public class PasswordGenerator {

    private static final char[] SPECIAL_CHARS = { '!', '£', '%', '&', '(', ')', '?', '#', '$' };

    @Autowired
    private PolicyDAO policyDAO;

    public String generate(final List<PasswordPolicySpec> ppSpecs)
            throws InvalidPasswordPolicySpecException {

        PasswordPolicySpec policySpec = merge(ppSpecs);

        check(policySpec);

        return generate(policySpec);
    }

    public String generate(final User user)
            throws InvalidPasswordPolicySpecException {

        List<PasswordPolicySpec> ppSpecs = new ArrayList<PasswordPolicySpec>();

        PasswordPolicy globalPP = policyDAO.getGlobalPasswordPolicy();
        if (globalPP != null && globalPP.getSpecification(PasswordPolicySpec.class) != null) {
            ppSpecs.add(globalPP.getSpecification(PasswordPolicySpec.class));
        }

        for (Role role : user.getRoles()) {
            if (role.getPasswordPolicy() != null
                    && role.getPasswordPolicy().getSpecification(PasswordPolicySpec.class) != null) {

                ppSpecs.add(role.getPasswordPolicy().getSpecification(PasswordPolicySpec.class));
            }
        }

        for (ExternalResource resource : user.getResources()) {
            if (resource.getPasswordPolicy() != null
                    && resource.getPasswordPolicy().getSpecification(PasswordPolicySpec.class) != null) {

                ppSpecs.add(resource.getPasswordPolicy().getSpecification(PasswordPolicySpec.class));
            }
        }

        PasswordPolicySpec policySpec = merge(ppSpecs);
        check(policySpec);
        return generate(policySpec);
    }

    private PasswordPolicySpec merge(final List<PasswordPolicySpec> ppSpecs) {
        PasswordPolicySpec fpps = new PasswordPolicySpec();
        fpps.setMinLength(0);
        fpps.setMaxLength(1000);

        for (PasswordPolicySpec policySpec : ppSpecs) {
            if (policySpec.getMinLength() > fpps.getMinLength()) {
                fpps.setMinLength(policySpec.getMinLength());
            }

            if ((policySpec.getMaxLength() != 0) && ((policySpec.getMaxLength() < fpps.getMaxLength()))) {
                fpps.setMaxLength(policySpec.getMaxLength());
            }
            fpps.getPrefixesNotPermitted().addAll(policySpec.getPrefixesNotPermitted());
            fpps.getSuffixesNotPermitted().addAll(policySpec.getSuffixesNotPermitted());

            if (!fpps.isNonAlphanumericRequired()) {
                fpps.setNonAlphanumericRequired(policySpec.isNonAlphanumericRequired());
            }

            if (!fpps.isAlphanumericRequired()) {
                fpps.setAlphanumericRequired(policySpec.isAlphanumericRequired());
            }
            if (!fpps.isDigitRequired()) {
                fpps.setDigitRequired(policySpec.isDigitRequired());
            }

            if (!fpps.isLowercaseRequired()) {
                fpps.setLowercaseRequired(policySpec.isLowercaseRequired());
            }
            if (!fpps.isUppercaseRequired()) {
                fpps.setUppercaseRequired(policySpec.isUppercaseRequired());
            }
            if (!fpps.isMustStartWithDigit()) {
                fpps.setMustStartWithDigit(policySpec.isMustStartWithDigit());
            }
            if (!fpps.isMustntStartWithDigit()) {
                fpps.setMustntStartWithDigit(policySpec.isMustntStartWithDigit());
            }
            if (!fpps.isMustEndWithDigit()) {
                fpps.setMustEndWithDigit(policySpec.isMustEndWithDigit());
            }
            if (fpps.isMustntEndWithDigit()) {
                fpps.setMustntEndWithDigit(policySpec.isMustntEndWithDigit());
            }
            if (!fpps.isMustStartWithAlpha()) {
                fpps.setMustStartWithAlpha(policySpec.isMustStartWithAlpha());
            }
            if (!fpps.isMustntStartWithAlpha()) {
                fpps.setMustntStartWithAlpha(policySpec.isMustntStartWithAlpha());
            }
            if (!fpps.isMustStartWithNonAlpha()) {
                fpps.setMustStartWithNonAlpha(policySpec.isMustStartWithNonAlpha());
            }
            if (!fpps.isMustntStartWithNonAlpha()) {
                fpps.setMustntStartWithNonAlpha(policySpec.isMustntStartWithNonAlpha());
            }
            if (!fpps.isMustEndWithNonAlpha()) {
                fpps.setMustEndWithNonAlpha(policySpec.isMustEndWithNonAlpha());
            }
            if (!fpps.isMustntEndWithNonAlpha()) {
                fpps.setMustntEndWithNonAlpha(policySpec.isMustntEndWithNonAlpha());
            }
            if (!fpps.isMustEndWithAlpha()) {
                fpps.setMustEndWithAlpha(policySpec.isMustEndWithAlpha());
            }
            if (!fpps.isMustntEndWithAlpha()) {
                fpps.setMustntEndWithAlpha(policySpec.isMustntEndWithAlpha());
            }
        }
        return fpps;
    }

    private void check(final PasswordPolicySpec policySpec)
            throws InvalidPasswordPolicySpecException {

        if (policySpec.getMinLength() == 0) {
            throw new InvalidPasswordPolicySpecException("Minimum length is zero");
        }
        if (policySpec.isMustEndWithAlpha() && policySpec.isMustntEndWithAlpha()) {
            throw new InvalidPasswordPolicySpecException(
                    "mustEndWithAlpha and mustntEndWithAlpha are both true");
        }
        if (policySpec.isMustEndWithAlpha() && policySpec.isMustEndWithDigit()) {
            throw new InvalidPasswordPolicySpecException(
                    "mustEndWithAlpha and mustEndWithDigit are both true");
        }
        if (policySpec.isMustEndWithDigit() && policySpec.isMustntEndWithDigit()) {
            throw new InvalidPasswordPolicySpecException(
                    "mustEndWithDigit and mustntEndWithDigit are both true");
        }
        if (policySpec.isMustEndWithNonAlpha() && policySpec.isMustntEndWithNonAlpha()) {
            throw new InvalidPasswordPolicySpecException(
                    "mustEndWithNonAlpha and mustntEndWithNonAlpha are both true");
        }
        if (policySpec.isMustStartWithAlpha() && policySpec.isMustntStartWithAlpha()) {
            throw new InvalidPasswordPolicySpecException(
                    "mustStartWithAlpha and mustntStartWithAlpha are both true");
        }
        if (policySpec.isMustStartWithAlpha() && policySpec.isMustStartWithDigit()) {
            throw new InvalidPasswordPolicySpecException(
                    "mustStartWithAlpha and mustStartWithDigit are both true");
        }
        if (policySpec.isMustStartWithDigit() && policySpec.isMustntStartWithDigit()) {
            throw new InvalidPasswordPolicySpecException(
                    "mustStartWithDigit and mustntStartWithDigit are both true");
        }
        if (policySpec.isMustStartWithNonAlpha() && policySpec.isMustntStartWithNonAlpha()) {
            throw new InvalidPasswordPolicySpecException(
                    "mustStartWithNonAlpha and mustntStartWithNonAlpha are both true");
        }
        if (policySpec.getMinLength() > policySpec.getMaxLength()) {
            throw new InvalidPasswordPolicySpecException("Minimun length (" + policySpec.getMinLength() + ")"
                    + "is greater than maximum length (" + policySpec.getMaxLength() + ")");
        }
    }

    private String generate(final PasswordPolicySpec policySpec) {
        String[] generatedPassword = new String[policySpec.getMinLength()];

        for (int i = 0; i < generatedPassword.length; i++) {
            generatedPassword[i] = "";
        }

        checkStartChar(generatedPassword, policySpec);

        checkEndChar(generatedPassword, policySpec);

        checkRequired(generatedPassword, policySpec);

        //filled empty chars
        for (int firstEmptyChar = firstEmptyChar(generatedPassword);
                firstEmptyChar < generatedPassword.length - 1; firstEmptyChar++) {
            generatedPassword[firstEmptyChar] = SecureRandomUtil.generateRandomLetter();
        }

        checkPrefixAndSuffix(generatedPassword, policySpec);

        return StringUtils.join(generatedPassword);
    }

    private void checkStartChar(final String[] generatedPassword, final PasswordPolicySpec policySpec) {
        if (policySpec.isMustStartWithAlpha()) {
            generatedPassword[0] = SecureRandomUtil.generateRandomLetter();
        }
        if (policySpec.isMustStartWithNonAlpha() || policySpec.isMustStartWithDigit()) {
            generatedPassword[0] = SecureRandomUtil.generateRandomNumber();
        }
        if (policySpec.isMustntStartWithAlpha()) {
            generatedPassword[0] = SecureRandomUtil.generateRandomNumber();

        }
        if (policySpec.isMustntStartWithDigit()) {
            generatedPassword[0] = SecureRandomUtil.generateRandomLetter();

        }
        if (policySpec.isMustntStartWithNonAlpha()) {
            generatedPassword[0] = SecureRandomUtil.generateRandomLetter();

        }
    }

    private void checkEndChar(final String[] generatedPassword, final PasswordPolicySpec policySpec) {
        if (policySpec.isMustEndWithAlpha()) {
            generatedPassword[policySpec.getMinLength() - 1] = SecureRandomUtil.generateRandomLetter();
        }
        if (policySpec.isMustEndWithNonAlpha() || policySpec.isMustEndWithDigit()) {
            generatedPassword[policySpec.getMinLength() - 1] = SecureRandomUtil.generateRandomNumber();
        }

        if (policySpec.isMustntEndWithAlpha()) {
            generatedPassword[policySpec.getMinLength() - 1] = SecureRandomUtil.generateRandomNumber();
        }
        if (policySpec.isMustntEndWithDigit()) {
            generatedPassword[policySpec.getMinLength() - 1] = SecureRandomUtil.generateRandomLetter();
        }
        if (policySpec.isMustntEndWithNonAlpha()) {
            generatedPassword[policySpec.getMinLength() - 1] = SecureRandomUtil.generateRandomLetter();

        }
    }

    private int firstEmptyChar(final String[] generatedPStrings) {
        int index = 0;
        while (!generatedPStrings[index].isEmpty()) {
            index++;
        }
        return index;
    }

    private void checkRequired(final String[] generatedPassword, final PasswordPolicySpec policySpec) {
        if (policySpec.isDigitRequired()
                && !PolicyPattern.DIGIT.matcher(StringUtils.join(generatedPassword)).matches()) {

            generatedPassword[firstEmptyChar(generatedPassword)] = SecureRandomUtil.generateRandomNumber();
        }

        if (policySpec.isUppercaseRequired()
                && !PolicyPattern.ALPHA_UPPERCASE.matcher(StringUtils.join(generatedPassword)).matches()) {

            generatedPassword[firstEmptyChar(generatedPassword)] = SecureRandomUtil.generateRandomLetter().toUpperCase();
        }

        if (policySpec.isLowercaseRequired()
                && !PolicyPattern.ALPHA_LOWERCASE.matcher(StringUtils.join(generatedPassword)).matches()) {

            generatedPassword[firstEmptyChar(generatedPassword)] = SecureRandomUtil.generateRandomLetter().toLowerCase();
        }

        if (policySpec.isNonAlphanumericRequired()
                && !PolicyPattern.NON_ALPHANUMERIC.matcher(StringUtils.join(generatedPassword)).matches()) {

            generatedPassword[firstEmptyChar(generatedPassword)] =
                    SecureRandomUtil.generateRandomSpecialCharacter(SPECIAL_CHARS);
        }
    }

    private void checkPrefixAndSuffix(final String[] generatedPassword, final PasswordPolicySpec policySpec) {
        for (String prefix : policySpec.getPrefixesNotPermitted()) {
            if (StringUtils.join(generatedPassword).startsWith(prefix)) {
                checkStartChar(generatedPassword, policySpec);
            }
        }

        for (String suffix : policySpec.getSuffixesNotPermitted()) {
            if (StringUtils.join(generatedPassword).endsWith(suffix)) {
                checkEndChar(generatedPassword, policySpec);
            }
        }
    }

}
