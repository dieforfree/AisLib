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
package dk.dma.ais.configuration.bus.consumer;

import javax.xml.bind.annotation.XmlSeeAlso;

import dk.dma.ais.bus.AisBusConsumer;
import dk.dma.ais.configuration.bus.AisBusSocketConfiguration;

@XmlSeeAlso({ StdoutConsumerConfiguration.class, TcpWriterConsumerConfiguration.class, TcpServerConsumerConfiguration.class,
        DistributerConsumerConfiguration.class })
public abstract class AisBusConsumerConfiguration extends AisBusSocketConfiguration {

    private int consumerPullMaxElements = 1000;
    private int consumerQueueSize = 10000;

    public AisBusConsumerConfiguration() {

    }

    public int getConsumerQueueSize() {
        return consumerQueueSize;
    }

    public void setConsumerQueueSize(int consumerQueueSize) {
        this.consumerQueueSize = consumerQueueSize;
    }

    public int getConsumerPullMaxElements() {
        return consumerPullMaxElements;
    }

    public void setConsumerPullMaxElements(int consumerPullMaxElements) {
        this.consumerPullMaxElements = consumerPullMaxElements;
    }

    protected AisBusConsumer configure(AisBusConsumer consumer) {
        consumer.setConsumerPullMaxElements(consumerPullMaxElements);
        consumer.setConsumerQueueSize(consumerQueueSize);
        super.configure(consumer);
        return consumer;
    }

}
