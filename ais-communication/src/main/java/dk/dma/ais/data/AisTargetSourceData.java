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
package dk.dma.ais.data;

import java.io.Serializable;
import java.util.Date;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacketTagging;
import dk.dma.ais.packet.AisPacketTagging.SourceType;
import dk.dma.ais.proprietary.GatehouseSourceTag;
import dk.dma.ais.proprietary.IProprietaryTag;

/**
 * Class to data about the source of an AIS target
 */
public class AisTargetSourceData implements Serializable {

    private static final long serialVersionUID = 1L;

    private AisPacketTagging tagging = new AisPacketTagging();
    private String sourceRegion;
    private Date created;

    public AisTargetSourceData() {
        this.created = new Date();
    }

    public void update(AisMessage aisMessage) {
        this.sourceRegion = null;
        // Get source region from Gatehouse tag
        if (aisMessage.getTags() != null) {
            for (IProprietaryTag tag : aisMessage.getTags()) {
                if (tag instanceof GatehouseSourceTag) {
                    GatehouseSourceTag ghTag = (GatehouseSourceTag) tag;
                    this.sourceRegion = ghTag.getRegion();
                }
            }
        }
        this.tagging = AisPacketTagging.parse(aisMessage.getVdm());
    }

    public AisPacketTagging getTagging() {
        return tagging;
    }

    public void setTagging(AisPacketTagging tagging) {
        this.tagging = tagging;
    }

    public String getSourceRegion() {
        return sourceRegion;
    }

    public void setSourceRegion(String sourceRegion) {
        this.sourceRegion = sourceRegion;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isSatData() {
        SourceType sourceType = tagging.getSourceType();
        return sourceType != null && sourceType == SourceType.SATELLITE;
    }

    public String getSourceType() {
        SourceType sourceType = tagging.getSourceType();
        if (sourceType != null && sourceType == SourceType.SATELLITE) {
            return "SAT";
        } else {
            return "LIVE";
        }
    }

}
