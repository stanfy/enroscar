Enroscar
=======

**An Android library focused on web-service clients implementation**

[![Build Status](https://secure.travis-ci.org/stanfy/enroscar.png?branch=master)](http://travis-ci.org/stanfy/enroscar)

Quick Start Guide
=================
Here you'll find information how to start using enroscar in few simple steps.

Declare application service in Android manifest file
----------------------------------------------------

Put the following lines to your AndroidManifest.xml file.
```xml      
  ...
  <application>
      ...

      <!-- Enroscar application service declaration. -->    
      <service
          android:name="com.stanfy.enroscar.rest.executor.ApplicationService"
          android:exported="false" >
          <intent-filter>
              <action android:name="com.stanfy.enroscar.rest.executor.ApiMethods" />
          </intent-filter>
      </service>

      ...

  </application>
```
Configure the enroscar components
---------------------------------

Create application class in your project that extends `android.app.Application` and put the following lines to its `onCreate` method.
```java
/**
 * Sample application.
 */
public class SampleApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    DefaultBeansManager.get(this).edit()
        .defaults()                   // default components configuration
        .remoteServerApi("json")      // use JSON format to communicate with remote server
        .commit();
  }

}
```

Use `SimpleRequestBuilder`
--------------------------

Use `SimpleRequestBuilder` for creating Android loader that uses the declared service for performing requests to the remote server API.
Please, go through [Android loaders docs](http://developer.android.com/guide/components/loaders.html) to refresh in mind how to use loaders.

```java
  
public class ExampleFragment extends Fragment implements LoaderCallbacks<ResponseData<Profile>> {

  public static class Profile {
    private String name;
    private String description;

    public String getName() { return name; }
    public String getDescription() { return description; }
  }

  private static final int LOADER_ID = 1;

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(LOADER_ID, null, this);
  }

  @Override
  public Loader<ResponseData<Profile>> onCreateLoader(final int id, final Bundle args) {
    return new SimpleRequestBuilder<Profile>(getActivity()) { }
      .setUrl("https://api.twitter.com/1/users/show.json")
      .setFormat("json")
      .addParam("screen_name", "TwitterAPI")
      .getLoader();
  }

  @Override
  public void onLoadFinished(
      final Loader<ResponseData<Profile>> loader, 
      final ResponseData<Profile> response) {
    
    if (response.isSuccessful()) {
      GUIUtils.shortToast(getActivity(), profile.getName() + " / " + profile.getDescription());
    }
    
  }

  @Override
  public void onLoaderReset(final Loader<ResponseData<Profile>> loader) {
    // nothing
  }

}

```

Create your own request builders
--------------------------------

Extend `BaseRequestBuilder` class to create your own request builder that exposes convenient methods for request parameters configuration.

```java
/**
 * Custom builder for requests that load twitter user timeline.
 */
public class TweetsRequestBuilder extends BaseRequestBuilder<List<Tweet>> {

  public TweetsRequestBuilder(final Context context) {
    super(context);
    setTargetUrl("https://api.twitter.com/1/statuses/user_timeline.json");
  }

  public TweetsRequestBuilder setScreenname(final String name) {
    addSimpleParameter("screen_name", name);
    return this;
  }

}
```

And now use your own request builder to create a loader.

```java
public class TweetsAcivity extends FragmentActivity implements LoaderCallbacks<ResponseData<List<Tweet>> {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportLoaderManager().initLoader(0, null, this);
  }

  @Override
  public Loader<ResponseData<List<Tweet>> onCreateLoader(final int id, final Bundle args) {
    return new TweetsRequestBuilder(this)
        .setScreenname("TwitterAPI")
        .getLoader();
  }

  @Override
  public void onLoadFinished(Loader<ResponseData<List<Tweet>> loader, ResponseData<List<Tweet> data) {
    // display list
  }

  // ...
}
```

Task queues
-----------

Be default all the requests are processed one-by-one outside the main thread.
But you can specify the queue name used for your request processing. Requests that share the same queue are processed one-by-one. And multiple queues are processed in parallel.

Use `setTasksQueueName` method of your request builders to specify the queue name.
Default queue name is "default".

You may also call method `setParallel` on your request builder in order to schedule your request processing outside queues in a thread from the pool.
On Honeycomb and later Android versions `AsyncTask`'s thread pool is used. And a separate pool is created for earlier versions.

Usage
-----

Enroscar is published to Maven central repository. And you can use it with the following string in your Gradle build file:

```
compile 'com.stanfy.enroscar:enroscar-assist:1.0'
```


License
-------

     Copyright 2013 Stanfy Corp.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
