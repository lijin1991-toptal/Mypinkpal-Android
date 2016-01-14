package com.brodev.socialapp.customview;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class LinkEnabledTextView extends TextView {
	
	// The String Containing the Text that we have to gather links from private
	// SpannableString linkableText;
	// Populating and gathering all the links that are present in the Text
	private ArrayList<Hyperlink> listOfLinks;
 
	// A Listener Class for generally sending the Clicks to the one which
	// requires it
	TextLinkClickListener mListener;
 
	// Pattern for gathering @usernames from the Text
	Pattern screenNamePattern = Pattern.compile("(@[a-zA-Z0-9_]+)");
 
	// Pattern for gathering #hasttags from the Text
	Pattern hashTagsPattern = Pattern.compile("(#[a-zA-Z0-9_-]+)");
 
	// Pattern for gathering http:// links from the Text
	Pattern hyperLinksPattern = Pattern.compile("([Hh][tT][tT][pP][sS]?:\\/\\/[^ ,'\">\\]\\)]*[^\\. ,'\">\\]\\)])");
 
	Context context;
	
	public LinkEnabledTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		listOfLinks = new ArrayList<Hyperlink>();
 
	}
	
	public LinkEnabledTextView(Context context) {
		super(context);
		this.context = context;
		listOfLinks = new ArrayList<Hyperlink>();
	}
 
	public SpannableString gatherLinksForText(Context context, String text) {
		
		SpannableString linkableText = new SpannableString(text);
		// gatherLinks basically collects the Links depending upon the Pattern
		// that we supply
		// and add the links to the ArrayList of the links
 
		gatherLinks(context, listOfLinks, linkableText, screenNamePattern);
		gatherLinks(context, listOfLinks, linkableText, hashTagsPattern);
		gatherLinks(context, listOfLinks, linkableText, hyperLinksPattern);
 
		for (int i = 0; i < listOfLinks.size(); i++) {
			Hyperlink linkSpec = listOfLinks.get(i);
			android.util.Log.v("listOfLinks :: " + linkSpec.textSpan,
					"listOfLinks :: " + linkSpec.textSpan + " " + linkSpec.span.toString());
 
			// this process here makes the Clickable Links from the text
 
			linkableText.setSpan(linkSpec.span, linkSpec.start, linkSpec.end,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
 
		// sets the text for the TextView with enabled links
 
		return linkableText;
	}
 
	// sets the Listener for later click propagation purpose
 
	public void setOnTextLinkClickListener(TextLinkClickListener newListener) {
		mListener = newListener;
	}
 
	// The Method mainly performs the Regex Comparison for the Pattern and adds
	// them to
	// listOfLinks array list
 
	private final void gatherLinks(Context context, ArrayList<Hyperlink> links, Spannable s,
			Pattern pattern) {
		// Matcher matching the pattern
		Matcher m = pattern.matcher(s);
 
		while (m.find()) {
			int start = m.start();
			int end = m.end();
 
			// Hyperlink is basically used like a structure for storing the
			// information about
			// where the link was found.
 
			Hyperlink spec = new Hyperlink();
 
			spec.textSpan = s.subSequence(start, end);
			spec.span = new InternalURLSpan(context, spec.textSpan.toString());
			spec.start = start;
			spec.end = end;
 
			links.add(spec);
		}
		
	}
 
	// This is class which gives us the clicks on the links which we then can
	// use.
 
	public class InternalURLSpan extends ClickableSpan {
		private String clickedSpan;
		private Context context;
		
		public InternalURLSpan(Context context, String clickedString) {
			clickedSpan = clickedString;
			this.context = context;
		}
 
		@Override
		public void onClick(View textView) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedSpan));
			browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(browserIntent);
		}
	}
 
	// Class for storing the information about the Link Location
 
	class Hyperlink {
		CharSequence textSpan;
		InternalURLSpan span;
		int start;
		int end;
		
		public Hyperlink () {
			
		}
	}
}