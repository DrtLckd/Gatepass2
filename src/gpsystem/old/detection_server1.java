package gpsystem.old;

import fi.iki.elonen.NanoHTTPD;
import java.util.Map;

public class detection_server1 extends NanoHTTPD {
    
    public detection_server1() throws Exception {
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
                Map<String, String> postData = session.getParms();
                session.parseBody(postData);
                System.out.println("Received data: " + postData.get("postData"));
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"success\"}");
            } catch (Exception e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error processing request.");
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }

    public static void main(String[] args) {
        try {
            new detection_server1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
    
}
