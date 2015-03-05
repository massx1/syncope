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

import java.util.ArrayList;
import java.util.List;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.pages.SchedTaskModalPage;
import org.apache.syncope.client.console.pages.Tasks;
import org.apache.syncope.client.console.pages.Tasks.TasksProvider;
import org.apache.syncope.client.console.wicket.ajax.markup.html.ClearIndicatingAjaxLink;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.ActionColumn;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.DatePropertyColumn;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLinksPanel;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.AbstractTaskTO;
import org.apache.syncope.common.lib.to.SchedTaskTO;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.http.WebResponse;

public class SchedTasks extends AbstractTasks {

    private static final long serialVersionUID = 525486152284253354L;

    private int paginatorRows;

    private WebMarkupContainer container;

    private ModalWindow window;

    private AjaxDataTablePanel<AbstractTaskTO, String> table;

    public SchedTasks(final String id, final PageReference pageRef) {
        super(id, pageRef);

        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        window = new ModalWindow("taskWin");
        window.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        window.setInitialHeight(WIN_HEIGHT);
        window.setInitialWidth(WIN_WIDTH);
        window.setCookieName(VIEW_TASK_WIN_COOKIE_NAME);
        add(window);

        ((Tasks) pageRef.getPage()).setWindowClosedCallback(window, container);

        paginatorRows = prefMan.getPaginatorRows(getWebRequest(), Constants.PREF_SCHED_TASKS_PAGINATOR_ROWS);

        table = Tasks.updateTaskTable(getColumns(),
                new TasksProvider<SchedTaskTO>(taskRestClient, jobRestClient, paginatorRows, getId(), SchedTaskTO.class),
                container,
                0,
                pageRef,
                taskRestClient);

        container.add(table);

        @SuppressWarnings("rawtypes")
        Form paginatorForm = new Form("PaginatorForm");

        @SuppressWarnings({"unchecked", "rawtypes"})
        final DropDownChoice rowsChooser = new DropDownChoice("rowsChooser", new PropertyModel(this, "paginatorRows"),
                prefMan.getPaginatorChoices());

        rowsChooser.add(new AjaxFormComponentUpdatingBehavior(Constants.ON_CHANGE) {

            private static final long serialVersionUID = -1107858522700306810L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                prefMan.set(getWebRequest(), (WebResponse) getResponse(), Constants.PREF_SCHED_TASKS_PAGINATOR_ROWS,
                        String.valueOf(paginatorRows));

                table = Tasks.updateTaskTable(getColumns(),
                        new TasksProvider<SchedTaskTO>(taskRestClient, jobRestClient, paginatorRows, getId(),
                                SchedTaskTO.class),
                        container,
                        table == null ? 0 : (int) table.getCurrentPage(),
                        pageRef,
                        taskRestClient);

                target.add(container);
            }
        });

        paginatorForm.add(rowsChooser);
        add(paginatorForm);

        AjaxLink createLink = new ClearIndicatingAjaxLink("createLink", pageRef) {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            protected void onClickInternal(final AjaxRequestTarget target) {
                window.setPageCreator(new ModalWindow.PageCreator() {

                    private static final long serialVersionUID = -7834632442532690940L;

                    @Override
                    public Page createPage() {
                        return new SchedTaskModalPage(window, new SchedTaskTO(), pageRef);
                    }
                });

                window.show(target);
            }
        };

        MetaDataRoleAuthorizationStrategy.authorize(
                createLink, RENDER, xmlRolesReader.getEntitlement(TASKS, "create"));

        add(createLink);
    }

    private List<IColumn<AbstractTaskTO, String>> getColumns() {
        final List<IColumn<AbstractTaskTO, String>> columns = new ArrayList<IColumn<AbstractTaskTO, String>>();

        columns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("key", this, null), "key", "key"));
        columns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("name", this, null), "name", "name"));
        columns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("description", this, null), "description", "description"));
        columns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("class", this, null), "jobClassName", "jobClassName"));
        columns.add(new DatePropertyColumn<AbstractTaskTO>(
                new StringResourceModel("lastExec", this, null), "lastExec", "lastExec"));
        columns.add(new DatePropertyColumn<AbstractTaskTO>(
                new StringResourceModel("nextExec", this, null), "nextExec", "nextExec"));
        columns.add(new PropertyColumn<AbstractTaskTO, String>(
                new StringResourceModel("latestExecStatus", this, null), "latestExecStatus", "latestExecStatus"));

        columns.add(new ActionColumn<AbstractTaskTO, String>(new StringResourceModel("actions", this, null, "")) {

            private static final long serialVersionUID = 2054811145491901166L;

            @Override
            public ActionLinksPanel getActions(final String componentId, final IModel<AbstractTaskTO> model) {

                final SchedTaskTO taskTO = (SchedTaskTO) model.getObject();

                final ActionLinksPanel panel = new ActionLinksPanel(componentId, model, pageRef);

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        window.setPageCreator(new ModalWindow.PageCreator() {

                            private static final long serialVersionUID = -7834632442532690940L;

                            @Override
                            public Page createPage() {
                                return new SchedTaskModalPage(window, taskTO, pageRef);
                            }
                        });

                        window.show(target);
                    }
                }, ActionLink.ActionType.EDIT, TASKS);

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

                        ((NotificationPanel) getPage().get(Constants.FEEDBACK)).refresh(target);
                        target.add(container);
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

                        ((NotificationPanel) getPage().get(Constants.FEEDBACK)).refresh(target);
                        target.add(container);
                    }
                }, ActionLink.ActionType.DRYRUN, TASKS);

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        try {
                            taskRestClient.delete(taskTO.getKey(), SchedTaskTO.class);
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
                @SuppressWarnings("rawtypes")
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
        return columns;
    }

    @Override
    public void onEvent(final IEvent<?> event) {
        if (event.getPayload() instanceof AbstractSearchResultPanel.EventDataWrapper) {
            ((AbstractSearchResultPanel.EventDataWrapper) event.getPayload()).getTarget().add(container);
        }
    }
}
