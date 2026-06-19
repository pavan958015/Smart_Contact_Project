package com.scm.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.List;
import com.opencsv.CSVReader;
import ezvcard.Ezvcard;
import ezvcard.VCard;

class ImportExportTests {

    @Test
    void testCSVParser() throws Exception {
        String csvData = "Name,Email,Phone,Address,Description,Website,LinkedIn,Favorite\n" +
                         "John Doe,john@example.com,123456789,123 Main St,Friend,http://john.com,http://linkedin.com/in/john,true\n" +
                         "Jane Smith,jane@example.com,987654321,456 Oak St,Colleague,http://jane.com,,false";
        
        try (Reader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvData.getBytes())))) {
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> rows = csvReader.readAll();
            assertEquals(3, rows.size());
            
            // Header
            String[] header = rows.get(0);
            assertEquals("Name", header[0]);
            
            // First Row
            String[] row1 = rows.get(1);
            assertEquals("John Doe", row1[0]);
            assertEquals("john@example.com", row1[1]);
            assertEquals("123456789", row1[2]);
            assertEquals("123 Main St", row1[3]);
            assertEquals("Friend", row1[4]);
            assertEquals("http://john.com", row1[5]);
            assertEquals("http://linkedin.com/in/john", row1[6]);
            assertEquals("true", row1[7]);
        }
    }

    @Test
    void testVCardParser() throws Exception {
        String vcardData = "BEGIN:VCARD\n" +
                           "VERSION:4.0\n" +
                           "FN:John Doe\n" +
                           "EMAIL:john@example.com\n" +
                           "TEL:123456789\n" +
                           "NOTE:Friend\n" +
                           "URL:http://john.com\n" +
                           "URL:http://linkedin.com/in/john\n" +
                           "END:VCARD";
        
        List<VCard> vcards = Ezvcard.parse(new ByteArrayInputStream(vcardData.getBytes())).all();
        assertEquals(1, vcards.size());
        VCard vcard = vcards.get(0);
        assertEquals("John Doe", vcard.getFormattedName().getValue());
        assertEquals("john@example.com", vcard.getEmails().get(0).getValue());
        assertEquals("123456789", vcard.getTelephoneNumbers().get(0).getText());
        assertEquals("Friend", vcard.getNotes().get(0).getValue());
        assertEquals("http://john.com", vcard.getUrls().get(0).getValue());
        assertEquals("http://linkedin.com/in/john", vcard.getUrls().get(1).getValue());
    }
}
