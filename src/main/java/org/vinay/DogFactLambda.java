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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class DogFactLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final String DYNAMODB_TABLE_NAME = "dogbreeds";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        DogApiResponse dogApiResponse = getDogFact();
        saveFactToDynamoDB(dogApiResponse);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Fact stored successfully!");
        return response;
    }
    private DogApiResponse fetchDataFromExternalAPI() {
        List<String> breedIds = Arrays.asList(
                "68f47c5a-5115-47cd-9849-e45d3c378f12",
                "4ddbe251-72af-495e-8e9d-869217e1d92a",
                "f534c847-bed1-4b58-b194-dc06ecfe20f9",
                "30f62219-e225-42cd-bd07-02425f944c07",
                "087979f3-1c45-4d8a-a153-462bf5ea379e",
                "dbff689b-8370-4b6a-9306-215aba549102",
                "6f540c30-27a8-48cc-8d88-0b1d9fa99167",
                "20b1d8be-ae44-4a70-8526-0612904bc9b2",
                "6dee41b1-0805-4f4e-a079-c8b1cdfa1768",
                "beff84c3-66c4-4335-beba-f346c2565881"
        );
        // Replace the URL with your actual external API endpoint
        String apiUrl = "https://dogapi.dog/api/v2/breeds/"+getRandomBreedId(breedIds);

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

                return parseDogApiResponse(result.toString());
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
    private static String getRandomBreedId(List<String> breedIds) {
        Random random = new Random();
        int randomIndex = random.nextInt(breedIds.size());
        return breedIds.get(randomIndex);
    }
    private DogApiResponse getDogFact() {
        // Code to call the external API and extract the fact from the response
        // You can use a library like Apache HttpClient or HttpURLConnection for HTTP requests
        // Parse the JSON response and return the fact
        return fetchDataFromExternalAPI();
    }
    public static DogApiResponse parseDogApiResponse(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            return objectMapper.treeToValue(rootNode, DogApiResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void saveFactToDynamoDB(DogApiResponse dogApiResponse) {
        if (dogApiResponse == null) {
            // Handle the case when dogApiResponse is null
            System.err.println("Error: dogApiResponse is null");
            return;
        }
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);

        String breedId = null;
        String breedName = null;
        String description = null;

        DogApiResponse.DogData dogData = dogApiResponse.getData();
        if (dogData != null) {
            breedId = dogData.getId();
            DogApiResponse.DogAttributes attributes = dogData.getAttributes();
            if (attributes != null) {
                breedName = attributes.getName();
                description = attributes.getDescription();
            }
        }

        String factId = generateUniqueId();

        // Save the fact to DynamoDB
        Item item = new Item()
                .withPrimaryKey("BreedId", breedId)
                .withString("breedName", breedName)
                .withString("Description", description);
        table.putItem(item);
    }

    private String generateUniqueId() {
        // Use UUID to generate a unique ID
        return UUID.randomUUID().toString();
    }
}
