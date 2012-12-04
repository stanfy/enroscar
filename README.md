Enroscar
=======

**An Android library focused on web-service clients implementation**


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
          android:name="com.stanfy.app.service.ApplicationService"
          android:exported="false" >
          <intent-filter>
              <action android:name="com.stanfy.app.service.ApiMethods" />
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
    BeansManager.get(this).edit()
        .defaults()                   // default components configuration
        .remoteServerApi("json")      // use JSON format to communicate with remote server
        .commit();
    EnroscarConnectionsEngine.config().install(this);
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


