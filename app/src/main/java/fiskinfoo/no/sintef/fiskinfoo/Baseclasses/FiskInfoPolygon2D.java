/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fiskinfoo.no.sintef.fiskinfoo.Baseclasses;

import java.util.ArrayList;
import java.util.List;

public class FiskInfoPolygon2D implements java.io.Serializable {
    List<Line> lines;
    List<Polygon> polygons;
    List<Point> points;

    public FiskInfoPolygon2D() {
        this.lines = new ArrayList<Line>();
        this.polygons = new ArrayList<Polygon>();
        this.points = new ArrayList<Point>();
    }

    public void addLine(Line line) {
        lines.add(line);
    }

    public void addPolygon(Polygon polygon) {
        polygons.add(polygon);
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public List<Point> getPoints() {
        return points;
    }

    public boolean checkCollsionWithPoint(Point point, double distance) {
        for (Line line : lines) {
            if (line.checkDistanceWithLineAndReportStatus(point, distance)) {
                return true;
            }
        }
        // Construct lines from polygons so we can do our
        for (Polygon polygon : polygons) {
            List<Point> vertices = polygon.getVertices();
            Point currPoint = vertices.get(0);
            Point prevPoint = null;
            for (int i = 1; i < vertices.size() - 1; i++) {
                prevPoint = currPoint;
                currPoint = vertices.get(i);
                Line currLine = new Line(prevPoint, currPoint);
                if (currLine.checkDistanceWithLineAndReportStatus(point, distance)) {
                    System.out.println("CRASHED WITH LINE AT: " + currLine.toString());
                    return true;
                }
            }
        }
        // Points
        for (Point currentPoint : points) {
            if(currentPoint.checkDistanceBetweenTwoPoints(point, distance)) {
                System.out.println("CRASHED WITH POINT AT: " + currentPoint.getLatitude() + "," + currentPoint.getLongitude());
                return true;
            }
        }
        return false;
    }


    /**
     * Utility functions might/should be removed after debugging
     */



    /**
     *
     */
    private static final long serialVersionUID = 1L;

}
