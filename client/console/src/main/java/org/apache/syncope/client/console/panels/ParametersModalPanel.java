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
package org.apache.syncope.client.console.panels;

import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.rest.api.service.ConfigurationService;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;

public class ParametersModalPanel extends AbstractModalPanel<AttrTO> {

    private static final long serialVersionUID = 4024126489500665435L;

    private final AttrTO attrTO;

    public ParametersModalPanel(
            final BaseModal<AttrTO> modal,
            final AttrTO attrTO,
            final PageReference pageRef) {
        super(modal, pageRef);
        this.attrTO = attrTO;
        Fragment fragment;
        if (!attrTO.getValues().isEmpty()) {
            fragment = new Fragment("parametersModal", "detailsFragment", this);
            fragment.addOrReplace(new ParametersDetailsPanel("parametersDetailsPanel", getItem()));
        } else {
            fragment = new Fragment("parametersModal", "wizardFragment", this);
            fragment.addOrReplace(
                    new ParametersCreateWizardPanel("parametersCreateWizardPanel",
                            new ParametersCreateWizardPanel.ParametersForm(), pageRef).build(false));
        }
        add(fragment);
    }

    @Override
    public final AttrTO getItem() {
        return this.attrTO;
    }

    @Override
    public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
        try {
            SyncopeConsoleSession.get().getService(ConfigurationService.class).set(attrTO);
            info(getString(Constants.OPERATION_SUCCEEDED));
            modal.close(target);
        } catch (Exception e) {
            LOG.error("While creating or updating AttrTO", e);
            error(getString(Constants.ERROR) + ": " + e.getMessage());
        }
        modal.getNotificationPanel().refresh(target);
    }
}
