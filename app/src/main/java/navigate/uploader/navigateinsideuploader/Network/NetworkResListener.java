package navigate.uploader.navigateinsideuploader.Network;

import android.graphics.Bitmap;

import org.json.JSONObject;

/**
 * NetworkResListener interface
 */
public interface NetworkResListener {
    /**
     * callback method which called when the resources update is started
     */
    public void onPreUpdate(String str);

    public void onPostUpdate(JSONObject res, ResStatus status);

    public void onPostUpdate(Bitmap res, ResStatus status);
}
