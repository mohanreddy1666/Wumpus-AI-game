package wumpusworld;

import java.util.ArrayList;

public class MyAgent implements Agent
{
    private World w;
    private int goal_x = 0;
    private int goal_y = 0;
    private boolean has_goal = false;
    private boolean if_shoot = false;
    private int pits = 0;
    private int[] dead_end = {0,0};
    private int wumpus_status = NOT_FOUND;
    private int[] wumpus_pos = {0,0};
    private int[] arrow_goal = {0,0};
    private int[] position2 = {0,0};
    private  boolean catch_bug = false;


    /*wumpus status*/
    public static final int NOT_FOUND = 0;
    public static final int FOUND = 1;
    public static final int DEAD = 2;




    /**
     * Creates a new instance of your solver agent.
     *
     * @param world Current world state
     */
    public MyAgent(World world)
    {
        w = world;
    }


    /**
     * Asks your solver agent to execute an action.
     */

    public void doAction()
    {

        int cX = w.getPlayerX();
        int cY = w.getPlayerY();

        if (w.hasGlitter(cX, cY))
        {
            w.doAction(World.A_GRAB);
            return;
        }

        if (w.isInPit())
        {
            w.doAction(World.A_CLIMB);
            return;
        }

        if (w.hasBreeze(cX, cY))
        {
            System.out.println("I am in a Breeze");
        }
        if (w.hasStench(cX, cY))
        {
            System.out.println("I am in a Stench");
        }
        if (w.hasPit(cX, cY))
        {
            System.out.println("I am in a Pit");
        }
        if (w.getDirection() == World.DIR_RIGHT)
        {
            System.out.println("I am facing Right");
        }
        if (w.getDirection() == World.DIR_LEFT)
        {
            System.out.println("I am facing Left");
        }
        if (w.getDirection() == World.DIR_UP)
        {
            System.out.println("I am facing Up");
        }
        if (w.getDirection() == World.DIR_DOWN)
        {
            System.out.println("I am facing Down");
        }

        if(cX==goal_x && cY==goal_y){
            has_goal=false;
        }

        if(!has_goal){

            NB2 nb = new NB2(w);

            if(wumpus_status==NOT_FOUND){
                if(nb.get_wumpus_pos(wumpus_pos)){
                    wumpus_status = FOUND;   /*Estimate the position of the wumpus*/
                }
            }
            else if (wumpus_status==FOUND){
                nb.update_wumpus_pos(wumpus_pos);
            }
            int[] goal = new int[2];
            if_shoot = get_goal(goal,wumpus_status,nb);
            has_goal = true;
            goal_x = goal[0];
            goal_y = goal[1];
            if(if_shoot){
                arrow_goal[0]=goal_x;
                arrow_goal[1]=goal_y;
            }
        }

        position2[0]=0;
        position2[1]=1;
        move_to_goal(cX,cY);

    }

    /**
    *Get the possible safe cell for making the next move
    * @param position
    * wumpus_status
    * nb
     */
    public boolean get_goal(int[] position,int wumpus_status,NB2 nb){

        /*update PIT_PROB*/
        nb.npits();
        NB2.PIT_PROB = nb.get_pit_prob();


        int index;
        boolean trigger_shoot=false;
        boolean safezone=false;
        double pit_probability = 0.2;

        ArrayList<double[]> probability =    nb.get_probability(1);

        if(wumpus_status==NOT_FOUND){

            double min_wumpus=1;
            int n = -1;
            while(n<0)
            {
                for(int i = 0; i< probability.size(); i++)
                {

                    double prob_wumpus = probability.get(i)[1];
                    double prob_pit = probability.get(i)[0];

                    if(prob_wumpus<=min_wumpus && prob_pit<pit_probability) {
                        if (prob_wumpus == min_wumpus && n>=0) {
                            if(nb.get_distance(nb.neighnours.get(i), nb.neighnours.get(n))) {
                                continue;
                            }
                        }
                        min_wumpus=prob_wumpus;
                        n=i;
                    }
                }

                pit_probability += NB2.OFFSET;
            }

            index = n;
            if(probability.get(index)[1]> NB2.RISK && w.hasArrow()){
                trigger_shoot = true;
            }
        }



        else{

            double min_pit=1;
            int n=-1;

            while(!safezone)
            {
                for(int i = 0; i< probability.size(); i++){

                    double p = probability.get(i)[0];
                    if(p<=min_pit) {
                        if (p == min_pit && n>=0 && nb.get_distance(nb.neighnours.get(i), nb.neighnours.get(n))) {
                            continue;
                        }
                        min_pit=p;
                        n=i;
                    }
                }

                if(wumpus_status!=MyAgent.DEAD){
                    if(wumpus_pos[0]== nb.neighnours.get(n)[0] && wumpus_pos[1]== nb.neighnours.get(n)[1])
                    {
                        if(w.hasArrow()){
                            trigger_shoot = true;
                            safezone = true;
                        }
                        else{
                            if(probability.size()>1){
                                probability.remove(probability.get(n));
                                min_pit=1;
                            }
                            else safezone = true;

                        }
                    }
                    else safezone=true;

                }
                else{
                    safezone=true;
                }
            }

            index = n;

        }

        position[0] = nb.neighnours.get(index)[0];
        position[1] = nb.neighnours.get(index)[1];

        return trigger_shoot;

    }


