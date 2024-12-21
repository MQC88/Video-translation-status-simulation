import static spark.Spark.*;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Server {
    // Read an environment variable "PROCESSING_DELAY" if set; otherwise default to 60 seconds.
    // This delay determines how long the server returns "pending" before switching to the final state.
    private static final double PROCESSING_DELAY = Double.parseDouble(System.getenv().getOrDefault("PROCESSING_DELAY", "60"));
    // Read an environment variable "FINAL_STATE" if set; otherwise default to "completed".
    private static final String FINAL_STATE = System.getenv().getOrDefault("FINAL_STATE", "completed");
    private static final long START_TIME = System.currentTimeMillis();

    public static void main(String[] args) {
        // Run the server on port 5000
        port(5000);

        // /status GET endpoint
        get("/status", (req, res) -> {
            // return JSON data
            res.type("application/json");
            // Calculate how much time has passed since the server started.
            long elapsedMs = System.currentTimeMillis() - START_TIME;
            double elapsedSec = elapsedMs / 1000.0;

            Map<String, String> response = new HashMap<>();
            // If the elapsed time is less than PROCESSING_DELAY, return "pending".
            // Otherwise, return the FINAL_STATE, which could be "completed" or "error".
            if (elapsedSec < PROCESSING_DELAY) {
                response.put("result", "pending");
            } else {
                response.put("result", FINAL_STATE);
            }
            return new Gson().toJson(response);
        });
    }
}
