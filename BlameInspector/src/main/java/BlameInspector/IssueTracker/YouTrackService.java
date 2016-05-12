package blameinspector.issuetracker;

import blameinspector.vcs.BlamedUserInfo;
import blameinspector.vcs.VersionControlServiceException;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class YouTrackService extends IssueTrackerService {

    private static final String REST_API = "rest";
    private static final String ISSUE = "issue";
    private static final String DASH = "-";
    private static final String SLASH = "/";

    private static final String FIELD_TAG = "field";
    private static final String DESCR_VALUE = "description";
    private static final String ASSGN_VALUE = "Assignee";
    private static final String NAME_ATTR = "name";
    private static final String VAL_TAG = "value";

    private String itsUrl;
    private int issueNumber;

    public YouTrackService(final String username, final String password,
                           final String repoOwner, final String projectName,
                           final String itsUrl) {
        super(username, password, repoOwner, projectName);
        ASSIGNEE_URL = "";
        ISSUE_URL = "https://youtrack.jetbrains.com/issue/" + projectName + "-";
        String[] urlParam = itsUrl.split(SLASH);
        this.itsUrl = urlParam[0] + SLASH + SLASH + urlParam[2] + SLASH + REST_API + SLASH + ISSUE + SLASH + projectName;
    }

    @Override
    public String getIssueBody(final int issueNumber) throws IOException, JSONException {
        this.issueNumber = issueNumber;
        String result = getRequest(itsUrl + DASH + issueNumber, null);
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        }
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(result));

        Document doc = null;
        try {
            doc = db.parse(is);
        } catch (SAXException e) {
            throw new IOException(e);
        }

        doc.getDocumentElement().normalize();

        String issueBody = "";
        Element element = (Element) doc.getElementsByTagName(ISSUE).item(0);
        for (int i = 0; i < element.getElementsByTagName(FIELD_TAG).getLength(); i++) {
            Element e = (Element) element.getElementsByTagName(FIELD_TAG).item(i);
            if (e.getAttribute(NAME_ATTR).equals(DESCR_VALUE)) {
                issueBody = e.getTextContent().trim();
                return issueBody;
            }
        }
        return "";
    }

    @Override
    public void setIssueAssignee(final String blameLogin) throws IOException, JSONException {
        String command = "command=Assignee " + blameLogin;
        String url = itsUrl + "-" + issueNumber + "/execute?" + command;
        putRestRequest(url, null);
    }


    private void putRestRequest(final String url, final String auth) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection httpCon = (HttpURLConnection) obj.openConnection();

        if (auth != null) {
            httpCon.setRequestProperty("Authorization", auth);
        }
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpCon.setRequestProperty("Accept", "application/xml;charset=UTF-8");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpCon.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }

    @Override
    public String getUserLogin(final BlamedUserInfo blamedUserInfo) throws IOException, JSONException, VersionControlServiceException, IssueTrackerException {
        try {
            if (blamedUserInfo.getUserEmail() != null) {
                return blamedUserInfo.getUserEmail();
            } else if (blamedUserInfo.getUserName() != null) {
                return blamedUserInfo.getUserName();
            }
            throw new Exception();
        } catch (Exception e) {
            throw new IssueTrackerException(true, "Can not get blame!");
        }
    }

    public String assignee(final int issueNumber) throws IOException {
        String result = getRequest(itsUrl + DASH + issueNumber, null);
        String path = itsUrl + DASH + issueNumber;
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        }
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(result));

        Document doc = null;
        try {
            doc = db.parse(is);
        } catch (SAXException e) {
            throw new IOException(e);
        }

        doc.getDocumentElement().normalize();

        String assignee = "";
        Element element = (Element) doc.getElementsByTagName(ISSUE).item(0);
        for (int i = 0; i < element.getElementsByTagName(FIELD_TAG).getLength(); i++) {
            Element e = (Element) element.getElementsByTagName(FIELD_TAG).item(i);
            if (e.getAttribute(NAME_ATTR).equals(ASSGN_VALUE)) {
                assignee = e.getElementsByTagName(VAL_TAG).item(0).getTextContent().trim();
                //System.out.println("assignee :" + assignee);
                return assignee;
            }
        }
        return null;
    }

    @Override
    public void refresh() {
        issueNumber = -1;
    }
}
