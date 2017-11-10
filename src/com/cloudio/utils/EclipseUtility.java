
package com.cloudio.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetInstallDataModelProperties;
import org.eclipse.jst.j2ee.web.project.facet.IWebFacetInstallDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.osgi.service.prefs.BackingStoreException;

import com.cloudio.exception.CIOException;
import com.cloudio.nature.IONature;

public class EclipseUtility {

  public static String setupIOProject(IProgressMonitor monitor, int taskIndex, String dirPath,
          String projectName)
          throws CIOException {
    if (monitor != null) monitor.setTaskName(Constants.TASK_SETTING_IOPROJECT);
    if (!dirPath.endsWith(File.separator)) {
      dirPath += File.separator;
    }
    dirPath += projectName;

    IProject project = setupIOProjectNarure(projectName, dirPath);
    // setupJSTWebnature(projectName);
    setupJavaNature(project);
    if (monitor != null) monitor.worked(taskIndex);
    return dirPath;
  }

  private static IProject setupIOProjectNarure(String projectName, String dirPath)
          throws CIOException {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(projectName);
    try {
      IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
      description.setLocation(new Path(dirPath));
      description.setNatureIds(new String[] {
          IONature.NATURE_ID,
         // "org.eclipse.jem.workbench.JavaEMFNature",
          "org.eclipse.jdt.groovy.core.groovyNature",
          "org.eclipse.wst.jsdt.core.jsNature",
          "org.eclipse.wst.common.modulecore.ModuleCoreNature",
          "org.eclipse.wst.common.project.facet.core.nature",
          JavaCore.NATURE_ID,
      });

      ICommand[] commands = new ICommand[] { description.newCommand(), description.newCommand() };
      commands[0].setBuilderName(org.eclipse.jdt.core.JavaCore.BUILDER_ID);
      commands[1].setBuilderName("org.eclipse.wst.common.project.facet.core.builder");
      description.setBuildSpec(commands);

      project.create(description, null);
      project.open(null);

      return project;

    } catch (CoreException e) {
      throw new CIOException(Constants.ERROR_ACTION_ABORTED, e.getMessage());
    }
  }

  public static void setupJSTWebnature(String projName) throws CIOException {
    IProjectFacet JAVA_FACET = ProjectFacetsManager.getProjectFacet(IJ2EEFacetConstants.JAVA);
    IDataModel webModel = DataModelFactory.createDataModel(IWebFacetInstallDataModelProperties.class);
    webModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projName);
    FacetDataModelMap facetMap = (FacetDataModelMap) webModel
            .getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
    IDataModel facetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.DYNAMIC_WEB);
    facetModel.setProperty(IWebFacetInstallDataModelProperties.INSTALL_WEB_LIBRARY, true);
    facetModel.setProperty(IFacetDataModelProperties.FACET_VERSION, IJ2EEFacetConstants.DYNAMIC_WEB_31);
    facetModel.setBooleanProperty(IJ2EEFacetInstallDataModelProperties.GENERATE_DD, true);// Deployment
                                                                                          // Descriptor
    IDataModel javaFacetModel = facetMap.getFacetDataModel(IJ2EEFacetConstants.JAVA);
    javaFacetModel.setProperty(IFacetDataModelProperties.FACET_VERSION,
            JAVA_FACET.getVersion(Constants.CONST_JAVA_VERSION));
    try {
      webModel.getDefaultOperation().execute(new NullProgressMonitor(), null);
    } catch (ExecutionException e) {
      throw new CIOException(Constants.ERROR_ACTION_ABORTED, e.getMessage());
    }
  }

  private static void setupJavaNature(IProject project) throws CIOException {
    try {
      IFolder src = project.getFolder("src");
      src.create(true, true, null);
      IJavaProject javaProject = JavaCore.create(project);
      List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
      IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime.getExecutionEnvironmentsManager();
      IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager.getExecutionEnvironments();
      for (IExecutionEnvironment iExecutionEnvironment : executionEnvironments) {
        if (Constants.CONST_JAVA_ENVIRONMENT.equals(iExecutionEnvironment.getId())) {
          entries.add(JavaCore.newContainerEntry(JavaRuntime.newJREContainerPath(iExecutionEnvironment)));
          break;
        }
      }

      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);

      /* bin/classes output folder */
      IFolder target = project.getFolder("target");
      target.create(true, true, null);

      IFolder classes = target.getFolder("classes");
      classes.create(true, true, null);

      /* compiled ".class" path */
      javaProject.setOutputLocation(classes.getFullPath(), null);

      IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
      IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
      System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);

      IPackageFragmentRoot packageRoot = javaProject.getPackageFragmentRoot(src);
      newEntries[oldEntries.length] = JavaCore.newSourceEntry(packageRoot.getPath(), new Path[] {}, new Path[] {},
              classes.getFullPath());

      // IFolder webPath =
      // project.getFolder("WebContent").getFolder("WEB-INF").getFolder("lib");
      //
      // IPath weblibpath = webPath.getFullPath();
      // newEntries[oldEntries.length + 1] =
      // JavaCore.newContainerEntry(weblibpath);
      // newLibraryEntry(weblibpath, null, null);
      javaProject.setRawClasspath(newEntries, null);
    } catch (Exception e) {
      throw new CIOException(Constants.ERROR_ACTION_ABORTED, e.getMessage());
    }
  }

  public static void savePreferences(String id, Map<String, String> values) throws CIOException {
    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Constants.PLUGIN_ID);
    Iterator<String> iterator = values.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      String value = values.get(key);
      prefs.put(key, value);
    }
    try {
      prefs.flush();
    } catch (BackingStoreException e) {
      throw new CIOException(Constants.ERROR_WHILE_SAVING_PREFS, e.getMessage());
    }
  }

  public static Map<String, String> loadPreferences() throws CIOException {
    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Constants.PLUGIN_ID);
    Map<String, String> map = new HashMap<>();
    String[] arr;
    try {
      arr = prefs.keys();
      for (String str : arr) {
        map.put(str, prefs.get(str, ""));
      }
    } catch (BackingStoreException e) {
      throw new CIOException(Constants.ERROR_WHILE_SAVING_PREFS, e.getMessage());
    }
    return map;
  }

  public static void startMonitor(IProgressMonitor monitor, int taskIndex, String taskName) {
    startMonitor(monitor, -1, taskIndex, taskName);
  }

  public static void startMonitor(IProgressMonitor monitor, int taskLength, int taskIndex, String taskName) {
    if (monitor != null) {
      if (taskIndex > 1) {
        monitor.setTaskName(taskName);
      } else {
        if (taskLength < 1) taskLength = 1;
        monitor.beginTask(taskName, taskLength);
      }
    }
  }

  public static void endMonitor(IProgressMonitor monitor, int taskIndex) {
    endMonitor(monitor, taskIndex, false);
  }

  public static void endMonitor(IProgressMonitor monitor, int taskIndex, boolean skipDone) {
    if (monitor != null) {
      monitor.worked(taskIndex);
      if (taskIndex == 1 && !skipDone) {
        monitor.done();
      }
    }
  }

}
