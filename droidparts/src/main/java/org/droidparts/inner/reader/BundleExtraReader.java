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
package org.droidparts.inner.reader;

import android.app.Activity;
import android.os.Bundle;

public class BundleExtraReader {

	static Object readVal(Object obj, String key, boolean optional)
			throws Exception {
		Bundle data;
		if (obj instanceof Activity) {
			data = ((Activity) obj).getIntent().getExtras();
		} else if (ValueReader.useSupport()) {
			data = SupportFragmentReader.getFragmentArguments(obj);
		} else if (ValueReader.nativeAvailable()) {
			data = NativeFragmentReader.getFragmentArguments(obj);
		} else {
			throw new IllegalArgumentException();
		}
		Object val = data.get(key);
		if (val == null && !optional) {
			throw new Exception("Bundle missing required key: " + key);
		} else {
			return val;
		}
	}
}
