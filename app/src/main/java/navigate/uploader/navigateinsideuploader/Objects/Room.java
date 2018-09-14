package navigate.uploader.navigateinsideuploader.Objects;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import navigate.uploader.navigateinsideuploader.Utills.Constants;

public class Room {

    String roomNum;
    String roomName;


    public Room(String roomNum,String roomName){
        this.roomNum=roomNum;
        this.roomName=roomName;
    }

    public String GetRoomNum(){
        return roomNum;
    }
    public String GetRoomName(){
        return roomName;
    }

    @Override
    public String toString() {
        return "\nRoom number: "+ roomNum + "\nRoom name: " + roomName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        if (roomNum != null ? !roomNum.equals(room.roomNum) : room.roomNum != null) return false;
        return roomName != null ? roomName.equals(room.roomName) : room.roomName == null;

    }

    public static Room parseJson(JSONObject obj){
        Room room = null;
        try{

            String RoomNumber = obj.getString(Constants.NUMBER);
            String RoomName = obj.getString(Constants.NAME);

            room = new Room(RoomNumber,RoomName);

            return room;
        }catch (JSONException e){
            Log.e("Exception :", e.getMessage());
            return null;
        }
    }
    @Override
    public int hashCode() {
        int result = roomNum != null ? roomNum.hashCode() : 0;
        result = 31 * result + (roomName != null ? roomName.hashCode() : 0);
        return result;
    }
}
