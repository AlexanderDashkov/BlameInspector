package BlameInspector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class PropertyService {

    private static final String PROJECT_TAG = "project";
    private static final String NAME_ATTR = "name";
    private static final String USER_NAME_TAG = "userName";
    private static final String PASSWORD_TAG = "password";
    private static final String PATH_TO_REPO_TAG = "pathToRepo";
    private static final String ISSUE_TRACKER_TAG = "issueTracker";

    private String projectName;
    private String userName;
    private String password;
    private String pathToRepo;
    private String issueTracker;

    private static final String CONFIG_FILE_NAME = "config.properties";

    public PropertyService(final String projectName) throws IOException, ProjectNotFoundException {
        this.projectName = projectName;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = db.parse(new File(CONFIG_FILE_NAME));
            doc.getDocumentElement().normalize();

        } catch (SAXException e) {
            e.printStackTrace();
        }
        NodeList nodeList = doc.getElementsByTagName(PROJECT_TAG);
        for (int i = 0; i < nodeList.getLength(); i++){
             Element element = (Element) nodeList.item(i);
             if(element.getAttribute(NAME_ATTR).equals(projectName)){
                 userName = getContentByTag(element, USER_NAME_TAG);
                 password = getContentByTag(element, PASSWORD_TAG);
                 pathToRepo = getContentByTag(element, PATH_TO_REPO_TAG);
                 issueTracker = getContentByTag(element, ISSUE_TRACKER_TAG);
             }
        }
        if (userName == null){
            throw new ProjectNotFoundException();
        }
    }

    private String getContentByTag(final Element element, final String tag){
        return element.getElementsByTagName(tag).item(0).getTextContent();
    }

    public String getProjectName(){
        return projectName;
    }

    public String getUserName(){
        return userName;
    }

    public String getPassword(){
        return password;
    }

    public String getPathToRepo(){
        return pathToRepo;
    }

    public String getIssueTracker(){
        return issueTracker;
    }

}