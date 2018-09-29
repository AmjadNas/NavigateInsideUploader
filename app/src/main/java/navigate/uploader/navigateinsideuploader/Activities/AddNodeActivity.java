package navigate.uploader.navigateinsideuploader.Activities;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.recognition.packets.Beacon;

import org.json.JSONObject;

import navigate.uploader.navigateinsideuploader.Logic.Listeners.BeaconListener;
import navigate.uploader.navigateinsideuploader.Logic.MyApplication;
import navigate.uploader.navigateinsideuploader.Logic.SysData;
import navigate.uploader.navigateinsideuploader.Network.NetworkConnector;
import navigate.uploader.navigateinsideuploader.Network.NetworkResListener;
import navigate.uploader.navigateinsideuploader.Network.ResStatus;
import navigate.uploader.navigateinsideuploader.Objects.BeaconID;
import navigate.uploader.navigateinsideuploader.Objects.Node;
import navigate.uploader.navigateinsideuploader.R;
import navigate.uploader.navigateinsideuploader.Utills.Constants;

public class AddNodeActivity extends AppCompatActivity implements BeaconListener, NetworkResListener {

    private TextView major, floar, building,minor;
    private CheckBox elevator, junction, outside;
    private SysData data;
    private BeaconID currntID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_node);
        data = SysData.getInstance();

        elevator = (CheckBox)findViewById(R.id.elevator);
        junction = (CheckBox)findViewById(R.id.junction);
        outside = (CheckBox)findViewById(R.id.outside);

        floar = (TextView)findViewById(R.id.edit_node_floor);
        building = (TextView)findViewById(R.id.edit_node_building);
        minor = (TextView)findViewById(R.id.edit_node_minor);
        major = (TextView)findViewById(R.id.edit_node_major);
    }

    public void SaveNode(View view) {
        Toast.makeText(this, "Uploading node", Toast.LENGTH_SHORT).show();
        Node n;

        if(!major.getEditableText().toString().isEmpty() && !minor.getEditableText().toString().isEmpty()) {
            String mjr = major.getEditableText().toString();
            String mnr = minor.getEditableText().toString();
            currntID = new BeaconID(Constants.DEFULTUID, mjr, mnr);
        }
        if (currntID != null) {
            n = new Node(currntID, junction.isChecked(), elevator.isChecked(), building.getEditableText().toString(), floar.getEditableText().toString());
            n.setOutside(outside.isChecked());
            n.setDirection(0);
            NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.INSERT_NODE, n, this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister beacon listeners
        ((MyApplication)getApplication()).stopRanging();
        ((MyApplication)getApplication()).unRegisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        // register beacon listener
        ((MyApplication)getApplication()).registerListener(this);
        ((MyApplication)getApplication()).startRanging();
    }

    @Override
    public void onBeaconEvent(Beacon beacon) {
        BeaconID tmp = new BeaconID(beacon.getProximityUUID(), String.valueOf(beacon.getMajor()), String.valueOf(beacon.getMinor()));
        if (!tmp.equals(currntID))
            currntID = tmp;

    }

    @Override
    public void onPreUpdate(String str) {

    }

    @Override
    public void onPostUpdate(JSONObject res, ResStatus status) {
        if (status == ResStatus.SUCCESS){
            if(data.saveNode(currntID,
                    floar.getEditableText().toString(), building.getEditableText().toString(),
                    junction.isChecked(), elevator.isChecked(), outside.isChecked(), 0))
                finish();
            else
                Toast.makeText(this, "Couldn't save node", Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(this, "Couldn't upload node", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostUpdate(Bitmap res, ResStatus status) {

    }
}
