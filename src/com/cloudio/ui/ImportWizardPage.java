
package com.cloudio.ui;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudio.utils.Constants;
import com.cloudio.utils.FileUtility;

public class ImportWizardPage extends WizardPage implements ModifyListener {

  private Text localDirPathText, projectNameText;
  private Text hostText, userNameText, passwordText;
  private Text debugHostText, debugPortText;

  private String localDirPath, projectName;
  private String host, userName, password;
  private String debugHost, debugPort;

  public ImportWizardPage(String pageName) {
    super(pageName);
    setTitle("Project Setup & Details");
    setDescription("Enter CloudIO project credentails");
  }

  @Override
  public void createControl(Composite parent) {
    setPageComplete(false);
    Composite container = new Composite(parent, SWT.NO_FOCUS);
    container.setLayout(new GridLayout());
    createLocalDestinationGroup(container);
    createAuthenticationGroup(container);
    createRemoteDebugConfig(container);
    setControl(container);
  }

  private Group createGroup(final Composite parent, final String text) {
    Group g = new Group(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    g.setLayout(layout);
    g.setText(text);
    final GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    g.setLayoutData(gd);
    return g;
  }

  private void newLabel(final Group g, final String text) {
    new Label(g, SWT.NULL).setText(text);
  }

  private GridData createFieldGridData() {
    return new GridData(SWT.FILL, SWT.DEFAULT, true, false);
  }

  private void createLocalDestinationGroup(final Composite parent) {
    Group g = createGroup(parent, "Local Destination");

    Label dirLabel = new Label(g, SWT.NONE);
    dirLabel.setText("Directory");
    dirLabel.setToolTipText("Choose Directory");
    Composite p = new Composite(g, SWT.NONE);
    GridLayout grid = new GridLayout();
    grid.numColumns = 2;
    p.setLayout(grid);
    p.setLayoutData(createFieldGridData());
    localDirPathText = new Text(p, SWT.BORDER);
    localDirPathText.setLayoutData(createFieldGridData());
    localDirPathText.setEditable(false);
    localDirPathText.setEnabled(false);
    final Button b = new Button(p, SWT.PUSH);
    b.setText("Browse");
    b.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        final DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.APPLICATION_MODAL);
        final String r = dialog.open();
        if (r != null) {
          localDirPathText.setText(r);
          localDirPath = localDirPathText.getText();
          projectNameText.setEditable(true);
          projectNameText.setFocus();
        }
      }
    });

    newLabel(g, "Project Name");
    projectNameText = new Text(g, SWT.BORDER);
    projectNameText.setLayoutData(createFieldGridData());
    projectNameText.setEditable(false);
    projectNameText.addModifyListener(this);
  }

  private Group createAuthenticationGroup(final Composite parent) {
    Group g = createGroup(parent, "Remote Destination");

    newLabel(g, "Host");
    hostText = new Text(g, SWT.BORDER);
    hostText.setLayoutData(createFieldGridData());
    hostText.addModifyListener(this);

    newLabel(g, "User Name");
    userNameText = new Text(g, SWT.BORDER);
    userNameText.setLayoutData(createFieldGridData());
    userNameText.addModifyListener(this);

    newLabel(g, "Password");
    passwordText = new Text(g, SWT.BORDER | SWT.PASSWORD);
    passwordText.setLayoutData(createFieldGridData());
    passwordText.addModifyListener(this);

    return g;
  }

  private Group createRemoteDebugConfig(final Composite parent) {
    Group g = createGroup(parent, "Remote Debug Configuration");

    newLabel(g, "Host");
    debugHostText = new Text(g, SWT.BORDER);
    debugHostText.setLayoutData(createFieldGridData());
    debugHostText.addModifyListener(this);

    newLabel(g, "Port");
    debugPortText = new Text(g, SWT.BORDER);
    debugPortText.setLayoutData(createFieldGridData());
    debugPortText.setText(Constants.CONST_DEBUG_PORT);
    debugPort = Constants.CONST_DEBUG_PORT;
    debugPortText.addModifyListener(this);

    return g;
  }

  @Override
  public void modifyText(ModifyEvent event) {
    if (event.widget == projectNameText) {
      projectName = projectNameText.getText();
      verifyProjectPath();
    } else if (event.widget == hostText) {
      debugHost = host = hostText.getText();
   /*   if (host != null || host.trim().length() > 1) {
         other than http:// or https:// 
        String protocal = Constants.CONST_PROTOCOL_HTTP;
        String _h = host;
        if (host.contains(Constants.CONST_PROTOCOL_HTTPS)) {
          protocal = Constants.CONST_PROTOCOL_HTTPS;
        }
        int startIndex = protocal.length();
        if (host.length() > startIndex) {
          _h = host.substring(startIndex);
          if (_h.contains(Constants.CONST_SYMBOLS_COLON)) {
            _h = _h.substring(0, _h.indexOf(":"));
          }
        }
        debugHost = protocal + _h;
      }*/
      debugHostText.setText(debugHost);
    } else if (event.widget == userNameText) {
      userName = userNameText.getText();
    } else if (event.widget == passwordText) {
      password = passwordText.getText();
    } else if (event.widget == debugHostText) {
      debugHost = debugHostText.getText();
    } else if (event.widget == debugPortText) {
      debugPort = debugPortText.getText();
    }
  }

  private void verifyProjectPath() {
    if (projectName == null || projectName.trim().length() < 1) {
      setPageComplete(false);
    }
    String errorMsg = null;
    String fullPath = localDirPath;
    if (!localDirPath.endsWith(File.separator)) {
      fullPath += File.separator;
    }
    fullPath += projectName;
    errorMsg = FileUtility.verifyPath(fullPath);
    final File absoluteFile = new File(fullPath).getAbsoluteFile();
    if (!canCreateSubdir(absoluteFile.getParentFile())) {
      errorMsg = NLS.bind("Can not create file/directory", absoluteFile.getPath());
    }
    setErrorMessage(errorMsg);
    setPageComplete(true);
    if (errorMsg != null) {
      setPageComplete(false);
    }
  }

  // this is actually just an optimistic heuristic - should be named
  // isThereHopeThatCanCreateSubdir() as probably there is no 100% reliable
  // way to check that in Java for Windows
  private static boolean canCreateSubdir(final File parent) {
    if (parent == null)
      return true;
    if (parent.exists())
      return parent.isDirectory() && parent.canWrite();
    return canCreateSubdir(parent.getParentFile());
  }

  public String getLocalDirPath() {
    return localDirPath;
  }

  public void setLocalDirPath(String localDirPath) {
    this.localDirPath = localDirPath;
  }

  public String getProjectName() {
    return projectName;
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDebugHost() {
    return debugHost;
  }

  public void setDebugHost(String debugHost) {
    this.debugHost = debugHost;
  }

  public String getDebugPort() {
    return debugPort;
  }

  public void setDebugPort(String debugPort) {
    this.debugPort = debugPort;
  }

}
