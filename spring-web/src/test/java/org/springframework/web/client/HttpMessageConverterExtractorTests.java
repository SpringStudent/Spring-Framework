/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

/**
 * Test fixture for {@link HttpMessageConverter}.
 *
 * @author Arjen Poutsma
 */
public class HttpMessageConverterExtractorTests {

	private HttpMessageConverterExtractor<?> extractor;

	private final ClientHttpResponse response = mock(ClientHttpResponse.class);


	@Test
	public void noContent() throws IOException {
		HttpMessageConverter<?> converter = mock(HttpMessageConverter.class);
		extractor = new HttpMessageConverterExtractor<>(String.class, createConverterList(converter));
		given(response.getStatusCode()).willReturn(HttpStatus.NO_CONTENT);

		Object result = extractor.extractData(response);
		assertNull(result);
	}

	@Test
	public void notModified() throws IOException {
		HttpMessageConverter<?> converter = mock(HttpMessageConverter.class);
		extractor = new HttpMessageConverterExtractor<>(String.class, createConverterList(converter));
		given(response.getStatusCode()).willReturn(HttpStatus.NOT_MODIFIED);

		Object result = extractor.extractData(response);
		assertNull(result);
	}

	@Test
	public void informational() throws IOException {
		HttpMessageConverter<?> converter = mock(HttpMessageConverter.class);
		extractor = new HttpMessageConverterExtractor<>(String.class, createConverterList(converter));
		given(response.getStatusCode()).willReturn(HttpStatus.CONTINUE);

		Object result = extractor.extractData(response);
		assertNull(result);
	}

	@Test
	public void zeroContentLength() throws IOException {
		HttpMessageConverter<?> converter = mock(HttpMessageConverter.class);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentLength(0);
		extractor = new HttpMessageConverterExtractor<>(String.class, createConverterList(converter));
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);

		Object result = extractor.extractData(response);
		assertNull(result);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void emptyMessageBody() throws IOException {
		HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(converter);
		HttpHeaders responseHeaders = new HttpHeaders();
		extractor = new HttpMessageConverterExtractor<>(String.class, createConverterList(converter));
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(response.getBody()).willReturn(new ByteArrayInputStream("".getBytes()));

		Object result = extractor.extractData(response);
		assertNull(result);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void normal() throws IOException {
		HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(converter);
		HttpHeaders responseHeaders = new HttpHeaders();
		MediaType contentType = MediaType.TEXT_PLAIN;
		responseHeaders.setContentType(contentType);
		String expected = "Foo";
		extractor = new HttpMessageConverterExtractor<>(String.class, converters);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(response.getBody()).willReturn(new ByteArrayInputStream(expected.getBytes()));
		given(converter.canRead(String.class, contentType)).willReturn(true);
		given(converter.read(eq(String.class), any(HttpInputMessage.class))).willReturn(expected);

		Object result = extractor.extractData(response);
		assertEquals(expected, result);
	}

	@Test(expected = RestClientException.class)
	@SuppressWarnings("unchecked")
	public void cannotRead() throws IOException {
		HttpMessageConverter<String> converter = mock(HttpMessageConverter.class);
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		converters.add(converter);
		HttpHeaders responseHeaders = new HttpHeaders();
		MediaType contentType = MediaType.TEXT_PLAIN;
		responseHeaders.setContentType(contentType);
		extractor = new HttpMessageConverterExtractor<>(String.class, converters);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(response.getBody()).willReturn(new ByteArrayInputStream("Foobar".getBytes()));
		given(converter.canRead(String.class, contentType)).willReturn(false);

		extractor.extractData(response);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void generics() throws IOException {
		GenericHttpMessageConverter<String> converter = mock(GenericHttpMessageConverter.class);
		List<HttpMessageConverter<?>> converters = createConverterList(converter);
		HttpHeaders responseHeaders = new HttpHeaders();
		MediaType contentType = MediaType.TEXT_PLAIN;
		responseHeaders.setContentType(contentType);
		String expected = "Foo";
		ParameterizedTypeReference<List<String>> reference = new ParameterizedTypeReference<List<String>>() {};
		Type type = reference.getType();
		extractor = new HttpMessageConverterExtractor<List<String>>(type, converters);
		given(response.getStatusCode()).willReturn(HttpStatus.OK);
		given(response.getHeaders()).willReturn(responseHeaders);
		given(response.getBody()).willReturn(new ByteArrayInputStream(expected.getBytes()));
		given(converter.canRead(type, null, contentType)).willReturn(true);
		given(converter.read(eq(type), eq(null), any(HttpInputMessage.class))).willReturn(expected);

		Object result = extractor.extractData(response);
		assertEquals(expected, result);
	}

	private List<HttpMessageConverter<?>> createConverterList(HttpMessageConverter<?> converter) {
		List<HttpMessageConverter<?>> converters = new ArrayList<>(1);
		converters.add(converter);
		return converters;
	}


}
