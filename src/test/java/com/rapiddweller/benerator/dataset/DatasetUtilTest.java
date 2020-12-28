/*
 * (c) Copyright 2006-2020 by rapiddweller GmbH & Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from rapiddweller GmbH & Volker Bergmann.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.rapiddweller.benerator.dataset;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Test;

/**
 * Tests the {@link DatasetUtil}.<br/><br/>
 * Created: 31.01.2012 11:17:29
 * @since 0.7.5
 * @author Volker Bergmann
 */
public class DatasetUtilTest {
	
	@Test
	public void testDefaultLanguageForRegion() {
		assertEquals(Locale.getDefault(), DatasetUtil.defaultLanguageForRegion(null));
		assertEquals(Locale.getDefault(), DatasetUtil.defaultLanguageForRegion(""));
		assertEquals(Locale.US, DatasetUtil.defaultLanguageForRegion("US"));
		assertEquals(Locale.ENGLISH, DatasetUtil.defaultLanguageForRegion("UK"));
		assertEquals(Locale.UK, DatasetUtil.defaultLanguageForRegion("GB"));
		assertEquals(new Locale("hi", "IN"), DatasetUtil.defaultLanguageForRegion("IN"));
		assertEquals(Locale.GERMANY, DatasetUtil.defaultLanguageForRegion("DE"));
		assertEquals(Locale.GERMAN, DatasetUtil.defaultLanguageForRegion("dach"));
		assertEquals(Locale.ENGLISH, DatasetUtil.defaultLanguageForRegion("europe"));
		assertEquals(Locale.ENGLISH, DatasetUtil.defaultLanguageForRegion("world"));
	}
	
}