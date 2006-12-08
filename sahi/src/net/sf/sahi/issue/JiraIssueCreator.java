package net.sf.sahi.issue;

import net.sf.sahi.util.FileNotFoundRuntimeException;
import net.sf.sahi.util.Utils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * User: dlewis
 * Date: Dec 4, 2006
 * Time: 5:11:59 PM
 */
public class JiraIssueCreator implements IssueCreator {
    private static Properties properties;

    private XmlRpcClient rpcClient;
    private String loginToken;

    public JiraIssueCreator(String propFile) {
        if (Utils.isBlankOrNull(propFile)) {
            propFile = "../config/jira.properties";
        }
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propFile));
        } catch (IOException e) {
            throw new FileNotFoundRuntimeException(e);
        }
        try {
            URL jiraRpcUrl = new URL(properties.getProperty("jira.url") + properties.getProperty("jira.rpc.path"));
            XmlRpcClientConfigImpl impl = new XmlRpcClientConfigImpl();
            impl.setServerURL(jiraRpcUrl);
            rpcClient = new XmlRpcClient();
            rpcClient.setConfig(impl);
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to CreateIssue", e);
        }
    }

    public static void main(String[] args) {
        JiraIssueCreator issueCreator = new JiraIssueCreator(null);
        try {
            issueCreator.createIssue(new Issue("Sahi Test", "blah"));
        } catch (Exception e) {
            e.printStackTrace();  
        }
    }

    public void createIssue(Issue issue) throws Exception {
        Map issueParams = getIssueParameters();
        issueParams.put("summary",issue.getSummary());
        issueParams.put("description",issue.getDescription());

        rpcClient.execute("jira1.createIssue", new Object[]{loginToken,issueParams});

        logout();
    }

    public Map getIssueParameters() {
        Map issueParams = new HashMap();
        try {
            String projectKey = new JiraItem("jira1.getProjects", null, properties.getProperty("jira.project")).get("key");
            issueParams.put("project", projectKey);
            issueParams.put("type", new JiraItem("jira1.getIssueTypes", null, properties.getProperty("jira.issueType")).get("id"));
            issueParams.put("priority", new JiraItem("jira1.getPriorities", null, properties.getProperty("jira.priority")).get("id"));
            issueParams.put("assignee", properties.getProperty("jira.assignee"));
            List param = new ArrayList();
            param.add(projectKey);
            issueParams.put("components", new JiraItem("jira1.getComponents", param, properties.getProperty("jira.component")).getMapInList("id"));
            if (!Utils.isBlankOrNull(properties.getProperty("jira.affectsVersions"))) {
                issueParams.put("affectsVersions", new JiraItem("jira1.getVersions", param, properties.getProperty("jira.affectsVersions")).getMapInList("id"));
            }
            if (!Utils.isBlankOrNull(properties.getProperty("jira.fixVersions"))) {
                issueParams.put("fixVersions", new JiraItem("jira1.getVersions", param, properties.getProperty("jira.fixVersions")).getMapInList("id"));
            }
        } catch (XmlRpcException e) {
            throw new RuntimeException(e);
        }
        return issueParams;
    }

    private void logout() throws XmlRpcException {
       rpcClient.execute("jira1.logout", new Object[]{loginToken});
    }

    public String getLoginToken() {
        if (loginToken == null) {
            login();
        }

        return loginToken;
    }

    private void login() {

        List loginParams = new ArrayList();
        loginParams.add(properties.getProperty("jira.username"));
        loginParams.add(properties.getProperty("jira.password"));
        try {
            loginToken = (String) rpcClient.execute("jira1.login", loginParams);
        } catch (Exception e) {
            throw new RuntimeException("Error logging in to CreateIssue", e);
        }
    }

    private class JiraItem {
        private Map attributes;

        public JiraItem(String method, List params, String itemName) throws XmlRpcException {
            boolean found = false;
            List toParams = new ArrayList();
            toParams.add(getLoginToken());
            if (params != null) {
                toParams.addAll(params);
            }
            Object[] listResult = (Object[]) rpcClient.execute(method, toParams);
            for (int i = 0; i < listResult.length; i++) {
                Map map = (Map) listResult[i];
                if (itemName.equals(map.get("name"))) {
                    attributes = map;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException(itemName + " not found in results for method " + method);
            }
        }

        public String get(String attribute) {
            return (String) attributes.get(attribute);
        }

        public List getMapInList(String attribute) {
            List list = new ArrayList();
            Map map = new HashMap();
            map.put(attribute, attributes.get(attribute));
            list.add(map);
            return list;
        }
    }


}