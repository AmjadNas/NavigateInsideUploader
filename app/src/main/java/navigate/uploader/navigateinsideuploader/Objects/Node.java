package navigate.uploader.navigateinsideuploader.Objects;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.util.Pair;

import java.util.ArrayList;

import navigate.uploader.navigateinsideuploader.R;
import navigate.uploader.navigateinsideuploader.Utills.Constants;

public class Node {

    private int id;
    private BeaconID _id; // new id to be used
    private boolean Junction;
    private boolean Elevator;
    private String Building;
    private String Floor;
    private boolean Outside;
    private boolean NessOutside;
    private boolean Visited=false;
    private Pair<Node,Integer> Father=null;
    private ArrayList<Pair<Node,Integer>> Neighbours;
    private ArrayList<Room> rooms;
    private int direction;
    private String roomsRange;   // must be entered in format x:y (from room x to y)
    private Bitmap image = null;

    public Node(int id,boolean Junction,boolean Elevator,String Building,String Floor){
        this.id = id;
        this.Junction = Junction;
        this.Elevator = Elevator;
        this.Building = Building;
        this.Floor = Floor;
        Neighbours = new ArrayList<>();
        this.rooms = new ArrayList<>();
    }

    public Node(BeaconID _id,boolean Junction,boolean Elevator,String Building,String Floor){
        this._id = _id;
        this.Junction = Junction;
        this.Elevator = Elevator;
        this.Building = Building;
        this.Floor = Floor;
        Neighbours = new ArrayList<>();
        this.rooms = new ArrayList<>();
    }


    public BeaconID get_id() {
        return _id;
    }

    public void setRoomsRange(String roomsRange) {
        this.roomsRange = roomsRange;
    }

    public String getRoomsRange() {
        if (!rooms.isEmpty()){
            if (rooms.size() > 1){
                return "Rooms: " + rooms.get(0).GetRoomNum() + " - " + rooms.get(rooms.size()-1).GetRoomNum();
            }else{
                return "Room: " + rooms.get(0).GetRoomNum();
            }
        }

        return "No rooms";
    }

    public Bitmap getImage(){
        return image;
    }

    public void setImage(Bitmap image){
        this.image=image;
    }
    public int getDirection() {
        return direction;
    }

    public void setFather(Pair<Node,Integer> Father){
        this.Father=Father;
    }

    public Pair<Node,Integer> getFather() {
        return Father;
    }

    public ArrayList<Pair<Node,Integer>> getNeighbours() {

        return Neighbours;
    }


    public boolean isVisited() {
        return Visited;
    }

    public void setVisited(boolean visited) {
        Visited = visited;
    }

    public void SetFatherNull(){
        this.Father=null;
    }

    public void AddNeighbour(Pair<Node,Integer> Neighbour){
        if(!Neighbours.contains(Neighbour))
            Neighbours.add(Neighbour);
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isJunction() {
        return Junction;
    }

    public void setJunction(boolean junction) {
        Junction = junction;
    }

    public void setJunction(int i){
        if(i == 1 ){
            Junction=true;
        }
        if(i == 0){
            Junction=false;
        }
    }

    public boolean isElevator() {
        return Elevator;
    }

    public void setElevator(boolean elevator) {
        Elevator = elevator;
    }

    public void setElevator(int i){
        if(i == 1){
            Elevator=true;
        }
        if(i == 0){
            Elevator=false;
        }
    }

    public String getBuilding() {
        return Building;
    }

    public void setBuilding(String building) {
        Building = building;
    }

    public String getFloor() {
        return Floor;
    }

    public void setFloor(String floor) {
        Floor = floor;
    }

    public boolean isOutside() {
        return Outside;
    }

    public void setOutside(boolean outside) {
        Outside = outside;
    }

    public void setOutside(int i) {
        if(i == 1){
            Outside=true;
        }
        if(i == 0){
            Outside=false;
        }

    }

    public boolean isNessOutside() {
        return NessOutside;
    }

    public void setNessOutside(boolean nessOutside) {
        NessOutside = nessOutside;
    }
    public void setNessOutside(int i) {
        if(i == 1){
            NessOutside=true;
        }
        if(i == 0){
            NessOutside=false;
        }
    }

    public ArrayList<Room> getRooms(){
        return rooms;
    }

    public void AddRoom(Room e){
        rooms.add(e);
    }

    public void DeleteRoom(Room e){
        rooms.remove(e);
    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return _id.equals(node._id);

    }

    public String toNameString(){
        return "Building :"+ getBuilding() + " floor"+ getFloor();
    }

    public String toRoomsString(){
        StringBuilder sb = new StringBuilder();
        for (Room r : rooms){
            sb.append(r);
        }
        return "Building :"+ getBuilding() + " floor"+ getFloor() +
                "\nAvailable Rooms :" + sb.toString();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (rooms.isEmpty()){
            if (isJunction()){

                return "Building :"+ getBuilding() + " floor"+ getFloor() + " stairs";

            }else if (isElevator()){
                return "Building :"+ getBuilding() + " floor"+ getFloor() +  " elevator";
            }else
                return toNameString();

        }else{
            return toRoomsString();
        }

    }

    public void setDirection(int d) {

        direction = d;
    }


    public static ContentValues getContentValues(Node node) {
        ContentValues cv = new ContentValues();

        cv.put(Constants.BEACONID, node.get_id().toString());
        cv.put(Constants.Junction, node.isJunction());
        cv.put(Constants.Elevator, node.isElevator());
        cv.put(Constants.Building, node.getBuilding());
        cv.put(Constants.Floor, node.getFloor());
        cv.put(Constants.Outside, node.isOutside());
        cv.put(Constants.NessOutside, node.isNessOutside());
        cv.put(Constants.Direction, node.getDirection());

        return cv;

    }
}
