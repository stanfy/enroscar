package com.stanfy.enroscar.test;

import java.util.LinkedList;

import android.content.Context;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.text.TextUtils;
import android.util.Log;

import com.stanfy.images.BuffersPool;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.XMLRequestMethod;
import com.stanfy.serverapi.request.Operation;
import com.stanfy.serverapi.request.OperationType;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.request.RequestExecutor;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.serverapi.response.xml.ElementProcessor;
import com.stanfy.serverapi.response.xml.ElementProcessor.Descriptor;
import com.stanfy.serverapi.response.xml.ElementProcessor.DescriptorBuilder;

/**
 * Tests for XMLRequestMethod.
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 */
public class XMLRequestMethodTest extends AndroidTestCase {

  /** Logging tag. */
  private static final String TAG = XMLRequestMethodTest.class.getSimpleName();

  /** URL. */
  public static final String URL = "http://dev.stanfy.com:8086/currencies";

//  <root>
//    <item>
//      <code>2249</code>
//      <char3>USD</char3>
//      <name>Американский доллар</name>
//      <size>100</size>
//      <rate>797.17</rate>
//      <change>-0.06000000000005912</change>
//    </item>
//    <item>
//      <code>2250</code>
//      <char3>EUR</char3>
//      <name>Евро</name>
//      <size>100</size>
//      <rate>1152.1498</rate>
//      <change>2.1454999999998563</change>
//    </item>
//    <item>
//      <code>2251</code>
//      <char3>RUB</char3>
//      <name>Российский рубль</name>
//      <size>10</size>
//      <rate>2.86</rate>
//      <change>0.020399999999999974</change>
//    </item>
//  </root>

  /**
   * Test method for {@link com.stanfy.serverapi.XMLHandler#handleResponse()}.
   * @throws RequestMethodException
   */
  public final void testHandleResponse() throws RequestMethodException {
    final ParserContext context = new TestContext();
    final Application app = (Application) getContext().getApplicationContext();
    final TestRequestBuilder rb = new TestRequestBuilder(getContext(), new TestRequestExecutor(app, context));
    rb.execute();
  }

  /**
   * Tests context.
   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
   */
  public static class TestContext extends ParserContext {

    /** Parsed currency items. */
    final LinkedList<Item> items = new LinkedList<XMLRequestMethodTest.Item>();

    @Override
    public void postData(final Object data) {
      assertNotNull(data);
      assertTrue(data instanceof Item);
      final Item item = (Item)data;
      assertTrue(item.code > 0);
      assertTrue(item.size > 0);
      assertTrue(item.rate > 0);
      assertTrue(item.code > 0);
      assertNotNull(item.char3);
      assertTrue(item.char3.length() == 3);
      assertFalse(TextUtils.isEmpty(item.name));
      items.add(item);
      Log.d(TAG, item.toString());
    }

    @Override
    protected ResponseData createResponseData(final Uri data) {
      assertTrue(items.size() == 3);
      return super.createResponseData(data);
    }

  }

  /**
   * Test processor.
   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
   */
  private static class TestProcessor extends ElementProcessor {
    /** Descriptor. */
    public static final Descriptor<TestProcessor> DESCRIPTOR = new Descriptor<TestProcessor>() {
      @Override
      public TestProcessor createProcessor() { return new TestProcessor(); }
    };

    /** Currency item. */
    private Item item = new Item();

    @Override
    public boolean processValue(final String name, final String value) {

      if ("code".equals(name)) {
        item.code = parseLong(value);
        return true;
      }

      if ("char3".equals(name)) {
        item.char3 = value;
        return true;
      }

      if ("name".equals(name)) {
        item.name = value;
        return true;
      }

      if ("size".equals(name)) {
        item.size = parseInt(value);
        return true;
      }

      if ("rate".equals(name)) {
        item.rate = parseDouble(value);
        return true;
      }

      if ("change".equals(name)) {
        item.change = parseDouble(value);
        return true;
      }

      return false;
    }

    @Override
    public void syncData() {
     getContext().postData(item);
    }
  }

  /**
   * Currency item.
   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
   */
  private static class Item {
    /** Code. */
    long code = -1;
    /** Short name. */
    String char3 = "";
    /** Name. */
    String name = "";
    /** Amount. */
    int size = -1;
    /** Rate. */
    double rate = -1;
    /** Change. */
    double change = -1;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "Item [code=" + code + ", char3=" + char3 + ", name=" + name
          + ", size=" + size + ", rate=" + rate + ", change=" + change + "]";
    }

  }

  /**
   * Test operation.
   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
   */
  private static class TestOperation implements Operation {

    /** This operation code. */
    public static final int TEST_OP_CODE = 1;

    @Override
    public int getCode() { return TEST_OP_CODE; }

    @Override
    public int getType() { return OperationType.SIMPLE_GET; }

    @Override
    public String getUrlPart() { return XMLRequestMethodTest.URL; }

  }

  /**
   * Test request builder.
   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
   */
  private static class TestRequestBuilder extends RequestBuilder {

    public TestRequestBuilder(final Context context, final RequestExecutor executor) {
      super(context, executor);
    }

    @Override
    public Operation getOperation() { return new TestOperation(); }

  }

  /**
   * Test request executor.
   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
   */
  private static class TestRequestExecutor implements RequestExecutor {

    /** Descriptor instance. */
    private static final Descriptor<?> DESCRIPTOR = new DescriptorBuilder()
    .root("root")
      .element("item", TestProcessor.DESCRIPTOR)
    .build();

    /** Request method. */
    private XMLRequestMethod method;
    /** Parser context. */
    private ParserContext context;
    /** System context. */
    private Context sysContext;

    public TestRequestExecutor(final Application app, final ParserContext context) {
      this.method = new XMLRequestMethod(null, new BuffersPool(new int[][] {{1, 1}}), DESCRIPTOR);
      this.context = context;
      method.setup(app);
      sysContext = app;
    }

    @Override
    public void performRequest(final RequestDescription rd) {
      try {
        // don't put me in background
        method.start(sysContext, rd, context);
      } catch (final RequestMethodException e) {
        Log.e(TAG, "Was unable to make request", e);
        fail("Was unable to make request");
      }
    }

  }

}
