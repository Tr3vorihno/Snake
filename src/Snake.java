public class Snake{
    private int x;
    private int y;
    public Snake(int x, int y){
        this.x = x;
        this.y = y;
    }
    public Snake(){// coordinate di partenza quindi serpente ad inizio gioco
        this.x = 240;
        this.y = 240;
    }
    public int getX(){return this.x;}
    public int getY(){return this.y;}
    public void setX(int x){this.x = x;}
    public void setY(int y){this.y = y;}
}