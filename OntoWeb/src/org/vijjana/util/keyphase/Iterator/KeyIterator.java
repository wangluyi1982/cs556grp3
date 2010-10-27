/*
 * Iterator.java
 *
 * Created on September 22, 2008, 10:28 PM
 * Copyright 2008 vijjana.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vijjana.util.keyphase.Iterator;

import org.vijjana.util.stemmer.LovinsStemmer;
import org.vijjana.util.stemmer.PorterStemmer;
import org.vijjana.util.stemmer.SremovalStemmer;

public abstract class KeyIterator {

	LovinsStemmer lsm = new LovinsStemmer();
	SremovalStemmer srs = new SremovalStemmer();
	PorterStemmer ps = new PorterStemmer();

	public KeyIterator(String input, String patternStr) {

	}

	public KeyIterator(String input, String patternStr, int number) {

	}

	public String stem(String word) {
		String result = word;
		result = lsm.stem(result);
		result = srs.stem(result);
		result = ps.stem(result);
		return result;
	}

}
