package org.spotter.shared.service;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link SpotterServiceResponse} class.
 */
public class SpotterServiceResponseTest {

	/**
	 * Tests default constructor and values.
	 */
	@Test
	public void testNullPayload() {
		SpotterServiceResponse<Object> ssr = new SpotterServiceResponse<Object>();
		Object obj = ssr.getPayload();
		Assert.assertEquals(obj, null);
		String s = ssr.getPayload(String.class);
		Assert.assertEquals(s, null);
	}
	
	/**
	 * Tests second constructor and the values.
	 */
	@Test
	public void testSimplePayload() {
		Object obj = new Object();
		SpotterServiceResponse<Object> ssr = new SpotterServiceResponse<Object>(obj, ResponseStatus.OK);
		Object obj2 = ssr.getPayload();
		Assert.assertEquals(obj, obj2);
		
		String s = "test";
		ssr = new SpotterServiceResponse<Object>(s, ResponseStatus.OK);
		Object s2 = ssr.getPayload();
		Assert.assertEquals(s, s2);
	}
	
	/**
	 * Tests set+get status.
	 */
	@Test
	public void testStatus() {
		SpotterServiceResponse<Object> ssr = new SpotterServiceResponse<Object>();
		ssr.setStatus(ResponseStatus.INVALID_STATE);
		ResponseStatus rs = ssr.getStatus();
		Assert.assertEquals(ResponseStatus.INVALID_STATE, rs);
		ssr.setStatus(ResponseStatus.OK);
		rs = ssr.getStatus();
		Assert.assertEquals(ResponseStatus.OK, rs);
	}
	
	/**
	 * Tests the error message setter + getter.
	 */
	@Test
	public void testErrorMessage() {
		SpotterServiceResponse<Object> ssr = new SpotterServiceResponse<Object>();
		ssr.setErrorMessage("error");
		String rs = ssr.getErrorMessage();
		Assert.assertEquals("error", rs);
		ssr.setErrorMessage("error2");
		rs = ssr.getErrorMessage();
		Assert.assertEquals("error2", rs);
	}
	
	/**
	 * Tests the third constructor with an error message.
	 */
	@Test
	public void testErrorPayload() {
		SpotterServiceResponse<Object> ssr = new SpotterServiceResponse<Object>("payload", ResponseStatus.INVALID_STATE, "error");
		String rs = ssr.getErrorMessage();
		Assert.assertEquals("error", rs);
		rs = ssr.getPayload(String.class);
		Assert.assertEquals("payload", rs);
	}
	
	/**
	 * Tests the invalid converting of the payload.
	 */
	@Test(expected=RuntimeException.class)
	public void testInvalidPayloadConverting() {
		SpotterServiceResponse<Object> ssr = new SpotterServiceResponse<Object>("payload", ResponseStatus.INVALID_STATE, "error");
		int rs = ssr.getPayload(Integer.class);
		Assert.assertEquals(0, rs);
	}
	
}
