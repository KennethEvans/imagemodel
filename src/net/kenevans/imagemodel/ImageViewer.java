package net.kenevans.imagemodel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.kenevans.imagemodel.utils.AboutBoxPanel;
import net.kenevans.imagemodel.utils.ImageUtils;
import net.kenevans.imagemodel.utils.Utils;

public class ImageViewer extends JFrame
{
    private static final String VERSION_STRING = "Image Viewer 1.0.0.0";
    private static final String INITIAL_PATH = "c:/Java/Fit2D";
    private static String[] suffixes = {"jpg", "jpe", "jpeg", "gif", "tif",
        "tiff", "png", "bmp"};
    private static final String title = "Image Viewer";
    private static final long serialVersionUID = 1L;
    private static final int WIDTH = 600;
    private static final int HEIGHT = WIDTH;
    private static boolean doDecorations = true;

    private Container contentPane = this.getContentPane();
    private JPanel displayPanel = new JPanel();
    private ImagePanel imagePanel = null;

    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileOpen = new JMenuItem();
    private JMenuItem menuFileSaveAs = new JMenuItem();
    private JMenuItem menuFilePrint = new JMenuItem();
    private JMenuItem menuFilePrintPreview = new JMenuItem();
    private JMenuItem menuFilePageSetup = new JMenuItem();
    private JMenuItem menuFileExit = new JMenuItem();
    private JMenu menuEdit = new JMenu();
    private JMenuItem menuEditCopy = new JMenuItem();
    private JMenuItem menuEditPaste = new JMenuItem();
    private JMenuItem menuEditPastePrint = new JMenuItem();
    private JMenu menuInfo = new JMenu();
    private JMenuItem menuInfoSuffixes = new JMenuItem();
    private JCheckBoxMenuItem menuImageScale = new JCheckBoxMenuItem();
    private JMenu menuImage = new JMenu();
    private JMenuItem menuInfoImageInfo = new JMenuItem();
    private JMenuItem menuImageGamma = new JMenu();
    private JMenuItem menuGammaLighten = new JMenuItem();
    private JMenuItem menuGammaDarken = new JMenuItem();
    private JMenuItem menuGammaSpecify = new JMenuItem();
    private JMenuItem menuImageBlur = new JMenuItem();
    private JMenuItem menuImageSharpen = new JMenuItem();
    private JMenuItem menuImageGrayscale = new JMenuItem();
    private JMenu menuFlip = new JMenu();
    private JMenuItem menuFlipHorizontal = new JMenuItem();
    private JMenuItem menuFlipVertical = new JMenuItem();
    private JMenuItem menuFlipBoth = new JMenuItem();
    private JMenu menuRotate = new JMenu();
    private JMenuItem menuRotatePlus90 = new JMenuItem();
    private JMenuItem menuRotateMinus90 = new JMenuItem();
    private JMenuItem menuRotate180 = new JMenuItem();
    private JMenuItem menuRotateAny = new JMenuItem();
    private JMenu menuCrop = new JMenu();
    private JMenuItem menuEnableCrop = new JMenuItem();
    private JMenuItem menuDisableCrop = new JMenuItem();
    private JMenuItem menuCropCrop = new JMenuItem();
    private JMenuItem menuImageRestore = new JMenuItem();
    private JMenu menuHelp = new JMenu();
    private JMenuItem menuHelpOverview = new JMenuItem();
    private JMenuItem menuHelpAbout = new JMenuItem();

    private double initialRotateAngle = 0.0;
    private double initialGamma = ImageModel.GAMMA_LIGHTEN;
    private String defaultPath = INITIAL_PATH;
    private ImageModel imageModel = new ImageModel();

    public ImageViewer() {
        this(true);
    }

    public ImageViewer(boolean initializeUI) {
        this(initializeUI, null);
    }

    public ImageViewer(boolean initializeUI, BufferedImage image) {
        if(image != null && imageModel != null) imageModel.replaceImage(image);

        // Get the available names
        if(false) {
            String[] readerFormatNames = ImageIO.getReaderFormatNames();
            // There are 6 names without the JAI tools. Use our names (some will
            // fail)
            // unless there is an indication more are supported. This presumably
            // means
            // the JAI tools are installed.
            if(readerFormatNames != null && readerFormatNames.length > 6) {
                suffixes = readerFormatNames;
            }
        } else {
            // There are 4 suffices without the JAI tools. Use our names (some
            // will
            // fail)
            // unless there is an indication more are supported. This presumably
            // means
            // the JAI tools are installed.
            Set<String> readerSuffixes = ImageUtils.getReaderSuffixes();
            int size = readerSuffixes.size();
            if(size > 4) {
                suffixes = new String[size];
                Iterator<String> iter1 = readerSuffixes.iterator();
                int i = 0;
                while(iter1.hasNext()) {
                    String suffix = (String)iter1.next();
                    suffixes[i] = suffix;
                    i++;
                }
            }
        }

        // Return here if we are not to initialize the UI
        if(!initializeUI) return;

        // Initialize the UI
        uiInit();

        // Menus
        initMenus();

        // Make the image panel be focusable so ^C and ^V will operate on the
        // panel
        // after the mouse is clicked there. Also requires requestFocusInWindow
        // in
        // the mousePressed handler.
        setFocusable(true);
    }

