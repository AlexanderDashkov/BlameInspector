package blameinspector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

public class PropertyService {

    private static final String PROJECT_TAG = "project";
    private static final String PROJECT_NAME_TAG = "projectName";
    private static final String NAME_ATTR = "name";
    private static final String USER_NAME_TAG = "userName";
    private static final String PASSWORD_TAG = "password";
    private static final String PATH_TO_REPO_TAG = "pathToRepo";
    private static final String ISSUE_TRACKER_TAG = "issueTracker";
    private static final String VERSION_CONTROL_TAG = "vcs";

    private static final String XML_SCHEMA = "projects.xsd";
    private static final String SCHEMA_FACTORY_W3 = "http://www.w3.org/2001/XMLSchema";

    private String projectName;
    private String userName;
    private String password;
    private String pathToRepo;
    private String issueTracker;
    private String versionControl;



    public PropertyService(final String projectName, final String configFileName) throws PropertyServiceException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            SchemaFactory schemaFactory =
                    SchemaFactory.newInstance(SCHEMA_FACTORY_W3);

//      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
//      dbf.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(classloader.getResourceAsStream(XML_SCHEMA))}));

            dbf.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(XML_SCHEMA)}));

            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(new SimpleErrorHandler());


            Document doc = db.parse(new InputSource(configFileName));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName(PROJECT_TAG);
            boolean found = false;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                if (element.getAttribute(NAME_ATTR).equals(projectName)) {
                    versionControl = getContentByTag(element, VERSION_CONTROL_TAG);
                    userName = getContentByTag(element, USER_NAME_TAG);
                    password = getContentByTag(element, PASSWORD_TAG);
                    pathToRepo = getContentByTag(element, PATH_TO_REPO_TAG);
                    issueTracker = getContentByTag(element, ISSUE_TRACKER_TAG);
                    this.projectName = getContentByTag(element, PROJECT_NAME_TAG);
                    found = true;
                }
            }
            if (!found) {
                throw new PropertyServiceException("Project with such name wasn't found in file.");
            }
        } catch (PropertyServiceException e){
            throw e;
        } catch (Exception e){
            throw new PropertyServiceException(e);
        }

    }

    private String getContentByTag(final Element element, final String tag) throws PropertyServiceException {
        try {
            return element.getElementsByTagName(tag).item(0).getTextContent().trim();
        }catch (NullPointerException e){
            throw new PropertyServiceException("No value in field!");
        }
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

    public class SimpleErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }

        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
}