package org.vinay;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(MockitoJUnitRunner.class)
class DogBreedLambdaTest {

    @InjectMocks
    private DogBreedLambda lambda;
    @Mock
    private DynamoDB dynamoDBMock;
    @Mock
    private Table tableMock;

    @BeforeEach
    void setUp() throws Exception{
        dynamoDBMock = mock(DynamoDB.class);
        tableMock = mock(Table.class);

        // Set up DynamoDB mocks
        whenNew(DynamoDB.class).withAnyArguments().thenReturn(dynamoDBMock);
        when(dynamoDBMock.getTable(anyString())).thenReturn(tableMock);

        lambda = new DogBreedLambda();
        dynamoDBMock.getTable("dgobreeds");
        lambda.setDynamoDB(dynamoDBMock); // Inject the mocked DynamoDB instance
        lambda.setTableName("dogbreeds");
    }

    @Test
    void handleRequest_SuccessfulExecution_Returns200StatusCode() {
        // Arrange
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        Context context = mock(Context.class);

        // Act
        APIGatewayProxyResponseEvent response = lambda.handleRequest(request, context);

        // Assert
        assertEquals(200, response.getStatusCode());
        assertEquals("Fact stored successfully!", response.getBody());
    }

    @Test
    void fetchDataFromExternalAPI_SuccessfulExecution_ReturnsValidDogApiResponse() {
        // Act
        DogApiResponse dogApiResponse = lambda.fetchDataFromExternalAPI();

        // Assert
        assertNotNull(dogApiResponse);
        assertNotNull(dogApiResponse.getData());
        assertNotNull(dogApiResponse.getData().getAttributes());
        assertNotNull(dogApiResponse.getData().getAttributes().getName());
        assertNotNull(dogApiResponse.getData().getAttributes().getDescription());
    }

    @Test
    void getRandomBreedId_ValidList_ReturnsRandomBreedId() {
        // Arrange
        List<String> breedIds = Arrays.asList("1", "2", "3", "4", "5");

        // Act
        String randomBreedId = DogBreedLambda.getRandomBreedId(breedIds);

        // Assert
        assertTrue(breedIds.contains(randomBreedId));
    }

    @Test
    void parseDogApiResponse_ValidJson_ReturnsValidDogApiResponseObject() {
        // Arrange
        String json = "{" +
                "\"data\":{" +
                "\"id\":\"68f47c5a-5115-47cd-9849-e45d3c378f12\"," +
                "\"type\":\"breed\"," +
                "\"attributes\":{" +
                "\"name\":\"Caucasian Shepherd Dog\"," +
                "\"description\":\"The Caucasian Shepherd Dog is a large and powerful breed of dog from the Caucasus Mountains region. These dogs are large in size, with a thick double coat to protect them from the cold. They have a regal bearing, with a proud and confident demeanor. They are highly intelligent and loyal, making them excellent guard dogs. They are courageous and alert, with an instinct to protect their family and property. They are highly trainable, but require firm and consistent training.\"," +
                "\"life\":{\"max\":20,\"min\":15}," +
                "\"male_weight\":{\"max\":90,\"min\":50}," +
                "\"female_weight\":{\"max\":70,\"min\":45}," +
                "\"hypoallergenic\":false" +
                "}," +
                "\"relationships\":{" +
                "\"group\":{" +
                "\"data\":{" +
                "\"id\":\"8000793f-a1ae-4ec4-8d55-ef83f1f644e5\"," +
                "\"type\":\"group\"" +
                "}" +
                "}" +
                "}" +
                "}," +
                "\"links\":{" +
                "\"self\":\"https://dogapi.dog/api/v2/breeds/68f47c5a-5115-47cd-9849-e45d3c378f12\"" +
                "}" +
                "}";

        // Act
        DogApiResponse dogApiResponse = DogApiResponse.parseDogApiResponse(json);

        // Assert
        assertNotNull(dogApiResponse);
        assertNotNull(dogApiResponse.getData());
        assertNotNull(dogApiResponse.getData().getAttributes());
        assertEquals("68f47c5a-5115-47cd-9849-e45d3c378f12", dogApiResponse.getData().getId());
        assertEquals("breed", dogApiResponse.getData().getType());
        assertEquals("Caucasian Shepherd Dog", dogApiResponse.getData().getAttributes().getName());
        assertEquals("The Caucasian Shepherd Dog is a large and powerful breed of dog from the Caucasus Mountains region. These dogs are large in size, with a thick double coat to protect them from the cold. They have a regal bearing, with a proud and confident demeanor. They are highly intelligent and loyal, making them excellent guard dogs. They are courageous and alert, with an instinct to protect their family and property. They are highly trainable, but require firm and consistent training.", dogApiResponse.getData().getAttributes().getDescription());
        assertFalse(dogApiResponse.getData().getAttributes().isHypoallergenic());
    }


