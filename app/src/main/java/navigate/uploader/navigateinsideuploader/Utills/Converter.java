package navigate.uploader.navigateinsideuploader.Utills;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * helper class for performing image conversion operations
 */
public final class Converter {

    private Converter(){}

    public static Bitmap decodeImage(byte[] arr){

        return BitmapFactory.decodeByteArray(arr, 0,arr.length);
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap, int quality){
        ByteArrayOutputStream bas;
        byte[] bArray;
        try {
            bas = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality ,bas);
            bArray = bas.toByteArray();
            bas.flush();
            bas.close();
        } catch (IOException e) {
            Log.e("IOException ",e.getMessage());
            return null;
        }
        return bArray;

    }

    public static Bitmap getImageTHumbnail(Bitmap img){
        return ThumbnailUtils.extractThumbnail(img,500,300);
    }

    public static Bitmap getImageTHumbnail(byte[] img){
        return ThumbnailUtils.extractThumbnail(decodeImage(img),500,300);
    }

    public static Bitmap getRotatedImage(Bitmap img, int degree){
        Matrix m = new Matrix();
        m.postRotate(degree);

         return Bitmap.createBitmap(img,0,0,img.getWidth(),img.getHeight(),m,false);
    }

}
