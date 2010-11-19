package net.kenevans.gpxinspector.handlers;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

import net.kenevans.gpxinspector.kml.KmlOptions;
import net.kenevans.gpxinspector.kml.KmlUtils;
import net.kenevans.gpxinspector.kml.SaveKmlDialog;
import net.kenevans.gpxinspector.model.GpxFileSetModel;
import net.kenevans.gpxinspector.utils.SWTUtils;
import net.kenevans.gpxinspector.utils.Utils;
import net.kenevans.gpxinspector.views.GpxView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class SaveKmlHandler extends AbstractHandler
{
    private static KmlOptions options;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
     * .ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if(window == null) {
            Utils.errMsg("Cannot determine the workbench window");
            return null;
        }

        if(options == null) {
            options = new KmlOptions();
        }

        SaveKmlDialog dialog = null;
        boolean success = false;
        // Without this try/catch, the application hangs on error
        try {
            dialog = new SaveKmlDialog(Display.getDefault().getActiveShell(),
                options);
            success = dialog.open();
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error with SaveKmlDialog", ex);
            return null;
        }
        if(!success) {
            return null;
        }

        // Find the GpxView
        GpxView view = null;
        try {
            view = (GpxView)window.getActivePage().findView(
                "net.kenevans.gpxinspector.gpxView");
            if(view == null) {
                SWTUtils.errMsgAsync("Cannot find GpxView");
                return null;
            }
        } catch(Exception ex) {
            SWTUtils.excMsgAsync("Error finding GpxView", ex);
        }
        if(view == null) {
            SWTUtils.errMsgAsync("GpxView is null");
            return null;
        }

        // Create the KML file
        options = dialog.getKmlOptions();
        if(options == null) {
            SWTUtils.errMsgAsync("KML Options is null");
            return null;
        }
        GpxFileSetModel fileSetModel = view.getGpxFileSetModel();
        if(fileSetModel == null) {
            SWTUtils.errMsgAsync("The KmlFileSetModel is null");
            return null;
        }
        try {
            KmlUtils.createKml(fileSetModel, options);
        } catch(Exception ex) {
            SWTUtils.excMsgAsync(
                "Error creating KML file: " + options.getKmlFileName(), ex);
        }

        return null;
    }

}
