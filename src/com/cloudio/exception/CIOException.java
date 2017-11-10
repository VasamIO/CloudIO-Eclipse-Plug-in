
package com.cloudio.exception;

public class CIOException extends Exception {

  private static final long serialVersionUID = 1L;
  String msg;
  String title;

  public CIOException(String title, String msg) {
    this.title = title;
    this.msg = msg;
  }

  public String getMessage() {
    return msg;
  }

  public void setMessage(String msg) {
    this.msg = msg;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return title + ":" + msg;
  }

}
