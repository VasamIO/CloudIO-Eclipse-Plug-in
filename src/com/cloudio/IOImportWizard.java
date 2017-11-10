
package com.cloudio;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.cloudio.exception.CIOException;
import com.cloudio.ui.ImportWizardPage;
import com.cloudio.utils.Constants;
import com.cloudio.utils.EclipseUtility;
import com.cloudio.utils.IOUtility;

import cloudio.Activator;

public class IOImportWizard extends Wizard implements IImportWizard {

  private ImportWizardPage importWizardPage;
  private String errorMsg = null;

  @Override
  public void init(IWorkbench arg0, IStructuredSelection arg1) {
    setWindowTitle("Import Projects from CloudIO");
    setNeedsProgressMonitor(true);
    importWizardPage = new ImportWizardPage("Import Title");
  }

  @Override
  public void addPages() {
    super.addPages();
    addPage(importWizardPage);
  }

  @Override
  public boolean performFinish() {
    try {
      getContainer().run(true, true, new IRunnableWithProgress() {
        String localDirPath, projectName, host, userName, password, debugHost, debugPort;
        Map<String, String> details = new HashMap<>();

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
              localDirPath = importWizardPage.getLocalDirPath();
              details.put(Constants.LABEL_LOCAL_DIR_PATH, localDirPath);
              projectName = importWizardPage.getProjectName();
              details.put(Constants.LABEL_PROJECT_NAME, projectName);
              host = importWizardPage.getHost();
              details.put(Constants.LABEL_HOST, host);
              userName = importWizardPage.getUserName();
              details.put(Constants.LABEL_USERNAME, userName);
              password = importWizardPage.getPassword();
              details.put(Constants.LABEL_PASSWORD, password);
              debugHost = importWizardPage.getDebugHost();
              details.put(Constants.LABEL_DEBUG_HOST, debugHost);
              debugPort = importWizardPage.getDebugPort();
              details.put(Constants.LABEL_DEBUG_PORT, debugPort);
            }
          });
          try {
            SubMonitor subMonitor = SubMonitor.convert(monitor, 4);
            String sessionId = IOUtility.authenticate(subMonitor, 1, host, userName, password);
            details.put(Constants.LABEL_SESSIONID, sessionId);
            EclipseUtility.savePreferences(projectName, details);
            EclipseUtility.setupIOProject(subMonitor, 2, localDirPath, projectName);
            IOUtility.downloadProjectData(subMonitor, 3);
            IOUtility.refreshProject(null);
          } catch (CIOException ex) {
            errorMsg = ex.getTitle() + ":" + ex.getMessage();
          }
        }
      });
      if (errorMsg != null) {
        Activator.handleError(errorMsg, null, true);
        return false;
      }
      return true;
    } catch (InvocationTargetException e) {
      Activator.handleError("Action aborted: " + e.getCause().getMessage(), e, true);
      return false;
    } catch (InterruptedException e) {
      Activator.handleError("Action aborted: " + e.getCause().getMessage(), e, true);
      return false;
    }
  }
}