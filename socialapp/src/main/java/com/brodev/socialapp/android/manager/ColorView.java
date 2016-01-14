package com.brodev.socialapp.android.manager;

import android.content.Context;
import android.graphics.Color;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.brodev.socialapp.entity.User;
import com.mypinkpal.app.R;

public class ColorView {
	
	Context context;
	private PagerSlidingTabStrip tabs;
	
	public ColorView(Context context) {
		this.context = context;
	}
	
	public ColorView(PagerSlidingTabStrip tabs, Context context) {
		this.tabs = tabs;
		this.context = context;
	}
	
	/**
	 * Get color code
	 * @param context
	 * @param user
	 * @return
	 */
	public String getColorCode(Context context, User user) {
		String colorCode = "#0084c9";
		
		if ("Brown".equalsIgnoreCase(user.getColor())) {
			colorCode = "#da6e00";
		} else if ("Pink".equalsIgnoreCase(user.getColor())) {
			colorCode = "#ef4964";
		} else if ("Green".equalsIgnoreCase(user.getColor())) {
			colorCode = "#348105";
		} else if ("Violet".equalsIgnoreCase(user.getColor())) {
			colorCode = "#8190db";
		} else if ("Red".equalsIgnoreCase(user.getColor())) {
			colorCode = "#ff0606";
		} else if ("Dark Violet".equalsIgnoreCase(user.getColor())) {
			colorCode = "#4e529b";
		}
		
		return colorCode;
	}
	
