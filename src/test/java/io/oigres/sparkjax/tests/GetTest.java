package io.oigres.sparkjax.tests;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Test;

public class GetTest extends HttpMethodTest {
    
	@Path("/ping")
	static public interface GetResources {

		@GET
	    @Produces(MediaType.APPLICATION_JSON)
	    @Consumes(MediaType.WILDCARD)
	    default Map<String, String> ping() {
			return null;
		}
		
		@GET
		@Path("/pong")
	    @Produces(MediaType.APPLICATION_JSON)
	    @Consumes(MediaType.WILDCARD)
	    default Map<String, String> pong() {
			return null;
		}

		@GET
		@Path("/string")
		default Map<String, String> stringParameter(@QueryParam("text") String str) {
			Map<String,String> result = new HashMap<String,String>();
			result.put("result", str);
			return result;
		}

		@GET
		@Path("/int")
		default Map<String, Integer> intParameter(@QueryParam("number") int integer) {
			Map<String,Integer> result = new HashMap<String,Integer>();
			result.put("result", integer);
			return result;
		}
		
		@GET
		@Path("/integer")
		default Map<String, Integer> integerParameter(@QueryParam("number") Integer integer) {
			Map<String,Integer> result = new HashMap<String,Integer>();
			result.put("result", integer);
			return result;
		}

		@GET
		@Path("/list/integer")
		default Map<String, List<Integer>> listIntegerParameter(@QueryParam("numbers") List<Integer> str) {
			Map<String,List<Integer>> result = new HashMap<String,List<Integer>>();
			result.put("result", str);
			return result;
		}

		@GET
		@Path("/list/string")
		default Map<String, List<String>> listStringParameter(@QueryParam("text") List<String> str) {
			Map<String,List<String>> result = new HashMap<String,List<String>>();
			result.put("result", str);
			return result;
		}

		@GET
		@Path("/multi/{param}/string")
		default Map<String, String> pathAndQueryParameter(@PathParam("param") String path, @QueryParam("text") String str) {
			Map<String,String> result = new HashMap<String,String>();
			result.put("path_param", path);
			result.put("query_param", str);
			return result;
		}

		@GET
		@Path("/multi/{param}")
		default Map<String, String> headerAndPathAndQueryParameter(@HeaderParam("X-Key") String key, @PathParam("param") String path, @QueryParam("text") String str) {
			Map<String,String> result = new HashMap<String,String>();
			result.put("header_param", key);
			result.put("path_param", path);
			result.put("query_param", str);
			return result;
		}

		@GET
		@Path("/longPathParameterWithNotNull/{param}")
		default Map<String, Long> longPathParameterWithNotNull(@NotNull @PathParam("param") Long path) {
			Map<String,Long> result = new HashMap<String,Long>();
			result.put("path_param", path);
			return result;
		}

		@GET
		@Path("/path_collision/{param}")
		default Map<String, Long> pathCollision(@NotNull @PathParam("param") Long path) {
			Map<String,Long> result = new HashMap<String,Long>();
			result.put("path_param", path);
			return result;
		}

		@GET
		@Path("/path_collision/issue")
		default Map<String, String> pathCollision() {
			Map<String,String> result = new HashMap<String,String>();
			result.put("status", "OK");
			return result;
		}

	}
	
	static public class GetController implements GetResources {

		@Override
	    public Map<String, String> ping() {
			Map<String,String> result = new HashMap<String,String>();
			result.put("hello", "world");
			return result;
		}

		@Override
	    public Map<String, String> pong() {
			Map<String,String> result = new HashMap<String,String>();
			result.put("pong", "world");
			return result;
	    }

		@Override
		public Map<String, String> stringParameter(String str) {
			Map<String,String> result = new HashMap<String,String>();
			result.put("result", str);
			return result;
		}

	}
	
	@Override
	protected Set<Object> getResources() {
    	return new HashSet<Object>(
        		Arrays.asList(
        				new GetController()
        	    )
        	);
	}
	
	@Test
	public void test_without_parameters() throws Exception {

		// Given
		InputStream request = getRequest("get/without_parameters.json");
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
		Assert.assertTrue(!body.getAsJsonObject().get("hello").isJsonNull());
        Assert.assertEquals("world", body.getAsJsonObject().get("hello").getAsString());
	}

	@Test
	public void test_without_parameters_2path_levels() throws Exception {

		// Given
		InputStream request = getRequest("get/without_parameters_2path_levels.json");
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
        Assert.assertTrue(!body.getAsJsonObject().get("pong").isJsonNull());
        Assert.assertEquals("world", body.getAsJsonObject().get("pong").getAsString());
	}

