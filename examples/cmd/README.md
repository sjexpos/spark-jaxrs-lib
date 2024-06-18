This app uses the library to create a REST API which can run from a jar, and it also shows how swagger can be added.

## Requirements

- Java 11 or later
- Maven 3.6.1 or later
- spark-jaxrs-lib installed locally

## Build

```bash
mvn install
```

## Run

```bash
mvn exec:java
```

## Swagger

```link
http://localhost:8080
```

## API Test

```bash
curl http://localhost:8080/api/info
>>> {"info":"Hello World! "}
```
