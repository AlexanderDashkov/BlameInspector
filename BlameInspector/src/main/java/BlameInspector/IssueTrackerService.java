package BlameInspector;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class IssueTrackerService {

    protected String userName;
    protected String repositoryName;
    protected String repositoryOwner;
    protected String password;
    protected int numberOfTickets;

    protected IssueTrackerService(final String userName, final String password,
                                  final String repositoryOwner, final String repositoryName){
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.userName = userName;
        this.password = password;
    }

    public abstract String getIssueBody(int issueNumber) throws IOException, JSONException;
    public abstract void setIssueAssignee(String blameLogin) throws IOException, JSONException;

    protected static String getRequest(final String url, final String auth) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        if (auth != null){
            con.setRequestProperty("Authorization", auth);
        }
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    protected static void putRequest(final String url, final String data, final String auth) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection httpCon = (HttpURLConnection) obj.openConnection();

        if (auth != null){
            httpCon.setRequestProperty("Authorization", auth);
        }
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.setRequestProperty("Content-Type", "application/json");
        httpCon.setRequestProperty("Accept", "application/json");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
        out.write(data);
        out.flush();
        out.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpCon.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    public abstract void refresh();

    public int getNumberOfTickets() {
        return numberOfTickets;
    }
}
