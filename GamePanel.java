import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel implements ActionListener{


    //clock
    private Timer timer;
    private static int DELAY = 100; // velocita di refresh in ms

    //impostazioni campo da gioco
    private final static Color FIELD_COLOR_1 = new Color(138, 201, 38);//chiaro
    private final static Color FIELD_COLOR_2 = new Color(125, 184, 30);//scuro
    
    // impostazioni schermo
    private final static int SCREEN_HEIGHT = 600;
    private final static int SCREEN_WIDTH = 600;
    private final static int DIM_GRID_LINES = 30;//==================================================================================
    
    //impostazioni del serpente
    private final static int MAX_SIZE_SNAKE_VECTOR = 255;
    private static int DIM_EFFETTIVA_SNAKE = 1;
    private final static Color SNAKE_HEAD_COLOR = new Color(227, 170, 24);// colore testa (primo elemento array snake)
    private final static Color SNAKE_BODY_COLOR = new Color(232, 245, 102);// colore del resto del corpo
    private final static int MAX_SIZE_SNAKE_DRAW = 22;//==================================================================================
    private final static int PADDING_SNAKE = (DIM_GRID_LINES - MAX_SIZE_SNAKE_DRAW)/2;
    private char direction = 'w';
    //impostazioni MELA
    private final static Color APPLE_COLOR = new Color(197, 34, 51);
    private final static int MAX_SIZE_APPLE_DRAW = 22;
    private final static int PADDING_APPLE = (DIM_GRID_LINES - MAX_SIZE_APPLE_DRAW)/2;
    //impostazioni partita
    private boolean mela_mangiata = true;
    private boolean collisione = false;

    private Apple mela;
    private Snake[] player = new Snake[MAX_SIZE_SNAKE_VECTOR];
    private BufferedImage backgroundBuffer;  // CREO UN IMMAGINE DELLE SFONDO CHE POI CARICO UNA VOLTA SOLA NEL COSTRUTTORE

    public GamePanel(){

        timer = new Timer(DELAY, this);
        timer.start();

        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.WHITE);
        this.addKeyListener(new MyKeyAdapter());
        this.setFocusable(true); // Importante per leggere i tasti dopo!

        backgroundBuffer = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = backgroundBuffer.createGraphics();

        for(int i = 0; i < (SCREEN_HEIGHT/DIM_GRID_LINES) ; i++){
            for(int j = 0; j<(SCREEN_WIDTH/DIM_GRID_LINES) ; j++){
                if((i+j)%2==0){
                    g2d.setColor(FIELD_COLOR_1);
                }else{
                    g2d.setColor(FIELD_COLOR_2);
                }
                g2d.fillRect(j * DIM_GRID_LINES, i * DIM_GRID_LINES, DIM_GRID_LINES, DIM_GRID_LINES);
            }
        }
        g2d.dispose();
        player[0] = new Snake(); // init del serpente tramite costruttore di default
        //player[1] = new Snake(player[0].getX(),player[0].getY()+DIM_GRID_LINES);
    }
    protected void paintComponent(Graphics g) {
        if(collisione){
            drawEndGame(g);
        }else{
            super.paintComponent(g); // pulisce il pannello
            g.drawImage(backgroundBuffer, 0, 0, null);

            for(int i = 0; i < DIM_EFFETTIVA_SNAKE; i++) {
                g.setColor(Color.BLACK);
                g.fillRect(player[i].getX(),player[i].getY(),DIM_GRID_LINES,DIM_GRID_LINES);
                if(i == 0) g.setColor(SNAKE_HEAD_COLOR);
                else{g.setColor(SNAKE_BODY_COLOR);}
                g.fillRect(player[i].getX()+PADDING_SNAKE, player[i].getY()+PADDING_SNAKE, MAX_SIZE_SNAKE_DRAW, MAX_SIZE_SNAKE_DRAW);
            }

            if(mela_mangiata){
                mela = new Apple(((int)(Math.random()*(SCREEN_WIDTH/DIM_GRID_LINES))*DIM_GRID_LINES),((int)(Math.random()*(SCREEN_HEIGHT/DIM_GRID_LINES))*DIM_GRID_LINES));
                mela_mangiata = false;
            } 
            g.setColor(Color.BLACK);
            g.fillRect(mela.getX(),mela.getY(),DIM_GRID_LINES,DIM_GRID_LINES);
            g.setColor(APPLE_COLOR);
            g.fillRect(mela.getX()+PADDING_APPLE,mela.getY()+PADDING_APPLE,MAX_SIZE_APPLE_DRAW,MAX_SIZE_APPLE_DRAW);
        }
    }
    public void drawEndGame(Graphics g){
        for(int i = 0; i < (SCREEN_HEIGHT/DIM_GRID_LINES) ; i++){
            for(int j = 0; j<(SCREEN_WIDTH/DIM_GRID_LINES) ; j++){
                if((i+j)%2==0){
                    g.setColor(Color.RED);
                }else{
                    g.setColor(Color.BLACK);
                }
                g.fillRect(j * DIM_GRID_LINES, i * DIM_GRID_LINES, DIM_GRID_LINES, DIM_GRID_LINES);
            }
        }
    }
    public void actionPerformed(ActionEvent e) {
        // Questo codice viene eseguito automaticamente ogni 100ms
        
        // A. Fai muovere il serpente (aggiorni le coordinate x,y)     
        hasEaten();
        checkCollisions();
        move(); 

        // B. Controlli se ha mangiato o sbattuto
        //
        
        repaint(); 
        Toolkit.getDefaultToolkit().sync();//<--- comando muy importante per la fluidità
    }

    public void move(){// devo sapere la direzione
        int x  = 0, y = 0;
        switch(direction){
            case 'W':{//nord
                y = (-1*DIM_GRID_LINES);
                break;
            }
            case 'S':{//sud
                y = DIM_GRID_LINES;
                break;
            }
            case 'D' :{//est
                x = DIM_GRID_LINES;
                break;
            }
            case 'A':{//ovest
                x = (-1*DIM_GRID_LINES);
                break;
            }
        }
        if(mela_mangiata){
            player[DIM_EFFETTIVA_SNAKE] = new Snake();
            DIM_EFFETTIVA_SNAKE++;
        }
        for(int i = DIM_EFFETTIVA_SNAKE - 1; i > 0 ; i--){
            player[i].setX(player[i-1].getX());
            player[i].setY(player[i-1].getY());
        }
        player[0].setX(player[0].getX() + x);
        player[0].setY(player[0].getY() + y);
    }

    public class MyKeyAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            
            switch(e.getKeyCode()) {
                case KeyEvent.VK_A:
                    if(direction != 'D') {
                        direction = 'A';
                    }
                    break;
                case KeyEvent.VK_D:
                    if(direction != 'A') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_W:
                    if(direction != 'S') {
                        direction = 'W';
                    }
                    break;
                case KeyEvent.VK_S:
                    if(direction != 'W') {
                        direction = 'S';
                    }
                    break;
            }
        }
    }
    public void hasEaten(){if(player[0].getX() == mela.getX() && player[0].getY() == mela.getY()) mela_mangiata = true;}
    public void checkCollisions(){
        if(player[0].getX() == 0 || player[0].getY() == 0 || player[0].getX() == SCREEN_WIDTH || player[0].getY() == SCREEN_HEIGHT) collisione = true;
        for(int i = 1 ; i < DIM_EFFETTIVA_SNAKE ; i++){
            if(player[0].getX() == player[i].getX() && player[0].getY() == player[i].getY()) collisione = true;
        }
    }
}