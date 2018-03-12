package net.kenevans.imagemodel.colorset;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.kenevans.imagemodel.ImageBrowser;
import net.kenevans.imagemodel.colorset.MMCQ.CMap;
import net.kenevans.imagemodel.colorset.MMCQ.VBox;
import net.kenevans.imagemodel.utils.Utils;

/**
 * ColorSetDialog is a dialog to set the Preferences for HMSViewer. It only
 * returns after Cancel. It can save the values to the preference store or set
 * them in the viewer. In either case it remains visible.
 * 
 * @author Kenneth Evans, Jr.
 */
/**
 * ColorSetDialog
 * 
 * @author Kenneth Evans, Jr.
 */
public class ColorSetDialog extends JDialog
{
    private static final long serialVersionUID = 1L;
    public static final String LS = System.getProperty("line.separator");
    private static final int DIR_COLS = 40;
    private static final int ACO_INDEX = 0;
    private static final int COLORS_INDEX = 1;
    private static final int GIMP_INDEX = 2;
    private static final int DEFAULT_QUALITY = 10;
    private static final int DEFAULT_NCOLORS = 24;
    private static String[] outputTypeStrings = {"Photoshop .aco",
        "Painter .colors", "Gimp .gpl"};

    private ImageBrowser browser;
    private BufferedImage image;

    JTextField fileNameText;
    JCheckBox ignoreWhiteCheck;
    JCheckBox acoCheck;
    JCheckBox colorsCheck;
    JTextField nColorsText;
    JTextField qualityText;
    JComboBox<String> outputFileTypeCombo;

    /**
     * Constructor
     */
    public ColorSetDialog(ImageBrowser browser, BufferedImage image) {
        super();
        this.image = image;
        this.browser = browser;
        init();
        this.setLocationRelativeTo(browser);
        // Set defaults
        File initialFile = browser.getLastColorSetFile();
        outputFileTypeCombo.setSelectedIndex(ACO_INDEX);
        if(initialFile != null) {
            fileNameText.setText(initialFile.getPath());
            String ext = Utils.getExtension(initialFile);
            if(ext != null) {
                if(ext.equalsIgnoreCase("aco")) {
                    outputFileTypeCombo.setSelectedIndex(ACO_INDEX);
                } else if(ext.equalsIgnoreCase("colors")) {
                    outputFileTypeCombo.setSelectedIndex(COLORS_INDEX);
                } else if(ext.equalsIgnoreCase("gpl")) {
                    outputFileTypeCombo.setSelectedIndex(GIMP_INDEX);
                }
            }
        }
        nColorsText.setText(String.format("%d", DEFAULT_NCOLORS));
        qualityText.setText(String.format("%d", DEFAULT_QUALITY));
        ignoreWhiteCheck.setSelected(true);
    }

    /**
     * This method initializes this dialog
     * 
     * @return void
     */
    private void init() {
        this.setTitle("Create Color Set");
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new GridBagLayout());

        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(2, 2, 2, 2);
        gbcDefault.anchor = GridBagConstraints.WEST;
        gbcDefault.fill = GridBagConstraints.NONE;
        GridBagConstraints gbc = null;
        int gridy = -1;

        // File Group ///////////////////////////////////////////////////////
        JPanel fileGroup = new JPanel();
        fileGroup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("File"),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        gridy++;
        fileGroup.setLayout(new GridBagLayout());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        contentPane.add(fileGroup, gbc);

        // Default directory 1
        JLabel label = new JLabel("Output File:");
        label.setToolTipText("Name of the output file.");
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        fileGroup.add(label, gbc);