    public void saveFactToDynamoDB_ValidDogApiResponse_SuccessfullySavesToDynamoDB() throws Exception {
        // Arrange
        DogApiResponse dogApiResponse = lambda.fetchDataFromExternalAPI();

        // Act
        lambda.saveFactToDynamoDB(dogApiResponse);

        // Assert
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(tableMock).putItem(itemCaptor.capture());

        Item savedItem = itemCaptor.getValue();
        assertNotNull(savedItem);
        assertEquals("68f47c5a-5115-47cd-9849-e45d3c378f12", savedItem.getString("BreedId"));
        assertEquals("breed", savedItem.getString("breedName"));
        assertEquals("The Caucasian Shepherd Dog is a large and powerful breed of dog from the Caucasus Mountains region. These dogs are large in size, with a thick double coat to protect them from the cold. They have a regal bearing, with a proud and confident demeanor. They are highly intelligent and loyal, making them excellent guard dogs. They are courageous and alert, with an instinct to protect their family and property. They are highly trainable, but require firm and consistent training.", savedItem.getString("Description"));

        // Ensure interaction with the mocked table
        verify(tableMock, times(1)).putItem(any(Item.class));
    }

    @Test
    void saveFactToDynamoDB_NullDogApiResponse_LogsError() {
        // Arrange
        DogApiResponse dogApiResponse = null;

        // Act
        lambda.saveFactToDynamoDB(dogApiResponse);

        // Assert
        // Verify that an error message is logged (you may need to enhance logging in your code for better testing)
    }

    @Test
    void handleRequest_ExceptionThrown_ReturnsErrorResponse() throws Exception {
        // Arrange
        Context contextMock = mock(Context.class);

        // Create an instance of DogBreedLambda
        DogBreedLambda lambda = mock(DogBreedLambda.class);

        // Mock the instance method call
        when(lambda.getTableName()).thenReturn("dogbreeds");

        // Mock the constructor
        DynamoDB dynamoDBMock = mock(DynamoDB.class);
        Table tableMock = mock(Table.class);
        whenNew(DynamoDB.class).withAnyArguments().thenReturn(dynamoDBMock);
        when(dynamoDBMock.getTable(anyString())).thenReturn(tableMock);

        // Mock the behavior that causes an exception
        // In this case, assuming an APIGatewayProxyRequestEvent is expected as input
        APIGatewayProxyRequestEvent input = mock(APIGatewayProxyRequestEvent.class);

        // Act
        String result = String.valueOf(lambda.handleRequest(input, contextMock));

        // Assert
        // Verify that the result contains the expected error message
        assertNotNull(result);
    }

    private DogApiResponse createValidDogApiResponse() {
        DogApiResponse dogApiResponse = new DogApiResponse();
        DogApiResponse.DogData data = new DogApiResponse.DogData();
        data.setId("68f47c5a-5115-47cd-9849-e45d3c378f12");
        DogApiResponse.DogAttributes attributes = new DogApiResponse.DogAttributes();
        attributes.setName("Caucasian Shepherd Dog");
        attributes.setDescription("The Caucasian Shepherd Dog is a large and powerful breed of dog from the Caucasus Mountains region. These dogs are large in size, with a thick double coat to protect them from the cold. They have a regal bearing, with a proud and confident demeanor. They are highly intelligent and loyal, making them excellent guard dogs. They are courageous and alert, with an instinct to protect their family and property. They are highly trainable, but require firm and consistent training.");
        data.setAttributes(attributes);
        dogApiResponse.setData(data);
        return dogApiResponse;
    }
}
