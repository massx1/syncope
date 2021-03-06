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
package org.apache.syncope.client.console.pages;

import static org.apache.syncope.client.console.pages.AbstractBasePage.LOG;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.syncope.client.console.ExtensionPanel;
import org.apache.syncope.client.console.commons.AttrLayoutType;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.commons.HttpResourceStream;
import org.apache.syncope.client.console.commons.PreferenceManager;
import org.apache.syncope.client.console.commons.SortableDataProviderComparator;
import org.apache.syncope.client.console.init.ImplementationClassNamesLoader;
import org.apache.syncope.client.console.panels.AbstractExtensionPanel;
import org.apache.syncope.client.console.panels.JQueryUITabbedPanel;
import org.apache.syncope.client.console.panels.LayoutsPanel;
import org.apache.syncope.client.console.panels.PoliciesPanel;
import org.apache.syncope.client.console.rest.LoggerRestClient;
import org.apache.syncope.client.console.rest.NotificationRestClient;
import org.apache.syncope.client.console.rest.SecurityQuestionRestClient;
import org.apache.syncope.client.console.rest.WorkflowRestClient;
import org.apache.syncope.client.console.wicket.extensions.markup.html.repeater.data.table.CollectionPropertyColumn;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLinksPanel;
import org.apache.syncope.client.console.wicket.markup.html.link.VeilPopupSettings;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.to.LoggerTO;
import org.apache.syncope.common.lib.to.NotificationTO;
import org.apache.syncope.common.lib.to.SecurityQuestionTO;
import org.apache.syncope.common.lib.types.LoggerLevel;
import org.apache.syncope.common.lib.types.PolicyType;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Configurations WebPage.
 */
public class Configuration extends BasePage {

    private static final long serialVersionUID = -2838270869037702214L;

    private static final int SYNCOPECONF_WIN_HEIGHT = 300;

    private static final int SYNCOPECONF_WIN_WIDTH = 900;

    private static final int NOTIFICATION_WIN_HEIGHT = 500;

    private static final int NOTIFICATION_WIN_WIDTH = 1100;

    private static final int SECURITY_QUESTION_WIN_HEIGHT = 300;

    private static final int SECURITY_QUESTION_WIN_WIDTH = 900;

    @SpringBean
    private LoggerRestClient loggerRestClient;

    @SpringBean
    private NotificationRestClient notificationRestClient;

    @SpringBean
    private SecurityQuestionRestClient securityQuestionRestClient;

    @SpringBean
    private WorkflowRestClient wfRestClient;

    @SpringBean
    private PreferenceManager prefMan;

    @SpringBean
    private ImplementationClassNamesLoader implementationClassNamesLoader;

    private final ModalWindow syncopeConfWin;

    private final ModalWindow createNotificationWin;

    private final ModalWindow editNotificationWin;

    private final ModalWindow createSecurityQuestionWin;

    private final ModalWindow editSecurityQuestionWin;

    private WebMarkupContainer notificationContainer;

    private WebMarkupContainer securityQuestionContainer;

    private int notificationPaginatorRows;

