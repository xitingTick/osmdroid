package org.andnav.osm.views.tiles.renderer.mapnik.geometry;

import java.util.Vector;

import org.andnav.osm.views.tiles.renderer.mapnik.geometry.MapnikVertex.VertexCommandType;

public class MapnikLineString extends MapnikGeometry {

	private Vector<MapnikVertex> mVertices;
	private int mItr;
	
	public MapnikLineString()
	{
		mVertices = new Vector<MapnikVertex>();
		mItr = 0;
	}
	
	@Override
	public boolean HitTest(double x, double y, double tol) {
		return MapnikGeometryUtil.pointOnPath(x, y, mVertices, tol);
	}

	@Override
	public MapnikVertex getNextVertex() {
		if (mItr >= mVertices.size())
			return null;
		return mVertices.get(mItr++);
	}

	@Override
	public GeomType getType() {
		return GeomType.LineString;
	}

	@Override
	public void labelPosition(double[] coords) {
        // calculate mid point on line string
        double x0=0;
        double y0=0;
        double x1=0;
        double y1=0;
        
        int size = mVertices.size();
        if (size == 1)
        {
            MapnikVertex v = mVertices.get(0);
            coords[0] = v.x;
            coords[1] = v.y;
        }
        else if (size == 2)
        {
        	MapnikVertex v1 = mVertices.get(0);
        	MapnikVertex v2 = mVertices.get(1);
        	coords[0] = 0.5 * (v1.x + v2.x);
        	coords[1] = 0.5 * (v1.y + v2.y);
        }
        else
        {
        	double len=0.0;
        	for (int pos = 1; pos < size; ++pos)
        	{
        		MapnikVertex v1 = mVertices.get(pos - 1);
        		MapnikVertex v2 = mVertices.get(pos);

        		double dx = v2.x - v1.x;
        		double dy = v2.y - v1.y;
        		len += Math.sqrt(dx * dx + dy * dy);
        	}
        	double midlen = 0.5 * len;
        	double dist = 0.0;
        	for (int pos = 1; pos < size;++pos)
        	{
        		MapnikVertex v1 = mVertices.get(pos - 1);
        		MapnikVertex v2 = mVertices.get(pos);

        		double dx = v2.x - v1.x;
        		double dy = v2.y - v1.y;

        		double seg_len = Math.sqrt(dx * dx + dy * dy);

        		if (( dist + seg_len) >= midlen)
        		{
        			double r = (midlen - dist)/seg_len;
        			coords[0] = x0 + (x1 - x0) * r;
        			coords[1] = y0 + (y1 - y0) * r;
        			break;
        		}
        		dist += seg_len;
        	}
        }

	}

	@Override
	public void lineTo(MapnikVertex v) {
		v.mCmd = VertexCommandType.SEG_LINETO;
		mVertices.add(v);
	}

	@Override
	public void moveTo(MapnikVertex v) {
		v.mCmd = VertexCommandType.SEG_MOVETO;
		mVertices.add(v);
	}

	@Override
	public int numPoints() {
		return mVertices.size();
	}

	@Override
	public void rewind(int pos) {
		mItr = 0;
	}

	@Override
	public void setCapacity(int size) {
		mVertices.ensureCapacity(size);
	}

}