package LabUrl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class URLReader {
    public static void main(String[] args) throws Exception {
        URL siteURL = new URL("http://www.google.com/");
        URLConnection connection = siteURL.openConnection();
        
        Map<String, List<String>> headers = connection.getHeaderFields();
        Set<Map.Entry<String, List<String>>> entries = headers.entrySet();

        for (Map.Entry<String, List<String>> entry : entries) {
            String headerName = entry.getKey();
            // A null name represents the HTTP status line.
            if (headerName != null) System.out.print(headerName + ":");
            for (String value : entry.getValue()) System.out.print(value);
            System.out.println();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                System.out.println(inputLine);
            }
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}