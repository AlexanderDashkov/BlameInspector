package BlameInspector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;

public class PropertyService {

    private static final String PROJECT_TAG = "project";
    private static final String NAME_ATTR = "name";
    private static final String USER_NAME_TAG = "userName";
    private static final String PASSWORD_TAG = "password";
    private static final String PATH_TO_REPO_TAG = "pathToRepo";
    private static final String ISSUE_TRACKER_TAG = "issueTracker";
    private static final String VERSION_CONTROL_TAG = "vcs";

    private static final String XML_SCHEMA = "projects.xsd";
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String SCHEMA_FACTORY_W3 = "http://www.w3.org/2001/XMLSchema";

    private String projectName;
    private String userName;
    private String password;
    private String pathToRepo;
    private String issueTracker;
    private String versionControl;



    public PropertyService(final String projectName) throws IOException, ProjectNotFoundException, SAXException {
        this.projectName = projectName;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        SchemaFactory schemaFactory =
                SchemaFactory.newInstance(SCHEMA_FACTORY_W3);

        dbf.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(XML_SCHEMA)}));

        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            db.setErrorHandler(new SimpleErrorHandler());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document doc = null;
        try {
            doc = db.parse(new InputSource(CONFIG_FILE_NAME));
            doc.getDocumentElement().normalize();

        } catch (SAXException e) {
            e.printStackTrace();
        }

        NodeList nodeList = doc.getElementsByTagName(PROJECT_TAG);
        for (int i = 0; i < nodeList.getLength(); i++){
             Element element = (Element) nodeList.item(i);
             if(element.getAttribute(NAME_ATTR).equals(projectName)){
                 versionControl = getContentByTag(element, VERSION_CONTROL_TAG);
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
        return element.getElementsByTagName(tag).item(0).getTextContent().trim();
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

    public String getVersionControl() {
        return versionControl;
    }
}