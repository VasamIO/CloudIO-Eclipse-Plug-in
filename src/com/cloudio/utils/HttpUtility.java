
package com.cloudio.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudio.exception.CIOException;

import gvjava.org.json.JSONException;
import gvjava.org.json.JSONObject;

public class HttpUtility {

  private static final int BUFFER_SIZE = 4096;
  private static final String MIME_APPICATION_JSON = "application/json";
  private static int currentProgress = -1;

  public static JSONObject httpPost(String serverUrl, String params) throws CIOException {
    StringBuilder sb = new StringBuilder();
    String http = serverUrl;
    HttpURLConnection urlConnection = null;
    try {
      URL url = new URL(http);
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setDoOutput(true);
      urlConnection.setRequestMethod("POST");
      urlConnection.setUseCaches(false);
      urlConnection.setConnectTimeout(50000);
      urlConnection.setReadTimeout(50000);
      urlConnection.setRequestProperty("Content-Type", MIME_APPICATION_JSON);
      urlConnection.connect();
      OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
      out.write(params);
      out.close();
      int HttpResult = urlConnection.getResponseCode();
      if (HttpResult == HttpURLConnection.HTTP_OK) {
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
        String line = null;
        while ((line = br.readLine()) != null) {
          sb.append(line + "\n");
        }
        br.close();
        return new JSONObject(sb.toString());
      } else {
        throw new CIOException("Error in respose", serverUrl + ", response code:" + HttpResult);
      }
    } catch (IOException | JSONException ex) {
      throw new CIOException("Error while connection", ex.getMessage());
    } finally {
      if (urlConnection != null)
        urlConnection.disconnect();
    }
  }

  public static File downloadFile(IProgressMonitor monitor, String fileURL, String saveDir) throws CIOException {
    URL url = null;
    HttpURLConnection httpConn = null;
    InputStream inputStream = null;
    FileOutputStream outputStream = null;
    try {
      url = new URL(fileURL);
      httpConn = (HttpURLConnection) url.openConnection();
      int responseCode = httpConn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        String fileName = "";
        String disposition = httpConn.getHeaderField("Content-Disposition");
        if (disposition != null) {
          int index = disposition.indexOf("filename=");
          if (index > 0) {
            fileName = disposition.substring(index + 10,
                    disposition.length() - 1);
          }
        } else {
          fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                  fileURL.length());
        }
        inputStream = httpConn.getInputStream();
        if (!saveDir.endsWith(File.separator)) {
          saveDir += File.separator + fileName;
        }
        outputStream = new FileOutputStream(saveDir);
        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        long downloadedFileSize = 0;
        long completeFileSize = httpConn.getContentLength();
        monitor.setTaskName(Constants.TASK_DOWNLOADING_RESOURCES);
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          downloadedFileSize += bytesRead;
          currentProgress = (int) ( (((double)downloadedFileSize) / ((double)completeFileSize)) * 100d);
          if(monitor != null && currentProgress > -1) {
            monitor.setTaskName(String.format(Constants.TASK_DOWNLOADING_RESOURCES_PROGRESS, currentProgress));
          }
          outputStream.write(buffer, 0, bytesRead);
        }
        return new File(saveDir);
      } else {
        throw new CIOException("Unable to download to resources", "Server replied HTTP code: " + responseCode);
      }
    } catch (IOException ex) {
      throw new CIOException("Error while connection", ex.getMessage());
    } finally {
      if (httpConn != null) httpConn.disconnect();
      if (outputStream != null) try {
        outputStream.close();
        if (inputStream != null) inputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  public static int getCurrentDownloadProgress() {
    return currentProgress;
  }

  public static void main(String[] args) throws CIOException {
//    long start = System.currentTimeMillis();
//    String fileUrl = "https://ebs.cloudio.io/d/ROOT_PROD.war";
//    String saveDest = "/Users/vasam/Downloads/war";
//    File warFile = downloadFile(null, fileUrl, saveDest);
//    FileUtility.unZip(warFile.getPath(), "/Users/vasam/Downloads/war/ROOT_PROD");
//    if (warFile.exists()) warFile.delete();
//    long end = System.currentTimeMillis();
//    System.out.println("Took: " + (((end - start) / 1000) / 60) + " min");
  }
}