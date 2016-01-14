package com.brodev.socialapp.view;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.dialogs.ProgressDialog;
import com.brodev.socialapp.view.base.ActivityHelper;
import com.brodev.socialapp.view.mediacall.CallActivity;
import com.mypinkpal.app.R;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.SideBarFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

public class BaseActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	protected ListFragment mFrag;
	private String colorCode;
	private ColorView colorView;
	private User user;

    //Quickblox
    public static final int DOUBLE_BACK_DELAY = 2000;

    protected final ProgressDialog progress;
    //    protected App app;
    protected android.app.ActionBar actionBar;
    protected QBService service;
    protected boolean useDoubleBackPressed;
    protected Fragment currentFragment;
    protected FailAction failAction;
    protected SuccessAction successAction;
    protected ActivityHelper activityHelper;

    private boolean doubleBackToExitPressedOnce;
    private boolean bounded;
    private ServiceConnection serviceConnection = new QBChatServiceConnection();

	public BaseActivity() {
//		mTitleRes = titleRes;
        progress = ProgressDialog.newInstance(R.string.dlg_wait_please);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setTitle(R.string.app_name);
		
		user = (User) getApplication().getApplicationContext();
		colorView = new ColorView(getApplicationContext());
		colorCode = colorView.getColorCode(getApplicationContext(), user);
		
		setBehindContentView(R.layout.activity_base);

		// set right side bar
		if (savedInstanceState == null) {
			FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
			// set fragment
			mFrag = new SideBarFragment();
			t.replace(R.id.menu_frame, mFrag);
			t.commit();
		} else {
			mFrag = (ListFragment) this.getSupportFragmentManager().findFragmentById(R.id.menu_frame);
		}

		final SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);

		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);

		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		//getSupportActionBar().setIcon(R.drawable.sidebar_button);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		if (!"Blue".equals(user.getColor())) {
			getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorCode)));
		}
		
		// set custom view top bar
		View topbar = LayoutInflater.from(this).inflate(R.layout.top_bar_notify, null);
		
		ActionBar.LayoutParams params = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		getSupportActionBar().setCustomView(topbar, params);

		getSupportActionBar().setDisplayShowCustomEnabled(true);
		
		//listen secondary view open
		sm.setSecondaryOnOpenListner(new SlidingMenu.OnOpenListener() {
			@Override
			public void onOpen() {
				requestFriend(getApplicationContext(), true, System.currentTimeMillis());
			}
		});
		
		//listen open side bar
		sm.setOnOpenedListener(new SlidingMenu.OnOpenedListener() {
			
			@Override
			public void onOpened() {
				requestSidebar(getApplicationContext());
			}
		});

        //Quickblox
        actionBar = getActionBar();
        failAction = new FailAction();
        successAction = new SuccessAction();
        activityHelper = new ActivityHelper(this, new GlobalListener());
        activityHelper.onCreate();
	}

    @Override
    protected void onStart() {
        super.onStart();
        connectToService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityHelper.onResume();
        addAction(QBServiceConsts.LOGIN_REST_SUCCESS_ACTION, successAction);
    }

    @Override
    protected void onPause() {
        activityHelper.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce || !useDoubleBackPressed) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        DialogUtils.show(this, getString(R.string.dlg_click_back_again));
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, DOUBLE_BACK_DELAY);
    }

    protected void onConnectedToService() {
    }

    protected void navigateToParent() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        if (intent == null) {
            finish();
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NavUtils.navigateUpTo(this, intent);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }

    protected void setCurrentFragment(Fragment fragment) {
        currentFragment = fragment;
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        android.app.FragmentTransaction transaction = buildTransaction();
        transaction.replace(R.id.container, fragment, null);
        transaction.commit();
    }

    private void unbindService() {
        if (bounded) {
            unbindService(serviceConnection);
        }
    }

    private void connectToService() {
        Intent intent = new Intent(BaseActivity.this, QBService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private android.app.FragmentTransaction buildTransaction() {
        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        return transaction;
    }

    protected void onFailAction(String action) {

    }

    protected void onSuccessAction(String action) {

    }

    public class FailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception e = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            ErrorUtils.showError(BaseActivity.this, e);
            hideProgress();
            onFailAction(bundle.getString(QBServiceConsts.COMMAND_ACTION));
        }
    }

    public class SuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            onSuccessAction(bundle.getString(QBServiceConsts.COMMAND_ACTION));
        }
    }

    private class GlobalListener implements ActivityHelper.GlobalActionsListener {

        @Override
        public void onReceiveChatMessageAction(Bundle extras) {
            boolean isSplashActivity = activityHelper.getContext() instanceof SplashActivity;
            if(!isSplashActivity) {
                activityHelper.onReceiveMessage(extras);
            }
        }

        @Override
        public void onReceiveForceReloginAction(Bundle extras) {
            activityHelper.forceRelogin();
        }

        @Override
        public void onReceiveRefreshSessionAction(Bundle extras) {
            DialogUtils.show(BaseActivity.this, getString(R.string.dlg_refresh_session));
            showProgress();
            activityHelper.refreshSession();
        }

        @Override
        public void onReceiveFriendActionAction(Bundle extras) {
            String alertMessage = extras.getString(QBServiceConsts.EXTRA_FRIEND_ALERT_MESSAGE);
            activityHelper.showFriendAlert(alertMessage);
        }
    }

    private class QBChatServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            bounded = true;
            service = ((QBService.QBServiceBinder) binder).getService();
            QBVideoChatHelper videoChatHelper = (QBVideoChatHelper) service.getHelper(QBService.VIDEO_CHAT_HELPER);
            if (videoChatHelper.getActivityClass() == false) {
                videoChatHelper.setActivityclass(CallActivity.class);
            }
            onConnectedToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    public FailAction getFailAction() {
        return failAction;
    }

    public synchronized void showProgress() {
        if (!progress.isAdded()) {
            progress.show(getFragmentManager(), null);
        }
    }

    public synchronized void hideProgress() {
        if (progress != null && progress.getActivity() != null) {
            progress.dismissAllowingStateLoss();
        }
    }

    public void hideActionBarProgress() {
        activityHelper.hideActionBarProgress();
    }

    public void showActionBarProgress() {
        activityHelper.showActionBarProgress();
    }

    public void addAction(String action, Command command) {
        activityHelper.addAction(action, command);
    }

    public boolean hasAction(String action) {
        return activityHelper.hasAction(action);
    }

    public void removeAction(String action) {
        activityHelper.removeAction(action);
    }

    public void updateBroadcastActionList() {
        activityHelper.updateBroadcastActionList();
    }

	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param
	 */
	public static void requestSidebar(Context context) {
        Intent intent = new Intent(Config.REQUEST_GET_SIDEBAR);
        
        context.sendBroadcast(intent);
    }
	
	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param bRequest
	 */
	public static void requestFriend(Context context, boolean bRequest, long time) {
        Intent intent = new Intent(Config.REQUEST_GET_FRIEND_ONLINE_ACTION);
        intent.putExtra("request_friend", bRequest);
        intent.putExtra("request_time", time);
        
        context.sendBroadcast(intent);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;		
		case R.id.actionBar_chat:			
			showSecondaryMenu();
			return true;
		case R.id.action_music:
			showSecondaryMenu();
			return true;
		case R.id.action_slide_bar:
			showSecondaryMenu();
			return true;
		}
		
		return false;	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.base, menu);

        return true;
    }

}
