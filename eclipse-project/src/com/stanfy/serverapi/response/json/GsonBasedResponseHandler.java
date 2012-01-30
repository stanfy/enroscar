package com.stanfy.serverapi.response.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.ResponseHanlder;

/**
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
public class GsonBasedResponseHandler extends ResponseHanlder {

  /** Default date format. */
  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

  /** Charset. */
  public  static final String CHARSET = "UTF-8";

  /** Builder. */
  public static final GsonBuilder GBUILDER = new GsonBuilder().setDateFormat(DEFAULT_DATE_FORMAT);

  /** Deserializer instance. */
  private final Gson gson;

  /** Input. */
  private final InputStream input;

  public GsonBasedResponseHandler(final InputStream input, final ParserContext context) {
    this(input, context, GBUILDER);
  }

  public GsonBasedResponseHandler(final InputStream input, final ParserContext context, final GsonBuilder gsonBuilder) {
    super(context);
    gson = gsonBuilder.create();
    this.input = input;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleResponse() throws RequestMethodException {
    @SuppressWarnings("rawtypes")
    final OneClassModelParserContext context = (OneClassModelParserContext<?>)getContext();
    try {
      final Serializable model = gson.fromJson(new InputStreamReader(input, CHARSET), context.getTypeToken().getType());
      context.defineModel(model);
    } catch (final IOException e) {
      throw new RequestMethodException(e);
    } finally {
      try {
        input.close();
      } catch (final IOException e) {
        throw new RequestMethodException(e);
      }
    }
  }

  @Override
  public String dumpState() { return gson.toString(); }

}
