package com.example.csvtojson;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CsvController {

    @PostMapping(value = "/csv-to-json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Map<String, String>> csvToJson(@RequestPart("file") MultipartFile file) throws IOException, CsvValidationException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio.");
        }

        // Se seu CSV for do Excel BR, normalmente é ';'
        char separator = ','; // troque para ';'

        CSVParser parser = new CSVParserBuilder()
                .withSeparator(separator)
                .withQuoteChar('"') // padrão CSV
                .build();

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(parser)
                     .build()) {

            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new IllegalArgumentException("CSV sem cabeçalho.");
            }

            List<Map<String, String>> rows = new ArrayList<>();
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                // pula linha totalmente vazia
                boolean allBlank = true;
                for (String v : values) {
                    if (v != null && !v.isBlank()) { allBlank = false; break; }
                }
                if (allBlank) continue;

                Map<String, String> obj = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i] == null ? "" : headers[i].trim();
                    String value = (i < values.length && values[i] != null) ? values[i].trim() : "";
                    obj.put(key, value);
                }
                rows.add(obj);
            }

            return rows;
        }
    }
}