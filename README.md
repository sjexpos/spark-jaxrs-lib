# JAX-RS implementation for SparkJava

[![GitHub release](https://img.shields.io/github/release/sjexpos/spark-jaxrs-lib.svg?style=plastic)](https://github.com/sjexpos/spark-jaxrs-lib/releases/latest)
[![CI workflow](https://img.shields.io/github/actions/workflow/status/sjexpos/spark-jaxrs-lib/ci.yaml?branch=main&label=ci&logo=github&style=plastic)](https://github.com/sjexpos/spark-jaxrs-lib/actions?workflow=CI)
[![Codecov](https://img.shields.io/codecov/c/github/sjexpos/spark-jaxrs-lib?logo=codecov&style=plastic)](https://codecov.io/gh/sjexpos/spark-jaxrs-lib)
[![Issues](https://img.shields.io/github/issues-search/sjexpos/spark-jaxrs-lib?query=is%3Aopen&label=issues&style=plastic)](https://github.com/sjexpos/spark-jaxrs-lib/issues)
![Commits](https://img.shields.io/github/last-commit/sjexpos/spark-jaxrs-lib?logo=github&style=plastic)


It is library to use [Sparkjava](https://sparkjava.com/) framework with [JAX-RS](https://jcp.org/en/jsr/detail?id=311) annotation. It is useful when working on projects with a lot of endpoints where it sometimes becomes messy to deal with all these Spark.something methods. It also saves the hassle of always getting back parameters, query parameters and headers from the Request object.

## Requirements

- Java 11 or later
- Maven 3.6.1 or later

## Build

```bash
mvn install
```

## How to include it in your project

### Maven

Add this dependency to your pom.xml

```xml
<dependency>
    <groupId>io.oigres</groupId>
    <artifactId>spark-jaxrs-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add the dependency as follow

```
dependencies {
    compile group: "io.oigres", name: "spark-jaxrs-lib", version:"1.0.0"
}
```

## How to use

### Simple GET request
Create a class and add a method with the annotation *@GET*

```Java
public class TestController {

    @GET
    @Path("/hello/{name}")
    public String hello(@PathParam("name") String name){
        return "Hello "+ name;
    }
}
```

Once this is done, you just need to add these lines of code in your main method or wherever you would usually declare your Spark endpoints.

```Java
public static void main(String[] args) {
    Gson requestObjectMapper = new GsonBuilder().create();
    JsonTransformer jsonTransformer = new JsonTransformer(requestObjectMapper);
    RouteBuilder routeBuilder = new RouteBuilder(requestObjectMapper, new ResponseTransformerProvider() {
        @Override
        public ResponseTransformer getTransformer(MediaType mediaType) {
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
                return jsonTransformer;
            }
            return null;
        }
    });
    routeBuilder.setupRoutes(
            Set.of(
                    new TestController()
            )
    );
    Spark.awaitInitialization();
    Spark.awaitStop();
}
```
And query the server to test
```bash
$ curl http://localhost:4567/hello/world
>>> Hello world
```

### Response Transformer

It offers a way to transform the response before sending it back to the client. The class ResponseTransformerProvider allows to define a Spark's ResponseTransformer according to the @Produces annotation if it's defined and/or the Accept header in the request.

```Java
    RouteBuilder routeBuilder = new RouteBuilder(requestObjectMapper, new ResponseTransformerProvider() {
        @Override
        public ResponseTransformer getTransformer(MediaType mediaType) {
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
                return jsonTransformer;
            }
            return null;
        }
    });
```

## Detailed usage
### Annotations
#### @Path
Used on a class or method to declare the request path.

| Parameter        | Usage                                                            |
|------------------|------------------------------------------------------------------|
| value (required) | Prefix path for every endpoints under this controller, or method |

See [RFC 5234](http://tools.ietf.org/html/rfc5234) for a description of the syntax

#### @GET

Used on a method to create a GET endpoint

| Parameter | Usage |
|-----------|-------|
 | < none >  | < none > |

#### @POST

Used on a method to create a POST endpoint

| Parameter | Usage |
|-----------|-------|
| < none >  | < none > |

#### @PUT

Used on a method to create a PUT endpoint

| Parameter | Usage |
|-----------|-------|
| < none >  | < none > |

#### @DELETE

Used on a method to create a DELETE endpoint

| Parameter | Usage |
|-----------|-------|
| < none >  | < none > |

#### @OPTIONS

Used on a method to create a OPTIONS endpoint

| Parameter | Usage |
|-----------|-------|
| < none >  | < none > |

#### @PathParam

Used on a method parameter. It will retrieve the value from the path.

| Parameter | Usage                                 |
|-----------|---------------------------------------|
| value     | The name of the parameter to retrieve |

```Java
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;

    @GET
    @Path("/hello/{name}")
    public String hello(@PathParam("name") String name){
        return "Hello "+ name;
    }
```

#### @QueryParam
Used on a method parameter. It will retrieve the value of a query strings

| Parameter | Usage |
|--------|--------|
| value | The name of the query param parameter to retrieve |

```Java
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;

@GET
public String hello(@QueryParam("name") String name){
	return name;
}
```

```bash
curl http://localhost:4567/hello
>>> hello
```

#### @HeaderParam
Used on a method parameter. It will retrieve the value of a request header

| Parameter | Usage |
|--------|--------|
| value | The name of the header to retrieve |

```Java

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;

@GET
public void auth(@HeaderParam("Authorization") String token){
	// do something
}
```

#### @BeanParam
Used on a method parameter. It will convert the body of a request to a Java object

| Parameter | Usage |
|-----------|-------|
| < none >  | < none > |

```Java
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;

@POST
public String hello(@BeanParam MyObject myObject) {
    // use myObject
}
```

```bash
curl -X POST http://localhost:4567/hello -H "Content-Type: application/json" -d "{...}"
```

#### @Consumes

Used on a method to define which mime types the endpoint will accept.

| Parameter | Usage                         |
|-----------|-------------------------------|
| value     | a list of mime type to accept |

```Java
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;

@POST
@Consumes("application/json")
public String hello(@BeanParam MyObject myObject) {
    // use myObject
}
```

#### @Produces

Used on a method to define which mime types the endpoint can return.

| Parameter | Usage                         |
|-----------|-------------------------------|
| value     | a list of mime type to return |

```Java
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

@POST
@Produces("application/json")
public String hello(@BeanParam MyObject myObject) {
    // use myObject
}
```

## Validation

It is possible to use Bean Validation 2.0 annotation [JSR 380](https://jcp.org/en/jsr/detail?id=380) in the endpoint parameter to add parameter restrictions.

```Java
import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@GET
@Path("/products/{product_id}")
public String getProduct(@PathParam("product_id") @Min(1) Integer productId) {
    return name;
}
```
It supports all constraints under the package javax.validation.constraints.

## Examples

- [Command line app](examples/cmd)

- [AWS Lambda](examples/aws-lambda)

