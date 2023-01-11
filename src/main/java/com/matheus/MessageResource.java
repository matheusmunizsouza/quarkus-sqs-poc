package com.matheus;

import static org.jboss.resteasy.resteasy_jaxrs.i18n.LogMessages.LOGGER;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

@Path("/messages")
public class MessageResource {

  @Inject
  SqsClient sqsClient;

  @GET
  @Path("/consume")
  @Produces
  public Response consumer() {
    List<Message> messages = sqsClient.receiveMessage(
        m -> m.maxNumberOfMessages(10)
            .queueUrl("http://localhost:4566/000000000000/aws-example-queue")).messages();

    return Response.ok(messages.stream()
        .map(Message::body)
        .map(s -> new String(s.getBytes(StandardCharsets.UTF_8)))
        .toList()).build();
  }

  @POST
  @Path("/publish")
  @Consumes
  @Produces
  public Response publish(@QueryParam("message") String message) {
    LOGGER.infov("Sending message \"{0}\"", message);
    sqsClient.sendMessage(
        builder -> builder.queueUrl("http://localhost:4566/000000000000/aws-example-queue")
            .messageBody(message));
    LOGGER.infov("Message \"{0}\" sent", message);
    return Response.ok().build();
  }
}