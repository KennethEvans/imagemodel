package net.kenevans.imagemodel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.kenevans.imagemodel.ScrolledImagePanel.Mode;
import net.kenevans.imagemodel.colorset.ColorSetDialog;
import net.kenevans.imagemodel.utils.AboutBoxPanel;
import net.kenevans.imagemodel.utils.ImageUtils;
import net.kenevans.imagemodel.utils.Utils;

public class ImageBrowser extends JFrame
{
    private static final long serialVersionUID = 1L;
    private static final boolean INITIALIZE_PATH = false; // For developing
    private static final boolean INITIAL_CROP_ENABLED = true;
    private static final boolean USE_STATUS_BAR = true;
    private static final String VERSION_STRING = "Image Browser 4.0.0";
    // private static final String INITIAL_PATH = "c:/users/evans/Pictures";
    private static final String INITIAL_PATH = "C:/Users/evans/Pictures/Android/Herondance";
    private static final String[] HOME_LOCATIONS = {"HOME", "HOME_PATH"};
    private static final String[] PICTURE_LOCATIONS = {"My Pictures",
        "Pictures"};
    private static String[] suffixes = {"jpg", "jpe", "jpeg", "gif", "tif",
        "tiff", "png", "bmp"};
    
    private File lastColorSetFile;

    public static enum ControlPanelMode {
        NONE, OPEN, ZOOM, CROP
    };

    private ControlPanelMode controlPanelMode = ControlPanelMode.NONE;
    private static final String TITLE = "Image Browser";
    private static final int WIDTH = 600;
    private static final int HEIGHT = WIDTH;
    private static final int MAIN_PANE_DIVIDER_LOCATION = 180;
    private static final int DIR_TEXT_COLS = 30;
    private PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
    private PageFormat pageFormat = PrinterJob.getPrinterJob().defaultPage();
    private PrintService printService = null;
    private boolean fitAlways = false;
    private boolean fitIfLarger = false;

