package com.matheus.component.resource;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

public class SQSResource implements QuarkusTestResourceLifecycleManager {

  private LocalStackContainer localStack;

  @Override
  public Map<String, String> start() {
    localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
        .withServices(SQS);

    localStack.start();

    try (SqsClient sqsClient = SqsClient.builder()
        .endpointOverride(localStack.getEndpointOverride(SQS))
        .region(Region.of(localStack.getRegion()))
        .credentialsProvider(() -> AwsBasicCredentials.create(localStack.getAccessKey(),
            localStack.getSecretKey()))
        .build()) {

      CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
          .queueName("aws-example-queue")
          .build();

      sqsClient.createQueue(createQueueRequest);
    }

    return Map.of(
        "quarkus.sqs.endpoint-override", localStack.getEndpointOverride(SQS).toString(),
        "quarkus.sqs.aws.region", localStack.getRegion(),
        "quarkus.sqs.aws.credentials.static-provider.access-key-id", localStack.getAccessKey(),
        "quarkus.sqs.aws.credentials.static-provider.secret-access-key", localStack.getSecretKey());
  }

  @Override
  public void stop() {
    if (localStack != null) {
      localStack.stop();
      localStack = null;
    }
  }
}
