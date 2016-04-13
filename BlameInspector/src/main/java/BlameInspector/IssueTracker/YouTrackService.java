package BlameInspector.IssueTracker;

import BlameInspector.VCS.VersionControlService;
import BlameInspector.VCS.VersionControlServiceException;
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
    private static final String NAME_ATTR = "name";

    private String itsUrl;
    private int issueNumber;

    public YouTrackService(final String username, final String password,
                           final String repoOwner, final String projectName,
                           final String itsUrl) {
        super(username, password, repoOwner, projectName);
        String urlParam[] = itsUrl.split(SLASH);
        this.itsUrl = urlParam[0] + SLASH + SLASH + urlParam[2] + SLASH + REST_API + SLASH + ISSUE + SLASH + projectName;
    }

    @Override
    public String getIssueBody(int issueNumber) throws IOException, JSONException {
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
        for (int i = 0 ; i < element.getElementsByTagName(FIELD_TAG).getLength(); i++ ){
            Element e = (Element) element.getElementsByTagName(FIELD_TAG).item(i);
            if(e.getAttribute(NAME_ATTR).equals(DESCR_VALUE)){
                issueBody = e.getTextContent().trim();
                return issueBody;
            }
        }
        throw new NoSuchFieldError("no field with description!");
    }

    @Override
    public void setIssueAssignee(String blameLogin) throws IOException, JSONException {
        String command  = "command=Assignee " + blameLogin;
        String url = itsUrl + "-" + issueNumber + "/execute?" + command;
        putRestRequest(url, null);
    }

    private void putRestRequest(final String url, final String auth) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection httpCon = (HttpURLConnection) obj.openConnection();

        if (auth != null){
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
    public String getUserLogin(VersionControlService vcs, String file, int number) throws IOException, JSONException, VersionControlServiceException, IssueTrackerException {
        return null;
    }

    @Override
    public void refresh() {

    }
}
