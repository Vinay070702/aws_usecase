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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;


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
    private String fetchDataFromExternalAPI() {
        // Replace the URL with your actual external API endpoint
        String apiUrl = "https://dog-api.kinduff.com/api/facts";

        try {
            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(apiUrl);

            HttpResponse response = httpClient.execute(httpGet);

            // Check if the response code is successful (2xx)
            if (response.getStatusLine().getStatusCode() == 200) {
                // Read the response content
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return result.toString();
            } else {
                // Handle non-successful response codes if needed
                throw new RuntimeException("Failed to fetch data from external API. Status code: " +
                        response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            // Handle exceptions if any
            throw new RuntimeException("Error fetching data from external API: " + e.getMessage(), e);
        }
    }
    private String getDogFact() {
        // Code to call the external API and extract the fact from the response
        // You can use a library like Apache HttpClient or HttpURLConnection for HTTP requests
        // Parse the JSON response and return the fact
        return fetchDataFromExternalAPI();
    }

    private void saveFactToDynamoDB(String fact) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

        // Generate a unique ID for the fact (you may use UUID or any other method)
        String factId = generateUniqueId();

        // Save the fact to DynamoDB
        Item item = new Item().withPrimaryKey("factId", factId).withString("fact", fact);
        table.putItem(item);
    }

    private String generateUniqueId() {
        // Use UUID to generate a unique ID
        return UUID.randomUUID().toString();
    }
}
