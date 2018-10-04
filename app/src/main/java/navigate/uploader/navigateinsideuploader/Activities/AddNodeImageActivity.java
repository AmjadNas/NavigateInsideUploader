package navigate.uploader.navigateinsideuploader.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.recognition.packets.Beacon;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import navigate.uploader.navigateinsideuploader.Logic.Listeners.BeaconListener;
import navigate.uploader.navigateinsideuploader.Logic.MyApplication;
import navigate.uploader.navigateinsideuploader.Logic.SysData;
import navigate.uploader.navigateinsideuploader.Network.NetworkConnector;
import navigate.uploader.navigateinsideuploader.Network.NetworkResListener;
import navigate.uploader.navigateinsideuploader.Network.ResStatus;
import navigate.uploader.navigateinsideuploader.Objects.BeaconID;
import navigate.uploader.navigateinsideuploader.Objects.Node;
import navigate.uploader.navigateinsideuploader.Utills.Constants;
import navigate.uploader.navigateinsideuploader.Utills.Converter;
import navigate.uploader.navigateinsideuploader.R;

public class AddNodeImageActivity extends AppCompatActivity implements SensorEventListener, NetworkResListener {

    private final static int IMAGE_CAPTUE_REQ = 1;
    private static final int PICK_IMAGE = 111;
    private Bitmap img;
    private SysData data;
    private BeaconID currntID;
    private int minDir = -1;

    // device sensor manager
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TextView dirct;
    private Spinner nodes;
    private float[] rMat = new float[9];
    private float[] orientation = new float[3];
    // azimuth and current page position
    private int mAzimuth;

    private ImageView panoWidgetView;
    private Bitmap tmp;
    private Spinner node2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_node_image);
        data = SysData.getInstance();
        initSpinner();
        dirct = (TextView) findViewById(R.id.dir);
        panoWidgetView = (ImageView) findViewById(R.id.thumb_add_node);

        initSensor();
    }

    private void initSpinner() {
        nodes = (Spinner)findViewById(R.id.nodelist);
        node2 = (Spinner)findViewById(R.id.nodelist2);

        List<String> idList = new ArrayList<>();
        for (Node n : data.getAllNodes())
            idList.add(n.get_id().toString());

        nodes.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, idList));
        node2.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, idList));

    }

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


    private void initSensor(){
        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR );
    }

    /**
     * launch cam or get photo from gallery
     * @param view
     */
    public void onClickCam(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "select"), PICK_IMAGE);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            // get the azimuth value (orientation[0]) in degree
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            if (minDir < 0)
                dirct.setText(String.valueOf(mAzimuth));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

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
                    loadImageto3D(img);
                } catch (FileNotFoundException e) {
                    Log.e("ERROR:","Loading file failed");
                }
            }
        }
    }

    public void UploadImage(View view){
        if(img == null){
            Toast.makeText(this, "missing fields", Toast.LENGTH_SHORT).show();
        }else {
            String s1 = (String) nodes.getSelectedItem();
            String s2 = (String) node2.getSelectedItem();
            if (!s1.equals(s2))
                NetworkConnector.getInstance().pairNodes(Constants.DEFULTUID.toString()+":"+s1, Constants.DEFULTUID.toString()+":"+s2,img, mAzimuth, false, this);
            else
                Toast.makeText(this, "you can't add the same node as it's neighbour", Toast.LENGTH_SHORT).show();

        }
    }

    public void Rotatepic(View view) {

        Matrix m = new Matrix();
        m.postRotate(90);
        img = Bitmap.createBitmap(img,0,0,img.getWidth(),img.getHeight(),m,false);
        tmp = Bitmap.createBitmap(tmp,0,0,tmp.getWidth(),tmp.getHeight(),m,false);
        panoWidgetView.setImageBitmap(tmp);
    }

    @Override
    public void onPreUpdate(String str) {

    }

    @Override
    public void onPostUpdate(JSONObject res, ResStatus status) {
        if (status == ResStatus.SUCCESS){

        }else
            Toast.makeText(this, "Couldn't upload node", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPostUpdate(Bitmap res, ResStatus status) {

    }

    public void Record(View view) {

    }
}
