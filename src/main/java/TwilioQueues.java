import static spark.Spark.*;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import com.twilio.sdk.verbs.TwiMLResponse;

public class TwilioQueues {
    public static void main(String[] args){
        final String QUEUE_NAME = "messages";

        get("/", (req, res) -> "Hello World!");
        post("/SayHello", (req, res) -> {
            // Create Factory
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");

            // establish connection
            Connection connection = factory.newConnection();

            // Declare channel
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // Convert message to JSON
            String from = req.queryParams("From");
            String to = req.queryParams("To");
            String body = req.queryParams("Body");
            Message message = new Message(from, to, body);
            Gson gson = new Gson();
            String jsonMessage = gson.toJson(message);

            // Publish message to channel
            channel.basicPublish("", QUEUE_NAME, null, jsonMessage.getBytes());

            // Close connections
            channel.close();
            connection.close();

            // Be a nice citizen
            res.status(200);
            res.type("text/xml");
            TwiMLResponse twiml = new TwiMLResponse();
            return twiml.toXML();
        });
    }
}