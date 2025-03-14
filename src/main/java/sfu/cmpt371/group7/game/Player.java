package sfu.cmpt371.group7.game;

public class Player {

    private String team;
    private int x;
    private int y;

    public Player(String team, int x, int y){
        this.team = team;
        this.x = x;
        this.y = y;
    }

    public void setTeam(String team){
        this.team = team;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public String getTeam(){
        return team;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }
}