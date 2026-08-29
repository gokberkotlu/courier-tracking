package com.gokberkotlu.couriertrackingapp.reader;

import com.gokberkotlu.couriertrackingapp.dto.StoreData;
import com.gokberkotlu.couriertrackingapp.exception.StoreDataLoadException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class StoreJsonReader {
  private final ObjectMapper objectMapper;

  @Value("${courier-tracking.store.file_path}")
  private Resource resource;

  public List<StoreData> read() {
    try (InputStream inputStream = resource.getInputStream()) {
      return objectMapper.readValue(inputStream, new TypeReference<>() {});
    } catch (IOException e) {
      throw new StoreDataLoadException(
          String.format(
              "Failed to read store data from '%s'.", ((ClassPathResource) resource).getPath()),
          e);
    }
  }
}
