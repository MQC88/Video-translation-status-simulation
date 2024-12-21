import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import com.google.gson.Gson;

public class TranslationStatusClient {
    private String baseUrl;
    private double initialInterval; // starting wait time (sec) between polls
    private double maxInterval; // max wait time (sec) between polls
    private int timeout; // max total wait time (sec)

    public TranslationStatusClient(String baseUrl, double initialInterval, double maxInterval, int timeout) {
        this.baseUrl = baseUrl;
        this.initialInterval = initialInterval;
        this.maxInterval = maxInterval;
        this.timeout = timeout;
    }

    public TranslationStatusClient() {
        // Default values:
        this("http://localhost:5000", 1.0, 8.0, 60);
    }

    // Makes a GET request to endpoint and returns "pending", "completed", or "error"
    public String getStatus() throws Exception {
        URL url = new URL(baseUrl + "/status");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        // Timeouts to avoid hanging if server is slow or unreachable
        con.setConnectTimeout(5000);  // 5 sec connect timeout
        con.setReadTimeout(5000);     // 5 sec read timeout

        // Read response code
        int status = con.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("Non-200 response: " + status);
        }

        // Read response body into a string
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            content.append(line);
        }
        in.close();
        con.disconnect();

        // Parse JSON response
        Gson gson = new Gson();
        Map<String, String> resultMap = gson.fromJson(content.toString(), Map.class);
        return resultMap.getOrDefault("result", "error");
    }

    // Polls the server until the result is no longer "pending" or we hit the timeout
    public String waitForCompletion() {
        long startTime = System.currentTimeMillis();
        double interval = initialInterval * 1000; // convert to ms
        double maxIntervalMs = maxInterval * 1000;
        long endTime = startTime + (timeout * 1000);

        while (true) {
            long now = System.currentTimeMillis();
            // Check if we've exceeded the total allowed time
            if (now > endTime) {
                System.out.println("Timed out waiting for completion.");
                throw new RuntimeException("Timed out waiting for completion");
            }

            try {
                String status = getStatus();
                System.out.println("Current status: " + status);
                // If final state reached, stop and return it
                if ("completed".equals(status) || "error".equals(status)) {
                    return status;
                }
            } catch (Exception e) {
                // If there's a network or parsing error, print and retry
                System.out.println("Error fetching status: " + e.getMessage());
            }

            // Still "pending", wait and then try again
            try {
                Thread.sleep((long) interval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Increase the wait interval up to the maximum
            interval = Math.min(interval * 2, maxIntervalMs);
        }
    }
}
