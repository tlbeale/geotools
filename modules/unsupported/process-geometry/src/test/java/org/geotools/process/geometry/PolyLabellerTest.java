/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2016, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */

package org.geotools.process.geometry;

import org.geotools.geometry.jts.GeometryBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class PolyLabellerTest {

    @Test
    public void testSquare() throws ParseException {
        WKTReader reader = new WKTReader();
        GeometryBuilder gb = new GeometryBuilder();

        Polygon p = (Polygon) reader.read("Polygon(( 0 0, 10 0, 10 10, 0 10, 0 0))");
        Point point = (Point) PolyLabeller.getPolylabel(p, 1);

        Point expected = gb.point(5, 5);
        Assert.assertEquals(expected, point);
    }

    @Test
    public void testDoubleDiamond() throws ParseException {

        WKTReader reader = new WKTReader();
        GeometryBuilder gb = new GeometryBuilder();

        Polygon p =
                (Polygon)
                        reader.read(
                                "POLYGON((0 5, 5 10, 10 6, 15 10, 20 5, 15 0, 10 4, 5 0, 0 5))");
        Point point = (Point) PolyLabeller.getPolylabel(p, 1);

        Point expected1 = gb.point(5, 5);

        // This is also a valid result, but the algorithm at this point prefers the
        // first.
        // Point expected2 = gb.point(15, 5);

        Assert.assertEquals(expected1, point);
    }

    @Test
    public void testDoubleDiamondHole() throws ParseException {

        WKTReader reader = new WKTReader();
        GeometryBuilder gb = new GeometryBuilder();

        Polygon p =
                (Polygon)
                        reader.read(
                                "Polygon ((0 5, 5 10, 10 6, 15 10, 20 5, 15 0, 10 4, 5 0, 0 5),(5.4267578125 6.68164062499999822, 3.7451171875 5.30761718749999822, 5.365234375 3.21582031249999822, 8.3388671875 5.08203124999999822, 5.4267578125 6.68164062499999822))");
        Point point = (Point) PolyLabeller.getPolylabel(p, 1);

        Point expected = gb.point(15, 5);
        Assert.assertEquals(expected, point);
    }

    @Test
    public void testMultiPolygon() throws ParseException {

        WKTReader reader = new WKTReader();
        GeometryBuilder gb = new GeometryBuilder();

        MultiPolygon p =
                (MultiPolygon)
                        reader.read(
                                "MultiPolygon (((0 5, 5 10, 10 5, 5 0, 0 5)),((11.74609375 1.6357421875, 11.7255859375 3.24609375, 13.9306640625 3.3076171875, 13.951171875 1.73828125, 11.74609375 1.6357421875)))");
        Point point = (Point) PolyLabeller.getPolylabel(p, 1);

        Point expected = gb.point(5, 5);
        Assert.assertEquals(expected, point);
    }

    @Test
    public void testBadInput() {

        GeometryBuilder gb = new GeometryBuilder();
        Polygon p = gb.polygon();
        try {
            PolyLabeller.getPolylabel(p, 1);
            Assert.fail("processed empty polygon");
        } catch (IllegalStateException e) {
            // this is good!
        }
        try {
            p = gb.polygon(0, 0, 0, 0, 0, 0, 0, 0);
            PolyLabeller.getPolylabel(p, 1);
            Assert.fail("processed invalid polygon");
        } catch (IllegalStateException e) {
            // this is good.
        }
    }
}
