
package com.cloudio.ui;

import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudio.exception.CIOException;
import com.cloudio.utils.Constants;
import com.cloudio.utils.EclipseUtility;

import cloudio.Activator;

public class LaunchConfigTab extends AbstractLaunchConfigurationTab {

  private Text hostText;
  private Text portText;
  private String host;
  private String port;
  
  private String host_C;
  private String port_C;

  @Override
  public void createControl(Composite parent) {

    Map<String, String> pref = null;
    try {
      pref = EclipseUtility.loadPreferences();
    } catch (CIOException e) {
      e.printStackTrace();
    }
    host_C = host = pref.get(Constants.LABEL_DEBUG_HOST);
    port_C = port = pref.get(Constants.LABEL_DEBUG_PORT);

    Composite comp = new Group(parent, SWT.BORDER);
    setControl(comp);

    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(comp);

    Label hostLabel = new Label(comp, SWT.NONE);
    hostLabel.setText("Host");
    GridDataFactory.swtDefaults().applyTo(hostLabel);

    hostText = new Text(comp, SWT.BORDER);
    hostText.setMessage("Host");
    if (host != null && host.trim().length() > 0) {
      hostText.setMessage(host);
      //hostText.setEditable(false);
    }
    hostText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent evnt) {
        host = hostText.getText();
        updateLaunchConfigurationDialog();
      }
    });
    GridDataFactory.fillDefaults().grab(true, false).applyTo(hostText);

    Label portLabel = new Label(comp, SWT.NONE);
    portLabel.setText("Port");
    GridDataFactory.swtDefaults().applyTo(portLabel);

    portText = new Text(comp, SWT.BORDER);
    portText.setMessage("Port");
    if (port != null && port.trim().length() > 0) {
      portText.setText(port);
    }
    portText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent evnt) {
        port = portText.getText();
        if(port != null && port.trim().length() > 0) {
          updateLaunchConfigurationDialog();
        }
      }
    });
    GridDataFactory.fillDefaults().grab(true, false).applyTo(portText);
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(Constants.LABEL_CONFIG_HOST, host);
    configuration.setAttribute(Constants.LABEL_CONFIG_PORT, port);
  }

  @Override
  public String getName() {
    return "Remote Debug Configs";
  }
  
  @Override
  public Image getImage() {
    return Activator.getImageDescriptor("icons/plug.png").createImage();
  }

}