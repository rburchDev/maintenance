package com.ryan.maintenance.libs.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;

import com.ryan.maintenance.exceptions.mongo.MongoDbException;
import com.ryan.maintenance.exceptions.validation.NotFoundException;
import com.ryan.maintenance.libs.base.Base;
import com.ryan.maintenance.libs.utility.Utility;
import com.ryan.maintenance.libs.validation.Validation;
import com.ryan.maintenance.models.DocumentModel;

import io.github.cdimascio.dotenv.Dotenv;

import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;

public class Mongo extends Base {
    private static final Dotenv DOTENV = Utility.getDotEnv(false);
    private static final String MONGOCLIENT = Objects.requireNonNull(DOTENV.get("MONGOURL"));
    private static final String SECRET = Objects.requireNonNull(DOTENV.get("SECRET"));
    private static final String DATABASE = "maintenance";
    private static final String COLLECTION = "vehicleCollection";

    public Mongo() {
        this.validate = new Validation();
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
     * method to establish a Mongo connection
     * @return the established Mongo connection
     * @throws MongoDbException Throw exception if Mongo issue occurs
     */
    public MongoCollection<Document> mongoClient() throws MongoDbException {

        MongoClient mongoConnection = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(MONGOCLIENT))
                        .build()
        );
        MongoDatabase db = mongoConnection.getDatabase(DATABASE);

        MongoCollection<Document> collection = db.getCollection(COLLECTION);
            try {
                Document response = testConnection(mongoConnection);
                LOGGER.info("Connection Test result: " + response);
            } catch (MongoException e) {
                LOGGER.error("ERROR with Mongo Connection");
                throw new MongoDbException("Error with Mongo Connection", e);
            }
            return collection;

    }

    /**
     * method to find the passed in document based on the key value pair
     * @param key the key to look for
     * @param value the value to look for
     * @return Found Document or null
     * @throws MongoDbException Throw exception if Mongo issue occurs
     */
    public Document getOne(String key, String value) throws MongoDbException, NotFoundException {
        Document response;
        Object docPassword;
        try {

            MongoCollection<Document> collection = mongoClient();

            response = collection.find(eq(key, value))
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
}

