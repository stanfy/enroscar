package com.stanfy.serverapi.response.json;

import java.io.Serializable;

import com.google.gson.reflect.TypeToken;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.Response;


/**
 * Parser context focused on using models with Gson library.
 * @param <T> model type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class OneClassModelParserContext<T extends Serializable> extends ParserContext {

  /** Model instance. */
  private T model;

  /** Model class. */
  private final TypeToken<T> typeToken;

  public OneClassModelParserContext() { this(null); }

  public OneClassModelParserContext(final TypeToken<T> type) { this.typeToken = type; }

  public static <MT extends Serializable> OneClassModelParserContext<MT> create(final TypeToken<MT> token) {
    return new OneClassModelParserContext<MT>(token);
  }

  /** @return the typeToken */
  public TypeToken<T> getTypeToken() { return typeToken; }

  @Override
  public Serializable getModel() { return model; }

  /** This method also should set a response instance. */
  public void defineModel(final T model) {
    defineResponse(new Response());
    this.model = model;
  }

}