    public Configuration() {
        super();

        // Layouts
        add(new LayoutsPanel("adminUserLayoutPanel", AttrLayoutType.ADMIN_USER, feedbackPanel));
        add(new LayoutsPanel("selfUserLayoutPanel", AttrLayoutType.SELF_USER, feedbackPanel));
        add(new LayoutsPanel("adminRoleLayoutPanel", AttrLayoutType.ADMIN_ROLE, feedbackPanel));
        add(new LayoutsPanel("selfRoleLayoutPanel", AttrLayoutType.SELF_ROLE, feedbackPanel));
        add(new LayoutsPanel("adminMembershipLayoutPanel", AttrLayoutType.ADMIN_MEMBERSHIP, feedbackPanel));
        add(new LayoutsPanel("selfMembershipLayoutPanel", AttrLayoutType.SELF_MEMBERSHIP, feedbackPanel));

        add(syncopeConfWin = new ModalWindow("syncopeConfWin"));
        syncopeConfWin.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        syncopeConfWin.setInitialHeight(SYNCOPECONF_WIN_HEIGHT);
        syncopeConfWin.setInitialWidth(SYNCOPECONF_WIN_WIDTH);
        syncopeConfWin.setCookieName("syncopeconf-modal");
        setupSyncopeConf();

        add(new PoliciesPanel("passwordPoliciesPanel", getPageReference(), PolicyType.PASSWORD));
        add(new PoliciesPanel("accountPoliciesPanel", getPageReference(), PolicyType.ACCOUNT));
        add(new PoliciesPanel("syncPoliciesPanel", getPageReference(), PolicyType.SYNC));

        add(createNotificationWin = new ModalWindow("createNotificationWin"));
        createNotificationWin.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        createNotificationWin.setInitialHeight(NOTIFICATION_WIN_HEIGHT);
        createNotificationWin.setInitialWidth(NOTIFICATION_WIN_WIDTH);
        createNotificationWin.setCookieName("create-notification-modal");
        add(editNotificationWin = new ModalWindow("editNotificationWin"));
        editNotificationWin.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        editNotificationWin.setInitialHeight(NOTIFICATION_WIN_HEIGHT);
        editNotificationWin.setInitialWidth(NOTIFICATION_WIN_WIDTH);
        editNotificationWin.setCookieName("edit-notification-modal");
        setupNotification();

        add(createSecurityQuestionWin = new ModalWindow("createSecurityQuestionWin"));
        createSecurityQuestionWin.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        createSecurityQuestionWin.setInitialHeight(SECURITY_QUESTION_WIN_HEIGHT);
        createSecurityQuestionWin.setInitialWidth(SECURITY_QUESTION_WIN_WIDTH);
        createSecurityQuestionWin.setCookieName("create-security-question-modal");
        add(editSecurityQuestionWin = new ModalWindow("editSecurityQuestionWin"));
        editSecurityQuestionWin.setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        editSecurityQuestionWin.setInitialHeight(SECURITY_QUESTION_WIN_HEIGHT);
        editSecurityQuestionWin.setInitialWidth(SECURITY_QUESTION_WIN_WIDTH);
        editSecurityQuestionWin.setCookieName("edit-security-question-modal");
        setupSecurityQuestion();

        // Workflow definition stuff
        WebMarkupContainer noActivitiEnabledForUsers = new WebMarkupContainer("noActivitiEnabledForUsers");
        noActivitiEnabledForUsers.setOutputMarkupPlaceholderTag(true);
        add(noActivitiEnabledForUsers);

        WebMarkupContainer workflowDefContainer = new WebMarkupContainer("workflowDefContainer");
        workflowDefContainer.setOutputMarkupPlaceholderTag(true);

        if (wfRestClient.isActivitiEnabledForUsers()) {
            noActivitiEnabledForUsers.setVisible(false);
        } else {
            workflowDefContainer.setVisible(false);
        }

        BookmarkablePageLink<Void> activitiModeler =
                new BookmarkablePageLink<>("activitiModeler", ActivitiModelerPopupPage.class);
        activitiModeler.setPopupSettings(new VeilPopupSettings().setHeight(600).setWidth(800));
        MetaDataRoleAuthorizationStrategy.authorize(activitiModeler, ENABLE,
                xmlRolesReader.getEntitlement("Configuration", "workflowDefRead"));
        workflowDefContainer.add(activitiModeler);
        // Check if Activiti Modeler directory is found
        boolean activitiModelerEnabled = false;
        try {
            String activitiModelerDirectory = WebApplicationContextUtils.getWebApplicationContext(
                    WebApplication.get().getServletContext()).getBean("activitiModelerDirectory", String.class);
            File baseDir = new File(activitiModelerDirectory);
            activitiModelerEnabled = baseDir.exists() && baseDir.canRead() && baseDir.isDirectory();
        } catch (Exception e) {
            LOG.error("Could not check for Activiti Modeler directory", e);
        }
        activitiModeler.setEnabled(activitiModelerEnabled);

        BookmarkablePageLink<Void> xmlEditor = new BookmarkablePageLink<>("xmlEditor", XMLEditorPopupPage.class);
        xmlEditor.setPopupSettings(new VeilPopupSettings().setHeight(480).setWidth(800));
        MetaDataRoleAuthorizationStrategy.authorize(xmlEditor, ENABLE,
                xmlRolesReader.getEntitlement("Configuration", "workflowDefRead"));
        workflowDefContainer.add(xmlEditor);

        Image workflowDefDiagram = new Image("workflowDefDiagram", new Model()) {

            private static final long serialVersionUID = -8457850449086490660L;

            @Override
            protected IResource getImageResource() {
                return new DynamicImageResource() {

                    private static final long serialVersionUID = 923201517955737928L;

                    @Override
                    protected byte[] getImageData(final IResource.Attributes attributes) {
                        return wfRestClient.isActivitiEnabledForUsers()
                                ? wfRestClient.getDiagram()
                                : new byte[0];
                    }
                };
            }
        };
        workflowDefContainer.add(workflowDefDiagram);

        MetaDataRoleAuthorizationStrategy.authorize(workflowDefContainer, ENABLE,
                xmlRolesReader.getEntitlement("Configuration", "workflowDefRead"));
        add(workflowDefContainer);

        // Logger stuff
        PropertyListView<LoggerTO> coreLoggerList =
                new LoggerPropertyList(null, "corelogger", loggerRestClient.listLogs());
        WebMarkupContainer coreLoggerContainer = new WebMarkupContainer("coreLoggerContainer");
        coreLoggerContainer.add(coreLoggerList);
        coreLoggerContainer.setOutputMarkupId(true);

        MetaDataRoleAuthorizationStrategy.authorize(coreLoggerContainer, ENABLE, xmlRolesReader.getEntitlement(
                "Configuration", "logList"));
        add(coreLoggerContainer);

        ConsoleLoggerController consoleLoggerController = new ConsoleLoggerController();
        PropertyListView<LoggerTO> consoleLoggerList =
                new LoggerPropertyList(consoleLoggerController, "consolelogger", consoleLoggerController.getLoggers());
        WebMarkupContainer consoleLoggerContainer = new WebMarkupContainer("consoleLoggerContainer");
        consoleLoggerContainer.add(consoleLoggerList);
        consoleLoggerContainer.setOutputMarkupId(true);

        MetaDataRoleAuthorizationStrategy.authorize(
                consoleLoggerContainer, ENABLE, xmlRolesReader.getEntitlement("Configuration", "logList"));
        add(consoleLoggerContainer);

        // Extension panels
        setupExtPanels();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setupSyncopeConf() {
        final WebMarkupContainer parameters = new WebMarkupContainer("parameters");
        parameters.setOutputMarkupId(true);
        add(parameters);

        setWindowClosedCallback(syncopeConfWin, parameters);

        AjaxLink<Void> confLink = new IndicatingAjaxLink<Void>("confLink") {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                syncopeConfWin.setPageCreator(new ModalWindow.PageCreator() {

                    private static final long serialVersionUID = -7834632442532690940L;

                    @Override
                    public Page createPage() {
                        return new ConfModalPage(getPageReference(), editNotificationWin, parameters);
                    }
                });

                syncopeConfWin.show(target);
            }
        };
        parameters.add(confLink);

        Link<Void> dbExportLink = new Link<Void>("dbExportLink") {

            private static final long serialVersionUID = -4331619903296515985L;

            @Override
            public void onClick() {
                try {
                    HttpResourceStream stream = new HttpResourceStream(confRestClient.dbExport());

                    ResourceStreamRequestHandler rsrh = new ResourceStreamRequestHandler(stream);
                    rsrh.setFileName(stream.getFilename() == null ? "content.xml" : stream.getFilename());
                    rsrh.setContentDisposition(ContentDisposition.ATTACHMENT);

                    getRequestCycle().scheduleRequestHandlerAfterCurrent(rsrh);
                } catch (Exception e) {
                    error(getString(Constants.ERROR) + ": " + e.getMessage());
                }
            }
        };
        MetaDataRoleAuthorizationStrategy.authorize(
                dbExportLink, ENABLE, xmlRolesReader.getEntitlement("Configuration", "export"));
        add(dbExportLink);
    }

