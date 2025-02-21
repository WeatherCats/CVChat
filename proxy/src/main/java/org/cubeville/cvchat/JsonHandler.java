package org.cubeville.cvchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class JsonHandler {

    public JsonHandler() {

    }

    public LinkedHashMap<String, String> queryIP(String ip) {
        String urlString = "http://ip-api.com/json/" + ip + "?fields=message,country,regionName,city,zip,timezone,isp,org,as,mobile,proxy,hosting,query";
        StringBuilder response = null;
        LinkedHashMap<String, String> out = new LinkedHashMap<>();
        try {
            URL url = new URL(urlString);
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                int responsecode = conn.getResponseCode();
                if(responsecode != 200) throw new RuntimeException("HttpResponseCode: " + responsecode);
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                response = new StringBuilder();
                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } catch(IOException e) {

            }
        } catch (MalformedURLException e) {

        }
        if(response != null) {
            String resp = String.valueOf(response);
            resp = resp.replace("{", "");
            resp = resp.replace("}", "");
            while(resp.contains(":")) {
                int middle = resp.indexOf("\"", 1) + 1;
                int end;
                String a = resp.substring(1, middle - 1);
                boolean isBoolean = a.equalsIgnoreCase("mobile") || a.equalsIgnoreCase("proxy") || a.equalsIgnoreCase("hosting");
                String b;
                if(isBoolean) {
                    b = resp.substring(middle + 1, resp.indexOf(","));
                    end = resp.indexOf(",");
                } else {
                    b = resp.substring(middle + 2, resp.indexOf("\"", middle + 2));
                    end = resp.indexOf("\"", middle + 2) + 1;
                }
                if(resp.length() <= end + 1) break;
                resp = resp.substring(end + 1);
                out.put(a, b);
            }
        }
        return out;
    }
}