    private void uiInit() {
        this.setTitle(title);

        // Display panel
        displayPanel.setLayout(new BorderLayout());
        displayPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        imagePanel = new ImagePanel(imageModel);
        displayPanel.add(imagePanel);

        // Content pane
        contentPane.setLayout(new BorderLayout());
        contentPane.add(displayPanel, BorderLayout.NORTH);
    }

    private void initMenus() {
        // Menu
        this.setJMenuBar(menuBar);

        // File
        menuFile.setText("File");
        menuBar.add(menuFile);

        // File Open
        menuFileOpen.setText("Open...");
        menuFileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                open();
            }
        });
        menuFile.add(menuFileOpen);

        // File Save as
        menuFileSaveAs.setText("Save As...");
        menuFileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
            InputEvent.CTRL_DOWN_MASK));
        menuFileSaveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel != null) {
                    imagePanel.setDefaultPath(defaultPath);
                    imagePanel.saveAs();
                    defaultPath = imagePanel.getDefaultPath();
                }
            }
        });
        menuFile.add(menuFileSaveAs);

        // File Print
        menuFilePrint.setText("Print...");
        menuFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
            InputEvent.CTRL_DOWN_MASK));
        menuFilePrint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel != null) imagePanel.print();
            }
        });
        menuFile.add(menuFilePrint);

        // File Print Preview
        menuFilePrintPreview.setText("Print Preview...");
        menuFilePrintPreview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel != null) imagePanel.printPreview();
            }
        });
        menuFile.add(menuFilePrintPreview);

        // File Page Setup
        menuFilePageSetup.setText("Page Setup...");
        menuFilePageSetup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel != null) imagePanel.pageSetup();
            }
        });
        menuFile.add(menuFilePageSetup);

        // File Exit
        menuFileExit.setText("Exit");
        menuFileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                quit();
            }
        });
        menuFile.add(menuFileExit);

        // Edit
        menuEdit.setText("Edit");
        menuBar.add(menuEdit);

        // Edit Copy
        menuEditCopy.setText("Copy");
        menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
            InputEvent.CTRL_DOWN_MASK));
        menuEditCopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel != null) imagePanel.copy();
            }
        });
        menuEdit.add(menuEditCopy);

        // Edit Paste
        menuEditPaste.setText("Paste");
        menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
            InputEvent.CTRL_DOWN_MASK));
        menuEditPaste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel != null) {
                    imagePanel.paste();
                    ImageViewer.this.setTitle("New Image");
                }
            }
        });
        menuEdit.add(menuEditPaste);

        // Edit Paste and Print
        menuEditPastePrint.setText("Paste and Print");
        menuEditPastePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
            InputEvent.CTRL_DOWN_MASK));
        menuEditPastePrint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                imagePanel.paste();
                imagePanel.print();
            }
        });
        menuEdit.add(menuEditPastePrint);

        // Info
        menuInfo.setText("Info");
        menuBar.add(menuInfo);

        // Image Info
        menuInfoImageInfo.setText("Image Info");
        menuInfoImageInfo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                imageInfo();
            }
        });
        menuInfo.add(menuInfoImageInfo);

        // InfoView Suffixes
        menuInfoSuffixes.setText("Avaliable suffixes...");
        menuInfoSuffixes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                showSuffixes();
            }
        });
        menuInfo.add(menuInfoSuffixes);

        // Image
        menuImage.setText("Image");
        menuBar.add(menuImage);

        // Image Gamma
        menuImageGamma.setText("Gamma");
        menuImage.add(menuImageGamma);

        // Gamma Lighten
        menuGammaLighten.setText("Lighten");
        menuGammaLighten.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.gamma(ImageModel.GAMMA_LIGHTEN);
            }
        });
        menuImageGamma.add(menuGammaLighten);

        // Gamma Darken
        menuGammaDarken.setText("Darken");
        menuGammaDarken.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.gamma(ImageModel.GAMMA_DARKEN);
            }
        });
        menuImageGamma.add(menuGammaDarken);

        // Gamma Specify
        menuGammaSpecify.setText("Specify Gamma...");
        menuGammaSpecify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                String result = JOptionPane.showInputDialog("Enter gamma",
                    Double.toString(initialGamma));
                if(result != null) {
                    double gamma = Double.valueOf(result).doubleValue();
                    initialGamma = gamma;
                    imageModel.gamma(gamma);
                }
            }
        });
        menuImageGamma.add(menuGammaSpecify);

        // Image Blur
        menuImageBlur.setText("Blur");
        menuImageBlur.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.blur();
            }
        });
        menuImage.add(menuImageBlur);

        // Image Sharpen
        menuImageSharpen.setText("Sharpen");
        menuImageSharpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.sharpen();
            }
        });
        menuImage.add(menuImageSharpen);

        // Image Grayscale
        menuImageGrayscale.setText("Grayscale");
        menuImageGrayscale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.grayscale();
            }
        });
        menuImage.add(menuImageGrayscale);

        // Flip
        menuFlip.setText("Flip");
        menuImage.add(menuFlip);

        // Flip Horizontal
        menuFlipHorizontal.setText("Horizontal");
        menuFlipHorizontal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.flip(true, false);
            }
        });
        menuFlip.add(menuFlipHorizontal);

        // Flip Vertical
        menuFlipVertical.setText("Vertical");
        menuFlipVertical.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.flip(false, true);
            }
        });
        menuFlip.add(menuFlipVertical);

        // Flip Both
        menuFlipBoth.setText("Both");
        menuFlipBoth.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.flip(true, true);
            }
        });
        menuFlip.add(menuFlipBoth);

        // Rotate
        menuRotate.setText("Rotate");
        menuImage.add(menuRotate);

        // Rotate 90
        menuRotatePlus90.setText("90 degrees CW");
        menuRotatePlus90.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.rotate(90);
            }
        });
        menuRotate.add(menuRotatePlus90);

        // Rotate -90
        menuRotateMinus90.setText("90 degrees CCW");
        menuRotateMinus90.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.rotate(-90);
            }
        });
        menuRotate.add(menuRotateMinus90);

        // Rotate 180
        menuRotate180.setText("180 degrees");
        menuRotate180.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.rotate(180);
                // Do flip instead
                // imageModel.flip(true, true);
            }
        });
        menuRotate.add(menuRotate180);

        // Rotate specify
        menuRotateAny.setText("Specify Angle...");
        menuRotateAny.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                String result = JOptionPane.showInputDialog(
                    "Enter CW angle in degrees",
                    Double.toString(initialRotateAngle));
                if(result != null) {
                    double degrees = Double.valueOf(result).doubleValue();
                    initialRotateAngle = degrees;
                    ;
                    imageModel.rotate(degrees);
                }
            }
        });
        menuRotate.add(menuRotateAny);

        // Crop
        menuCrop.setText("Crop");
        menuImage.add(menuCrop);

        // Enable
        menuEnableCrop.setText("Enable");
        menuEnableCrop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel == null) return;
                imagePanel.setMode(ImagePanel.Mode.CROP);
            }
        });
        menuCrop.add(menuEnableCrop);

        // Disable
        menuDisableCrop.setText("Disable");
        menuDisableCrop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel == null) return;
                imagePanel.setMode(ImagePanel.Mode.NONE);
            }
        });
        menuCrop.add(menuDisableCrop);

        // Crop
        menuCropCrop.setText("Crop");
        menuCropCrop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel == null) return;
                imagePanel.crop();
            }
        });
        menuCrop.add(menuCropCrop);

        // Image Scale
        menuImageScale.setText("Scale Image");
        menuImageScale.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
            InputEvent.CTRL_DOWN_MASK));
        menuImageScale.setState(imagePanel.isScaled());
        menuImageScale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                scaleImage();
            }
        });
        menuImage.add(menuImageScale);

        // Image Restore
        menuImageRestore.setText("Restore");
        menuImageRestore.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
            InputEvent.CTRL_DOWN_MASK));
        menuImageRestore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.restore();
            }
        });
        menuImage.add(menuImageRestore);

        // Help
        menuHelp.setText("Help");
        menuBar.add(menuHelp);

        menuHelpOverview.setText("Overview");
        menuHelpOverview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                overview();
            }
        });
        menuHelp.add(menuHelpOverview);

        menuHelpAbout.setText("About");
        menuHelpAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(null, new AboutBoxPanel(
                    VERSION_STRING), "About", JOptionPane.PLAIN_MESSAGE);
            }
        });
        menuHelp.add(menuHelpAbout);
    }

    /**
     * Implements opening a file with a JFileChooser.
     */
    private void open() {
        if(displayPanel == null || imagePanel == null) return;
        String fileName = null;
        File file = null;
        JFileChooser chooser = new JFileChooser();
        if(defaultPath != null) {
            chooser.setCurrentDirectory(new File(defaultPath));
        }
        int result = chooser.showOpenDialog(this);
        if(result != JFileChooser.APPROVE_OPTION) return;
        // Save the selected path for next time
        defaultPath = chooser.getSelectedFile().getParentFile().getPath();
        // Process the file
        file = chooser.getSelectedFile();
        fileName = file.getPath();
        if(fileName == null || fileName.length() == 0) {
            Utils.errMsg("Bad filename");
            return;
        }
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            imageModel.readImage(file);
            this.setTitle(file.getPath());
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Removes references. Use this to allow it to be garbage collected.
     */
    public void finish() {
        setImageModel(null);
        setImagePanel(null);
    }

    /**
     * Quits the application.
     */
    private void quit() {
        System.exit(0);
    }

    /**
     * Toggles scaling the image.
     */
    private void scaleImage() {
        if(menuImageScale != null && imagePanel != null) {
            imagePanel.setScaled(menuImageScale.getState());
        }
    }

    /**
     * Shows the image info.
     */
    private void imageInfo() {
        if(imageModel == null) return;
        String info = imageModel.getInfo();
        Utils.infoMsg(info);
    }

    /**
     * Shows available suffices in a message box.
     */
    private static void showSuffixes() {
        String info = ImageUtils.getSuffixInfo();
        Utils.infoMsg(info);
    }

    /**
     * Overview message.
     */
    private void overview() {
        String ls = Utils.LS;
        StringBuilder sb = new StringBuilder();
        sb.append("Image Viewer allows you to choose a directory and display"
            + ls);
        sb.append("images in that directory.  You can save the image as a" + ls);
        sb.append("new file and do Cut, Copy, and Paste.  Some image" + ls);
        sb.append("manipulation is available from the Image menu." + ls);
        sb.append("" + ls);
        sb.append("The image formats you can use will depend on whether" + ls);
        sb.append("Java Advanced Imaging (JAI and JAI ImageIO) are" + ls);
        sb.append("installed.  You can see the available formats under the "
            + ls);
        sb.append("Info menu." + ls);
        sb.append("" + ls);
        sb.append("Accelerator keys:" + ls);
        sb.append("  Copy: Ctrl-C" + ls);
        sb.append("  Paste: Ctrl-V" + ls);
        sb.append("  Paste and Print: Ctrl-B" + ls);
        sb.append("  Print: Ctrl-P" + ls);
        sb.append("  Scale (Fit): Ctrl-F" + ls);
        sb.append("  Save: Ctrl-S" + ls);
        sb.append("  Restore: Ctrl-R" + ls);
        Utils.infoMsg(sb.toString());
    }

    // Getters and setters

    /**
     * @return The value of imagePanel.
     */
    public ImagePanel getImagePanel() {
        return imagePanel;
    }

    /**
     * @param imagePanel the imagePanel to set
     */
    public void setImagePanel(ImagePanel imagePanel) {
        if(imagePanel == this.imagePanel) return;
        if(displayPanel != null && this.imagePanel != null) {
            displayPanel.remove(this.imagePanel);
        }
        this.imagePanel = imagePanel;
        if(displayPanel != null && this.imagePanel != null) {
            displayPanel.add(imagePanel);
        }
    }

    /**
     * @return The value of imageModel.
     */
    public ImageModel getImageModel() {
        return imageModel;
    }

    /**
     * @param imageModel The new value for imageModel.
     */
    public void setImageModel(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    /**
     * @return The value of VERSION_STRING.
     */
    public static String getVersionString() {
        return VERSION_STRING;
    }

    /**
     * Runs the application in its JFrame.
     */
    public void run() {
        // Make the job run in the AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    if(doDecorations) {
                        UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                        SwingUtilities.updateComponentTreeUI(ImageViewer.this);
                    }
                    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    ImageUtils.setIconImageFromResource(ImageViewer.this,
                        "/resources/ImageViewer.32x32.gif");
                    pack();
                    setVisible(true);
                    setLocationRelativeTo(null);
                } catch(Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    /**
     * Main program.
     * 
     * @param args
     */
    public static void main(String[] args) {
        ImageViewer app = new ImageViewer();
        app.run();
    }

}
