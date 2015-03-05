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

import static org.apache.wicket.markup.html.panel.Panel.PANEL;

import java.util.ArrayList;
import java.util.List;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.pages.RoleTemplateModalPage;
import org.apache.syncope.client.console.pages.SyncTaskModalPage;
import org.apache.syncope.client.console.pages.UserTemplateModalPage;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.ActionColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.DatePropertyColumn;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLinksPanel;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.AbstractTaskTO;
import org.apache.syncope.common.lib.to.SyncTaskTO;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

public class SyncTasksPanel extends AbstractProvisioningTasksPanel<SyncTaskTO> {

    private static final long serialVersionUID = 53189199346016099L;

    public SyncTasksPanel(final String id, final PageReference pageRef) {
        super(id, pageRef, SyncTaskTO.class);
        initTasksTable();
    }

    @Override
    protected List<IColumn<AbstractTaskTO, String>> getColumns() {
        final List<IColumn<AbstractTaskTO, String>> syncTaskscolumns = new ArrayList<>();

        syncTaskscolumns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("key", this, null), "key", "key"));
        syncTaskscolumns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("name", this, null), "name", "name"));
        syncTaskscolumns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("description", this, null), "description", "description"));
        syncTaskscolumns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("resourceName", this, null), "resource", "resource"));
        syncTaskscolumns.add(new DatePropertyColumn<AbstractTaskTO>(
                new StringResourceModel("lastExec", this, null), "lastExec", "lastExec"));
        syncTaskscolumns.add(new DatePropertyColumn<AbstractTaskTO>(
                new StringResourceModel("nextExec", this, null), "nextExec", "nextExec"));
        syncTaskscolumns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("latestExecStatus", this, null), "latestExecStatus", "latestExecStatus"));

        syncTaskscolumns.add(
                new ActionColumn<AbstractTaskTO, String>(new StringResourceModel("actions", this, null, "")) {

                    private static final long serialVersionUID = 2054811145491901166L;

                    @Override
                    public ActionLinksPanel getActions(final String componentId, final IModel<AbstractTaskTO> model) {

                        final SyncTaskTO taskTO = (SyncTaskTO) model.getObject();

                        final ActionLinksPanel panel = new ActionLinksPanel(componentId, model, pageRef);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {

                                window.setPageCreator(new ModalWindow.PageCreator() {

                                    private static final long serialVersionUID = -7834632442532690940L;

                                    @Override
                                    public Page createPage() {
                                        return new SyncTaskModalPage(window, taskTO, pageRef);
                                    }
                                });

                                window.show(target);
                            }
                        }, ActionLink.ActionType.EDIT, TASKS);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {

                                window.setPageCreator(new ModalWindow.PageCreator() {

                                    private static final long serialVersionUID = -7834632442532690940L;

                                    @Override
                                    public Page createPage() {
                                        return new UserTemplateModalPage(pageRef, window, taskTO);
                                    }
                                });

                                window.show(target);
                            }
                        }, ActionLink.ActionType.USER_TEMPLATE, TASKS);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {

                                window.setPageCreator(new ModalWindow.PageCreator() {

                                    private static final long serialVersionUID = -7834632442532690940L;

                                    @Override
                                    public Page createPage() {
                                        return new RoleTemplateModalPage(pageRef, window, taskTO);
                                    }
                                });

                                window.show(target);
                            }
                        }, ActionLink.ActionType.ROLE_TEMPLATE, TASKS);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {
                                try {
                                    taskRestClient.startExecution(taskTO.getKey(), false);
                                    getSession().info(getString(Constants.OPERATION_SUCCEEDED));
                                } catch (SyncopeClientException scce) {
                                    error(scce.getMessage());
                                }

                                target.add(container);
                                ((NotificationPanel) getPage().get(Constants.FEEDBACK)).refresh(target);
                            }
                        }, ActionLink.ActionType.EXECUTE, TASKS);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {
                                try {
                                    taskRestClient.startExecution(taskTO.getKey(), true);
                                    getSession().info(getString(Constants.OPERATION_SUCCEEDED));
                                } catch (SyncopeClientException scce) {
                                    error(scce.getMessage());
                                }

                                target.add(container);
                                ((NotificationPanel) getPage().get(Constants.FEEDBACK)).refresh(target);
                            }
                        }, ActionLink.ActionType.DRYRUN, TASKS);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {
                                try {
                                    taskRestClient.delete(taskTO.getKey(), SyncTaskTO.class);
                                    info(getString(Constants.OPERATION_SUCCEEDED));
                                } catch (SyncopeClientException scce) {
                                    error(scce.getMessage());
                                }
                                target.add(container);
                                ((NotificationPanel) getPage().get(Constants.FEEDBACK)).refresh(target);
                            }
                        }, ActionLink.ActionType.DELETE, TASKS);

                        return panel;
                    }

                    @Override
                    public Component getHeader(final String componentId) {
                        final ActionLinksPanel panel = new ActionLinksPanel(componentId, new Model(), pageRef);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -7978723352517770644L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {
                                if (target != null) {
                                    target.add(table);
                                }
                            }
                        }, ActionLink.ActionType.RELOAD, TASKS, "list");

                        return panel;
                    }
                });

        syncTaskscolumns.add(
                new ActionColumn<AbstractTaskTO, String>(new StringResourceModel("jobs", this, null, "")) {

                    private static final long serialVersionUID = 2054811145491901166L;

                    @Override
                    public ActionLinksPanel getActions(final String componentId, final IModel<AbstractTaskTO> model) {

                        final SyncTaskTO taskTO = (SyncTaskTO) model.getObject();

                        final ActionLinksPanel panel = new ActionLinksPanel(componentId, model, pageRef);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {
                                try {
                                    jobRestClient.pause(taskTO.getKey());
                                    getSession().info(getString(Constants.OPERATION_SUCCEEDED));
                                } catch (SyncopeClientException scce) {
                                    error(scce.getMessage());
                                }

                                target.add(container);
                                ((NotificationPanel) getPage().get(Constants.FEEDBACK)).refresh(target);
                            }
                        }, ActionLink.ActionType.SUSPEND, TASKS);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {
                                try {
                                    jobRestClient.pause(taskTO.getKey());
                                    getSession().info(getString(Constants.OPERATION_SUCCEEDED));
                                } catch (SyncopeClientException scce) {
                                    error(scce.getMessage());
                                }

                                target.add(container);
                                ((NotificationPanel) getPage().get(Constants.FEEDBACK)).refresh(target);
                            }
                        }, ActionLink.ActionType.REACTIVATE, TASKS);

                        panel.add(new Label("jobStatus", jobRestClient.status(taskTO.getKey())));

                        //bulkActions.add(ActionType.DELETE);
                        return panel;
                    }

                    @Override
                    public Component getHeader(final String componentId) {
                        final ActionLinksPanel panel = new ActionLinksPanel(componentId, new Model(), pageRef);

                        panel.add(new ActionLink() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target) {
                                if (target != null) {
                                    target.add(table);
                                }
                            }
                        }, ActionLink.ActionType.RELOAD, TASKS, "list");

                        return panel;
                    }
                });

        return syncTaskscolumns;
    }
}