    private Comparator<File> fileComparator = null;
    private Container contentPane = this.getContentPane();
    private JToolBar toolBar = new JToolBar("ImageBrowser Tool Bar");
    private JPanel mainPanel = new JPanel();
    private JPanel controlPanel = new JPanel();
    private JPanel listPanel = new JPanel();
    private JPanel displayPanel = new JPanel();
    private JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        listPanel, displayPanel);
    private DefaultListModel<File> listModel = new DefaultListModel<File>();
    private JList<File> list = new JList<File>(listModel);
    private JScrollPane listScrollPane;
    private ScrolledImagePanel imagePanel = null;
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menuFile = new JMenu();
    private JMenuItem menuFileOpen = new JMenuItem();
    private JMenuItem menuDirectoryOpen = new JMenuItem();
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
    private JCheckBoxMenuItem menuImageFit = new JCheckBoxMenuItem();
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
    private ImageBrowser frame = this;
    private double initialRotateAngle = 0.0;
    private double initialGamma = ImageModel.GAMMA_LIGHTEN;
    private String currentDir = null;
    private File directoryFile = null;
    private ImageModel imageModel = new ImageModel();

    private JMenuItem menuColorSet = new JMenuItem();

    private JCheckBox controlPanelCropEnableCheckBox;
    private JButton controlPanelCropButton;
    private JButton controlPanelRestoreCropButton;
    private JTextField dirText;
    private JButton browseButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton zoomResetButton;
    private JButton zoomFitButton;

    public ImageBrowser() {
        this(true);
    }

    public ImageBrowser(boolean initializeUI) {
        // Create the file comparator
        fileComparator = new Comparator<File>() {
            public int compare(File fa, File fb) {
                return (fa.getName().compareTo(fb.getName()));
            }
        };

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
                    String suffix = iter1.next();
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
        menuInit();

        // Make the image panel be focusable so ^C and ^V will operate on the
        // panel
        // after the mouse is clicked there. Also requires requestFocusInWindow
        // in
        // the mousePressed handler.
        setFocusable(true);

        // Set whether crop is enabled by default. When there are more mouse
        // operations, this may not be advisable.
        setCrop(INITIAL_CROP_ENABLED);

        // Set the initial directory (useful when debugging)
        if(INITIALIZE_PATH && INITIAL_PATH != null) {
            File file = new File(INITIAL_PATH);
            currentDir = INITIAL_PATH;
            if(dirText != null) {
                dirText.setText(file.getPath());
            }
            populateList(currentDir);
        } else {
            // TODO fix this for other platforms than Windows
            // Try to find the home directory
            String userDir = null;
            for(String dir : HOME_LOCATIONS) {
                userDir = System.getenv(dir);
                if(userDir != null) {
                    break;
                }
            }
            // Try to find the Pictures directory
            if(userDir != null) {
                File file;
                for(String dir : PICTURE_LOCATIONS) {
                    file = new File(userDir + File.separator + dir);
                    if(file.exists()) {
                        currentDir = file.getPath();
                        break;
                    }
                }
            }
        }
    }

    private void uiInit() {
        this.setTitle(TITLE);

        // List panel
        listScrollPane = new JScrollPane(list);
        listPanel.setLayout(new BorderLayout());
        listPanel.add(listScrollPane, BorderLayout.CENTER);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent ev) {
                onListValueChanged(ev);
            }
        });
        if(false) {
            // This version does not show the selection as highlighted
            list.setCellRenderer(new ListCellRenderer<Object>() {
                public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                    File file = (File)value;
                    JLabel label = new JLabel(file.getName());
                    return label;
                }
            });
        } else if(true) {
            // This version shows the selection as highlighted
            list.setCellRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                    JLabel label = (JLabel)super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                    // Use the file name as the text
                    File file = (File)value;
                    label.setText(file.getName());
                    return label;
                }
            });
        } else {
            // This version colors alternating labels
            list.setCellRenderer(new DefaultListCellRenderer() {
                private static final long serialVersionUID = 1L;

                public Component getListCellRendererComponent(JList<?> list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                    JLabel label = (JLabel)super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                    // Make stripes
                    if(index % 2 == 0) {
                        // label.setBackground(new Color(204, 255, 204)); // Lt.
                        // Green
                        // label.setBackground(new Color(204, 255, 255)); // Lt.
                        // Cyan
                        // label.setBackground(new Color(210, 240, 255)); // Lt.
                        // Blue
                        // label.setBackground(new Color(245, 245, 245)); // Lt.
                        // Gray
                        Color bg = listPanel.getBackground();
                        label.setBackground(bg);
                    } else {
                        // label.setBackground(new Color(255, 255, 204)); // Lt.
                        // Yellow
                    }
                    // Use the file name as the text
                    File file = (File)value;
                    label.setText(file.getName());
                    return label;
                }
            });
        }

        // Display panel
        displayPanel.setLayout(new BorderLayout());
        displayPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        imagePanel = new ScrolledImagePanel(imageModel, USE_STATUS_BAR);
        displayPanel.add(imagePanel);

        // Main split pane
        mainPane.setContinuousLayout(true);
        mainPane.setDividerLocation(MAIN_PANE_DIVIDER_LOCATION);
        if(false) {
            mainPane.setOneTouchExpandable(true);
        }

        // Create the tool bar
        toolbarInit();

        // Main panel
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(mainPane, BorderLayout.CENTER);

        // Content pane
        // For the drag behavior to work correctly, the tool bar must be in a
        // container that uses the BorderLayout layout manager. The component
        // that
        // the tool bar affects is generally in the center of the container. The
        // tool bar must be the only other component in the container, and it
        // must
        // not be in the center.
        contentPane.setLayout(new BorderLayout());
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(mainPanel, BorderLayout.CENTER);
    }

    private void toolbarInit() {
        // Settings
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        // Open button
        JButton button = makeToolBarButton("/resources/fldr_obj.gif",
            "Open Folder", "Open Folder");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                resetControlPanel(ControlPanelMode.OPEN);
            }
        });

        // Refresh directory button
        button = makeToolBarButton("/resources/refresh.gif",
            "Refresh Directory", "Refresh Directory");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                populateList(currentDir);
            }
        });

        // Save As button
        button = makeToolBarButton("/resources/saveas_edit.gif", "Save As",
            "Save As");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                saveAs();
            }
        });

        // Separator
        toolBar.addSeparator();

        // Copy button
        button = makeToolBarButton("/resources/copy_edit.gif", "Copy", "Copy");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                copy();
            }
        });

        // Paste button
        button = makeToolBarButton("/resources/paste_edit.gif", "Paste",
            "Paste");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                paste();
            }
        });

        // Print button
        button = makeToolBarButton("/resources/print_edit.gif", "Print",
            "Print");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                print();
            }
        });

        // Separator
        toolBar.addSeparator();

        // Restore button
        button = makeToolBarButton("/resources/undo_edit.gif", "Restore",
            "Restore");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if(imageModel != null) {
                    imageModel.restore();
                }
            }
        });

        // Scale button
        button = makeToolBarButton("/resources/scale.gif",
            "Toggle fit image to current area", "Fit Image");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                fitIfLarger = !fitIfLarger;
                fitImage();
            }
        });

        // Zoom button
        button = makeToolBarButton("/resources/zoom.gif", "Zoom Controls",
            "Zoom");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                resetControlPanel(ControlPanelMode.ZOOM);
            }
        });

        // Crop button
        button = makeToolBarButton("/resources/crop.gif", "Toggle crop",
            "Crop");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                resetControlPanel(ControlPanelMode.CROP);
            }
        });

        // Separator
        toolBar.addSeparator();

        // Control panel
        // controlPanel.setBackground(Color.RED);
        controlPanel.setLayout(new BorderLayout(5, 5));
        toolBar.add(controlPanel);
    }

    /**
     * Sets the control panel according to mode.
     * 
     * @param mode The mode to set.
     */
    protected void resetControlPanel(ControlPanelMode mode) {
        // Clean out the old contents
        controlPanel.removeAll();
        // All components that are global and created in createXXX methods must
        // be
        // null'ed here
        controlPanelCropButton = null;
        controlPanelCropEnableCheckBox = null;
        dirText = null;
        browseButton = null;
        zoomInButton = null;
        zoomOutButton = null;
        zoomResetButton = null;
        zoomFitButton = null;

        // Make new contents
        if(mode == ControlPanelMode.NONE) {
            createEmptyControlPanel();
        } else if(mode == ControlPanelMode.OPEN) {
            createOpenControlPanel();
        } else if(mode == ControlPanelMode.ZOOM) {
            createZoomControlPanel();
        } else if(mode == ControlPanelMode.CROP) {
            createCropControlPanel();
        }

        // Set the mode
        this.controlPanelMode = mode;
    }

    /**
     * Makes an empty control panel.
     */
    protected void createEmptyControlPanel() {
        controlPanel.repaint();
        contentPane.validate();
    }

    /**
     * Adds open components to the control panel.
     */
    protected void createOpenControlPanel() {
        JLabel label = new JLabel("Directory:");
        controlPanel.add(label, BorderLayout.LINE_START);

        dirText = new JTextField();
        if(currentDir != null) {
            dirText.setText(currentDir);
        }
        dirText.setColumns(DIR_TEXT_COLS);
        controlPanel.add(dirText, BorderLayout.CENTER);
        dirText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                populateList(dirText.getText());
            }
        });

        browseButton = new JButton();
        browseButton.setText("Browse");
        controlPanel.add(browseButton, BorderLayout.LINE_END);
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                openDirectory();
            }
        });

        controlPanel.repaint();
        contentPane.validate();
    }

    /**
     * Adds zoom components to the control panel.
     */
    protected void createZoomControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        controlPanel.add(panel, BorderLayout.LINE_START);

        JPanel panel1 = new JPanel(new BorderLayout());
        panel.add(panel1, BorderLayout.LINE_END);
        JPanel panel2 = new JPanel(new BorderLayout());
        panel1.add(panel2, BorderLayout.LINE_END);

        zoomInButton = new JButton();
        zoomInButton.setText("Zoom In");
        panel.add(zoomInButton, BorderLayout.LINE_START);
        zoomInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if(imagePanel != null) {
                    imagePanel.zoomIn();
                }
            }
        });

        zoomOutButton = new JButton();
        zoomOutButton.setText("Zoom Out");
        panel1.add(zoomOutButton, BorderLayout.LINE_START);
        zoomOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if(imagePanel != null) {
                    imagePanel.zoomOut();
                }
            }
        });

        zoomResetButton = new JButton();
        zoomResetButton.setText("Zoom Reset");
        panel2.add(zoomResetButton, BorderLayout.LINE_START);
        zoomResetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if(imagePanel != null) {
                    imagePanel.zoomReset();
                }
            }
        });

        zoomFitButton = new JButton();
        zoomFitButton.setText("Zoom Fit");
        panel2.add(zoomFitButton, BorderLayout.LINE_END);
        zoomFitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if(imagePanel != null) {
                    imagePanel.zoomFit();
                }
            }
        });

        controlPanel.repaint();
        contentPane.validate();
    }

    /**
     * Adds crop components to the control panel.
     */
    protected void createCropControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        controlPanel.add(panel, BorderLayout.LINE_START);

        controlPanelCropEnableCheckBox = new JCheckBox("Enable");
        panel.add(controlPanelCropEnableCheckBox, BorderLayout.LINE_START);
        controlPanelCropEnableCheckBox.setSelected(getCrop());
        controlPanelCropEnableCheckBox.setText("Enable Crop");
        controlPanelCropEnableCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setCrop(controlPanelCropEnableCheckBox.isSelected());
            }
        });

        controlPanelRestoreCropButton = new JButton("Previous");
        panel.add(controlPanelRestoreCropButton);
        controlPanelRestoreCropButton
            .setToolTipText("Restore the last crop rectangle used");
        controlPanelRestoreCropButton.setEnabled(getCrop());
        controlPanelRestoreCropButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if(imagePanel == null) return;
                imagePanel.restoreClipRectangle();
                imagePanel.repaint();
                imagePanel.revalidate();
            }
        });

        controlPanelCropButton = new JButton("Crop");
        panel.add(controlPanelCropButton, BorderLayout.LINE_END);
        controlPanelCropButton.setToolTipText("Perform the crop");
        controlPanelCropButton.setEnabled(getCrop());
        controlPanelCropButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if(imagePanel == null) return;
                imagePanel.crop();
                imagePanel.repaint();
                imagePanel.revalidate();
            }
        });

        controlPanel.repaint();
        contentPane.validate();
    }

    /**
     * Makes a button for the tool bar.
     * 
     * @param imageName Path to the image.
     * @param toolTipText Text for the tool tip.
     * @param altText Button text when image not found.
     * @return The button.
     */
    protected JButton makeToolBarButton(String imageName, String toolTipText,
        String altText) {
        // Look for the image.
        URL imageURL = ImageBrowser.class.getResource(imageName);

        // Create and initialize the button.
        JButton button = new JButton();
        button.setToolTipText(toolTipText);

        if(imageURL != null) {
            button.setIcon(new ImageIcon(imageURL, altText));
        } else {
            button.setText(altText);
            Utils.errMsg("Resource not found: " + imageName);
        }

        toolBar.add(button);

        return button;
    }

    private void menuInit() {
        // Menu
        this.setJMenuBar(menuBar);

        // File
        menuFile.setText("File");
        menuBar.add(menuFile);

        // Directory Open
        menuDirectoryOpen.setText("Open Directory...");
        menuDirectoryOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openDirectory();
            }
        });
        menuFile.add(menuDirectoryOpen);

        // File Open
        menuFileOpen.setText("Open File...");
        menuFileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                openFile();
            }
        });
        menuFile.add(menuFileOpen);

        // File Save as
        menuFileSaveAs.setText("Save As...");
        menuFileSaveAs.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        menuFileSaveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                saveAs();
            }
        });
        menuFile.add(menuFileSaveAs);

        // File Print
        menuFilePrint.setText("Print...");
        menuFilePrint.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        menuFilePrint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                print();
            }
        });
        menuFile.add(menuFilePrint);

        // File Print Preview
        menuFilePrintPreview.setText("Print Preview...");
        menuFilePrintPreview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                printPreview();
            }
        });
        menuFile.add(menuFilePrintPreview);

        // File Page Setup
        menuFilePageSetup.setText("Page Setup...");
        menuFilePageSetup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                pageSetup();
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
        menuEditCopy.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        menuEditCopy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                copy();
            }
        });
        menuEdit.add(menuEditCopy);

        // Edit Paste
        menuEditPaste.setText("Paste");
        menuEditPaste.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        menuEditPaste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                paste();
            }
        });
        menuEdit.add(menuEditPaste);

        // Edit Paste and Print
        menuEditPastePrint.setText("Paste and Print");
        menuEditPastePrint.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        menuEditPastePrint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                paste();
                print();
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
                imagePanel.revalidate();
            }
        });
        menuFlip.add(menuFlipHorizontal);

        // Flip Vertical
        menuFlipVertical.setText("Vertical");
        menuFlipVertical.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.flip(false, true);
                imagePanel.revalidate();
            }
        });
        menuFlip.add(menuFlipVertical);

        // Flip Both
        menuFlipBoth.setText("Both");
        menuFlipBoth.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.flip(true, true);
                imagePanel.revalidate();
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
                imagePanel.revalidate();
            }
        });
        menuRotate.add(menuRotatePlus90);

        // Rotate -90
        menuRotateMinus90.setText("90 degrees CCW");
        menuRotateMinus90.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.rotate(-90);
                imagePanel.revalidate();
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
                imagePanel.revalidate();
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
                    imagePanel.revalidate();
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
                setCrop(true);
            }
        });
        menuCrop.add(menuEnableCrop);

        // Disable
        menuDisableCrop.setText("Disable");
        menuDisableCrop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel == null) return;
                setCrop(false);
            }
        });
        menuCrop.add(menuDisableCrop);

        // Crop
        menuCropCrop.setText("Crop");
        menuCropCrop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imagePanel == null) return;
                imagePanel.crop();
                imagePanel.revalidate();
            }
        });
        menuCrop.add(menuCropCrop);

        // Image Fit
        menuImageFit.setText("Fit Image");
        menuImageFit.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        menuImageFit.setState(fitIfLarger);
        menuImageFit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                fitIfLarger = menuImageFit.isSelected();
                fitImage();
            }
        });
        menuImage.add(menuImageFit);

        // Image Restore
        menuImageRestore.setText("Restore");
        menuImageRestore.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        menuImageRestore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                imageModel.restore();
                imagePanel.revalidate();
            }
        });
        menuImage.add(menuImageRestore);

        menuImage.add(new JSeparator());

        // Make Color Set
        menuColorSet.setText("Make Color Set...");
        menuColorSet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(imageModel == null) return;
                makeColorSet();
            }
        });
        menuImage.add(menuColorSet);

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
                JOptionPane.showMessageDialog(null,
                    new AboutBoxPanel(VERSION_STRING), "About",
                    JOptionPane.PLAIN_MESSAGE);
            }
        });
        menuHelp.add(menuHelpAbout);
    }

    /**
     * Implements opening a file.
     */
    private void openFile() {
        if(displayPanel == null) return;
        JFileChooser chooser = new JFileChooser();
        if(currentDir != null) {
            File file = new File(currentDir);
            if(file != null && file.exists()) {
                chooser.setCurrentDirectory(file);
            }
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            Cursor oldCursor = getCursor();
            try {
                File file = chooser.getSelectedFile();
                // Save the selected path for next time
                currentDir = chooser.getSelectedFile().getParentFile()
                    .getPath();
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                imageModel.readImage(file);
                fitImage();
                this.setTitle(file.getPath());
            } finally {
                setCursor(oldCursor);
            }
        }
    }

    /**
     * Implements opening a directory.
     */
    private void openDirectory() {
        if(displayPanel == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(currentDir != null) {
            File file = new File(currentDir);
            // File parent = file.getParentFile();
            // if(parent != null && parent.exists()) {
            // chooser.setCurrentDirectory(parent);
            // } else if(file != null && file.exists()) {
            // chooser.setCurrentDirectory(file);
            // }
            if(file != null && file.exists()) {
                chooser.setCurrentDirectory(file);
            }
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Save the selected path for next time
            currentDir = chooser.getSelectedFile().getPath();
            if(dirText != null) {
                dirText.setText(currentDir);
            }
            populateList(currentDir);
        }
    }

    /**
     * Implements print.
     */
    public void print() {
        if(displayPanel == null) return;
        if(imageModel == null) return;
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PrinterJob job = PrinterJob.getPrinterJob();
            // Use the PrintService (printer) specified last time
            if(printService != null) job.setPrintService(printService);
            if(job.printDialog(printAttributes)) {
                job.setPrintable(imageModel);
                job.print(printAttributes);
                printService = job.getPrintService();
            }
        } catch(Exception ex) {
            Utils.excMsg("Printing failed:", ex);
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Implements print setup.
     */
    public void pageSetup() {
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PrinterJob job = PrinterJob.getPrinterJob();
            pageFormat = job.pageDialog(printAttributes);
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Implements print preview.
     */
    public void printPreview() {
        if(displayPanel == null) return;
        if(imageModel == null) return;
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PrintPreviewDialog dialog = new PrintPreviewDialog(imageModel,
                pageFormat, 1);
            dialog.setVisible(true);
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Saves the image to the clipboard.
     */
    public void copy() {
        if(displayPanel == null) return;
        if(imageModel == null) return;
        BufferedImage image = imageModel.getCurrentImage();
        if(image == null) {
            Utils.errMsg("No image");
            return;
        }
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            imageModel.copy();
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Pastes an image.
     */
    public void paste() {
        if(displayPanel == null) return;
        if(imageModel == null) return;
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            imageModel.paste();
            fitImage();
            this.setTitle("New Image");
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Saves the display frame to a file
     */
    public void saveAs() {
        if(displayPanel == null) return;
        if(imageModel == null) return;
        BufferedImage image = imageModel.getCurrentImage();
        if(image == null) {
            Utils.errMsg("No image");
            return;
        }
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            File file = ImageUtils.saveImageToFile(image, currentDir);
            if(file != null && file.exists()) {
                imageModel.setFile(file);
                // Set the currentDirectory based on what was saved
                File parent = file.getParentFile();
                if(parent != null && parent.exists()) {
                    currentDir = parent.getPath();
                } else {
                    currentDir = file.getPath();
                }
            }
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Saves the display frame to a file
     */
    public boolean save(File file) {
        boolean retVal = false;
        if(displayPanel == null) return false;
        if(imageModel == null) return false;
        BufferedImage image = imageModel.getCurrentImage();
        if(image == null) {
            Utils.errMsg("No image");
            return false;
        }
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            retVal = ImageUtils.saveImage(image, file);
            frame.setCursor(Cursor.getDefaultCursor());
        } finally {
            setCursor(oldCursor);
        }
        return retVal;
    }

    /**
     * Quits the application
     */
    private void quit() {
        System.exit(0);
    }

    /**
     * Scales the image according to the fitAlways and fitIfLarger;
     */
    private void fitImage() {
        menuImageFit.setSelected(fitIfLarger);
        if(imagePanel == null) {
            return;
        }
        if(fitAlways) {
            imagePanel.zoomFit();
        } else if(fitIfLarger) {
            imagePanel.zoomFitIfLarger();
        } else {
            imagePanel.zoomReset();
        }
    }

    /**
     * Scales the image according to the fitAlways and fitIfLarger;
     */
    private void makeColorSet() {
        if(imagePanel == null || imageModel == null) {
            return;
        }
        BufferedImage cur = imageModel.getCurrentImage();
        if(cur == null) return;
        ColorSetDialog dialog = new ColorSetDialog(this, cur);
        // For modal, use this and dialog.showDialog() instead of
        // dialog.setVisible(true)
        // dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        URL url = ImageBrowser.class.getResource("/ImageBrowser.32x32.gif");
        if(url != null) {
            dialog.setIconImage(new ImageIcon(url).getImage());
        }
        dialog.setVisible(true);
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
     * Handler for the list.
     * 
     * @param ev
     */
    private void onListValueChanged(ListSelectionEvent ev) {
        if(ev.getValueIsAdjusting()) return;
        if(imageModel == null) return;
        File file = list.getSelectedValue();
        if(file == null) return;
        Cursor oldCursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            imageModel.readImage(file);
            imagePanel.revalidate();
            // Revalidate the scroll pane's client
            fitImage();
            this.setTitle(file.getPath());
        } finally {
            setCursor(oldCursor);
        }
    }

    /**
     * Handler for the directory TextField.
     * 
     * @param ev
     */
    private void populateList(String directoryName) {
        if(directoryName == null) return;
        directoryFile = new File(directoryName);
        if(!directoryFile.isDirectory()) {
            Utils.errMsg("Invalid directory:\n" + directoryFile.getPath());
            return;
        }
        currentDir = directoryName;
        File[] files = directoryFile.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                String lcName = name.toLowerCase();
                for(int i = 0; i < suffixes.length; i++) {
                    String suffix = suffixes[i];
                    if(lcName.endsWith("." + suffix)) return true;
                }
                return false;
            }
        });
        // Convert it to a list so we can sort it
        // Seems to be necessary on Linux
        List<File> sortedList = Arrays.asList(files);
        Collections.sort(sortedList, fileComparator);
        list.setEnabled(false);
        listModel.removeAllElements();
        for(File file : sortedList) {
            listModel.addElement(file);
        }
        list.validate();
        mainPane.validate();
        list.setEnabled(true);
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
        sb.append(
            "Image Browser allows you to choose a directory and display" + ls);
        sb.append(
            "images in that directory.  You can save the image as a" + ls);
        sb.append("new file and do Cut, Copy, and Paste.  Some image" + ls);
        sb.append("manipulation is available from the Image menu." + ls);
        sb.append("" + ls);
        sb.append("The image formats you can use will depend on whether" + ls);
        sb.append("Java Advanced Imaging (JAI and JAI ImageIO) are" + ls);
        sb.append(
            "installed.  You can see the available formats under the " + ls);
        sb.append("Info menu." + ls);
        sb.append("" + ls);
        sb.append("Accelerator keys:" + ls);
        sb.append("  Copy: Ctrl-C" + ls);
        sb.append("  Paste: Ctrl-V" + ls);
        sb.append("  Paste and Print: Ctrl-B" + ls);
        sb.append("  Print: Ctrl-P" + ls);
        sb.append("  Always Fit: Ctrl-F" + ls);
        sb.append("  Save: Ctrl-S" + ls);
        sb.append("  Restore: Ctrl-R" + ls);
        sb.append("" + ls);
        sb.append("Mouse:" + ls);
        sb.append("  Drag: Set rectangle" + ls);
        sb.append("  Double click: Remove rectangle" + ls);
        sb.append("  Shift-click: Zoom In" + ls);
        sb.append("  Ctrl-click: Zoom Out" + ls);
        sb.append("  Alt-click: Zoom Reset" + ls);
        Utils.infoMsg(sb.toString());
    }

    // Getters and setters

    /**
     * Toggles whether crop is enabled or not.
     * 
     * @param crop
     */
    public void toggleCrop() {
        if(imagePanel != null) {
            if(imagePanel.getMode() != Mode.CROP) {
                imagePanel.setMode(Mode.CROP);
            } else {
                imagePanel.setMode(Mode.NONE);
            }
        }
    }

    /**
     * Set whether crop is enabled or not.
     * 
     * @param crop
     */
    public void setCrop(boolean crop) {
        if(imagePanel != null) {
            if(crop) {
                if(imagePanel.getMode() != Mode.CROP) {
                    imagePanel.setMode(Mode.CROP);
                }
            } else {
                if(imagePanel.getMode() == Mode.CROP) {
                    imagePanel.setMode(Mode.NONE);
                }
            }
            if(controlPanelCropEnableCheckBox != null) {
                controlPanelCropEnableCheckBox.setSelected(getCrop());
            }
            if(controlPanelCropButton != null) {
                controlPanelCropButton.setEnabled(getCrop());
            }
            if(menuCropCrop != null) {
                menuCropCrop.setEnabled(getCrop());
            }
        }
    }

    /**
     * Return whether crop is enabled or not.
     */
    public boolean getCrop() {
        if(imagePanel != null) {
            return imagePanel.getMode() == Mode.CROP;
        } else {
            return false;
        }
    }

    /**
     * @return The value of controlPanelMode.
     */
    public ControlPanelMode getControlPanelMode() {
        return controlPanelMode;
    }

    /**
     * @param controlPanelMode The new value for controlPanelMode.
     */
    public void setControlPanelMode(ControlPanelMode controlPanelMode) {
        resetControlPanel(controlPanelMode);
    }

    /**
     * @return The value of imagePanel.
     */
    public ScrolledImagePanel getImagePanel() {
        return imagePanel;
    }

    /**
     * @param imagePanel the imagePanel to set
     */
    public void setImagePanel(ScrolledImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    /**
     * @return The value of imageModel.
     */
    public ImageModel getImageModel() {
        return imageModel;
    }

    /**
     * @return The value of VERSION_STRING.
     */
    public static String getVersionString() {
        return VERSION_STRING;
    }
    
    /**
     * @return The value of lastColorSetFile.
     */
    public File getLastColorSetFile() {
        return lastColorSetFile;
    }

    /**
     * @param lastColorSetFile The new value for lastColorSetFile.
     */
    public void setLastColorSetFile(File lastColorSetFile) {
        this.lastColorSetFile = lastColorSetFile;
    }

    /**
     * Main program.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            // Set window decorations
            JFrame.setDefaultLookAndFeelDecorated(true);

            // Set the native look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Make the job run in the AWT thread
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ImageBrowser app = new ImageBrowser();
                    // Make it exit when the window manager close button is
                    // clicked
                    app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    ImageUtils.setIconImageFromResource(app,
                        "/resources/ImageBrowser.32x32.gif");
                    app.pack();
                    app.setVisible(true);
                    app.setLocationRelativeTo(null);
                }
            });
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

}
