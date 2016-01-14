package com.brodev.socialapp.fragment.membership;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.ColorView;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.fragment.BROADCAST;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.droidparts.activity.Activity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MembershipFragment extends SherlockFragment {

    private User user;
    private PhraseManager phraseManager;
    private ColorView colorView;

    private ArrayList<MembershipPackage>    mPackageList;
    private ArrayList<PaymentGateway>       mGatewayList;
    private MembershipPackageAdapter        mPackageAdapter;
    private ListView                        mPackageListView;
    private LayoutInflater                  mInflater;
    private RelativeLayout                  mPaymentLayout;
    private LinearLayout                    mPaymentSubLayout;
    private TextView                        mPaymentDescText;
    private TextView                        mCloseText;
    private TextView                        mStatusText;
    private ProgressBar                     mLoading;
    private CheckoutButton                  mPaypalButton;

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;
    private NetworkUntil networkUntil;

    private RelativeLayout noInternetLayout;
    private TextView noInternetTitle, noInternetContent;
    private ImageView noInternetImg;
    private Button noInternetBtn;

    private int mSelectedListId;
    private String mPurchaseId;
    private String mPaymentStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = (User) getActivity().getApplicationContext();
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());

        mSelectedListId = -1;
        mPurchaseId = "";
        mPaymentStatus = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //create view from membership_fragment xml
        View view = inflater.inflate(R.layout.membership_fragment, container, false);

        mInflater = inflater;
        connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        networkUntil = new NetworkUntil();

        //no internet connection
        noInternetLayout = (RelativeLayout) view.findViewById(R.id.no_internet_layout);
        noInternetBtn = (Button) view.findViewById(R.id.no_internet_button);
        noInternetTitle = (TextView) view.findViewById(R.id.no_internet_title);
        noInternetContent = (TextView) view.findViewById(R.id.no_internet_content);
        noInternetImg = (ImageView) view.findViewById(R.id.no_internet_image);

        //change color for no internet
        colorView.changeImageForNoInternet(noInternetImg, noInternetBtn, user.getColor());

        //set text for no internet element
        noInternetBtn.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.try_again"));
        noInternetTitle.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_title"));
        noInternetContent.setText(phraseManager.getPhrase(getActivity().getApplicationContext(), "accountapi.no_internet_content"));

        //action click load try again
        noInternetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loadMembershipPackages();
            }
        });

        mPackageList = new ArrayList<MembershipPackage>();
        mPackageAdapter = new MembershipPackageAdapter(getActivity().getApplicationContext(), mPackageList);
        mPackageListView = (ListView) view.findViewById(R.id.membershipPackageList);
        mPackageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onPackageItemClick(position);
            }
        });

        mGatewayList = new ArrayList<PaymentGateway>();
        mPaymentLayout = (RelativeLayout) view.findViewById(R.id.membershipPaymentLayout);
        mPaymentSubLayout = (LinearLayout) view.findViewById(R.id.membershipPaymentSubLayout);
        mPaymentDescText = (TextView) view.findViewById(R.id.membershipPaymentText);
        mCloseText = (TextView) view.findViewById(R.id.membershipCloseButon);
        mCloseText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                closePaymentLayout();
            }
        });
        mStatusText = (TextView) view.findViewById(R.id.membershipStatusTxt);
        mStatusText.setText(user.getMembership());
        mLoading = (ProgressBar) view.findViewById(R.id.membershipLoading);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadMembershipPackages();
    }

    public class ApiRequestTask extends AsyncTask<String, Void, String> {

        public String apiType;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }

            String resultstring = null;
            apiType = params[0];

            try {
                // url request
                String URL = Config.makeUrl(user.getCoreUrl(), null, false);
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();

                if (apiType.equals("getPackages")) {
                    pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                    pairs.add(new BasicNameValuePair("method", "accountapi.getPackages"));
                } else if (apiType.equals("checkPackage")) {
                    MembershipPackage selectedPackage = mPackageList.get(mSelectedListId);
                    if (selectedPackage == null) return null;

                    pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                    pairs.add(new BasicNameValuePair("method", "accountapi.checkPackage"));
                    pairs.add(new BasicNameValuePair("package_id", String.valueOf(selectedPackage.id)));
                } else if (apiType.equals("getGateways")) {
                    URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);
                    pairs.add(new BasicNameValuePair("mode", "getGateways"));
                } else if (apiType.equals("purchasePackage")) {
                    MembershipPackage selectedPackage = mPackageList.get(mSelectedListId);
                    if (selectedPackage == null) return null;

                    pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                    pairs.add(new BasicNameValuePair("method", "accountapi.purchasePackage"));
                    pairs.add(new BasicNameValuePair("package_id", String.valueOf(selectedPackage.id)));
                } else if (apiType.equals("paymentComplete")) {
                    MembershipPackage selectedPackage = mPackageList.get(mSelectedListId);
                    if (selectedPackage == null) return null;

                    pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                    pairs.add(new BasicNameValuePair("method", "accountapi.paymentComplete"));
                    pairs.add(new BasicNameValuePair("purchase_id", mPurchaseId));
                    pairs.add(new BasicNameValuePair("status", mPaymentStatus));
                    pairs.add(new BasicNameValuePair("total_paid", selectedPackage.cost));
                } else {
                    return null;
                }

                // request GET method to server
                resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);
            } catch (Exception ex) {
                mLoading.setVisibility(View.GONE);
                ex.printStackTrace();
                return null;
            }

            return resultstring;
        }

        @Override
        protected void onPostExecute(String result) {
            mLoading.setVisibility(View.GONE);

            if (apiType.equals("getPackages")) {
                if (result != null) {
                    showPackageList(result);
                    loadPaymentGateways();
                }
            } else if (apiType.equals("checkPackage")) {
                parseCheckPackage(result);
            } else if (apiType.equals("getGateways")) {
                parseGateways(result);
                initPayPalMPL();
            } else if (apiType.equals("purchasePackage")) {
                parsePurchasePackage(result);
            } else if (apiType.equals("paymentComplete")) {
                parsePaymentComplete(result);
            }
        }
    }

    public class MembershipPackage {
        public String id;
        public String title;
        public String description;
        public String is_active;
        public String cost;
        public String currency_id;
        public String recurring_cost;
        public String image_path;

        // constructor
        public MembershipPackage() {
            super();

            id = "";
            title = "";
            description = "";
            is_active = "";
            cost = "";
            currency_id = "";
            recurring_cost = "";
            image_path = "";
        }
    }

    public class PaymentGateway {
        public String id;
        public String title;
        public String description;
        public String is_active;
        public String is_test;
        public String paypal_email;

        // constructor
        public PaymentGateway() {
            super();

            id = "";
            title = "";
            description = "";
            is_active = "";
            is_test = "";
            paypal_email = "";
        }
    }

    public void loadMembershipPackages() {
        try {
            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                mPaymentLayout.setVisibility(View.INVISIBLE);

                //fetch data
                mLoading.setVisibility(View.VISIBLE);
                new ApiRequestTask().execute("getPackages");
            } else {
                // display error
                noInternetLayout.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);
                mPaymentLayout.setVisibility(View.INVISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            noInternetLayout.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.GONE);
            mPaymentLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void showPackageList(String resString) {
        try {
            mPackageList.clear();

            JSONObject mainJSON = new JSONObject(resString);
            JSONArray packageJsonArray = mainJSON.getJSONObject("output").getJSONArray("packages");
            JSONObject packageJson = null;

            for (int i = 0; i < packageJsonArray.length(); i++) {
                packageJson = packageJsonArray.getJSONObject(i);
                MembershipPackage membershipPackage = new MembershipPackage();

                //set package id
                if (packageJson.has("package_id") && !packageJson.isNull("package_id"))
                    membershipPackage.id = packageJson.getString("package_id");
                //set title
                if (packageJson.has("title") && !packageJson.isNull("title"))
                    membershipPackage.title = Html.fromHtml(packageJson.getString("title")).toString();
                //set description
                if (packageJson.has("description") && !packageJson.isNull("description"))
                    membershipPackage.description = Html.fromHtml(packageJson.getString("description")).toString();
                //set is_active
                if (packageJson.has("is_active") && !packageJson.isNull("is_active"))
                    membershipPackage.is_active = packageJson.getString("is_active");
                //set cost
                if (packageJson.has("default_cost") && !packageJson.isNull("default_cost"))
                    membershipPackage.cost = packageJson.getString("default_cost");
                //set currency
                if (packageJson.has("default_currency_id") && !packageJson.isNull("default_currency_id"))
                    membershipPackage.currency_id = packageJson.getString("default_currency_id");
                //set recurring cost
                if (packageJson.has("default_recurring_cost") && !packageJson.isNull("default_recurring_cost"))
                    membershipPackage.recurring_cost = Html.fromHtml(packageJson.getString("default_recurring_cost")).toString();
                //set image
                if (packageJson.has("image_path") && !packageJson.isNull("image_path"))
                    membershipPackage.image_path = packageJson.getString("image_path");

                mPackageList.add(membershipPackage);
            }

            mPackageListView.setAdapter(mPackageAdapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadPaymentGateways() {
        mLoading.setVisibility(View.VISIBLE);
        new ApiRequestTask().execute("getGateways");
    }

    public void parseGateways(String resString) {
        if (resString != null) {
            try {
                mGatewayList.clear();

                JSONObject mainJSON = new JSONObject(resString);
                JSONArray gatewayJsonArray = mainJSON.getJSONArray("gateways");
                JSONObject gatewayJson = null;
                JSONObject settingJson = null;

                for (int i = 0; i < gatewayJsonArray.length(); i++) {
                    gatewayJson = gatewayJsonArray.getJSONObject(i);
                    PaymentGateway gateway = new PaymentGateway();

                    //set package id
                    if (gatewayJson.has("gateway_id") && !gatewayJson.isNull("gateway_id"))
                        gateway.id = Html.fromHtml(gatewayJson.getString("gateway_id")).toString();
                    //set title
                    if (gatewayJson.has("title") && !gatewayJson.isNull("title"))
                        gateway.title = Html.fromHtml(gatewayJson.getString("title")).toString();
                    //set description
                    if (gatewayJson.has("description") && !gatewayJson.isNull("description"))
                        gateway.description = Html.fromHtml(gatewayJson.getString("description")).toString();
                    //set is_active
                    if (gatewayJson.has("is_active") && !gatewayJson.isNull("is_active"))
                        gateway.is_active = gatewayJson.getString("is_active");
                    //set cost
                    if (gatewayJson.has("is_test") && !gatewayJson.isNull("is_test"))
                        gateway.is_test = gatewayJson.getString("is_test");
                    //set currency
                    if (gatewayJson.has("setting") && !gatewayJson.isNull("setting")) {
                        settingJson = gatewayJson.getJSONObject("setting");
                        if (settingJson.has("paypal_email") && !settingJson.isNull("paypal_email"))
                            gateway.paypal_email = Html.fromHtml(settingJson.getString("paypal_email")).toString();
                    }

                    mGatewayList.add(gateway);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Create membership package array adapter
     *
     * @author dmytro
     */
    public class MembershipPackageAdapter extends ArrayAdapter<MembershipPackage> {

        public MembershipPackageAdapter(Context context, ArrayList<MembershipPackage> packages) {
            super(context, 0, packages);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return createPackageListItem((MembershipPackage)getItem(position));
        }
    }

    public View createPackageListItem(MembershipPackage membershipPackage) {
        View view = mInflater.inflate(R.layout.membership_package_item, null);
        ImageView packageImage = (ImageView) view.findViewById(R.id.membershipPackageImage);
        TextView titleText = (TextView) view.findViewById(R.id.membershipPackageTitle);
        TextView recurringText = (TextView) view.findViewById(R.id.membershipPackageRecurring);
        TextView descText = (TextView) view.findViewById(R.id.membershipPackageDescription);

        titleText.setText(membershipPackage.title);
        recurringText.setText(membershipPackage.recurring_cost);
        descText.setText(membershipPackage.description);
        networkUntil.drawImageUrl(packageImage, membershipPackage.image_path, R.drawable.loading);

        return view;
    }

    public void onPackageItemClick(int position) {
        if (mPaymentLayout.getVisibility() == View.VISIBLE)
            return;

        mSelectedListId = position;
        mLoading.setVisibility(View.VISIBLE);
        new ApiRequestTask().execute("checkPackage");
    }

    public void parseCheckPackage(String result) {
        if (result != null) {
            try {
                MembershipPackage selectedPackage = mPackageList.get(mSelectedListId);
                JSONObject resJson = new JSONObject(result).getJSONObject("output");

                if (resJson.has("error_message")) {
                    Toast.makeText(getActivity().getApplicationContext(), resJson.getString("error_message") , Toast.LENGTH_LONG).show();
                    mSelectedListId = -1;
                    mPurchaseId = "";
                    mPaymentStatus = "";
                } else if (selectedPackage.cost.equals("0")){
                    PayPalButtonClick();
                } else {
                    mPaymentLayout.setVisibility(View.VISIBLE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Membership update failed. Please try again." , Toast.LENGTH_LONG).show();
        }
    }

    public void initPayPalMPL(){
        for (int i = 0; i < mGatewayList.size(); i++) {
            PaymentGateway gateway = mGatewayList.get(i);
            if (gateway == null || !gateway.id.equals("paypal"))
                continue;

            PayPal pp = PayPal.getInstance();
            if (pp == null) {
                if (gateway.is_test.equals("1"))
                    pp = PayPal.initWithAppID(getActivity().getApplicationContext(), "APP-80W284485P519543T", PayPal.ENV_SANDBOX);
                else
                    pp = PayPal.initWithAppID(getActivity().getApplicationContext(), "APP-80W284485P519543T", PayPal.ENV_LIVE);

                pp.setLanguage("en_US");
                pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);
                pp.setShippingEnabled(true);
                pp.setDynamicAmountCalculationEnabled(false);
            }

            mPaymentDescText.setText(gateway.description);
            showPayPalButton();
            break;
        }
    }

    public void showPayPalButton() {
        PayPal pp = PayPal.getInstance();
        mPaypalButton = pp.getCheckoutButton(getActivity().getApplicationContext(), PayPal.BUTTON_194x37, CheckoutButton.TEXT_PAY);
        mPaypalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                PayPalButtonClick();
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPaypalButton.setLayoutParams(params);
        mPaymentSubLayout.addView(mPaypalButton);
    }

    public void PayPalButtonClick() {
        mPaymentLayout.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
        new ApiRequestTask().execute("purchasePackage");

        mPaymentSubLayout.removeView(mPaypalButton);
        showPayPalButton();
    }

    public void parsePurchasePackage(String resString) {
        if (resString != null) {
            try {
                JSONObject mainJSON = new JSONObject(resString).getJSONObject("output");

                if (mainJSON.has("error_message")) {
                    Toast.makeText(getActivity().getApplicationContext(), Html.fromHtml(mainJSON.getString("error_message")).toString(), Toast.LENGTH_LONG).show();
                    mSelectedListId = -1;
                    mPurchaseId = "";
                    mPaymentStatus = "";
                } else if (mainJSON.has("message")) {
                    Toast.makeText(getActivity().getApplicationContext(), Html.fromHtml(mainJSON.getString("message")).toString(), Toast.LENGTH_LONG).show();
                    mSelectedListId = -1;
                    mPurchaseId = "";
                    mPaymentStatus = "";
                    new UpdateUserInfoTask().execute();
                } else {
                    mPurchaseId = mainJSON.getString("purchase_id");
                    for (int i = 0; i < mGatewayList.size(); i++) {
                        PaymentGateway gateway = mGatewayList.get(i);
                        if (gateway == null || !gateway.id.equals("paypal"))
                            continue;

                        MembershipPackage membershipPackage = mPackageList.get(mSelectedListId);
                        PayPalPayment payment = new PayPalPayment();
                        payment.setDescription(membershipPackage.description);
                        payment.setCurrencyType(membershipPackage.currency_id);
                        payment.setRecipient(gateway.paypal_email);
                        payment.setSubtotal(new BigDecimal(membershipPackage.cost));
                        payment.setPaymentType(PayPal.PAYMENT_TYPE_NONE);
                        payment.setPaymentSubtype(PayPal.PAYMENT_SUBTYPE_NONE);
                        payment.setInvoiceData(null);
                        payment.setMerchantName("");
                        payment.setCustomID("");
                        payment.setMemo("");

                        PayPal pp = PayPal.getInstance();
                        Intent paypalIntent = pp.checkout(payment, getActivity().getApplicationContext());
                        this.startActivityForResult(paypalIntent, 1);

                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Membership update failed. Please try again." , Toast.LENGTH_LONG).show();
            mSelectedListId = -1;
            mPurchaseId = "";
            mPaymentStatus = "";
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        PayPalActivityResult(requestCode, resultCode, intent);
    }

    public void PayPalActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK)
            mPaymentStatus = "completed";
        else if (resultCode == Activity.RESULT_CANCELED)
            mPaymentStatus = "cancel";
        else
            mPaymentStatus = "fail";

        mLoading.setVisibility(View.VISIBLE);
        new ApiRequestTask().execute("paymentComplete");
    }

    public void parsePaymentComplete(String resString) {
        mSelectedListId = -1;
        mPurchaseId = "";
        mPaymentStatus = "";

        if (resString != null) {
            try {
                JSONObject mainJSON = new JSONObject(resString).getJSONObject("output");

                if (mainJSON.has("error_message")) {
                    Toast.makeText(getActivity().getApplicationContext(), Html.fromHtml(mainJSON.getString("error_message")).toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), Html.fromHtml(mainJSON.getString("message")).toString(), Toast.LENGTH_LONG).show();
                    new UpdateUserInfoTask().execute();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Membership update failed. Please try again." , Toast.LENGTH_LONG).show();
        }
    }

    public void closePaymentLayout() {
        mPaymentLayout.setVisibility(View.INVISIBLE);

        mSelectedListId = -1;
        mPurchaseId = "";
        mPaymentStatus = "";
    }

    private class UpdateUserInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (isCancelled()) {
                return null;
            }

            String resultstring = null;
            try {
                // Use BasicNameValuePair to create GET data
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                pairs.add(new BasicNameValuePair("method", "accountapi.getUserInfo"));
                pairs.add(new BasicNameValuePair("user_id", user.getUserId()));
                pairs.add(new BasicNameValuePair("login", "1"));

                // url request
                String URL = null;
                if (Config.CORE_URL == null) {
                    URL = Config.makeUrl(user.getCoreUrl(), null, false);
                } else {
                    URL = Config.makeUrl(Config.CORE_URL, null, false);
                }

                // request GET method to server
                resultstring = networkUntil.makeHttpRequest(URL, "GET", pairs);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return resultstring;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(String result) {
            mLoading.setVisibility(View.GONE);

            if (result == null)
                return;

            try {
                JSONObject mainJson = new JSONObject(result);
                JSONObject outputJson = mainJson.getJSONObject("output");
                if (outputJson.has("info")) {
                    JSONObject info = outputJson.getJSONObject("info");
                    if (info.has("Member Since")) {
                        user.setMember_since(Html.fromHtml(info.getString("Member Since")).toString());
                    }
                    if (info.has("Membership")) {
                        user.setMembership(Html.fromHtml(info.getString("Membership")).toString());
                    }

                    BROADCAST.sideBarFragment.sa.notifyDataSetChanged();
                }

                mStatusText.setText(user.getMembership());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.onPostExecute(result);
        }
    }
}
