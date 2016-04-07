package com.delhivery.dependencies.ep.RequestResponse;

import java.util.List;

import lombok.Data;

@Data
public class ExpathResponse {
  private String originCode;
  private String destCode;
  private String productType;
  private String serviceType;
  private String tat;
  private List<TimeSlotWithPrice> priceTimeSlots = null;
}
