Enroscar Net
============

Android networking library based on `URLConnection`.

Makes it possible to create `URLConnection`s for such schemes as

* `content`, `android.resource` - using Android's `ContentResolver`
```java
new URL("content://authority/data/1").openConnection()
```

* [the "data" URL scheme](https://www.ietf.org/rfc/rfc2397.txt)
```java
new URL("data://data:image/gif;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAAAMAAw"
    + "AAAC8IyPqcvt3wCcDkiLc7C0qwyGHhSWpjQu5yqmCYsapyuvUUlvONmOZtfzgFz"
    + "ByTB10QgxOR0TqBQejhRNzOfkVJ+5YiUqrXF5Y5lKh/DeuNcP5yLWGsEbtLiOSp"
    + "a/TPg7JpJHxyendzWTBfX0cxOnKPjgBzi4diinWGdkF8kjdfnycQZXZeYGejmJl"
    + "ZeGl9i2icVqaNVailT6F5iJ90m6mvuTS4OK05M0vDk0Q4XUtwvKOzrcd3iq9uis"
    + "F81M1OIcR7lEewwcLp7tuNNkM3uNna3F2JQFo97Vriy/Xl4/f1cf5VWzXyym7PH"
    + "hhx4dbgYKAAA7"").openConnection()
```

TODO...


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
