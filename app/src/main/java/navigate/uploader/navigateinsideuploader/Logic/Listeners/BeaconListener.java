package navigate.uploader.navigateinsideuploader.Logic.Listeners;

import com.estimote.coresdk.recognition.packets.Beacon;

public interface BeaconListener {

    void onBeaconEvent(Beacon beacon);
}
