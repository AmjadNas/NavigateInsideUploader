package navigate.uploader.navigateinsideuploader.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import navigate.uploader.navigateinsideuploader.Logic.SysData;
import navigate.uploader.navigateinsideuploader.Network.NetworkConnector;
import navigate.uploader.navigateinsideuploader.Network.NetworkResListener;
import navigate.uploader.navigateinsideuploader.Network.ResStatus;
import navigate.uploader.navigateinsideuploader.Objects.Node;
import navigate.uploader.navigateinsideuploader.Objects.Room;
import navigate.uploader.navigateinsideuploader.R;
import navigate.uploader.navigateinsideuploader.Utills.Constants;

public class AddStuff extends AppCompatActivity implements NetworkResListener {
    
    private Button add, relate, addroom;
    private SysData data;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stuff);

        NetworkConnector.getInstance().initialize(getApplicationContext());
        data = SysData.getInstance();
        data.initDatBase(getApplicationContext());

        NetworkConnector.getInstance().update(this);

        initView();
    }

    private void initView(){

        addroom = (Button) findViewById(R.id.addRoom_btn);
        add = (Button) findViewById(R.id.addnode);
        relate = (Button) findViewById(R.id.relaetnode);

        addroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(AddStuff.this, AddRoomActivity.class);
                startActivity(intent);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddStuff.this, AddNodeActivity.class);
                startActivity(intent);
            }
        });
        relate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddStuff.this, ADNEBERACtivity.class);
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onStop() {
        super.onStop();
        data.closeDatabase();
    }

    @Override
    public void onPreUpdate(String str) {

    }

    @Override
    public void onPostUpdate(JSONObject res, ResStatus status) {
        if(status == ResStatus.SUCCESS){
            try {
                JSONArray arr = res.getJSONArray(Constants.Node), nbers, rooms;
                JSONObject o, nbr;
                Node n;

                for(int i = 0; i < arr.length(); i++){
                    o = arr.getJSONObject(i);
                    n = Node.parseJson(o);
                    if(n != null)
                        SysData.getInstance().insertNode(n);


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onPostUpdate(Bitmap res, ResStatus status) {

    }
}

