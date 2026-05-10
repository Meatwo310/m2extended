package m2extended.world.blocks.distribution;

import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;

public class DirectedRouter extends Block{
    public float speed = 8f;
    public int[] outputOffsets = {0, 1, -1};

    public DirectedRouter(String name){
        super(name);
        solid = false;
        underBullets = true;
        update = true;
        rotate = true;
        hasItems = true;
        itemCapacity = 1;
        group = BlockGroup.transportation;
        unloadable = false;
        noUpdateDisabled = true;
    }

    public DirectedRouter outputs(int... outputOffsets){
        this.outputOffsets = outputOffsets;
        return this;
    }

    public class DirectedRouterBuild extends Building{
        public Item lastItem;
        public float time;
        public int outputIndex;

        @Override
        public void updateTile(){
            if(lastItem == null && items.any()){
                lastItem = items.first();
            }

            if(lastItem != null){
                time += 1f / speed * delta();
                Building target = getTileTarget(lastItem, false);

                if(target != null && (time >= 1f || !(target.block instanceof Router || target.block.instantTransfer))){
                    getTileTarget(lastItem, true);
                    target.handleItem(this, lastItem);
                    items.remove(lastItem, 1);
                    lastItem = null;
                }
            }
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            return 0;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return source != null && team == source.team && lastItem == null && items.total() == 0 && source.relativeTo(tile) == rotation;
        }

        @Override
        public void handleItem(Building source, Item item){
            items.add(item, 1);
            lastItem = item;
            time = 0f;
        }

        @Override
        public int removeStack(Item item, int amount){
            int result = super.removeStack(item, amount);
            if(result != 0 && item == lastItem){
                lastItem = null;
            }
            return result;
        }

        public Building getTileTarget(Item item, boolean set){
            for(int i = 0; i < outputOffsets.length; i++){
                int index = (i + outputIndex) % outputOffsets.length;
                Building other = nearby(Mathf.mod(rotation + outputOffsets[index], 4));

                if(other != null && other.team == team && other.acceptItem(this, item)){
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
