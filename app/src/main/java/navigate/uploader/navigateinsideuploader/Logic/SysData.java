package navigate.uploader.navigateinsideuploader.Logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;

import java.util.ArrayList;
import java.util.UUID;

import navigate.uploader.navigateinsideuploader.Network.NetworkConnector;
import navigate.uploader.navigateinsideuploader.Objects.BeaconID;
import navigate.uploader.navigateinsideuploader.Objects.Node;
import navigate.uploader.navigateinsideuploader.Objects.Room;
import navigate.uploader.navigateinsideuploader.Utills.Constants;

public class SysData {
    //Its should contain Nodes List
    private static SysData instance = null;
    private ArrayList<Node> AllNodes;
    private DataBase db;

    private SysData(){
        AllNodes = new ArrayList<>();
    }

    public static SysData getInstance(){
        if(instance == null){
            instance = new SysData();
        }
        return instance;
    }

    public ArrayList<Node> getAllNodes(){
        return AllNodes;
    }

    public BeaconID getNodeIdByRoom(String room){
        for(Node n : AllNodes)
            for (Room m : n.getRooms()) {
                if(room.equals(m.GetRoomName()) || room.equals(m.GetRoomNum())){
                    return n.get_id();
                }
            }
        return null;
    }


    public void initDatBase(Context context){
        db = new DataBase(context);
    }

    public void closeDatabase(){
        if(db != null)
            db.close();
    }

    public Node getNodeByBeaconID(BeaconID bid) {
        for (Node node : AllNodes)
            if (bid.equals(node.get_id()))
                return node;

        return null;

    }

    /*public Bitmap getImageForNode(BeaconID id) {
        Bitmap img = db.getNodeImage(id.toString());

        return img;
    }*/
    public void InitializeData(){
        db.getNodes(AllNodes);


    }
    public boolean saveNode(BeaconID bid,  String floar, String building, boolean junction, boolean Elevator, boolean outside,int dir) {
        Node node = new Node(bid,junction, Elevator, building, floar);
        node.setOutside(outside);
        node.setDirection(dir);

            return AllNodes.add(node);


    }

    public boolean linkNodes(String s1, String s2, int direction, boolean isdirect) {
        Node node1 = getNodeByBeaconID(BeaconID.from(s1));
        Node node2 = getNodeByBeaconID(BeaconID.from(s2));
        int dir = (direction + 180) % 360;

            node1.AddNeighbour(new Pair<Node, Integer>(node2, direction));
            node2.AddNeighbour(new Pair<Node, Integer>(node1, dir));
            return true;


    }

    public boolean insertRoomToNode(String bid, String num, String nm) {

        Node node = getNodeByBeaconID(BeaconID.from(bid));

            node.AddRoom(new Room(num, nm));
            return true;

    }

    public void insertImageToDB(BeaconID currentBeacon,Bitmap res) {
        db.insertImage(currentBeacon, res);
    }

    public boolean insertNode(Node n) {



            return AllNodes.add(n);

    }
}