    /**
    * Calculate the distance
    */

    public double calculate_distance(int newX, int newY){
        int currentX = w.getPlayerX();
        int currentY = w.getPlayerY();
        double distance = Math.abs(currentX-newX)+Math.abs(currentY-newY);
        return distance;
    }

    /*
    *Change the direction to reach the goal
    */
    public void move_to_goal(int x,int y)
    {
        boolean is_adjacent = false;
        if(calculate_distance(goal_x,goal_y)==1){
            is_adjacent = true;
        }
        int dir = w.getDirection();

            if(x<goal_x){
                if(!w.isUnknown(x+1,y)&&!w.hasPit(x+1,y)&&!(x+1== position2[0])||is_adjacent){
                    change_direction(dir,World.DIR_RIGHT);
                    return;
                }
            }

            if(x>goal_x){
                if(!w.isUnknown(x-1,y)&&!w.hasPit(x-1,y)&&!(x-1== position2[0])||is_adjacent){
                    change_direction(dir,World.DIR_LEFT);
                    return;
                }
            }

            if(y<goal_y){
                if(!w.isUnknown(x,y+1)&&!w.hasPit(x,y+1)&&!(y+1== position2[1])||is_adjacent){
                    change_direction(dir,World.DIR_UP);
                    return;
                }
            }

            if(y>goal_y){
                if(!w.isUnknown(x,y-1)&&!w.hasPit(x,y-1)&&!(y-1== position2[1])||is_adjacent){
                    change_direction(dir,World.DIR_DOWN);
                    return;
                }
            }


        int pitdir = -1;

        if(w.isVisited(x+1,y) && w.hasPit(x+1,y)){
            pitdir=w.DIR_RIGHT;
        }
        if(w.isVisited(x-1,y) && w.hasPit(x-1,y)){
            pitdir=w.DIR_LEFT;
        }
        if(w.isVisited(x,y+1) && w.hasPit(x,y+1)){
            pitdir=w.DIR_UP;
        }
        if(w.isVisited(x,y-1) && w.hasPit(x,y-1)){
            pitdir=w.DIR_DOWN;
        }
        else if(w.isVisited(x,y-1) && !w.hasPit(x,y-1)){
            pitdir = -1;
        }


        if( pitdir >= 0){
            position2[0]=x;
            position2[1]=y;
            change_direction(dir,pitdir);
            return;
        }

        check_new_goal(x,y, false);


        move_to_goal(w.getPlayerX(),w.getPlayerY());
    }

    public void check_new_goal(int x,int y,boolean has_new_goal){
        if(x != goal_x){
            for(int i=1; i<=w.getSize(); i++){
                boolean has_path = true;
                if(x>goal_x){
                    for(int j=x; j>=goal_x; j--){
                        if(w.isUnknown(j,i)){
                            has_path = false;
                            break;
                        }
                    }
                }

                else{
                    for(int k=x; k<=goal_x; k++){
                        if(w.isUnknown(k,i)){
                            has_path = false;
                            break;
                        }
                    }
                }

                if(has_path){
                    goal_y = i;
                    has_new_goal = true;
                    break;
                }
            }
        }

        else if(y != goal_y){
            boolean has_path = true;
            for(int xx=1; xx<=w.getSize(); xx++){
                if(y>goal_y){
                    for(int yy=y; yy>=goal_y; yy--){
                        if(w.isUnknown(xx,yy)){
                            has_path = false;
                            break;
                        }
                    }
                }

                else{
                    for(int yy=y; yy<=goal_y; y++){
                        if(w.isUnknown(xx,yy)){
                            has_path = false;
                            break;
                        }
                    }
                }

                if(has_path){
                    goal_x = xx;
                    has_new_goal = true;
                    break;
                }
            }
        }
        else System.out.println("");
        if(!has_new_goal){
            change_goal();
        }
    }


    private void change_direction(int currentDir, int goalDir)
    {
        int dir = currentDir-goalDir;

        if(w.isInPit()){
            w.doAction(World.A_CLIMB);
        }
        if(dir==0){

            if(if_shoot&&calculate_distance(arrow_goal[0],arrow_goal[1])==1){
                w.doAction(World.A_SHOOT);
                if_shoot=false;
                if(!w.wumpusAlive()){
                    wumpus_status = DEAD;
                }
            }
            else {
                w.doAction(World.A_MOVE);
            }
        }
        else if(dir == -1 ||dir == 3) {
            w.doAction(World.A_TURN_RIGHT);
        }
        else  {
            w.doAction(World.A_TURN_LEFT);
        }

    }


    private  void change_goal(){
        if(!w.isUnknown(goal_x+1,goal_y) && w.isValidPosition(goal_x+1,goal_y)){
            goal_x += 1;
        }

        if(!w.isUnknown(goal_x-1,goal_y) && w.isValidPosition(goal_x-1,goal_y)){
           goal_x -= 1;
        }

        if(!w.isUnknown(goal_x,goal_y+1) && w.isValidPosition(goal_x,goal_y+1)){ ;
          goal_y += 1;
        }

        if(!w.isUnknown(goal_x,goal_y-1) && w.isValidPosition(goal_x,goal_y-1)){
           goal_y -= 1;
        }
    }




}

