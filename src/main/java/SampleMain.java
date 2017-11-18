import orient.http.Server;

public class SampleMain {
  public static void main(String[] args) {
    Server server = new Server(8765);
    server.start();
  }
}
