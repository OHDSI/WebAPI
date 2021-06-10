/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableValueFactoryProvider implements ValueFactoryProvider {

    private final ServiceLocator locator;

    @Inject
    public PageableValueFactoryProvider(ServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public Factory<?> getValueFactory(Parameter parameter) {
        if (parameter.getRawType() == Pageable.class
                && parameter.isAnnotationPresent(Pagination.class)) {
            Factory<?> factory = new PageableValueFactory(locator);
            return factory;
        }
        return null;
    }

    @Override
    public PriorityType getPriority() {
        return Priority.NORMAL;
    }

    private static class PageableValueFactory
            extends AbstractContainerRequestValueFactory<Pageable> {

        @QueryParam("page") @DefaultValue("0") Integer page;
        @QueryParam("size") @DefaultValue("10") Integer size;
        @QueryParam("sort") List<String> sort;

        private final ServiceLocator locator;

        private PageableValueFactory(ServiceLocator locator) {
            this.locator = locator;
        }

        @Override
        public Pageable provide() {
            locator.inject(this);

            List<Sort.Order> orders = new ArrayList<>();
            for (String propOrder: sort) {
                String[] propOrderSplit = propOrder.split(",");
                String property = propOrderSplit[0];
                if (propOrderSplit.length == 1) {
                    orders.add(new Sort.Order(property));
                } else {
                    Sort.Direction direction
                            = Sort.Direction.fromStringOrNull(propOrderSplit[1]);
                    orders.add(new Sort.Order(direction, property));
                }
            }

            return new PageRequest(page, size,
                    orders.isEmpty() ? null : new Sort(orders));
        }
    }
}