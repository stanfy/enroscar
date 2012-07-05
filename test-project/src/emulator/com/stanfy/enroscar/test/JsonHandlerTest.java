package com.stanfy.enroscar.test;

import android.test.AndroidTestCase;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class JsonHandlerTest extends AndroidTestCase {

  /** JSON. */
  private String json = "{"
    + "\"id\": 2584,"
    + "\"categoryId\": 1,"
    + "\"name\": \"Restaurants \u0026 Cafes\""
    + "}";

//  /** Response handler. */
//  private ResponseHanlder handler;
//
//  /** Parser context. */
//  private ParserContext context;
//
//  @Override
//  protected void setUp() throws Exception {
//    super.setUp();
//    context = new InfoParserContext();
//    handler = new GsonBasedResponseHandler(new ByteArrayInputStream(json.getBytes()), context);
//  }
//
//  public void testHandler() throws Exception {
//    handler.handleResponse();
//    final Info model = (Info)context.getModel();
//    assertEquals(1, model.getCategoryId());
//    assertEquals("Restaurants & Cafes", model.getName());
//    final int id = 2584;
//    assertEquals(id, model.getId());
//  }
//
//  /** Parser context. */
//  private static class InfoParserContext extends OneClassModelParserContext<Info> {
//    public InfoParserContext() { super(new TypeToken<Info>() { }); }
//  }
//
//  /** Model class. */
//  private static class Info implements Serializable {
//    /** serialVersionUID. */
//    private static final long serialVersionUID = 7757769234099486430L;
//
//    /** Identifier. */
//    private int id;
//    /** Some id. */
//    private int categoryId;
//    /** Name. */
//    private String name;
//
//    /** @return the id */
//    public int getId() { return id; }
//    /** @return the categoryId */
//    public int getCategoryId() { return categoryId; }
//    /** @return the name */
//    public String getName() { return name; }
//
//  }

}
