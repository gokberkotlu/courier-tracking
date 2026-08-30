package com.gokberkotlu.couriertrackingapp.store;

import com.gokberkotlu.couriertrackingapp.exception.StoreDataLoadException;
import com.gokberkotlu.couriertrackingapp.properties.StoreProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class StoreJsonReader {
  private final ObjectMapper objectMapper;
  private final StoreProperties storeProperties;

  public List<StoreJsonModel> read() {
    try (InputStream inputStream = storeProperties.storeFileResource().getInputStream()) {
      return objectMapper.readValue(inputStream, new TypeReference<List<StoreJsonModel>>() {});
    } catch (IOException e) {
      throw new StoreDataLoadException(
          "Failed to read store data from " + storeProperties.storeFileResource().getDescription(),
          e);
    }
  }
}
