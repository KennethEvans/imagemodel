package net.kenevans.imagemodel;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.event.EventListenerList;

import net.kenevans.imagemodel.utils.ImageSelection;
import net.kenevans.imagemodel.utils.ImageUtils;
import net.kenevans.imagemodel.utils.Utils;

public class ImageModel implements Printable
{
    // private static final long serialVersionUID = 1L;
    public static double GAMMA_LIGHTEN = 1.1;
    public static double GAMMA_DARKEN = 1. / GAMMA_LIGHTEN;
    private static final String LS = Utils.LS;
    private EventListenerList listenerList = null;
    private BufferedImage image; // The current image
    private BufferedImage image0; // The original image
    private File file = null;

    // Property change names
    public static final String IMAGE_CHANGED = ImageModel.class.getName()
        + ".ImageChanged";

    public ImageModel() {
        listenerList = new EventListenerList();
    }

    public void addImageModelListener(ImageModelListener l) {
        listenerList.add(ImageModelListener.class, l);
    }

    public void removeImageModelListener(ImageModelListener l) {
        listenerList.remove(ImageModelListener.class, l);
    }

    /**
     * Fires a default ImageModelEvent, which has propertyName = IMAGE_CHANGED,
     * oldValue = null, and newValue = the current image.
     */
    protected void fireDefaultImageModelEvent() {
        EventListener[] listeners = listenerList
            .getListeners(ImageModelListener.class);
        for(EventListener listener : listeners) {
            ImageModelEvent imageModelEvent = new ImageModelEvent(this,
                "Image", null, image);
            ((ImageModelListener)listener).propertyChange(imageModelEvent);
        }
    }

    public int print(Graphics g, PageFormat pf, int page) {
        if(image == null) return Printable.NO_SUCH_PAGE;
        // Only allow printing one page
        if(page > 0) return Printable.NO_SUCH_PAGE;

        // Get sizes
        Graphics2D g2 = (Graphics2D)g;
        double x0 = pf.getImageableX();
        double y0 = pf.getImageableY();
        double width0 = pf.getImageableWidth();
        double height0 = pf.getImageableHeight();
        int width = image.getWidth();
        int height = image.getHeight();
        if(width <= 0 || height <= 0) return Printable.NO_SUCH_PAGE;

        // Translate to the origin of the imageable part
        // These are done in fifo order
        g2.translate(x0, y0);

        // See if it is necessary to scale to fit
        double widthScale = width0 / width;
        double heightScale = height0 / height;
        double scale = widthScale < heightScale ? widthScale : heightScale;
        if(scale > 1) {
            scale = 1;
        }

        // Center horizontally
        double newWidth = scale * width;
        double xoff = .5 * (width0 - newWidth);
        if(xoff > 0) {
            g2.translate(xoff, 0);
        }

        // Scale last
        if(scale < 1) g2.scale(scale, scale);

        // Draw the image
        g2.drawImage(image, 0, 0, width, height, null);
        return Printable.PAGE_EXISTS;
    }

    public void readImage(File file) {
        this.file = file;
        if(file == null) return;
        if(file.length() == 0) {
            Utils.warnMsg("File is empty");
            return;
        }
        try {
            BufferedImage newImage = ImageIO.read(file);
            if(newImage == null) {
                String msg = "Cannot read file:" + LS + file.getName() + LS;
                if(!ImageUtils.isImageIOSupported(file)) {
                    msg += LS + ImageUtils.getImageIOSupportedStatus(file);
                }
                Utils.errMsg(msg);
            } else {
                replaceImage(newImage);
            }
        } catch(Exception ex) {
            Utils.errMsg("Error processing file:" + LS
                + ((file != null) ? file.getName() : "null") + LS + ex + LS
                + ex.getMessage());
        }
    }

    // private void printHierarchy() {
    // System.out.println("Hierarchy");
    // Component component = this;
    // while(component != null) {
    // System.out.println(component.getClass().getName());
    // System.out.println(" " + component.getWidth() + "x"
    // + component.getHeight());
    // Dimension dimension = component.getPreferredSize();
    // System.out.println(" Preferred: " + dimension.getWidth() + "x"
    // + dimension.getHeight());
    // System.out.println(" Valid = " + component.isValid());
    // System.out.println(" Visible = " + component.isVisible());
    // System.out.println(" Displayable = " + component.isDisplayable());
    // System.out.println(" Showing = " + component.isShowing());
    //
    // component = component.getParent();
    // }
    // }
    //
    // private void printSizes() {
    // System.out.println("Image Size: " + image.getWidth() + "x"
    // + image.getHeight());
    // System.out.println("Size: " + this.getWidth() + "x" + this.getHeight());
    // Dimension dimension = this.getPreferredSize();
    // System.out.println("Preferred Size: " + dimension.getWidth() + "x"
    // + dimension.getHeight());
    // System.out.println("Frame Size: " + frame.getWidth() + "x"
    // + frame.getWidth());
    // }

