package com.stanfy.app.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import android.content.SharedPreferences;
import android.net.Uri;

import com.stanfy.app.service.ApiMethodsImpl.APICallInfoData;
import com.stanfy.serverapi.request.Operation;
import com.stanfy.serverapi.response.ResponseData;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ApiOperationDataTest extends AbstractApiMethodsImplTest {

  /** Test storage. */
  private SharedPreferences storage;

  @Override
  public void setup() {
    super.setup();
    storage = appService.getSharedPreferences("test", 0);
  }

  /**
   * Test how operation data is stored and loaded.
   */
  @Test
  public void testLastApiOperationData() {
    final APICallInfoData opData = apiMethodsImpl.lastOperation;
    assertNotNull(opData);

    opData.set(1, 2); // operation, token
    assertThat(opData.operation, equalTo(1));
    assertThat(opData.token, equalTo(2));

    final ResponseData rd = new ResponseData(Uri.parse("http://test"), null);
    rd.setErrorCode(3);
    rd.setMessage("test");
    opData.set(rd);
    assertThat(opData.responseData.getErrorCode(), equalTo(3));
    assertThat(opData.responseData.getMessage(), equalTo("test"));
    assertThat(opData.responseData.getData().toString(), equalTo("http://test"));

    // save
    opData.save(storage);
    // reset
    opData.set(ApiMethodsImpl.NULL_OPERATION_DATA);
    assertThat(opData.responseData, is(ApiMethodsImpl.NULL_OPERATION_DATA.responseData));
    assertThat(opData.operation, equalTo(Operation.NOP));

    // load back
    opData.load(storage);
    assertThat(opData.operation, equalTo(1));
    assertThat(opData.token, equalTo(2));
    assertThat(opData.responseData.getErrorCode(), equalTo(3));
    assertThat(opData.responseData.getMessage(), equalTo("test"));
    assertThat(opData.responseData.getData().toString(), equalTo("http://test"));

  }

}
