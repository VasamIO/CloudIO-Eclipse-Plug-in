
package com.cloudio.launcher;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Display;

import com.cloudio.exception.CIOException;
import com.cloudio.utils.Constants;
import com.cloudio.utils.EclipseUtility;

public class RemoteDebugDeligate implements ILaunchConfigurationDelegate {
  

  @Override
  public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
          throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType launchConfigurationType = launchManager
            .getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);
    ILaunchConfigurationWorkingCopy workingCopy = launchConfigurationType.newInstance(null,
            "CloudIo Remote Debug");
    workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
    workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_CONNECTOR,
            IJavaLaunchConfigurationConstants.ID_SOCKET_ATTACH_VM_CONNECTOR);
    Map<String, String> connectMap = new HashMap<String, String>();
    Map<String, Object> attrs = config.getAttributes();
    connectMap.put("hostname", (String) attrs.get(Constants.LABEL_CONFIG_HOST));
    connectMap.put("port", (String) attrs.get(Constants.LABEL_CONFIG_PORT));
    workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, connectMap);
    Map<String, String> prefs = null;
    try {
      prefs = EclipseUtility.loadPreferences();
    } catch (CIOException e) {
      e.printStackTrace();
    }
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(prefs.get(Constants.LABEL_PROJECT_NAME));
    if (project != null) {
      workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getName());
    }
    
// // get the default VM connector 
//    IVMConnector connector = JavaRuntime.getDefaultVMConnector(); 
//
//    // connect to remote VM 
//    connector.connect(connectMap, monitor, launch); 
//
//    // check for cancellation 
//    if (monitor.isCanceled()) {
//        IDebugTarget[] debugTargets = launch.getDebugTargets(); 
//        for (IDebugTarget target : debugTargets) { 
//            if (target.canDisconnect()) { 
//                target.disconnect(); 
//            } 
//        } 
//    } 
   
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        DebugUITools.launch(workingCopy, "debug");
      }
    });
    
  }

}
