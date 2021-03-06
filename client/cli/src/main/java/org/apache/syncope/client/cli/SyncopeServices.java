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
package org.apache.syncope.client.cli;

import java.util.ResourceBundle;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.client.lib.SyncopeClientFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SyncopeServices {

    private static final Logger LOG = LoggerFactory.getLogger(SyncopeServices.class);

    private final static ResourceBundle SYNCOPE_PROPS = ResourceBundle.getBundle("syncope");

    private static final SyncopeClient CLIENT = new SyncopeClientFactoryBean()
            .setAddress(SYNCOPE_PROPS.getString("syncope.rest.services"))
            .create(SYNCOPE_PROPS.getString("syncope.user"), SYNCOPE_PROPS.getString("syncope.password"));

    public static <T> T get(final Class<T> claz) {
        LOG.debug("Creting service for {}", claz.getName());
        return CLIENT.getService(claz);
    }

    private SyncopeServices() {
        // private constructor for static utility class
    }
}
