package orient.http;

import orient.util.RequestHandlar;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h3>Orient Network Server</h3>
 * <p>This class is create this server.<br>
 * Here is can config header, port number, HTTP status and etc...<br>
 * Sample is run testMain.java.</p>
 *
 * <p>Note:<br>
 * Available protocol be going to rapidly increase from now on!</p>
 **/
public class Server extends Thread {
  int port;

  /**
   * This Constructor is config port.
   *
   * @param port The port of network HTTP connector.
   */
  public Server(int port) {
    setPort(port);
  }

  /**
   * This method is setting port.
   *
   * @param port The port of network.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * This method is started this server.
   */
  public void start() {
    //System.out.println(current date);
    System.out.println("Starting server\n");

    try {
      ServerSocket serverSocket = new ServerSocket(port);
      ExecutorService executor = Executors.newFixedThreadPool(3);

      while(true) {
        executor.submit(new RequestHandlar(serverSocket.accept()));
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