    public String getInfo() {
        return getInfo(image);
    }

    public String getOriginalAndCurrentInfo() {
        String info = "Original Image" + LS + LS;
        info += getInfo(image0);
        info += LS + "Current Image" + LS + LS;
        info += getInfo(image);
        return info;
    }

    public String getInfo(BufferedImage image) {
        String info = "";
        if(image == null) {
            info += "No image";
            return info;
        }
        if(file != null) {
            info += file.getPath() + LS;
            info += file.getName() + LS;
        } else {
            info += "Unknown file" + LS;
        }
        info += LS;
        info += image.getWidth() + " x " + image.getHeight() + LS;
        Map<String, String> types = new HashMap<String, String>();
        types.put("5", "TYPE_3BYTE_BGR");
        types.put("6", "TYPE_4BYTE_ABGR");
        types.put("7", "TYPE_4BYTE_ABGR_PRE");
        types.put("12", "TYPE_BYTE_BINARY");
        types.put("10", "TYPE_BYTE_GRAY");
        types.put("13", "TYPE_BYTE_INDEXED");
        types.put("0", "TYPE_CUSTOM");
        types.put("2", "TYPE_INT_ARGB");
        types.put("3", "TYPE_INT_ARGB_PRE");
        types.put("4", "TYPE_INT_BGR");
        types.put("1", "TYPE_INT_RGB");
        types.put("9", "TYPE_USHORT_555_RGB");
        types.put("8", "TYPE_USHORT_565_RGB");
        types.put("11", "TYPE_USHORT_GRAY");
        Integer type = Integer.valueOf(image.getType());
        String stringType = types.get(type.toString());
        if(stringType == null) stringType = "Unknown";
        info += "Type: " + stringType + " [" + type + "]" + LS;
        info += "Properties:" + LS;
        String[] props = image.getPropertyNames();
        if(props == null) {
            info += "  No properties found" + LS;
        } else {
            for(int i = 0; i < props.length; i++) {
                info += "  " + props[i] + ": " + image.getProperty(props[i])
                    + LS;
            }
        }
        info += "ColorModel:" + LS;
        // The following assumes a particular format for toString()
        String colorModel = image.getColorModel().toString();
        String[] tokens = colorModel.split(" ");
        String colorModelName = tokens[0];
        info += "  " + colorModelName + LS;
        info += "  ";
        for(int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            if(token.equals("=")) {
                i++;
                info += "= " + tokens[i] + LS + "  ";
            } else {
                info += token + " ";
            }
        }
        info += LS;

        // The profile is always sRGB as implemented.
        // // Find the ICC profile used
        // String desc = ImageUtils.getICCProfileName(image);
        // if(desc != null) {
        // info += "ICC Profile=" + desc + LS;
        // info += "  (This is what Java ImageIO is using and may not be" + LS
        // + "    the same as any embedded ICC profile in the file.)" + LS;
        // }

        return info;
    }

    public void gamma(double gamma) {
        // Check arguments
        if(image == null || image.getWidth() <= 0 || image.getHeight() <= 0)
            return;
        if(gamma == 0) {
            // Prompt for gamma
            if(gamma == 0) {
                Utils.errMsg("Gamma is zero" + LS + "Nothing done");
                return;
            }
        }

        // Generate lookup table
        byte[] val = new byte[256];
        for(int i = 0; i < 256; i++) {
            double x = Math.pow(i / 255., 1. / gamma);
            byte b = (byte)Math.round(x * 255.);
            val[i] = (b < 256) ? b : (byte)255;
        }
        ByteLookupTable table = new ByteLookupTable(0, val);
        LookupOp op = new LookupOp(table, null);
        filter(op);
    }

    public void blur() {
        // Sum of elements must be 1
        float val = 1.0f;
        float weight = val / 9.0f;
        float[] elements = {weight, weight, weight, weight, weight, weight,
            weight, weight, weight};
        convolve(elements);
    }

    public void sharpen() {
        // Sum of elements must be 1
        // center = 5 is a common example, since the sides are then -1
        float center = 2.0f;
        float side = (1.0f - center) / 4.0f;
        float[] elements = {0.0f, side, 0.0f, side, center, side, 0.0f, side,
            0.0f};
        convolve(elements);
    }

    private void convolve(float[] elements) {
        int dim = (int)Math.sqrt(elements.length);
        Kernel kernel = new Kernel(dim, dim, elements);
        ConvolveOp op = new ConvolveOp(kernel);
        filter(op);
    }

