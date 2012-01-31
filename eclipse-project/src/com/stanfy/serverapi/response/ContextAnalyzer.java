package com.stanfy.serverapi.response;

import android.content.Context;
import android.net.Uri;

/**
 * Parser context analyzer.
 * It can perform additional operations on retrieved after parsing models and store this data with content resolver.
 * @param <T> context type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class ContextAnalyzer<T extends ParserContext> {

  /**
   * @param parserContext context to analyze
   * @param systemContext system context
   * @return results URI
   */
  public abstract Uri analyze(final T parserContext, final Context systemContext);

}
