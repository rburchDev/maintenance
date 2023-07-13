package com.ryan.maintenance.libs.utility;

import com.ryan.maintenance.libs.base.Base;

/**
 * Class to convert individual inputs into correct DB naming
 */
public class NameFormat extends Base {
    /**
     * Method to convert params into DB acceptable String
     * @param year Integer year of vehicle
     * @param make String make of vehicle
     * @param model String model of vehicle
     * @return DB String format
     */
    public String setName(Integer year, String make, String model) {
        String refinedModel = model.replace(" ", "_");
        return String.format("%d_%s_%s", year,  make.toLowerCase(), refinedModel.toLowerCase());
    }
}
