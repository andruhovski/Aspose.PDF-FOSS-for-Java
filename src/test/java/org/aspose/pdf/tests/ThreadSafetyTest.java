package org.aspose.pdf.tests;

import org.aspose.pdf.Document;
import org.aspose.pdf.Page;
import org.aspose.pdf.engine.pdfobjects.PdfName;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for thread safety of core components.
public class ThreadSafetyTest {

    @Test
    public void testConcurrentPageAccess() throws Exception {
        Document doc = new Document();
        // Add several pages
        for (int i = 0; i < 10; i++) {
            doc.getPages().add();
        }

        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    // Access pages concurrently
                    for (int i = 1; i <= doc.getPages().getCount(); i++) {
                        Page page = doc.getPages().get(i);
                        assertNotNull(page);
                        assertNotNull(page.getMediaBox());
                    }
                } catch (Throwable e) {
                    errors.add(e);
                }
            }));
        }

        // Release all threads at once
        startLatch.countDown();

        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        if (!errors.isEmpty()) {
            fail("Concurrent access caused errors: " + errors.get(0).getMessage());
        }
    }

    @Test
    public void testPdfNameConcurrentIntern() throws Exception {
        int threadCount = 8;
        int namesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < namesPerThread; i++) {
                        // All threads create the same names — should get identical instances
                        String name = "ConcurrentName_" + i;
                        PdfName n1 = PdfName.of(name);
                        PdfName n2 = PdfName.of(name);
                        assertSame(n1, n2, "PdfName interning failed for: " + name);
                    }
                } catch (Throwable e) {
                    errors.add(e);
                }
            }));
        }

        startLatch.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        if (!errors.isEmpty()) {
            fail("Concurrent PdfName.of() caused errors: " + errors.get(0).getMessage());
        }
    }

    @Test
    public void testConcurrentGetPages() throws Exception {
        Document doc = new Document();
        doc.getPages().add();

        int threadCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    // getPages() should be safe to call from multiple threads
                    assertNotNull(doc.getPages());
                    assertTrue(doc.getPages().getCount() > 0);
                } catch (Throwable e) {
                    errors.add(e);
                }
            }));
        }

        startLatch.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        if (!errors.isEmpty()) {
            fail("Concurrent getPages() caused errors: " + errors.get(0).getMessage());
        }
    }
}
