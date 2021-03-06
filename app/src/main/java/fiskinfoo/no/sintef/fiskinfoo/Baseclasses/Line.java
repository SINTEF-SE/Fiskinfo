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

public class Line implements java.io.Serializable{
    private static final long serialVersionUID = 1337L;

    private Point start;
    private Point stop;
    private double dx;
    private double dy;

    public Line(Point start, Point stop) {
        this.start = start;
        this.stop = stop;
        this.dx = stop.getLatitude() - start.getLatitude();
        this.dy = stop.getLongitude() - start.getLongitude();
    }

    public String toString() {
        return "start: " + start.getLatitude() + "," + start.getLongitude() + "   " + stop.getLatitude() + "," + stop.getLongitude();
    }
    /**
     * Only use this when you are so close to the point that the curvature of the earth almost does not affect results.
     * @param point
     *      The point to measure against
     * @param distance
     *      The distance to compare to
     * @return
     *      Whether the distance between the given point and current line is greater than the given distance
     */
    public boolean checkDistanceWithLineLegacy(Point point, double distance) {
        double denominator = Math.abs((this.dy * point.getLatitude()) - (this.dx * point.getLongitude()) - (this.start.getLatitude() * this.stop.getLongitude()) + (this.stop.getLatitude() * this.start.getLongitude()));
        double distanceInDegrees = denominator / Math.sqrt(((this.dx * this.dx) + (this.dy * this.dy)));
        double distanceMeters = computeLengthOfDegrees(distanceInDegrees);

        return distanceMeters - distance < 0;
    }

    public boolean checkDistanceWithLineAndReportStatus(Point point, double distance) {
        if(start.getLatitude() == stop.getLatitude() && start.getLongitude() == stop.getLongitude()) {
            Point linePoint = new Point(start.getLatitude(), start.getLongitude());
            return linePoint.checkDistanceBetweenTwoPoints(point, distance);
        }

        if (distance < 1600) {
            return checkDistanceWithLineLegacy(point, distance);
        } else {
            Distance a = point.inverseCalculation(start);
            Distance b = point.inverseCalculation(stop);

            return a.distance - distance < 0 || b.distance - distance < 0;
        }
    }

    public double computeLengthOfDegrees(double degree) {
        double lat = Math.toRadians(degree);

        // Constants
        double m1 = 111132.92; // latitude calculation term 1
        double m2 = -559.82; // latitude calculation term 2
        double m3 = 1.175; // latitude calculation term 3
        double m4 = -0.0023; // latitude calculation term 4
        double p1 = 111412.84; // longitude calculation term 1
        double p2 = -93.5; // longitude calculation term 2
        double p3 = 0.118; // longitude calculation term 3

        double latLen = m1 + (m2 * Math.cos(2 * lat)) + (m3 * Math.cos(4 * lat)) + (m4 * Math.cos(6 * lat));
        @SuppressWarnings("unused")
        double lonLen = (p1 * Math.cos(lat)) + (p2 * Math.cos(3 * lat)) + (p3 * Math.cos(5 * lat));

        return (double) Math.round(latLen);

    }
}
