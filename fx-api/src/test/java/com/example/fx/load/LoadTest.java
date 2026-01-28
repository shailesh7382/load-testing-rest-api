package com.example.fx.load;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ClassicHttpResponse;

/**
 * LoadTest is a comprehensive JUnit-based load testing framework for the FX Pricing & Booking REST API.
 *
 * It supports Baseline, Load, Spike, Soak, and Stress test scenarios, and measures:
 * - Throughput (RPS)
 * - Latency (average, p90, p95, p99, p99.9, p99.99)
 * - Error rate
 * - SLA compliance
 *
 * All test parameters are externalized in loadtest.properties for easy tuning.
 *
 * The test summary is printed in a tabular format with a legend for interpretation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoadTest {

    // Load test parameters (populated from loadtest.properties)
    private String BASE_URL;
    private int THREADS;
    private int REQUESTS_PER_THREAD;
    private int BASELINE_THREADS;
    private int BASELINE_REQUESTS_PER_THREAD;
    private int SPIKE_THREADS;
    private int SPIKE_REQUESTS_PER_THREAD;
    private int SOAK_THREADS;
    private int SOAK_REQUESTS_PER_THREAD;
    private int SOAK_DURATION_SECONDS;
    private int STRESS_THREADS;
    private int STRESS_REQUESTS_PER_THREAD;
    private double SLA_MAX_ERROR_RATE;
    private long SLA_P95_LATENCY_MS;
    private double SLA_MIN_RPS;

    // JSON templates for quote and trade requests
    private String quoteJsonTemplate;
    private String tradeJsonTemplate;

    /**
     * Optionally sets CPU affinity for the current process to improve performance on multi-core systems.
     * This is a best-effort approach and works only on supported OS/JVMs.
     * Uses the 'taskset' command on Linux if available.
     */
    private void setCpuAffinity() {
        String os = System.getProperty("os.name").toLowerCase();
        // Only attempt on Linux
        if (os.contains("linux")) {
            try {
                // Pin to first 2 CPUs (0,1) for demonstration; adjust as needed
                String pid = String.valueOf(ProcessHandle.current().pid());
                Process p = new ProcessBuilder("taskset", "-cp", "0,1", pid).start();
                p.waitFor(2, TimeUnit.SECONDS);
                System.out.println("CPU affinity set to CPUs 0,1 for PID " + pid);
            } catch (Exception e) {
                System.out.println("Could not set CPU affinity: " + e.getMessage());
            }
        } else {
            System.out.println("CPU affinity not set (only supported on Linux with 'taskset').");
        }
    }

    /**
     * Loads test parameters and JSON templates before any test runs.
     */
    @BeforeAll
    void setup() throws IOException {
        setCpuAffinity();
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("loadtest.properties")) {
            props.load(in);
        }
        BASE_URL = props.getProperty("baseUrl", "http://localhost:8080/api");
        THREADS = Integer.parseInt(props.getProperty("threads", "10"));
        REQUESTS_PER_THREAD = Integer.parseInt(props.getProperty("requestsPerThread", "20"));
        BASELINE_THREADS = Integer.parseInt(props.getProperty("baseline.threads", "2"));
        BASELINE_REQUESTS_PER_THREAD = Integer.parseInt(props.getProperty("baseline.requestsPerThread", "5"));
        SPIKE_THREADS = Integer.parseInt(props.getProperty("spike.threads", "50"));
        SPIKE_REQUESTS_PER_THREAD = Integer.parseInt(props.getProperty("spike.requestsPerThread", "10"));
        SOAK_THREADS = Integer.parseInt(props.getProperty("soak.threads", "5"));
        SOAK_REQUESTS_PER_THREAD = Integer.parseInt(props.getProperty("soak.requestsPerThread", "5"));
        SOAK_DURATION_SECONDS = Integer.parseInt(props.getProperty("soak.durationSeconds", "120"));
        STRESS_THREADS = Integer.parseInt(props.getProperty("stress.threads", "100"));
        STRESS_REQUESTS_PER_THREAD = Integer.parseInt(props.getProperty("stress.requestsPerThread", "20"));
        SLA_MAX_ERROR_RATE = Double.parseDouble(props.getProperty("sla.maxErrorRate", "0.01"));
        SLA_P95_LATENCY_MS = Long.parseLong(props.getProperty("sla.p95LatencyMs", "250"));
        SLA_MIN_RPS = Double.parseDouble(props.getProperty("sla.minRps", "50.0"));

        RestAssured.baseURI = BASE_URL;
        quoteJsonTemplate = readResourceFile("quote.json");
        tradeJsonTemplate = readResourceFile("trade.json");
    }

    /**
     * Reads a resource file from the classpath as a String.
     */
    private String readResourceFile(String filename) throws IOException {
        return new String(
                getClass().getClassLoader().getResourceAsStream(filename).readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    /**
     * TestResult holds the metrics for a single test scenario.
     */
    private static class TestResult {
        String name;
        int totalRequests;
        double durationSeconds;
        double rps;
        double avgLatency;
        long p90Latency;
        long p95Latency;
        long p99Latency;
        long p999Latency;
        long p9999Latency;
        int concurrency;
        int errorCount;
        double errorRate;

        TestResult(String name, int totalRequests, double durationSeconds, double rps, double avgLatency,
                   long p90Latency, long p95Latency, long p99Latency, long p999Latency, long p9999Latency,
                   int concurrency, int errorCount, double errorRate) {
            this.name = name;
            this.totalRequests = totalRequests;
            this.durationSeconds = durationSeconds;
            this.rps = rps;
            this.avgLatency = avgLatency;
            this.p90Latency = p90Latency;
            this.p95Latency = p95Latency;
            this.p99Latency = p99Latency;
            this.p999Latency = p999Latency;
            this.p9999Latency = p9999Latency;
            this.concurrency = concurrency;
            this.errorCount = errorCount;
            this.errorRate = errorRate;
        }
    }

    // Stores all test scenario results for summary reporting
    private final List<TestResult> allResults = new ArrayList<>();

    /**
     * Runs all load test scenarios and prints a summary.
     *
     * Scenarios:
     * - Baseline: Low concurrency, few requests (reference)
     * - Load: Typical expected load
     * - Spike: Sudden high concurrency
     * - Soak: Sustained load over time
     * - Stress: Very high concurrency to find breaking point
     */
    @Test
    void allLoadTestScenariosSummary() throws InterruptedException {
        TestResult baselineResult = runLoadTest("Baseline Test", BASELINE_THREADS, BASELINE_REQUESTS_PER_THREAD);
        TestResult loadResult = runLoadTest("Load Test", THREADS, REQUESTS_PER_THREAD);
        TestResult spikeResult = runLoadTest("Spike Test", SPIKE_THREADS, SPIKE_REQUESTS_PER_THREAD);
        TestResult soakResult = runSoakTest("Soak Test", SOAK_THREADS, SOAK_REQUESTS_PER_THREAD, SOAK_DURATION_SECONDS);
        TestResult stressResult = runLoadTest("Stress Test", STRESS_THREADS, STRESS_REQUESTS_PER_THREAD);

        allResults.add(baselineResult);
        allResults.add(loadResult);
        allResults.add(spikeResult);
        allResults.add(soakResult);
        allResults.add(stressResult);

        printSummary();
    }

    /**
     * Runs a load test scenario with the given concurrency and requests per thread.
     * Each thread sends quote and trade requests in sequence using Apache HttpClient 5.x.
     *
     * @param testName Name of the scenario
     * @param threads Number of concurrent threads
     * @param requestsPerThread Number of iterations per thread
     * @return TestResult with all metrics
     */
    private TestResult runLoadTest(String testName, int threads, int requestsPerThread) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger errorCount = new AtomicInteger(0);
        int totalRequests = threads * requestsPerThread * 2; // 2 requests per loop (quote + trade)

        long testStart = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    for (int j = 0; j < requestsPerThread; j++) {
                        String quoteId = "Q" + ThreadLocalRandom.current().nextInt(100000, 999999);
                        String tradeId = "T" + ThreadLocalRandom.current().nextInt(100000, 999999);

                        // Send quote request
                        String quoteJson = quoteJsonTemplate.replace("${quoteId}", quoteId);
                        long start = System.nanoTime();
                        int status1 = doPost(client, BASE_URL + "/quotes", quoteJson);
                        long end = System.nanoTime();
                        latencies.add((end - start) / 1_000_000); // ms
                        if (status1 < 200 || status1 >= 300) errorCount.incrementAndGet();

                        // Send trade request referencing the quote
                        String tradeJson = tradeJsonTemplate
                                .replace("${tradeId}", tradeId)
                                .replace("${quoteId}", quoteId);
                        start = System.nanoTime();
                        int status2 = doPost(client, BASE_URL + "/trades", tradeJson);
                        end = System.nanoTime();
                        latencies.add((end - start) / 1_000_000); // ms
                        if (status2 < 200 || status2 >= 300) errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            }));
        }

        // Wait for all threads to finish
        for (Future<?> f : futures) {
            try {
                f.get(5, TimeUnit.MINUTES);
            } catch (Exception e) {
                errorCount.incrementAndGet();
                Assertions.fail(testName + " thread failed: " + e.getMessage());
            }
        }

        executor.shutdown();
        Assertions.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        long testEnd = System.nanoTime();
        double durationSeconds = (testEnd - testStart) / 1_000_000_000.0;
        double rps = totalRequests / durationSeconds;

        // Calculate latency percentiles and error rate
        List<Long> sortedLatencies = new ArrayList<>(latencies);
        Collections.sort(sortedLatencies);
        double avg = sortedLatencies.stream().mapToLong(l -> l).average().orElse(0);
        long p90 = getPercentile(sortedLatencies, 0.90);
        long p95 = getPercentile(sortedLatencies, 0.95);
        long p99 = getPercentile(sortedLatencies, 0.99);
        long p999 = getPercentile(sortedLatencies, 0.999);
        long p9999 = getPercentile(sortedLatencies, 0.9999);

        double errorRate = totalRequests == 0 ? 0 : ((double) errorCount.get() / totalRequests);

        return new TestResult(testName, totalRequests, durationSeconds, rps, avg, p90, p95, p99, p999, p9999,
                threads, errorCount.get(), errorRate);
    }

    /**
     * Runs a soak test scenario for a fixed duration, repeatedly sending quote and trade requests using Apache HttpClient 5.x.
     *
     * @param testName Name of the scenario
     * @param threads Number of concurrent threads
     * @param requestsPerThread Number of requests per thread per loop
     * @param durationSeconds Total duration of the soak test in seconds
     * @return TestResult with all metrics
     */
    private TestResult runSoakTest(String testName, int threads, int requestsPerThread, int durationSeconds) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger errorCount = new AtomicInteger(0);
        int totalRequests = 0;
        long testStart = System.nanoTime();
        long endTime = System.currentTimeMillis() + durationSeconds * 1000L;

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                int localCount = 0;
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    while (System.currentTimeMillis() < endTime) {
                        for (int j = 0; j < requestsPerThread; j++) {
                            String quoteId = "Q" + ThreadLocalRandom.current().nextInt(100000, 999999);
                            String tradeId = "T" + ThreadLocalRandom.current().nextInt(100000, 999999);

                            // Send quote request
                            String quoteJson = quoteJsonTemplate.replace("${quoteId}", quoteId);
                            long start = System.nanoTime();
                            int status1 = doPost(client, BASE_URL + "/quotes", quoteJson);
                            long end = System.nanoTime();
                            latencies.add((end - start) / 1_000_000); // ms
                            if (status1 < 200 || status1 >= 300) errorCount.incrementAndGet();

                            // Send trade request referencing the quote
                            String tradeJson = tradeJsonTemplate
                                    .replace("${tradeId}", tradeId)
                                    .replace("${quoteId}", quoteId);
                            start = System.nanoTime();
                            int status2 = doPost(client, BASE_URL + "/trades", tradeJson);
                            end = System.nanoTime();
                            latencies.add((end - start) / 1_000_000); // ms
                            if (status2 < 200 || status2 >= 300) errorCount.incrementAndGet();
                            localCount += 2;
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
                return localCount;
            }));
        }

        // Aggregate total requests from all threads
        int totalRequestsSoak = 0;
        for (Future<?> f : futures) {
            try {
                totalRequestsSoak += (Integer) f.get(durationSeconds + 60, TimeUnit.SECONDS);
            } catch (Exception e) {
                errorCount.incrementAndGet();
                Assertions.fail(testName + " thread failed: " + e.getMessage());
            }
        }

        executor.shutdown();
        Assertions.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        long testEnd = System.nanoTime();
        double actualDurationSeconds = (testEnd - testStart) / 1_000_000_000.0;
        double rps = totalRequestsSoak / actualDurationSeconds;

        // Calculate latency percentiles and error rate
        List<Long> sortedLatencies = new ArrayList<>(latencies);
        Collections.sort(sortedLatencies);
        double avg = sortedLatencies.stream().mapToLong(l -> l).average().orElse(0);
        long p90 = getPercentile(sortedLatencies, 0.90);
        long p95 = getPercentile(sortedLatencies, 0.95);
        long p99 = getPercentile(sortedLatencies, 0.99);
        long p999 = getPercentile(sortedLatencies, 0.999);
        long p9999 = getPercentile(sortedLatencies, 0.9999);

        double errorRate = totalRequestsSoak == 0 ? 0 : ((double) errorCount.get() / totalRequestsSoak);

        return new TestResult(testName, totalRequestsSoak, actualDurationSeconds, rps, avg, p90, p95, p99, p999, p9999,
                threads, errorCount.get(), errorRate);
    }

    /**
     * Utility to get the Nth percentile from a sorted list of latencies.
     *
     * @param sortedLatencies List of latencies (must be sorted)
     * @param percentile e.g. 0.95 for 95th percentile
     * @return latency value at the given percentile
     */
    private long getPercentile(List<Long> sortedLatencies, double percentile) {
        if (sortedLatencies.isEmpty()) return 0;
        int index = (int) Math.ceil(percentile * sortedLatencies.size()) - 1;
        index = Math.min(Math.max(index, 0), sortedLatencies.size() - 1);
        return sortedLatencies.get(index);
    }

    /**
     * Prints a formatted summary of all test results, including SLA status.
     *
     * SLA is considered PASS if:
     * - error rate <= sla.maxErrorRate
     * - p95 latency <= sla.p95LatencyMs
     * - RPS >= sla.minRps
     */
    private void printSummary() {
        System.out.println();
        System.out.println("===============================================================================================================================================================================");
        System.out.println("                                                          LOAD TEST RESULTS SUMMARY");
        System.out.println("===============================================================================================================================================================================");
        System.out.printf("%-16s | %-15s | %-10s | %-10s | %-14s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s%n",
                "Scenario", "Total Requests", "Duration", "RPS", "Avg Latency", "p90(ms)", "p95(ms)", "p99(ms)", "p99.9(ms)", "p99.99(ms)", "Concurrency", "Errors", "ErrRate", "SLA");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        for (TestResult r : allResults) {
            long max = Math.max(Math.max(Math.max(Math.max(r.p90Latency, r.p95Latency), r.p99Latency), r.p999Latency), r.p9999Latency);
            String slaStatus = (r.errorRate <= SLA_MAX_ERROR_RATE && r.p95Latency <= SLA_P95_LATENCY_MS && r.rps >= SLA_MIN_RPS)
                    ? "PASS" : "FAIL";
            System.out.printf("%-16s | %-15d | %-10.2f | %-10.2f | %-14.2f | %-10d | %-10d | %-10d | %-10d | %-10d | %-10d | %-10d | %-10.2f | %-10s%n",
                    r.name, r.totalRequests, r.durationSeconds, r.rps, r.avgLatency, r.p90Latency, r.p95Latency, r.p99Latency, r.p999Latency, r.p9999Latency,
                    r.concurrency, r.errorCount, r.errorRate * 100, slaStatus);
        }
        System.out.println("===============================================================================================================================================================================");
        System.out.println();
        System.out.println("Legend:");
        System.out.println("  Scenario        : Name of the test scenario (Baseline, Load, Spike, Soak, Stress)");
        System.out.println("  Total Requests  : Total number of HTTP requests sent during the scenario");
        System.out.println("  Duration        : Total duration of the scenario in seconds");
        System.out.println("  RPS             : Requests per second (throughput)");
        System.out.println("  Avg Latency     : Average response time in milliseconds");
        System.out.println("  p90(ms)         : 90th percentile latency (90% of requests were faster than this)");
        System.out.println("  p95(ms)         : 95th percentile latency (95% of requests were faster than this)");
        System.out.println("  p99(ms)         : 99th percentile latency (99% of requests were faster than this)");
        System.out.println("  p99.9(ms)       : 99.9th percentile latency (99.9% of requests were faster than this)");
        System.out.println("  p99.99(ms)      : 99.99th percentile latency (99.99% of requests were faster than this)");
        System.out.println("  Concurrency     : Number of concurrent threads/users used in the scenario");
        System.out.println("  Errors          : Number of failed requests (non-2xx status)");
        System.out.println("  ErrRate         : Error rate as a percentage");
        System.out.println("  SLA             : PASS if error rate <= 1%, p95 latency <= 250ms, RPS >= 50; otherwise FAIL");
        System.out.println();
        System.out.println("Interpretation:");
        System.out.println("  - Lower latency and higher RPS indicate better performance.");
        System.out.println("  - p90, p95, p99, p99.9, and p99.99 latencies help identify outliers and worst-case response times.");
        System.out.println("  - Use Baseline to establish a reference, Load for expected traffic, Spike for sudden surges, Soak for long-term stability, and Stress to find breaking points.");
        System.out.println("  - SLA is considered PASS if error rate <= 1%, p95 latency <= 250ms, and RPS >= 50.");
        System.out.println();
    }

    /**
     * Helper method to POST JSON using Apache HttpClient 5.x and return HTTP status code.
     */
    private int doPost(CloseableHttpClient client, String url, String json) {
        try {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(json));
            try (ClassicHttpResponse response = client.executeOpen(null, post, null)) {
                return response.getCode();
            }
        } catch (Exception e) {
            return 0;
        }
    }

    // =========================
    // EXTENSION SUGGESTIONS
    // =========================
    // - Add support for custom endpoints or payloads via properties or feeders.
    // - Add CSV/JSON export for results.
    // - Integrate with monitoring APIs (Prometheus, etc.) for system metrics.
    // - Add warm-up phase before main test.
    // - Add ramp-up/ramp-down logic for more realistic scenarios.
    // - Add assertions for individual metrics (fail test if SLA not met).
    // - Parameterize endpoints for different environments.
    // - Add support for GET/PUT/DELETE and more complex workflows.
    // - Add logging of slowest requests or error responses for diagnostics.
    // - Add support for distributed load generation.
}
