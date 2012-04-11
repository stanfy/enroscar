package com.stanfy.app.service

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import org.junit.Test

import android.content.SharedPreferences;
import android.net.Uri;

import com.stanfy.app.service.ApiMethodsImpl.APICallInfoData;
import com.stanfy.serverapi.request.Operation;
import com.stanfy.serverapi.response.ResponseData;

/**
 * Test for {@link APICallInfoData}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
class ApiOperationSaveLoadTest extends AbstractApiMethodsImplTest {

  /** Test storage. */
  private SharedPreferences storage;

  @Override
  void setup() {
    super.setup();
    storage = appService.getSharedPreferences("test", 0);
  }

  @Test
  void simpleTest() {
    final APICallInfoData opData = apiMethodsImpl.lastOperation
    assertNotNull opData

    opData.set 1, 2 // operation, token
    assertThat opData.operation, equalTo(1)
    assertThat opData.token, equalTo(2)

    final ResponseData rd = new ResponseData(Uri.parse("http://test"), null)
    rd.errorCode = 3
    rd.message = "test"
    opData.set rd
    assertThat opData.responseData.errorCode, equalTo(3)
    assertThat opData.responseData.message, equalTo("test")
    assertThat opData.responseData.data.toString(), equalTo("http://test")

    // save
    opData.save storage
    // reset
    opData.set ApiMethodsImpl.NULL_OPERATION_DATA
    assertThat opData.responseData, is(ApiMethodsImpl.NULL_OPERATION_DATA.responseData)
    assertThat opData.operation, equalTo(Operation.NOP)

    // load back
    opData.load storage
    assertThat opData.operation, equalTo(1)
    assertThat opData.token, equalTo(2)
    assertThat opData.responseData.errorCode, equalTo(3)
    assertThat opData.responseData.message, equalTo("test")
    assertThat opData.responseData.data.toString(), equalTo("http://test")
  }

}
