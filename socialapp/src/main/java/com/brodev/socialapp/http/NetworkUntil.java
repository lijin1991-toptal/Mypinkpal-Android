package com.brodev.socialapp.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.brodev.socialapp.android.manager.CoreManager;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.handler.CoreXMLHandler;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class NetworkUntil {
	
	static InputStream inputstream = null;

	//Create default http client
	static DefaultHttpClient httpClient;
	static HttpPost httpPost;
	static HttpGet httpGet;
	static HttpEntity entity;
	static HttpParams httpParams;
	static HttpResponse httpResponse;
	
	private Context context;
	private CoreManager coreManager;
	private CoreXMLHandler coreXMLHandler;
	private String line;
	private StringBuilder total;
	private BufferedReader rd;
	private String relString;
	
    //constructor
    public NetworkUntil() {
    }
    
    /**
     * @param context
     * @param assetManager
     */
    public NetworkUntil(Context context, AssetManager assetManager) {
    	this.context = context;
    	coreManager = new CoreManager(this.context);
    	coreXMLHandler = new CoreXMLHandler();
    	
		//if core url or gcm key is null
    	if ("".equals(coreManager.getCoreUrl()) || "".equals(coreManager.getGCMKey())) {
    		Config config = new Config();
    		coreXMLHandler = config.getUrlXmlHandler(this.context, assetManager);
    		
    		//set core url
    		Config.CORE_URL = config.readUrl(coreXMLHandler);
    		
    		//set gcm key
    		Config.SENDER_ID = config.readGCMKey(coreXMLHandler);
    		
    	}
    }
    
    /**
	 * function draw image from url 
	 * @param icon
	 * @param url
	 * @param drawable/image
	 */
	public void drawImageUrl(ImageView icon, String url, int i) {
		UrlImageViewHelper.setUrlDrawable(icon, url, i, new UrlImageViewCallback() {
			 @Override
               public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
                   if (!loadedFromCache) {
                       ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, ScaleAnimation.RELATIVE_TO_SELF, .5f, ScaleAnimation.RELATIVE_TO_SELF, .5f);
                       scale.setDuration(300);
                       scale.setInterpolator(new OvershootInterpolator());
                       imageView.startAnimation(scale);
                   }
               }
		});
	}
    
    public String makeHttpRequest(String url, String method, List<NameValuePair> params) {
    	// Making HTTP request
        try {
            // defaultHttpClient
            httpClient = new DefaultHttpClient();
            httpParams = httpClient.getParams();
            ClientConnectionManager mgr = httpClient.getConnectionManager();
            
            httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams, mgr.getSchemeRegistry()), httpParams);
            
			HttpConnectionParams.setConnectionTimeout(httpParams, Config.NETWORK_CONNECT_TIMEOUT);
			HttpConnectionParams.setSoTimeout(httpParams, Config.NETWORK_READ_TIMEOUT);
			httpClient.setParams(httpParams);
			
            // check for request method
            if(method.equals("POST")) {
            	Log.i("URL_POST", url);
                // request method is POST
                httpPost = new HttpPost(url);
                
                // Set Character
				entity = new UrlEncodedFormEntity(params, "utf8");
				
                httpPost.setEntity(entity);
 
                httpResponse = httpClient.execute(httpPost);
                entity = httpResponse.getEntity();
                if(entity != null) {
                	inputstream = entity.getContent();
                	relString = null;
                	
                	try {
                		relString = convertStreamToString(inputstream);	
                	} catch (Exception ex) {
                		ex.printStackTrace();
                	} finally {
                		inputstream.close();	
                	}
                	
	        		return relString;
                }
 
            } else if(method.equals("GET")){
                // request method is GET
            	if (params != null) {
            		 String paramString = URLEncodedUtils.format(params, "UTF-8");
                     url += "?" + paramString;
            	}
                Log.i("URL_GET", url);
                httpGet = new HttpGet(url);
 
                httpResponse = httpClient.execute(httpGet);
                entity = httpResponse.getEntity();
                if (entity != null) 
                {
                	inputstream = entity.getContent();
                	
                	relString = null;
                	try {
                		relString = convertStreamToString(inputstream);	
                	} catch (Exception ex) {
                		ex.printStackTrace();
                	} finally {
                		inputstream.close();
                	}
                	
	        		return relString;
                }
            }           
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    	return null;
    }
    
    /**
	 * Convert Stream to String
	 * @param is
	 * @return String;
	 */
	 private String convertStreamToString(InputStream is) throws IOException {
        line = "";
        total = new StringBuilder();
        rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (Exception e) {	
        	e.printStackTrace();
        } finally {
        	if (rd != null) {
        		rd.close();
        		rd = null;
        	}
        }
        return total.toString();
    }
}
