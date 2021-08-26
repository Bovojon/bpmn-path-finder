package bpmn.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import com.google.gson.Gson;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;

class Foo {
  public String id;
  public String bpmn20Xml;
}

public class App {
  private static String url = "https://n35ro2ic4d.execute-api.eu-central-1.amazonaws.com/prod/engine-rest/process-definition/key/invoice/xml";

  public static void main(String[] args) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    Gson gson = new Gson();
    Foo obj = gson.fromJson(response.body(), Foo.class);
    InputStream stream = new ByteArrayInputStream(obj.bpmn20Xml.getBytes(Charset.forName("UTF-8")));
    BpmnModelInstance modelInstance = Bpmn.readModelFromStream(stream);
    FlowNode startNode = (FlowNode) modelInstance.getModelElementById("approveInvoice");
    System.out.println((startNode.getName()));
  }
}
