package m2extended.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class InstantDirectedRouter extends Block{
    public int[] outputOffsets = {0, 1, -1};
    public TextureRegion topRegion;

    public InstantDirectedRouter(String name){
        super(name);
        solid = false;
        underBullets = true;
        update = false;
        destructible = true;
        rotate = true;
        hasItems = true;
        instantTransfer = true;
        itemCapacity = 0;
        group = BlockGroup.transportation;
        unloadable = false;
        canOverdrive = false;
    }

    public InstantDirectedRouter outputs(int... outputOffsets){
        this.outputOffsets = outputOffsets;
        return this;
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

    public class InstantDirectedRouterBuild extends Building{
        public int outputIndex;

        @Override
        public void draw(){
            Draw.rect(region, x, y);
            Draw.rect(topRegion, x, y, rotdeg());
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            return 0;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return source != null && team == source.team && source.relativeTo(tile) == rotation && getTileTarget(item, source, false) != null;
        }

        @Override
        public void handleItem(Building source, Item item){
            Building target = getTileTarget(item, source, true);

            if(target != null){
                target.handleItem(this, item);
            }
        }

        public Building getTileTarget(Item item, Building source, boolean set){
            for(int i = 0; i < outputOffsets.length; i++){
                int index = (i + outputIndex) % outputOffsets.length;
                Building other = nearby(Mathf.mod(rotation + outputOffsets[index], 4));

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
            items.clear();
        }
    }
}
