/*
 * www.javagl.de - Geom - Geometry utilities
 *
 * Copyright (c) 2013-2015 Marco Hutter - http://www.javagl.de
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package de.javagl.geom;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods related to Shapes
 */
public class Shapes
{
    /**
     * Create a list containing line segments that approximate the given 
     * shape.
     * 
     * @param shape The shape
     * @param flatness The allowed flatness
     * @return The list of line segments
     */
    public static List<Line2D> computeLineSegments(
        Shape shape, double flatness)
    {
        List<Line2D> result = new ArrayList<Line2D>();
        PathIterator pi = shape.getPathIterator(null, flatness);
        double[] coords = new double[6];
        double previous[] = new double[2];
        double first[] = new double[2];
        while (!pi.isDone())
        {
            int segment = pi.currentSegment(coords);
            switch (segment)
            {
                case PathIterator.SEG_MOVETO:
                    previous[0] = coords[0];
                    previous[1] = coords[1];
                    first[0] = coords[0];
                    first[1] = coords[1];
                    break;

                case PathIterator.SEG_CLOSE:
                    result.add(new Line2D.Double(
                        previous[0], previous[1],
                        first[0], first[1]));
                    previous[0] = first[0];
                    previous[1] = first[1];
                    break;

                case PathIterator.SEG_LINETO:
                    result.add(new Line2D.Double(
                        previous[0], previous[1],
                        coords[0], coords[1]));
                    previous[0] = coords[0];
                    previous[1] = coords[1];
                    break;

                case PathIterator.SEG_QUADTO:
                    // Should never occur
                    throw new AssertionError(
                        "SEG_QUADTO in flattened path!");

                case PathIterator.SEG_CUBICTO:
                    // Should never occur
                    throw new AssertionError(
                        "SEG_CUBICTO in flattened path!");

                default:
                    // Should never occur
                    throw new AssertionError(
                        "Invalid segment in flattened path!");
            }
            pi.next();
        }
        return result;
    }

    /**
     * Create a list containing line points that approximate the given 
     * shape.
     * 
     * @param shape The shape
     * @param flatness The allowed flatness
     * @param storeOnClose Whether a point should be stored when the 
     * shape is closed. (This will result in points being stored twice
     * in the returned list). 
     * @return The list of points
     */
    public static List<Point2D> computePoints(
        Shape shape, double flatness, boolean storeOnClose)
    {
        List<Point2D> result = new ArrayList<Point2D>();
        PathIterator pi = shape.getPathIterator(null, flatness);
        double[] coords = new double[6];
        Point2D previousMove = null;
        while (!pi.isDone())
        {
            int segment = pi.currentSegment(coords);
            switch (segment)
            {
                case PathIterator.SEG_MOVETO:
                    result.add(new Point2D.Double(coords[0], coords[1]));
                    if (storeOnClose)
                    {
                        previousMove = new Point2D.Double(coords[0], coords[1]);
                    }
                    break;

                case PathIterator.SEG_CLOSE:
                    if (storeOnClose)
                    {
                        result.add(previousMove);
                    }
                    break;

                case PathIterator.SEG_LINETO:
                    result.add(new Point2D.Double(coords[0], coords[1]));
                    break;

                case PathIterator.SEG_QUADTO:
                    // Should never occur
                    throw new AssertionError(
                        "SEG_QUADTO in flattened path!");

                case PathIterator.SEG_CUBICTO:
                    // Should never occur
                    throw new AssertionError(
                        "SEG_CUBICTO in flattened path!");

                default:
                    // Should never occur
                    throw new AssertionError(
                        "Invalid segment in flattened path!");
            }
            pi.next();
        }
        return result;
    }
    
    
    /**
     * Create a shape that is created from interpolating between the 
     * given shapes, according to the given interpolation value
     * 
     * @param shape0 The first shape
     * @param shape1 The second shape
     * @param alpha The interpolation value, usually between 0.0 and 1.0
     * @return The interpolated shape
     * @throws IllegalArgumentException If the given shapes do not consist
     * of the same segments (that is, when they are not structurally equal)
     */
    public static Shape interpolate(Shape shape0, Shape shape1, double alpha)
    {
        Path2D path = new Path2D.Double();
        
        PathIterator pi0 = shape0.getPathIterator(null);
        PathIterator pi1 = shape1.getPathIterator(null);
        
        double coords0[] = new double[6];
        double coords1[] = new double[6];
        double coords[] = new double[6];
        while (!pi0.isDone())
        {
            if (pi1.isDone())
            {
                throw new IllegalArgumentException(
                    "Iterator 1 is done, but not iterator 0");
            }
            int segment0 = pi0.currentSegment(coords0);
            int segment1 = pi1.currentSegment(coords1);
            if (segment0 != segment1)
            {
                throw new IllegalArgumentException(
                    "Incompatible segments: "+segment0+" vs. "+segment1);
            }
            switch (segment0)
            {
                case PathIterator.SEG_MOVETO:
                    interpolate(coords0, coords1, coords, alpha, 2);
                    path.moveTo(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_LINETO:
                    interpolate(coords0, coords1, coords, alpha, 2);
                    path.lineTo(coords[0], coords[1]);
                    break;
                    
                case PathIterator.SEG_QUADTO:
                    interpolate(coords0, coords1, coords, alpha, 4);
                    path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                    
                case PathIterator.SEG_CUBICTO:
                    interpolate(coords0, coords1, coords, alpha, 6);
                    path.curveTo(
                        coords[0], coords[1], 
                        coords[2], coords[3], 
                        coords[4], coords[5]);
                    break;
                    
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
                
                default:
                    throw new AssertionError("Unknown segment type");
            }
            pi0.next();
            pi1.next();
        }
        if (!pi1.isDone())
        {
            throw new IllegalArgumentException(
                "Iterator 0 is done, but not iterator 1");
        }
        
        return path;
    }
 
    /**
     * Interpolate between <code>c0</code> and <code>c1</code> according
     * to the given alpha value, and place the result in <code>c</code>.
     * This assumes that none of the arrays is <code>null</code>, and
     * that all arrays have the size <code>n</code>.
     * 
     * @param c0 The first values
     * @param c1 The second values
     * @param c The result
     * @param alpha The interpolation value, usually between 0.0 and 1.0
     * @param n The size of the arrays
     */
    private static void interpolate(
        double c0[], double c1[], double c[], double alpha, int n)
    {
        for (int i=0; i<n; i++)
        {
            c[i] = c0[i] + (c1[i]-c0[i]) * alpha;
        }
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private Shapes()
    {
        // Private constructor to prevent instantiation
    }
    
}