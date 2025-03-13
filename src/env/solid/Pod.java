package solid;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

/**
 * A CArtAgO artifact that agent can use to interact with LDP containers in a Solid pod.
 */
public class Pod extends Artifact {

    private String podURL; // the location of the Solid pod 

  /**
   * Method called by CArtAgO to initialize the artifact. 
   *
   * @param podURL The location of a Solid pod
   */
    public void init(String podURL) {
        this.podURL = podURL;
        log("Pod artifact initialized for: " + this.podURL);
    }

  /**
   * CArtAgO operation for creating a Linked Data Platform container in the Solid pod
   *
   * @param containerName The name of the container to be created
   * 
   */
    @OPERATION
    public void createContainer(String containerName) {
        try {
            URL url = new URL(podURL + "/" + containerName + "/");
            log(url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "text/turtle");
            connection.setDoOutput(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                log("Container created successfully: " + containerName);
            } else {
                log("Failed to create container: " + containerName + ". Response code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            log("Error creating container: " + e.getMessage());
        }
    }

  /**
   * CArtAgO operation for publishing data within a .txt file in a Linked Data Platform container of the Solid pod
   * 
   * @param containerName The name of the container where the .txt file resource will be created
   * @param fileName The name of the .txt file resource to be created in the container
   * @param data An array of Object data that will be stored in the .txt file
   */
    @OPERATION
    public void publishData(String containerName, String fileName, Object[] data) {
        try {
            URL url = new URL(podURL + "/" + containerName + "/" + fileName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            connection.setDoOutput(true);

            String dataString = createStringFromArray(data);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(dataString.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                log("Data published successfully to: " + fileName);
            } else {
                log("Failed to publish data to: " + fileName + ". Response code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            log("Error publishing data: " + e.getMessage());
        }
    }

  /**
   * CArtAgO operation for reading data of a .txt file in a Linked Data Platform container of the Solid pod
   * 
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be read
   * @param data An array whose elements are the data read from the .txt file
   */
    @OPERATION
    public void readData(String containerName, String fileName, OpFeedbackParam<Object[]> data) {
        data.set(readData(containerName, fileName));
    }

  /**
   * Method for reading data of a .txt file in a Linked Data Platform container of the Solid pod
   * 
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be read
   * @return An array whose elements are the data read from the .txt file
   */
    public Object[] readData(String containerName, String fileName) {
        try {
            URL url = new URL(podURL + "/" + containerName + "/" + fileName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/plain");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream is = connection.getInputStream();
                     Scanner scanner = new Scanner(is)) {
                    scanner.useDelimiter("\\A");
                    String dataString = scanner.hasNext() ? scanner.next() : "";
                    return createArrayFromString(dataString);
                }
            } else {
                log("Failed to read data from: " + fileName + ". Response code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException e) {
            log("Error reading data: " + e.getMessage());
        }
        return new Object[0];
    }

  /**
   * Method that converts an array of Object instances to a string, 
   * e.g. the array ["one", 2, true] is converted to the string "one\n2\ntrue\n"
   *
   * @param array The array to be converted to a string
   * @return A string consisting of the string values of the array elements separated by "\n"
   */
    public static String createStringFromArray(Object[] array) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : array) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }

  /**
   * Method that converts a string to an array of Object instances computed by splitting the given string with delimiter "\n"
   * e.g. the string "one\n2\ntrue\n" is converted to the array ["one", "2", "true"]
   *
   * @param str The string to be converted to an array
   * @return An array consisting of string values that occur by splitting the string around "\n"
   */
    public static Object[] createArrayFromString(String str) {
        return str.split("\n");
    }


  /**
   * CArtAgO operation for updating data of a .txt file in a Linked Data Platform container of the Solid pod
   * The method reads the data currently stored in the .txt file and publishes in the file the old data along with new data 
   * 
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be updated
   * @param data An array whose elements are the new data to be added in the .txt file
   */
    @OPERATION
    public void updateData(String containerName, String fileName, Object[] data) {
        Object[] oldData = readData(containerName, fileName);
        Object[] allData = new Object[oldData.length + data.length];
        System.arraycopy(oldData, 0, allData, 0, oldData.length);
        System.arraycopy(data, 0, allData, oldData.length, data.length);
        publishData(containerName, fileName, allData);
    }
}
