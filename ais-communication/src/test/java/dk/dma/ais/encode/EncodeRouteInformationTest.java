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
package dk.dma.ais.encode;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import dk.dma.ais.binary.SixbitException;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage6;
import dk.dma.ais.message.AisMessageException;
import dk.dma.ais.message.AisPosition;
import dk.dma.ais.message.binary.AddressedRouteInformation;
import dk.dma.ais.message.binary.AisApplicationMessage;
import dk.dma.ais.message.binary.RouteInformation;
import dk.dma.ais.sentence.Abm;
import dk.dma.ais.sentence.SentenceException;
import dk.dma.ais.sentence.Vdm;
import dk.dma.enav.model.geometry.Position;

public class EncodeRouteInformationTest {

    /**
     * Test encoding of binary route information message
     * 
     * @throws SentenceException
     * @throws AisMessageException
     * @throws BitExhaustionException
     */
    @Test
    public void addressedRouteEncode() throws SentenceException, SixbitException, AisMessageException {
        int userId = 992199007;
        int destination = 219015063;

        // Make ASM
        AddressedRouteInformation route = new AddressedRouteInformation();
        route.setSenderClassification(1);
        route.setRouteType(RouteInformation.RouteType.RECOMMENDED.ordinal());
        route.setStartMonth(1);
        route.setStartDay(13);
        route.setStartHour(16);
        route.setStartMin(50);
        route.setDuration(35);
        route.setMsgLinkId(10);
        // Add waypoints
        List<AisPosition> waypoints = new ArrayList<>();
        waypoints.add(new AisPosition(Position.create(55.845283333333334, 12.704933333333333)));
        waypoints.add(new AisPosition(Position.create(55.913383333333336, 12.6453)));
        waypoints.add(new AisPosition(Position.create(55.93476666666667, 12.644016666666667)));
        waypoints.add(new AisPosition(Position.create(55.97728333333333, 12.7015)));
        waypoints.add(new AisPosition(Position.create(56.00, 12.8)));
        waypoints.add(new AisPosition(Position.create(56.10, 12.9)));
        for (AisPosition aisPosition : waypoints) {
            route.addWaypoint(aisPosition);
        }

        // Make addressed binary AIS message
        AisMessage6 msg6 = new AisMessage6();
        msg6.setUserId(userId);
        msg6.setDestination(destination);
        msg6.setRetransmit(0);
        msg6.setAppMessage(route);

        System.out.println("Route message 6: " + msg6);

        // Make ABM sentences
        Abm abm = new Abm();
        abm.setTalker("AI");
        abm.setTotal(1);
        abm.setNum(1);
        abm.setDestination(destination);
        abm.setBinaryData(msg6);
        abm.setSequence(0);
        String encoded = abm.getEncoded();
        System.out.println("Route ABM encoded: " + encoded);

        // Make VDM sentences
        String[] vdms = Vdm.createSentences(msg6, 0);
        System.out.println("Route VDM encoded:\n" + StringUtils.join(vdms, "\r\n"));

        // Decode VDM sentences
        Vdm vdm = new Vdm();
        for (int i = 0; i < vdms.length; i++) {
            int result = vdm.parse(vdms[i]);
            if (i < vdms.length - 1) {
                Assert.assertEquals(result, 1);
            } else {
                Assert.assertEquals(result, 0);
            }

        }
        // Extract AisMessage6
        msg6 = (AisMessage6) AisMessage.getInstance(vdm);
        System.out.println("msg6 decoded: " + msg6);
        // Get the ASM
        AisApplicationMessage appMsg = msg6.getApplicationMessage();
        AddressedRouteInformation parsedRoute = (AddressedRouteInformation) appMsg;
        System.out.println("msg 6 application: " + appMsg);

        // Assert if mathes original
        Assert.assertEquals(parsedRoute.getWaypointCount(), waypoints.size());
        Assert.assertEquals(route.getMsgLinkId(), parsedRoute.getMsgLinkId());
        Assert.assertEquals(route.getSenderClassification(), parsedRoute.getSenderClassification());
        Assert.assertEquals(route.getRouteType(), parsedRoute.getRouteType());
        Assert.assertEquals(route.getDuration(), parsedRoute.getDuration());
        Assert.assertEquals(route.getStartMonth(), parsedRoute.getStartMonth());
        Assert.assertEquals(route.getStartDay(), parsedRoute.getStartDay());
        Assert.assertEquals(route.getStartHour(), parsedRoute.getStartHour());
        Assert.assertEquals(route.getStartMin(), parsedRoute.getStartMin());
        for (int i = 0; i < parsedRoute.getWaypointCount(); i++) {
            AisPosition parsedWp = parsedRoute.getWaypoints().get(i);
            AisPosition orgWp = waypoints.get(i);
            Assert.assertTrue(parsedWp.equals(orgWp));
        }

    }

}
