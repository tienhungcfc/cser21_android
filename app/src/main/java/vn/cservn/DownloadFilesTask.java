package vn.cservn;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class DownloadFilesTask extends AsyncTask<String, String, String> {
    public App21 app21;
    private static final String shareNamme = "downloaded";
    private String getCache(String url) {
        MainActivity m = (MainActivity) app21.mContext;
        SharedPreferences s = m.getShared(shareNamme);
        return s.getString(url, null);

    }
    private void clearCache(String key) {
        MainActivity m = (MainActivity) app21.mContext;
        SharedPreferences s = m.getShared(shareNamme);
        SharedPreferences.Editor editor = s.edit();
        if (key == null) editor.clear();
        else
            editor.remove(key);
        editor.apply();

    }
    private void setCache(String url, String localPath) {
        MainActivity m = (MainActivity) app21.mContext;
        SharedPreferences s = m.getShared(shareNamme);
        SharedPreferences.Editor editor = s.edit();
        editor.putString(url, localPath);
        editor.apply();

    }
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }
    protected void onPostExecute(String localPath) {
        //showDialog("Downloaded " + result + " bytes");
    }
    public void clear(final String file, final Runnable callback) {
        final boolean delete_all = file == null || "".equals(file);
        String _fname = getCache(file); // file: có thể là url
        if (_fname == null) _fname = file;
        else {
            clearCache(file);
        }
        final String fname = _fname;
        new Runnable() {
            @Override
            public void run() {

                ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
                File directory = cw.getDir("profile", Context.MODE_PRIVATE);
                if (!directory.exists()) {
                    callback.run();
                    return;
                }

                File[] fs = directory.listFiles();


                for (File f : fs) {
                    try {
                        String[] Segs = fname.split("/");
                        String last = Segs[Segs.length - 1];
                        if (delete_all || f.getName() == last) {
                            boolean deleted = f.delete();
                        }
                    } catch (Exception ex) {

                    }
                }
                if (delete_all) clearCache(null);
                callback.run();
            }
        }.run();

    }
    @Override
    protected String doInBackground(String... addresses) {
        InputStream in = null;
        String localPath = null;
        try {
            for (String address : addresses) {

                localPath = getCache(address);
                if (localPath != null && !"".equals(localPath)) break;
                ;

                // 1. Declare a URL Connection
                URL url = new URL(address);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // 2. Open InputStream to connection
                conn.connect();
                in = conn.getInputStream();

                String[] segs = address.split("\\?")[0].split("/");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
                Date date = new Date(System.currentTimeMillis());
                String fnasme = formatter.format(date) + "-" + segs[segs.length - 1];
                ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
                File directory = cw.getDir("profile", Context.MODE_PRIVATE);
                if (!directory.exists()) {
                    directory.mkdir();
                }


                // 3. Download and decode the bitmap using BitmapFactory
                File file = new File(directory, fnasme);
                try (OutputStream output = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = in.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }

                    output.flush();
                    localPath = file.getAbsolutePath();
                    setCache(address, localPath);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return localPath;
    }
    public List<FileInfo> getlist() {

        String[] lst = new String[]{};
        ContextWrapper cw = new ContextWrapper(app21.mContext.getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        List<FileInfo> arr = new ArrayList<FileInfo>();
        if (directory.exists()) {

            for (File f : directory.listFiles()) {
                FileInfo x = new FileInfo();
                x.name = f.getName();
                x.len = f.length();
                x.abspath = f.getAbsolutePath();
                try {
                    BasicFileAttributes attr = Files.readAttributes(Paths.get(x.abspath), BasicFileAttributes.class);
                    FileTime c = attr.creationTime();
                    FileTime l = attr.lastAccessTime();
                    x.create = new Date(c.toMillis());
                    x.last = new Date(l.toMillis());

                } catch (IOException ex) {
                    // handle exception
                }
                arr.add(x);
            }
        }


        return arr;
    }

    public static String strBase64(String input){
        try{
            byte[] data = input.getBytes();
            return  Base64.getUrlEncoder().encodeToString(data);
        }catch (Exception ex){
            return "";
        }
    }

    public String toBase64(String localPath){

        try{
            File file = new File(localPath);
            InputStream finput = new FileInputStream(file);
            byte[] fileBytes = new byte[(int)file.length()];
            finput.read(fileBytes, 0, fileBytes.length);
            finput.close();
            String imageStr = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                imageStr = Base64.getUrlEncoder().encodeToString(fileBytes);
            }
            return  imageStr;
        }catch (Exception ex){
            return  null;
        }
    }

    class FileInfo {

        public String name;

        public Date create;

        public Date last;
        public long len;
        public String abspath;
    }
}
