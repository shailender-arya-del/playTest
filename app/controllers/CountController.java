package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.Address;
import models.Serviceability;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;
import services.Counter;

import com.delhivery.Requests.ServiceabilitiesRequest;
import com.delhivery.Response.ServiceabilityResponse;
import com.delhivery.constants.ServiceCategory;
import com.delhivery.constants.Tat;
import com.delhivery.dependencies.ep.RequestResponse.ExpathResponse;
import com.delhivery.dependencies.ep.RequestResponse.TimeSlotWithPrice;
import com.delhivery.dependencies.ep.impl.ExpathImpl;
import com.delhivery.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This controller demonstrates how to use dependency injection to bind a
 * component into a controller class. The class contains an action that shows an
 * incrementing count to users. The {@link Counter} object is injected by the
 * Guice dependency injection system.
 */
@Singleton
public class CountController extends Controller {

  private final Counter counter;

  @Inject
  WSClient ws;
  @Inject 
  ExpathImpl expathImpl;

  @Inject
  public CountController(Counter counter) {
    this.counter = counter;
  }

  /**
   * An action that responds with the {@link Counter}'s current count. The
   * result is plain text. This action is mapped to <code>GET</code> requests
   * with a path of <code>/count</code> requests by an entry in the
   * <code>routes</code> config file.
   */
  public Result count() {
    return ok(Integer.toString(counter.nextCount()));
  }

  public CompletionStage<Result> getServiceability() throws IOException {
    JsonNode input = request().body().asJson();
    ServiceabilitiesRequest req = Utils.json2Object(
            ServiceabilitiesRequest.class, input);

    ExecutorService es = Executors.newWorkStealingPool();

    return CompletableFuture.supplyAsync(() -> {
      return getServiceabilityInt(req);
    }, es);

  }

  private Result getServiceabilityInt(ServiceabilitiesRequest req)  {
    String originCode = getDelhiveryCenterCodeByAddr(req.getSource());
    String destCode = getDelhiveryCenterCodeByAddr(req.getSource());
    System.out.println("expathImpl ==" + expathImpl);
    List<CompletableFuture<ExpathResponse>> serviceabilities = expathImpl
            .getAllServices(originCode, destCode);
    try {
      CompletableFuture.allOf(
              serviceabilities.toArray(new CompletableFuture[serviceabilities
                      .size()])).join();
    } catch (CompletionException e) {
      System.out.println("Caught Completion Exception" + e);
    }
    
    return ok(Utils.Object2Json(getResponse(serviceabilities)));

  }

  private ServiceabilityResponse getResponse(List<CompletableFuture<ExpathResponse>> futures) {
    ServiceabilityResponse resp = new ServiceabilityResponse();
    List<ExpathResponse> expathResponses = new ArrayList<>(futures.size());

    for( CompletableFuture<ExpathResponse> compExpath: futures){
      try {
        expathResponses.add(compExpath.get());
      } catch (InterruptedException | ExecutionException e1) {
        e1.printStackTrace();
      }
    }
    Map<ServiceCategory, List< TimeSlotWithPrice>> sdd = new HashMap<>();
    Serviceability sddServc = new Serviceability();
    expathResponses.stream().filter(e-> Tat.ONE_DAY.value().equals(e.getTat()))
              .forEach(e -> { 
                              System.out.println("filtered output");
                              sddServc.setExist(true);
                              ServiceCategory scat = ServiceCategory.valueOf(e.getServiceType());
                              List<TimeSlotWithPrice> tsps = Arrays.asList(e.getPriceTimeSlots());
                              sdd.put(scat, tsps);
                             });
    resp.setSdd(sddServc);
    return resp;
  }

  private String getDelhiveryCenterCodeByAddr(Address source) {
    return source.getLocality();
  }

}
