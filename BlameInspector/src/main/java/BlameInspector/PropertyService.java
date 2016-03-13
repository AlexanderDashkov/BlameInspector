package BlameInspector;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyService {

    private static final String PROJECT_NAME = "projectName";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "password";
    private static final String PATH_TO_REPO = "pathToRepo";
    private static final String ISSUE_TRACKER = "issueTracker";

    private static final String CONFIG_FILE_NAME = "config.properties";
    private Properties properties;

    public PropertyService(){
        properties = new Properties();
    }

    public void setProjectName(final String projectName){
        properties.setProperty(PROJECT_NAME, projectName);
    }

    public String getProjectName(){
        return properties.getProperty(PROJECT_NAME);
    }

    public void setUserName(final String userName){
        properties.setProperty(USER_NAME, userName);
    }

    public String getUserName(){
        return properties.getProperty(USER_NAME);
    }

    public void setPassword(final String password){
        properties.setProperty(PASSWORD, password);
    }

    public String getPassword(){
        return properties.getProperty(PASSWORD);
    }

    public void setPathToRepo(final String pathToRepo){
        properties.setProperty(PATH_TO_REPO, pathToRepo);
    }

    public String getPathToRepo(){
        return properties.getProperty(PATH_TO_REPO);
    }

    public void setIssueTracker(final String issueTracker){
        properties.setProperty(ISSUE_TRACKER, issueTracker);
    }

    public String getIssueTracker(){
        return properties.getProperty(ISSUE_TRACKER);
    }


    public void writeInFile() throws IOException{
        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = new FileOutputStream(new File(CONFIG_FILE_NAME));
            properties.storeToXML(fileOutputStream, null);
        }catch (IOException e){
            throw e;
        }

    }

    public void readFromFile() throws IOException {
        FileInputStream fileInputStream;

        try {
            fileInputStream = new FileInputStream(CONFIG_FILE_NAME);
            properties.loadFromXML(fileInputStream);
        } catch (IOException e){
            throw e;
        }
    }

}
