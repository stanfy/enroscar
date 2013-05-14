Enroscar-Net
============

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
