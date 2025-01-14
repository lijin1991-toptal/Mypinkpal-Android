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
package org.droidparts.inner.converter;

import org.droidparts.inner.TypeHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;

public class LongConverter extends Converter<Long> {

	@Override
	public boolean canHandle(Class<?> cls) {
		return TypeHelper.isLong(cls);
	}

	@Override
	public String getDBColumnType() {
		return INTEGER;
	}

	@Override
	public <V> Long readFromJSON(Class<Long> valType, Class<V> componentType,
			JSONObject obj, String key) throws JSONException {
		return obj.getLong(key);
	}

	@Override
	protected <V> Long parseFromString(Class<Long> valType,
			Class<V> componentType, String str) {
		return Long.valueOf(str);
	}

	@Override
	public <V> void putToContentValues(Class<Long> valueType,
			Class<V> componentType, ContentValues cv, String key, Long val) {
		cv.put(key, val);
	}

	@Override
	public <V> Long readFromCursor(Class<Long> valType, Class<V> componentType,
			Cursor cursor, int columnIndex) {
		return cursor.getLong(columnIndex);
	}

}
