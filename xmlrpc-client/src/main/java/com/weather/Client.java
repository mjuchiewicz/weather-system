package com.weather;

import org.apache.xmlrpc.client.TimingOutCallback;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class Client {

    public static void main(String[] args) throws Throwable {
        System.out.println("Starting XML-RPC Client...");

        // Create configuration
        var config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL("http://127.0.0.1:8090/xmlrpc"));
        config.setEnabledForExtensions(true);
        config.setConnectionTimeout(60 * 1000);
        config.setReplyTimeout(60 * 1000);

        // Create client and set settings
        var client = new org.apache.xmlrpc.client.XmlRpcClient();
        client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
        client.setConfig(config);

        // Synchronous call
        System.out.println("Executing synchronous call...");
        var result = client.execute("MessagesService.echo", List.of("Message One [1]"));
        System.out.println(result);

        // Create callbacks with different timeouts
        var callback_short_timelimit = new TimingOutCallback(1000);  // 1 second - zbyt krótki!
        var callback_long_timelimit = new TimingOutCallback(5 * 1000);  // 5 seconds - OK

        // Asynchronous calls
        System.out.println("Executing asynchronous call...");
        client.executeAsync("MessagesService.echoWithDelay",
                List.of("Message Two [2]"), callback_short_timelimit);

        System.out.println("Executing asynchronous call...");
        client.executeAsync("MessagesService.echoWithDelay",
                List.of("Message Three [3]"), callback_long_timelimit);

        // Wait for responses
        System.out.println(getAsyncResponse(callback_short_timelimit));
        System.out.println(getAsyncResponse(callback_long_timelimit));

        System.out.println("Client finished.");
    }

    // Method to wait for responses and print failures
    public static Optional<Object> getAsyncResponse(TimingOutCallback callback) {
        try {
            return Optional.ofNullable(callback.waitForResponse());
        } catch (TimingOutCallback.TimeoutException e) {
            System.out.println("No response from server before timeout.");
        } catch (Throwable e) {
            System.out.println("Server returned an error message.");
        }
        return Optional.empty();
    }
}