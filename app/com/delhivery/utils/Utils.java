package com.delhivery.utils;

import java.io.IOException;

import org.apache.http.HttpStatus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class Utils {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
  }

  private Utils() {
  }

  public static JsonNode Object2Json(Object object) {
    return OBJECT_MAPPER.valueToTree(object);
  }

  public static ObjectNode newJsonObject() {
    return OBJECT_MAPPER.createObjectNode();
  }

  public static <T> T json2Object(Class<T> clazz, JsonNode requestJson)
          throws IOException {
    T t = null;
    try {
      t = (T) OBJECT_MAPPER.readerFor(clazz).readValue(requestJson);  //(requestJson, clazz);
    } catch (IOException e) {
      
      throw e;
    }
    return t;
  }

  public static boolean isStatusOK(int status) {
    switch (status) {
      case HttpStatus.SC_OK:
      case HttpStatus.SC_CREATED:
      case HttpStatus.SC_ACCEPTED:
      case HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION:
      case HttpStatus.SC_NO_CONTENT:
      case HttpStatus.SC_RESET_CONTENT:
      case HttpStatus.SC_PARTIAL_CONTENT:
      case HttpStatus.SC_MULTI_STATUS:
        return true;
    }
    return false;
  }

}
