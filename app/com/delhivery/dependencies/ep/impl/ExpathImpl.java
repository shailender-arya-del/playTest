package com.delhivery.dependencies.ep.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import models.Address;
import play.Configuration;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import com.delhivery.constants.ProductCategory;
import com.delhivery.constants.ServiceCategory;
import com.delhivery.constants.Tat;
import com.delhivery.dependencies.ep.Expath;
import com.delhivery.dependencies.ep.RequestResponse.ExpathRequest;
import com.delhivery.dependencies.ep.RequestResponse.ExpathResponse;
import com.delhivery.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;

@Singleton
public class ExpathImpl implements Expath {

  private Configuration configuration;
  private final WSClient ws;
  private final String END_POINT ;//= configuration.getString("Expath.url");
  private final Integer PORT;

  @Inject
  public ExpathImpl(WSClient ws, Configuration configuration){
    this.configuration = configuration;
    this.ws = ws;
    END_POINT = configuration.getString("Expath.url");
    PORT = (Integer) configuration.getNumber("Expath.port");
  }
  public CompletableFuture<ExpathResponse> getPricePerTimeslots(
          ExpathRequest req) {

    System.out.println("endPoint ==" + END_POINT + " port = " + PORT);
    WSRequest wsRequest = ws
            .url("http://" + END_POINT + ":" + PORT + "/expath")
            .setRequestTimeout(50)
            .setHeader("Content-Type", "application/json");
    return wsRequest.post(Utils.Object2Json(req))
            .thenApply(ExpathImpl::parseEPResponse).exceptionally(e -> {
              ExpathResponse resp = new ExpathResponse();
              return resp;
            }).toCompletableFuture();

  }

  private static ExpathResponse parseEPResponse(WSResponse wsResp) {
    ExpathResponse exResp = null;
    if (!Utils.isStatusOK(wsResp.getStatus())) {
      System.out.println("Bad Response From EP");
      throw new RuntimeException("Bad response from EP");
    }
    try {
      JsonNode json =  wsResp.asJson();
      System.out.println("Json Received = " + json);
      exResp = Utils.json2Object(ExpathResponse.class,json);
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Exception while converting to ");
    }
    return exResp;
  }

  public List<CompletableFuture<ExpathResponse>> getAllServices(String origin,
          String dest) {
    List<CompletableFuture<ExpathResponse>> allServices = new ArrayList<>();

    for (ProductCategory prodCat : ProductCategory.values()) {
      for (ServiceCategory serviceCat : ServiceCategory.values()) {
        for (Tat tat : Tat.values()) {
          ExpathRequest req = getRequest(origin, dest, prodCat, serviceCat, tat);
          allServices.add(getPricePerTimeslots(req));
        }
      }
    }
    return allServices;

  }

  private ExpathRequest getRequest(String origin, String dest,
          ProductCategory prodCat, ServiceCategory serviceCat, Tat tat) {
    ExpathRequest req = new ExpathRequest();
    req.setOriginCode(origin);
    req.setStartTime("0");
    req.setEndTime("24");
    req.setDestCode(dest);
    req.setProductType(prodCat.toString());
    req.setServiceType(serviceCat.toString());
    req.setTat(tat.value());
    return req;
  }

}
