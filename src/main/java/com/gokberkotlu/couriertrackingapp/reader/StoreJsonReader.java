package com.gokberkotlu.couriertrackingapp.reader;

import com.gokberkotlu.couriertrackingapp.dto.StoreData;
import com.gokberkotlu.couriertrackingapp.exception.StoreDataLoadException;
import com.gokberkotlu.couriertrackingapp.properties.StoreProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class StoreJsonReader {
  private final ObjectMapper objectMapper;
  private final StoreProperties storeProperties;

  public List<StoreData> read() {
    try (InputStream inputStream = storeProperties.storeFileResource().getInputStream()) {
      return objectMapper.readValue(inputStream, new TypeReference<>() {});
    } catch (IOException e) {
      throw new StoreDataLoadException(
          String.format(
              "Failed to read store data from '%s'.",
              ((ClassPathResource) storeProperties.storeFileResource()).getPath()),
          e);
    }
  }
}
