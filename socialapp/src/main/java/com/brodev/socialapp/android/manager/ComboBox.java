package com.brodev.socialapp.android.manager;

import java.util.ArrayList;
import java.util.List;

import com.brodev.socialapp.config.Config;
import com.brodev.socialapp.entity.ComboBoxItem;
import com.mypinkpal.app.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ComboBox {

	// parent layout
	Context context;
	private String valueChoose;
	private List<String> listComboShow = new ArrayList<String>();
	private ArrayList<ComboBoxItem> listCombo = new ArrayList<ComboBoxItem>();
	
	public ComboBox(Context context) {
		this.context = context;
	}

	public String getValue() {
		return valueChoose;
	}

	/**
	 * Add Comment to View
	 * 
	 * @param listComment2
	 */
	public void addComboToView(Context context, ArrayList<ComboBoxItem> listItems, 
			String defaultValue, LinearLayout parentLayout, String title, String action) {
        int valuePosition = 0;
        ComboBoxItem item = null;
        listComboShow = new ArrayList<String>();
        listComboShow.clear();

        for (int i = 0; i < listItems.size(); i++) {
            item = listItems.get(i);
            listComboShow.add(item.getName());
            if (item.getValue().equals(defaultValue)) {
                valuePosition = i;
            }
        }
        valueChoose = defaultValue;
        listCombo = listItems;

        Spinner spinner = new Spinner(context);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listComboShow);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(valuePosition);
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener(action));

        //add view
        if (title != null) {
            TextView titleText = new TextView(context);
            titleText.setTextColor(Color.BLACK);
            titleText.setText(title);
            parentLayout.addView(titleText);
        }

        parentLayout.addView(spinner);
    }

	public class CustomOnItemSelectedListener implements OnItemSelectedListener {
		
		private String action;
		
		public CustomOnItemSelectedListener(String action) {
			this.action = action;
		}
		
		public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
			if (listCombo.size() > 0) {
				valueChoose = listCombo.get(pos).getValue();
				if ("country".equals(action)) {
					displayMessage(context, valueChoose);
				} else if ("category".equals(action)) {
					displayCategoryMessage(context, valueChoose);
				}
			}
			
		}
	 
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		}
	}
	
	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param message
	 */
	public static void displayMessage(Context context, String bRequest) {
        Intent intent = new Intent(Config.DISPLAY_REQUEST_CHILDREN_COUNTRY);
       
        intent.putExtra("request", bRequest);
        
        context.sendBroadcast(intent);
    }
	
	/**
	 * This method is defined in the common helper because it's used both by
     * the UI and the background service.
	 * @param context
	 * @param message
	 */
	public static void displayCategoryMessage(Context context, String bRequest) {
        Intent intent = new Intent(Config.DISPLAY_REQUEST_CHILDREN_CATEGORY);
       
        intent.putExtra("request", bRequest);
        
        context.sendBroadcast(intent);
    }
}
