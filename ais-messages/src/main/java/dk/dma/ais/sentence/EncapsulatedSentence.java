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
package dk.dma.ais.sentence;

import dk.dma.ais.binary.BinArray;
import dk.dma.ais.binary.SixbitEncoder;
import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessage;

/**
 * Abstract base class for encapsulating sentences VDM, ABM and BBM
 */
public abstract class EncapsulatedSentence extends Sentence {

    protected int msgId;
    protected int total;
    protected Integer sequence;
    private int lastSeq = -1;
    protected int num;
    protected Character channel;
    protected BinArray binArray = new BinArray();
    protected boolean completePacket;
    protected String sixbitString = "";
    protected int padBits;

    public EncapsulatedSentence() {
        super();
        channel = null;
    }

    /**
     * Base parse method to be used by extending classes
     */
    @Override
    protected void baseParse(String line) throws SentenceException {

        super.baseParse(line);

        // Should at least have four fields
        if (fields.length < 4) {
            throw new SentenceException("Sentence have less than four fields");
        }

        // Get sentence count properties
        int thisTotal = Sentence.parseInt(fields[1]);
        int thisNum = Sentence.parseInt(fields[2]);
        int thisSeq = 0;
        try {
            thisSeq = Sentence.parseInt(fields[3]);
        } catch (SentenceException e) {
            // null sequence is not fatal
        }

        if (lastSeq < 0) {
            // New group of sentences
            total = thisTotal;
            num = thisNum;
            sequence = thisSeq;
            lastSeq = thisSeq;
            if (num != 1 || num > total) {
                throw new SentenceException("Out of sequence sentence: " + line);
            }
        } else {
            // Sentence part of existing group
            if (total != thisTotal || thisNum != num + 1 || thisSeq != lastSeq) {
                throw new SentenceException("Out of sequence sentence: " + line);
            }
            num = thisNum;
        }

        // Are we done
        if (num == total) {
            completePacket = true;
            lastSeq = -1;
        }

    }

    /**
     * Encode method to be used by extending classes
     */
    protected void encode() {
        super.encode();
        encodedFields.add(Integer.toString(total));
        encodedFields.add(Integer.toString(num));
        String seq = sequence == null ? "" : Integer.toString(sequence);
        encodedFields.add(seq);
        encodedFields.add(channel != null ? Character.toString(channel) : "");
        encodedFields.add(sixbitString);
        encodedFields.add(Integer.toString(padBits));
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * Get total number of actual sentences
     * 
     * @return number of sentences
     */
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * Get sentence number
     * 
     * @return
     */
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Character getChannel() {
        return channel;
    }

    public void setChannel(Character channel) {
        this.channel = channel;
    }

    /**
     * Get binary encapsulated data
     * 
     * @return
     */
    public BinArray getBinArray() {
        return binArray;
    }

    /**
     * Set binary encapsulated data
     * 
     * @param binArray
     */
    public void setBinArray(BinArray binArray) {
        this.binArray = binArray;
    }

    /**
     * Set binary part and pad bits from encoder
     * 
     * @param encoder
     * @throws SixbitException
     */
    public void setEncodedMessage(SixbitEncoder encoder) throws SixbitException {
        sixbitString = encoder.encode();
        padBits = encoder.getPadBits();
    }

    /**
     * Set the binary encapsulated data from AIS message
     * 
     * @param aisMessage
     * @throws SixbitException
     */
    public void setMessageData(AisMessage aisMessage) throws SixbitException {
        this.msgId = aisMessage.getMsgId();
        SixbitEncoder encoder = aisMessage.getEncoded();
        sixbitString = encoder.encode();
        padBits = encoder.getPadBits();
    }

    public int getPadBits() {
        return padBits;
    }

    /**
     * @return is complete packet has been received
     */
    public boolean isCompletePacket() {
        return completePacket;
    }

    /**
     * Set the six bit string of the sentence
     * 
     * @param sixbitString
     */
    public void setSixbitString(String sixbitString) {
        this.sixbitString = sixbitString;
    }

    public void setPadBits(int padBits) {
        this.padBits = padBits;
    }

    public String getSixbitString() {
        return sixbitString;
    }

}
