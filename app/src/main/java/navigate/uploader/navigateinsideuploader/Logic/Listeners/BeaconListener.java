package navigate.uploader.navigateinsideuploader.Logic.Listeners;

import com.estimote.coresdk.recognition.packets.Beacon;
/**
 * an interface designed to enable the activities to interact with the events of finding beacons
 */
public interface BeaconListener {

    void onBeaconEvent(Beacon beacon);
}
