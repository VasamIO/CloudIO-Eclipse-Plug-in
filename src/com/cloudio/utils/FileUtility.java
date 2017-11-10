
package com.cloudio.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

import com.cloudio.exception.CIOException;

public class FileUtility {

  public static void unZip(String fileURL, String saveDir, boolean deleteAfterUnzip) throws CIOException {
    ZipFile zipFile = null;
    try {
      Enumeration<?> entries;
      zipFile = new ZipFile(fileURL);
      entries = zipFile.entries();
      String fileName = null;
      if (!saveDir.endsWith(File.separator)) {
        saveDir += File.separator;
      }
      while (entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) entries.nextElement();
        if (!entry.isDirectory()) {
          fileName = entry.getName();
          copyInputStream(zipFile.getInputStream(entry), saveDir + fileName);
        }
      }
      File file = new File(fileURL);
      if (deleteAfterUnzip && file != null && file.exists()) {
        file.delete();
      }
    } catch (IOException ex) {
      throw new CIOException("Error unziping resources", ex.getMessage());
    } finally {
      try {
        if (zipFile != null) zipFile.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void copyInputStream(InputStream in, String outFileName) throws CIOException {
    try {
      File f = new File(outFileName);
      f.getParentFile().mkdirs();
      copyInputStream(in, new BufferedOutputStream(new FileOutputStream(outFileName)));
    } catch (IOException ex) {
      throw new CIOException("Error copy stream", ex.getMessage());
    }
  }

  public static void copyInputStream(InputStream in, OutputStream out) throws CIOException {
    try {
      byte[] buffer = new byte[10240];
      int len;
      while ((len = in.read(buffer)) >= 0)
        out.write(buffer, 0, len);
      in.close();
      out.close();
    } catch (IOException ex) {
      throw new CIOException("Error copy stream", ex.getMessage());
    }
  }

  public static String verifyPath(String dir) {
    String errorMessage = null;
    if (dir.length() == 0) {
      errorMessage = "Not an absolute path";
    }
    File testFile = new File(dir);
    IPath path = Path.fromOSString(dir);
    for (String segment : path.segments()) {
      IStatus status = ResourcesPlugin.getWorkspace().validateName(segment, IResource.FOLDER);
      if (!status.isOK()) {
        errorMessage = status.getMessage();
      }
    }
    if (!path.isAbsolute()) {
      errorMessage = NLS.bind("Not an absolute path", dir);
    }
    if (testFile.exists() && !testFile.isDirectory()) {
      errorMessage = NLS.bind("No such directory is avaiable!", dir);
    }
    if (testFile.exists()) {
      errorMessage = NLS.bind("Project already exists!", dir);
    }
    return errorMessage;
  }

}
