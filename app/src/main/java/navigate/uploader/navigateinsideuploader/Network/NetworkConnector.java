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
    // singleton pattern
    private static NetworkConnector mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    // server address
    private final String PORT = "8080";
    private final String IP = "132.74.210.25";
    private final String HOST_URL = "http://" + IP + ":" + PORT +"/";
    private final String BASE_URL = HOST_URL + "projres";

    // server requests
    public static final String GET_ALL_NODES_JSON_REQ = "0";
    public static final String GET_NODE_IMAGE = "1";
    public static final String INSERT_NODE = "2";
    public static final String ADD_ROOM_TO_NODE = "3";
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
        // initialize request repeater and image loader cache
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
        // set on response handlers
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
        // send request
        getRequestQueue().add(jsObjRequest);
    }

    private void addImageRequestToQueue(String query, final NetworkResListener listener){

        String reqUrl = BASE_URL + "?" + query;

        notifyPreUpdateListeners(listener);
        // set on response handlers and send request to fetch image
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

    /**
     * performs operations dependant on the node data and the requests
     * @param requestCode
     * @param data
     * @param listener
     */
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
                if (data.getImage() == null) { // upload node without an image
                    builder.appendQueryParameter(REQ, INSERT_NODE);
                    builder.appendQueryParameter(Constants.BEACONID, Constants.DEFULTUID.toString() + ":" + data.get_id().toString());
                    builder.appendQueryParameter(Constants.Junction, String.valueOf(data.isJunction()));
                    builder.appendQueryParameter(Constants.Elevator, String.valueOf(data.isElevator()));
                    builder.appendQueryParameter(Constants.Outside, String.valueOf(data.isOutside()));
                    builder.appendQueryParameter(Constants.NessOutside, String.valueOf(data.isNessOutside()));
                    builder.appendQueryParameter(Constants.Direction, String.valueOf(data.getDirection()));
                    builder.appendQueryParameter(Constants.Building, data.getBuilding());
                    builder.appendQueryParameter(Constants.Floor, data.getFloor());

                    String query = builder.build().getEncodedQuery();
                    addToRequestQueue(query, listener);
                }else { // upload node with an image
                    uploadNode(data, listener);
                }
                break;
            }
        }
    }

    /**
     * upload edge relation and the image related to it
     * @param n1 first id
     * @param n2 second id
     * @param img related image
     * @param direction
     * @param isDirect
     * @param listener
     */
    public void pairNodes(String n1, String n2 , Bitmap img, int direction, boolean isDirect, NetworkResListener listener){
        if(n1 == null || n2 == null){
            return;
        }
        uploadItemImage(img, n1, n2, direction, isDirect, listener);

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

    /**
     * helper method to upload the node with an image
     * @param node
     * @param listener
     */
    private void uploadNode(final Node node, final NetworkResListener listener){

        String reqUrl = HOST_URL + "web_node_manage?";
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
                params.put(Constants.BEACONID, Constants.DEFULTUID.toString() + ":" + node.get_id().toString());
                params.put(Constants.Junction, String.valueOf(node.isJunction()));
                params.put(Constants.Elevator, String.valueOf(node.isElevator()));
                params.put(Constants.Outside, String.valueOf(node.isOutside()));
                params.put(Constants.NessOutside, String.valueOf(node.isNessOutside()));
                params.put(Constants.Direction, String.valueOf(node.getDirection()));
                params.put(Constants.Building, node.getBuilding());
                params.put(Constants.Floor, node.getFloor());
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                byte[] pic = Converter.getBitmapAsByteArray(node.getImage(), 100);
                params.put("fileField", new DataPart(imagename + ".png", pic));
                return params;
            }
        };

        //adding the request to volley
        getRequestQueue().add(volleyMultipartRequest);
    }

    /**
     * helper method to upload the edge with an image
     * @param img
     * @param id
     * @param id2
     * @param dir
     * @param isDir
     * @param listener
     */
    private void uploadItemImage(final Bitmap img, final String id, final String id2, final int dir, final boolean isDir, final NetworkResListener listener) {

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
                params.put(Constants.FirstID, id);
                params.put(Constants.SecondID, id2);
                params.put(Constants.Direction, String.valueOf(dir));
                params.put(Constants.DIRECT, String.valueOf(isDir));
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                byte[] pic = Converter.getBitmapAsByteArray(img, 100);
                params.put("fileField", new DataPart(imagename + ".png", pic));
                return params;
            }
        };

        //adding the request to volley
        getRequestQueue().add(volleyMultipartRequest);
    }

    /**
     * dowload all data from the online database
     * @param listener
     */
    public void update(NetworkResListener listener){

        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter(REQ , GET_ALL_NODES_JSON_REQ);
        builder.appendQueryParameter(Constants.ID , "-1");
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
