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
package org.apache.syncope.core.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Resource;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.SyncopeClient;
import org.apache.syncope.common.SyncopeConstants;
import org.apache.syncope.common.to.MembershipTO;
import org.apache.syncope.common.to.NotificationTaskTO;
import org.apache.syncope.common.to.RoleTO;
import org.apache.syncope.common.to.UserTO;
import org.apache.syncope.common.types.AttributableType;
import org.apache.syncope.common.types.IntMappingType;
import org.apache.syncope.common.types.TraceLevel;
import org.apache.syncope.core.persistence.beans.Entitlement;
import org.apache.syncope.core.persistence.beans.Notification;
import org.apache.syncope.core.persistence.beans.NotificationTask;
import org.apache.syncope.core.persistence.beans.conf.CAttr;
import org.apache.syncope.core.persistence.beans.conf.CSchema;
import org.apache.syncope.core.persistence.dao.ConfDAO;
import org.apache.syncope.core.persistence.dao.EntitlementDAO;
import org.apache.syncope.core.persistence.dao.NotificationDAO;
import org.apache.syncope.core.persistence.dao.SchemaDAO;
import org.apache.syncope.core.persistence.dao.TaskDAO;
import org.apache.syncope.core.rest.UserTestITCase;
import org.apache.syncope.core.rest.controller.RoleController;
import org.apache.syncope.core.rest.controller.TaskController;
import org.apache.syncope.core.rest.controller.UserController;
import org.apache.syncope.core.util.AttributableUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:coreContext.xml",
    "classpath:restTestEnv.xml",
    "classpath:persistenceContext.xml",
    "classpath:schedulingContext.xml",
    "classpath:workflowContext.xml"
})
@Transactional
public class NotificationTest {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NotificationTest.class);

    private static final String SMTP_HOST = "localhost";

    private static final int SMTP_PORT = 2525;

    private static final String POP3_HOST = "localhost";

    private static final int POP3_PORT = 1110;

    private static final String MAIL_ADDRESS = "notificationtest@syncope.apache.org";

    private static final String MAIL_PASSWORD = "password";

    private static GreenMail greenMail;

    @Resource(name = "adminUser")
    private String adminUser;

    @Autowired
    private EntitlementDAO entitlementDAO;

    @Autowired
    private NotificationDAO notificationDAO;

    @Autowired
    private TaskDAO taskDAO;

    @Autowired
    private SchemaDAO schemaDAO;

    @Autowired
    private ConfDAO confDAO;

    @Autowired
    private UserController userController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private TaskController taskController;

    @Autowired
    private NotificationJob notificationJob;

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private JavaMailSender mailSender;

    @BeforeClass
    public static void startGreenMail() {
        ServerSetup[] config = new ServerSetup[2];
        config[0] = new ServerSetup(SMTP_PORT, SMTP_HOST, ServerSetup.PROTOCOL_SMTP);
        config[1] = new ServerSetup(POP3_PORT, POP3_HOST, ServerSetup.PROTOCOL_POP3);
        greenMail = new GreenMail(config);
        greenMail.setUser(MAIL_ADDRESS, MAIL_PASSWORD);
        greenMail.start();
    }

    @AfterClass
    public static void stopGreenMail() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }

    @Before
    public void setupSecurity() {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (Entitlement entitlement : entitlementDAO.findAll()) {
            authorities.add(new SimpleGrantedAuthority(entitlement.getName()));
        }

        UserDetails userDetails = new User(adminUser, "FAKE_PASSWORD", true, true, true, true, authorities);
        Authentication authentication = new TestingAuthenticationToken(userDetails, "FAKE_PASSWORD", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Before
    public void setupSMTP() throws Exception {
        JavaMailSenderImpl sender = (JavaMailSenderImpl) mailSender;
        sender.setDefaultEncoding(SyncopeConstants.DEFAULT_ENCODING);
        sender.setHost(SMTP_HOST);
        sender.setPort(SMTP_PORT);
    }

    private boolean verifyMail(final String sender, final String subject) throws Exception {
        LOG.info("Waiting for notification to be sent...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        boolean found = false;
        Session session = Session.getDefaultInstance(System.getProperties());
        Store store = session.getStore("pop3");
        store.connect(POP3_HOST, POP3_PORT, MAIL_ADDRESS, MAIL_PASSWORD);

        Folder inbox = store.getFolder("INBOX");
        assertNotNull(inbox);
        inbox.open(Folder.READ_WRITE);

        Message[] messages = inbox.getMessages();
        for (int i = 0; i < messages.length; i++) {
            if (sender.equals(messages[i].getFrom()[0].toString()) && subject.equals(messages[i].getSubject())) {
                found = true;
                messages[i].setFlag(Flag.DELETED, true);
            }
        }

        inbox.close(true);
        store.close();
        return found;
    }

    @Test
    public void notifyByMail() throws Exception {
        // 1. create suitable notification for subsequent tests
        Notification notification = new Notification();
        notification.addEvent("[REST]:[UserController]:[]:[create]:[SUCCESS]");
        notification.setUserAbout(SyncopeClient.getUserSearchConditionBuilder().hasRoles(7L).query());
        notification.setRecipients(SyncopeClient.getUserSearchConditionBuilder().hasRoles(8L).query());
        notification.setSelfAsRecipient(true);

        notification.setRecipientAttrName("email");
        notification.setRecipientAttrType(IntMappingType.UserSchema);

        Random random = new Random(System.currentTimeMillis());
        String sender = "syncopetest-" + random.nextLong() + "@syncope.apache.org";
        notification.setSender(sender);
        String subject = "Test notification " + random.nextLong();
        notification.setSubject(subject);
        notification.setTemplate("optin");

        Notification actual = notificationDAO.save(notification);
        assertNotNull(actual);

        notificationDAO.flush();

        // 2. create user
        UserTO userTO = UserTestITCase.getSampleTO(MAIL_ADDRESS);
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(7);
        userTO.getMemberships().add(membershipTO);

        userController.create(userTO, true);

        // 3. force Quartz job execution and verify e-mail
        notificationJob.execute(null);
        assertTrue(verifyMail(sender, subject));

        // 4. get NotificationTask id and text body
        Long taskId = null;
        String textBody = null;
        for (NotificationTask task : taskDAO.findAll(NotificationTask.class)) {
            if (sender.equals(task.getSender())) {
                taskId = task.getId();
                textBody = task.getTextBody();
            }
        }
        assertNotNull(taskId);
        assertNotNull(textBody);
        assertTrue("Notification mail text doesn't contain expected content.",
                textBody.contains("Your email address is notificationtest@syncope.apache.org."));
        assertTrue("Notification mail text doesn't contain expected content.",
                textBody.contains("Your email address inside a link: "
                        + "http://localhost/?email=notificationtest%40syncope.apache.org ."));

        // 5. execute Notification task and verify e-mail
        taskController.execute(taskId, false);
        assertTrue(verifyMail(sender, subject));
    }

    @Test
    public void issueSYNCOPE192() throws Exception {
        // 1. create suitable notification for subsequent tests
        Notification notification = new Notification();
        notification.addEvent("[REST]:[UserController]:[]:[create]:[SUCCESS]");
        notification.setUserAbout(SyncopeClient.getUserSearchConditionBuilder().hasRoles(7L).query());
        notification.setRecipients(SyncopeClient.getUserSearchConditionBuilder().hasRoles(8L).query());
        notification.setSelfAsRecipient(true);

        notification.setRecipientAttrName("email");
        notification.setRecipientAttrType(IntMappingType.UserSchema);

        Random random = new Random(System.currentTimeMillis());
        String sender = "syncope192-" + random.nextLong() + "@syncope.apache.org";
        notification.setSender(sender);
        String subject = "Test notification " + random.nextLong();
        notification.setSubject(subject);
        notification.setTemplate("optin");
        notification.setTraceLevel(TraceLevel.NONE);

        Notification actual = notificationDAO.save(notification);
        assertNotNull(actual);

        // 2. create user
        UserTO userTO = UserTestITCase.getSampleTO(MAIL_ADDRESS);
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(7);
        userTO.getMemberships().add(membershipTO);

        userController.create(userTO, true);

        // 3. force Quartz job execution and verify e-mail
        notificationJob.execute(null);
        assertTrue(verifyMail(sender, subject));

        // 4. get NotificationTask id
        Long taskId = null;
        for (NotificationTask task : taskDAO.findAll(NotificationTask.class)) {
            if (sender.equals(task.getSender())) {
                taskId = task.getId();
            }
        }
        assertNotNull(taskId);

        // 5. verify that last exec status was updated
        NotificationTaskTO task = (NotificationTaskTO) taskController.read(taskId, true);
        assertNotNull(task);
        assertTrue(task.getExecutions().isEmpty());
        assertTrue(task.isExecuted());
        assertTrue(StringUtils.isNotBlank(task.getLatestExecStatus()));
    }

    @Test
    public void notifyByMailEmptyAbout() throws Exception {
        // 1. create suitable notification for subsequent tests
        Notification notification = new Notification();
        notification.addEvent("[REST]:[UserController]:[]:[create]:[SUCCESS]");
        notification.setUserAbout(null);
        notification.setRecipients(SyncopeClient.getUserSearchConditionBuilder().hasRoles(8L).query());
        notification.setSelfAsRecipient(true);

        notification.setRecipientAttrName("email");
        notification.setRecipientAttrType(IntMappingType.UserSchema);

        Random random = new Random(System.currentTimeMillis());
        String sender = "syncopetest-" + random.nextLong() + "@syncope.apache.org";
        notification.setSender(sender);
        String subject = "Test notification " + random.nextLong();
        notification.setSubject(subject);
        notification.setTemplate("optin");

        Notification actual = notificationDAO.save(notification);
        assertNotNull(actual);

        notificationDAO.flush();

        // 2. create user
        UserTO userTO = UserTestITCase.getSampleTO(MAIL_ADDRESS);
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(7);
        userTO.getMemberships().add(membershipTO);

        userController.create(userTO, true);

        // 3. force Quartz job execution and verify e-mail
        notificationJob.execute(null);
        assertTrue(verifyMail(sender, subject));

        // 4. get NotificationTask id
        Long taskId = null;
        for (NotificationTask task : taskDAO.findAll(NotificationTask.class)) {
            if (sender.equals(task.getSender())) {
                taskId = task.getId();
            }
        }
        assertNotNull(taskId);

        // 5. execute Notification task and verify e-mail
        taskController.execute(taskId, false);
        assertTrue(verifyMail(sender, subject));
    }

    @Test
    public void notifyByMailWithRetry() throws Exception {
        // 1. create suitable notification for subsequent tests
        Notification notification = new Notification();
        notification.addEvent("[REST]:[UserController]:[]:[create]:[SUCCESS]");
        notification.setUserAbout(null);
        notification.setRecipients(SyncopeClient.getUserSearchConditionBuilder().hasRoles(8L).query());
        notification.setSelfAsRecipient(true);

        notification.setRecipientAttrName("email");
        notification.setRecipientAttrType(IntMappingType.UserSchema);

        Random random = new Random(System.currentTimeMillis());
        String sender = "syncopetest-" + random.nextLong() + "@syncope.apache.org";
        notification.setSender(sender);
        String subject = "Test notification " + random.nextLong();
        notification.setSubject(subject);
        notification.setTemplate("optin");

        Notification actual = notificationDAO.save(notification);
        assertNotNull(actual);

        notificationDAO.flush();

        // 2. create user
        UserTO userTO = UserTestITCase.getSampleTO(MAIL_ADDRESS);
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(7);
        userTO.getMemberships().add(membershipTO);

        userController.create(userTO, true);

        // 3. Set number of retries
        CAttr maxRetries = new CAttr();
        maxRetries.setSchema(schemaDAO.find("notification.maxRetries", CSchema.class));
        maxRetries.addValue("5", AttributableUtil.getInstance(AttributableType.CONFIGURATION));
        confDAO.save(maxRetries);
        confDAO.flush();

        // 4. Stop mail server to force error sending mail
        stopGreenMail();

        // 5. force Quartz job execution multiple times
        for (int i = 0; i < 10; i++) {
            notificationJob.execute(null);
        }

        // 6. get NotificationTask, count number of executions
        NotificationTask foundTask = null;
        for (NotificationTask task : taskDAO.findAll(NotificationTask.class)) {
            if (sender.equals(task.getSender())) {
                foundTask = task;
            }
        }
        assertNotNull(foundTask);
        assertEquals(6, notificationManager.countExecutionsWithStatus(foundTask.getId(),
                NotificationJob.Status.NOT_SENT.name()));

        // 7. start mail server again
        startGreenMail();

        // 8. reset number of retries
        maxRetries = new CAttr();
        maxRetries.setSchema(schemaDAO.find("notification.maxRetries", CSchema.class));
        maxRetries.addValue("0", AttributableUtil.getInstance(AttributableType.CONFIGURATION));
        confDAO.save(maxRetries);
        confDAO.flush();
    }

    @Test
    public void issueSYNCOPE445() throws Exception {
        // 1. create suitable notification for subsequent tests
        Notification notification = new Notification();
        notification.addEvent("[REST]:[UserController]:[]:[create]:[SUCCESS]");
        notification.setUserAbout(SyncopeClient.getUserSearchConditionBuilder().hasRoles(7L).query());
        notification.setRecipients(SyncopeClient.getUserSearchConditionBuilder().hasRoles(8L).query());
        notification.setSelfAsRecipient(true);

        notification.setRecipientAttrName("email");
        notification.setRecipientAttrType(IntMappingType.UserSchema);

        notification.getStaticRecipients().add("syncope445@syncope.apache.org");

        Random random = new Random(System.currentTimeMillis());
        String sender = "syncopetest-" + random.nextLong() + "@syncope.apache.org";
        notification.setSender(sender);
        String subject = "Test notification " + random.nextLong();
        notification.setSubject(subject);
        notification.setTemplate("optin");

        Notification actual = notificationDAO.save(notification);
        assertNotNull(actual);

        notificationDAO.flush();

        // 2. create user
        UserTO userTO = UserTestITCase.getSampleTO(MAIL_ADDRESS);
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(7);
        userTO.getMemberships().add(membershipTO);

        userController.create(userTO, true);

        // 3. force Quartz job execution and verify e-mail
        notificationJob.execute(null);
        assertTrue(verifyMail(sender, subject));

        // 4. get NotificationTask id and text body
        Long taskId = null;
        String textBody = null;
        Set<String> recipients = null;
        for (NotificationTask task : taskDAO.findAll(NotificationTask.class)) {
            if (sender.equals(task.getSender())) {
                taskId = task.getId();
                textBody = task.getTextBody();
                recipients = task.getRecipients();
            }
        }

        assertNotNull(taskId);
        assertNotNull(textBody);
        assertTrue(recipients.contains("syncope445@syncope.apache.org"));

        // 5. execute Notification task and verify e-mail
        taskController.execute(taskId, false);
        assertTrue(verifyMail(sender, subject));
    }

    @Test
    public void issueSYNCOPE492() throws Exception {
        // 1. create suitable disabled notification for subsequent tests
        Notification notification = new Notification();
        notification.addEvent("[REST]:[UserController]:[]:[create]:[SUCCESS]");
        notification.setUserAbout(SyncopeClient.getUserSearchConditionBuilder().hasRoles(7L).query());
        notification.setSelfAsRecipient(true);

        notification.setRecipientAttrName("email");
        notification.setRecipientAttrType(IntMappingType.UserSchema);

        notification.getStaticRecipients().add("syncope492@syncope.apache.org");

        Random random = new Random(System.currentTimeMillis());
        String sender = "syncopetest-" + random.nextLong() + "@syncope.apache.org";
        notification.setSender(sender);
        String subject = "Test notification " + random.nextLong();
        notification.setSubject(subject);
        notification.setTemplate("optin");
        notification.setActive(false);

        Notification actual = notificationDAO.save(notification);
        assertNotNull(actual);

        notificationDAO.flush();

        final int tasksNumberBefore = taskDAO.findAll(NotificationTask.class).size();

        // 2. create user
        UserTO userTO = UserTestITCase.getUniqueSampleTO(MAIL_ADDRESS);
        MembershipTO membershipTO = new MembershipTO();
        membershipTO.setRoleId(7);
        userTO.getMemberships().add(membershipTO);

        userController.create(userTO, true);

        // 3. force Quartz job execution
        notificationJob.execute(null);

        // 4. check if number of tasks is not incremented
        assertEquals(tasksNumberBefore, taskDAO.findAll(NotificationTask.class).size());
    }

    @Test
    public void issueSYNCOPE446() throws Exception {

        // 1. create suitable notification for subsequent tests
        Notification notification = new Notification();
        notification.addEvent("[REST]:[RoleController]:[]:[create]:[SUCCESS]");
        notification.setRoleAbout(SyncopeClient.getRoleSearchConditionBuilder().is("name").equalTo("role446").query());
        notification.setSelfAsRecipient(false);

        notification.setRecipientAttrName("email");
        notification.setRecipientAttrType(IntMappingType.UserSchema);

        notification.getStaticRecipients().add(MAIL_ADDRESS);

        Random random = new Random(System.currentTimeMillis());
        String sender = "syncopetest-" + random.nextLong() + "@syncope.apache.org";
        notification.setSender(sender);
        String subject = "Test notification " + random.nextLong();
        notification.setSubject(subject);
        notification.setTemplate("optin");

        Notification actual = notificationDAO.save(notification);
        assertNotNull(actual);

        notificationDAO.flush();

        // 2. create role
        RoleTO roleTO = new RoleTO();
        roleTO.setName("role446");
        roleTO.setParent(1L);

        RoleTO createdRole = roleController.create(roleTO);
        assertNotNull(createdRole);

        // 3. force Quartz job execution and verify e-mail
        notificationJob.execute(null);
        assertTrue(verifyMail(sender, subject));

        // 4. get NotificationTask id and text body
        Long taskId = null;
        String textBody = null;
        Set<String> recipients = null;
        for (NotificationTask task : taskDAO.findAll(NotificationTask.class)) {
            if (sender.equals(task.getSender())) {
                taskId = task.getId();
                textBody = task.getTextBody();
                recipients = task.getRecipients();
            }
        }

        assertNotNull(taskId);
        assertNotNull(textBody);
        assertTrue(recipients.contains(MAIL_ADDRESS));

        // 5. execute Notification task and verify e-mail
        taskController.execute(taskId, false);
        assertTrue(verifyMail(sender, subject));
    }
}
