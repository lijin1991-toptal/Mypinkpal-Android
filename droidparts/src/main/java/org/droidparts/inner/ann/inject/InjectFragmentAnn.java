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
package org.droidparts.inner.ann.inject;

import org.droidparts.annotation.inject.InjectFragment;

public class InjectFragmentAnn extends InjectAnn<InjectFragment> {

	public final int id;

	public InjectFragmentAnn(InjectFragment annotation) {
		super(InjectFragment.class);
		id = annotation.id();
	}

}
