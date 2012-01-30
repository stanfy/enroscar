package com.stanfy.serverapi;

import com.stanfy.images.BuffersPool;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ParserContext;

/**
 * Contains factory methods for request descriptions and parser contexts.
 * Should be instantiated by application object.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class RequestMethodHelper {

  /** XML type. */
  public static final int TYPE_XML = 1;
  /** JSON type. */
  public static final int TYPE_JSON = 2;

  /** Request method type. */
  private final int type;

  /** Cache authority. */
  private final String cacheAuthority;

  /** Buffers pool. */
  private final BuffersPool buffersPool = new BuffersPool(new int[][] {
    {1, 8192}
  });

  /**
   * Create a helper with the defined main API format.
   * @param type main API format (XML - {@link #TYPE_XML} or JSON - {@link #TYPE_JSON})
   * @param authority cache content authority (null is ok)
   */
  public RequestMethodHelper(final int type, final String authority) {
    this.cacheAuthority = authority;
    switch (type) {
    case TYPE_JSON:
    case TYPE_XML:
      break;
    default:
      throw new UnsupportedOperationException("Unknow request method type " + type);
    }
    this.type = type;
  }

  /**
   * @param requestDescription request description instance
   * @return new request method
   */
  public RequestMethod createRequestMethod(final RequestDescription requestDescription) {
    switch (type) {
    case TYPE_JSON: return new JSONRequestMethod(cacheAuthority, buffersPool);
    default:
      return null;
    }
  }

  /** @return new request description instance */
  public RequestDescription createRequestDescription() { return new RequestDescription(); }

  /**
   * @param requestDescription request description instance
   * @return new parser context instance
   */
  public ParserContext createParserContext(final RequestDescription requestDescription) { return new ParserContext(); }

  /**
   * Release resources.
   */
  public void flush() {
    buffersPool.flush();
  }

  /** @return the cacheAuthority */
  public String getCacheAuthority() { return cacheAuthority; }
  /** @return the type */
  public int getType() { return type; }
  /** @return the buffersPool */
  public BuffersPool getBuffersPool() { return buffersPool; }

}
