package com.matheus.component;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.matheus.SqsService;
import com.matheus.component.resource.SQSResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;

@QuarkusTest
@QuarkusTestResource(SQSResource.class)
class MessageComponentTest {

  @Inject
  SqsClient sqsClient;
  @Inject
  SqsService sqsService;

  @Test
  @DisplayName("Should produce a message successfully")
  void shouldProduceMessageSuccessfully() {
    GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
        .queueName("aws-example-queue")
        .build();

    GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);

    sqsService.produce("Produce message");
    sqsService.produce("Produce message, again");

    List<Message> messages = sqsClient.receiveMessage(
            m -> m.maxNumberOfMessages(10)
                .queueUrl(getQueueUrlResponse.queueUrl()))
        .messages();

    assertAll(
        () -> assertEquals(2, messages.size()),
        () -> assertEquals("Produce message", messages.get(0).body()),
        () -> assertEquals("Produce message, again", messages.get(1).body()));
  }

  @Test
  @DisplayName("Should consume a message successfully")
  void shouldConsumeMessageSuccessfully() {

    GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
        .queueName("aws-example-queue")
        .build();

    GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);

    sqsClient.sendMessage(
        builder -> builder
            .queueUrl(getQueueUrlResponse.queueUrl())
            .messageBody("message 1"));

    sqsClient.sendMessage(
        builder -> builder
            .queueUrl(getQueueUrlResponse.queueUrl())
            .messageBody("message 2"));

    List<Message> messages = sqsService.consume();

    assertAll(
        () -> assertEquals(2, messages.size()),
        () -> assertEquals("message 1", messages.get(0).body()),
        () -> assertEquals("message 2", messages.get(1).body()));
  }
}
