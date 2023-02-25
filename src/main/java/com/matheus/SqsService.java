package com.matheus;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;

@ApplicationScoped
public class SqsService {

  @Inject
  SqsClient sqsClient;

  public void produce(String messageBody) {
    GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
        .queueName("aws-example-queue")
        .build();

    GetQueueUrlResponse queueUrl = sqsClient.getQueueUrl(getQueueUrlRequest);

    sqsClient.sendMessage(
        builder -> builder
            .queueUrl(queueUrl.queueUrl())
            .messageBody(messageBody));
  }

  public List<Message> consume() {
    GetQueueUrlRequest getQueueUrlRequest = GetQueueUrlRequest.builder()
        .queueName("aws-example-queue")
        .build();

    GetQueueUrlResponse getQueueUrlResponse = sqsClient.getQueueUrl(getQueueUrlRequest);

    return sqsClient.receiveMessage(
            m -> m.maxNumberOfMessages(10)
                .queueUrl(getQueueUrlResponse.queueUrl()))
        .messages();
  }
}
