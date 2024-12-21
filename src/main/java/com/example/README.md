HeyGen Status Simulation
Overview
This project simulates a video translation backend and provides a client library that polls the server to determine whether a video translation job is pending, completed, or in an error state. The code demonstrates an approach calling the server and implements features such as exponential backoff and configurable timeouts.

What This Project Does
Server Simulation
Mocks the HeyGen video translation process.
Returns {"result": "pending"} until a configured delay has passed, then returns {"result": "completed"} or {"result": "error"}.

Client Library

A Java class, TranslationStatusClient, that hits the server’s GET /status endpoint.
Uses exponential backoff to avoid hammering the server frequently if the translation is still pending.
Supports a timeout to stop polling if the job takes too long.

Integration Test

Demonstrates how to spin up the server (in one terminal) and use the client library (in another) to poll until the status is no longer pending.

How We Implemented It
Server.java uses Spark Java to create a simple REST endpoint /status.

PROCESSING_DELAY (in seconds) determines how long the server returns pending.
FINAL_STATE determines if the final response after the delay is "completed" or "error".
Both can be configured using environment variables or default to 60 seconds (for PROCESSING_DELAY) and "completed" (for FINAL_STATE).
TranslationStatusClient.java is a small client library that:

Makes a GET request to /status.
Retries in a loop if it sees "pending", increasing the wait time (exponential backoff).
Stops if the server returns "completed", "error", or if the total wait time exceeds a timeout.
This is better than a trivial approach because we do not rely on the user to manually call the endpoint nor do we do fixed-interval polling; instead, we gradually increase the polling interval and enforce a maximum timeout.
IntegrationTest.java shows how to instantiate TranslationStatusClient, configure intervals, and run a test that polls the server until completion or timeout.

File-by-File Summary
pom.xml

Maven configuration file specifying dependencies (Spark Java, Gson) and Java version.
Server.java

Spins up an HTTP server on port 5000.
GET /status returns "pending" until PROCESSING_DELAY seconds have passed.
After that delay, it returns either "completed" or "error", depending on FINAL_STATE.
TranslationStatusClient.java

Connects to http://localhost:5000/status by default (configurable).
Polls the server using exponential backoff until the job is done or a timeout occurs.
Methods:
getStatus() — single GET call to retrieve current status.
waitForCompletion() — a loop that calls getStatus(), handling pending, completed, error, and timeouts.
IntegrationTest.java

Demonstrates how to use TranslationStatusClient.
Configurable parameters: initialInterval, maxInterval, timeout.
Prints out logs showing the polling process.
How to Run This Program
Prerequisites
Java 17 or above installed.
Maven installed
1. Clone or Download This Project
bash
Copy code
git clone https://github.com/your-username/heygen-status.git
cd heygen-status
(Replace with your actual repository link once you’ve uploaded it.)

2. Run the Server
Open one terminal (or VS Code terminal) inside the project folder.
(Optional) Configure environment variables:
bash
Copy code
set PROCESSING_DELAY=60
set FINAL_STATE=completed
On Windows, use set.
On Mac/Linux, use export.
For example, set PROCESSING_DELAY=90 will make the server return pending for 90 seconds, then "completed" or "error".
Run:
bash
Copy code
mvn compile exec:java -Dexec.mainClass=Server
This starts the server on http://localhost:5000.
3. Run the Integration Test
Open another terminal (leaving the server running).
(Optional) Adjust the parameters in IntegrationTest.java if you want to set different initialInterval, maxInterval, or timeout values.
Run the test:
bash
Copy code
mvn compile exec:java -Dexec.mainClass=IntegrationTest
Watch the console output to see the client polling. You’ll see logs like:
lua
Copy code
Current status: pending
Current status: pending
...
Current status: completed
Final status: completed
Stop the server at any time by pressing Ctrl + C in the server’s terminal.
4. Mimicking a Random Process
You can randomize your environment variables or set them to different values each time. For instance:
bash
Copy code
set PROCESSING_DELAY=150
set FINAL_STATE=error
This setup returns {"result":"pending"} for 150 seconds, then {"result":"error"}.
By changing these on each run, you effectively produce random or varied scenarios without modifying the code.
5. Changing Polling Logic
If you need to change the initialInterval, maxInterval, or timeout for the polling logic, open IntegrationTest.java (or the constructor calls to TranslationStatusClient) and modify:
java
Copy code
double initialInterval = 1.0; // seconds
double maxInterval = 8.0;    // seconds
int timeout = 60;            // seconds
Then re-run mvn compile exec:java -Dexec.mainClass=IntegrationTest.