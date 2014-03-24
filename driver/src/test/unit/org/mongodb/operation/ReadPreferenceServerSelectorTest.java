/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.operation;

import org.junit.Test;
import org.mongodb.ReadPreference;
import org.mongodb.connection.ClusterDescription;
import org.mongodb.connection.ServerAddress;
import org.mongodb.connection.ServerDescription;
import org.mongodb.connection.ServerType;

import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mongodb.connection.ClusterConnectionMode.MULTIPLE;
import static org.mongodb.connection.ClusterType.REPLICA_SET;
import static org.mongodb.connection.ServerConnectionState.CONNECTED;

public class ReadPreferenceServerSelectorTest {
    @Test
    public void testAll() throws UnknownHostException {
        ReadPreferenceServerSelector selector = new ReadPreferenceServerSelector(ReadPreference.primary());

        assertEquals(ReadPreference.primary(), selector.getReadPreference());

        assertEquals(new ReadPreferenceServerSelector(ReadPreference.primary()), selector);
        assertNotEquals(new ReadPreferenceServerSelector(ReadPreference.secondary()), selector);
        assertNotEquals(new Object(), selector);

        assertEquals(new ReadPreferenceServerSelector(ReadPreference.primary()).hashCode(), selector.hashCode());

        assertEquals("ReadPreferenceServerSelector{readPreference=primary}", selector.toString());

        ServerDescription primary = ServerDescription.builder()
                                                     .state(CONNECTED)
                                                     .address(new ServerAddress())
                                                     .ok(true)
                                                     .type(ServerType.REPLICA_SET_PRIMARY)
                                                     .build();
        assertEquals(asList(primary), selector.choose(new ClusterDescription(MULTIPLE, REPLICA_SET, asList(primary))));
    }

    @Test
    public void testChaining() throws UnknownHostException {
        ReadPreferenceServerSelector selector = new ReadPreferenceServerSelector(ReadPreference.secondary());
        ServerDescription primary = ServerDescription.builder()
                                                     .state(CONNECTED)
                                                     .address(new ServerAddress())
                                                     .ok(true)
                                                     .type(ServerType.REPLICA_SET_PRIMARY)
                                                     .averagePingTime(1, TimeUnit.MILLISECONDS)
                                                     .build();
        ServerDescription secondaryOne = ServerDescription.builder()
                                                          .state(CONNECTED)
                                                          .address(new ServerAddress("localhost:27018"))
                                                          .ok(true)
                                                          .type(ServerType.REPLICA_SET_SECONDARY)
                                                          .averagePingTime(2, TimeUnit.MILLISECONDS)
                                                          .build();
        ServerDescription secondaryTwo = ServerDescription.builder()
                                                          .state(CONNECTED)
                                                          .address(new ServerAddress("localhost:27019"))
                                                          .ok(true)
                                                          .type(ServerType.REPLICA_SET_SECONDARY)
                                                          .averagePingTime(20, TimeUnit.MILLISECONDS)
                                                          .build();
        assertEquals(asList(secondaryOne), selector.choose(new ClusterDescription(MULTIPLE,
                                                                                  REPLICA_SET,
                                                                                  asList(primary, secondaryOne, secondaryTwo))));

    }
}