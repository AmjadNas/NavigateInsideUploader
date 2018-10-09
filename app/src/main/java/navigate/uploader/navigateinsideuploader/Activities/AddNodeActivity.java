package navigate.uploader.navigateinsideuploader.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.recognition.packets.Beacon;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;

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
import navigate.uploader.navigateinsideuploader.Utills.Converter;

public class AddNodeActivity extends AppCompatActivity implements BeaconListener, NetworkResListener {
    // image request types for picking or capturing
    private final static int IMAGE_CAPTUE_REQ = 1;
    private static final int PICK_IMAGE = 111;
    private TextView major, floar, building,minor;
    private CheckBox elevator, junction, outside;
    private SysData data;
    private BeaconID currntID;
    private Bitmap img, tmp;
    private ImageView panoWidgetView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_node);
        data = SysData.getInstance();
        // init views
        panoWidgetView = (ImageView) findViewById(R.id.thumb_add_node);

        elevator = (CheckBox)findViewById(R.id.elevator);
        junction = (CheckBox)findViewById(R.id.junction);
        outside = (CheckBox)findViewById(R.id.outside);

        floar = (TextView)findViewById(R.id.edit_node_floor);
        building = (TextView)findViewById(R.id.edit_node_building);
        minor = (TextView)findViewById(R.id.edit_node_minor);
        major = (TextView)findViewById(R.id.edit_node_major);
    }

    /**
     * handles the save click for node
     * @param view
     */
    public void SaveNode(View view) {
        Toast.makeText(this, "Uploading node", Toast.LENGTH_SHORT).show();
        Node n;
        // if the major and minor fields are filled ignore the detected beacon id
        // (if there was one) and make a new id
        if(!major.getEditableText().toString().isEmpty() && !minor.getEditableText().toString().isEmpty()) {
            String mjr = major.getEditableText().toString();
            String mnr = minor.getEditableText().toString();
            currntID = new BeaconID(Constants.DEFULTUID, mjr, mnr);
        }
        if (currntID != null) { // if beacon id wasn't null upload node
            n = new Node(currntID, junction.isChecked(), elevator.isChecked(), building.getEditableText().toString(), floar.getEditableText().toString());
            n.setOutside(outside.isChecked());
            n.setNessOutside(false);
            n.setDirection(0);
            n.setImage(img);
            NetworkConnector.getInstance().sendRequestToServer(NetworkConnector.INSERT_NODE, n, this);
        }

    }
    /**
     * launch cam or get photo from gallery
     * @param view
     */
    public void FetchImage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pickone)
                .setItems(R.array.choice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, getString(R.string.selectpick)), PICK_IMAGE);
                        }else if (which == 1){
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent,IMAGE_CAPTUE_REQ);
                        }
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
       /* Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select"), PICK_IMAGE);*/
    }

    /**
     * rotate the image
     * @param view
     */
    public void RotateImg(View view) {
        img = Converter.getRotatedImage(img, 90);
        tmp = Converter.getRotatedImage(tmp, 90);
        panoWidgetView.setImageBitmap(tmp);
    }

    /**
     * handle captured images
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if (requestCode == IMAGE_CAPTUE_REQ ) { // if image was captured from camera
                // restore photo from intent
                img = (Bitmap) data.getExtras().get("data");
            }else if (requestCode == PICK_IMAGE){  // if image was chosen from gallery

                Uri selectedImage = data.getData();
                InputStream imageStream = null;
                try {
                    // get image using content resolver
                    imageStream = getContentResolver().openInputStream(selectedImage);
                    img = BitmapFactory.decodeStream(imageStream);
                } catch (FileNotFoundException e) {
                    Log.e("ERROR:","Loading file failed");
                }
            }
            loadImageto3D(img);
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

    /**
     * handel on finding beacon event
     * @param beacon
     */
    @Override
    public void onBeaconEvent(Beacon beacon) {
        BeaconID tmp = new BeaconID(beacon.getProximityUUID(), String.valueOf(beacon.getMajor()), String.valueOf(beacon.getMinor()));
        if (!tmp.equals(currntID))
            currntID = tmp;

    }

    /**
     * helper method to load the image and display it on the screen
     * @param res
     */
    private void loadImageto3D(final Bitmap res) {
        new AsyncTask<Void, Void, byte[]>(){
            @Override
            protected void onPostExecute(byte[] aVoid) {
                tmp = Converter.getImageTHumbnail(aVoid);
                panoWidgetView.setImageBitmap(tmp);
                img = Converter.decodeImage(aVoid);
            }

            @Override
            protected byte[] doInBackground(Void... params) {

                return Converter.getBitmapAsByteArray(res, 100);
            }
        }.execute();

    }

    @Override
    public void onPreUpdate(String str) {

    }

    @Override
    public void onPostUpdate(JSONObject res, ResStatus status) {
        if (status == ResStatus.SUCCESS){
            // if the upload was successful add node to system list
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
