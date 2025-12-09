package com.weather;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;

import java.net.URL;
import java.util.Optional;

/**
 * Weather XML-RPC Client
 *
 * ZGODNE Z LISTĄ Cw0:
 * - XmlRpcClientConfigImpl
 * - XmlRpcClient
 * - execute() - synchronous
 * - executeAsync() - asynchronous
 * - TimingOutCallback (short + long timeout)
 * - waitForResponse()
 */
public class WeatherXmlRpcClient {

    private static final String SERVER_URL = "http://127.0.0.1:8099/xmlrpc";

    public static void main(String[] args) throws Throwable {

        System.out.println("========================================");
        System.out.println("  Weather XML-RPC Client - Test Suite");
        System.out.println("========================================\n");

        // ========== KONFIGURACJA - ZGODNA Z LISTĄ! ==========
        System.out.println("Configuring XML-RPC client...");
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(SERVER_URL));
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000); // 60 sec
        config.setReplyTimeout(60 * 1000);      // 60 sec

        // Create client - ZGODNE Z LISTĄ!
        XmlRpcClient client = new XmlRpcClient();
        client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
        client.setConfig(config);
        System.out.println("✅ Client configured\n");

        // ========== TEST 1: SYNCHRONOUS CALL ==========
        System.out.println("========================================");
        System.out.println("TEST 1: Synchronous Call");
        System.out.println("========================================");
        testSynchronousCall(client);

        // ========== TEST 2: ASYNC WITH SHORT TIMEOUT (will timeout) ==========
        System.out.println("\n========================================");
        System.out.println("TEST 2: Asynchronous Call (short timeout)");
        System.out.println("========================================");
        testAsyncShortTimeout(client);

        // ========== TEST 3: ASYNC WITH LONG TIMEOUT (will succeed) ==========
        System.out.println("\n========================================");
        System.out.println("TEST 3: Asynchronous Call (long timeout)");
        System.out.println("========================================");
        testAsyncLongTimeout(client);

        // ========== TEST 4: BONUS - Statistics ==========
        System.out.println("\n========================================");
        System.out.println("TEST 4: Get Statistics (BONUS)");
        System.out.println("========================================");
        testStatistics(client);

        System.out.println("\n========================================");
        System.out.println("✅ All tests completed!");
        System.out.println("========================================");
    }

    /**
     * TEST 1: SYNCHRONOUS CALL - client.execute()
     * ZGODNE Z LISTĄ!
     */
    private static void testSynchronousCall(XmlRpcClient client) throws Exception {
        System.out.println("Calling: WeatherService.getHistory(\"Warsaw\", 5)");
        System.out.println("Method: execute() - SYNCHRONOUS");

        Object[] params = {"Warsaw", 5};
        Object result = client.execute("WeatherService.getHistory", params);

        System.out.println("✅ Response received:");
        System.out.println(result);
    }

    /**
     * TEST 2: ASYNC WITH SHORT TIMEOUT - will timeout!
     * ZGODNE Z LISTĄ! (callback_short_timelimit)
     */
    private static void testAsyncShortTimeout(XmlRpcClient client) throws XmlRpcException {
        System.out.println("Calling: WeatherService.getHistoryWithDelay(\"London\", 3)");
        System.out.println("Method: executeAsync() - ASYNCHRONOUS");
        System.out.println("Timeout: 1000ms (1 second)");
        System.out.println("Server delay: 3000ms (3 seconds)");
        System.out.println("Expected: TIMEOUT ❌\n");

        // TimingOutCallback z krótkim timeoutem - ZGODNE Z LISTĄ!
        TimingOutCallback callbackShort = new TimingOutCallback(1000); // 1 sec

        Object[] params = {"London", 3};
        client.executeAsync("WeatherService.getHistoryWithDelay", params, callbackShort);

        System.out.println("Request sent (async, non-blocking)...");

        Optional<Object> response = getAsyncResponse(callbackShort);
        if (response.isEmpty()) {
            System.out.println("Result: TIMEOUT (as expected) ❌");
        } else {
            System.out.println("Result: " + response.get());
        }
    }

    /**
     * TEST 3: ASYNC WITH LONG TIMEOUT - will succeed!
     * ZGODNE Z LISTĄ! (callback_long_timelimit)
     */
    private static void testAsyncLongTimeout(XmlRpcClient client) throws XmlRpcException {
        System.out.println("Calling: WeatherService.getHistoryWithDelay(\"Paris\", 3)");
        System.out.println("Method: executeAsync() - ASYNCHRONOUS");
        System.out.println("Timeout: 5000ms (5 seconds)");
        System.out.println("Server delay: 3000ms (3 seconds)");
        System.out.println("Expected: SUCCESS ✅\n");

        // TimingOutCallback z długim timeoutem - ZGODNE Z LISTĄ!
        TimingOutCallback callbackLong = new TimingOutCallback(5000); // 5 sec

        Object[] params = {"Paris", 3};
        client.executeAsync("WeatherService.getHistoryWithDelay", params, callbackLong);

        System.out.println("Request sent (async, non-blocking)...");
        System.out.println("Waiting for response...");

        Optional<Object> response = getAsyncResponse(callbackLong);
        if (response.isPresent()) {
            System.out.println("✅ Response received:");
            System.out.println(response.get());
        }
    }

    /**
     * TEST 4: BONUS - Get statistics
     */
    private static void testStatistics(XmlRpcClient client) throws Exception {
        System.out.println("Calling: WeatherService.getStatistics(\"Warsaw\")");

        Object[] params = {"Warsaw"};
        Object result = client.execute("WeatherService.getStatistics", params);

        System.out.println("✅ Statistics received:");
        System.out.println(result);
    }

    /**
     * Helper method - wait for async response
     * ZGODNE Z LISTĄ! (getAsyncResponse z Optional)
     */
    private static Optional<Object> getAsyncResponse(TimingOutCallback callback) {
        try {
            Object response = callback.waitForResponse();
            return Optional.ofNullable(response);
        } catch (TimingOutCallback.TimeoutException e) {
            System.out.println("⏱️  Timeout: No response from server before timeout.");
        } catch (Throwable e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        return Optional.empty();
    }
}