/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.cache.listener;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.map.listener.MapClearedListener;
import com.hazelcast.map.listener.MapEvictedListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyEntryListener implements EntryAddedListener<String, String>,
            EntryRemovedListener<String, String>,
            EntryUpdatedListener<String, String>,
            EntryEvictedListener<String, String>,
            MapEvictedListener,
            MapClearedListener   {
        @Override
        public void entryAdded( EntryEvent<String, String> event ) {
            log.info( "Entry Added:" + event );
        }

        @Override
        public void entryRemoved( EntryEvent<String, String> event ) {
        	log.info( "Entry Removed:" + event );
        }

        @Override
        public void entryUpdated( EntryEvent<String, String> event ) {
            log.info( "Entry Updated:" + event );
        }

        @Override
        public void entryEvicted( EntryEvent<String, String> event ) {
        	log.info( "Entry Evicted:" + event );
        }

        @Override
        public void mapEvicted( MapEvent event ) {
        	log.info( "Map Evicted:" + event );
        }

        @Override
        public void mapCleared( MapEvent event ) {
        	log.info( "Map Cleared:" + event );
        }

    }