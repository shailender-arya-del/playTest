package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;


import play.Configuration;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.ExecutionContextExecutor;
import akka.actor.ActorSystem;
import akka.actor.Scheduler;

import com.delhivery.cache.ECMemCache;
import com.delhivery.dependencies.ep.RequestResponse.ExpathRequest;
import com.delhivery.dependencies.ep.RequestResponse.ExpathResponse;
import com.delhivery.dependencies.ep.RequestResponse.TimeSlotWithPrice;
import com.delhivery.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * This controller contains an action that demonstrates how to write simple
 * asychronous code in a controller. It uses a timer to asynchronously delay
 * sending a response for 1 second.
 *
 * @param actorSystem
 *          We need the {@link ActorSystem}'s {@link Scheduler} to run code
 *          after a delay.
 * @param exec
 *          We need a Java {@link Executor} to apply the result of the
 *          {@link CompletableFuture} and a Scala {@link ExecutionContext} so we
 *          can use the Akka {@link Scheduler}. An
 *          {@link ExecutionContextExecutor} implements both interfaces.
 */
@Singleton
public class AsyncController extends Controller {

  private final ActorSystem actorSystem;
  private final ExecutionContextExecutor exec;
  @Inject
  private WSClient ws;
  @Inject
  private Configuration configuration;

  // String asdf = Play.application().configuration().getString("asdf") ;

  @Inject
  public AsyncController(ActorSystem actorSystem, ExecutionContextExecutor exec) {
    this.actorSystem = actorSystem;
    this.exec = exec;
  }

  public Result exPath() throws IOException {
    JsonNode input = request().body().asJson();
    ExpathRequest req = Utils.json2Object(ExpathRequest.class, input);
    ExpathResponse resp = new ExpathResponse();
    resp.setOriginCode(req.getOriginCode());
    resp.setDestCode(req.getDestCode());
    resp.setProductType(req.getProductType());
    resp.setServiceType(req.getServiceType());
    resp.setTat(req.getTat());
    List<TimeSlotWithPrice> timePriceSlots = new ArrayList<>();
    timePriceSlots.add(new TimeSlotWithPrice("0", "1", "4"));
    timePriceSlots.add(new TimeSlotWithPrice("3", "4", "5"));
    resp.setPriceTimeSlots(timePriceSlots.toArray(new TimeSlotWithPrice[2]));
//    System.out.println("Json of dummy data = " + Utils.Object2Json(resp));
    return ok(Utils.Object2Json(resp));
  }

  /**
   * An action that returns a plain text message after a delay of 1 second.
   *
   * The configuration in the <code>routes</code> file means that this method
   * will be called when the application receives a <code>GET</code> request
   * with a path of <code>/message</code>.
   */
  public CompletionStage<Result> message() {
    // return getFutureMessage(1, TimeUnit.SECONDS).thenApplyAsync(msg ->
    // ok(msg), exec);
    return getFutureMessage(1, TimeUnit.SECONDS).thenApply(rs -> ok(rs));
  }

  private CompletionStage<JsonNode> getFutureMessage(long time,
          TimeUnit timeUnit) {
    System.out.println("configuration = " + configuration.getNumber("asdf"));

    ExecutorService es = Executors.newWorkStealingPool();

    return CompletableFuture.supplyAsync(() -> {
      WSRequest wsRequest = ws.url("http://localhost:9000/count");
      wsRequest = wsRequest.setRequestTimeout(1000);
      System.out.println("after Generating Req");
      JsonNode js = null;
      try {
        js = wsRequest.get().thenApply(AsyncController::getJsonNode)
                .toCompletableFuture().get();
      } catch (Exception e) {
        // TODO Auto-generated catch block
            e.printStackTrace();
          }
          try {
            setAndGetToCache(js);
          } catch (Exception e) {

          }

          return js;
        }, es);

  }

  private static JsonNode getJsonNode(WSResponse ws) {
    return ws.asJson();
  }

  private void setAndGetToCache(JsonNode js) {
    ECMemCache cache = new ECMemCache();
    try {
      cache.set("test", js.toString(), 111);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      Object ob = cache.get("test");
      System.out.println("object received = " + ob);
      JsonNode out = Utils.Object2Json(ob);
      System.out.println("output = " + out);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("in = " + js);
  }
}
