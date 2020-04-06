package vn.cservn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    public DownloadFilesTask downloadFilesTask;

    public void Rotate(String params, Callback21 callback21) {


        try {

            ImageUtilInfo imageUtilInfo = new Gson().fromJson(params, ImageUtilInfo.class);

            String imgPath = "";
            File file = downloadFilesTask.getFile(imageUtilInfo.path);
            Bitmap bitmap = downloadFilesTask.getBitmap(imageUtilInfo.path);
            // ExifInterface exif = new ExifInterface(imgPath);


            Matrix matrix = new Matrix();
            /*
             int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    break;
            }
            */
            matrix.postRotate(imageUtilInfo.degrees);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            FileOutputStream out = new FileOutputStream(file.getAbsoluteFile());

            String ext = downloadFilesTask.getExt(imageUtilInfo.path);
            switch (ext) {
                case "png":
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                    break;
                default://jpg
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                    break;
            }


            callback21.ok();
        } catch (Exception ex) {
            callback21.lastExp = ex;
            callback21.no();
        }
    }
}

class ImageUtilInfo {
    public String path;
    public float degrees;
}
