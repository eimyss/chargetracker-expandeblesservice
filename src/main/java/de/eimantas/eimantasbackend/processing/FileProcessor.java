package de.eimantas.eimantasbackend.processing;


import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

@Service
public class FileProcessor {


  private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

  private SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

  public <T> List<T> loadObjectList(Class<T> type, String fileName) {
    try {
      CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader().withColumnSeparator(';');
      CsvMapper mapper = (CsvMapper) new CsvMapper()
          .registerModule(new ParameterNamesModule())
          .registerModule(new Jdk8Module())
          .registerModule(new JavaTimeModule())
          .registerModule(new Jackson2HalModule())
          .setDateFormat(df);
      File file = new ClassPathResource(fileName).getFile();
      MappingIterator<T> readValues =
          mapper.reader(type).with(bootstrapSchema).readValues(file);
      return readValues.readAll();
    } catch (Exception e) {
      logger.error("Error occurred while loading object list from file " + fileName, e);
      return Collections.emptyList();
    }
  }

}
