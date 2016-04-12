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
import java.io.IOException;
import java.io.StringReader;

public class YouTrackService extends IssueTrackerService {

    private String itsUrl;
    private int issueNumber;

    public YouTrackService(final String username, final String password,
                           final String repoOwner, final String projectName,
                           final String itsUrl) {
        super(username, password, repoOwner, projectName);
        this.itsUrl = itsUrl;
    }

    @Override
    public String getIssueBody(int issueNumber) throws IOException, JSONException {
        this.issueNumber = issueNumber;
        String result = getRequest(itsUrl + "-" + issueNumber, null);
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
        Element element = (Element) doc.getElementsByTagName("issue").item(0);
        for (int i = 0 ; i < element.getElementsByTagName("field").getLength(); i++ ){
            Element e = (Element) element.getElementsByTagName("field").item(i);
            if(e.getAttribute("name").equals("description")){
                issueBody = e.getTextContent().trim();
                return issueBody;
            }
        }
        throw new NoSuchFieldError("no field with description!");
    }

    @Override
    public void setIssueAssignee(String blameLogin) throws IOException, JSONException {
        String command  = "Assignee " + blameLogin;
        String url = itsUrl + "-" + issueNumber + "/execute?" + command;
        //putRequest(url, "", null);
    }

    @Override
    public String getUserLogin(VersionControlService vcs, String file, int number) throws IOException, JSONException, VersionControlServiceException, IssueTrackerException {
        return null;
    }

    @Override
    public void refresh() {

    }
}
