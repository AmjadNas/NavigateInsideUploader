package navigate.uploader.navigateinsideuploader.Objects;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.io.Serializable;
import java.util.UUID;

public class BeaconID implements Serializable{

    private UUID proximityUUID;
    private int major;
    private int minor;

    public BeaconID(UUID proximityUUID, int major, int minor) {
        this.proximityUUID = proximityUUID;
        this.major = major;
        this.minor = minor;
    }

    public BeaconID(String UUIDString, int major, int minor) {
        this(UUID.fromString(UUIDString), major, minor);
    }

    public UUID getProximityUUID() {
        return proximityUUID;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public BeaconRegion toBeaconRegion() {
        return new BeaconRegion(toString(), getProximityUUID(), getMajor(), getMinor());
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
        if (o == null) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (getClass() != o.getClass()) {
            return super.equals(o);
        }

        BeaconID other = (BeaconID) o;

        return getProximityUUID().equals(other.getProximityUUID())
                && getMajor() == other.getMajor()
                && getMinor() == other.getMinor();
    }

    public static BeaconID from(String bid) {
        String[] id = bid.split(":");
        return  new BeaconID(UUID.fromString(id[0]), Integer.parseInt(id[1]), Integer.parseInt(id[2]));
    }
}
