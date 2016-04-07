package com.delhivery.Requests;

import lombok.Data;
import models.Address;

@Data
public class ServiceabilitiesRequest {
  Address source;
  Address destination;
}