    private void filter(BufferedImageOp op) {
        if(image == null || image.getWidth() <= 0 || image.getHeight() <= 0)
            return;
        BufferedImage filteredImage = null;
        if(true) {
            filteredImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        } else {
            filteredImage = new BufferedImage(image.getWidth(),
                image.getHeight(), image.getType());
        }
        op.filter(image, filteredImage);
        image = filteredImage;
        fireDefaultImageModelEvent();
    }

    /**
     * Resets the images to null. Can be used to free resources.
     */
    public void reset() {
        image = null;
        image0 = null;
    }

    /**
     * Copies image0 to image and does a repaint().
     */
    public void restore() {
        if(image0 == null) {
            image = image0;
        } else {
            image = ImageUtils.copyImage(image0);
        }
        fireDefaultImageModelEvent();
    }

    /**
     * Saves the original as image0 and does restore, so that image is a
     * TYPE_INT_ARGB version of the original.
     * 
     * @param newImage
     */
    public void replaceImage(BufferedImage newImage) {
        if(newImage == null) {
            Utils.errMsg("Bad image");
            return;
        }
        try {
            image0 = newImage;
            restore();
        } catch(Exception ex) {
            Utils.errMsg("Error processing image:" + LS + ex + LS
                + ex.getMessage());
        }
    }

    /**
     * Sets image to a TYPE_INT_ARGB version of the new image and does a
     * repaint.
     * 
     * @param newImage
     */
    public void setImage(BufferedImage newImage) {
        if(newImage == null) {
            Utils.errMsg("Bad image");
            return;
        }
        image = ImageUtils.copyImage(newImage);
        fireDefaultImageModelEvent();
    }

    public void grayscale() {
        if(image == null || image.getWidth() <= 0 || image.getHeight() <= 0)
            return;
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        filter(op);
    }

