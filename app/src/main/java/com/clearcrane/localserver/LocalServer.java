package com.clearcrane.localserver;

import android.os.Handler;
import android.util.Log;

import com.clearcrane.localserver.NanoHTTPD.Response.Status;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;


public class LocalServer extends NanoHTTPD {

    public final static String TAG = "LocalServer";
    public static int port = 16309;
    private String rootDir = "";

    private Handler mHandler = new Handler();
    private Runnable startRunnable = new Runnable() {
 
        @Override
        public void run() {
            mLocalServer = new LocalServer(++port);
        }
    };
    
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {
        {
            put("css", "text/css");
            put("htm", "text/html");
            put("html", "text/html");
            put("xml", "text/xml");
            put("java", "text/x-java-source, text/java");
            put("md", "text/plain");
            put("txt", "text/plain");
            put("asc", "text/plain");
            put("gif", "image/gif");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("mp3", "audio/mpeg");
            put("m3u", "audio/mpeg-url");
            put("mp4", "video/mp4");
            put("ogv", "video/ogg");
            put("flv", "video/x-flv");
            put("mov", "video/quicktime");
            put("swf", "application/x-shockwave-flash");
            put("js", "application/javascript");
            put("pdf", "application/pdf");
            put("doc", "application/msword");
            put("ogg", "application/x-ogg");
            put("zip", "application/octet-stream");
            put("exe", "application/octet-stream");
            put("class", "application/octet-stream");
            put("ts", "text/plain");
        }
    };

    private static LocalServer mLocalServer = null;

    public static LocalServer instance() {
        if (mLocalServer == null) {
            mLocalServer = new LocalServer(port);
        }
        return mLocalServer;
    }

    private LocalServer(int port) {
        super(port);
        
        try {
            start();
        } catch (Exception e) {
            Log.e(TAG, "[" + e.getMessage() + "]");
            mHandler.postDelayed(startRunnable, 2000);
        }
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public void destroy() {
        mLocalServer.stop();
    }

    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header, Map<String, String> parameters,
                          Map<String, String> files) {
         Log.e("zxb", method + " '" + uri + "' ");

        // Iterator<String> iter = header.keySet().iterator();
        // while (iter.hasNext()) {
        // String value = iter.next();
        // L.w(TAG, "  HDR: '" + value + "' = '" + header.get(value) + "'");
        // }
        // iter = parameters.keySet().iterator();
        // while (iter.hasNext()) {
        // String value = iter.next();
        // L.w(TAG, "  PRM: '" + value + "' = '" + parameters.get(value) +
        // "'");
        // }

        String bytesStr = header.get("range");
        int bytes = 0;
        if (bytesStr != null && bytesStr.trim().length() > 0) {
            bytesStr = bytesStr.substring("bytes=".length(),
                    bytesStr.length() - 1);
            Log.d(TAG, "bytes=" + bytesStr);
            bytes = Integer.parseInt(bytesStr);
        }

        FileInputStream fis = null;
        Response response = null;
        try {
            // fis = new FileInputStream(rootDir + uri);
            fis = new FileInputStream(uri);
            // L.w(TAG, "file size=" + fis.available());
            
            if (bytes > 0 && bytes <= fis.available()) {
                fis.skip(bytes);
            }
            
            String extention = "";
            int i = uri.lastIndexOf(".");
            if (i > -1 && i < uri.length()) {
                extention = uri.substring(i + 1);
            }
            String mimeType = "text/plain";
            if (uri.endsWith(".m3u8")) {
                mimeType = "application/vnd.apple.mpegURL";
            }
            response = new NanoHTTPD.Response(Status.PARTIAL_CONTENT, mimeType,
                    fis);
            response.addHeader("accept-ranges", "bytes");
            response.addHeader(
                    "content-range",
                    "bytes " + bytes + "-" + (fis.available() - 1) + "/"
                            + fis.available());
            response.addHeader("content-length", "" + (fis.available() - bytes));
            response.addHeader("connection", "close");
        } catch (Exception e) {
            Log.w(TAG, "[" + Log.getStackTraceString(e) + "]");
            response = new NanoHTTPD.Response(Status.NOT_FOUND, "text/html",
                    "404");
        }

        return response;
    }

