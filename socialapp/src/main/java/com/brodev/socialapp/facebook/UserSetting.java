package com.brodev.socialapp.facebook;

public class UserSetting {
	
	SettingFacebookApp settingFB;

	public void storeAppId(String appId, boolean displayFb) {
		settingFB.storeAppId(appId, displayFb);
	}

	public String getAppId() {
		return settingFB.getAppId();
	}

	public boolean getDisplayFb() {
		return settingFB.getDisplayFb();
	}
}
