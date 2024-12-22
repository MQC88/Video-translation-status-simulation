public class IntegrationTest {
    public static void main(String[] args) {
        // You can adjust these values before each run
        double initialInterval = 1.0;  // seconds
        double maxInterval = 8.0;     // seconds
        int timeout = 60;             // seconds (overall wait)

        System.out.println("----- Integration Test Starting -----");
        System.out.println(" Using initialInterval = " + initialInterval + "s");
        System.out.println(" Using maxInterval     = " + maxInterval + "s");
        System.out.println(" Using timeout         = " + timeout + "s");

        // Create the client with the above parameters 
        TranslationStatusClient client = new TranslationStatusClient(
            "http://localhost:5000",
            initialInterval,
            maxInterval,
            timeout
        );

        try {
            // Poll until completed or error, or until timeout is reached
            String finalStatus = client.waitForCompletion();
            System.out.println("Final status: " + finalStatus);
        } catch (Exception e) {
            System.out.println("Exception occurred during waitForCompletion: " + e.getMessage());
        }

        System.out.println("----- Integration Test Finished -----");
    }
}
