/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.bus.provider;

import java.io.InputStream;

import dk.dma.ais.reader.AisStreamReader;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Provider reading from stream
 */
@ThreadSafe
public class StreamReaderProvider extends AisReaderProvider {

    @GuardedBy("this")
    private InputStream stream;

    public StreamReaderProvider() {
        super();
    }

    public StreamReaderProvider(InputStream stream) {
        super();
        this.stream = stream;
    }

    protected synchronized void setStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public synchronized void init() {
        setAisReader(new AisStreamReader(stream));
        super.init();
    }

}
