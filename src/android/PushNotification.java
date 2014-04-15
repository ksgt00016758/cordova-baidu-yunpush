package org.apache.cordova.baiduyunpush;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

public class PushNotification extends CordovaPlugin
{
    private CallbackContext pushCallbackContext = null;
    public static CordovaWebView gWebView;
    public static final String API_KEY = "api_key";
    public static final String CALL_BACK_METHOD = "ecb";
    public static String jsString;
    
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException
	{
		if(action.equals("register"))
		{
			gWebView = this.webView;
			//gWebView.sendJavascript(_d); 
			this.pushCallbackContext = callbackContext;
			super.initialize(cordova, webView);
	        IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(PushConstants.ACTION_RECEIVE);
	        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            final JSONObject params = args.getJSONObject(0);
            String api_key = params.getString(API_KEY);
            String ecb = params.getString(CALL_BACK_METHOD);
            jsString = "javascript:" + ecb + "()";
            
	        PushManager.startWork(cordova.getActivity().getApplicationContext(), 0, api_key);
	        System.out.print("#########PushManager");
	        LOG.d("#########PushManager", "CordovaActivity.onCreate()");
            return true;
		}
		return false;
	}
	
	public static void executeCallback(){
		gWebView.sendJavascript(jsString);
	}

}
