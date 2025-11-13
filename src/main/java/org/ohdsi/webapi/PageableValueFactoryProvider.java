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
import java.util.function.Function;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Jersey 3 value provider for Spring Data Pageable injection.
 *
 * Extracts pagination parameters from query strings:
 * - page: Page number (0-indexed, default: 0)
 * - size: Page size (default: 10)
 * - sort: Sort specification in format "property,direction" (e.g., "name,asc")
 */
public class PageableValueFactoryProvider implements ValueParamProvider {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final ServiceLocator locator;

    @Inject
    public PageableValueFactoryProvider(ServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public Function<org.glassfish.jersey.server.ContainerRequest, ?> getValueProvider(Parameter parameter) {
        if (parameter.getRawType() == Pageable.class
                && parameter.isAnnotationPresent(Pagination.class)) {
            return this::extractPageable;
        }
        return null;
    }

    private Pageable extractPageable(org.glassfish.jersey.server.ContainerRequest request) {
        int page = getQueryParamAsInt(request, "page", DEFAULT_PAGE);
        int size = getQueryParamAsInt(request, "size", DEFAULT_SIZE);

        List<String> sortParams = request.getUriInfo().getQueryParameters().get("sort");
        Sort sort = parseSort(sortParams);

        return PageRequest.of(page, size, sort);
    }

    private int getQueryParamAsInt(org.glassfish.jersey.server.ContainerRequest request, String param, int defaultValue) {
        String value = request.getUriInfo().getQueryParameters().getFirst(param);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private Sort parseSort(List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String sortParam : sortParams) {
            String[] parts = sortParam.split(",");
            String property = parts[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;

            if (parts.length > 1) {
                String directionStr = parts[1].trim().toUpperCase();
                if ("DESC".equals(directionStr)) {
                    direction = Sort.Direction.DESC;
                }
            }

            orders.add(new Sort.Order(direction, property));
        }

        return Sort.by(orders);
    }

    @Override
    public PriorityType getPriority() {
        return Priority.NORMAL;
    }
}