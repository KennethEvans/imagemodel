package net.kenevans.gpxinspector.ui;

import net.kenevans.gpx.WptType;
import net.kenevans.gpxinspector.model.GpxWaypointModel;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import utils.LabeledText;

/*
 * Created on Aug 23, 2010
 * By Kenneth Evans, Jr.
 */

public class WptInfoDialog extends Dialog
{
    private static final int TEXT_COLS_LARGE = 50;
    // private static final int TEXT_COLS_SMALL = 10;
    private boolean success = false;

    private GpxWaypointModel model;
    private Text nameText;
    private Text descText;
    private Text latText;
    private Text lonText;
    private Text eleText;
    private Text cmtText;
    private Text typeText;
    private Text timeText;
    private Text fixText;
    private Text srcText;
    private Text symText;
    private Text ageOfText;
    private Text dgpsidText;
    private Text geoidHeightText;
    private Text hdopText;
    private Text magvarText;
    private Text pdopText;
    private Text satText;
    private Text vdopText;

    /**
     * Constructor.
     * 
     * @param parent
     */
    public WptInfoDialog(Shell parent, GpxWaypointModel model) {
        // We want this to be modeless
        this(parent, SWT.DIALOG_TRIM | SWT.NONE, model);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent of this dialog.
     * @param style Style passed to the parent.
     */
    public WptInfoDialog(Shell parent, int style, GpxWaypointModel model) {
        super(parent, style);
        this.model = model;
    }

    /**
     * Convenience method to open the dialog.
     * 
     * @return Whether OK was selected or not.
     */
    public boolean open() {
        Shell shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
        shell.setText("Waypoint");
        // It can take a long time to do this so use a wait cursor
        // Probably not, though
        Cursor waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
        if(waitCursor != null) getParent().setCursor(waitCursor);
        createContents(shell);
        setWidgetsFromModel();
        getParent().setCursor(null);
        waitCursor.dispose();
        shell.pack();
        shell.open();
        Display display = getParent().getDisplay();
        while(!shell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return success;
    }

    /**
     * Creates the contents of the dialog.
     * 
     * @param shell
     */
    private void createContents(final Shell shell) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        shell.setLayout(gridLayout);

        // Create the groups
        createInfoGroup(shell);

        // Create the buttons
        // Make a zero margin composite for the OK and Cancel buttons
        Composite composite = new Composite(shell, SWT.NONE);
        // Change END to FILL to center the buttons
        GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL)
            .grab(true, false).applyTo(composite);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);

