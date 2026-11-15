package org.ohdsi.webapi.conceptset;

import org.junit.After;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertTrue;

public class ConceptSetConcurrentSaveTest extends AbstractDatabaseTest {

    @Autowired
    private ConceptSetService service;

    @Autowired
    private ConceptSetRepository conceptSetRepository;

    private final List<Integer> createdIds = new CopyOnWriteArrayList<>();

    @After
    public void cleanUp() {
        for (Integer id : createdIds) {
            conceptSetRepository.findById(id).ifPresent(conceptSetRepository::delete);
        }
        createdIds.clear();
    }

    private ConceptSetDTO create(String name) {
        ConceptSetDTO dto = new ConceptSetDTO();
        dto.setName(name);
        ConceptSetDTO created = service.createConceptSet(dto);
        createdIds.add(created.getId());
        return created;
    }

    private List<Throwable> runAll(int threads, int rounds, BiConsumer<Integer, Integer> body) throws Exception {
        SecurityContext ctx = SecurityContextHolder.getContext();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CyclicBarrier barrier = new CyclicBarrier(threads);
            List<Future<?>> futures = new ArrayList<>();
            List<Throwable> errors = new CopyOnWriteArrayList<>();
            for (int t = 0; t < threads; t++) {
                final int tid = t;
                futures.add(pool.submit(() -> {
                    SecurityContextHolder.setContext(ctx);
                    for (int r = 0; r < rounds; r++) {
                        try {
                            barrier.await(30, TimeUnit.SECONDS);
                            body.accept(tid, r);
                        } catch (Throwable e) {
                            errors.add(e);
                            barrier.reset();
                        }
                    }
                }));
            }
            for (Future<?> f : futures) {
                f.get(300, TimeUnit.SECONDS);
            }
            return errors;
        } finally {
            pool.shutdownNow();
        }
    }

    /** Concurrent renames and item saves on one concept set must not deadlock or fail. */
    @Test
    public void concurrentRenameAndItemSave() throws Exception {
        ConceptSetDTO base = create("base");
        List<Throwable> errors = runAll(4, 50, (tid, r) -> {
            if (tid == 0) {
                service.saveConceptSetItems(base.getId(), new ConceptSetItem[0]);
            } else {
                ConceptSetDTO dto = new ConceptSetDTO();
                dto.setId(base.getId());
                dto.setName("rename-" + tid + "-" + r);
                service.updateConceptSet(base.getId(), dto);
            }
        });
        errors.forEach(Throwable::printStackTrace);
        assertTrue("errors: " + errors.size(), errors.isEmpty());
    }

    /** A rename racing a delete either succeeds or fails with 404, nothing else. */
    @Test
    public void renameWhileDeleting() throws Exception {
        List<Throwable> unexpected = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 40; i++) {
            ConceptSetDTO cs = create("victim-" + i);
            List<Throwable> errors = runAll(2, 1, (tid, r) -> {
                if (tid == 0) {
                    service.deleteConceptSet(cs.getId());
                } else {
                    ConceptSetDTO dto = new ConceptSetDTO();
                    dto.setId(cs.getId());
                    dto.setName("renamed");
                    service.updateConceptSet(cs.getId(), dto);
                }
            });
            for (Throwable e : errors) {
                if (!(e instanceof ResponseStatusException)
                        || ((ResponseStatusException) e).getStatusCode() != HttpStatus.NOT_FOUND) {
                    unexpected.add(e);
                }
            }
        }
        unexpected.forEach(t -> System.out.println("RENAME-DELETE ERROR: " + t));
        assertTrue("unexpected errors: " + unexpected.size(), unexpected.isEmpty());
    }
}
