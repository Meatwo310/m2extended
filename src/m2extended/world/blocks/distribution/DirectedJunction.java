package m2extended.world.blocks.distribution;

import arc.math.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.*;

public class DirectedJunction extends Junction{
    public DirectedJunction(String name){
        super(name);
        rotate = true;
    }

    public class DirectedJunctionBuild extends JunctionBuild{
        @Override
        public boolean acceptItem(Building source, Item item){
            if(source == null){
                return false;
            }

            int relative = source.relativeTo(tile);

            if((relative != rotation && relative != rightDirection(rotation)) || !buffer.accepts(relative)){
                return false;
            }

            Building to = nearby(relative);
            return to != null && to.team == team;
        }
    }

    private int rightDirection(int direction){
        return Mathf.mod(direction - 1, 4);
    }
}
