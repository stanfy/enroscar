package com.stanfy.enroscar.rest.response;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.rest.RequestMethod.RequestMethodException;
import com.stanfy.enroscar.rest.request.RequestDescription;


/**
 * An analyzers that transforms one item response to response with a list that contains that item.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 * @param <T> item type
 */
@EnroscarBean(ItemToListTransformer.BEAN_NAME)
public class ItemToListTransformer<T> implements ContentAnalyzer<T, List<T>> {

  /** Bean name. */
  public static final String BEAN_NAME = "ItemToListTransformer";

  @Override
  public ResponseData<List<T>> analyze(final Context context, final RequestDescription description, final ResponseData<T> responseData) throws RequestMethodException {
    final ResponseData<List<T>> result = new ResponseData<List<T>>(responseData);
    if (result.isSuccessful()) {
      final ArrayList<T> list = new ArrayList<T>(1);
      list.add(responseData.getModel());
      result.setModel(list);
    }
    return result;
  }

}