	/**
	 * Change color text
	 * @param blogTxt
	 * @param colorCode
	 */
	public void changeColorText(TextView blogTxt, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			blogTxt.setTextColor(Color.parseColor("#da6e00"));
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			blogTxt.setTextColor(Color.parseColor("#ef4964"));
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			blogTxt.setTextColor(Color.parseColor("#348105"));
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			blogTxt.setTextColor(Color.parseColor("#8190db"));
		}  else if ("Red".equalsIgnoreCase(colorCode)) {
			blogTxt.setTextColor(Color.parseColor("#ff0606"));
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			blogTxt.setTextColor(Color.parseColor("#4e529b"));
		}else {
			blogTxt.setTextColor(Color.parseColor("#0084c9"));
		}
	}

    /**
     * Change color for like/comment view
     * @param likeImg
     * @param commentImg
     * @param colorCode
     */
	public void changeColorLikeCommnent(ImageView likeImg, ImageView commentImg, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.brown_like_icon);
			commentImg.setImageResource(R.drawable.brown_commet_icon);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.pink_like_icon);
			commentImg.setImageResource(R.drawable.pink_commet_icon);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.green_like_icon);
			commentImg.setImageResource(R.drawable.green_commet_icon);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.violet_like_icon);
			commentImg.setImageResource(R.drawable.violet_commet_icon);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.red_like_icon);
			commentImg.setImageResource(R.drawable.red_commet_icon);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.dark_violet_like_icon);
			commentImg.setImageResource(R.drawable.dark_violet_commet_icon);
		}  else {
			likeImg.setImageResource(R.drawable.like_icon);
			commentImg.setImageResource(R.drawable.commet_icon);
		}
	}
	
	/**
	 * Change color
	 * @param colorCode
	 */
	public void changeColorShare(ImageView shareImg, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			shareImg.setBackgroundResource(R.drawable.brown_share_post_search);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			shareImg.setBackgroundResource(R.drawable.pink_share_post_search);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			shareImg.setBackgroundResource(R.drawable.green_share_post_search);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			shareImg.setBackgroundResource(R.drawable.violet_share_post_search);
		}  else if ("Red".equalsIgnoreCase(colorCode)) {
			shareImg.setBackgroundResource(R.drawable.red_share_post_search);
		}  else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			shareImg.setBackgroundResource(R.drawable.dark_violet_share_post_search);
		} else {
			shareImg.setBackgroundResource(R.drawable.share_post_search);
		}
	}
	
	/**
	 * Change color
	 * @param colorCode
	 */
	public void changeColorLikeIcon(ImageView likeImg, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.brown_like_icon);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.pink_like_icon);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.green_like_icon);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.violet_like_icon);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.red_like_icon);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			likeImg.setImageResource(R.drawable.dark_violet_like_icon);
		} else {
			likeImg.setImageResource(R.drawable.like_icon);
		}
	}
	
	
	public void changeColorTabs(String colorCode) {
		int newColor = Color.parseColor("#0084c9");
		
		if ("Brown".equalsIgnoreCase(colorCode)) {
			newColor = Color.parseColor("#da6e00");
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			newColor = Color.parseColor("#ef4964");
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			newColor = Color.parseColor("#348105");
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			newColor = Color.parseColor("#8190db");
		}  else if ("Red".equalsIgnoreCase(colorCode)) {
			newColor = Color.parseColor("#ff0606");
		}  else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			newColor = Color.parseColor("#4e529b");
		} 
		
		tabs.setIndicatorColor(newColor);
	}
	

	/**
	 * Change color action
	 * @param btnAction
	 * @param colorCode
	 */
	public void changeColorAction(RelativeLayout btnAction, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			btnAction.setBackgroundColor(Color.parseColor("#da6e00"));
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			btnAction.setBackgroundColor(Color.parseColor("#ef4964"));
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			btnAction.setBackgroundColor(Color.parseColor("#348105"));
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			btnAction.setBackgroundColor(Color.parseColor("#8190db"));
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			btnAction.setBackgroundColor(Color.parseColor("#ff0606"));
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			btnAction.setBackgroundColor(Color.parseColor("#4e529b"));
		} else {
			btnAction.setBackgroundColor(Color.parseColor("#0084c9"));
		}
	}
	
	/**
	 * Change color
	 * @param shareImg
	 * @param colorCode
	 */
	public void changeColorPrivacy(ImageView shareImg, String colorCode) {
		if ("Brown".equalsIgnoreCase(colorCode)) {
			shareImg.setImageResource(R.drawable.brown_post_stt_privacy);
		} else if ("Pink".equalsIgnoreCase(colorCode)) {
			shareImg.setImageResource(R.drawable.pink_post_stt_privacy);
		} else if ("Green".equalsIgnoreCase(colorCode)) {
			shareImg.setImageResource(R.drawable.green_post_stt_privacy);
		} else if ("Violet".equalsIgnoreCase(colorCode)) {
			shareImg.setImageResource(R.drawable.violet_post_stt_privacy);
		} else if ("Red".equalsIgnoreCase(colorCode)) {
			shareImg.setImageResource(R.drawable.red_post_stt_privacy);
		} else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
			shareImg.setImageResource(R.drawable.dark_violet_post_stt_privacy);
		} else {
			shareImg.setImageResource(R.drawable.post_stt_privacy);
		}
	}

    /**
     * Change color
     * @param imageImg
     * @param buttonBtn
     * @param colorCode
     */
    public void changeImageForNoInternet(ImageView imageImg, Button buttonBtn, String colorCode) {
        if ("Brown".equalsIgnoreCase(colorCode)) {
            imageImg.setImageResource(R.drawable.brown_no_internet_img);
            buttonBtn.setBackgroundResource(R.drawable.brown_login_button);
        } else if ("Pink".equalsIgnoreCase(colorCode)) {
            imageImg.setImageResource(R.drawable.pink_no_internet_img);
            buttonBtn.setBackgroundResource(R.drawable.pink_login_button);
        } else if ("Green".equalsIgnoreCase(colorCode)) {
            imageImg.setImageResource(R.drawable.green_no_internet_img);
            buttonBtn.setBackgroundResource(R.drawable.green_login_button);
        } else if ("Violet".equalsIgnoreCase(colorCode)) {
            imageImg.setImageResource(R.drawable.violet_no_internet_img);
            buttonBtn.setBackgroundResource(R.drawable.violet_login_button);
        } else if ("Red".equalsIgnoreCase(colorCode)) {
            imageImg.setImageResource(R.drawable.red_no_internet_img);
            buttonBtn.setBackgroundResource(R.drawable.red_login_button);
        } else if ("Dark Violet".equalsIgnoreCase(colorCode)) {
            imageImg.setImageResource(R.drawable.dark_violet_no_internet_img);
            buttonBtn.setBackgroundResource(R.drawable.dark_violet_login_button);
        } else {
            imageImg.setImageResource(R.drawable.no_internet_img);
            buttonBtn.setBackgroundResource(R.drawable.login_button);
        }
    }
}
