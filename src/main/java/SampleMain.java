import orient.http.Server;

public class SampleMain {
  public static void main(String[] args) {
    Server server = new Server(8888);
    server.start();
  }
}
