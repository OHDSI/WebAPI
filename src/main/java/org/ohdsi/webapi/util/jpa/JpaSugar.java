package org.ohdsi.webapi.util.jpa;


import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Syntactic sugar to get more expressive semantics on the JPA operations.
 */
public interface JpaSugar {

    /**
     * Creates a simple select query, with no ordering but straightforward, SQL-like semantics
     *
     * @param em entity manager to use
     * @param clazz Root class to use in FROM query section
     * @param <T> root entity type
     */
    static <T> Where<T, TypedQuery<T>> select(EntityManager em, Class<T> clazz) {
        return conditions -> {
            CriteriaQuery<T> criteriaQuery = query(em, clazz, (cb, cq) -> {
                Root<T> root = cq.from(clazz);
                CriteriaQuery<T> query = cq.select(root);
                return query.where(Filter.and(conditions).apply(cb, query).apply(root));
            });
            return em.createQuery(criteriaQuery);
        };
    }


    /**
     * The most basic syntactic sugar function that saves the caller the need to write
     * EntityManager.getCriteriaBuilder() and CriteriaQuery.createQuery() calls
     * @param em entity manager to use
     * @param clazz query return class
     * @param query query building function. Takes criteria builder, criteria query, root path and produces complete query
     * @param <T> query return type
     * @param <V> method return type. Since this function does not perform a call to em.createQuery() itself,
     * this allows for flexible return type, so that the caller can do it both inside the query function
     * or as part of processing return value from this method
     */
    static <T, V> V query(EntityManager em, Class<T> clazz, BiFunction<CriteriaBuilder, CriteriaQuery<T>, V> query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        return query.apply(cb, cb.createQuery(clazz));
    }

    static <E> int update(
            EntityManager em, Class<E> clazz,
            BiFunction<CriteriaBuilder, CriteriaUpdate<E>, Function<Path<E>, CriteriaUpdate<E>>> query
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<E> q = cb.createCriteriaUpdate(clazz);
        return em.createQuery(query.apply(cb, q).apply(q.from(clazz))).executeUpdate();
    }

    @FunctionalInterface
    interface Where<T, R> {
        R where(Filter<T>... biFunctions);
    }

    @FunctionalInterface
    interface Filter<E> {
        Function<Path<E>, Predicate> apply(CriteriaBuilder criteriaBuilder, AbstractQuery<?> query);

        @SafeVarargs
        static <R> Filter<R> or(Filter<R>... fns) {
            return or(Arrays.asList(fns));
        }

        static <R> Filter<R> or(List<Filter<R>> list) {
            return (cb, query) -> root ->
                    list.stream().map(item ->
                            item.apply(cb, query).apply(root)
                    ).reduce(cb::or).orElseGet(cb::disjunction);
        }

        @SafeVarargs
        static <R> Filter<R> and(Filter<R>... fns) {
            return and(Arrays.asList(fns));
        }

        static <R> Filter<R> and(List<Filter<R>> list) {
            return (cb, query) -> root ->
                    list.stream().map(item ->
                            item.apply(cb, query).apply(root)
                    ).reduce(cb::and).orElseGet(cb::conjunction);
        }

        static <T, L> Where<L, Filter<T>> subquery(Class<T> entityClass, Class<L> linkClass, SingularAttribute<? super L, T> attribute) {
            return filter -> (cb, query) -> path -> {
                Subquery<T> sq = query.subquery(entityClass);
                Root<L> root = sq.from(linkClass);
                return path.in(sq.select(root.get(attribute)).where(and(filter).apply(cb, sq).apply(root)));
            };
        }

        default <V> Filter<V> on(SingularAttribute<? super V, E> attribute) {
            return (cb, query) -> path -> apply(cb, query).apply(path.get(attribute));
        }

        default <T> Filter<T> on(Class<T> entityClass, Class<E> linkClass, SingularAttribute<? super E, T> attribute) {
            return (cb, query) -> path -> {
                Subquery<T> sq = query.subquery(entityClass);
                Root<E> root = sq.from(linkClass);
                return path.in(sq.select(root.get(attribute)).where(apply(cb, sq).apply(root)));
            };
        }

    }
    
    /**
     * A basic predicate-holding function.
     * Normally encapsulates predicate functions and one or more values required to apply it.
     *
     * @param <E> type of value on which predicates operate
     */
    @FunctionalInterface
    interface Condition<E> extends BiFunction<CriteriaBuilder, Path<E>, Predicate>, Filter<E> {
        @Override
        default Function<Path<E>, Predicate> apply(CriteriaBuilder cb, AbstractQuery<?> query) {
            return root -> apply(cb, root);
        };

        /**
         * A go-to where function for trivial queries selecting by a single attribute.
         * Not incredibly well-thought in terms of composition potential, but we'll need more active use cases
         * to sort that out.
         *
         * @param attribute attribute metamodel reference
         * @param value value to match against using equals
         * @param <E> Entity type
         * @param <V> Attribute value type
         */
        static <V, E> Condition<E> has(SingularAttribute<? super E, V> attribute, V value) {
            return (cb, path) -> cb.equal(path.get(attribute), value);
        }

        static <V, U, E> Condition<E> has(SingularAttribute<? super E, U> attribute1, SingularAttribute<? super U, V> attribute2, V value) {
            return (cb, path) -> cb.equal(path.get(attribute1).get(attribute2), value);
        }

        static <E> Condition<E> in(Collection<E> values) {
            return values.isEmpty() ? (cb, path) -> cb.disjunction() : (cb, path) -> path.in(values);
        }

        /**
         * Merges multiple conditions operating on the same entity into a single one, using AND and merging operation.
         *
         * @param conditions conditions to merge
         * @param <E>        entity or attribute type of all conditions
         */
        @SafeVarargs
        static <E> Condition<E> and(Condition<E>... conditions) {
            return (cb, path) -> Stream.of(conditions).map(
                    condition -> condition.apply(cb, path)
            ).reduce(cb::and).orElseGet(cb::conjunction);
        }

        /**
         * Merges multiple conditions operating on the same entity into a single one, using OR operation.
         *
         * @param conditions conditions to merge
         * @param <E> entity or attribute type of all conditions
         */
        @SafeVarargs
        static <E> Condition<E> or(Condition<E>... conditions) {
            return (cb, path) -> Stream.of(conditions).map(
                    condition -> condition.apply(cb, path)
            ).reduce(cb::or).orElseGet(cb::disjunction);
        }


        /**
         * A functional composition method that applies current condition after extracting the provided attribute.
         * Purely for convenience purposes for cases when we have no need to define a variable to hold the transition path separately
         *
         * @param attribute attribute.
         * @param <T> entity type to read attribute from
         */
        default <T> Condition<T> on(SingularAttribute<? super T, E> attribute) {
            return (cb, path) -> apply(cb, path.get(attribute));
        }
    }
}
