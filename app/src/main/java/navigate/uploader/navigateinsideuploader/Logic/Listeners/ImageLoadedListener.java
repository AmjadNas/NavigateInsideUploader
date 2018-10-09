package navigate.uploader.navigateinsideuploader.Logic.Listeners;

import android.graphics.Bitmap;
/**
 * an interface that helps the activities to handle image loading events
 */
public interface ImageLoadedListener {
    void onImageLoaded(Bitmap image);
}
