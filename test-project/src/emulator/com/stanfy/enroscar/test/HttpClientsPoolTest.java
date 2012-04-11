package com.stanfy.enroscar.test;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.stanfy.app.HttpClientsPool;

import android.test.AndroidTestCase;

/**
 * Testing HTTP clients pool.
 * @author Olexandr Tereshchuk (Stanfy - http://www.stanfy.com)
 */
public class HttpClientsPoolTest extends AndroidTestCase {

  /** HTTP clients pool. */
  private HttpClientsPool pool = null;
  
  protected void setUp() throws Exception {
    super.setUp();
    pool = new HttpClientsPool(getContext());
  }
  
  public final void testFlush() {
    final HttpClient client = pool.getHttpClient();
    pool.releaseHttpClient(client);
    pool.flush();
    assertNotSame(client, pool.getHttpClient());
  }

  public final void testGetHttpClient() {
    pool.flush();
    final HttpClient client = pool.getHttpClient();
    assertNotNull(client);
    assertNotNull(pool.getHttpClient());
  }

  public final void testReleaseHttpClient() {
    pool.flush();
    final HttpClient client = pool.getHttpClient();
    pool.releaseHttpClient(client);
    assertSame(client, pool.getHttpClient());
    pool.releaseHttpClient(null);
    assertNotNull(pool.getHttpClient());
    pool.flush();
    pool.releaseHttpClient(client);
    pool.releaseHttpClient(client);
    assertSame(client, pool.getHttpClient());
    assertNotSame(client, pool.getHttpClient());
    pool.flush();
  }
  
  public final void testConcurrent() throws InterruptedException {
    concurentTest(1, 2, 100);
    concurentTest(2, 1, 100);
  }

  public final void testDestroy() {
    pool.destroy();
  }
  
  private void concurentTest(final int p1, final int p2, final int count) throws InterruptedException {
    final Thread t2 = new Thread(new Runnable() {
      
      @Override
      public void run() {
        for (int i = 0; i < count; i++) {
          pool.releaseHttpClient(new DefaultHttpClient());
          if (i % 3 == 0) { pool.getHttpClient(); }
          if (i % 5 == 0) { pool.releaseHttpClient(pool.getHttpClient()); }
        }
      }
    });
    final Thread t1 = new Thread(new Runnable() {
      
      @Override
      public void run() {
        while (t2.isAlive()) {
          pool.flush();
        }
      }
    });
    
    t1.setPriority(p1);
    t2.setPriority(p2);
    t2.start();
    t1.start();
    t2.join();
    t1.join();
  }

}
