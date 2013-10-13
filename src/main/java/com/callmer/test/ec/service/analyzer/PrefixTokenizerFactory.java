/**
 * $Id$
 * Copyright 2009-2012 Oak Pacific Interactive. All rights reserved.
 */
package com.callmer.test.ec.service.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.util.TokenizerFactory;


/**
 * 
 * @author <a href="mailto:rebricate@gmail.com">刘刚</a>
 * @version 2013-9-28
 */
public class PrefixTokenizerFactory extends TokenizerFactory {
    
    public PrefixTokenizer create(Reader in) {
      return new PrefixTokenizer(in);
    }
  }