    public void flip(boolean doHorizontal, boolean doVertical) {
        if(image == null || image.getWidth() <= 0 || image.getHeight() <= 0)
            return;
        if(doHorizontal == false && doVertical == false) return;
        double sx = doHorizontal ? -1 : 1;
        double sy = doVertical ? -1 : 1;
        double tx = doHorizontal ? -image.getWidth() : 0;
        double ty = doVertical ? -image.getHeight() : 0;
        AffineTransform at = AffineTransform.getScaleInstance(sx, sy);
        at.translate(tx, ty);
        AffineTransformOp op = new AffineTransformOp(at,
            AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        filter(op);
    }

    public void rotate1(double degrees) {
        if(image == null || image.getWidth() <= 0 || image.getHeight() <= 0)
            return;
        if(degrees == 0) return;
        double radians = Math.toRadians(degrees);
        AffineTransform at = new AffineTransform();
        double xcen = image.getWidth() / 2;
        double ycen = image.getHeight() / 2;
        at.rotate(radians, xcen, ycen);
        AffineTransformOp op = new AffineTransformOp(at,
            AffineTransformOp.TYPE_BILINEAR);
        if(true) {
            System.out.println("Before: width =" + image.getWidth()
                + " height=" + image.getHeight());
        }
        filter(op);
        if(true) {
            System.out.println("After: width =" + image.getWidth() + " height="
                + image.getHeight());
        }
    }

    public void rotate(double degrees) {
        // Check arguments
        if(image == null || image.getWidth() <= 0 || image.getHeight() <= 0)
            return;
        if(degrees == 0) {
            Utils.errMsg("Angle is zero" + LS + "Nothing done");
            return;
        }

        // Determine size and translation needed after rotation
        double tx = 0, ty = 0;
        int newWidth = 0, newHeight = 0;
        // Do the 90 deg ones explicitly to avoid roundoff
        if(degrees == 90) {
            newWidth = image.getHeight();
            newHeight = image.getWidth();
            tx = image.getHeight();
            ty = 0;
        } else if(degrees == -90) {
            newWidth = image.getHeight();
            newHeight = image.getWidth();
            tx = 0;
            ty = image.getWidth();
        } else if(degrees == 180) {
            newWidth = image.getWidth();
            newHeight = image.getHeight();
            tx = image.getWidth();
            ty = image.getHeight();
        } else {
            // Find the bounding box
            double width = image.getWidth();
            double height = image.getHeight();
            double rad = Math.toRadians(degrees);
            double phi = Math.atan2(height, width);
            double theta = Math.sqrt(width * width + height * height);
            double[][] vtx = { {0, 0},
                {width * Math.cos(rad), width * Math.sin(rad)},
                {theta * Math.cos(rad + phi), theta * Math.sin(rad + phi)},
                {-height * Math.sin(rad), height * Math.cos(rad)}};
            double left = vtx[0][0];
            double right = left;
            double top = vtx[0][1];
            double bottom = top;
            for(int i = 1; i < 4; i++) {
                double x = vtx[i][0];
                double y = vtx[i][1];
                if(x < left) left = x;
                if(x > right) right = x;
                if(y < top) top = y;
                if(y > bottom) bottom = y;
            }
            newWidth = (int)Math.ceil(right - left);
            newHeight = (int)Math.ceil(bottom - top);
            tx = -left;
            ty = -top;
        }

        // Make a new image with the calculated width and height
        // System.out.println("degrees=" + degrees + " w=" + newWidth + " h="
        // + newHeight + " tx=" + tx + " ty=" + ty);
        BufferedImage newImage = new BufferedImage(newWidth, newHeight,
            image.getType());

        // Transform - will be done in lifo order
        AffineTransform at = new AffineTransform();
        at.translate(tx, ty);
        at.rotate(Math.toRadians(degrees), 0, 0);
        Graphics2D g2d = (Graphics2D)newImage.getGraphics();
        g2d.transform(at);
        g2d.drawImage(image, 0, 0, null);
        image = newImage;
        fireDefaultImageModelEvent();
    }

    public void cropImage(Rectangle rect) {
        if(rect == null) return;
        BufferedImage newImage = new BufferedImage(rect.width, rect.height,
            BufferedImage.TYPE_INT_ARGB);
        // Use "null" as the ImageObserver since no asynchronism is involved
        // For drawing the first time, using "this" is appropriate (Component
        // implements ImageObserver)
        Graphics2D gc = newImage.createGraphics();
        // Do this to insure transparency propagates
        gc.setComposite(AlphaComposite.Src);
        gc.drawImage(image, -rect.x, -rect.y, null);
        gc.dispose();
        image = newImage;
        fireDefaultImageModelEvent();
    }

    public void copy() {
        if(image == null) return;
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        ImageSelection sel = new ImageSelection(image);
        clipboard.setContents(sel, null);
    }

    public void paste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        DataFlavor flavor = DataFlavor.imageFlavor;
        Transferable contents = clipboard.getContents(null);
        if(contents != null && contents.isDataFlavorSupported(flavor)) {
            try {
                Image image = (Image)contents.getTransferData(flavor);
                // DEBUG
                if(false && image instanceof BufferedImage) {
                    System.out.println("image=" + image);
                    for(int i = 0; i < image.getWidth(null); i++) {
                        System.out.printf("%3d #%8x\n", i,
                            ((BufferedImage)image).getRGB(i, 0));
                    }
                }
                BufferedImage newImage = ImageUtils.imageToBufferedImage(image);
                // DEBUG
                if(false) {
                    System.out.println("newImage=" + newImage);
                    for(int i = 0; i < newImage.getWidth(); i++) {
                        System.out.printf("%3d #%8x\n", i,
                            newImage.getRGB(i, 0));
                    }
                }
                setImage(newImage);
            } catch(Exception ex) {
                Utils.excMsg("Error pasting image:", ex);
            }
        }
    }

    public boolean drop(DropTargetDropEvent ev) {
        boolean retCode = false;
        int action = ev.getDropAction();
        if((ev.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
            return retCode;
        }
        Transferable contents = ev.getTransferable();
        if(contents == null) {
            return retCode;
        }
        if(contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            ev.acceptDrop(action);
            try {
                Image image = (Image)contents
                    .getTransferData(DataFlavor.imageFlavor);
                BufferedImage newImage = ImageUtils.imageToBufferedImage(image);
                if(image != null) {
                    setImage(newImage);
                    retCode = true;
                }
            } catch(Exception ex) {
                Utils.excMsg("Error dropping image:", ex);
            }
        } else if(contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            ev.acceptDrop(action);
            try {
                // Use ? instead of File to avoid unchecked cast warning
                List<?> fileList = (List<?>)contents
                    .getTransferData(DataFlavor.javaFileListFlavor);
                File file = null;
                if(fileList.size() > 0) {
                    file = (File)fileList.get(0);
                }
                if(file != null) {
                    if(ImageUtils.isImageIOSupported(file)) {
                        readImage(file);
                        retCode = true;
                    }
                }
            } catch(Exception ex) {
                Utils.excMsg("Error dropping image:", ex);
            }
        }
        return retCode;
    }

    // Getters and Setters

    /**
     * @return Returns the current image.
     */
    public BufferedImage getCurrentImage() {
        return image;
    }

    /**
     * @return Returns the original image.
     */
    public BufferedImage getOriginalImage() {
        return image0;
    }

    /**
     * @return The value of file.
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file The new value for file.
     */
    public void setFile(File file) {
        this.file = file;
    }

}
