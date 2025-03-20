package com.starcom.navigation;

public class GeoMath
{
  public final static double DEGREE_PER_METER = 0.000008993;
  public final static double METER_PER_DEGREE = 1.0/0.000008993;
  public final static double KMH_TO_MSEC = 0.2777777778;

  /** The square of x.
   *  @return x * x **/
  public static double sqr(double x) { return x * x; }
  
  /** The distance between 2 points squared.
   *  @return The squared distance **/
  public static double dist2(double v_x, double v_y, double w_x, double w_y)
  {
    return sqr(v_x - w_x) + sqr(v_y - w_y);
  }
  
  /** Calculates the estimated distance in degree. Function from GeoPoint.java! **/
  public static double fastDistance(double lat1, double lon1, double lat2, double lon2)
  {
    return Math.hypot(lon1 - lon2, lat1 - lat2);
  }
  
  /** Calculate distance to line-segment .
   *  Hint from: https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
   *  @param p_x The point.x
   *  @param p_y The point.y
   *  @param v_x The startpoint.x of line.
   *  @param v_y The startpoint.y of line.
   *  @param w_x The endpoint.x of line.
   *  @param w_y The endpoint.y of line. **/
  public static double distToLineSegment(double p_x, double p_y, double v_x, double v_y, double w_x, double w_y)
  {
    double l2 = dist2(v_x, v_y, w_x, w_y);
    if (l2 == 0) { return dist2(p_x, p_y, v_x, v_y); }
    double t = ((p_x - v_x) * (w_x - v_x) + (p_y - v_y) * (w_y - v_y)) / l2;
    t = Math.max(0, Math.min(1, t));
    double distSquared = dist2(p_x, p_y, v_x + t * (w_x - v_x), v_y + t * (w_y - v_y) );
    return Math.sqrt(distSquared);
  }
  
//  /** Calculates the distance and bearing.
//   * <br/> Function got from Android open source project Location.java */
//  public static void computeDistanceAndBearing(double lat1, double lon1,
//	        double lat2, double lon2) {
//	        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
//	        // using the "Inverse Formula" (section 4)
//	        int MAXITERS = 20;
//	        // Convert lat/long to radians
//	        lat1 *= Math.PI / 180.0;
//	        lat2 *= Math.PI / 180.0;
//	        lon1 *= Math.PI / 180.0;
//	        lon2 *= Math.PI / 180.0;
//	        double a = 6378137.0; // WGS84 major axis
//	        double b = 6356752.3142; // WGS84 semi-major axis
//	        double f = (a - b) / a;
//	        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);
//	        double L = lon2 - lon1;
//	        double A = 0.0;
//	        double U1 = Math.atan((1.0 - f) * Math.tan(lat1));
//	        double U2 = Math.atan((1.0 - f) * Math.tan(lat2));
//	        double cosU1 = Math.cos(U1);
//	        double cosU2 = Math.cos(U2);
//	        double sinU1 = Math.sin(U1);
//	        double sinU2 = Math.sin(U2);
//	        double cosU1cosU2 = cosU1 * cosU2;
//	        double sinU1sinU2 = sinU1 * sinU2;
//	        double sigma = 0.0;
//	        double deltaSigma = 0.0;
//	        double cosSqAlpha;
//	        double cos2SM;
//	        double cosSigma;
//	        double sinSigma;
//	        double cosLambda = 0.0;
//	        double sinLambda = 0.0;
//	        double lambda = L; // initial guess
//	        for (int iter = 0; iter < MAXITERS; iter++) {
//	            double lambdaOrig = lambda;
//	            cosLambda = Math.cos(lambda);
//	            sinLambda = Math.sin(lambda);
//	            double t1 = cosU2 * sinLambda;
//	            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
//	            double sinSqSigma = t1 * t1 + t2 * t2; // (14)
//	            sinSigma = Math.sqrt(sinSqSigma);
//	            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda; // (15)
//	            sigma = Math.atan2(sinSigma, cosSigma); // (16)
//	            double sinAlpha = (sinSigma == 0) ? 0.0 :
//	                cosU1cosU2 * sinLambda / sinSigma; // (17)
//	            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
//	            cos2SM = (cosSqAlpha == 0) ? 0.0 :
//	                cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha; // (18)
//	            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq; // defn
//	            A = 1 + (uSquared / 16384.0) * // (3)
//	                (4096.0 + uSquared *
//	                 (-768 + uSquared * (320.0 - 175.0 * uSquared)));
//	            double B = (uSquared / 1024.0) * // (4)
//	                (256.0 + uSquared *
//	                 (-128.0 + uSquared * (74.0 - 47.0 * uSquared)));
//	            double C = (f / 16.0) *
//	                cosSqAlpha *
//	                (4.0 + f * (4.0 - 3.0 * cosSqAlpha)); // (10)
//	            double cos2SMSq = cos2SM * cos2SM;
//	            deltaSigma = B * sinSigma * // (6)
//	                (cos2SM + (B / 4.0) *
//	                 (cosSigma * (-1.0 + 2.0 * cos2SMSq) -
//	                  (B / 6.0) * cos2SM *
//	                  (-3.0 + 4.0 * sinSigma * sinSigma) *
//	                  (-3.0 + 4.0 * cos2SMSq)));
//	            lambda = L +
//	                (1.0 - C) * f * sinAlpha *
//	                (sigma + C * sinSigma *
//	                 (cos2SM + C * cosSigma *
//	                  (-1.0 + 2.0 * cos2SM * cos2SM))); // (11)
//	            double delta = (lambda - lambdaOrig) / lambda;
//	            if (Math.abs(delta) < 1.0e-12) {
//	                break;
//	            }
//	        }
//	        results.mDistance = (float) (b * A * (sigma - deltaSigma));
//	        float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
//	            cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
//	        initialBearing *= 180.0 / Math.PI;
//	        results.mInitialBearing = initialBearing; // This is the needed one
//	        float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
//	                -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
//	        finalBearing *= 180.0 / Math.PI;
//	        results.mFinalBearing = finalBearing;
//	        results.mLat1 = lat1;
//	        results.mLat2 = lat2;
//	        results.mLon1 = lon1;
//	        results.mLon2 = lon2;
//	    }
}
