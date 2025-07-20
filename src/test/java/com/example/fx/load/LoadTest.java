package com.example.fx.load;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoadTest {

    private final String BASE_URL = "http://localhost:8080/api";
    private final int THREADS = 10;
    private final int REQUESTS_PER_THREAD = 20;
    private String quoteJsonTemplate;
    private String tradeJsonTemplate;

    @BeforeAll
    void setup() throws IOException {
        RestAssured.baseURI = BASE_URL;
        quoteJsonTemplate = readResourceFile("quote.json");
        tradeJsonTemplate = readResourceFile("trade.json");
    }

    private String readResourceFile(String filename) throws IOException {
        return new String(
                getClass().getClassLoader().getResourceAsStream(filename).readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    private static class TestResult {
        String name;
        int totalRequests;
        double durationSeconds;
        double rps;
        double avgLatency;
        long p95Latency;
        long p99Latency;

        TestResult(String name, int totalRequests, double durationSeconds, double rps, double avgLatency, long p95Latency, long p99Latency) {
            this.name = name;
            this.totalRequests = totalRequests;
            this.durationSeconds = durationSeconds;
            this.rps = rps;
            this.avgLatency = avgLatency;
            this.p95Latency = p95Latency;
            this.p99Latency = p99Latency;
        }
    }

    private final List<TestResult> allResults = new ArrayList<>();

    @Test
    void loadAndSpikeTestSummary() throws InterruptedException {
        TestResult loadResult = runLoadTest("Load Test", THREADS, REQUESTS_PER_THREAD);
        TestResult spikeResult = runLoadTest("Spike Test", 50, 10);

        allResults.add(loadResult);
        allResults.add(spikeResult);

        printSummary();
    }

    private TestResult runLoadTest(String testName, int threads, int requestsPerThread) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        int totalRequests = threads * requestsPerThread * 2; // 2 requests per loop

        long testStart = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    String quoteId = "Q" + ThreadLocalRandom.current().nextInt(100000, 999999);
                    String tradeId = "T" + ThreadLocalRandom.current().nextInt(100000, 999999);

                    String quoteJson = quoteJsonTemplate.replace("${quoteId}", quoteId);
                    long start = System.nanoTime();
                    RestAssured.given()
                            .contentType(ContentType.JSON)
                            .body(quoteJson)
                            .post("/quotes")
                            .then()
                            .statusCode(anyOf(is(200), is(201)));
                    long end = System.nanoTime();
                    latencies.add((end - start) / 1_000_000); // ms

                    String tradeJson = tradeJsonTemplate
                            .replace("${tradeId}", tradeId)
                            .replace("${quoteId}", quoteId);
                    start = System.nanoTime();
                    RestAssured.given()
                            .contentType(ContentType.JSON)
                            .body(tradeJson)
                            .post("/trades")
                            .then()
                            .statusCode(anyOf(is(200), is(201)));
                    end = System.nanoTime();
                    latencies.add((end - start) / 1_000_000); // ms
                }
            }));
        }

        for (Future<?> f : futures) {
            try {
                f.get(2, TimeUnit.MINUTES);
            } catch (Exception e) {
                Assertions.fail(testName + " thread failed: " + e.getMessage());
            }
        }

        executor.shutdown();
        Assertions.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        long testEnd = System.nanoTime();
        double durationSeconds = (testEnd - testStart) / 1_000_000_000.0;
        double rps = totalRequests / durationSeconds;

        // Calculate latency stats
        List<Long> sortedLatencies = new ArrayList<>(latencies);
        Collections.sort(sortedLatencies);
        double avg = sortedLatencies.stream().mapToLong(l -> l).average().orElse(0);
        long p95 = sortedLatencies.get((int) (sortedLatencies.size() * 0.95) - 1);
        long p99 = sortedLatencies.get((int) (sortedLatencies.size() * 0.99) - 1);

        return new TestResult(testName, totalRequests, durationSeconds, rps, avg, p95, p99);
    }

    private void printSummary() {
        System.out.println("\n==================== LOAD TEST SUMMARY ====================");
        System.out.printf("%-15s | %-14s | %-10s | %-10s | %-12s | %-10s | %-10s%n",
                "Test", "Total Requests", "Seconds", "RPS", "Avg Latency", "p95(ms)", "p99(ms)");
        System.out.println("--------------------------------------------------------------------------");
        for (TestResult r : allResults) {
            System.out.printf("%-15s | %-14d | %-10.2f | %-10.2f | %-12.2f | %-10d | %-10d%n",
                    r.name, r.totalRequests, r.durationSeconds, r.rps, r.avgLatency, r.p95Latency, r.p99Latency);
        }
        System.out.println("============================================================\n");
    }
}
