package m2extended.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class InstantDirectedJunction extends Block{
    public TextureRegion topRegion;

    public InstantDirectedJunction(String name){
        super(name);
        update = false;
        destructible = true;
        solid = false;
        underBullets = true;
        instantTransfer = true;
        group = BlockGroup.transportation;
        unloadable = false;
        itemCapacity = 0;
        canOverdrive = false;
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

    @Override
    public boolean outputsItems(){
        return true;
    }

    public class InstantDirectedJunctionBuild extends Building{
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

            return source.team == team && getTileTarget(item, source) != null;
        }

        @Override
        public void handleItem(Building source, Item item){
            Building target = getTileTarget(item, source);

            if(target != null){
                target.handleItem(this, item);
            }
        }

        public Building getTileTarget(Item item, Building source){
            int relative = source.relativeTo(tile);

            if(relative != rotation && relative != rightDirection(rotation)){
                return null;
            }

            Building to = nearby(relative);
            return to != null && to.team == team && !(source.block.instantTransfer && to.block.instantTransfer) && to.acceptItem(this, item) ? to : null;
        }

    }

    private int rightDirection(int direction){
        return Mathf.mod(direction - 1, 4);
    }
}
