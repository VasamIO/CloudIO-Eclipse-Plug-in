package cloudio;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "CloudIO"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	 /**
   * Handle an error. The error is logged. If <code>show</code> is
   * <code>true</code> the error is shown to the user.
   *
   * @param message
   *            a localized message
   * @param throwable
   * @param show
   */
  public static void handleError(String message, Throwable throwable,
      boolean show) {
    handleIssue(IStatus.ERROR, message, throwable, show);
  }

  /**
   * Handle an issue. The issue is logged. If <code>show</code> is
   * <code>true</code> the issue is shown to the user.
   *
   * @param severity
   *            status severity, use constants defined in {@link IStatus}
   * @param message
   *            a localized message
   * @param throwable
   * @param show
   * @since 2.2
   */
  public static void handleIssue(int severity, String message, Throwable throwable,
      boolean show) {
    IStatus status = toStatus(severity, message, throwable);
    int style = StatusManager.LOG;
    if (show)
      style |= StatusManager.SHOW;
    StatusManager.getManager().handle(status, style);
  }
  
  /**
   * Creates an {@link IStatus} from the parameters. If the throwable is an
   * {@link InvocationTargetException}, the status is created from the first
   * exception that is either not an InvocationTargetException or that has a
   * message. If the message passed is empty, tries to supply a message from
   * that exception.
   *
   * @param severity
   *            of the {@link IStatus}
   * @param message
   *            for the status
   * @param throwable
   *            that caused the status, may be {@code null}
   * @return the status
   */
  private static IStatus toStatus(int severity, String message,
      Throwable throwable) {
    Throwable exc = throwable;
    while (exc instanceof InvocationTargetException) {
      String msg = exc.getLocalizedMessage();
      if (msg != null && !msg.isEmpty()) {
        break;
      }
      Throwable cause = exc.getCause();
      if (cause == null) {
        break;
      }
      exc = cause;
    }
    if (exc != null && (message == null || message.isEmpty())) {
      message = exc.getLocalizedMessage();
    }
    return new Status(severity, getPluginId(), message, exc);
  }
  
  /**
   * @return the id of the egit ui plugin
   */
  public static String getPluginId() {
    return getDefault().getBundle().getSymbolicName();
  }
}
