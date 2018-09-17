package navigate.uploader.navigateinsideuploader.Network;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.LruCache;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import navigate.uploader.navigateinsideuploader.Objects.Node;
import navigate.uploader.navigateinsideuploader.Objects.Room;
import navigate.uploader.navigateinsideuploader.Utills.Constants;
import navigate.uploader.navigateinsideuploader.Utills.Converter;


public class NetworkConnector {

    private static NetworkConnector mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    // server address
    private final String PORT = "8080";
    private final String IP = "132.74.210.108";
    private final String HOST_URL = "http://" + IP + ":" + PORT +"/";
    private final String BASE_URL = HOST_URL + "projres";

    // server requests
    public static final String GET_ALL_NODES_JSON_REQ = "0";
    public static final String GET_NODE_IMAGE = "1";
    public static final String INSERT_NODE = "2";
    public static final String ADD_ROOM_TO_NODE = "3";
    public static final String PAIR_NODES = "4";
    public static final String DELETE_NODE = "5";

    private String tempReq;
    private static final String RESOURCE_FAIL_TAG = "{\"result_code\":0}";
    private static final String RESOURCE_SUCCESS_TAG = "{\"result_code\":1}";

    public static final String REQ = "req";


    private NetworkConnector() {

    }

    public static synchronized NetworkConnector getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkConnector();
        }
        return mInstance;
    }

    public void initialize(Context context){
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    private void addToRequestQueue(String query, final NetworkResListener listener) {

        String reqUrl = BASE_URL + "?" + query;
        notifyPreUpdateListeners(listener);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, reqUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        notifyPostUpdateListeners(response, ResStatus.SUCCESS, listener);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        JSONObject err = null;
                        try {
                            err = new JSONObject(RESOURCE_FAIL_TAG);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finally {
                            notifyPostUpdateListeners(err, ResStatus.FAIL, listener);
                        }

                    }
                });

        getRequestQueue().add(jsObjRequest);
    }

    private void addImageRequestToQueue(String query, final NetworkResListener listener){

        String reqUrl = BASE_URL + "?" + query;

        notifyPreUpdateListeners(listener);

        getImageLoader().get(reqUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bm = response.getBitmap();
                notifyPostBitmapUpdateListeners(bm, ResStatus.SUCCESS, listener);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                notifyPostBitmapUpdateListeners(null, ResStatus.FAIL, listener);
            }
        });
    }

    private ImageLoader getImageLoader() {
        return mImageLoader;
    }
    

    public void sendRequestToServer(String requestCode, Node data, NetworkResListener listener){

        if(data==null){
            return;
        }

        Uri.Builder builder = new Uri.Builder();
        tempReq = requestCode;

        switch (requestCode){
           
            case GET_NODE_IMAGE: case DELETE_NODE:{
                builder.appendQueryParameter(REQ , requestCode);
                builder.appendQueryParameter(Constants.BEACONID , data.get_id().toString());

                String query = builder.build().getEncodedQuery();
                addToRequestQueue(query, listener);
                break;
            }
            case INSERT_NODE:{
                uploadItemImage(data, listener);
                break;
            }
        }
    }

    public void pairNodes(String n1, String n2 , int direction, boolean isDirect, NetworkResListener listener){
        if(n1 == null || n2 == null){
            return;
        }

        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter(REQ , PAIR_NODES);
        builder.appendQueryParameter(Constants.FirstID , n1);
        builder.appendQueryParameter(Constants.SecondID , n2);
        builder.appendQueryParameter(Constants.Direction , String.valueOf(direction));
        builder.appendQueryParameter(Constants.DIRECT, String.valueOf(isDirect));

        String query = builder.build().getEncodedQuery();
        addToRequestQueue(query, listener);

    }

    public void addRoomToNode(String data, String name, String number, NetworkResListener listener){

        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter(REQ , ADD_ROOM_TO_NODE);
        builder.appendQueryParameter(Constants.BEACONID , Constants.DEFULTUID.toString()+":"+data);
        builder.appendQueryParameter(Constants.NUMBER , number);
        builder.appendQueryParameter(Constants.NAME , name);

        String query = builder.build().getEncodedQuery();
        addToRequestQueue(query, listener);
    }

    private void uploadItemImage(final Node item, final NetworkResListener listener) {

        String reqUrl = HOST_URL + "web_item_manage?";
        notifyPreUpdateListeners(listener);

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, reqUrl,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(mCtx, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            notifyPostUpdateListeners(obj, ResStatus.SUCCESS, listener);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mCtx, error.getMessage(), Toast.LENGTH_SHORT).show();
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(RESOURCE_FAIL_TAG );
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finally {
                            notifyPostUpdateListeners(obj, ResStatus.FAIL, listener);
                        }

                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constants.BEACONID, Constants.DEFULTUID.toString()+":"+item.get_id().toString());
                params.put(Constants.Junction, String.valueOf(item.isJunction()));
                params.put(Constants.Elevator,  String.valueOf(item.isElevator()));
                params.put(Constants.Outside, String.valueOf(item.isOutside()));
                params.put(Constants.NessOutside, String.valueOf(item.isNessOutside()));
                params.put(Constants.Direction, String.valueOf(item.getDirection()));
                params.put(Constants.Building, item.getBuilding());
                params.put(Constants.Floor, item.getFloor());
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                byte[] pic = Converter.getBitmapAsByteArray(item.getImage(), 100);
                params.put("fileField", new DataPart(imagename + ".png", pic));
                return params;
            }
        };

        //adding the request to volley
        getRequestQueue().add(volleyMultipartRequest);
    }

    public void update(NetworkResListener listener){

        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter(REQ , GET_ALL_NODES_JSON_REQ);
        String query = builder.build().getEncodedQuery();

        addToRequestQueue(query, listener);
    }


    private  void notifyPostBitmapUpdateListeners(final Bitmap res, final ResStatus status, final NetworkResListener listener) {

        Handler handler = new Handler(mCtx.getMainLooper());

        Runnable myRunnable = new Runnable() {

            @Override
            public void run() {
                try{
                    listener.onPostUpdate(res, status);
                }
                catch(Throwable t){
                    t.printStackTrace();
                }
            }
        };
        handler.post(myRunnable);

    }

    private  void notifyPostUpdateListeners(final JSONObject res, final ResStatus status, final NetworkResListener listener) {

        Handler handler = new Handler(mCtx.getMainLooper());

        Runnable myRunnable = new Runnable() {

            @Override
            public void run() {
                try{
                    listener.onPostUpdate(res, status);
                }
                catch(Throwable t){
                    t.printStackTrace();
                }
            }
        };
        handler.post(myRunnable);

    }

    private void notifyPreUpdateListeners(final NetworkResListener listener) {

        Handler handler = new Handler(mCtx.getMainLooper());

        Runnable myRunnable = new Runnable() {

            @Override
            public void run() {
                try{
                        listener.onPreUpdate(tempReq);
                }
                catch(Throwable t){
                    t.printStackTrace();
                }
            }
        };
        handler.post(myRunnable);

    }
}
