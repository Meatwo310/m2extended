package m2extended.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

public class DirectedJunction extends Junction{
    public TextureRegion topRegion;

    public DirectedJunction(String name){
        super(name);
        rotate = true;
    }

    @Override
    public void load(){
        super.load();
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    public class DirectedJunctionBuild extends JunctionBuild{
        @Override
        public void draw(){
            Draw.rect(region, x, y);
            Draw.rect(topRegion, x, y, rotdeg());
        }

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
        return arc.math.Mathf.mod(direction - 1, 4);
    }
}