    // @Override
    // public Response serve(IHTTPSession session) {
    // Map<String, String> header = session.getHeaders();
    // Map<String, String> parms = session.getParms();
    // String uri = session.getUri();
    //
    // L.w(TAG, "uri[" + uri + "]");
    // String bytes = header.get("Range");
    // L.w(TAG, "bytes=" + bytes + " 2:" + parms.get("Range") + " 3:" +
    // files.get("Range"));
    // FileInputStream fis = null;
    // Response response = null;
    // try {
    // fis = new FileInputStream(rootDir + uri);
    // L.w(TAG, "file size=" + fis.available());
    // String extention = "";
    // int i = uri.lastIndexOf(".");
    // if (i > -1 && i < uri.length()) {
    // extention = uri.substring(i+1);
    // }
    // String mimeType = MIME_TYPES.get(extention);
    // if (mimeType == null || mimeType.trim().length() <= 0) {
    // mimeType = "text/plain";
    // }
    // response = new NanoHTTPD.Response(Status.PARTIAL_CONTENT, mimeType, fis);
    // response.addHeader("Accept-Ranges", "bytes");
    // response.addHeader("Content-Range", "bytes 0-"
    // + (fis.available() - 1) + "/" + fis.available());
    // response.addHeader("Content-Length", "" + fis.available());
    // response.addHeader("Connection", "close");
    // } catch (Exception e) {
    // L.w(TAG, "[" + L.getStackTraceString(e) + "]");
    // response = new NanoHTTPD.Response(Status.NOT_FOUND, "text/html", "404");
    // }
    //
    // return response;
    // }

    /*
     * // thread used to listening to client requests public class ListenThread
     * extends Thread { public void run() { while (true) { try { Socket client =
     * serverSocket.accept(); try { BufferedReader in = new BufferedReader( new
     * InputStreamReader(client.getInputStream()));
     * 
     * String line = in.readLine(); String resource =
     * line.substring(line.indexOf('/') + 1, line.lastIndexOf('/') - 5); // get
     * the path of requested file resource = URLDecoder.decode(resource,
     * "UTF-8");
     * 
     * // return the requested file to client fileService(resource, client);
     * closeSocket(client); continue;
     * 
     * } catch (Exception e) { L.w(TAG, "read request failure " +
     * e.getLocalizedMessage()); } } catch (Exception e) { L.w(TAG,
     * "accept failure " + e.getLocalizedMessage()); } } } }
     * 
     * // thread trying to start the local server public class StartThread
     * extends Thread { public void run() { Random random = new Random(); port =
     * Math.abs(random.nextInt()) % 65535; while (!serverStarted) { try {
     * serverSocket = new ServerSocket(port); new ListenThread().start();
     * serverStarted = true; } catch (Exception e) { // port used, set a new
     * random port if (e instanceof java.net.BindException) { port =
     * Math.abs(random.nextInt()) % 65535; } } SystemClock.sleep(3000); } } }
     * 
     * private void closeSocket(Socket socket) { try { socket.close(); } catch
     * (Exception e) { e.printStackTrace(); } }
     * 
     * private void fileService(String fileName, Socket socket) { L.i(TAG,
     * "try transfer " + fileName); try { PrintStream out = new
     * PrintStream(socket.getOutputStream(), true); // find file File fileToSend
     * = new File(rootDir + "/" + fileName); if (fileToSend.exists() &&
     * !fileToSend.isDirectory()) { L.i(TAG, "start sending " + fileName);
     * out.println("HTTP/1.1 206 Partial Content");// ����Ӧ����Ϣ,������Ӧ��
     * //out.println("HTTP/1.1 200 OK"); //
     * out.println("Server: Apache-Coyote/1.1");
     * out.println("Accept-Ranges: bytes"); //
     * out.println("ETag: W/\"240-1387425011189\""); //
     * out.println("Last-Modified: Thu, 19 Dec 2013 03:50:11 GMT");
     * out.println("Content-Range: bytes 0-" + (fileToSend.length() - 1) + "/" +
     * fileToSend.length()); if (fileName.endsWith(".m3u8")) {
     * out.println("Content-Type: application/vnd.apple.mpegURL"); } // for .ts,
     * no content-type needed out.println("Content-Length: " +
     * fileToSend.length());// ���������ֽ��� //
     * out.println("Date: Thu, 19 Dec 2013 06:14:10 GMT");
     * out.println("Connection: close"); // complete header with a blank line
     * out.println();
     * 
     * FileInputStream fis = new FileInputStream(fileToSend); byte data[]; if
     * (fis.available() < MAXSIZE) { data = new byte[fis.available()]; } else {
     * data = new byte[MAXSIZE]; } int sentLength = 0; while (true) { int
     * bytesRead = fis.read(data); if (bytesRead <= 0) { break; } else {
     * sentLength += bytesRead; out.write(data, 0, bytesRead); out.flush(); } }
     * L.i(TAG, "sent length " + sentLength); out.flush(); out.close();
     * fis.close(); } else { L.w(TAG, "file not found " + fileName);
     * out.println("HTTP/1.0 404 Not Found"); out.println(); out.flush();
     * out.close(); } } catch (Exception e) { L.w(TAG,
     * "error in file transmission " + e.getLocalizedMessage()); } }
     */
}
