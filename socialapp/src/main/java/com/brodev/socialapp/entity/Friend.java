package com.brodev.socialapp.entity;

import android.text.Html;

public class Friend {

	/**
	 * @string user id
	 */
	private String user_id;

    /**
     * @string user email
     */
    private String email;

	/**
	 * @string gender (male/female)
	 */
	private String gender;

	/**
	 * @string icon (link url icon)
	 */
	private String icon;

	/**
	 * @string birthday (day/month/year)
	 */
	private String birthday;

    /**
     * @string user_name
     */
    private String user_name;

	/**
	 * @string fullname
	 */
	private String fullname;

	/**
	 * @boolean confirm request
	 */
	private boolean isConfirmRequest;

	/**
	 * @string notice request
	 */
	private String notice;

	/**
	 * @integer request id
	 */
	private int requestId;

    private int mutualFriends;

    private String is_friend;
    private String sexuality;
    private String location;
    private String age;
    private String distance;

    /**
     * @string Quickblox id and password
     */
    //bronislaw
    private String quickbloxid;
    private String quickbloxpswd;

	/**
	 * @boolean is online
	 */
	private boolean isOnline;

	public Friend() {
		super();
	}

    public Friend(String user_id, String email, String gender, String icon, String birthday, String fullname, boolean isConfirmRequest, String notice, int requestId, int mutualFriends, String is_friend, String sexuality, String location, String age, String distance, boolean isOnline, String quickbloxid, String quickbloxpswd) {
        this.user_id = user_id;
        this.email = email;
        this.gender = gender;
        this.icon = icon;
        this.birthday = birthday;
        this.fullname = fullname;
        this.isConfirmRequest = isConfirmRequest;
        this.notice = notice;
        this.requestId = requestId;
        this.mutualFriends = mutualFriends;
        this.is_friend = is_friend;
        this.sexuality = sexuality;
        this.location = location;
        this.age = age;
        this.distance = distance;
        this.isOnline = isOnline;
        this.quickbloxid = quickbloxid;
        this.quickbloxpswd = quickbloxpswd;
    }

    public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}

	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public boolean isConfirmRequest() {
		return isConfirmRequest;
	}

	public void setConfirmRequest(boolean isConfirmRequest) {
		this.isConfirmRequest = isConfirmRequest;
	}

    public String getUsername() {
        return user_name;
    }

    public void setUsername(String username) {

        this.user_name = username;
    }

	public String getFullname() {
		if (fullname != null) {
			return Html.fromHtml(fullname).toString();
		}
		return fullname;
	}

	public void setFullname(String fullname) {
		
		this.fullname = fullname;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

    public int getMutualFriends() {
        return mutualFriends;
    }

    public void setMutualFriends(int mutualFriends) {
        this.mutualFriends = mutualFriends;
    }

    public String getIs_friend() {
        return is_friend;
    }

    public void setIs_friend(String is_friend) {
        this.is_friend = is_friend;
    }

    public String getSexuality() {
        return sexuality;
    }

    public void setSexuality(String sexuality) {
        this.sexuality = sexuality;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    /* Quickblox id and password */
    //bronislaw
    public String getQuickbloxid() {
        return quickbloxid;
    }

    public void setQuickbloxID(String id) {
        this.quickbloxid = id;
    }

    public String getQuickbloxpswd() {
        return quickbloxpswd;
    }

    public void setQuickbloxpswd(String pass) {
        this.quickbloxpswd = pass;
    }

}
