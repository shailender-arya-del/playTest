package controllers;

import java.util.concurrent.CompletionStage;

import javax.inject.*;

import com.delhivery.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.*;
import play.mvc.Http.RequestBody;
import services.Counter;

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

  public CompletionStage<Result> getServiceability() {
    RequestBody reqBody = request().body();
    reqBody.asJson();
    JsonNode json = request().body().asJson();

    ObjectNode output = Utils.newJsonObject();
    output.put("try", 1);

    System.out.println("received = JsonNode + {" + json + "");
    WSRequest wsRequest = ws.url("http://localhost:9000/count");
    wsRequest = wsRequest.setRequestTimeout(1000);
    CompletionStage<WSResponse> response = wsRequest.get();
    
    return  response.thenApply(resp ->{ return ok("Count Response = " + resp.asJson()); });
}


}
