/* Copyright 2012-2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.cache.web.filter;

import grails.plugin.cache.SerializableOutputStream;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletOutputStream;

/**
 * A custom {@link javax.servlet.ServletOutputStream} for use by our filters.
 *
 * Based on net.sf.ehcache.constructs.web.filter.FilterServletOutputStream.
 *
 * @author Greg Luck
 * @author Burt Beckwith
 */
public class FilterServletOutputStream extends ServletOutputStream implements Serializable {

	private static final long serialVersionUID = 1;

	protected SerializableOutputStream stream;

	public FilterServletOutputStream(final SerializableOutputStream stream) {
		this.stream = stream;
	}

	@Override
	public void write(final int b) throws IOException {
		stream.write(b);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		stream.write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		stream.write(b, off, len);
	}
}
