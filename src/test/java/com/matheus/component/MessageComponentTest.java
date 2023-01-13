package com.matheus.component;

import static io.restassured.RestAssured.given;

import com.matheus.component.resource.SQSResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

@QuarkusTest
@QuarkusTestResource(SQSResource.class)
class MessageComponentTest {

  @Inject
  SqsClient sqsClient;

  @Test
  @DisplayName("Should consume a message successfully")
  void shouldConsumeMessageSuccessfully() {

    CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
        .queueName("aws-example-queue")
        .build();
    sqsClient.createQueue(createQueueRequest);

    sqsClient.sendMessage(
        builder -> builder.queueUrl("http://localhost:4566/000000000000/aws-example-queue")
            .messageBody("message"));

    given()
        .log().ifValidationFails()
        .when()
        .get("/messages/consume")
        .then()
        .log().ifValidationFails()
        .statusCode(200)
        .body(Matchers.is(""));
  }
}
