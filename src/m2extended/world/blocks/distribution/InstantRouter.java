package m2extended.world.blocks.distribution;

import arc.math.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class InstantRouter extends Block{
    public int[] outputOffsets = {0, 1, -1};

    public InstantRouter(String name){
        super(name);
        solid = false;
        underBullets = true;
        update = false;
        destructible = true;
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

    public class InstantRouterBuild extends Building{
        public int outputIndex;

        @Override
        public boolean acceptItem(Building source, Item item){
            return source != null && source.team == team && getTileTarget(item, source, false) != null;
        }

        @Override
        public void handleItem(Building source, Item item){
            Building target = getTileTarget(item, source, true);

            if(target != null){
                target.handleItem(this, item);
            }
        }

        public Building getTileTarget(Item item, Building source, boolean set){
            int from = source.relativeTo(tile);
            if(from == -1) return null;

            for(int i = 0; i < outputOffsets.length; i++){
                int index = (i + outputIndex) % outputOffsets.length;
                int direction = Mathf.mod(from + outputOffsets[index], 4);
                Building other = nearby(direction);

                if(other != null && other.team == team && !(source.block.instantTransfer && other.block.instantTransfer) && other.acceptItem(this, item)){
                    if(set){
                        outputIndex = (index + 1) % outputOffsets.length;
                    }
                    return other;
                }
            }

            return null;
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(outputIndex);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            outputIndex = read.s();
        }
    }
}
