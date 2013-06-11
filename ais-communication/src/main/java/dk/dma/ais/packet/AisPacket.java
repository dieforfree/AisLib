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

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.message.IPositionMessage;
import dk.dma.ais.reader.AisPacketReader;
import dk.dma.ais.sentence.SentenceException;
import dk.dma.ais.sentence.Vdm;
import dk.dma.enav.model.geometry.Position;
import dk.dma.enav.model.geometry.PositionTime;

/**
 * Encapsulation of the VDM lines containing a single AIS message including leading proprietary tags and comment/tag blocks.
 * 
 * @author Kasper Nielsen
 */
public class AisPacket implements Comparable<AisPacket> {

    private final transient long receiveTimestamp;
    private final String rawMessage;
    private transient Vdm vdm;
    private AisMessage message;

    public AisPacket(String stringMessage) {
        this(stringMessage, System.currentTimeMillis());
    }

    public AisPacket(String stringMessage, long receiveTimestamp) {
        this.rawMessage = requireNonNull(stringMessage);
        this.receiveTimestamp = receiveTimestamp;
    }

    public AisPacket(Vdm vdm, String stringMessage) {
        this(vdm, stringMessage, System.currentTimeMillis());
    }

    public AisPacket(Vdm vdm, String stringMessage, long receiveTimestamp) {
        this(stringMessage, receiveTimestamp);
        this.vdm = vdm;
    }

    /**
     * Calculates a 128 bit hash on the received package.
     * 
     * @return a 128 hash on the received package
     */
    public byte[] calculateHash128() {
        return Hashing.murmur3_128().hashString(rawMessage).asBytes();
    }

    public static AisPacket fromByteArray(byte[] array) {
        byte[] b = Arrays.copyOfRange(array, 1, array.length);
        return from(new String(b, StandardCharsets.US_ASCII), -1);
    }

    public byte[] toByteArray() {
        return Bytes.concat(new byte[] { 1 }, rawMessage.getBytes(StandardCharsets.US_ASCII));
    }

    public long getBestTimestamp() {
        Date date = getTimestamp();
        return date == null ? receiveTimestamp : date.getTime();
    }

    public long getReceiveTimestamp() {
        return receiveTimestamp;
    }

    public String getStringMessage() {
        return rawMessage;
    }

    public List<String> getStringMessageLines() {
        return Arrays.asList(rawMessage.split("\\r?\\n"));
    }

    /**
     * Get existing VDM or parse one from message string
     * 
     * @return Vdm
     */
    public Vdm getVdm() {
        if (vdm == null) {
            AisPacket packet;
            try {
                packet = AisPacketReader.from(rawMessage);
                if (packet != null) {
                    vdm = packet.getVdm();
                }
            } catch (SentenceException e) {
                e.printStackTrace();
                return null;
            }
        }
        return vdm;
    }

    // TODO fix
    public AisMessage tryGetAisMessage() {
        try {
            return getAisMessage();
        } catch (AisMessageException | SixbitException ignore) {
            return null;
        }
    }

    /**
     * Try to get AIS message from packet
     * 
     * @return
     * @throws SixbitException
     * @throws AisMessageException
     */
    public AisMessage getAisMessage() throws AisMessageException, SixbitException {
        if (message != null || getVdm() == null) {
            return message;
        }
        return this.message = AisMessage.getInstance(getVdm());
    }

    /**
     * Check if VDM contains a valid AIS message
     * 
     * @return
     */
    public boolean isValidMessage() {
        return tryGetAisMessage() != null;
    }

    /**
     * Try to get timestamp for packet.
     * 
     * @return
     */
    public Date getTimestamp() {
        if (getVdm() == null) {
            return null;
        }
        return vdm.getTimestamp();
    }

    public PositionTime tryGetPositionTime() {
        AisMessage m = tryGetAisMessage();
        if (m instanceof IPositionMessage) {
            Position p = ((IPositionMessage) m).getPos().getGeoLocation();
            return p == null ? null : PositionTime.create(p, getBestTimestamp());
        }
        return null;

    }

    public static AisPacket from(String stringMessage, long receiveTimestamp) {
        return new AisPacket(stringMessage, receiveTimestamp);
    }

    public static AisPacket from(String stringMessage) {
        return new AisPacket(stringMessage, System.currentTimeMillis());
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(AisPacket p) {
        Date t1 = getTimestamp();
        Date t2 = p.getTimestamp();
        if (t2 == null) {
            return -1;
        }
        if (t1 == null) {
            return 1;
        }
        return t1.compareTo(t2);
    }
}
