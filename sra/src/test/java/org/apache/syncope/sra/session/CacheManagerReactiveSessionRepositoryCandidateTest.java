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
package org.apache.syncope.sra.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentMap;
import org.apache.syncope.sra.SessionConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.session.MapSession;
import org.springframework.test.util.ReflectionTestUtils;

class CacheManagerReactiveSessionRepositoryCandidateTest {

    @Test
    void findByIdReturnsExpiredSessionFromDefaultCache() {
        CacheManager cacheManager = new SessionConfig().cacheManager();
        AbstractCacheManager.class.cast(cacheManager).initializeCaches();

        CacheManagerReactiveSessionRepository repository = new CacheManagerReactiveSessionRepository();
        ReflectionTestUtils.setField(repository, "cacheManager", cacheManager);

        MapSession expired = new MapSession();
        expired.setMaxInactiveInterval(Duration.ofSeconds(1));
        expired.setLastAccessedTime(Instant.now().minus(Duration.ofMinutes(5)));
        assertTrue(expired.isExpired());

        repository.save(expired).block();

        @SuppressWarnings("unchecked")
        ConcurrentMap<Object, Object> nativeCache = (ConcurrentMap<Object, Object>) cacheManager.
                getCache(SessionConfig.DEFAULT_CACHE).getNativeCache();
        assertEquals(1, nativeCache.size());

        MapSession found = repository.findById(expired.getId()).block();
        assertNotNull(found);
        assertTrue(found.isExpired());
        assertEquals(1, nativeCache.size());
    }
}
