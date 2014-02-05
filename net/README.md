Enroscar Net
============

This Android library broadens what you can do with `URLConnection` adding support
for `content`, `android.resource`, and 'data' schemes.

After setting things up (TODO) you are able to use code like:
```java
// Open input stream for reading contact photo bytes
new URL("content://contacts/photos/1").openConnection().getInputStream();

// For reading text from another package resources
new URL("android.resource://another.app.package/raw/some_text.txt").openConnection();

// For getting bytes of a base64 encoded image
URLConnection connection = new URL("data:image/gif;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAAAMAAw"
    + "AAAC8IyPqcvt3wCcDkiLc7C0qwyGHhSWpjQu5yqmCYsapyuvUUlvONmOZtfzgFz"
    + "ByTB10QgxOR0TqBQejhRNzOfkVJ+5YiUqrXF5Y5lKh/DeuNcP5yLWGsEbtLiOSp"
    + "a/TPg7JpJHxyendzWTBfX0cxOnKPjgBzi4diinWGdkF8kjdfnycQZXZeYGejmJl"
    + "ZeGl9i2icVqaNVailT6F5iJ90m6mvuTS4OK05M0vDk0Q4XUtwvKOzrcd3iq9uis"
    + "F81M1OIcR7lEewwcLp7tuNNkM3uNna3F2JQFo97Vriy/Xl4/f1cf5VWzXyym7PH"
    + "hhx4dbgYKAAA7"").openConnection()
assert connection.getContentType() == "image/gif"
```

It also changes how `URLConnection` handles `file` scheme: passing URLs to a `ContentResolver`,
so that you may use URLConnection to access your application assets e.g.
```java
new URL("file:///android_assets/myDbFile.db").openConnection()
```

So what is the benefit, you ask? Actually it unifies how you access different sources of data,
making it possible to describe these different sources with a URI.
And power of this approach can be seen in
[integration with Retrofit library](#Retrofit-Integration).

Retrofit Integration
--------------------
Enroscar Net can be easily integrated with [Retrofit](https://github.com/square/retrofit).
After setting the library up (TODO) use Enroscar's
[`RetrofitClient`](src/main/java/com/stanfy/enroscar/net/retrofit/RetrofitClient.java)
to build a `RestAdapter`:
```java
RestAdapter adapter = new RestAdapter.Builder()
    .setClient(new RetrofitClient())
    .setEndpoint("https://my.backend.com")
    .build();
```

And now not only an HTTP server can be your endpoint, but a content provider as well:
```java
// content provider implementation
public class MyProvider extends ContentProvider {
  // ...

  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    return ParcelFileDescriptor.open(
        new File(getContext().getFilesDir(), "user.json"),
        ParcelFileDescriptor.MODE_READ_ONLY
    );
  }
  public String getType(Uri uri) {
    return "application/json";
  }

  // ...
}

// Your Retrofit interface
interface Service {
  @GET("/user")
  User getUser();
}

// REST adapter with content provider endpoint
RestAdapter adapter = new RestAdapter.Builder()
    .setClient(new RetrofitClient())
    .setEndpoint("content://my.package")
    .build();

// get your user
User user = adapter.create(Service.class).getUser();
```

Have a test that invokes one of your Retrofit interface methods and want to mock its response?
Use an adapter with a `data` URI as an endpoint.
```java
RestAdapter adapter = new RestAdapter.Builder()
    .setClient(new RetrofitClient())
    .setEndpoint("data:application/json," + Uri.encode("{\"name\": \"John\"}") + "?")
    .build();
Service service = adapter.create(Service.class);

assertThat(service.getUser().getName()).equalsTo("John");
```
Note: a trailing `?` in the `data` URI is used by `RetrofitClient` to remove any paths or query
parameters from your service method annotations.


TODO: decide about lines below

Analyzing network traffic
-------------------------

Every request you build with `RequestBuilder` can be tagged for futher debugging with [DDMS Network Traffic tool](http://developer.android.com/tools/debugging/ddms.html#network).

Example:

```java
	// ...
	new SimpleRequestBuilder<Person>(context) {}
		.setUrl("http://www.example.com/api/1/person.json")
		.setTafficStatsTag(5);
	// ...
```

Value provided with `setTafficStatsTag` method will be used as an argument for [TrafficStats.setThreadStatsTag](http://developer.android.com/reference/android/net/TrafficStats).html#setThreadStatsTag(int)).

Strings can also be used for tagging.

```java
	// ...
	new SimpleRequestBuilder<Person>(context) {}
		.setUrl("http://www.example.com/api/1/person.json")
		.setTafficStatsTag("example.com");
	// ...
```

In this case value passed to [TrafficStats.setThreadStatsTag](http://developer.android.com/reference/android/net/TrafficStats) is calculated from string hash code.
If `RemoteServerApiConfig.setDebugRest` is set to `true`, you'll see correspondance between strings and ints in LogCat under "RequestBuilder" tag.
