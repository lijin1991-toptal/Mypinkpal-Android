package com.brodev.socialapp.fragment;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalPayment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.droidparts.activity.Activity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreditFragment extends SherlockFragment {

    private User user;
    private PhraseManager phraseManager;
    private ColorView colorView;

    private LayoutInflater                  mInflater;
    private ArrayList<CreditPackage>        mCreditList;
    private ArrayList<PaymentGateway>       mGatewayList;
    private CreditPackageAdapter            mCreditAdapter;
    private ListView                        mCreditListView;
    private RelativeLayout                  mPaymentLayout;
    private LinearLayout                    mPaymentSubLayout;
    private TextView                        mCloseText;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = (User) getActivity().getApplicationContext();
        phraseManager = new PhraseManager(getActivity().getApplicationContext());
        colorView = new ColorView(getActivity().getApplicationContext());

        mSelectedListId = -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //create view from credit_fragment xml
        View view = inflater.inflate(R.layout.credit_fragment, container, false);

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
                loadCreditPackages();
            }
        });

        mCreditList = new ArrayList<CreditPackage>();
        mCreditAdapter = new CreditPackageAdapter(getActivity().getApplicationContext(), mCreditList);
        mCreditListView = (ListView) view.findViewById(R.id.creditPackageList);
        mCreditListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onCreditItemClick(position);
            }
        });

        mGatewayList = new ArrayList<PaymentGateway>();
        mPaymentLayout = (RelativeLayout) view.findViewById(R.id.creditPaymentLayout);
        mPaymentSubLayout = (LinearLayout) view.findViewById(R.id.creditPaymentSubLayout);
        mCloseText = (TextView) view.findViewById(R.id.creditCloseButon);
        mCloseText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                closePaymentLayout();
            }
        });
        mLoading = (ProgressBar) view.findViewById(R.id.creditLoading);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadCreditPackages();
    }

    public class CreditPackage {
        public String id;
        public String title;
        public String description;
        public String is_active;
        public String cost;
        public String currency;
        public String credits;

        // constructor
        public CreditPackage() {
            super();

            id = "";
            title = "";
            description = "";
            is_active = "";
            cost = "";
            currency = "";
            credits = "";
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

                if (apiType.equals("getCreditPackages")) {
                    pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                    pairs.add(new BasicNameValuePair("method", "accountapi.getCreditPackages"));
                } else if (apiType.equals("getGateways")) {
                    URL = Config.makeUrl("http://mypinkpal.com/mypinkpal", null, false);
                    pairs.add(new BasicNameValuePair("mode", "getGateways"));
                } else if (apiType.equals("creditPurchaseComplete")) {
                    CreditPackage selectedPackage = mCreditList.get(mSelectedListId);
                    if (selectedPackage == null) return null;

                    pairs.add(new BasicNameValuePair("token", user.getTokenkey()));
                    pairs.add(new BasicNameValuePair("method", "accountapi.creditPurchaseComplete"));
                    pairs.add(new BasicNameValuePair("package_id", String.valueOf(selectedPackage.id)));
                    pairs.add(new BasicNameValuePair("credit", String.valueOf(selectedPackage.credits)));
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

            if (apiType.equals("getCreditPackages")) {
                if (result != null) {
                    showPackageList(result);
                    loadPaymentGateways();
                }
            } else if (apiType.equals("getGateways")) {
                parseGateways(result);
                initPayPalMPL();
            } else if (apiType.equals("creditPurchaseComplete")) {
                parsePurchaseComplete(result);
            }
        }
    }

    public class CreditPackageAdapter extends ArrayAdapter<CreditPackage> {

        public CreditPackageAdapter(Context context, ArrayList<CreditPackage> packages) {
            super(context, 0, packages);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return createPackageListItem((CreditPackage)getItem(position));
        }
    }

    public View createPackageListItem(CreditPackage creditPackage) {
        View view = mInflater.inflate(R.layout.credit_package_item, null);
        TextView titleText = (TextView) view.findViewById(R.id.creditPackageTitle);
        TextView descText = (TextView) view.findViewById(R.id.creditPackageDescription);
        TextView costText = (TextView) view.findViewById(R.id.creditPackageCost);
        TextView currencyText = (TextView) view.findViewById(R.id.creditPackageCurrency);
        TextView creditsText = (TextView) view.findViewById(R.id.creditPackageCredits);

        titleText.setText(creditPackage.title);
        descText.setText(creditPackage.description);
        costText.setText(creditPackage.cost);
        currencyText.setText(creditPackage.currency);
        creditsText.setText(creditPackage.credits);

        return view;
    }

    public void loadCreditPackages() {
        try {
            if (networkInfo != null && networkInfo.isConnected()) {
                noInternetLayout.setVisibility(View.GONE);
                mPaymentLayout.setVisibility(View.GONE);

                //fetch data
                mLoading.setVisibility(View.VISIBLE);
                new ApiRequestTask().execute("getCreditPackages");
            } else {
                // display error
                noInternetLayout.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);
                mPaymentLayout.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            noInternetLayout.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.GONE);
            mPaymentLayout.setVisibility(View.GONE);
        }
    }

    public void showPackageList(String resString) {
        try {
            mCreditList.clear();

            JSONObject mainJSON = new JSONObject(resString);
            JSONArray packageJsonArray = mainJSON.getJSONObject("output").getJSONArray("packages");
            JSONObject packageJson = null;

            for (int i = 0; i < packageJsonArray.length(); i++) {
                packageJson = packageJsonArray.getJSONObject(i);
                CreditPackage creditPackage = new CreditPackage();

                //set package id
                if (packageJson.has("package_id") && !packageJson.isNull("package_id"))
                    creditPackage.id = packageJson.getString("package_id");
                //set title
                if (packageJson.has("title") && !packageJson.isNull("title"))
                    creditPackage.title = Html.fromHtml(packageJson.getString("title")).toString();
                //set description
                if (packageJson.has("description") && !packageJson.isNull("description"))
                    creditPackage.description = Html.fromHtml(packageJson.getString("description")).toString();
                //set is_active
                if (packageJson.has("active") && !packageJson.isNull("active"))
                    creditPackage.is_active = packageJson.getString("active");
                //set cost
                if (packageJson.has("amount") && !packageJson.isNull("amount"))
                    creditPackage.cost = packageJson.getString("amount");
                //set currency
                if (packageJson.has("currency") && !packageJson.isNull("currency"))
                    creditPackage.currency = packageJson.getString("currency");
                //set recurring cost
                if (packageJson.has("credit") && !packageJson.isNull("credit"))
                    creditPackage.credits = packageJson.getString("credit");

                mCreditList.add(creditPackage);
            }

            mCreditListView.setAdapter(mCreditAdapter);
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

    public void onCreditItemClick(int position) {
        if (mPaymentLayout.getVisibility() == View.VISIBLE)
            return;

        mSelectedListId = position;
        mPaymentLayout.setVisibility(View.VISIBLE);
    }

    public void PayPalButtonClick() {
        mPaymentLayout.setVisibility(View.GONE);

        for (int i = 0; i < mGatewayList.size(); i++) {
            PaymentGateway gateway = mGatewayList.get(i);
            if (gateway == null || !gateway.id.equals("paypal"))
                continue;

            CreditPackage creditPackage = mCreditList.get(mSelectedListId);
            PayPalPayment payment = new PayPalPayment();
            payment.setDescription(creditPackage.description);
            payment.setCurrencyType(creditPackage.currency);
            payment.setRecipient(gateway.paypal_email);
            payment.setSubtotal(new BigDecimal(creditPackage.cost));
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

        mPaymentSubLayout.removeView(mPaypalButton);
        showPayPalButton();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        PayPalActivityResult(requestCode, resultCode, intent);
    }

    public void PayPalActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            mLoading.setVisibility(View.VISIBLE);
            new ApiRequestTask().execute("creditPurchaseComplete");
        } else {
            String message = "Purchase credit ";
            if (resultCode == Activity.RESULT_CANCELED)
                message += "canceled";
            else
                message += "failed";

            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    public void parsePurchaseComplete(String resString) {
        mSelectedListId = -1;

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
            Toast.makeText(getActivity().getApplicationContext(), "Purchase credit failed. Please try again." , Toast.LENGTH_LONG).show();
        }
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
                if (outputJson.has("total_credit")) {
                    user.setCredits(Integer.valueOf(outputJson.getString("total_credit")));
                    BROADCAST.sideBarFragment.sa.notifyDataSetChanged();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.onPostExecute(result);
        }
    }

    public void closePaymentLayout() {
        mPaymentLayout.setVisibility(View.GONE);
        mSelectedListId = -1;
    }

}
