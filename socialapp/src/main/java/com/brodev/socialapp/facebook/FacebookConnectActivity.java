package com.brodev.socialapp.facebook;

import java.util.Arrays;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.brodev.socialapp.view.BaseActivity;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.Session.StatusCallback;
import com.facebook.model.GraphUser;

public class FacebookConnectActivity extends FragmentActivity implements StatusCallback {
	
	/**
     * **********************************
     * *********** Constants ************
     * **********************************
     */
	private static final String PERMISSION_PUBLISH = "publish_actions";
	private static final String PERMISSION_LOCATION = "user_location";
	private static final String PERMISSION_BIRTHDAY = "user_birthday";
	private static final String PERMISSION_EMAIL = "email";
	protected static List<String> WRITE_PERMISSIONS = Arrays.asList(PERMISSION_PUBLISH);
	protected static List<String> READ_PERMISSIONS = Arrays.asList(PERMISSION_BIRTHDAY, PERMISSION_EMAIL, PERMISSION_LOCATION, "basic_info");
    
    /**
     * **********************************
     * *********** Variables ************
     * **********************************
     */
	private PendingAction pendingAction = PendingAction.NONE;
	private Item requestInfo;
	
	private UiLifecycleHelper uiHelper;
	
	private FacebookUser user;
    private FacebookResponse response;
    private SettingFacebookApp settingFB;
    
	private LoginListener loginListener;

	
    /**
     * **********************************
     * ********* Class Methods **********
     * **********************************
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setData(savedInstanceState);
    }
	
    @Override
    public void call(Session session, SessionState state, Exception exception) {
    	onSessionStateChange(session, state, exception);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    
    /**
     * **********************************
     * ******** Private Methods *********
     * **********************************
     */
    private void setData(Bundle savedInstanceState) {
    	uiHelper = new UiLifecycleHelper(this, this);

        uiHelper.onCreate(savedInstanceState);
        
    	user = new FacebookUser(this);
        response = new FacebookResponse(this);
        settingFB = new SettingFacebookApp(this);
            
    }
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	if (pendingAction == PendingAction.NONE) {
    		if (state.equals(SessionState.CLOSED) && loginListener != null) {
    			loginListener.onClosed(session, state, exception);
    		} else if (state.equals(SessionState.CLOSED_LOGIN_FAILED) && loginListener != null) {
    			loginListener.onClosedLoginFailed(session, state, exception);
    		} else if (state.equals(SessionState.OPENED) && loginListener != null) {
    			loginListener.onOpened(session, state, exception);
    			makeMeRequest(session);
    		} else if (state.equals(SessionState.CREATED) && loginListener != null) {
    			loginListener.onCreated(session, state, exception);
    		}
    	} else if (pendingAction != PendingAction.NONE && (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
    		requestInfo.getListener().onError(null, exception);
    		pendingAction = PendingAction.NONE;
    	} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
    		handlePendingAction();
    	}
    }
    
    private void handlePendingAction() {
    	pendingAction = PendingAction.NONE;
    }
    
    private void makeMeRequest(final Session session) {
        loginListener.onStart();
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

            @Override
            public void onCompleted(GraphUser facebookUser, Response facebookResponse) {
                if (facebookUser != null) {
                    user.storeUser(facebookUser);
                }
                if (facebookResponse != null) {
                    response.storeResponse(facebookResponse);
                }
                if (session == Session.getActiveSession()) {
                    loginListener.onSuccess(facebookResponse);
                }
                if (facebookResponse.getError() != null) {
                	loginListener.onError(facebookResponse.getError());
                }
                loginListener.onFinish();
            }

        });
        request.executeAsync();
    }
    
    /**
     * **********************************
     * ******** Public Methods **********
     * **********************************
     */
    public boolean isConnected()  {
        Session session = Session.getActiveSession();
        return session.isOpened();
    }
    
    public void disconnect() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}
	}
    
    public void connect(LoginListener listener)  {
    	Log.i("Facebook App id", getAppId());
        loginListener = listener;
//        Session session = Session.getActiveSession();
        Session session = new Session.Builder(getBaseContext()).setApplicationId(getAppId()).build();
        Session.setActiveSession(session);
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(this).setPermissions(READ_PERMISSIONS));
        } else {
            Session.openActiveSession(this, true, this);
        }
        session = Session.getActiveSession();
        if (session.isOpened()) {
            makeMeRequest(session);
        }
    }
    
    public String getAppId() {
    	return settingFB.getAppId();
    }
    
    public boolean getDisplayFb() {
    	return settingFB.getDisplayFb();
    }
    
    public String getFullName() {
    	return (isConnected()) ? user.getFullName() : null;
    }
    
    public String getUserBirth() {
        return (isConnected()) ? user.getBirthday() : null;
    }

    public String getUserId() {
        return (isConnected()) ? user.getId() : null;
    }

    public String getUserName() {
        return (isConnected()) ? user.getUserName() : null;
    }

    public String getUserEmail() {
        return (isConnected()) ? response.getEmail() : null;
    }

    public String getUserGender() {
        return (isConnected()) ? response.getGender() : null;
    }

    public String getAccessToken() {
        return (isConnected()) ?response.getAccessToken() : null;
    }

    public String getApplicationId() {
        return (isConnected()) ? response.getAppId() : null;
    }

}
