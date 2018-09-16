package navigate.uploader.navigateinsideuploader.Logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;

import com.estimote.mgmtsdk.feature.settings.api.Eddystone;

import java.util.ArrayList;
import java.util.UUID;

import navigate.uploader.navigateinsideuploader.Objects.BeaconID;
import navigate.uploader.navigateinsideuploader.Objects.Node;
import navigate.uploader.navigateinsideuploader.Objects.Room;
import navigate.uploader.navigateinsideuploader.R;
import navigate.uploader.navigateinsideuploader.Utills.Constants;
import navigate.uploader.navigateinsideuploader.Utills.Converter;


public class DataBase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = String.valueOf(R.string.app_name);

    private static final String SQL_CREATE_NODE_TABLE = "CREATE TABLE "+ Constants.Node + " ("+
            Constants.BEACONID + " VARCHAR(100) PRIMARY KEY, "+   // beaconid with format UID:Major:Minor for
            Constants.Junction + " BOOLEAN, " +                   // example fadsfasdf-afda-dasffdfd:1555:54654
            Constants.Elevator + " BOOLEAN, "+
            Constants.Building + " VARCHAR(25), "+
            Constants.Floor + " VARCHAR(25), " +
            Constants.Outside + " BOOLEAN, "+
            Constants.NessOutside + " BOOLEAN, "+
            Constants.Direction + " INTEGER, " +
            Constants.Image +" BLOB " +
            ")";

    private static final String SQL_CREATE_RELATION_TABLE = "CREATE TABLE " + Constants.Relation +" ("+
            Constants.FirstID + " VARCHAR(100), "+
            Constants.SecondID + " VARCHAR(100), "+
            Constants.Direction + " INTEGER, "+
            Constants.DIRECT + " BOOLEAN, "+
            "FOREIGN KEY (" + Constants.FirstID + ") REFERENCES " + Constants.Node + " (" + Constants.BEACONID + "), "+
            "FOREIGN KEY (" + Constants.SecondID + ") REFERENCES " + Constants.Node +" (" + Constants.BEACONID + "), " +
            "CONSTRAINT PK1 PRIMARY KEY (" + Constants.FirstID + "," + Constants.SecondID + ")" +
            " )";

    private static final String SQL_CREATE_ROOMS_TABLE = "CREATE TABLE " + Constants.Room +" ("+
            Constants.BEACONID + " VARCHAR(100), "+
            Constants.NUMBER + " VARCHAR(12), "+
            Constants.NAME + " VARCHAR(50), "+
            "FOREIGN KEY (" + Constants.BEACONID + ") REFERENCES " + Constants.Node + " (" + Constants.BEACONID + "), "+
            "CONSTRAINT PK2 PRIMARY KEY (" + Constants.BEACONID + "," + Constants.NUMBER + "," + Constants.NAME +")" +
            " )";


    public DataBase(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NODE_TABLE);
        db.execSQL(SQL_CREATE_RELATION_TABLE);
        db.execSQL(SQL_CREATE_ROOMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public boolean insertRoom(String bid, String name, String number){
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(Constants.BEACONID, bid);
            cv.put(Constants.NAME, name);
            cv.put(Constants.NUMBER, number);

            long i = db.insert(Constants.Room, null, cv);

            db.close();

            return i >= 0;
        }catch (Throwable th){
            th.printStackTrace();
            return false;
        }
    }

    public void getNodeRooms(Node n){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                Constants.NAME,
                Constants.NUMBER
        };

        Cursor r = db.query(Constants.Room, projection,Constants.BEACONID + " = ?", new String[]{n.get_id().toString()},null,null,null);
        Room rm;
        while(r.moveToNext()){

            rm = new Room(r.getString(1), r.getString(0));
            n.AddRoom(rm);

        }
        r.close();
        db.close();
    }

    public void getNodes(ArrayList<Node> nodes){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                Constants.BEACONID,
                Constants.Junction,
                Constants.Elevator,
                Constants.Building,
                Constants.Floor,
                Constants.Outside,
                Constants.NessOutside,
                Constants.Direction,
        };

        Cursor r = db.query(Constants.Node,projection,null,null,null,null,null);
        String[] beaconID;
        BeaconID Id;
        int major, minor;
        while(r.moveToNext()){
            beaconID = r.getString(0).split(":");

            major = Integer.parseInt(beaconID[1]);
            minor = Integer.parseInt(beaconID[2]);

            Id = new BeaconID(UUID.fromString(beaconID[0]), String.valueOf(major), String.valueOf(minor));

            Node n = new Node(Id, false, false, r.getString(3), r.getString(4));
            n.setJunction(r.getInt(1));
            n.setElevator(r.getInt(2));
            n.setOutside(r.getInt(5));
            n.setNessOutside(r.getInt(6));
            n.setDirection(r.getInt(7));
            getNodeRooms(n);

            nodes.add(n);

        }
        r.close();
        db.close();

        getNodesRelation(nodes);

    }

    public void getNodesRelation(ArrayList<Node> nodes){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { Constants.SecondID, Constants.Direction, Constants.DIRECT };

        Cursor r = null;
        String[] beaconID;
        BeaconID Id;
        int major, minor;
        int dir;

        for(Node n : nodes) {
            r = db.query(Constants.Relation, projection, Constants.FirstID + " = ?", new String[]{n.get_id().toString()}, null, null, null);
            while (r.moveToNext()) {
                beaconID = r.getString(0).split(":");
                major = Integer.parseInt(beaconID[1]);
                minor = Integer.parseInt(beaconID[2]);
                Id = new BeaconID(UUID.fromString(beaconID[0]), String.valueOf(major), String.valueOf(minor));

                for (Node nb : nodes)
                    if (nb.get_id().equals(Id) && !nb.equals(n)) {
                        dir = (r.getInt(1) + 180) % 360;
                        // if(r.getInt(2) == 1)
                        nb.AddNeighbour(new Pair<Node, Integer>(n, dir));
                        // else
                        //   nb.AddNeighbour(new Pair<Node, Integer>(n, nb.getDirection()));
                        n.AddNeighbour(new Pair<Node, Integer>(nb, r.getInt(1)));

                        break;
                    }
            }
            r.close();
        }
        db.close();
    }

    public Bitmap getNodeImage(String s) {
        if(s != null){
            SQLiteDatabase sq = getReadableDatabase();
            Cursor c = sq.query(Constants.Node, new String[]{Constants.Image}, Constants.BEACONID + " = ?", new String[]{s}, null, null, null);
            byte[] arr;
            Bitmap btm = null;

            while (c.moveToNext()){
                if (!c.isNull(0)) {
                    arr = c.getBlob(0);
                    btm = Converter.decodeImage(arr);
                }
            }

            c.close();
            sq.close();
            return btm;
        }
        return null;
    }

    public boolean insertNode(ContentValues contentValues) {
        try {
            SQLiteDatabase db = getWritableDatabase();

            long i = db.insert(Constants.Node, null, contentValues);

            db.close();

            return i >= 0;
        }catch (Throwable th){
            th.printStackTrace();
            return false;
        }
    }

    public void insertImage(BeaconID bid, Bitmap img) {

        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(Constants.Image, Converter.getBitmapAsByteArray(img, 100));
            db.update(Constants.Node, cv, Constants.BEACONID + " = ?", new String[]{bid.toString()});


            db.close();
        }catch (Throwable th){
            th.printStackTrace();
        }
    }

    public boolean insertRelation(String s1, String s2, int direction, boolean isdirect) {

        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(Constants.FirstID, s1);
            cv.put(Constants.SecondID, s2);
            cv.put(Constants.Direction, direction);
            cv.put(Constants.DIRECT, isdirect);

            long i = db.insert(Constants.Relation, null, cv);

            db.close();

            return i >= 0;
        }catch (Throwable th){
            th.printStackTrace();
            return false;
        }
    }
}