	@Test
	public void test_string_parameters_with_value() throws Exception {

		// Given
		InputStream request = getRequest("get/string_parameters_with_value.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.OK.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
		Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
		Assert.assertEquals("1234", body.getAsJsonObject().get("result").getAsString());
	}

	@Test
	public void test_string_parameters_without_value() throws Exception {

		// Given
		InputStream request = getRequest("get/string_parameters_without_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().has("result"));
	}

	@Test
	public void test_string_parameters_with_empty_value() throws Exception {

		// Given
		InputStream request = getRequest("get/string_parameters_with_empty_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
		Assert.assertEquals("", body.getAsJsonObject().get("result").getAsString());
	}

	@Test
	public void test_int_parameters_with_value() throws Exception {

		// Given
		InputStream request = getRequest("get/int_parameters_with_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
        Assert.assertEquals(1234, body.getAsJsonObject().get("result").getAsInt());
	}

	@Test
	public void test_int_parameters_with_empty_value() throws Exception {

		// Given
		InputStream request = getRequest("get/int_parameters_with_empty_value.json");
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
        Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
        Assert.assertEquals(0, body.getAsJsonObject().get("result").getAsInt());
	}

	@Test
	public void test_int_parameters_without_value() throws Exception {

		// Given
		InputStream request = getRequest("get/int_parameters_without_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
		Assert.assertEquals(0, body.getAsJsonObject().get("result").getAsInt());
	}

	@Test
	public void test_integer_parameters_with_value() throws Exception {

		// Given
		InputStream request = getRequest("get/integer_parameters_with_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
        Assert.assertEquals(1234, body.getAsJsonObject().get("result").getAsInt());
	}

	@Test
	public void test_integer_parameters_with_empty_value() throws Exception {

		// Given
		InputStream request = getRequest("get/integer_parameters_with_empty_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().has("result"));
	}

	@Test
	public void test_integer_parameters_without_value() throws Exception {

		// Given
		InputStream request = getRequest("get/integer_parameters_without_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().has("result"));
	}

	@Test
	public void test_list_integer_parameters_with_empty_value() throws Exception {

		// Given
		InputStream request = getRequest("get/list_integer_parameters_with_empty_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
        Assert.assertTrue(body.getAsJsonObject().get("result").isJsonArray());
        Assert.assertFalse(body.getAsJsonObject().get("result").getAsJsonArray().size() > 0);
	}

	@Test
	public void test_list_integer_parameters_with_value() throws Exception {

		// Given
		InputStream request = getRequest("get/list_integer_parameters_with_value.json");
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
        Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
        Assert.assertTrue(body.getAsJsonObject().get("result").isJsonArray());
        Assert.assertTrue(body.getAsJsonObject().get("result").getAsJsonArray().size() > 0);
        Assert.assertEquals(2, body.getAsJsonObject().get("result").getAsJsonArray().size());

		Iterator<JsonElement> list = body.getAsJsonObject().get("result").getAsJsonArray().iterator();
		Assert.assertEquals(1234, list.next().getAsInt());
        Assert.assertEquals(5678, list.next().getAsInt());
	}

	@Test
	public void test_list_integer_parameters_without_value() throws Exception {

		// Given
		InputStream request = getRequest("get/list_integer_parameters_without_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().has("result"));
	}

	@Test
	public void test_list_string_parameters_with_empty_value() throws Exception {

		// Given
		InputStream request = getRequest("get/list_string_parameters_with_empty_value.json");
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
		Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
		Assert.assertTrue(body.getAsJsonObject().get("result").isJsonArray());
		Assert.assertFalse(body.getAsJsonObject().get("result").getAsJsonArray().size() > 0);
	}

	@Test
	public void test_list_string_parameters_with_value() throws Exception {

		// Given
		InputStream request = getRequest("get/list_string_parameters_with_value.json");
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

		Assert.assertTrue(!body.getAsJsonObject().get("result").isJsonNull());
		Assert.assertTrue(body.getAsJsonObject().get("result").isJsonArray());
		Assert.assertTrue(body.getAsJsonObject().get("result").getAsJsonArray().size() > 0);
		Assert.assertEquals(2, body.getAsJsonObject().get("result").getAsJsonArray().size());

		Iterator<JsonElement> list = body.getAsJsonObject().get("result").getAsJsonArray().iterator();
		Assert.assertEquals("abcd", list.next().getAsString());
		Assert.assertEquals("efgh", list.next().getAsString());
	}

	@Test
	public void test_list_string_parameters_without_value() throws Exception {

		// Given
		InputStream request = getRequest("get/list_string_parameters_without_value.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.OK.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
		Assert.assertTrue(!body.getAsJsonObject().has("result"));
	}

	@Test
	public void test_path_and_query_parameter() throws Exception {

		// Given
		InputStream request = getRequest("get/path_and_query_parameter.json");
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
        Assert.assertTrue(!body.getAsJsonObject().get("path_param").isJsonNull());
        Assert.assertEquals("1a2b3c", body.getAsJsonObject().get("path_param").getAsString());
        Assert.assertTrue(body.getAsJsonObject().has("query_param"));
        Assert.assertEquals("Hello world!", body.getAsJsonObject().get("query_param").getAsString());
	}

	@Test
	public void test_header_and_path_and_query_parameter() throws Exception {

		// Given
		InputStream request = getRequest("get/header_and_path_and_query_parameter.json");
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
        Assert.assertTrue(body.getAsJsonObject().has("header_param"));
        Assert.assertEquals("abcd-degh-ijkl-mnop", body.getAsJsonObject().get("header_param").getAsString());
        Assert.assertTrue(body.getAsJsonObject().has("path_param"));
        Assert.assertEquals("1a2b3c", body.getAsJsonObject().get("path_param").getAsString());
        Assert.assertTrue(body.getAsJsonObject().has("query_param"));
        Assert.assertEquals("Hello world!", body.getAsJsonObject().get("query_param").getAsString());
	}

	@Test
	public void test_longPathParameterWithNotNull() throws Exception {
		// longPathParameterWithNotNull
		// Given
		InputStream request = getRequest("get/long_path_parameter_with_notnull.json");
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
        Assert.assertEquals(123,  body.getAsJsonObject().get("path_param").getAsLong());
	}

	@Test
	public void test_path_collision() throws Exception {
		// longPathParameterWithNotNull
		// Given
		InputStream request = getRequest("get/path_collision.json");
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
        Assert.assertEquals(123,  body.getAsJsonObject().get("path_param").getAsLong());
	}

	@Test
	public void test_path_collision_issue() throws Exception {
		// longPathParameterWithNotNull
		// Given
		InputStream request = getRequest("get/path_collision_issue.json");
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
        Assert.assertTrue(body.getAsJsonObject().has("status"));
        Assert.assertEquals("OK",  body.getAsJsonObject().get("status").getAsString());
	}

}
