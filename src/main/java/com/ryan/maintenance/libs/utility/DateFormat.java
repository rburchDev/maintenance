package com.ryan.maintenance.libs.utility;

import com.ryan.maintenance.libs.base.Base;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFormat extends Base {
    /**
     * Method to convert given date purchased into correct DateTime format
     * @param purchasedDate String date value of vehicle purchase date
     * @return String formatted date field
     */
    public String formatDate(String purchasedDate) {
        LocalDate date = LocalDate.parse(purchasedDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
