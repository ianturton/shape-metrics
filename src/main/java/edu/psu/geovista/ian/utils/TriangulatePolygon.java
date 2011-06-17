/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.psu.geovista.ian.utils;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Triangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Triangulates a simple polygon
 * based on ElGindy, H., H. Everett, and G.T. Toussaint.
 * Slicing an ear using prune-and-search. Pattern Recognition Letters.
 * September 1993, 719-722.
 * http://www-cgrl.cs.mcgill.ca/~godfried/publications/ear.ps.gz
 * @author ijt1
 */
public class TriangulatePolygon {
    List<Polygon> triangles = new ArrayList<Polygon>();
    GeometryFactory fac = new GeometryFactory();
    public List<Polygon> triangulate(Polygon poly){
        triangles = new ArrayList<Polygon>();
        assert(CGAlgorithms.isCCW(poly.getExteriorRing().getCoordinates()));
        if(poly.getExteriorRing().getNumPoints()==4){//a triangle
            triangles.add(poly);
            return triangles;
        }
        findAnEar(poly, poly.getExteriorRing().getStartPoint());
        return triangles;
    }
    private void findAnEar(Polygon gsp, Point pi){
        //if pi is an ear report it
        if(ear(gsp,pi)!=null){

            triangles.add(gsp);
        }

        //else
        //find a vertex pj such that pi,pj is a diagonal of gsp
        //let gsp' be the good sub-polyon formed by pi,pj
        //findAnEar(gsp',floor(k/2)

    }

    private Polygon ear(Polygon poly,Point p){
        Coordinate[] c=poly.getCoordinates();
        final Coordinate coordinate = p.getCoordinate();
        Coordinate last = null,next = null;
        for(int i=0;i<c.length;i++){
            Coordinate coord = c[i];
            if(coord.equals2D(coordinate)){
                if(i>0){
                    last = c[i-1];
                }else{
                    last = c[c.length-1];
                }
                if(i<c.length-2){
                    next = c[i+1];
                }else{
                    next = c[0];
                }
            }
        }
        Coordinate[] coordinates = new Coordinate[3];
        coordinates[0] = coordinate;
        coordinates[1] = next;
        coordinates[2] = last;

        //coordinates[1] =
        LinearRing shell = fac.createLinearRing(coordinates);
        Polygon e = fac.createPolygon(shell, null);
        return e;
    }
}
