
package com.cloudio.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudio.exception.CIOException;

import gvjava.org.json.JSONArray;
import gvjava.org.json.JSONException;
import gvjava.org.json.JSONObject;

public class IOUtility {

  public final static int OFFSET = 0;
  public final static int LIMIT = 500;

  public static String authenticate(IProgressMonitor monitor, int taskIndex, String url, String userName,
          String password)
          throws CIOException {
    try {
      String taskName = String.format(Constants.TASK_AUTHENTICATING, url);
      if (monitor != null) monitor.setTaskName(taskName);
      JSONObject params = new JSONObject();
      params.put(Constants.LABEL_USERNAME, userName);
      params.put(Constants.LABEL_PASSWORD, password);
      if (!url.endsWith(Constants.SLASH)) {
        url += Constants.SLASH;
      }
      url += Constants.API_SIGNIN;
      JSONObject response = HttpUtility.httpPost(url, params.toString());
      if (response == null || response.isNull(Constants.LABEL_SESSIONID)) {
        if (response != null && response.has(Constants.LABEL_ERRORMSG)) {
          throw new CIOException(Constants.ERROR_WHILE_AUTHENTICATION,
                  response.getString(Constants.LABEL_ERRORMSG));
        }
        throw new CIOException(Constants.ERROR_WHILE_AUTHENTICATION, Constants.ERROR_AUTHENTICATION_FAILED);
      }
      if (monitor != null) monitor.worked(taskIndex);
      return response.getString(Constants.LABEL_SESSIONID);
    } catch (Exception e) {
      throw new CIOException(Constants.ERROR_WHILE_AUTHENTICATION, e.getMessage());
    }
  }

  public static void downloadProjectData(IProgressMonitor monitor, int taskIndex) throws CIOException {
    try {
      monitor.setTaskName(Constants.TASK_GETTING_DATA);
      Map<String, String> map = EclipseUtility.loadPreferences();
      String sessionId = null;
      if (map.containsKey(Constants.LABEL_SESSIONID)) {
        sessionId = map.get(Constants.LABEL_SESSIONID);
      } else {
        // Throw authentication exception or relogin popup
      }
      String host = map.get(Constants.LABEL_HOST);
      if (!host.endsWith(Constants.SLASH)) {
        host += Constants.SLASH;
      }
      String dirPath = map.get(Constants.LABEL_LOCAL_DIR_PATH);
      String projectName = map.get(Constants.LABEL_PROJECT_NAME);
      if(!dirPath.endsWith(File.separator)) {
        dirPath += File.separator;
      }
      dirPath += projectName;
      JSONObject params = null;
      JSONObject response = null;

      /* RAObjects setup */
      String raObjectAPI = host + Constants.API_RA_OBJECTS;
      params = new JSONObject();
      params.put(Constants.LABEL_SESSIONID, sessionId);
      params.put("offset", OFFSET);
      params.put("limit", LIMIT);
      params.put("orderBy", "#lastUpdateDate# desc");
      response = getRestData(raObjectAPI, sessionId, params);
      createRAObjects(monitor, dirPath, response);
      /* End RAObjects setup */

      /* IoPages setup */
      /* End IoPages setup */
    } catch (Exception e) {
      throw new CIOException(Constants.ERROR_WHILE_GETTING_DATA, e.getMessage());
    }
  }

  public static JSONObject getRestData(String api, String sessionId, JSONObject params) throws CIOException {
    JSONObject response = null;
    try {
      response = HttpUtility.httpPost(api, params.toString());
      // Check for error... Dont forget
      return response;
    } catch (Exception e) {
      throw new CIOException(Constants.ERROR_WHILE_GETTING_DATA, e.getMessage());
    }
  }

  public static void refreshProject(IProgressMonitor monitor) throws CIOException {
    try {
      ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
    } catch (CoreException e) {
      throw new CIOException(Constants.ERROR_ACTION_ABORTED, e.getMessage());
    }
  }

  public static void createRAObjects(IProgressMonitor monitor, String dirPath, JSONObject response)
          throws JSONException, IOException {
    if (!dirPath.endsWith(File.separator)) {
      dirPath += File.separator;
    }
    dirPath += Constants.PATH_RAOBJECTS; // Root/projectname/src/io/datasource/
    Map<String, Integer> fileIndex = new HashMap<>();
    String objectName = null;
    if (response != null && !response.isNull(Constants.LABEL_DATA)) {
      JSONArray list = response.getJSONArray(Constants.LABEL_DATA);
      if (monitor != null) monitor.setTaskName(Constants.TASK_CREATING_RAOBECTS);
      for (int i = 0; i < list.length(); i++) {
        JSONObject raObject = list.getJSONObject(i);
        objectName = raObject.getString("objectName");
        if (monitor != null)
          monitor.setTaskName(String.format(Constants.TASK_CREATING_RAOBJECT, objectName, (i + 1), list.length()));
        createRAObject(dirPath, raObject, fileIndex);
      }
    }
  }

  private static void createRAObject(String dirPath, JSONObject data, Map<String, Integer> fileIndex)
          throws JSONException, IOException {
    StringBuilder preQuery = new StringBuilder();
    StringBuilder postQuery = new StringBuilder();

    if (data.has("preQueryScript")) {
      preQuery.append(data.getString("preQueryScript"));
    }
    if (data.has("postQueryScript")) {
      postQuery.append(data.getString("postQueryScript"));
    }

    String objectName = data.getString("objectName");
    objectName = getUniqueFileName(objectName, fileIndex);

    Map<String, StringBuilder> files = new HashMap<>();
    files.put("PreQuery.groovy", preQuery);
    files.put("PostQuery.groovy", postQuery);

    Iterator<String> iterator = files.keySet().iterator();
    String path, key;
    File file;
    while (iterator.hasNext()) {
      key = iterator.next();
      path = dirPath + objectName + File.separator + key;
      file = new File(path);
      if (file.exists()) {
        file.delete();
      }
      ;
      file.getParentFile().mkdirs();
      file.createNewFile();
      Files.write(Paths.get(path), files.get(key).toString().getBytes());
    }
  }

