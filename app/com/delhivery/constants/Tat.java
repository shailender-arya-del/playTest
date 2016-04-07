package com.delhivery.constants;

public enum Tat {
  ONE_HR("1hr"),
  TWO_HR("2hr"),
  ONE_DAY("1Day"),
  TWO_DAY("2Day"),
  STANDARD("7Day");
  
  private String val;
  private Tat(String val){
    this.val = val;
  }
  
  public String value(){
    return val;
  }
}
