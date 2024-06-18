package io.oigres.sparkjax.tests;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import io.oigres.sparkjax.JsonTransformer;
import io.oigres.sparkjax.ResponseExceptionHandler;

import org.junit.Assert;
import org.junit.Test;

import spark.Spark;

public class ValidatedPostModelTest extends HttpMethodTest {
    private Gson objectMapper = new Gson();
	private ResponseExceptionHandler responseExceptionHandler;

	public ValidatedPostModelTest() {
		JsonTransformer jsonTransformer = new JsonTransformer(this.objectMapper);
		this.responseExceptionHandler = new ResponseExceptionHandler(jsonTransformer, MediaType.APPLICATION_JSON_TYPE, true, true);
	}

	static public class Model {
		@NotNull
		private String name;
		@Min(value = 25)
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
	static public interface ValidatedPostResources {
		@POST
		@Path("/validated")
		default Map<String,Object> postModel(@BeanParam @Valid @NotNull Model model) {
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("result", model);
			return result;
		}
		@POST
		@Path("/validated/{extra}")
		default Map<String,Object> postModelDualParams(@PathParam("extra") String text, @BeanParam @Valid Model model) {
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("path_param", text);
			result.put("bean_param", model);
			return result;
		}

		@POST
		@Path("/validated/{extra}/string")
		default Map<String,Object> postModelPathAndQueryParams(@PathParam("extra") String text, @BeanParam @Valid Model model, @QueryParam("text") String str) {
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("path_param", text);
			result.put("bean_param", model);
			result.put("query_param", str);
			return result;
		}
	}
	
	static public class ValidatedPostController implements ValidatedPostResources {
	}
	
	@Override
	protected Set<Object> getResources() {
    	return new HashSet<Object>(
        		Arrays.asList(
        				new ValidatedPostController()
        	    )
        	);
	}
	
	@Override
	public void setup() throws Exception {
		super.setup();
		Spark.exception(ConstraintViolationException.class, (exception, request, response) -> this.responseExceptionHandler.handle(exception, request, response));
	}
	
	@Test
	public void test_model_full_body() throws Exception {
		// Given
		InputStream request = getRequest("post/model/validated/full_body.json");
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
        Assert.assertEquals("Juan Perez", body.getAsJsonObject().get("result").getAsJsonObject().get("name").getAsString());
        Assert.assertEquals(26L, body.getAsJsonObject().get("result").getAsJsonObject().get("age").getAsLong());
    }
	
	@Test
	public void test_model_null_body() throws Exception {
		// Given
		InputStream request = getRequest("post/model/validated/null_body.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());

		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse,  Response.Status.BAD_REQUEST.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

        String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);

        Assert.assertTrue(!body.getAsJsonObject().get("timestamp").getAsString().isEmpty());
        Assert.assertEquals("BAD_REQUEST", body.getAsJsonObject().get("status").getAsString());
        Assert.assertEquals("Bad Request", body.getAsJsonObject().get("error").getAsString());
        Assert.assertEquals("javax.validation.ConstraintViolationException", body.getAsJsonObject().get("exception").getAsString());
        Assert.assertTrue(!body.getAsJsonObject().get("trace").getAsString().isEmpty());
        Assert.assertEquals("Validation failed. Error count: 1", body.getAsJsonObject().get("message").getAsString());
        Assert.assertEquals("/ping/validated", body.getAsJsonObject().get("path").getAsString());

		JsonArray errors = body.getAsJsonObject().get("errors").getAsJsonArray();
        Assert.assertTrue( errors.isJsonArray() );
        Assert.assertEquals(1, errors.size());
		JsonElement firstConstraint = errors.get(0);

		Assert.assertEquals("{javax.validation.constraints.NotNull.message}", firstConstraint.getAsJsonObject().get("messageTemplate").getAsString());
        Assert.assertTrue(!firstConstraint.getAsJsonObject().get("defaultMessage").getAsString().isEmpty());
        Assert.assertEquals("ValidatedPostController", firstConstraint.getAsJsonObject().get("objectName").getAsString());
        Assert.assertEquals("NotNull", firstConstraint.getAsJsonObject().get("source").getAsString());
        Assert.assertTrue(!firstConstraint.getAsJsonObject().get("field").getAsString().isEmpty());
        Assert.assertEquals("null", firstConstraint.getAsJsonObject().get("rejectedValue").getAsString());
	}

