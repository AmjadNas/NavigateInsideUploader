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
import navigate.uploader.navigateinsideuploader.Utills.Constants;


public class NetworkConnector {

    private static NetworkConnector mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    // server address
    private final String PORT = "8080";
    private final String IP = "132.74.213.48";
    private final String HOST_URL = "http://" + IP + ":" + PORT +"/";
    private final String BASE_URL = HOST_URL + "projres";

    // server requests
    public static final String GET_ALL_NODES_JSON_REQ = "0";
    public static final String GET_NODE_IMAGE = "1";

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
           
            case GET_NODE_IMAGE:{
                builder.appendQueryParameter(REQ , requestCode);
                builder.appendQueryParameter(Constants.BEACONID , data.get_id().toString());

                String query = builder.build().getEncodedQuery();
                addToRequestQueue(query, listener);
                break;
            }
        }
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