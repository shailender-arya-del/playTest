package models;

import lombok.Data;

@Data
public class Address {
  String Country;
  String State;
  String city;
  String pzCode;
  String locality;
  String addressLine1;
  String addressLine2;
  String landMark;

}
