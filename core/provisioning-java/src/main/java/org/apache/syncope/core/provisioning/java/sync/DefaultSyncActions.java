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
package org.apache.syncope.core.provisioning.java.sync;

import org.apache.syncope.common.lib.patch.AnyPatch;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.core.provisioning.api.sync.SyncActions;
import org.apache.syncope.core.provisioning.api.sync.ProvisioningProfile;
import org.apache.syncope.core.provisioning.api.sync.ProvisioningReport;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.quartz.JobExecutionException;

/**
 * Default (empty) implementation of {@link SyncActions}.
 */
public abstract class DefaultSyncActions implements SyncActions {

    @Override
    public void beforeAll(final ProvisioningProfile<?, ?> profile) throws JobExecutionException {
    }

    @Override
    public <A extends AnyTO, P extends AnyPatch> SyncDelta beforeUpdate(
            final ProvisioningProfile<?, ?> profile,
            final SyncDelta delta,
            final A any,
            final P anyMod) throws JobExecutionException {

        return delta;
    }

    @Override
    public <A extends AnyTO> SyncDelta beforeDelete(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final A any)
            throws JobExecutionException {

        return delta;
    }

    @Override
    public <A extends AnyTO> SyncDelta beforeAssign(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final A any)
            throws JobExecutionException {

        return delta;
    }

    @Override
    public <A extends AnyTO> SyncDelta beforeProvision(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final A any)
            throws JobExecutionException {

        return delta;
    }

    @Override
    public <A extends AnyTO> SyncDelta beforeLink(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final A any)
            throws JobExecutionException {

        return delta;
    }

    @Override
    public <A extends AnyTO> SyncDelta beforeUnassign(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final A any)
            throws JobExecutionException {

        return delta;
    }

    @Override
    public <A extends AnyTO> SyncDelta beforeDeprovision(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final A any)
            throws JobExecutionException {

        return delta;
    }

    @Override
    public <A extends AnyTO> SyncDelta beforeUnlink(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final A any)
            throws JobExecutionException {

        return delta;
    }

    @Override
    public void onError(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final ProvisioningReport result,
            final Exception error) throws JobExecutionException {
    }

    @Override
    public <A extends AnyTO> void after(
            final ProvisioningProfile<?, ?> profile, final SyncDelta delta, final A any,
            final ProvisioningReport result)
            throws JobExecutionException {
    }

    @Override
    public void afterAll(final ProvisioningProfile<?, ?> profile)
            throws JobExecutionException {
    }
}