  public static void createIOPages(IProgressMonitor monitor, String dirPath, JSONObject response)
          throws JSONException, IOException {
    Map<String, Integer> fileIndex = new HashMap<>();
    String pageName = null;
    if (response != null && !response.isNull(Constants.LABEL_DATA)) {
      JSONArray list = response.getJSONArray(Constants.LABEL_DATA);
      monitor.setTaskName(Constants.TASK_CREATING_IOPAGES);
      for (int i = 0; i < list.length(); i++) {
        JSONObject page = list.getJSONObject(i);
        if (page.has(Constants.LABEL_PAGE_NAME)) {
          pageName = page.getString(Constants.LABEL_PAGE_NAME);
        }
        if (monitor != null)
          monitor.setTaskName(String.format(Constants.TASK_CREATING_IOPAGE, pageName, (i + 1), list.length()));
        createIOPage(dirPath, page, fileIndex);
      }
    }
  }

  private static void createIOPage(String dirPath, JSONObject data, Map<String, Integer> fileIndex)
          throws JSONException, IOException {
    StringBuilder template = new StringBuilder();
    StringBuilder controller = new StringBuilder();
    StringBuilder resolveJson = new StringBuilder();
    StringBuilder details = new StringBuilder();
    StringBuilder config = new StringBuilder();

    JSONObject detailsJson = new JSONObject();
    JSONObject configJson = new JSONObject();

    String pageName = null;

    if (data.has("templateHtml")) {
      template.append(data.getString("templateHtml"));
    }
    if (data.has("controller")) {
      controller.append(data.getString("controller"));
    }
    if (data.has("resolveJson")) {
      resolveJson.append(data.getString("resolveJson"));
    }

    if (data.has("pageName")) {
      pageName = data.getString("pageName");
      detailsJson.put("pageName", pageName);
    }
    pageName = getUniqueFileName(pageName, fileIndex);
    if (data.has("i18nCode")) {
      detailsJson.put("i18nCode", data.getString("i18nCode"));
    }
    if (data.has("pageCode")) {
      detailsJson.put("pageCode", data.getString("pageCode"));
    }
    if (data.has("groupHeader")) {
      detailsJson.put("groupHeader", data.getString("groupHeader"));
    }
    if (data.has("icon")) {
      detailsJson.put("icon", data.getString("icon"));
    }
    if (data.has("abstract")) {
      detailsJson.put("abstract", data.getString("abstract"));
    }
    if (data.has("seqNo")) {
      detailsJson.put("seqNo", data.getString("seqNo"));
    }
    if (data.has("pageState")) {
      detailsJson.put("pageState", data.getString("pageState"));
    }
    if (data.has("seqNo")) {
      detailsJson.put("seqNo", data.getString("seqNo"));
    }
    if (data.has("pageStateParams")) {
      detailsJson.put("pageStateParams", data.getString("pageStateParams"));
    }
    if (data.has("url")) {
      detailsJson.put("url", data.getString("url"));
    }
    if (data.has("controllerName")) {
      detailsJson.put("controllerName", data.getString("controllerName"));
    }

    if (data.has("pageId")) {
      configJson.put("pageId", data.getString("pageId"));
    }
    if (data.has("orgId")) {
      configJson.put("orgId", data.getString("orgId"));
    }

    details.append(detailsJson.toString(3));
    config.append(configJson.toString(3));

    Map<String, StringBuilder> files = new HashMap<>();
    files.put("template.html", template);
    files.put("controller.js", controller);
    files.put("details.json", details);
    files.put("config.json", config);

    Iterator<String> iterator = files.keySet().iterator();
    String path, key;
    File file;
    while (iterator.hasNext()) {
      key = iterator.next();
      path = dirPath + pageName + File.separator + key;
      file = new File(path);
      if (file.exists()) {
        file.delete();
      }
      ;
      file.getParentFile().mkdirs();
      file.createNewFile();
      Files.write(Paths.get(path), files.get(key).toString().getBytes());
    }

  }

  private static String getUniqueFileName(String fileName, Map<String, Integer> fileIndex) {
    fileName = fileName.replaceAll("\\s+", "");
    if (fileIndex.containsKey(fileName)) {
      int index = fileIndex.get(fileName);
      fileIndex.put(fileName, ++index);
    } else {
      fileIndex.put(fileName, 0);
    }
    if (fileIndex.get(fileName) > 0) {
      return fileName + "_" + fileIndex.get(fileName);
    }
    return fileName;
  }

  public static void main(String[] args) throws Exception {
//    String host = "http://192.168.10.112:3480";
//    String userName = "admin";
//    String password = "admin@123";
//    String dirPath = "/Users/vasam/Downloads/git/Test";
//    String sessionId = IOUtility.authenticate(null, 1, host, userName, password);
//    host += "/" + Constants.API_RA_OBJECTS;
//    JSONObject params = new JSONObject();
//    params.put(Constants.LABEL_SESSIONID, sessionId);
//    params.put("offset", OFFSET);
//    params.put("limit", LIMIT);
//    params.put("orderBy", "#lastUpdateDate# desc");
//    JSONObject data = getRestData(host, sessionId, params);
//    createRAObjects(null, dirPath, data);
  }

}
