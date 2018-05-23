/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Pavel Grafkin
 *
 */

package org.ohdsi.webapi.util;


import org.apache.commons.collections4.map.PassiveExpiringMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpiringMultimap<K, V> {

    private Integer timeToLiveMs;
    private Map<K, PassiveExpiringMap<V, Object>> map = new HashMap<>();

    public ExpiringMultimap(Integer timeToLiveMs) {

        this.timeToLiveMs = timeToLiveMs;
    }

    public synchronized List<V> get(K key) {

        return map.containsKey(key)
                ? map.get(key).entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())
                : new ArrayList<>();
    }


    public synchronized void put(K key, V value) {

        PassiveExpiringMap<V, Object> storage = map.computeIfAbsent(key, k -> new PassiveExpiringMap<>(timeToLiveMs));
        storage.put(value, null);
        map.put(key, storage);
    }
}
