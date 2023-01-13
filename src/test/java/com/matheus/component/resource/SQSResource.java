package com.matheus.component.resource;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;

public class SQSResource implements QuarkusTestResourceLifecycleManager {

  LocalStackContainer sqs;
  @Override
  public Map<String, String> start() {
    sqs = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
        .withServices(Service.SQS);

    sqs.start();
    return Map.of(
        "quarkus.aws.sqs.endpoint-override", sqs.getEndpointOverride(Service.SQS).toString(),
        "quarkus.sqs.aws.region", sqs.getRegion(),
        "quarkus.sqs.aws.credentials.static-provider.access-key-id", sqs.getAccessKey(),
        "quarkus.sqs.aws.credentials.static-provider.secret-access-key", sqs.getSecretKey());
  }

  @Override
  public void stop() {
    if (sqs != null) {
      sqs.stop();
      sqs = null;
    }
  }
}
