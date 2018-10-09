package navigate.uploader.navigateinsideuploader.Utills;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import navigate.uploader.navigateinsideuploader.Logic.Listeners.ImageLoadedListener;
import navigate.uploader.navigateinsideuploader.Logic.SysData;
import navigate.uploader.navigateinsideuploader.Objects.BeaconID;
/**
 * class for loading images on a new thread
 */
public class ImageLoader extends AsyncTask<Bitmap,Bitmap,Bitmap> {
    private boolean downloaded;
    private ImageLoadedListener imageLoadedListener;
    private BeaconID currentID;

    public ImageLoader(BeaconID currentID, ImageLoadedListener imageLoadedListener, boolean downloaded){
        this.downloaded = downloaded;
        this.imageLoadedListener = imageLoadedListener;
        this.currentID = currentID;
    }
    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {

        return Converter.getImageTHumbnail(bitmaps[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageLoadedListener.onImageLoaded(bitmap);
    }
}
