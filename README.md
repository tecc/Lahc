# Lightweight Asynchronous HTTP Client (LAHC)

This is a very simple and lightweight HTTP client for Java 8+. For bigger projects, use other HTTP client libraries like [Apache's HTTPComponents Client](https://hc.apache.org/httpcomponents-client-5.1.x/) or [OkHttp](https://squareup.github.io/okhttp).

## Usage

> Note: Usage is subject to change.

### Simple request

```java
// Create an HttpClient with its default settings
HttpClient client = new HttpClient();

// Create a request
HttpRequest request = new HttpRequest()
        // the index.html of example.com - you need to be explicit because Lahc doesn't automatically do this 
        .url("https://example.com/index.html");

// Execute the request using the client
Future<HttpResponse> responseFuture = client.execute(request);
// Note that getting the result can throw exceptions - handle them however you want!
try {
    HttpResponse response = responseFuture.get();
    // Lahc creates toString() methods for debugging purposes
    // Very neat! - tecc
    System.out.println("Response: \n" + response)
} catch (Exception e) {
    // Example exception handling - once again, handle how you want
    e.printStackTrace();
}
```


## Licence

Lahc is licensed under the MIT licence. The licence text is available in the [LICENSE.txt](./LICENCE.txt) file.