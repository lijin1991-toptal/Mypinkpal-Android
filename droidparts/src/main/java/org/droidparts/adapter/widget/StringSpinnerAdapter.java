/**
 * Copyright 2013 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.adapter.widget;

import java.util.Arrays;
import java.util.List;

import android.widget.Spinner;

public class StringSpinnerAdapter extends SpinnerAdapter<String> {

	public StringSpinnerAdapter(Spinner spinner, int stringArrResId) {
		this(spinner, spinner.getContext().getResources()
				.getStringArray(stringArrResId));
	}

	public StringSpinnerAdapter(Spinner spinner, String[] arr) {
		this(spinner, Arrays.asList(arr));
	}

	public StringSpinnerAdapter(Spinner spinner, List<String> list) {
		super(spinner, list);
	}

}
