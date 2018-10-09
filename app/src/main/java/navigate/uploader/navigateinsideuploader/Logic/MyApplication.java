package navigate.uploader.navigateinsideuploader.Logic;

import android.app.Application;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.ArrayList;
import java.util.List;

import navigate.uploader.navigateinsideuploader.Logic.Listeners.BeaconListener;

/**
 * extension of the Application class to override system behaviour
 */
public class MyApplication extends Application {

    private BeaconManager beaconManager;
    private List<BeaconListener> listeners;
    private BeaconRegion region;

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize listeners list and beacon manger
        listeners = new ArrayList<>();
        beaconManager = new BeaconManager(getApplicationContext());
        //beaconManager.setBackgroundScanPeriod(1000,500);
        // set scan delay and ranging behaviour
        beaconManager.setForegroundScanPeriod(1000, 1000);
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                for (BeaconListener ltnr : listeners)
                    if(!beacons.isEmpty())
                        ltnr.onBeaconEvent(beacons.get(0));
            }
        });
        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> beacons) {
                for (BeaconListener ltnr : listeners)
                    if(!beacons.isEmpty())
                        ltnr.onBeaconEvent(beacons.get(0));

            }
            @Override
            public void onExitedRegion(BeaconRegion region) { }
        });

    }

    public void startRanging(){
        // create ranging region
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                region = new BeaconRegion(
                        "ranged region",
                        null,
                        null, null);
                // beaconManager.startMonitoring(region);
                // ranged region
                beaconManager.startRanging(region);
            }
        });
    }

    public void stopRanging(){
        if (region != null)
            beaconManager.stopRanging(region);
        // beaconManager.stopMonitoring(region.getIdentifier());
    }

    public void registerListener(BeaconListener listener){
        if (!listeners.contains(listener)){
            listeners.add(listener);
        }
    }

    public void unRegisterListener(BeaconListener listener){
        listeners.remove(listener);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        beaconManager.disconnect();
    }
}
