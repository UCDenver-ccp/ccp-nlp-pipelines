package edu.northwestern.at.utils.corpuslinguistics.tokenizer;

/**
 * this class is duplicated here and modified slightly to remove a static variable that was causing issues with UIMA AS parallel processing
 */

/*	Please see the license information at the end of this file. */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import edu.northwestern.at.utils.ListFactory;
import edu.northwestern.at.utils.PatternReplacer;

/**
 * Split text into tokens according the Penn Treebank tokenization rules.
 *
 * <p>
 * Based upon the sed script written by Robert McIntyre at
 * http://www.cis.upenn.edu/~treebank/tokenizer.sed .
 * </p>
 */

public class PennTreebankTokenizer extends AbstractWordTokenizer implements WordTokenizer {
	/** Replacement patterns for transforming original text. */

	private final List<PatternReplacer> pennPatterns = new ArrayList<PatternReplacer>(
			Arrays.asList(new PatternReplacer("``", "`` "), new PatternReplacer("''", "  ''"),
					new PatternReplacer("([?!\".,;:@#$%&])", " $1 "), new PatternReplacer("\\.\\.\\.", " ... "),
					new PatternReplacer("\\s+", " "),

					new PatternReplacer(",([^0-9])", " , $1"),

					new PatternReplacer("([^.])([.])([\\])}>\"']*)\\s*$", "$1 $2$3 "),

					new PatternReplacer("([\\[\\](){}<>])", " $1 "), new PatternReplacer("--", " -- "),

					new PatternReplacer("$", " "), new PatternReplacer("^", " "),

					new PatternReplacer("([^'])' ", "$1 ' "), new PatternReplacer("'([sSmMdD]) ", " '$1 "),
					new PatternReplacer("'ll ", " 'll "), new PatternReplacer("'re ", " 're "),
					new PatternReplacer("'ve ", " 've "), new PatternReplacer("'em ", " 'em "),
					new PatternReplacer("n't ", " n't "), new PatternReplacer("'LL ", " 'LL "),
					new PatternReplacer("'RE ", " 'RE "), new PatternReplacer("'EM ", " 'EM "),
					new PatternReplacer("'VE ", " 'VE "), new PatternReplacer("N'T ", " N'T "),

					new PatternReplacer(" ([Cc])annot ", " $1an not "), new PatternReplacer(" ([Dd])'ye ", " $1' ye "),
					new PatternReplacer(" ([Gg])imme ", " $1im me "), new PatternReplacer(" ([Gg])onna ", " $1on na "),
					new PatternReplacer(" ([Gg])otta ", " $1ot ta "), new PatternReplacer(" ([Ll])emme ", " $1em me "),
					new PatternReplacer(" ([Mm])ore'n ", " $1ore 'n "), new PatternReplacer(" '([Tt])is ", " '$1 is "),
					new PatternReplacer(" '([Tt])was ", " '$1 was "), new PatternReplacer(" ([Ww])anna ", " $1an na "),
					new PatternReplacer(" ([Ww])anna ", " $1an na "),
					new PatternReplacer(" ([Ww])haddya ", " $1ha dd ya "),
					new PatternReplacer(" ([Ww])hatcha ", " $1ha t cha "),

					new PatternReplacer("([A-MO-Za-mo-z])'([tT])", "$1 '$2"),

					new PatternReplacer(" ([A-Z]) \\.", " $1. "), new PatternReplacer("\\s+", " "),
					new PatternReplacer("^\\s+", "")));

	/**
	 * Create a simple word tokenizer.
	 */

	public String prepareTextForTokenization(String s) {
		for (int i = 0; i < pennPatterns.size(); i++) {
			s = pennPatterns.get(i).replace(s);
		}

		return s.trim();
	}

	/**
	 * Break text into word tokens.
	 *
	 * @param text
	 *            Text to break into word tokens.
	 *
	 * @return List of word tokens.
	 *
	 *         <p>
	 *         Word tokens may be words, numbers, punctuation, etc.
	 *         </p>
	 */

	public List<String> extractWords(String text) {
		// Holds listof tokenized words.

		List<String> result = ListFactory.createNewList();

		// Prepare text for tokenization
		// by splitting words and punctuation
		// according to Penn Treebank rules.

		String fixedText = prepareTextForTokenization(text);

		// All we have to do now is pick
		// up the individual "words" which
		// are separated by one or more blanks.
		// Use a StringTokenizer for this.

		StringTokenizer tokenizer = new StringTokenizer(fixedText);

		// Add each token to the results list.

		while (tokenizer.hasMoreTokens()) {
			result.add(tokenizer.nextToken());
		}
		// Return tokenizer list of words.
		return result;
	}

}

/*
 * Copyright (c) 2008, 2009 by Northwestern University. All rights reserved.
 * Developed by: Academic and Research Technologies Northwestern University
 * http://www.it.northwestern.edu/about/departments/at/ Permission is hereby
 * granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal with the Software
 * without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions: Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimers. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 * Neither the names of Academic and Research Technologies, Northwestern
 * University, nor the names of its contributors may be used to endorse or
 * promote products derived from this Software without specific prior written
 * permission. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS WITH THE SOFTWARE.
 */