package org.vinay;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class DogFactLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String DYNAMODB_TABLE_NAME = "dogfacts";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String fact = getDogFact();
        saveFactToDynamoDB(fact);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Fact stored successfully!");
        return response;
    }

    private String getDogFact() {
        // Code to call the external API and extract the fact from the response
        // You can use a library like Apache HttpClient or HttpURLConnection for HTTP requests
        // Parse the JSON response and return the fact
        return "The Mayans and Aztecs symbolized every tenth day with the dog...";
    }

    private void saveFactToDynamoDB(String fact) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

        // Generate a unique ID for the fact (you may use UUID or any other method)
        String factId = "uniqueId";

        // Save the fact to DynamoDB
        Item item = new Item().withPrimaryKey("factId", factId).withString("fact", fact);
        table.putItem(item);
    }
}
