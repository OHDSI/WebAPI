/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: May 29, 2017
 *
 */

package org.ohdsi.webapi.arachne.commons.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class ConverterUtils {

    private GenericConversionService conversionService;

    @Autowired
    public ConverterUtils(GenericConversionService conversionService) {

        this.conversionService = conversionService;
    }

    public <S, R> List<R> convertList(List<S> source, Class<R> targetClass) {

        return convertList(source, targetClass, null);
    }

    public <S, T> Set<T> convertSet(Collection<S> source, Class<T> targetClass) {

        return convertCollection(source, targetClass, null, Collectors.toSet());
    }

    public <S, T> List<T> convertList(List<S> source, Class<T> targetClass, Comparator<T> comparator) {

        return convertCollection(source, targetClass, comparator, Collectors.toList());
    }

    public <S, T, A, R> R convertCollection(Collection<S> source, Class<T> targetClass, Comparator<T> comparator, Collector<T, A, R> collector) {

        Stream<T> intermediateStream = source.stream()
                .map(item -> conversionService.convert(item, targetClass));
        if (comparator != null) {
            intermediateStream = intermediateStream.sorted(comparator);
        }
        return intermediateStream.collect(collector);
    }
}
