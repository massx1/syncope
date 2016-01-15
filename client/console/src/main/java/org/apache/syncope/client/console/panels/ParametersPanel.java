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

import static org.apache.wicket.Component.ENABLE;

import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.commons.SearchableDataProvider;
import org.apache.syncope.client.console.pages.BasePage;
import org.apache.syncope.client.console.panels.ParametersPanel.ParametersProvider;
import org.apache.syncope.client.console.rest.BaseRestClient;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLinksPanel;
import org.apache.syncope.client.console.wizards.AbstractModalPanelBuilder;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.client.console.wizards.WizardMgtPanel;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.syncope.common.rest.api.service.ConfigurationService;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class ParametersPanel extends AbstractSearchResultPanel<
        AttrTO, AttrTO, ParametersProvider, BaseRestClient> {

    private static final long serialVersionUID = 2765863608539154422L;

    public ParametersPanel(final String id, final PageReference pageRef) {
        super(id, new Builder<AttrTO, AttrTO, BaseRestClient>(null, pageRef) {

            private static final long serialVersionUID = 8769126634538601689L;

            @Override
            protected WizardMgtPanel<AttrTO> newInstance(final String id) {
                return new ParametersPanel(id, this);
            }
        });

        this.addNewItemPanelBuilder(new AbstractModalPanelBuilder<AttrTO>(
                BaseModal.CONTENT_ID, new AttrTO(), pageRef) {

            private static final long serialVersionUID = 1995192603527154740L;

            @Override
            public ModalPanel<AttrTO> build(final int index, final boolean edit) {
                return new ParametersModalPanel(modal, newModelObject(), pageRef);
            }

            @Override
            protected void onCancelInternal(final AttrTO modelObject) {
            }

            @Override
            protected Serializable onApplyInternal(final AttrTO modelObject) {
                return null;
            }
            
        }, true);

        setFooterVisibility(true);
        modal.addSumbitButton();
        modal.size(Modal.Size.Large);
        initResultTable();

        MetaDataRoleAuthorizationStrategy.authorize(addAjaxLink, ENABLE, StandardEntitlement.CONFIGURATION_SET);
    }

    public ParametersPanel(
            final String id, final Builder<AttrTO, AttrTO, BaseRestClient> builder) {
        super(id, builder);
    }

    @Override
    protected ParametersProvider dataProvider() {
        return new ParametersProvider(rows);
    }

    @Override
    protected String paginatorRowsKey() {
        return Constants.PREF_PARAMETERS_PAGINATOR_ROWS;
    }

    @Override
    protected Collection<ActionLink.ActionType> getBulkActions() {
        return Collections.<ActionLink.ActionType>emptyList();
    }

    @Override
    protected List<IColumn<AttrTO, String>> getColumns() {

        final List<IColumn<AttrTO, String>> columns = new ArrayList<>();

        for (final Field field : AttrTO.class.getDeclaredFields()) {

            if (field != null && !Modifier.isStatic(field.getModifiers())) {
                final String fieldName = field.getName();
                if (field.getType().isArray()) {
                    final IColumn<AttrTO, String> column = new PropertyColumn<AttrTO, String>(
                            new ResourceModel(field.getName()), field.getName()) {

                        private static final long serialVersionUID = 377850700587306254L;

                        @Override
                        public String getCssClass() {
                            String css = super.getCssClass();
                            if ("schema".equals(fieldName)) {
                                css = StringUtils.isBlank(css)
                                        ? "medium_fixedsize"
                                        : css + " medium_fixedsize";
                            }
                            return css;
                        }
                    };
                    columns.add(column);

                } else {
                    final IColumn<AttrTO, String> column = new PropertyColumn<AttrTO, String>(
                            new ResourceModel(field.getName()), field.getName(), field.getName()) {

                        private static final long serialVersionUID = -6902459669035442212L;

                        @Override
                        public String getCssClass() {
                            String css = super.getCssClass();
                            if ("schema".equals(fieldName)) {
                                css = StringUtils.isBlank(css)
                                        ? "medium_fixedsize"
                                        : css + " medium_fixedsize";
                            }
                            return css;
                        }
                    };
                    columns.add(column);
                }
            }
        }

        columns.add(new AbstractColumn<AttrTO, String>(new ResourceModel("actions", "")) {

            private static final long serialVersionUID = -3503023501954863131L;

            @Override
            public String getCssClass() {
                return "action";
            }

            @Override
            public void populateItem(
                    final Item<ICellPopulator<AttrTO>> item,
                    final String componentId,
                    final IModel<AttrTO> model) {

                final AttrTO attrTO = model.getObject();

                final ActionLinksPanel.Builder<Serializable> actionLinks
                        = ActionLinksPanel.builder(page.getPageReference());
                actionLinks.setDisableIndicator(true);
                ActionLinksPanel.Builder<Serializable> addWithRoles = actionLinks
                        .addWithRoles(new ActionLink<Serializable>() {

                            private static final long serialVersionUID = 3257738274365467945L;

                            @Override
                            public void onClick(final AjaxRequestTarget target, final Serializable ignore) {
                                send(ParametersPanel.this, Broadcast.EXACT,
                                        new AjaxWizard.EditItemActionEvent<>(model.getObject(), target));
                            }
                        }, ActionLink.ActionType.EDIT, StandardEntitlement.CONFIGURATION_SET)
                        .addWithRoles(new ActionLink<Serializable>() {

                            private static final long serialVersionUID = 3257738274365467945L;

                            @Override
                            public void onClick(final AjaxRequestTarget target, final Serializable ignore) {
                                try {
                                    SyncopeConsoleSession.get().getService(
                                            ConfigurationService.class).delete(attrTO.getSchema());
                                    info(getString(Constants.OPERATION_SUCCEEDED));
                                    target.add(container);
                                } catch (Exception e) {
                                    LOG.error("While deleting AttrTO", e);
                                    error(getString(Constants.ERROR) + ": " + e.getMessage());
                                }
                                ((BasePage) getPage()).getNotificationPanel().refresh(target);

                            }
                        }, ActionLink.ActionType.DELETE, StandardEntitlement.CONFIGURATION_DELETE);

                item.add(actionLinks.build(componentId));
            }
        });

        return columns;

    }

    protected final class ParametersProvider extends SearchableDataProvider<AttrTO> {

        private static final long serialVersionUID = -185944053385660794L;

        private ParametersProvider(final int paginatorRows) {
            super(paginatorRows);
        }

        @Override
        public Iterator<AttrTO> iterator(final long first, final long count) {
            final List<AttrTO> list = SyncopeConsoleSession.get().getService(ConfigurationService.class).list();
            return list.subList((int) first, (int) first + (int) count).iterator();
        }

        @Override
        public long size() {
            return SyncopeConsoleSession.get().getService(ConfigurationService.class).list().size();
        }

        @Override
        public IModel<AttrTO> model(final AttrTO object) {
            return new CompoundPropertyModel<>(object);
        }
    }
}
