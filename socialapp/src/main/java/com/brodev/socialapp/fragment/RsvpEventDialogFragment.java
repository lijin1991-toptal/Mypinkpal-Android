package com.brodev.socialapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.http.NetworkUntil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class RsvpEventDialogFragment extends DialogFragment {
	
	private static String _module;
	private PhraseManager phraseManager;
	private static int rsvpId, _item;
	private int selected;
	private AlertDialog.Builder builder;
	private NetworkUntil networkUntil = new NetworkUntil();
	private User user;
	
	public static RsvpEventDialogFragment newInstance(String module, int item) {	
		Bundle b = new Bundle();
		RsvpEventDialogFragment f = new RsvpEventDialogFragment();
		f.setArguments(b);
		_module = module;
		_item = item;
		return f;
	}
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
	}
	
	private void checkSelected(int rsvpId) {
		if (rsvpId == 1) {
			selected = 0;
		} else if (rsvpId == 2) {
			selected = 1;
		} else if (rsvpId == 3) {
			selected = 2;
		} else {
			selected = -1;
		}
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		final CharSequence[] items = {
			phraseManager.getPhrase(getActivity().getApplicationContext(), "event.attending"),
			phraseManager.getPhrase(getActivity().getApplicationContext(), "event.maybe_attending"),
			phraseManager.getPhrase(getActivity().getApplicationContext(), "event.not_attending"),
		};
		
		builder = new AlertDialog.Builder(getActivity());
		
		builder.setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
					case 0:
						rsvpId = 1;
						break;
					case 1:
						rsvpId = 2;
						break;
					case 2:
						rsvpId = 3;
						break;
				}
			}
		});
		
		builder.setNegativeButton(phraseManager.getPhrase(getActivity().getApplicationContext(), "core.close"),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		
		builder.setPositiveButton(phraseManager.getPhrase(getActivity().getApplicationContext(), "event.update_your_rsvp"), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				new UpdateRsvpAsyncTask().execute(_module, String.valueOf(rsvpId));
				displayMessage(getActivity().getApplicationContext(), _module, String.valueOf(rsvpId));
				builder.create().dismiss();
			}
		});
		
		return builder.create();
	}
	
	/**
	 * Update rsvp 
	 */
	public class UpdateRsvpAsyncTask extends AsyncTask<String, Void, String> {
		
		String reString = null;
		
		@Override
		protected String doInBackground(String... params) {
			if (isCancelled()) {
				return null;
			}
			try {
				String URL = null;
				// String url
				if (Config.CORE_URL == null) {
					URL = Config.makeUrl(user.getCoreUrl(), "updateRSVP", true) + "&token=" + user.getTokenkey();
				} else {
					URL = Config.makeUrl(Config.CORE_URL, "updateRSVP", true) + "&token=" + user.getTokenkey();
				}
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("event_id", "" + params[0]));
				pairs.add(new BasicNameValuePair("rsvp_id", "" + params[1]));
				
				reString = networkUntil.makeHttpRequest(URL, "POST", pairs);

			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
			return reString;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	}
	
	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param message
	 */
	public static void displayMessage(Context context, String eventId, String rsvpId) {
        Intent intent = new Intent(Config.DISPLAY_UPDATE_RSVP);
       
        intent.putExtra("eventId", eventId);
        intent.putExtra("rsvpId", rsvpId);
        
        context.sendBroadcast(intent);
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		phraseManager = new PhraseManager(getActivity().getApplicationContext());
		user = (User) getActivity().getApplicationContext();
		checkSelected(_item);
		super.onCreate(savedInstanceState);
	}
	
}
