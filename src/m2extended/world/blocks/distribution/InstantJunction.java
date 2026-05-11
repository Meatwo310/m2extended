package m2extended.world.blocks.distribution;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class InstantJunction extends Block{
    public InstantJunction(String name){
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
    }

    @Override
    public boolean outputsItems(){
        return true;
    }

    public class InstantJunctionBuild extends Building{
        @Override
        public boolean acceptItem(Building source, Item item){
            return source != null && source.team == team && getTileTarget(item, source) != null;
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
            if(relative == -1) return null;

            Building to = nearby(relative);
            return to != null && to.team == team && !(source.block.instantTransfer && to.block.instantTransfer) && to.acceptItem(this, item) ? to : null;
        }
    }
}
