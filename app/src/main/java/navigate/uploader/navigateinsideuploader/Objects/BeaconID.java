package navigate.uploader.navigateinsideuploader.Objects;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.io.Serializable;
import java.util.UUID;

public class BeaconID implements Serializable{

    private UUID proximityUUID;
    private String major;
    private String minor;

    public BeaconID(UUID proximityUUID, String major, String minor) {
        this.proximityUUID = proximityUUID;
        this.major = major;
        this.minor = minor;
    }

    public BeaconID(String UUIDString, String major, String minor) {
        this(UUID.fromString(UUIDString), major, minor);
    }

    public UUID getProximityUUID() {
        return proximityUUID;
    }

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }


    public String toString() {
        return getProximityUUID().toString() + ":" + getMajor() + ":" + getMinor();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeaconID beaconID = (BeaconID) o;

        if (!proximityUUID.equals(beaconID.proximityUUID)) return false;
        if (!major.equals(beaconID.major)) return false;
        return minor.equals(beaconID.minor);
    }

    public static BeaconID from(String bid) {
        String[] id = bid.split(":");
        return  new BeaconID(UUID.fromString(id[0]), id[1], id[2]);
    }
}
