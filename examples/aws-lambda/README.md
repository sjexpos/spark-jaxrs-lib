This app uses the library to create a REST API which can run from a jar, and it also shows how swagger can be added.

## Requirements

- Java 11 or later
- Maven 3.6.1 or later
- AWS Serverless Application Model - [SAM](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)
- spark-jaxrs-lib installed locally

## Build

```bash
mvn install
```

## Run

```bash
sam local start-api --host 0.0.0.0 --warm-containers EAGER --template target/template.yaml
```

## API Test

```bash
curl http://localhost:3000/api/info
>>> {"info":"Hello World! "}
```

**Note:** It is possible that the first request takes a long time if sam never run before. Because it will download the image where the lambda function will be run.
