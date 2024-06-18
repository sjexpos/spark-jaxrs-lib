package io.oigres.sparkjax.tests;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.oigres.sparkjax.JsonTransformer;
import io.oigres.sparkjax.RouteBuilder;
import io.oigres.sparkjax.jaxrs.ResponseTransformerProvider;

import org.junit.Assert;
import org.junit.Before;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spark.SparkLambdaContainerHandler;

import spark.ResponseTransformer;
import spark.Spark;

public abstract class HttpMethodTest {
	protected Gson objectMapper = new Gson();
	protected SparkLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

	protected abstract Set<Object> getResources();
	
	@Before
	public void setup() throws Exception {
    	Set<Object> resources = getResources();
		this.handler = SparkLambdaContainerHandler.getAwsProxyHandler();
		final JsonTransformer jsonTransformer = new JsonTransformer(this.objectMapper);
		RouteBuilder routeBuilder = new RouteBuilder(this.objectMapper, new ResponseTransformerProvider() {
			@Override
			public ResponseTransformer getTransformer(MediaType mediaType) {
				if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
					return jsonTransformer;
				}
				return null;
			}
		});
		routeBuilder.setupRoutes(resources);
		Spark.awaitInitialization();
	}
	
	protected InputStream getRequest(String filename) {
		InputStream request = GetTest.class.getResourceAsStream(filename);
		if (request == null) {
			request = GetTest.class.getClassLoader().getResourceAsStream(filename);
		}
		return request;
	}

	protected JsonElement readResponse(ByteArrayOutputStream response) {
		return JsonParser.parseString(response.toString());
	}
	
	protected void assertStatus(JsonElement response, int status) {
		Assert.assertEquals(status, response.getAsJsonObject().get("statusCode").getAsInt());
	}
	
	protected void assertContentType(JsonElement response, MediaType mediaType) {
		JsonObject headers = response.getAsJsonObject().get("multiValueHeaders").getAsJsonObject();
		Assert.assertTrue(headers.has("Content-Type"));
		Assert.assertTrue(headers.get("Content-Type").isJsonArray());
		List<String> contentTypes = new LinkedList<String>();
		headers.get("Content-Type").getAsJsonArray().forEach(s -> contentTypes.add(s.getAsString()));
		Assert.assertTrue( !contentTypes.isEmpty() );
		Assert.assertEquals(mediaType.toString(), contentTypes.stream().filter(s -> mediaType.toString().equals(s)).findAny().orElse(""));
	}
	
}
