package jp.isaplan.nfcentrycontrol.gsonrequest;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import jp.isaplan.nfcentrycontrol.utils.StringUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class GsonRequest<T> extends Request<T> {
	
	private String TAG = "GsonRequest";
	
    private final Gson gson = new Gson();
    private final Class<T> clazz;
    private final Listener<T> listener;
  
    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     * @param headers Map of request headers
     */
    public GsonRequest(String url, Map<String, String> params, Class<T> clazz, Listener<T> listener, ErrorListener errorListener) {
    	super(Method.GET, StringUtils.generateUrlWithGetParams(url, params), errorListener);
    	this.clazz = clazz;
        this.listener = listener;
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }
  
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        Log.d(TAG, "parseNetworkResponse");
    	try {
            String json = new String(
                    response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(
                    gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }
}
