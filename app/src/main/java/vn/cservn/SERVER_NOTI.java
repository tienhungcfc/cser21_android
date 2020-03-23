package vn.cservn;

import com.google.android.gms.common.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SERVER_NOTI {
    public  void  run(final Result result, final Callback21 callback21){
        new Runnable() {
            @Override
            public void run() {

                try {
                    InputStream in = null;
                    URL url = new URL(result.params);
                    HttpURLConnection conn = null;
                    conn = (HttpURLConnection) url.openConnection();
                    // 2. Open InputStream to connection
                    conn.connect();
                    in = conn.getInputStream();
                    byte[] bytes = IOUtils.toByteArray(in);
                    String str = new String(bytes, "UTF-8");


                    result.success =true;

                } catch (IOException e) {
                    e.printStackTrace();
                    result.error = e.getMessage();
                    result.success =false;
                }
                callback21.result(result);

            }
        }.run();
    }

    public  void  run(){

    }

    class Data{
        List<Noti21> notis;
    }
}
