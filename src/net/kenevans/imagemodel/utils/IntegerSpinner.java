package net.kenevans.imagemodel.utils;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * IntegerSpinner implements a JSpinner with a JSpinner.NumberEditor and some
 * convenience functions. Further control can be obtained through the associated
 * JFormattedTextField. The default SpinnerNumberModel uses 0, 0,
 * Integer.MAX_VALUE, 1). The default DecimalFormat is "0".
 * 
 * 
 * @author Kenneth Evans, Jr.
 */
public class IntegerSpinner extends JSpinner
{
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_NUMBER_FORMAT = "0";
    private static final int DEFAULT_N_COLUMNS = 5;
    private static final int DEFAULT_HORIZONTAL_ALIGNMENT = JTextField.LEFT;
    private JFormattedTextField textField = null;

    /**
     * IntegerSpinner constructor using the default SpinnerNumberModel and
     * format. The default model uses (0, 0, Integer.MAX_VALUE, 1).
     */
    public IntegerSpinner() {
        this(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1),
            DEFAULT_NUMBER_FORMAT);
    }

    /**
     * IntegerSpinner constructor using the default SpinnerNumberModel. The
     * default model uses (0, 0, Integer.MAX_VALUE, 1).
     * 
     * @param format is the DecimalFormat String used to define the format.
     */
    public IntegerSpinner(String format) {
        this(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1), format);
    }

    /**
     * IntegerSpinner constructor using the default format.
     * 
     * @param model is the SpinnerNumberModel.
     */
    public IntegerSpinner(SpinnerNumberModel model) {
        this(model, DEFAULT_NUMBER_FORMAT);
    }

    /**
     * IntegerSpinner constructor
     * 
     * @param model is the SpinnerNumberModel.
     * @param format is the DecimalFormat String used to define the format.
     */
    public IntegerSpinner(SpinnerNumberModel model, String format) {
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(this, format);
        this.setEditor(editor);
        this.setModel(model);
        textField = editor.getTextField();
        textField.setColumns(DEFAULT_N_COLUMNS);
        textField.setHorizontalAlignment(DEFAULT_HORIZONTAL_ALIGNMENT);
    }

    /**
     * Convenience method to set the value from an int.
     * 
     * @param number
     */
    public void setText(int number) {
        this.setValue(number);
    }

    /**
     * Convenience method to set the value from a String.
     * 
     * @param text
     */
    public void setText(String text) {
        this.setValue(text);
    }

    /**
     * Convenience method to set the value from an int.
     * 
     * @param number
     */
    public void setValue(int number) {
        Integer intVal = Integer.valueOf(number);
        super.setValue(intVal);
    }

    /**
     * Convenience method to set the value from a String.
     * 
     * @param text
     */
    public void setValue(String text) {
        super.setValue(Integer.getInteger(text));
    }

    /**
     * Convenience method to get the value as an int.
     * 
     * @return
     */
    public int getIntValue() {
        return ((Integer)this.getValue()).intValue();
    }

    /**
     * Convenience method to set the horizontal alignment. The default is
     * JTextField.LEFT.
     * 
     * @param alignment
     */
    public void setHorizontalAlignment(int alignment) {
        textField.setHorizontalAlignment(alignment);
    }

    /**
     * Convenience method to set the number of columns. The default is 5.
     * 
     * @param nColumns
     */
    public void setColumns(int nColumns) {
        textField.setColumns(nColumns);
    }

    /**
     * Method to get the JFormattedText field, which can be used for further
     * customization of the control.
     * 
     * @return Returns the textField.
     */
    public JFormattedTextField getTextField() {
        return textField;
    }

}