	@Test
	public void test_model_invalid_age() throws Exception {
		// Given
		InputStream request = getRequest("post/model/validated/invalid_age.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());

		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse, Response.Status.BAD_REQUEST.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);

        Assert.assertTrue(!body.getAsJsonObject().get("timestamp").getAsString().isEmpty());
        Assert.assertEquals("BAD_REQUEST", body.getAsJsonObject().get("status").getAsString());
        Assert.assertEquals("Bad Request", body.getAsJsonObject().get("error").getAsString());
        Assert.assertEquals("javax.validation.ConstraintViolationException", body.getAsJsonObject().get("exception").getAsString());
        Assert.assertTrue(!body.getAsJsonObject().get("trace").getAsString().isEmpty());
        Assert.assertEquals("Validation failed. Error count: 1", body.getAsJsonObject().get("message").getAsString());
        Assert.assertEquals("/ping/validated", body.getAsJsonObject().get("path").getAsString());

		JsonArray errors = body.getAsJsonObject().get("errors").getAsJsonArray();

        Assert.assertTrue( errors.isJsonArray() );
        Assert.assertEquals(1, errors.size());
		JsonElement firstConstraint = errors.get(0);

        Assert.assertEquals("{javax.validation.constraints.Min.message}", firstConstraint.getAsJsonObject().get("messageTemplate").getAsString());
        Assert.assertTrue(!firstConstraint.getAsJsonObject().get("defaultMessage").getAsString().isEmpty());
        Assert.assertEquals("Model", firstConstraint.getAsJsonObject().get("objectName").getAsString());
        Assert.assertEquals("Min", firstConstraint.getAsJsonObject().get("source").getAsString());
        Assert.assertTrue(!firstConstraint.getAsJsonObject().get("field").getAsString().isEmpty());
        Assert.assertEquals("23", firstConstraint.getAsJsonObject().get("rejectedValue").getAsString());
	}

	@Test
	public void test_model_invalid_name() throws Exception {
		// Given
		InputStream request = getRequest("post/model/validated/invalid_name.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());

		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse, Response.Status.BAD_REQUEST.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);
        Assert.assertTrue(!body.getAsJsonObject().get("timestamp").getAsString().isEmpty());
        Assert.assertEquals("BAD_REQUEST", body.getAsJsonObject().get("status").getAsString());
        Assert.assertEquals("Bad Request", body.getAsJsonObject().get("error").getAsString());
        Assert.assertEquals("javax.validation.ConstraintViolationException", body.getAsJsonObject().get("exception").getAsString());
        Assert.assertTrue(!body.getAsJsonObject().get("trace").getAsString().isEmpty());
        Assert.assertEquals("Validation failed. Error count: 1", body.getAsJsonObject().get("message").getAsString());
        Assert.assertEquals("/ping/validated", body.getAsJsonObject().get("path").getAsString());

		JsonArray errors = body.getAsJsonObject().get("errors").getAsJsonArray();

		Assert.assertTrue( errors.isJsonArray() );
        Assert.assertEquals(1, errors.size());
		JsonElement firstConstraint = errors.get(0);

        Assert.assertEquals("{javax.validation.constraints.NotNull.message}", firstConstraint.getAsJsonObject().get("messageTemplate").getAsString());
        Assert.assertTrue(!firstConstraint.getAsJsonObject().get("defaultMessage").getAsString().isEmpty());
        Assert.assertEquals("Model", firstConstraint.getAsJsonObject().get("objectName").getAsString());
        Assert.assertEquals("NotNull", firstConstraint.getAsJsonObject().get("source").getAsString());
        Assert.assertTrue(!firstConstraint.getAsJsonObject().get("field").getAsString().isEmpty());
        Assert.assertEquals("null", firstConstraint.getAsJsonObject().get("rejectedValue").getAsString());
	}

