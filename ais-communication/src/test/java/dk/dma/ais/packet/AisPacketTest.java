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
package dk.dma.ais.packet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.message.IPositionMessage;
import dk.dma.ais.packet.AisPacketTagging.SourceType;
import dk.dma.ais.reader.AisPacketReader;
import dk.dma.ais.reader.AisStreamReader;
import dk.dma.ais.sentence.SentenceException;
import dk.dma.enav.util.function.Consumer;

public class AisPacketTest {

    @Test
    public void readPacketTest() throws IOException, InterruptedException {
        // Open input stream
        URL url = ClassLoader.getSystemResource("small_cb_example.txt");
        Assert.assertNotNull(url);
        InputStream inputStream = url.openStream();
        Assert.assertNotNull(inputStream);

        // Make AIS reader instance
        AisStreamReader aisReader = new AisStreamReader(inputStream);
        // Set the source name
        aisReader.setSourceId("some_file_dump");

        aisReader.registerPacketHandler(new Consumer<AisPacket>() {
            @Override
            public void accept(AisPacket aisPacket) {
                System.out.println("--\npacket received:\n" + aisPacket.getStringMessage());

                // Try to get timestamp
                Date timestamp = aisPacket.getTimestamp();
                System.out.println("timestamp: " + timestamp);
                Assert.assertNotNull(timestamp);

                // Get tagging
                AisPacketTagging tagging = AisPacketTagging.parse(aisPacket);
                Assert.assertEquals(tagging.getSourceId(), "some_file_dump");

                // Try to get AIS message
                try {
                    AisMessage message = aisPacket.getAisMessage();
                    if (message instanceof IPositionMessage) {
                        // Position message
                        ((IPositionMessage) message).getPos();
                    }
                } catch (AisMessageException | SixbitException e) {
                    // Failed to parse AIS message in VDM
                    e.printStackTrace();
                }

            }
        });

        aisReader.start();
        aisReader.join();

    }

    @Test
    public void retryTest() throws IOException, InterruptedException {
        // Open input stream
        URL url = ClassLoader.getSystemResource("retry_example.txt");
        Assert.assertNotNull(url);
        InputStream inputStream = url.openStream();
        Assert.assertNotNull(inputStream);

        // Make AIS reader instance
        AisStreamReader aisReader = new AisStreamReader(inputStream);
        aisReader.registerPacketHandler(new Consumer<AisPacket>() {            
            final int[] senders = {563510000, 211235220, 2655619, 246250000, 205634000, 211462260};
            int count;
            @Override            
            public void accept(AisPacket aisPacket) {                
                AisMessage message = null;
                try {
                    message = aisPacket.getAisMessage();
                } catch (AisMessageException | SixbitException e) {
                    Assert.fail(e.getMessage());
                }
                Assert.assertEquals(senders[count++], message.getUserId());
            }
        });
        aisReader.start();
        aisReader.join();
    }

    @Test
    public void packetFromStringTest() throws SentenceException, AisMessageException, SixbitException {
        String msg;
        msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
        msg += "\\1G2:0125,c:1354719387*0D\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
        msg += "\\2G2:0125*7B\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";
        AisPacket packet = AisPacketReader.from(msg);
        Assert.assertNotNull(packet);
        Assert.assertNotNull(packet.getVdm());
        Assert.assertNotNull(packet.getVdm().getTags());
        AisMessage aisMessage = AisMessage.getInstance(packet.getVdm());
        Assert.assertEquals(aisMessage.getMsgId(), 5);
    }

    @Test
    public void packetTaggingTest() throws SentenceException {
        String msg;
        msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
        msg += "\\1G2:0125,c:1354719387*0D\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
        msg += "\\2G2:0125*7B\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";
        AisPacket packet = AisPacketReader.from(msg);
        AisPacketTagging tags = AisPacketTagging.parse(packet);
        Assert.assertEquals(tags.getSourceId(), null);
        Assert.assertEquals(tags.getSourceCountry().getThreeLetter(), "NLD");
        Assert.assertEquals(tags.getTimestamp().getTime(), 1354719387000L);
        Assert.assertEquals(tags.getSourceBs(), null);
        Assert.assertEquals(tags.getSourceType(), null);

        msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
        msg += "\\si:AISD*3F\\\r\n";
        msg += "\\1G2:0125,c:1354719387*0D\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
        msg += "\\2G2:0125*7B\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";
        packet = AisPacketReader.from(msg);
        tags = AisPacketTagging.parse(packet);
        Assert.assertEquals(tags.getSourceId(), "AISD");
        Assert.assertEquals(tags.getSourceCountry().getThreeLetter(), "NLD");
        Assert.assertEquals(tags.getTimestamp().getTime(), 1354719387000L);
        Assert.assertEquals(tags.getSourceBs(), null);
        Assert.assertEquals(tags.getSourceType(), null);

        msg = "$PGHP,1,2013,3,13,10,39,18,375,219,,2190047,1,4A*57\r\n";
        msg += "\\si:AISD,sb:2190048,sc:SWE,st:SAT*1E\\\r\n";
        msg += "\\g:1-2-0136,c:1354725824*22\\!BSVDM,2,1,4,B,53B>2V000000uHH4000@T4p4000000000000000S30C6340006h00000,0*4C\r\n";
        msg += "\\g:2-2-0136*59\\!BSVDM,2,2,4,B,000000000000000,2*3A";
        packet = AisPacketReader.from(msg);
        tags = AisPacketTagging.parse(packet);
        Assert.assertEquals(tags.getSourceId(), "AISD");
        Assert.assertEquals(tags.getSourceCountry().getThreeLetter(), "SWE");
        Assert.assertEquals(tags.getTimestamp().getTime(), 1354725824000L);
        Assert.assertEquals(tags.getSourceBs().intValue(), 2190048);
        Assert.assertEquals(tags.getSourceType(), SourceType.SATELLITE);
    }

}
