package gpsystem;

import fi.iki.elonen.NanoHTTPD;
import java.util.Map;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class detection_server extends NanoHTTPD {

    public detection_server() throws Exception {
        super(5000); // Start server on port 5000
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server is running on http://localhost:5000/");
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (Method.GET.equals(session.getMethod())) {
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "Server is up and running.");
        }

        if (Method.POST.equals(session.getMethod())) {
            try {
                // Parse POST data
                Map<String, String> postData = session.getParms();
                session.parseBody(postData);

                String receivedData = postData.get("postData");
                System.out.println("Received data: " + receivedData);

                // Parse JSON to extract coordinates
                JsonObject jsonObject = JsonParser.parseString(receivedData).getAsJsonObject();
                JsonObject coordinates = jsonObject.getAsJsonObject("coordinates");

                int xmin = coordinates.get("xmin").getAsInt();
                int ymin = coordinates.get("ymin").getAsInt();
                int xmax = coordinates.get("xmax").getAsInt();
                int ymax = coordinates.get("ymax").getAsInt();
                String type = jsonObject.get("type").getAsString();

                // Assuming the screengrab path is provided dynamically
                String screengrabPath = "captures/screengrab.jpg"; // Replace with the actual path
                String outputPath = "processed/cropped_" + System.currentTimeMillis() + ".jpg";

                // Forward data to image_preprocess
                image_preprocess.preprocessAndSave(screengrabPath, outputPath, xmin, ymin, xmax, ymax);

                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"success\"}");
            } catch (Exception e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error processing request.");
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }

    public void stop() {
        super.stop();
        System.out.println("Detection Server stopped gracefully.");
    }
    
    public static void main(String[] args) {
        try {
            new detection_server();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