        // File JPanel holds the filename and browse button
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new GridBagLayout());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 1;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        fileGroup.add(filePanel, gbc);

        fileNameText = new JTextField(DIR_COLS);
        fileNameText.setToolTipText(label.getToolTipText());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        filePanel.add(fileNameText, gbc);

        JButton button = new JButton();
        button.setText("Browse");
        button.setToolTipText("Choose the file.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                if(fileNameText == null) {
                    return;
                }
                String initialFileName = fileNameText.getText();
                String outputFileName = browse(initialFileName);
                if(outputFileName != null) {
                    fileNameText.setText(outputFileName);
                }
            }
        });
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 1;
        filePanel.add(button);

        // Options Group ////////////////////////////////////////////////////
        JPanel optionsGroup = new JPanel();
        optionsGroup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Options"),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        gridy++;
        optionsGroup.setLayout(new GridBagLayout());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.weightx = 100;
        contentPane.add(optionsGroup, gbc);

        // Output file type
        String toolTip = "The type of file to gererate.";
        label = new JLabel("Output Type:");
        label.setToolTipText(toolTip);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 1;
        optionsGroup.add(label, gbc);

        outputFileTypeCombo = new JComboBox<String>(outputTypeStrings);
        outputFileTypeCombo.setToolTipText("label.getText()");
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 2;
        optionsGroup.add(outputFileTypeCombo, gbc);

        // NColors
        toolTip = "Number of entries in the color set.";
        label = new JLabel("N Colors:");
        label.setToolTipText(toolTip);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 1;
        optionsGroup.add(label, gbc);

        nColorsText = new JTextField(5);
        nColorsText.setToolTipText(label.getText());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 2;
        // gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        optionsGroup.add(nColorsText, gbc);

        // Quality
        toolTip = "0 is the highest quality settings. 10 is the default."
            + "There is a trade-off between quality and speed. The bigger the"
            + "number, the faster a color will be returned but the greater"
            + "the likelihood that it will not be the visually most dominant"
            + "color.";
        label = new JLabel("Quality:");
        label.setToolTipText(toolTip);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 1;
        optionsGroup.add(label, gbc);

        qualityText = new JTextField(5);
        qualityText.setToolTipText(label.getText());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 2;
        // gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        optionsGroup.add(qualityText, gbc);

        // Ignore White
        ignoreWhiteCheck = new JCheckBox("Ignore White");
        ignoreWhiteCheck.setToolTipText("Whether to ignore white.");
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 1;
        optionsGroup.add(ignoreWhiteCheck, gbc);

        // Button panel /////////////////////////////////////////////////////
        gridy++;
        JPanel buttonPanel = new JPanel();
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = gridy;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(buttonPanel, gbc);

        button = new JButton();
        button.setText("Create");
        button.setToolTipText("Create the color set file.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                if(createOutputFile()) {
                    ColorSetDialog.this.setVisible(false);
                }
                ;
            }
        });
        buttonPanel.add(button);

        button = new JButton();
        button.setText("Cancel");
        button.setToolTipText("Close the dialog and do nothing.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                ColorSetDialog.this.setVisible(false);
            }
        });
        buttonPanel.add(button);

        pack();
    }

    /**
     * Brings up a JFileChooser to choose a file.
     */
    private String browse(String initialFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter acoFiles = new FileNameExtensionFilter(
            "Photoshop *.aco", "aco");
        FileNameExtensionFilter colorFiles = new FileNameExtensionFilter(
            "Painter *.colors", "colors");
        FileNameExtensionFilter gimpFiles = new FileNameExtensionFilter(
            "Gimp *.gpl", "palette");
        chooser.addChoosableFileFilter(acoFiles);
        chooser.addChoosableFileFilter(colorFiles);
        chooser.addChoosableFileFilter(gimpFiles);
        if(outputFileTypeCombo.getSelectedIndex() == ACO_INDEX) {
            chooser.setFileFilter(acoFiles);
        } else if(outputFileTypeCombo.getSelectedIndex() == COLORS_INDEX) {
            chooser.setFileFilter(colorFiles);
        } else if(outputFileTypeCombo.getSelectedIndex() == GIMP_INDEX) {
            chooser.setFileFilter(gimpFiles);
        }
        if(initialFileName != null) {
            File file = new File(initialFileName);
            chooser.setSelectedFile(file);
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Process the file
            File file = chooser.getSelectedFile();
            String fileName = file.getPath();
            String ext = Utils.getExtension(file);
            if(ext != null) {
                if(ext.equalsIgnoreCase("aco")) {
                    outputFileTypeCombo.setSelectedIndex(ACO_INDEX);
                } else if(ext.equalsIgnoreCase("colors")) {
                    outputFileTypeCombo.setSelectedIndex(COLORS_INDEX);
                } else if(ext.equalsIgnoreCase("gpl")) {
                    outputFileTypeCombo.setSelectedIndex(GIMP_INDEX);
                }
            }
            return fileName;
        } else {
            return null;
        }
    }

    /**
     * Creates the output file.
     * 
     * @return If successful or not.
     */
    private boolean createOutputFile() {
        try {
            String fileName = fileNameText.getText();
            File file = new File(fileName);
            if(file.exists()) {
                int res = JOptionPane.showConfirmDialog(this,
                    "File exists:" + LS + file.getPath() + LS
                        + "OK to overwrite?",
                    "File Exists", JOptionPane.OK_CANCEL_OPTION);
                if(res != JOptionPane.OK_OPTION) return false;
            }
            int nColors = Integer.parseInt(nColorsText.getText());
            int quality = Integer.parseInt(qualityText.getText());
            int outputType = outputFileTypeCombo.getSelectedIndex();
            boolean ignoreWhite = ignoreWhiteCheck.isSelected();

            // Get the Cmap
            // Need to use nColors + 1 here
            CMap cmap = ColorThief.getColorMap(image, nColors + 1, quality,
                ignoreWhite);
            if(cmap == null) return false;

            // DEBUG
            if(cmap.vboxes.size() != nColors) {
                int res = JOptionPane.showConfirmDialog(this,
                    "Did not get correct nColors:" + LS + "  nColors=" + nColors
                        + " CMap VBox Size=" + cmap.vboxes.size(),
                    "Warning", JOptionPane.OK_CANCEL_OPTION);
                if(res != JOptionPane.OK_OPTION) return false;
            }

            browser.setLastColorSetFile(file);
            if(outputType == ACO_INDEX) {
                writeACOFile(image, cmap, file);
                return true;
            } else if(outputType == COLORS_INDEX) {
                writeColorsFile(image, cmap, file);
                return true;
            } else if(outputType == GIMP_INDEX) {
                writeGimpPaletteFile(image, cmap, file);
                return true;
            } else {
                Utils.errMsg("Invalid output type: " + outputType);
                return false;
            }
        } catch(Exception ex) {
            Utils.excMsg("Failed to create output file", ex);
            return false;
        }
    }

    /**
     * Writes an Photoshop ACO file.
     * 
     * @param image The image to use.
     * @param cmap The CMap to use.
     * @param file The output File to write.
     * @throws IOException
     */
    private void writeACOFile(BufferedImage image, CMap cmap, File file)
        throws IOException {
        // Write the data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        // Version
        dos.writeShort(1);
        // Number of colors
        int nColors = cmap.vboxes.size();
        dos.writeShort(nColors);
        // Process the colors
        int[] rgb;
        int i = 0;
        for(VBox vbox : cmap.vboxes) {
            rgb = vbox.avg(false);
            // DEBUG
            System.out.printf("%3d %02x %02x %02x" + LS, i, rgb[0], rgb[1],
                rgb[2]);
            i++;
            // Color space (0=RGB)
            dos.writeShort(0);
            // Red
            dos.writeShort(rgb[0] << 8);
            // Green
            dos.writeShort(rgb[1] << 8);
            // Blue
            dos.writeShort(rgb[2] << 8);
            // Unused
            dos.writeShort(0);
        }
        dos.close();

        // Write it to the file (This CTOR does not append)
        OutputStream os = new FileOutputStream(file);
        baos.writeTo(os);
        if(os != null) os.close();

        Utils.infoMsg("Wrote: " + file.getPath() + LS + "nColors=" + nColors
            + LS + "File size=" + file.length());
    }

    /**
     * Writes a Painter COLORS file.
     * 
     * @param image The image to use.
     * @param cmap The CMap to use.
     * @param file The output File to write.
     * @throws IOException
     */
    private void writeColorsFile(BufferedImage image, CMap cmap, File file)
        throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(file));
        // Number of colors
        int nColors = cmap.vboxes.size();
        // Process the colors
        int[] rgb;
        int i = 0;
        for(VBox vbox : cmap.vboxes) {
            rgb = vbox.avg(false);
            // DEBUG
            System.out.printf("%3d %02x %02x %02x" + LS, i, rgb[0], rgb[1],
                rgb[2]);
            i++;
            out.printf("R:%d, G:%d, B:%d HV:0.00, SV:0.00, VV:0.00 %s" + LS,
                rgb[0], rgb[1], rgb[2], "Color " + i);
        }
        out.close();

        Utils.infoMsg("Wrote: " + file.getPath() + LS + "nColors=" + nColors
            + LS + "File size=" + file.length());
    }

    /**
     * Writes a Gimp palette file.
     * 
     * @param image The image to use.
     * @param cmap The CMap to use.
     * @param file The output File to write.
     * @throws IOException
     */
    private void writeGimpPaletteFile(BufferedImage image, CMap cmap, File file)
        throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(file));
        String ext = Utils.getExtension(file);
        String name = file.getName();
        if(ext != null) {
            int index = name.lastIndexOf('.');
            if(index != -1) {
                name = name.substring(0, index);
            }
        }
        // Number of colors
        int nColors = cmap.vboxes.size();
        // Write the header
        out.println("GIMP Palette");
        out.println("Name: " + name);
        out.println("#");
        // Process the colors
        int[] rgb;
        int i = 0;
        for(VBox vbox : cmap.vboxes) {
            rgb = vbox.avg(false);
            // DEBUG
            System.out.printf("%3d %02x %02x %02x" + LS, i, rgb[0], rgb[1],
                rgb[2]);
            i++;
            out.printf("%3d %3d %3d #%02x%02x%02x" + LS, rgb[0], rgb[1], rgb[2],
                rgb[0], rgb[1], rgb[2]);
        }
        out.close();

        Utils.infoMsg("Wrote: " + file.getPath() + LS + "nColors=" + nColors
            + LS + "File size=" + file.length());
    }

}
