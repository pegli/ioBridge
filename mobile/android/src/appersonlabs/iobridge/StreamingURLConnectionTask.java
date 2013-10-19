package appersonlabs.iobridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class StreamingURLConnectionTask extends AsyncTask<String, Void, Void> {

    private static final String LCAT   = "StreamingURLConnectionTask";

    private KrollProxy          eventSink;

    private Socket              socket = null;

    public StreamingURLConnectionTask(KrollProxy eventSink) {
        this.eventSink = eventSink;
    }

    private boolean cancelled;

    public void cancel() {
        closeSocket();
    }

    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            }
            catch (IOException e) {
                Log.e(LCAT, e.getMessage());
            }
        }
    }

    @Override
    protected Void doInBackground(String... args) {
        try {
            URL url = new URL(args[0]);
            int port = url.getPort();

            socket = new Socket(url.getHost(), port > 1 ? port : 80);
            Log.i(LCAT, "socket connected? " + socket.isConnected());

            // send HTTP request
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.format("GET %s?%s\n", url.getPath(), url.getQuery());
            out.format("User-Agent: ioBridge Module/1.0\n");
            out.format("Host: %s\n", url.getHost());
            out.format("Accept: */*\n");
            out.print("\n\n");
            out.flush();

            // read response stream
            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];
            while (!cancelled) {
                int count = in.read(buffer);
                if (count == -1) break;

                try {
                    JSONObject obj = new JSONObject(new String(buffer, 0, count));
                    eventSink.fireEvent("stream", obj);
                }
                catch (JSONException e) {
                    Log.w(LCAT, "JSON parse error: " + e.getMessage());
                }
            }
            Log.i(LCAT, "DONE");
        }
        catch (MalformedURLException e) {
            Log.e(LCAT, e.getMessage());
        }
        catch (UnknownHostException e) {
            Log.e(LCAT, e.getMessage());
        }
        catch (IOException e) {
            Log.e(LCAT, e.getMessage());
        }
        finally {
            closeSocket();
        }

        /*
         * HttpURLConnection conn = null; try { URL url = new URL(args[0]); conn
         * = (HttpURLConnection) url.openConnection();
         * 
         * Log.i(LCAT, "opened connection");
         * 
         * BufferedReader reader = new BufferedReader(new
         * InputStreamReader(conn.getInputStream())); String line; while ((line
         * = reader.readLine()) != null) { Log.i(LCAT, "LINE: " + line); }
         * 
         * Log.i(LCAT, "finished reading connection"); } catch
         * (MalformedURLException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
         * catch block e.printStackTrace(); } finally { if (conn != null) {
         * conn.disconnect(); Log.i(LCAT, "closed connection"); } }
         */
        return null;
    }

}
