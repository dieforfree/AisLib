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
package dk.dma.ais.configuration.bus.provider;

import javax.xml.bind.annotation.XmlSeeAlso;

import dk.dma.ais.bus.AisBusProvider;
import dk.dma.ais.configuration.bus.AisBusSocketConfiguration;

@XmlSeeAlso({ TcpClientProviderConfiguration.class, TcpServerProviderConfiguration.class, FileReaderProviderConfiguration.class,
        CollectorProviderConfiguration.class, RepeatingFileReaderProviderConfiguration.class })
public abstract class AisBusProviderConfiguration extends AisBusSocketConfiguration {

    public AisBusProviderConfiguration() {

    }

    protected AisBusProvider configure(AisBusProvider provider) {
        super.configure(provider);
        return provider;
    }

}
