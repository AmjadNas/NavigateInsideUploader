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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.recognition.packets.Beacon;

import java.io.FileNotFoundException;
import java.io.InputStream;

import navigate.uploader.navigateinsideuploader.Logic.Listeners.BeaconListener;
import navigate.uploader.navigateinsideuploader.Logic.MyApplication;
import navigate.uploader.navigateinsideuploader.Logic.SysData;
import navigate.uploader.navigateinsideuploader.Objects.BeaconID;
import navigate.uploader.navigateinsideuploader.Utills.Constants;
import navigate.uploader.navigateinsideuploader.Utills.Converter;
import navigate.uploader.navigateinsideuploader.R;

public class AddNodeActivity extends AppCompatActivity implements BeaconListener, SensorEventListener{

    private final static int IMAGE_CAPTUE_REQ = 1;
    private static final int PICK_IMAGE = 111;
    private TextView major, floar, building,minor;
    private CheckBox elevator, junction, outside;
    private Bitmap img;
    private SysData data;
    private BeaconID currntID;

    // device sensor manager
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] rMat = new float[9];
    private float[] orientation = new float[3];
    // azimuth and current page position
    private int mAzimuth;

    private ImageView panoWidgetView;
    private Bitmap tmp;

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

        panoWidgetView = (ImageView) findViewById(R.id.thumb_add_node);



        initSensor();
    }

    private void loadImageto3D(final Bitmap res) {
        new AsyncTask<Void, Void, Bitmap>(){
            @Override
            protected void onPostExecute(Bitmap aVoid) {
                tmp = Converter.getImageTHumbnail(aVoid);
                panoWidgetView.setImageBitmap(tmp);
                img = aVoid;
            }

            @Override
            protected Bitmap doInBackground(Void... params) {

                return Converter.compreesBitmap(res);
            }
        }.execute();

    }

    /*
    public static String saveImage(String id, Bitmap bitmap){
        try{
            String name = id+".png";
            String fDir = Environment.getExternalStorageDirectory().toString();
            File file = new File(fDir+"/saved_images");
            file.mkdir();

            File dir = new File(file, name);

            FileOutputStream fos = new FileOutputStream(dir);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);

            fos.flush();
            fos.close();

            return dir.getAbsolutePath();
        }catch (Exception e){
            Log.e("Exception ",e.getMessage());
            return null;
        }

    }*/

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
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        // register beacon listener
        ((MyApplication)getApplication()).registerListener(this);
        ((MyApplication)getApplication()).startRanging();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister beacon listeners
        ((MyApplication)getApplication()).stopRanging();
        ((MyApplication)getApplication()).unRegisterListener(this);
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

    public void SaveNode(View view) {

        if(!major.getEditableText().toString().isEmpty() && !minor.getEditableText().toString().isEmpty()){
            int mjr = Integer.parseInt(major.getEditableText().toString());
            int mnr = Integer.parseInt(minor.getEditableText().toString());
            BeaconID id = new BeaconID(Constants.DEFULTUID,mjr, mnr);

            data.saveNode(id,
                    floar.getEditableText().toString(), building.getEditableText().toString(),
                    junction.isChecked(), elevator.isChecked(), outside.isChecked(), img, mAzimuth);
        }else {
            data.saveNode(currntID,
                    floar.getEditableText().toString(), building.getEditableText().toString(),
                    junction.isChecked(), elevator.isChecked(), outside.isChecked(), img, mAzimuth);
        }

        finish();


    }


    @Override
    public void onBeaconEvent(Beacon beacon) {
        currntID = new BeaconID(beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor());

    }

    public void Rotatepic(View view) {

        Matrix m = new Matrix();
        m.postRotate(90);
        img = Bitmap.createBitmap(img,0,0,img.getWidth(),img.getHeight(),m,false);
        tmp = Bitmap.createBitmap(tmp,0,0,tmp.getWidth(),tmp.getHeight(),m,false);
        panoWidgetView.setImageBitmap(tmp);
    }
}
