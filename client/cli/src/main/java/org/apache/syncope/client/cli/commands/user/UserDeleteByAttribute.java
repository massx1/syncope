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
package org.apache.syncope.client.cli.commands.user;

import java.util.HashMap;
import java.util.Map;
import javax.xml.ws.WebServiceException;
import org.apache.syncope.client.cli.Input;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.BulkActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDeleteByAttribute extends AbstractUserCommand {

    private static final Logger LOG = LoggerFactory.getLogger(UserDeleteByAttribute.class);

    private static final String SEARCH_HELP_MESSAGE = "user --delete-by-attribute {REALM} {ATTR-NAME}={ATTR-VALUE}";

    private final Input input;

    public UserDeleteByAttribute(final Input input) {
        this.input = input;
    }

    public void delete() {
        if (input.parameterNumber() == 2) {
            final String realm = input.firstParameter();
            final Input.PairParameter pairParameter = input.toPairParameter(input.secondParameter());
            try {
                if (!realmSyncopeOperations.exists(realm)) {
                    userResultManager.notFoundError("Realm", realm);
                    return;
                }
                final Map<String, BulkActionResult.Status> results = userSyncopeOperations.deleteByAttribute(
                        realm, pairParameter.getKey(), pairParameter.getValue());
                final Map<String, String> users = new HashMap<>();
                int deletedUsers = 0;
                for (final Map.Entry<String, BulkActionResult.Status> entrySet : results.entrySet()) {
                    final String userId = entrySet.getKey();
                    final BulkActionResult.Status status = entrySet.getValue();
                    if (!BulkActionResult.Status.SUCCESS.equals(status)) {
                        users.put(userId, status.name());
                    } else {
                        deletedUsers++;
                    }
                }
                userResultManager.genericMessage("Deleted users: " + deletedUsers);
                if (!users.isEmpty()) {
                    userResultManager.printUndeletedUsers(users);
                }
            } catch (final WebServiceException | SyncopeClientException ex) {
                LOG.error("Error searching user", ex);
                if (ex.getMessage().startsWith("NotFound")) {
                    userResultManager.notFoundError("User with " + pairParameter.getKey(), pairParameter.getValue());
                } else {
                    userResultManager.genericError(ex.getMessage());
                }
            } catch (final IllegalArgumentException ex) {
                LOG.error("Error searching user", ex);
                userResultManager.genericError(ex.getMessage());
                userResultManager.genericError(SEARCH_HELP_MESSAGE);
            }
        } else {
            userResultManager.commandOptionError(SEARCH_HELP_MESSAGE);
        }
    }
}
