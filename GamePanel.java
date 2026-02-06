/*
 *  TODO: aggiungere gioco "infinito" quindi tasto di restart del gioco
 *        aggiungere punteggi e record con classifica tramite file
 */

import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Scanner;
import java.io.FileReader;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GamePanel extends JPanel implements ActionListener{


    //clock
    private Timer timer;
    private static int DELAY = 120; // velocita di refresh in ms

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
    private final static String FILE_END_GAME = "game_over.txt";
    private final static String FILE_MENU = "menu.txt";
    private final static String RIGA_NERA = "________________________________________";
    private Image offScreenImage;
    private Graphics offScreenGraphics;
    private static boolean FLIKKER_END_GAME = false; 
    private static int FLIKKER_TIMER = 0; 
    private static boolean GAME_STARTED = false;
    private Apple mela = new Apple(((int)(Math.random()*(SCREEN_WIDTH/DIM_GRID_LINES))*DIM_GRID_LINES),((int)(Math.random()*(SCREEN_HEIGHT/DIM_GRID_LINES))*DIM_GRID_LINES));
    private Snake[] player = new Snake[MAX_SIZE_SNAKE_VECTOR];
    private BufferedImage backgroundBuffer;  // CREO UN IMMAGINE DELLE SFONDO CHE POI CARICO UNA VOLTA SOLA NEL COSTRUTTORE
    private JButton playButton;
    private JButton title;
    private JButton scritta_finale;
    public GamePanel(){
        this.setLayout(null);
        timer = new Timer(DELAY, this);
        timer.start();

        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.BLACK);
        this.addKeyListener(new MyKeyAdapter());
        this.setDoubleBuffered(true);
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
        System.out.println("Setup playbuyttonme");
        setupPlayButton();
    }
    protected void paintComponent(Graphics g) {
        /*

            CREO L'IMMAGINA E POI LA CARICO IN UNA BOTTA SOLA
            QUESTO PER EVITARE DI SCRIVERE SVARIATE VOLTE AL SECONDO SULLO SCHERMO TUTTI I VARI QUADRATINI
            CAUSAVA IL FLIKKER DELLA FINESTRA

        */
        // CREO IMMAGINE SE NON È ANCORA STATA SETTATA
        if (offScreenImage == null || offScreenImage.getWidth(null) != SCREEN_WIDTH || offScreenImage.getHeight(null) != SCREEN_HEIGHT) {
            offScreenImage = createImage(SCREEN_WIDTH, SCREEN_HEIGHT);
            offScreenGraphics = offScreenImage.getGraphics();
        }
        if(GAME_STARTED){
            if(collisione){// CONTROLLO SE IN QUELL'ISTANTE VADO A COLLIDERE CON QUALCOSA
                drawEndGame();
            }else{
                drawGame();
            }
        }else{// menu iniziale
            drawStartingMenu();
            //setupPlayButton();
        }
        
        // DISEGNO EFFETTIVAMENTE L'IMMAGINE A SCHERMO IN UN COLPO SOLO
        // LO METTO FUORI DALL'IF PERCHÈ offScreenGraphics È GLOBALE E MI PERMETTE DI SFRUTTARLO ANCHE PER LA CASISTICA GAME OVER
        g.drawImage(offScreenImage, 0, 0, this);
        //setupPlayButton();
    }
    public void reset(){
        //playButton.setVisible(true);
        //title.setVisible(true);
        GAME_STARTED = false;
        DIM_EFFETTIVA_SNAKE = 1;
        player[0] = new Snake();
        direction = 'w';
        mela_mangiata = true;
        collisione = false;
        scritta_finale.setVisible(false);
        playButton.setVisible(false); // Nascondi quello che esiste già
        title.setVisible(false);      // Nascondi quello che esiste già
        timer.restart();
        
        repaint();
    }
    public void drawEndGame(){
        
        scritta_finale.setVisible(true);

        Color scritta_end_game;
        if(FLIKKER_TIMER % 5 == 0){
            FLIKKER_END_GAME = !FLIKKER_END_GAME;
            FLIKKER_TIMER = 0;
        } 

        if(FLIKKER_END_GAME) scritta_end_game = new Color(244, 0, 0);
        else{scritta_end_game = new Color(244, 78, 63);}
            
        String temp = "";
        int dim = contaRigheFile();
        int offset = ((SCREEN_HEIGHT/DIM_GRID_LINES)-dim)/2;
        
        try{
            Scanner s = new Scanner(new FileReader(FILE_END_GAME));
            for(int i = 0; i < (SCREEN_HEIGHT/DIM_GRID_LINES) ; i++){

                if(s.hasNextLine() && i>=offset)temp = s.nextLine();
                else{temp = RIGA_NERA;}
                
                for(int j = 0; j<(SCREEN_WIDTH/DIM_GRID_LINES) ; j++){
                     if(temp.charAt(j) == 'X'){
                        // disegno quadrato rosso di game over
                        offScreenGraphics.setColor(scritta_end_game);
                    }else{
                        offScreenGraphics.setColor(Color.BLACK);
                        // disegno quadrato nero di sfondo
                    }
                    offScreenGraphics.fillRect(j * DIM_GRID_LINES, i * DIM_GRID_LINES, DIM_GRID_LINES, DIM_GRID_LINES);
                }
            }
            s.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        FLIKKER_TIMER++;
    }
    public void drawGame(){
        playButton.setVisible(false);
        title.setVisible(false);
        scritta_finale.setVisible(false);
        offScreenGraphics.clearRect(0,0,SCREEN_WIDTH,SCREEN_HEIGHT);// PULISCO L'IMMAGINE
        offScreenGraphics.drawImage(backgroundBuffer,0,0,null);// CARICO L'IMMAGINE CON LO SFONFO A SCACCHI

        for(int i = 0; i < DIM_EFFETTIVA_SNAKE; i++) {// DISEGNO IL SERPENTE NEL SUO STATO ATTUALE
            offScreenGraphics.setColor(Color.BLACK);
            offScreenGraphics.fillRect(player[i].getX(),player[i].getY(),DIM_GRID_LINES,DIM_GRID_LINES);
            if(i == 0) offScreenGraphics.setColor(SNAKE_HEAD_COLOR);
            else{offScreenGraphics.setColor(SNAKE_BODY_COLOR);}
            offScreenGraphics.fillRect(player[i].getX()+PADDING_SNAKE, player[i].getY()+PADDING_SNAKE, MAX_SIZE_SNAKE_DRAW, MAX_SIZE_SNAKE_DRAW);
        }
        if(mela_mangiata){// SE LA MELA È STATA MANGIATA CREO UNA NUOVA MELA CON COORDINATE RANDOM
            mela = new Apple(((int)(Math.random()*(SCREEN_WIDTH/DIM_GRID_LINES))*DIM_GRID_LINES),((int)(Math.random()*(SCREEN_HEIGHT/DIM_GRID_LINES))*DIM_GRID_LINES));
            mela_mangiata = false;
        }
        // DISEGNO LA MELA A SCHERMO
        offScreenGraphics.setColor(Color.BLACK);
        offScreenGraphics.fillRect(mela.getX(),mela.getY(),DIM_GRID_LINES,DIM_GRID_LINES);
        offScreenGraphics.setColor(APPLE_COLOR);
        offScreenGraphics.fillRect(mela.getX()+PADDING_APPLE,mela.getY()+PADDING_APPLE,MAX_SIZE_APPLE_DRAW,MAX_SIZE_APPLE_DRAW);
    }
    public void drawStartingMenu(){
        title.setVisible(true);
        playButton.setVisible(true);
        offScreenGraphics.setColor(FIELD_COLOR_2);
        for(int i = 0; i < (SCREEN_HEIGHT/DIM_GRID_LINES) ; i++){
            for(int j = 0; j<(SCREEN_WIDTH/DIM_GRID_LINES) ; j++){       
                offScreenGraphics.fillRect(j * DIM_GRID_LINES, i * DIM_GRID_LINES, DIM_GRID_LINES, DIM_GRID_LINES);
            }
        }
        
    }
    public int contaRigheFile(){
        String temp = "";
        int i = 0;
        try{
            Scanner s = new Scanner(new FileReader(FILE_END_GAME));
            while(s.hasNextLine()){
                temp = s.nextLine();
                i++;
            }
            s.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return i;
    }
    public void actionPerformed(ActionEvent e) {// codice eseguito automaticamente ogni DELAY millisecondi   
        if(GAME_STARTED){
            hasEaten();
            checkCollisions();
            move(); 
        }
        repaint(); 
        Toolkit.getDefaultToolkit().sync();//<--- comando muy importante per la fluidità
    }

    public void move(){
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
    private void setupPlayButton() {
        
        ImageIcon btnIcon = new ImageIcon("play.png"); // Assicurati che il file si chiami così
        ImageIcon serpe = new ImageIcon("snake.png");
        int btnWidth_snake = 400; 
        int btnHeight_snake = 200;
        playButton = new JButton(btnIcon);
        title = new JButton(serpe);
        title.setBounds(150, 70, 300, 80);
        title.setBorderPainted(false);      // Niente bordo
        title.setContentAreaFilled(false);  // Niente sfondo grigio
        title.setFocusPainted(false);       // Niente rettangolino di selezione
        title.setOpaque(false);
        title.setBorderPainted(false);
        title.setEnabled(true);
        title.setFocusable(false);

        int x = (SCREEN_WIDTH - 230) / 2;
        int y = SCREEN_HEIGHT - 150;            // 150 pixel dal fondo
        playButton.setBounds(x, y, 230, 100);
        playButton.setBorderPainted(false);      // Niente bordo
        playButton.setContentAreaFilled(false);  // Niente sfondo grigio
        playButton.setFocusPainted(false);       // Niente rettangolino di selezione
        playButton.setOpaque(false);
        playButton.setBorderPainted(false);
        playButton.setFocusable(false);
        // 6. Cosa succede quando clicchi
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GAME_STARTED = true;
                //this.requestFocusInWindow();
                playButton.setVisible(false);
                title.setVisible(false);
            }
        });
        scritta_finale = new JButton();
        scritta_finale.setBounds(0,0,SCREEN_HEIGHT,SCREEN_WIDTH);
        scritta_finale.setOpaque(false);            
        scritta_finale.setContentAreaFilled(false); 
        scritta_finale.setBorderPainted(false);     
        scritta_finale.setFocusable(false);
        scritta_finale.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scritta_finale.setVisible(false);
                //timer.stop();
                reset();
            }
        });

        scritta_finale.setVisible(false);
        playButton.setVisible(false);
        title.setVisible(false);

        this.add(scritta_finale);
        this.add(playButton);
        this.add(title);
    }
    
}