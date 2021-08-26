package bpmn.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import com.google.gson.Gson;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.FlowNode;

class ResponseObject {
  public String id;
  public String bpmn20Xml;
}

public class App {
  private static String url = "https://n35ro2ic4d.execute-api.eu-central-1.amazonaws.com/prod/engine-rest/process-definition/key/invoice/xml";

  public static void main(String[] args) throws IOException, InterruptedException {
    String startID = args[0];
    String endID = args[1];
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    Gson gson = new Gson();
    ResponseObject obj = gson.fromJson(response.body(), ResponseObject.class);
    InputStream stream = new ByteArrayInputStream(obj.bpmn20Xml.getBytes(Charset.forName("UTF-8")));
    BpmnModelInstance modelInstance = Bpmn.readModelFromStream(stream);
    FlowNode startNode = (FlowNode) modelInstance.getModelElementById(startID);
    List<String> path = new ArrayList<>();
    Map<String, Boolean> visited = new HashMap<>();
    Map<String, String> backtracking = new HashMap<>();
    Queue<FlowNode> bfsQ = new ArrayDeque<>();
    visited.put(startID, true);
    bfsQ.add(startNode);
    while(!bfsQ.isEmpty()) {
      FlowNode node = bfsQ.remove();
      if (node.getId().equals(endID)) {
        String tempStartID = "";
        String tempEndID = endID;
        path.add(endID);
        while (!tempStartID.equals(startID)) {
          tempStartID = backtracking.get(tempEndID);
          path.add(0, tempStartID);
          tempEndID = tempStartID;
        }
        System.out.printf("The path from %s to %s is:\n", startID, endID);
        System.out.println(path);
        return;
      }
      Collection<SequenceFlow> edges = node.getOutgoing();
      for (SequenceFlow edge : edges) {
        FlowNode adjacentNode = edge.getTarget();
        backtracking.put(adjacentNode.getId(), node.getId());
        if (visited.get(adjacentNode.getId()) == null || !visited.get(adjacentNode.getId())) {
          visited.put(adjacentNode.getId(), true);
          bfsQ.add(adjacentNode);
        }
      }
    }
    System.out.println(-1);
    System.exit(-1);
  }
}
