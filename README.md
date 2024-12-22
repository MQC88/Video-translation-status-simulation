HeyGen Status Simulation

1. Overview

This project simulates a video translation backend and provides a client library that polls the server to determine whether a video translation job is pending, completed, or in an error state. The code demonstrates a smarter approach than trivially calling the server by implementing features such as exponential backoff and configurable timeouts.

2. What This Project Does

● Server Simulation

Mocks the HeyGen video translation process.
Returns {"result": "pending"} until a configured delay has passed, then returns {"result": "completed"} or {"result": "error"}.

● Client Library

A Java class, TranslationStatusClient, that hits the server’s GET /status endpoint.
Uses exponential backoff to avoid hammering the server frequently if the translation is still pending.
Supports a timeout to stop polling if the job takes too long.

● Integration Test

Demonstrates how to spin up the server (in one terminal) and use the client library (in another) to poll until the status is no longer pending.

3. How We Implemented It

● Server.java uses Spark Java to create a simple REST endpoint /status.

PROCESSING_DELAY (in seconds) determines how long the server returns pending.
FINAL_STATE determines if the final response after the delay is "completed" or "error".
Both can be configured using environment variables or default to 60 seconds (for PROCESSING_DELAY) and "completed" (for FINAL_STATE).

● TranslationStatusClient.java is a small client library that:

Makes a GET request to /status.
Retries in a loop if it sees "pending", increasing the wait time (exponential backoff).
Stops if the server returns "completed", "error", or if the total wait time exceeds a timeout.
This is better than a trivial approach because we do not rely on the user to manually call the endpoint nor do we do fixed-interval polling; instead, we gradually increase the polling interval and enforce a maximum timeout.

● IntegrationTest.java shows how to instantiate TranslationStatusClient, configure intervals, and run a test that polls the server until completion or timeout.


4. How to Run This Program

Prerequisites
Java 17 or above installed.
Maven installed
(1) Clone or Download This Project
```
git clone https://github.com/MQC88/Video-translation-status-simulation.git
```
(2) Run the Server
Open one terminal inside the project folder.

(Optional) Configure environment variables. On Windows, use set.
```
set PROCESSING_DELAY=60
set FINAL_STATE=completed
```

On Mac/Linux, use export.
```
export PROCESSING_DELAY=60
export FINAL_STATE=completed
```
For example, `set PROCESSING_DELAY=90 set FINAL_STATE=completed` will make the server return "pending" for 90 seconds, then "completed".

`mvn compile exec:java -Dexec.mainClass=Server`

This starts the server on http://localhost:5000.

(3) Run the Integration Test

Open another terminal (leaving the server running).
(Optional) Adjust the parameters in IntegrationTest.java if you want to set different `initialInterval`, `maxInterval`, or `timeout values`.
Run the test:
```
mvn compile exec:java -Dexec.mainClass=IntegrationTest
```
Watch the console output to see the client polling. 

Stop the server at any time by pressing Ctrl + C in the server’s terminal.

(4) Mimicking a Random Video Transfer Process

You can randomize your environment variables or set them to different values each time. For instance:
```
set PROCESSING_DELAY=150
set FINAL_STATE=error
```
This setup returns {"result":"pending"} for 150 seconds, then {"result":"error"}.
By changing these on each run, you produce random scenarios.

(5) Changing Polling Logic
If you need to change the `initialInterval`, `maxInterval`, or `timeout` for the polling logic, open IntegrationTest.java and modify:
```
double initialInterval = 1.0; // seconds
double maxInterval = 8.0;    // seconds
int timeout = 60;            // seconds
```
Then re-run 
```
mvn compile exec:java -Dexec.mainClass=IntegrationTest.
```


5. Integration Tests

This section outlines a set of integration tests designed to simulate different server video translation scenarios. Each test is configured through environment variables to mimic various outcomes and behaviors.

(1) Successful Video Translation Process

This test simulates a successful video translation. Configure the environment as follows:

```
set PROCESSING_DELAY=40
set FINAL_STATE=completed
```
Test Outcome: The results of this test are documented below.

![Screenshot 2024-12-21 193304](https://github.com/user-attachments/assets/39b2ae8c-37d7-447c-a6d4-cf4346704e34)


(2) Video Translation Process with an Error

This test introduces an error state in the video translation process. Set up the environment variables as shown:

```
set PROCESSING_DELAY=40
set FINAL_STATE=error
```
Test Outcome: The results of this test are provided below.

![Screenshot 2024-12-21 193457](https://github.com/user-attachments/assets/c077a1e3-700b-4a0b-b852-d5fc95b5e2e1)


(3) Increased Polling Frequency

This test keeps the server behavior the same but increases the frequency of polling by adjusting the intervals. 
```
set PROCESSING_DELAY=40
set FINAL_STATE=completed
```
Change the polling intervals on the client side:

Initial Interval: Reduce from 1 second to 0.5 seconds.
Maximum Interval: Reduce from 8 seconds to 4 seconds.

Test Outcome: The results are shown below, demonstrating the effect of more frequent polling.

![Screenshot 2024-12-21 193907](https://github.com/user-attachments/assets/2b670f11-34c4-421b-88fb-263166385ae2)


(4) Timeout Due to Extended Processing

This test checks the system's handling of a timeout caused by an extended processing delay:

```
set PROCESSING_DELAY=80
set FINAL_STATE=completed
```
With a processing delay set to 80 sec, exceeding the 60 sec timeout threshold, this test evaluates the timeout behavior.

Test Outcome: The results are documented below, showing the timeout response.

![Screenshot 2024-12-21 194156](https://github.com/user-attachments/assets/d1548740-286f-439f-86e8-211b212b72e2)
