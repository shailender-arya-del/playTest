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

import com.delhivery.constants.ProductCategories;
import com.delhivery.constants.ServiceCategories;
import com.delhivery.constants.Tat;
import com.delhivery.dependencies.ep.Expath;
import com.delhivery.dependencies.ep.RequestResponse.ExpathRequest;
import com.delhivery.dependencies.ep.RequestResponse.ExpathResponse;
import com.delhivery.utils.Utils;

public class ExpathImpl implements Expath {

  @Inject
  private static Configuration configuration;
  @Inject
  private WSClient ws;
  private static final String END_POINT = configuration.getString("Expath.url");
  private static final String PORT = configuration.getString("Expath.port");

  public CompletableFuture<ExpathResponse> getPricePerTimeslots(
          ExpathRequest req) {
    WSRequest wsRequest = ws.url(END_POINT + ":" + PORT).setRequestTimeout(50)
            .setHeader("Content-Type", "application/json");
    return wsRequest.post(Utils.Object2Json(req))
            .thenApply(ExpathImpl::parseEPResponse).exceptionally(e -> {
              ExpathResponse resp = new ExpathResponse();
              return resp;
            }).toCompletableFuture();

  }

  private static ExpathResponse parseEPResponse(WSResponse wsResp) {
    ExpathResponse exResp = null;
    if (!Utils.isStatusOK(wsResp.getStatus())) { throw new RuntimeException(
            "Bad response from EP"); }
    try {
      exResp = Utils.json2Object(ExpathResponse.class, wsResp.asJson());
    } catch (IOException e) {
      System.out.println("Exception while converting to ");
    }
    return exResp;
  }

  public List<CompletableFuture<ExpathResponse>> getAllServices(String origin,
          String dest) {
    List<CompletableFuture<ExpathResponse>> allServices = new ArrayList<>();

    for (ProductCategories prodCat : ProductCategories.values()) {
      for (ServiceCategories serviceCat : ServiceCategories.values()) {
        for (Tat tat : Tat.values()) {
          ExpathRequest req = getRequest(origin, dest, prodCat, serviceCat, tat);
          allServices.add(getPricePerTimeslots(req));
        }
      }
    }
    return allServices;

  }

  private ExpathRequest getRequest(String origin, String dest,
          ProductCategories prodCat, ServiceCategories serviceCat, Tat tat) {
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
