/*
 * TokenIterator.java
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

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vijjana.util.stemmer.SremovalStemmer;

/**
 * @author luyi Wang
 * @version 1.0
 * 
 * 
 *          build an TokenIterator which iterator the words which already
 *          eliminates unknown symbols and single Symbols.
 * 
 */
public class TokenIterator extends KeyIterator implements Iterator {

	// Holds the original input to search for tokens
	private final String input;

	// Used to find tokens
	private final Matcher matcher;

	// The current matched value.
	private String match;

	private final SremovalStemmer srm = new SremovalStemmer();
	private final int counter = 0;

	/**
	 * patternStr is a regular expression pattern that identifies tokens. only
	 * those tokens that match the pattern are returned.
	 * 
	 * @param input
	 *            : Original String
	 * 
	 * @param patternStr
	 *            the pattern String which is the regular expression.
	 */

	public TokenIterator(String input, String patternStr) {
		super(input, patternStr);
		this.input = input;

		// Compile pattern and prepare input
		Pattern pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(input);
	}

	/**
	 * @Returns true if there are more tokens or delimiters.(non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (matcher == null) {
			System.out.println("no Match");
			return false;
		}
		if (matcher.find()) {
			match = matcher.group();
		}
		return match != null;
	}

	/**
	 * @Returns the next token (or delimiter if returnDelims is
	 *          true).(non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		String result = null;

		if (match != null) {
			result = match;
			result = srm.stem(result);
			match = null;
		}
		return result;
	}

	/**
	 * @Returns true if the call to next() will return a token rather than a
	 *          delimiter.
	 */
	public boolean isNextToken() {
		return match != null;
	}

	/**
	 * remove Not supported.(non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
        }
        public String stem(String input)
        {
            return super.stem(input);
        }
	public static void main(String[] args) {
		String str = "The project that is the subject of this report was approved by the Governing Board of the National Research Council, whose members are drawn from the councils of the"
				+ "National Academy of Sciences' the National Academy of Engineering, and the Institute of Medicine. The members of the committee responsible for the report were chosen for their special competences and with regard for appropriate balance.";
		String patternStr = "([a-zA-Z]+){2,100}";
		try {
			Iterator tokenIterator = new TokenIterator(str, patternStr);
			int i = 0;
			for (; tokenIterator.hasNext();) {
				String pstr = (String) tokenIterator.next();
				System.out.println(pstr);
				i++;
			}
			System.out.println(i);
		} catch (Exception e) {
			System.out.print(e.getStackTrace());
		}
	}
}
