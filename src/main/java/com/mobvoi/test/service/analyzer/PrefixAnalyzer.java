/**
 * $Id$
 * Copyright 2009-2012 Oak Pacific Interactive. All rights reserved.
 */
package com.mobvoi.test.service.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.search.PrefixFilter;


/**
 * 逐字输入搜索专门写的分词器
 * 例如：abcd，分词成a，ab，abc，abcd
 * @author <a href="mailto:rebricate@gmail.com">刘刚</a>
 * @version 2013-9-28
 */
public final class PrefixAnalyzer extends Analyzer {

    /**
     * Creates
     * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
     * used to tokenize all the text in the provided {@link Reader}.
     * 
     * @return {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
     *         built from a {@link PrefixTokenizer} filtered with
     *         {@link PrefixFilter}
     */
      @Override
      protected TokenStreamComponents createComponents(String fieldName,
          Reader reader) {
        final Tokenizer source = new PrefixTokenizer(reader);
        return new TokenStreamComponents(source);
      }
  }
