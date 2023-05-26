package com.ryan.maintenance.api.api_controller;

import com.ryan.maintenance.exceptions.mongo.MongoDbException;
import com.ryan.maintenance.exceptions.validation.BadRequestException;
import com.ryan.maintenance.exceptions.validation.NotFoundException;
import com.ryan.maintenance.libs.base.Base;
import com.ryan.maintenance.libs.database.Mongo;
import com.ryan.maintenance.libs.validation.Validation;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.bson.Document;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "vaultAPI")
public class ApiController extends Base {

    public ApiController() {
        this.validate = new Validation();
        this.mongo = new Mongo();
    }

    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "HealthCheck", description = "Used to determine the health of the application")
    public ResponseEntity<Document> getHealth() {
        Document doc = new Document().append("ping", "pong");

        LOGGER.info("Health Check Success");

        return new ResponseEntity<>(doc, HttpStatus.OK);
    }

    @GetMapping(value = "/getMaintenance", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Maintenance", description = "Set of APIs to maintain a collection of vehicle maintenance records within a MongoDB")
    public ResponseEntity<Document> getCreds(@RequestParam(value = "year") Integer year, String make, String model)
            throws NotFoundException, MongoDbException, BadRequestException {
        try {
            LOGGER.info("Getting Vehicle Maintenance Record for: " + year + " " + make + " " + model);

            String convertedString = String.format("%d_%s_%s", year,  make, model);

            Document docResponse = this.mongo.getOne("Vehicle", convertedString);

            return new ResponseEntity<>(docResponse, HttpStatus.OK);

        } catch (MongoDbException e) {
            LOGGER.error(e.getMessage());
            throw new MongoDbException(e.getMessage());
        } catch (NotFoundException e) {
            LOGGER.error(e.getMessage());
            throw new NotFoundException(e.getMessage());
        }
    }
}
