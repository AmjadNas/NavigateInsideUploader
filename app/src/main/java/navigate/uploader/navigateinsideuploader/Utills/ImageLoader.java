package navigate.uploader.navigateinsideuploader.Utills;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import navigate.uploader.navigateinsideuploader.Logic.Listeners.ImageLoadedListener;
import navigate.uploader.navigateinsideuploader.Logic.SysData;
import navigate.uploader.navigateinsideuploader.Objects.BeaconID;

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
        if (downloaded)
            SysData.getInstance().insertImageToDB(currentID, bitmaps[0]);

        return Converter.getImageTHumbnail(bitmaps[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageLoadedListener.onImageLoaded(bitmap);
    }
}
