package org.vinay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class DogApiResponse {
    @JsonProperty("data")
    public DogData data;

    @JsonProperty("links")
    public DogLinks links;

    // Getter and Setter methods

    public static DogApiResponse parseDogApiResponse(String jsonResponse) {
        // Implement your JSON parsing logic here (using Jackson, Gson, etc.)
        // Return an instance of DogApiResponse
        return null; // Replace with actual parsing logic
    }

    public DogData getData() {
        return data;
    }

    public DogLinks getLinks() {
        return links;
    }

    // Other methods or utility functions can be added here

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DogData {
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
    }


    public static class DogLinks {
        public String self;

        // Getter and Setter methods
    }
}
