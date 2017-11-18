package orient.util;

import orient.http.Server;
import orient.http.HttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class RequestHandlar implements Runnable {
  private Socket socket;
  private StringBuffer header = new StringBuffer();
  BufferedReader in;
  Hashtable headers;
  InputStream input;
  int status = 200;
  OutputStream output;
  String rn = "\r\n";
  String CHARSET = "UTF-8";
  String path = "./WEB-CONTENT/index.html";
  String pathInfo, queryString;
  String method, uri, protocol;
  static Hashtable types = new Hashtable() { {
  	put("", "text/html");
  	put("ico", "image/x-icon");
  	put("jpeg","image/jpeg");
  	put("jpe", "image/jpeg");
  	put("jpg", "image/jpeg");
  	put("tiff","image/tiff");
  	put("tif", "image/tiff");
  	put("gif", "image/gif");
  	put("png", "image/png");
  	put("bmp", "image/bmp");
  	put("css", "text/css");
  	put("htm", "text/html");
  	put("html","text/html");
  	put("java","text/plain");
  	put("psp", "text/plain");
  	put("doc", "application/msword");
  	put("xls", "application/vnd.ms-excel");
  	put("ppt", "application/vnd.ms-powerpoint");
  	put("pps", "application/vnd.ms-powerpoint");
  	put("js",  "application/javascript");
  	put("jse", "application/javascript");
  	put("reg", "application/octet-stream");
  	put("eps", "application/postscript");
  	put("ps",  "application/postscript");
  	put("gz",  "application/x-gzip");
  	put("hta", "application/hta");
  	put("jar", "application/zip");
  	put("zip", "application/zip");
  	put("pdf", "application/pdf");
  	put("qt",  "video/quicktime");
  	put("mov", "video/quicktime");
  	put("avi", "video/x-msvideo");
  	put("wav", "audio/x-wav");
  	put("snd", "audio/basic");
  	put("mid", "audio/basic");
  	put("au",  "audio/basic");
  	put("mpeg","video/mpeg");
  	put("mpe", "video/mpeg");
  	put("mpg", "video/mpeg");
  	put("au", "video/mpeg");
  }};

  /**
   * This constructor is socket and setting content type;
   *
   * @param socket Socket of this server.
   */
  public RequestHandlar(Socket socket) {
    this.socket = socket;
    setContentType("text/html;charset=utf-8");
  }

  /**
   * This method is setting header.
   *
   * @param name The name of header.
   * @param value The configured value in name.
   */
  public void setHeader(String name, String value) {
     header.append(name + ": " + value + rn);
  }

  /**
   * This method is getting header.
   *
   * @param name The name of header.
   * @return This name of header;
   */
  public String getHeader(String name) {
	   return (String) headers.get(name);
  }

  /**
   * This method is setting context-type.
   *
   * @param type The context-type.
   */
  public void setContentType(String type) {
    setHeader("Content-Type", type != null ? type : "text/plain");
  }

  /**
   * This method is setting status.
   *
   * @param status The status of response.
   */
  public void setStatus(int status) {
    this.status = status;
  }

  public void run() {
    try {
      input = socket.getInputStream();
      in = new BufferedReader(new InputStreamReader(input, CHARSET));
      String line = in.readLine();
      String[] datas = line.split(" ", 4);  //StringTokenizer toks = new StringTokenizer(line, " ");

      //System.out.println("-----------Tokenizer----------");
      //while(toks.hasMoreTokens()) {
      //  System.out.println(toks.nextToken());
      //}
      //System.out.println("------------------------------");

      method = datas[0];
      uri = datas[1];
      protocol = datas[2];

      int index = uri.indexOf('?');

      if(index >= 0) {
        pathInfo = uri.substring(0, index);
        queryString = uri.substring(index + 1);
      } else {
        pathInfo = uri;
      }

      pathInfo = URLDecoder.decode(pathInfo, CHARSET);
      headers = new Hashtable();

      while((line = in.readLine()) != null) {
        if(line.length() == 0) {
          break;
        }

        index = line.indexOf(':');
        String name = line.substring(0, index);
        String value = line.substring(index + 2);
        headers.put(name, value);
      }

      String fullpath = pathInfo;
      index = pathInfo.substring(1).indexOf('/');

      if(index < 0) {
        index = pathInfo.length() - 1;
      }

      String subName = pathInfo.substring(1, index + 1);

      try {
        File pathFile = new File(pathInfo);

        if(pathFile.isDirectory() || pathInfo.charAt(pathInfo.length() - 1) == '/') {
          pathFile = new File(pathInfo + "/index.html");
        }

        InputStream doc = null;
        String stamp = null;

        if(doc == null) {
          doc = new FileInputStream("./WEB-CONTENT" + pathFile);
          stamp = HttpResponse.gmtDate(pathFile.lastModified());
        }

        String since = getHeader("If-Modified-Since");

        if(since != null) {
          if(since.charAt(5) == '0') {
            since = since.substring(0, 5) + since.substring(6);
          }
        }

        index = pathInfo.lastIndexOf('.');
        String ext = index > 0 ? pathInfo.substring(index + 1) : "html";
        String mimeType = (String) types.get(ext);
        setContentType(mimeType);

        OutputStream os = null;

        if (false || stamp == null || since == null || !stamp.equals(since.substring(0, stamp.length()))) {
          os = getOutputStream();
          int bytesRead;
          byte buffer[] = new byte[8 * 1024];

          while ((bytesRead = doc.read(buffer))!= -1) {
            os.write(buffer, 0, bytesRead);
          }
        } else {
          setStatus( 304 );
          os = getOutputStream();
        }

        os.flush();
      } catch (Exception e) {
        e.printStackTrace();
      }

      socket.close();
    } catch (FileNotFoundException fnfe) {
      try {
        PrintWriter erout = getWriter();
  			erout.println("HTTP/1.0 404 Not Found");
  			erout.println("Content-Type: text/plain");
  			erout.println("File not found: " + path);
  			erout.close();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is output response header.
   *
   * @return Returns output stream inner socket.
   */
  public OutputStream getOutputStream() {
    try {
      String response = "HTTP/1.0 " + status + " OK" + rn + "Connection: close" + rn + header + rn;
      output = socket.getOutputStream();

      //PrintWriter hOutput = new PrintWriter(new OutputStreamWriter(output));
      output.write(response.getBytes());
      //System.out.println(response);
      //hOutput.flush();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    return output;
  }

  /**
   * This method is printing text.
   *
   * @return Returns PrintWriter of getOutputStream().
   * @throws IOException if i/o was exception.
   */
  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(new OutputStreamWriter(getOutputStream()));
  }
}
