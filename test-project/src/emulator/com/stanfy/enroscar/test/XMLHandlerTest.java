package com.stanfy.enroscar.test;

import android.test.AndroidTestCase;

/**
 * Tests for XMLHandler.
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 */
public class XMLHandlerTest extends AndroidTestCase {

  //private static final String TAG = XMLHandlerTest.class.getSimpleName();

  /** XML. */
  private static final String XML =
    "<root>"
    + "<values>"
      + "<bool>1</bool>"
      + "<int>1</int>"
      + "<long>2</long>"
      + "<date>1990-12-18 04:56:00 +2</date>"
    + "</values>"
    + "<str>test</str>"
  + "</root>";

//  /** Descriptor instance. */
//  private static final Descriptor<?> DESCRIPTOR = new DescriptorBuilder()
//  .root("root")
//    .element("values", TestProcessor.DESCRIPTOR)
//  .build();
//
//  /** Handler. */
//  private ResponseHanlder handler = null;
//
//  /* (non-Javadoc)
//   * @see android.test.AndroidTestCase#setUp()
//   */
//  @Override
//  protected void setUp() throws Exception {
//    super.setUp();
//    final XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
//    parser.setInput(new StringReader(XML));
//    handler = new XMLHandler(parser, new TestContext(), DESCRIPTOR);
//  }
//
//  /**
//   * Test method for {@link com.stanfy.serverapi.XMLHandler#handleResponse()}.
//   * @throws RequestMethodException
//   */
//  public final void testHandleResponse() throws RequestMethodException {
//    handler.handleResponse();
//  }
//
//  /**
//   * Tests context.
//   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
//   */
//  private static class TestContext extends ParserContext {
//
//    {
//      defineSimpleResultHandler(new SimpleResultHandler() {
//        @Override
//        public void handleValue(final String name, final String value) {
//          assertEquals("str", name);
//          assertEquals("test", value);
//        }
//      });
//    }
//
//    @Override
//    public void postData(final Object d) {
//      assertNotNull(d);
//      assertTrue(d instanceof ArrayList<?>);
//      assertFalse(((ArrayList<?>)d).isEmpty());
//      for (final Object data : (ArrayList<?>)d) {
//        assertNotNull(data);
//        if (data instanceof Boolean) {
//          assertEquals(Boolean.TRUE, data);
//        } else if (data instanceof Integer) {
//          assertEquals(Integer.valueOf(1), data);
//        } else if (data instanceof Long) {
//          assertEquals(Long.valueOf(2), data);
//        } else if (data instanceof Date) {
//          try {
//            assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse("1990-12-18 04:56:00 +2"), data);
//          } catch (final ParseException e) {
//            fail(e.getMessage());
//          }
//        } else {
//          fail("Wrong data: " + data.toString());
//        }
//      }
//    }
//  }
//
//  /**
//   * Test processor.
//   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
//   */
//  private static class TestProcessor extends ElementProcessor {
//    /** Descriptor. */
//    public static final Descriptor<TestProcessor> DESCRIPTOR = new Descriptor<TestProcessor>() {
//      @Override
//      public TestProcessor createProcessor() { return new TestProcessor(); }
//    };
//
//    /** Result list. */
//    private ArrayList<Object> list = new ArrayList<Object>();
//
//    @Override
//    public boolean processValue(final String name, final String value) {
//      if ("bool".equals(name)) {
//        list.add(parseBoolean(value));
//        return true;
//      }
//
//      if ("int".equals(name)) {
//        list.add(parseInt(value));
//        return true;
//      }
//
//      if ("long".equals(name)) {
//        list.add(parseLong(value));
//        return true;
//      }
//
//      if ("date".equals(name)) {
//        list.add(parseDate(value));
//        return true;
//      }
//
//      list.add(value);
//      return true;
//    }
//
//    @Override
//    public void syncData() {
//     getContext().postData(list);
//    }
//  }

}