        Button button = new Button(composite, SWT.PUSH);
        button.setText("Reset");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setWidgetsFromModel();
            }
        });

        button = new Button(composite, SWT.PUSH);
        button.setText("Save");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setModelFromWidgets();
                success = true;
                shell.close();
            }
        });

        button = new Button(composite, SWT.PUSH);
        button.setText("Cancel");
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL)
            .grab(true, true).applyTo(button);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                success = false;
                shell.close();
            }
        });
        shell.setDefaultButton(button);
    }

    /**
     * Creates the icons group.
     * 
     * @param shell
     */
    private void createInfoGroup(Shell shell) {
        Group box = new Group(shell, SWT.BORDER);
        box.setText("Waypoint");
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        box.setLayout(gridLayout);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(box);

        // Name
        LabeledText labeledText = new LabeledText(box, "Name:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        nameText = labeledText.getText();
        nameText.setToolTipText("The GPS name of the element.");

        // Desc
        labeledText = new LabeledText(box, "Desc:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        descText = labeledText.getText();
        descText.setToolTipText("A text description of the element.Holds "
            + "additional\n"
            + "information about the element intended for the\n"
            + "user, not the GPS.");

        // Comment
        labeledText = new LabeledText(box, "Cmt:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        cmtText = labeledText.getText();
        cmtText.setToolTipText("GPS waypoint comment. Sent to GPS as comment.");

        // Lat
        labeledText = new LabeledText(box, "Lat:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        latText = labeledText.getText();
        latText.setToolTipText("The latitude of the point. Decimal degrees, "
            + "WGS84 datum.");

        // Lon
        labeledText = new LabeledText(box, "Lon:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        lonText = labeledText.getText();
        lonText.setToolTipText("The longitude of the point. Decimal degrees, "
            + "WGS84 datum.");

        // Ele
        labeledText = new LabeledText(box, "Ele:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        eleText = labeledText.getText();
        eleText.setToolTipText("Elevation in meters of the point.");

        // Symbol
        labeledText = new LabeledText(box, "Sym:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        symText = labeledText.getText();
        symText.setToolTipText("Text of GPS symbol name. For interchange with "
            + "other\n"
            + "programs. Use the exact spelling of the symbol on the\n"
            + "GPS, if known.");

        // Ageofdgpsdata
        labeledText = new LabeledText(box, "Ageofdgpsdata:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        ageOfText = labeledText.getText();
        ageOfText.setToolTipText("Number of seconds since last DGPS update.");

        // Dgpsid
        labeledText = new LabeledText(box, "Dgpsid:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        dgpsidText = labeledText.getText();
        dgpsidText.setToolTipText("ID of DGPS station used in differential "
            + "correction.");

        // Fix
        labeledText = new LabeledText(box, "Fix:", TEXT_COLS_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        fixText = labeledText.getText();
        fixText.setToolTipText("Type of GPX fix.");

        // Geoidheight
        labeledText = new LabeledText(box, "Geoidheight:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        geoidHeightText = labeledText.getText();
        geoidHeightText
            .setToolTipText("Height, in meters, of geoid (mean sea level) "
                + "above\n" + "WGS-84 earth ellipsoid. (NMEA GGA message).");

        // Hdop
        labeledText = new LabeledText(box, "Hdop:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        hdopText = labeledText.getText();
        hdopText.setToolTipText("Horizontal dilution of precision.");

        // Vdop
        labeledText = new LabeledText(box, "Vdop:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        vdopText = labeledText.getText();
        vdopText.setToolTipText("Vertical dilution of precision.");

        // Pdop
        labeledText = new LabeledText(box, "Pdop:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        pdopText = labeledText.getText();
        pdopText.setToolTipText("Position dilution of precision.");

        // Magvar
        labeledText = new LabeledText(box, "Magvar:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        magvarText = labeledText.getText();
        magvarText.setToolTipText("Magnetic variation at the point.");

        // Sat
        labeledText = new LabeledText(box, "Sat:", TEXT_COLS_LARGE);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        satText = labeledText.getText();
        satText
            .setToolTipText("Number of satellites used to calculate the GPS fix.\n"
                + "(Not number of satellites in view).");

        // Source
        labeledText = new LabeledText(box, "Src:", TEXT_COLS_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        srcText = labeledText.getText();
        srcText.setToolTipText("Source of data. Included to give user some\n"
            + "idea of reliability and accuracy of data.");

        // Type
        labeledText = new LabeledText(box, "Type:", TEXT_COLS_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        typeText = labeledText.getText();
        typeText.setToolTipText("Type (classification) of element.");

        // Time
        labeledText = new LabeledText(box, "Time:", TEXT_COLS_LARGE);
        // labeledText.getText().setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false)
            .applyTo(labeledText.getComposite());
        timeText = labeledText.getText();
        timeText.setToolTipText("Creation/modification timestamp for element. "
            + "Date and time\n"
            + "in are in Univeral Coordinated Time (UTC), notlocal time!\n"
            + "Conforms to ISO 8601 specification for date/time\n"
            + "representation. Fractional seconds are allowed for\n"
            + "millisecond timing in tracklogs.");
    }

    /**
     * Sets the values from the Text's to the model. Only does this if the Text
     * is editable.
     */
    private void setModelFromWidgets() {
        WptType wpt = model.getWaypoint();
        Text text = cmtText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setCmt(LabeledText.toString(text));
        }
        text = descText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setDesc(LabeledText.toString(text));
        }
        text = eleText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setEle(LabeledText.toBigDecimal(text));
        }
        text = fixText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setFix(LabeledText.toString(text));
        }
        text = latText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setLat(LabeledText.toBigDecimal(text));
        }
        text = lonText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setLon(LabeledText.toBigDecimal(text));
        }
        text = nameText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setName(LabeledText.toString(text));
        }
        text = srcText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setSrc(LabeledText.toString(text));
        }
        text = symText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setSym(LabeledText.toString(text));
        }
        text = timeText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setTime(LabeledText.toXMLGregorianCalendar(text));
        }
        text = typeText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setType(LabeledText.toString(text));
        }
        text = ageOfText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setAgeofdgpsdata(LabeledText.toBigDecimal(text));
        }
        text = dgpsidText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setDgpsid(LabeledText.toInteger(text));
        }
        text = geoidHeightText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setGeoidheight(LabeledText.toBigDecimal(text));
        }
        text = hdopText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setHdop(LabeledText.toBigDecimal(text));
        }
        text = magvarText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setMagvar(LabeledText.toBigDecimal(text));
        }
        text = pdopText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setPdop(LabeledText.toBigDecimal(text));
        }
        text = satText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setSat(LabeledText.toBigInteger(text));
        }
        text = vdopText;
        if(text != null && !text.isDisposed() && text.getEditable()) {
            wpt.setVdop(LabeledText.toBigDecimal(text));
        }
    }

    /**
     * Sets the values form the model to the Text's.
     */
    private void setWidgetsFromModel() {
        WptType wpt = model.getWaypoint();
        LabeledText.read(cmtText, wpt.getCmt());
        LabeledText.read(descText, wpt.getDesc());
        LabeledText.read(eleText, wpt.getEle());
        LabeledText.read(fixText, wpt.getFix());
        LabeledText.read(latText, wpt.getLat());
        LabeledText.read(lonText, wpt.getLon());
        LabeledText.read(nameText, wpt.getName());
        LabeledText.read(srcText, wpt.getSrc());
        LabeledText.read(symText, wpt.getSym());
        LabeledText.read(timeText, wpt.getTime());
        LabeledText.read(typeText, wpt.getType());
        LabeledText.read(ageOfText, wpt.getAgeofdgpsdata());
        LabeledText.read(dgpsidText, wpt.getDgpsid());
        LabeledText.read(geoidHeightText, wpt.getGeoidheight());
        LabeledText.read(hdopText, wpt.getHdop());
        LabeledText.read(magvarText, wpt.getMagvar());
        LabeledText.read(pdopText, wpt.getPdop());
        LabeledText.read(satText, wpt.getSat());
        LabeledText.read(vdopText, wpt.getVdop());
    }

    /**
     * @return The value of model.
     */
    public GpxWaypointModel getModel() {
        return model;
    }

}
