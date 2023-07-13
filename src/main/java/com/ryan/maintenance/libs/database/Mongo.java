package com.ryan.maintenance.libs.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.ryan.maintenance.exceptions.mongo.MongoDbException;
import com.ryan.maintenance.exceptions.validation.NotFoundException;
import com.ryan.maintenance.libs.base.Base;
import com.ryan.maintenance.libs.utility.Utility;
import com.ryan.maintenance.libs.validation.Validation;

import com.ryan.maintenance.models.VehicleModel;
import io.github.cdimascio.dotenv.Dotenv;

import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;

import java.util.Objects;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.eq;

public class Mongo extends Base {
    private static final Dotenv DOTENV = Utility.getDotEnv(false);
    private static final String MONGOCLIENT = Objects.requireNonNull(DOTENV.get("MONGOURL"));
    private static final String SECRET = Objects.requireNonNull(DOTENV.get("SECRET"));
    private static final String DATABASE = "maintenance";
    private static final String COLLECTION_VEHICLE = "vehicleCollection";
    private static final String COLLECTION_MAINTENANCE = "maintenanceCollection";

    public Mongo() {
        this.validate = new Validation();
    }

    private Document setDocument(String model, String data, String datePurchased) {
        try {
            switch (model) {
                case "vehicle" -> {
                    VehicleModel vehicleModel = new VehicleModel(data, LocalDate.now().toString(), datePurchased);
                    ObjectMapper mapper = new ObjectMapper();
                    String docString = mapper.writeValueAsString(vehicleModel);
                    return Document.parse(docString);
                }
                case "maintenance" -> LOGGER.info("SOON TO HAVE");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * helper method to test if the connection to Mongo is good
     * @param mongoConnection the established Mongo connection
     * @return Command result or null
     * @throws MongoDbException Throw exception if Mongo issue occurs
     */
    private Document testConnection(MongoClient mongoConnection) throws MongoDbException {

        MongoDatabase db = mongoConnection.getDatabase(DATABASE);
        Document commandResult = null;
        try {
            Bson command = new BsonDocument("ping", new BsonInt64(1));
            commandResult = db.runCommand(command);
            LOGGER.info("Connection successful to Mongo");
            LOGGER.info(commandResult);
        } catch (MongoException e) {
            LOGGER.error("ERROR WITH MONGO");
            throw new MongoDbException("Error with Mongo", e);

        }
        return commandResult;
    }

    /**
     * method to create connection to collections
     * @param db MongoDB Database connection
     * @return HashMap Collection Connections
     * @throws MongoDbException Throw exception if Mongo issue occurs
     */
    public HashMap<String, MongoCollection<Document>> mongoCollections(MongoDatabase db) throws MongoDbException {
        try {
            //Create HashMap to hold collection connections
            HashMap<String, MongoCollection<Document>> collections = new HashMap<>();
            //Establish connections
            MongoCollection<Document> maintenance = db.getCollection(COLLECTION_MAINTENANCE);
            MongoCollection<Document> vehicle = db.getCollection(COLLECTION_VEHICLE);

            //Add to HashMap and return
            collections.put("maintenance", maintenance);
            collections.put("vehicle", vehicle);
            return collections;

        } catch (MongoException e) {
            LOGGER.error("ERROR with Mongo Collection Connection");
            throw new MongoDbException("Error with Mongo Collection Connection", e);
        }
    }

    /**
     * method to establish a Mongo connection and create array of collection connections
     * @return HashMap of Collection Connections
     * @throws MongoDbException Throw exception if Mongo issue occurs
     */
    public HashMap<String, MongoCollection<Document>> mongoClient() throws MongoDbException {

        MongoClient mongoConnection = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(MONGOCLIENT))
                        .build()
        );
        MongoDatabase db = mongoConnection.getDatabase(DATABASE);

            try {
                HashMap<String, MongoCollection<Document>> collections = mongoCollections(db);

                Document response = testConnection(mongoConnection);
                LOGGER.info("Connection Test result: " + response);

                return collections;

            } catch (MongoException e) {
                LOGGER.error("ERROR with Mongo Connection");
                throw new MongoDbException("Error with Mongo Connection", e);
            }

    }

    /**
     * method to find the passed in document based on the key value pair
     * @param key the key to look for
     * @param value the value to look for
     * @return Found Document or null
     * @throws MongoDbException Throw exception if Mongo issue occurs
     */
    public Document getOne(String key, String value, String collection) throws MongoDbException, NotFoundException {
        Document response;

        try {
            HashMap<String, MongoCollection<Document>> collections = mongoClient();

            response = collections.get(collection).find(eq(key, value))
                    .first();
            // Check if the response is null or not, if null throw ERROR
            response = this.validate.checkResponse(response);

        } catch (MongoException | MongoDbException e) {
            LOGGER.error("An error occurred with MongoDB");
            throw new MongoDbException("Error with getting Mongo Document", e);
        } catch (RuntimeException er) {
            LOGGER.error("A Runtime Error occurred");
            throw new MongoDbException("Error with getting Mongo Document", er);
        } catch (NotFoundException e) {
            LOGGER.warn("The response returned a Null when attempting to get the document");
            throw new NotFoundException("Response returned as NULL", e);
        }
        return response;
    }

    public String setOne(String key, String value, String datePurchased, String collection, Boolean upsert) throws MongoDbException {
        try {
            Document modeledDoc = Objects.requireNonNull(setDocument(collection, value, datePurchased));

            HashMap<String, MongoCollection<Document>> collections = mongoClient();

            Bson filter = Filters.eq(key, value);
            Bson update = new Document("$set", modeledDoc);
            UpdateOptions options = new UpdateOptions().upsert(upsert);

            collections.get(collection).updateOne(filter, update, options);
        } catch (MongoException | MongoDbException e) {
            LOGGER.error("An error occurred with MongoDB");
            throw new MongoDbException("Error with getting Mongo Document", e);
        }
        return "Success";
    }
}

