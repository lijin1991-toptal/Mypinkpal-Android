package com.brodev.socialapp.entity;

import java.io.Serializable;

import android.text.Html;

public class UserProfile implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @string user id
	 */
	private String user_id;
	
	/**
	 * @string fullname
	 */
	private String full_name;

    /**
     * @string username
     */
    private String user_name;

    /**
     * @string quickblox id
     */
    private String quickblox_id;

	/**
	 * @string dob_setting
	 */
	private String dob_setting;
	
	/**
	 * @string location_phrase
	 */
	private String location_phrase;
	
	/**
	 * @string luser_image
	 */
	private String user_image;
	
	/**
	 * @string birthday_phrase
	 */
	private String birthday_phrase;
	
	/**
	 * @string cover_photo
	 */
	private String cover_photo;
	
	/**
	 * @string title
	 */
	private String title;

	/**
	 * @string Category
	 */
	private String category_id;
	
	/**
	 * @string Pages Image
	 */
	private String pages_image;
	
	/**
	 * @string Gender
	 */
	private String Gender;
	
	/**
	 * @string Age
	 */
	private String Age;
	
	/**
	 * @string Location
	 */
	private String location_info;

    private String sexuality;
	
	/**
	 * @string Last_login
	 */
	private String Last_login;
	
	/**
	 * @string Member_since
	 */
	private String Member_since;
	
	/**
	 * @string Membership
	 */
	private String Membership;
	
	/**
	 * @string Profile_views
	 */
	private String Profile_views;
	
	/**
	 * @string RSS_Subscribers
	 */
	private String RSS_Subscribers;
		
	/**
	 * @string is_friend
	 */
	private String is_friend;
	
	/**
	 * @string total_friend
	 */
	private String total_friend;
	
	/**
	 * @string request_id
	 */
	private int request_id;
	
	/**
	 * @string text
	 */
	private String text;
	
	/**
	 * @string like
	 */
	private String is_liked;
	
	/**
	 * @string total_like
	 */
	private String total_like;
	
	/**
	 * @string profile_page_id
	 */
	private String profile_page_id;

    private String relationship_status;

    /**
     * @string city and zip
     */
    private String profile_city;
    private String profile_zip;

    /**
     * @string Religion and Timeline
     */
    private String religion;
    private String timeline;

    /**
     * @string About Me
     */
    private String about_me;
    private String who_I_like_to_meet;

    /**
     * @string movies, interests, music
     */
    private String movies;
    private String interests;
    private String music;

    /**
     * @string Smoker and Drinker
     */
    private String smoker;
    private String drinker;

	
	public UserProfile() {
		super();
	}

    public UserProfile(String user_id, String full_name, String dob_setting, String location_phrase, String user_image, String birthday_phrase,
                       String cover_photo, String title, String category_id, String pages_image, String gender, String age, String location_info,
                       String last_login, String member_since, String membership, String profile_views, String RSS_Subscribers, String is_friend,
                       String total_friend, int request_id, String text, String is_liked, String total_like, String profile_page_id,
                       String relationship_status, String profile_city, String profile_zip, String religion, String timeline, String about_me,
                       String who_I_like_to_meet, String movies, String interests, String music, String smoker, String drinker) {
        this.user_id = user_id;
        this.full_name = full_name;
        this.dob_setting = dob_setting;
        this.location_phrase = location_phrase;
        this.user_image = user_image;
        this.birthday_phrase = birthday_phrase;
        this.cover_photo = cover_photo;
        this.title = title;
        this.category_id = category_id;
        this.pages_image = pages_image;
        Gender = gender;
        Age = age;
        this.location_info = location_info;
        Last_login = last_login;
        Member_since = member_since;
        Membership = membership;
        Profile_views = profile_views;
        this.RSS_Subscribers = RSS_Subscribers;
        this.is_friend = is_friend;
        this.total_friend = total_friend;
        this.request_id = request_id;
        this.text = text;
        this.is_liked = is_liked;
        this.total_like = total_like;
        this.profile_page_id = profile_page_id;
        this.relationship_status = relationship_status;
        this.profile_city = profile_city;
        this.profile_zip = profile_zip;
        this.religion = religion;
        this.timeline = timeline;
        this.about_me = about_me;
        this.who_I_like_to_meet = who_I_like_to_meet;
        this.movies = movies;
        this.interests = interests;
        this.music = music;
        this.smoker = smoker;
        this.drinker = drinker;
    }

    public String getProfile_page_id() {
		return profile_page_id;
	}

	public void setProfile_page_id(String profile_page_id) {
		this.profile_page_id = profile_page_id;
	}

	public String getTotal_like() {
		return total_like;
	}

	public void setTotal_like(String total_like) {
		this.total_like = total_like;
	}

	public String getIs_liked() {
		return is_liked;
	}

	public void setIs_liked(String is_liked) {
		this.is_liked = is_liked;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getRequest_id() {
		return request_id;
	}

	public void setRequest_id(int request_id) {
		this.request_id = request_id;
	}

	public String getTotal_friend() {
		return total_friend;
	}

	public void setTotal_friend(String total_friend) {
		this.total_friend = total_friend;
	}

	public String getIs_friend() {
		return is_friend;
	}

	public void setIs_friend(String is_friend) {
		this.is_friend = is_friend;
	}
	
	public String getGender() {
		return Gender;
	}

	public void setGender(String gender) {
		Gender = gender;
	}

	public String getAge() {
		return Age;
	}

	public void setAge(String age) {
		Age = age;
	}

	public String getLocation_info() {
		return location_info;
	}

	public void setLocation_info(String location_info) {
		this.location_info = location_info;
	}

	public String getLast_login() {
		return Last_login;
	}

	public void setLast_login(String last_login) {
		Last_login = last_login;
	}

	public String getMember_since() {
		return Member_since;
	}

	public void setMember_since(String member_since) {
		Member_since = member_since;
	}

	public String getMembership() {
		return Membership;
	}

	public void setMembership(String membership) {
		Membership = membership;
	}

	public String getProfile_views() {
		return Profile_views;
	}

	public void setProfile_views(String profile_views) {
		Profile_views = profile_views;
	}

	public String getRSS_Subscribers() {
		return RSS_Subscribers;
	}

	public void setRSS_Subscribers(String rSS_Subscribers) {
		RSS_Subscribers = rSS_Subscribers;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getFullname() {
		if (full_name != null) {
			return Html.fromHtml(full_name).toString();
		}		
		return full_name;
	}

	public void setFullname(String full_name) {
		this.full_name = full_name;
	}

    // bronislaw
    public String getUsername() {
        return user_name;
    }

    public void setUsername(String user_name) {
        this.user_name = user_name;
    }

    public String getQuickbloxID() {
        return quickblox_id;
    }

    public void setQuickbloxID(String quickbloxId) {
        this.quickblox_id = quickbloxId;
    }
	
	public String getDob() {
		return dob_setting;
	}

	public void setDob(String dob_setting) {
		this.dob_setting = dob_setting;
	}
	
	public String getLocation() {
		if (location_phrase != null) {
			return Html.fromHtml(location_phrase).toString();
		}
		return location_phrase;
	}

	public void setLocation(String location_phrase) {
		this.location_phrase = location_phrase;
	}
	
	public String getUserImage() {
		return user_image;
	}

	public void setUserImage(String user_image) {
		this.user_image = user_image;
	}
	
	public String getBirthday() {
		if (birthday_phrase != null) {
			return Html.fromHtml(birthday_phrase).toString();
		}
		return birthday_phrase;
	}

	public void setBirthday(String birthday_phrase) {
		this.birthday_phrase = birthday_phrase;
	}
	
	public String getCoverPhoto() {
		return cover_photo;
	}

	public void setCoverPhoto(String cover_photo) {
		this.cover_photo = cover_photo;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getCategory() {
		return category_id;
	}

	public void setCategory(String category_id) {
		this.category_id = category_id;
	}
	
	public String getPagesImage() {
		return pages_image;
	}

	public void setPagesImage(String pages_image) {
		this.pages_image = pages_image;
	}

    public String getRelationship_status() {
        return relationship_status;
    }

    public void setRelationship_status(String relationship_status) {
        this.relationship_status = relationship_status;
    }

    public String getSexuality() {
        return sexuality;
    }

    public void setSexuality(String sexuality) {
        this.sexuality = sexuality;
    }

    public String getProfile_city() {
        return profile_city;
    }

    public void setProfile_city(String profile_city) {
        this.profile_city = profile_city;
    }

    public String getProfile_zip() {
        return profile_zip;
    }

    public void setProfile_zip(String profile_zip) {
        this.profile_zip = profile_zip;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.religion = religion;
    }

    public String getAbout_me() {
        return about_me;
    }

    public void setAbout_me(String about_me) {
        this.about_me = about_me;
    }

    public String getWho_I_like_to_meet() {
        return who_I_like_to_meet;
    }

    public void setWho_I_like_to_meet(String who_I_like_to_meet) {
        this.who_I_like_to_meet = who_I_like_to_meet;
    }

    public String getMovies() {
        return movies;
    }

    public void setMovies(String movies) {
        this.movies = movies;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getMusic() {
        return music;
    }

    public void setMusic(String music) {
        this.music = music;
    }

    public String getSmoker() {
        return smoker;
    }

    public void setSmoker(String smoker) {
        this.smoker = smoker;
    }

    public String getDrinker() {
        return drinker;
    }

    public void setDrinker(String drinker) {
        this.drinker = drinker;
    }
}
