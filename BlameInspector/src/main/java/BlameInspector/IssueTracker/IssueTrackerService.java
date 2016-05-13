package blameinspector.issuetracker;

import blameinspector.vcs.BlamedUserInfo;
import blameinspector.vcs.VersionControlServiceException;
import org.json.JSONException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;

public abstract class IssueTrackerService implements Serializable {

    protected String userName;
    protected String repositoryName;
    protected String repositoryOwner;
    protected String password;
    protected int numberOfTickets;

    protected static String ISSUE_URL;
    protected static String ASSIGNEE_URL;

    protected IssueTrackerService(final String userName, final String password,
                                  final String repositoryOwner, final String repositoryName) {
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.userName = userName;
        this.password = password;
    }

    public abstract String assignee(final int number) throws IOException;

    public abstract String getIssueBody(int issueNumber) throws IOException, JSONException;

    public abstract void setIssueAssignee(final String blameLogin) throws IOException, JSONException;

    public abstract String getUserLogin(final BlamedUserInfo blamedUserInfo)
            throws IOException, JSONException, VersionControlServiceException, IssueTrackerException;

    public ArrayList<String> assigneeUrl(ArrayList<String> userNames) {
        ArrayList<String> assigneeUrl = new ArrayList<>();
        for (String assignee : userNames) {
            assigneeUrl.add(MessageFormat.format(ASSIGNEE_URL, assignee));
        }
        return assigneeUrl;
    }

    public String ticketUrl(int issueNumber) {
        return ISSUE_URL + String.valueOf(issueNumber);
    }


    protected static String getRequest(final String url, final String auth) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        if (auth != null) {
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

        if (auth != null) {
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
