package org.vinay;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.DoubleSummaryStatistics;
import java.util.Map;

@DynamoDBTable(tableName = "dogbreeds")
@DynamoDBDocument
public class DogApiResponse {
    @JsonProperty("data")
    public DogData data;

    @JsonProperty("links")
    public DogLinks links;

    public static DogApiResponse parseDogApiResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Create a DogApiResponse object and set its properties
            DogApiResponse dogApiResponse = new DogApiResponse();
            dogApiResponse.setData(objectMapper.treeToValue(rootNode.get("data"), DogData.class));
            dogApiResponse.setLinks(objectMapper.treeToValue(rootNode.get("links"), DogLinks.class));

            return dogApiResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle the exception appropriately, e.g., logging, throwing a custom exception
        }
    }

    // Getter and Setter methods

    public DogData getData() {
        return data;
    }

    public DogLinks getLinks() {
        return links;
    }

    public void setData(DogData data) {
        this.data = data;
    }

    public void setLinks(DogLinks links) {
        this.links = links;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @DynamoDBDocument
    public static class DogData {
        @DynamoDBHashKey
        private String id;
        private String type;
        private DogAttributes attributes;
        private Map<String, Object> relationships; // Add this field

        // Getter methods for id, type, attributes, and relationships

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public DogAttributes getAttributes() {
            return attributes;
        }

        public Map<String, Object> getRelationships() {
            return relationships;
        }

        public void setId(String s) {
            this.id = s;
        }

        public void setAttributes(DogAttributes attributes) {
            this.attributes = attributes;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DogAttributes {
        private String name;
        private int min_life;
        private int max_life;
        private String description;
        private boolean hypoallergenic;

        // Getter methods for name, min_life, max_life, description, and hypoallergenic

        public String getName() {
            return name;
        }

        public int getMinLife() {
            return min_life;
        }

        public int getMaxLife() {
            return max_life;
        }

        public String getDescription() {
            return description;
        }

        public boolean isHypoallergenic() {
            return hypoallergenic;
        }

        public void setName(String breedName) {
            this.name = breedName;
        }

        public void setDescription(String breedDescription) {
            this.description = breedDescription;
        }

    }

    public static class DogLinks {
        public String self;

        // Getter and Setter methods
    }
}