	@Test
	public void test_model_invalid_all() throws Exception {
		// Given
		InputStream request = getRequest("post/model/validated/invalid_all.json");
		ByteArrayOutputStream response = new ByteArrayOutputStream();

		// When
        this.handler.proxyStream(request, response, new DummyContext());

        // Verify
        System.out.println(response.toString());

		JsonElement lambdaResponse = readResponse(response);
		assertStatus(lambdaResponse, Response.Status.BAD_REQUEST.getStatusCode());
		assertContentType(lambdaResponse, MediaType.APPLICATION_JSON_TYPE);

		String bodyJson = lambdaResponse.getAsJsonObject().get("body").getAsString();
		JsonElement body = JsonParser.parseString(bodyJson);

        Assert.assertTrue(!body.getAsJsonObject().get("timestamp").getAsString().isEmpty());
        Assert.assertEquals("BAD_REQUEST", body.getAsJsonObject().get("status").getAsString());
        Assert.assertEquals("Bad Request", body.getAsJsonObject().get("error").getAsString());
        Assert.assertEquals("javax.validation.ConstraintViolationException", body.getAsJsonObject().get("exception").getAsString());
        Assert.assertTrue(!body.getAsJsonObject().get("trace").getAsString().isEmpty());
        Assert.assertEquals("Validation failed. Error count: 2", body.getAsJsonObject().get("message").getAsString());
        Assert.assertEquals("/ping/validated", body.getAsJsonObject().get("path").getAsString());

		JsonArray errors = body.getAsJsonObject().get("errors").getAsJsonArray();
		Assert.assertTrue( errors.isJsonArray() );
		Assert.assertEquals(2, errors.size());

		JsonElement firstConstraint = errors.get(0);
		boolean differentErrorOrder = false;
		// fix for random errors order
		if (!"{javax.validation.constraints.Min.message}".equals(firstConstraint.getAsJsonObject().get("messageTemplate").getAsString())) {
			firstConstraint = errors.get(1);
			differentErrorOrder = true;
		}

		Assert.assertEquals("{javax.validation.constraints.Min.message}", firstConstraint.getAsJsonObject().get("messageTemplate").getAsString());
		Assert.assertTrue(!firstConstraint.getAsJsonObject().get("defaultMessage").getAsString().isEmpty());
		Assert.assertEquals("Model", firstConstraint.getAsJsonObject().get("objectName").getAsString());
		Assert.assertEquals("Min", firstConstraint.getAsJsonObject().get("source").getAsString());
		Assert.assertTrue(!firstConstraint.getAsJsonObject().get("field").getAsString().isEmpty());
		Assert.assertEquals("23", firstConstraint.getAsJsonObject().get("rejectedValue").getAsString());

		JsonElement secondConstraint = errors.get(1);
		if (differentErrorOrder) {
			secondConstraint = errors.get(0);
		}
		Assert.assertEquals("{javax.validation.constraints.NotNull.message}", secondConstraint.getAsJsonObject().get("messageTemplate").getAsString());
		Assert.assertTrue(!secondConstraint.getAsJsonObject().get("defaultMessage").getAsString().isEmpty());
		Assert.assertEquals("Model", secondConstraint.getAsJsonObject().get("objectName").getAsString());
		Assert.assertEquals("NotNull", secondConstraint.getAsJsonObject().get("source").getAsString());
		Assert.assertTrue(!secondConstraint.getAsJsonObject().get("field").getAsString().isEmpty());
		Assert.assertEquals("null", secondConstraint.getAsJsonObject().get("rejectedValue").getAsString());
	}
	
}
