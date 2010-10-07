package net.kenevans.gpxinspector.model;

import java.util.ArrayList;
import java.util.List;

import net.kenevans.gpx.TrkType;

/*
 * Created on Aug 22, 2010
 * By Kenneth Evans, Jr.
 */

public class GpxTrackModel extends GpxModel implements IGpxElementConstants
{
    private TrkType track;
    private List<GpxPropertyModel> propertyModels;

    public GpxTrackModel(GpxModel parent, TrkType track) {
        this.parent = parent;
        this.track = track;

        // Add properties for this element
        propertyModels = new ArrayList<GpxPropertyModel>();
        GpxPropertyModel model = new GpxPropertyModel(this, COLOR_KEY,
            COLOR_DEFAULT);
        propertyModels.add(model);
        model = new GpxPropertyModel(this, LINE_WIDTH_KEY, LINE_WIDTH_DEFAULT);
        propertyModels.add(model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#dispose()
     */
    public void dispose() {
        if(disposed) {
            return;
        }
        for(GpxModel model : propertyModels) {
            model.dispose();
        }
        propertyModels.clear();
        removeAllGpxModelListeners();
        disposed = true;
    }

    /**
     * @return The value of track.
     */
    public TrkType getTrack() {
        return track;
    }

    /**
     * @return The value of propertyModels.
     */
    public List<GpxPropertyModel> getPropertyModels() {
        return propertyModels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.kenevans.gpxinspector.model.GpxModel#getLabel()
     */
    @Override
    public String getLabel() {
        if(track != null) {
            return track.getName();
        }
        return "Null Track";
    }

}
