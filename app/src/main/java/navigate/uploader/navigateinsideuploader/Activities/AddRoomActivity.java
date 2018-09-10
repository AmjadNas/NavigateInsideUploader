package navigate.uploader.navigateinsideuploader.Activities;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

import navigate.uploader.navigateinsideuploader.Logic.SysData;
import navigate.uploader.navigateinsideuploader.Network.NetworkConnector;
import navigate.uploader.navigateinsideuploader.Network.NetworkResListener;
import navigate.uploader.navigateinsideuploader.Network.ResStatus;
import navigate.uploader.navigateinsideuploader.Objects.Node;
import navigate.uploader.navigateinsideuploader.R;


public class AddRoomActivity extends AppCompatActivity implements NetworkResListener {
    private Spinner node1;
    private SysData data;
    private TextView name, number;
    private ListView lView;
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        node1 = (Spinner) findViewById(R.id.spinner1);
        data = SysData.getInstance();

        name = (TextView) findViewById(R.id.room_name_add);
        number = (TextView) findViewById(R.id.rom_num_add);

        lView = (ListView) findViewById(R.id.lView);

        ArrayList<String> strings = new ArrayList<>();

        for (Node node : data.getAllNodes()) {
            strings.add(node.get_id().toString());
        }

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        lView.setAdapter(arrayAdapter);

        node1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings));
    }

    public void addRoom(View view) {
        String nm = name.getEditableText().toString();
        String num = number.getEditableText().toString();
        String bid = (String)node1.getSelectedItem();

        NetworkConnector.getInstance().addRoomToNode(bid, nm, num, this);


    }

    public void exit(View view) {
        finish();
    }

    @Override
    public void onPreUpdate(String str) {

    }

    @Override
    public void onPostUpdate(JSONObject res, ResStatus status) {
        if (status == ResStatus.SUCCESS){
            String nm = name.getEditableText().toString();
            String num = number.getEditableText().toString();
            String bid = (String)node1.getSelectedItem();
            if(!data.insertRoomToNode(bid, num, nm)){
                Toast.makeText(this, "Could'nt add room to db", Toast.LENGTH_SHORT).show();
            }else
                fileList();
        }else
            Toast.makeText(this, "Could'nt upload room", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostUpdate(Bitmap res, ResStatus status) {

    }
}

