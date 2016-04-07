package com.delhivery.dependencies.ep.RequestResponse;

import lombok.Data;

@Data
public class TimeSlotWithPrice {

  private String startTime;
  private String endTime;
  private String price;
  
  public TimeSlotWithPrice(String stTime, String eTime, String price){
    this.startTime = stTime;
    this.endTime = eTime;
    this.price = price;
  }
}
