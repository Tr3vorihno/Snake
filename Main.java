import javax.swing.*;
public class Main{
    public static void main(String args[]){
        
        JFrame frame = new JFrame();
        GamePanel g = new GamePanel();
        frame.add(g);

        frame.setTitle("Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Chiude il processo quando premi la X
        frame.setResizable(false); // blocca la dimensione
        frame.pack(); // Adatta la finestra alla grandezza del pannello interno
        frame.setVisible(true); // Rende la finestra visibile
        frame.setLocationRelativeTo(null); // La centra a metà schermo
        
    }
}