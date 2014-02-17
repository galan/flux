Efficient lightweight client-library for easy fluent http/rest requests in Java.

# Features
* HTTP methods GET, PUT, POST, DELETE, HEAD, TRACE and OPTIONS
* Basic-Authentication support
* Proxy support (with authentication)
* Configurable retries
* Configurable default timeouts and headers

# Integration with Maven
Use the [maven repository and artifact][1] on github.

# Example usage
## Requests
### Creating Requests
Simplest case

    Response response = Flupi.request("http://host/resource").get();
    System.out.println(response.getStreamAsString()); // Work with response, eg. print body
    response.close();

Using AutoCloseable

    try (Response response = Flupi.request("http://host/resource").get()) {
        System.out.println(response.getStreamAsString());
    }

### Methods
Calling the HTTP methods executes the http requests and returns a `Response`.

    Flupi.request("http://host/resource").get();

The following methods are available:

* `get()`
* `put()`
* `post()`
* `delete()`
* `head()`
* `trace()`
* `options()`
* `method(Method method)`
    
### Proxy
Enable a proxy by passing the proxy to a `proxy(..)` method.

    Flupi.request("http://host/resource").proxy("1.2.3.4:8080").get();

If the proxy requires authentication use `proxyAuthentication(String username, String password)`.

    Flupi.request("http://host/resource").proxy("1.2.3.4:8080").proxyAuthentication("user", "pass").get();

### Query parameters
You can add parameter by calling the fluent `parameter`methods, adding multiple parameter with the same name is possible.

    Flupi.request("http://host/resource").parameter("key", "value").get();
    Flupi.request("http://host/resource").parameter("q", "search").paramter("page", "2").get();
    Flupi.request("http://host/resource").parameter("cities", "hamburg", "halle").get();

Additional methods for rarely used arguments are also available:

* `paramter(Map<String, String>)`
* `parameterList(Map<String, List<String>>)`.

### Body
You can pass a body by calling body

    Flupi.request("http://host/resource").body("Hello World").get();
    Flupi.request("http://host/resource").body(new byte[]{72, 101, 108, ...}).get();

### Timeout
Setting the default connection and read timeout at once (can be overriden using the builder)

    Flupi.setDefaultTimeout(2500L); // using 2,5 seconds default
    Flupi.setDefaultTimeout("2s500ms"); // using 2,5 seconds default
    Flupi.setDefaultTimeout("1m 5s"); // using 1 minutes and 5 seconds default

Setting the timeout using the builder

    Flupi.request("http://host/resource").timeout(2500L).get(); // using 2,5 seconds
    Flupi.request("http://host/resource").timeout("2s500ms").get(); // using 2,5 seconds
    Flupi.request("http://host/resource").timeout("1m 5s").get(); // using 1 minutes and 5 seconds

Setting the connection and read timeout independently

    Flupi.setDefaultTimeoutConnection("5s");
    Flupi.setDefaultTimeoutRead("1m");
    Flupi.request("http://host/resource").timeoutConnection("5s").get();
    Flupi.request("http://host/resource").timeoutRead("1m").get();

### Retries
Failed requests can be retried automatically, enable by setting the amount of retries and the time between the retries. If the retries also fail, the last failed response will be returned.

    // repeat 2 times with 20 seconds in between
    Flupi.request("http://host/resource").retries(2L, "10s").get(); 

## Response
The `Response` returned from the HttpClient contains status information as well as an open connection to retrieve the stream (therefor the required `close()`or `AutoClosable` - benefit: you don't have to retrieve the whole content if you do not require it).

Actions that can be performed on the `Response` object:
* `getStream()` - Returns the raw body `InputStream`
* `getStreamAsBytearray()`- Returns the raw body as Byte array (`byte[]`)
* `getStreamAsString()` - Returns the body as String (UTF-8 is assumed)
* `getStreamAsString(String encoding)` - Returns the body as String (`encoding` is used to decode the String)
* `storeStream(File file)` - Stores the raw body in the given file
* `storeStream(File file, boolean gunzip)` - Stores the raw body in the given file with the option to gzip it
* `getContentEncoding()` - Returns the "Content-Encoding" header
* `getContentType()` - Returns the "Content-Type" header
* `getStatusCode()` - Returns the http status code
* `isFailed()` - Returns true if the status code is not succeded
* `isSucceded()` - Returns true if the status code is between 200 and 299
* `isInformational()` - Returns true if the status code is between 100 and 199
* `isRedirection()` - Returns true if the status code is between 300 and 399
* `isClientError()` - Returns true if the status code is between 400 and 499
* `isServerError()` - Returns true if the status code is between 500 and 599
* `getHeaderField(String name)` - Returns the value for a response header
* `getHeaderFields()` - Returns all response headers as `Map<String, String>`
* `close()` - Closes the response (stream); note that `Response` is `Autoclosable`

# Notes & Hints
* You don't have to use the static `Flupi` factory class, if you want to Inject the builder using a DI framework such as Guice, you can simply bind an instance of `FluentHttpClient`.
* Flupi is thread-safe
* For testing it is useful to know that you can utilize the `MockCommonHttpClient`. You can pass the `MockCommonHttpClient` with canned Responses.

  [1]: https://github.com/d8bitr/maven-repository
