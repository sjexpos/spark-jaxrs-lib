package io.oigres.sparkjax.tests;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

public class PostModelTest extends HttpMethodTest {

	static public class Model {
		private String name;
		private long age;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public long getAge() {
			return age;
		}
		public void setAge(long age) {
			this.age = age;
		}
	}
	
	@Path("/ping")
	static public interface PostResources {
		@POST
		@Path("/model")
		default Map<String,Object> postModel(@BeanParam Model model) {
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("result", model);
			return result;
		}
		@POST
		@Path("/model/{extra}")
		default Map<String,Object> postModelDualParams(@PathParam("extra") String text, @BeanParam Model model) {
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("path_param", text);
			result.put("bean_param", model);
			return result;
		}

		@POST
		@Path("/model/{extra}/string")
		default Map<String,Object> postModelPathAndQueryParams(@PathParam("extra") String text, @BeanParam Model model, @QueryParam("text") String str) {
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("path_param", text);
			result.put("bean_param", model);
			result.put("query_param", str);
			return result;
		}

		@POST
		@Path("/model/{serialNumber}/with_upper_case")
		default Map<String,Object> postPathParamInUpperCase(@PathParam("serialNumber") String text, @BeanParam Model model) {
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("path_param", text);
			result.put("bean_param", model);
			return result;
		}
	}
	
	static public class PostController implements PostResources {
	}
	
	@Override
	protected Set<Object> getResources() {
    	return new HashSet<Object>(
        		Arrays.asList(
        				new PostController()
        	    )
        	);
	}
	
	@Test
	public void test_model_full_body() throws Exception {
		// Given
		InputStream request = getRequest("post/model/full_body.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());
		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.OK.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
        Assert.assertTrue(body.getAsJsonObject().has("result"));
        Assert.assertEquals("Juan Perez", body.getAsJsonObject().get("result").getAsJsonObject().get("name").getAsString());
        Assert.assertEquals(26L, body.getAsJsonObject().get("result").getAsJsonObject().get("age").getAsLong());
	}

	@Test
	public void test_model_empty_body() throws Exception {
		// Given
		InputStream request = getRequest("post/model/empty_body.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());
		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.OK.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
		Assert.assertTrue(!body.getAsJsonObject().isJsonNull());
		Assert.assertTrue(!body.getAsJsonObject().has("result"));
	}

	@Test
	public void test_model_null_body() throws Exception {
		// Given
		InputStream request = getRequest("post/model/null_body.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());
		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.OK.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
        Assert.assertTrue(!body.getAsJsonObject().isJsonNull());
        Assert.assertTrue(!body.getAsJsonObject().has("result"));
	}

	@Test
	public void test_model_path_and_bean_parameters() throws Exception {
		// Given
		InputStream request = getRequest("post/model/path_and_bean_parameters.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());
		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.OK.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
        Assert.assertTrue(body.getAsJsonObject().has("path_param"));
        Assert.assertEquals("1a2b3c", body.getAsJsonObject().get("path_param").getAsString());
        Assert.assertTrue(body.getAsJsonObject().has("bean_param"));
        Assert.assertEquals("Juan Perez", body.getAsJsonObject().get("bean_param").getAsJsonObject().get("name").getAsString());
        Assert.assertEquals(26L, body.getAsJsonObject().get("bean_param").getAsJsonObject().get("age").getAsLong());
	}

	@Test
	public void test_model_query_param_and_bean_parameters() throws Exception {
		// Given
		InputStream request = getRequest("post/model/header_path_and_query_parameter.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
		this.handler.proxyStream(request, response, new DummyContext());

		// Verify
		System.out.println(response.toString());
		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.OK.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
		Assert.assertTrue(body.getAsJsonObject().has("path_param"));
		Assert.assertEquals("1a2b3c", body.getAsJsonObject().get("path_param").getAsString());
		Assert.assertTrue(body.getAsJsonObject().has("bean_param"));
		Assert.assertEquals("Juan Perez", body.getAsJsonObject().get("bean_param").getAsJsonObject().get("name").getAsString());
		Assert.assertEquals(26L, body.getAsJsonObject().get("bean_param").getAsJsonObject().get("age").getAsLong());
		Assert.assertTrue(body.getAsJsonObject().has("query_param"));
		Assert.assertEquals("Hello world!", body.getAsJsonObject().get("query_param").getAsString());
	}
	
	@Test
	public void test_path_parameter_in_upper_case() throws Exception {
		// Given
		InputStream request = getRequest("post/model/path_with_upper_case.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());
		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.OK.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
        Assert.assertTrue(body.getAsJsonObject().has("path_param"));
        Assert.assertEquals("1a2b3c", body.getAsJsonObject().get("path_param").getAsString());
        Assert.assertTrue(body.getAsJsonObject().has("bean_param"));
        Assert.assertEquals("Juan Perez", body.getAsJsonObject().get("bean_param").getAsJsonObject().get("name").getAsString());
        Assert.assertEquals(26L, body.getAsJsonObject().get("bean_param").getAsJsonObject().get("age").getAsLong());
	}
	
}
