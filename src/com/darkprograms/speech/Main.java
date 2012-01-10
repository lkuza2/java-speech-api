/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.darkprograms.speech;

import com.darkprograms.speech.gui.MainGUI;
import javax.swing.UIManager;

/**
 *
 * @author User
 */
public class Main {

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            MainGUI gui = new MainGUI();
            gui.setVisible(true);
            gui.setLocationRelativeTo(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