    private void setupNotification() {
        notificationPaginatorRows = prefMan.getPaginatorRows(getRequest(), Constants.PREF_NOTIFICATION_PAGINATOR_ROWS);

        final List<IColumn<NotificationTO, String>> notificationCols = new ArrayList<>();
        notificationCols.add(new PropertyColumn<NotificationTO, String>(
                new ResourceModel("key"), "key", "key"));
        notificationCols.add(new CollectionPropertyColumn<NotificationTO>(
                new ResourceModel("events"), "events", "events"));
        notificationCols.add(new PropertyColumn<NotificationTO, String>(
                new ResourceModel("subject"), "subject", "subject"));
        notificationCols.add(new PropertyColumn<NotificationTO, String>(
                new ResourceModel("template"), "template", "template"));
        notificationCols.add(new PropertyColumn<NotificationTO, String>(
                new ResourceModel("traceLevel"), "traceLevel", "traceLevel"));
        notificationCols.add(new PropertyColumn<NotificationTO, String>(
                new ResourceModel("active"), "active", "active"));

        notificationCols.add(new AbstractColumn<NotificationTO, String>(new ResourceModel("actions", "")) {

            private static final long serialVersionUID = 2054811145491901166L;

            @Override
            public String getCssClass() {
                return "action";
            }

            @Override
            public void populateItem(final Item<ICellPopulator<NotificationTO>> cellItem, final String componentId,
                    final IModel<NotificationTO> model) {

                final NotificationTO notificationTO = model.getObject();

                final ActionLinksPanel panel = new ActionLinksPanel(componentId, model, getPageReference());

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        editNotificationWin.setPageCreator(new ModalWindow.PageCreator() {

                            private static final long serialVersionUID = -7834632442532690940L;

                            @Override
                            public Page createPage() {
                                return new NotificationModalPage(Configuration.this.getPageReference(),
                                        editNotificationWin, notificationTO, false);
                            }
                        });

                        editNotificationWin.show(target);
                    }
                }, ActionLink.ActionType.EDIT, "Notification");

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        try {
                            notificationRestClient.delete(notificationTO.getKey());
                        } catch (SyncopeClientException e) {
                            LOG.error("While deleting a notification", e);
                            error(e.getMessage());
                            return;
                        }

                        info(getString(Constants.OPERATION_SUCCEEDED));
                        feedbackPanel.refresh(target);
                        target.add(notificationContainer);
                    }
                }, ActionLink.ActionType.DELETE, "Notification");

                cellItem.add(panel);
            }
        });

        final AjaxFallbackDefaultDataTable<NotificationTO, String> notificationTable =
                new AjaxFallbackDefaultDataTable<>(
                        "notificationTable", notificationCols, new NotificationProvider(), notificationPaginatorRows);

        notificationContainer = new WebMarkupContainer("notificationContainer");
        notificationContainer.add(notificationTable);
        notificationContainer.setOutputMarkupId(true);

        add(notificationContainer);

        setWindowClosedCallback(createNotificationWin, notificationContainer);
        setWindowClosedCallback(editNotificationWin, notificationContainer);

        AjaxLink<Void> createNotificationLink = new AjaxLink<Void>("createNotificationLink") {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                createNotificationWin.setPageCreator(new ModalWindow.PageCreator() {

                    private static final long serialVersionUID = -7834632442532690940L;

                    @Override
                    public Page createPage() {
                        return new NotificationModalPage(Configuration.this.getPageReference(), createNotificationWin,
                                new NotificationTO(), true);
                    }
                });

                createNotificationWin.show(target);
            }
        };

        MetaDataRoleAuthorizationStrategy.authorize(createNotificationLink, ENABLE, xmlRolesReader.getEntitlement(
                "Notification", "create"));
        add(createNotificationLink);

        @SuppressWarnings("rawtypes")
        Form notificationPaginatorForm = new Form("notificationPaginatorForm");

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final DropDownChoice rowsChooser = new DropDownChoice("rowsChooser", new PropertyModel(this,
                "notificationPaginatorRows"), prefMan.getPaginatorChoices());

        rowsChooser.add(new AjaxFormComponentUpdatingBehavior(Constants.ON_CHANGE) {

            private static final long serialVersionUID = -1107858522700306810L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                prefMan.set(getRequest(), getResponse(), Constants.PREF_NOTIFICATION_PAGINATOR_ROWS, String.valueOf(
                        notificationPaginatorRows));
                notificationTable.setItemsPerPage(notificationPaginatorRows);

                target.add(notificationContainer);
            }
        });

        notificationPaginatorForm.add(rowsChooser);
        add(notificationPaginatorForm);
    }

    private void setupSecurityQuestion() {
        final List<IColumn<SecurityQuestionTO, String>> securityQuestionCols = new ArrayList<>();
        securityQuestionCols.add(new PropertyColumn<SecurityQuestionTO, String>(
                new ResourceModel("key"), "key", "key"));
        securityQuestionCols.add(new PropertyColumn<SecurityQuestionTO, String>(
                new ResourceModel("content"), "content", "content"));

        securityQuestionCols.add(new AbstractColumn<SecurityQuestionTO, String>(new ResourceModel("actions", "")) {

            private static final long serialVersionUID = 2054811145491901166L;

            @Override
            public String getCssClass() {
                return "action";
            }

            @Override
            public void populateItem(final Item<ICellPopulator<SecurityQuestionTO>> cellItem, final String componentId,
                    final IModel<SecurityQuestionTO> model) {

                final SecurityQuestionTO securityQuestionTO = model.getObject();

                final ActionLinksPanel panel = new ActionLinksPanel(componentId, model, getPageReference());

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        editSecurityQuestionWin.setPageCreator(new ModalWindow.PageCreator() {

                            private static final long serialVersionUID = -7834632442532690940L;

                            @Override
                            public Page createPage() {
                                return new SecurityQuestionModalPage(Configuration.this.getPageReference(),
                                        editSecurityQuestionWin, securityQuestionTO, false);
                            }
                        });

                        editSecurityQuestionWin.show(target);
                    }
                }, ActionLink.ActionType.EDIT, "SecurityQuestion");

                panel.add(new ActionLink() {

                    private static final long serialVersionUID = -3722207913631435501L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        try {
                            securityQuestionRestClient.delete(securityQuestionTO.getKey());
                        } catch (SyncopeClientException e) {
                            LOG.error("While deleting a security question", e);
                            error(e.getMessage());
                            return;
                        }

                        info(getString(Constants.OPERATION_SUCCEEDED));
                        feedbackPanel.refresh(target);
                        target.add(securityQuestionContainer);
                    }
                }, ActionLink.ActionType.DELETE, "SecurityQuestion");

                cellItem.add(panel);
            }
        });

        final AjaxFallbackDefaultDataTable<SecurityQuestionTO, String> securityQuestionTable =
                new AjaxFallbackDefaultDataTable<>("securityQuestionTable",
                        securityQuestionCols, new SecurityQuestionProvider(), 50);

        securityQuestionContainer = new WebMarkupContainer("securityQuestionContainer");
        securityQuestionContainer.add(securityQuestionTable);
        securityQuestionContainer.setOutputMarkupId(true);

        add(securityQuestionContainer);

        setWindowClosedCallback(createSecurityQuestionWin, securityQuestionContainer);
        setWindowClosedCallback(editSecurityQuestionWin, securityQuestionContainer);

        AjaxLink<Void> createSecurityQuestionLink = new AjaxLink<Void>("createSecurityQuestionLink") {

            private static final long serialVersionUID = -7978723352517770644L;

            @Override
            public void onClick(final AjaxRequestTarget target) {

                createSecurityQuestionWin.setPageCreator(new ModalWindow.PageCreator() {

                    private static final long serialVersionUID = -7834632442532690940L;

                    @Override
                    public Page createPage() {
                        return new SecurityQuestionModalPage(Configuration.this.getPageReference(),
                                createSecurityQuestionWin, new SecurityQuestionTO(), true);
                    }
                });

                createSecurityQuestionWin.show(target);
            }
        };

        MetaDataRoleAuthorizationStrategy.authorize(
                createSecurityQuestionLink, ENABLE, xmlRolesReader.getEntitlement("SecurityQuestion", "create"));
        add(createSecurityQuestionLink);
    }

    private void setupExtPanels() {
        List<AbstractTab> tabs = new ArrayList<>();
        int index = 0;
        for (final Class<? extends AbstractExtensionPanel> clazz
                : implementationClassNamesLoader.getExtPanelClasses()) {

            String title = clazz.getAnnotation(ExtensionPanel.class) == null
                    ? "Extension " + index
                    : clazz.getAnnotation(ExtensionPanel.class).value();
            tabs.add(new AbstractTab(new Model<>(title)) {

                private static final long serialVersionUID = -5861786415855103549L;

                @Override
                public WebMarkupContainer getPanel(final String panelId) {
                    Panel panel;

                    try {
                        panel = ClassUtils.getConstructorIfAvailable(clazz, String.class, PageReference.class).
                                newInstance(panelId, Configuration.this.getPageReference());
                    } catch (Exception e) {
                        panel = new Panel(panelId) {

                            private static final long serialVersionUID = 5538299138211283825L;

                        };

                        LOG.error("Could not instantiate {}", clazz.getName(), e);
                    }

                    return panel;
                }
            });

            index++;
        }

        JQueryUITabbedPanel<AbstractTab> extPanels = new JQueryUITabbedPanel<>("extPanels", tabs);
        extPanels.setVisible(!tabs.isEmpty());
        add(extPanels);
    }

    private class NotificationProvider extends SortableDataProvider<NotificationTO, String> {

        private static final long serialVersionUID = -276043813563988590L;

        private final SortableDataProviderComparator<NotificationTO> comparator;

        public NotificationProvider() {
            //Default sorting
            setSort("key", SortOrder.ASCENDING);
            comparator = new SortableDataProviderComparator<>(this);
        }

        @Override
        public Iterator<NotificationTO> iterator(final long first, final long count) {
            List<NotificationTO> list = notificationRestClient.getAllNotifications();

            Collections.sort(list, comparator);

            return list.subList((int) first, (int) first + (int) count).iterator();
        }

        @Override
        public long size() {
            return notificationRestClient.getAllNotifications().size();
        }

        @Override
        public IModel<NotificationTO> model(final NotificationTO notification) {
            return new AbstractReadOnlyModel<NotificationTO>() {

                private static final long serialVersionUID = 774694801558497248L;

                @Override
                public NotificationTO getObject() {
                    return notification;
                }
            };
        }
    }

    private class SecurityQuestionProvider extends SortableDataProvider<SecurityQuestionTO, String> {

        private static final long serialVersionUID = -1458398823626281188L;

        private final SortableDataProviderComparator<SecurityQuestionTO> comparator;

        public SecurityQuestionProvider() {
            //Default sorting
            setSort("key", SortOrder.ASCENDING);
            comparator = new SortableDataProviderComparator<>(this);
        }

        @Override
        public Iterator<SecurityQuestionTO> iterator(final long first, final long count) {
            List<SecurityQuestionTO> list = securityQuestionRestClient.list();

            Collections.sort(list, comparator);

            return list.subList((int) first, (int) first + (int) count).iterator();
        }

        @Override
        public long size() {
            return securityQuestionRestClient.list().size();
        }

        @Override
        public IModel<SecurityQuestionTO> model(final SecurityQuestionTO securityQuestionTO) {
            return new AbstractReadOnlyModel<SecurityQuestionTO>() {

                private static final long serialVersionUID = 5079291243768775704L;

                @Override
                public SecurityQuestionTO getObject() {
                    return securityQuestionTO;
                }
            };
        }
    }

    private class LoggerPropertyList extends PropertyListView<LoggerTO> {

        private static final long serialVersionUID = 5911412425994616111L;

        private final ConsoleLoggerController consoleLoggerController;

        public LoggerPropertyList(final ConsoleLoggerController consoleLoggerController, final String id,
                final List<? extends LoggerTO> list) {

            super(id, list);
            this.consoleLoggerController = consoleLoggerController;
        }

        @Override
        protected void populateItem(final ListItem<LoggerTO> item) {
            item.add(new Label("key"));

            DropDownChoice<LoggerLevel> level = new DropDownChoice<>("level");
            level.setModel(new IModel<LoggerLevel>() {

                private static final long serialVersionUID = -2350428186089596562L;

                @Override
                public LoggerLevel getObject() {
                    return item.getModelObject().getLevel();
                }

                @Override
                public void setObject(final LoggerLevel object) {
                    item.getModelObject().setLevel(object);
                }

                @Override
                public void detach() {
                }
            });
            level.setChoices(Arrays.asList(LoggerLevel.values()));
            level.setOutputMarkupId(true);
            level.add(new AjaxFormComponentUpdatingBehavior(Constants.ON_CHANGE) {

                private static final long serialVersionUID = -1107858522700306810L;

                @Override
                protected void onUpdate(final AjaxRequestTarget target) {
                    try {
                        if (getId().equals("corelogger")) {
                            loggerRestClient.setLogLevel(item.getModelObject().getKey(),
                                    item.getModelObject().getLevel());
                        } else {
                            consoleLoggerController.setLogLevel(item.getModelObject().getKey(),
                                    item.getModelObject().getLevel());
                        }

                        info(getString(Constants.OPERATION_SUCCEEDED));
                    } catch (SyncopeClientException e) {
                        info(getString(Constants.OPERATION_ERROR));
                    }

                    feedbackPanel.refresh(target);
                }
            });

            MetaDataRoleAuthorizationStrategy.authorize(level, ENABLE, xmlRolesReader.getEntitlement(
                    "Configuration", "logSetLevel"));

            item.add(level);
        }
    }

    private static class ConsoleLoggerController implements Serializable {

        private static final long serialVersionUID = -1550459341476431714L;

        public List<LoggerTO> getLoggers() {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

            List<LoggerTO> result = new ArrayList<>();
            for (LoggerConfig logger : ctx.getConfiguration().getLoggers().values()) {
                final String loggerName = LogManager.ROOT_LOGGER_NAME.equals(logger.getName())
                        ? SyncopeConstants.ROOT_LOGGER : logger.getName();
                if (logger.getLevel() != null) {
                    LoggerTO loggerTO = new LoggerTO();
                    loggerTO.setKey(loggerName);
                    loggerTO.setLevel(LoggerLevel.fromLevel(logger.getLevel()));
                    result.add(loggerTO);
                }
            }

            return result;
        }

        public void setLogLevel(final String name, final LoggerLevel level) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            LoggerConfig logConf = SyncopeConstants.ROOT_LOGGER.equals(name)
                    ? ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
                    : ctx.getConfiguration().getLoggerConfig(name);
            logConf.setLevel(level.getLevel());
            ctx.updateLoggers();
        }
    }
}
