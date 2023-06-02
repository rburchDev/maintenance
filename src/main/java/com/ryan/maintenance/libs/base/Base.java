package com.ryan.maintenance.libs.base;

import com.ryan.maintenance.libs.database.Mongo;
import com.ryan.maintenance.libs.validation.Validation;
import com.ryan.maintenance.libs.utility.NameFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;

public abstract class Base {
    public static final Logger LOGGER = LogManager.getLogger();
    public static MessageDigest sha;
    public Validation validate;
    public Mongo mongo;
    public NameFormat nameFormat;

    public Base() {
    }
}
