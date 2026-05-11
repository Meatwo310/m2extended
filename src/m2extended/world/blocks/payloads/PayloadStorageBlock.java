package m2extended.world.blocks.payloads;

import arc.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.ctype.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class PayloadStorageBlock extends PayloadBlock{
    public int payloadCapacity = 8;
    public float maxPayloadSize = 3f;
    public String fallbackRegion = "container";

    public PayloadStorageBlock(String name){
        super(name);

        size = 3;
        solid = true;
        rotate = true;
        outputsPayload = true;
        acceptsPayload = true;
        acceptsUnitPayloads = true;
        outputFacing = true;
        group = BlockGroup.payloads;
        canOverdrive = false;
    }

    @Override
    public void load(){
        super.load();

        if(!region.found()){
            region = Core.atlas.find(fallbackRegion);
        }

        if(!teamRegion.found()){
            teamRegion = Core.atlas.find(fallbackRegion + "-team");
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.payloadCapacity, payloadCapacity, StatUnit.none);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("payloads", (PayloadStorageBuild build) -> new Bar(
            () -> Core.bundle.format("bar.m2extended-payloads", build.payloadAmount(), payloadCapacity),
            () -> Pal.accent,
            () -> build.payloadAmount() / (float)payloadCapacity
        ));
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
    }

    public class PayloadStorageBuild extends PayloadBlockBuild<Payload>{
        public Seq<Payload> payloadQueue = new Seq<>();
        public PayloadSeq payloadCounts = new PayloadSeq();
        public boolean exporting;

        @Override
        public boolean acceptUnitPayload(Unit unit){
            return payload == null && unit.type.allowedInPayloads && unit.hitSize / tilesize <= maxPayloadSize && canStore(unit.type);
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return this.payload == null && payload.fits(maxPayloadSize) && canStore(payload.content());
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            exporting = false;
            super.handlePayload(source, payload);
        }

        public boolean canStore(UnlockableContent content){
            UnlockableContent current = currentContent();
            return payloadAmount() < payloadCapacity && (current == null || current == content);
        }

        public int payloadAmount(){
            return payloadQueue.size + (payload == null ? 0 : 1);
        }

        public UnlockableContent currentContent(){
            if(payload != null){
                return payload.content();
            }

            return payloadQueue.any() ? payloadQueue.first().content() : null;
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(payload != null){
                if(exporting){
                    moveOutPayload();
                }else if(moveInPayload()){
                    storePayload(payload);
                    payload = null;
                }
                return;
            }

            if(enabled && payloadQueue.any()){
                Payload next = payloadQueue.first();
                if(canOutput(next)){
                    payload = payloadQueue.remove(0);
                    exporting = true;
                    payVector.setZero();
                    payRotation = rotdeg();
                    rebuildPayloadCounts();
                    moveOutPayload();
                }
            }
        }

        public boolean canOutput(Payload payload){
            Building front = front();
            return front != null && front.team == team && (front.block.outputsPayload || front.block.acceptsPayload) && front.acceptPayload(this, payload);
        }

        public void storePayload(Payload payload){
            payloadQueue.add(payload);
            payloadCounts.add(payload.content());
        }

        public void rebuildPayloadCounts(){
            payloadCounts.clear();
            for(Payload queued : payloadQueue){
                payloadCounts.add(queued.content());
            }
        }

        public int countPayloads(Content content){
            int amount = 0;
            for(Payload queued : payloadQueue){
                if(queued.content() == content) amount++;
            }

            if(payload != null && payload.content() == content) amount++;
            return amount;
        }

        @Override
        public Payload takePayload(){
            if(payload != null){
                Payload taken = payload;
                payload = null;
                return taken;
            }

            if(payloadQueue.any()){
                Payload taken = payloadQueue.remove(0);
                rebuildPayloadCounts();
                return taken;
            }

            return null;
        }

        @Override
        public PayloadSeq getPayloads(){
            return payloadCounts;
        }

        public void drawStoredPayload(){
            if(payloadQueue.isEmpty()) return;

            Payload queued = payloadQueue.first();
            float previewSize = Math.min(queued.size(), size * tilesize);

            Draw.z(Layer.blockOver);
            Draw.rect(queued.icon(), x, y, previewSize, previewSize);
        }

        @Override
        public void onRemoved(){
            super.onRemoved();

            if(!carried){
                for(Payload queued : payloadQueue){
                    queued.set(x, y, rotdeg());
                    queued.dump();
                }
                payloadQueue.clear();
                payloadCounts.clear();
            }
        }

        @Override
        public void onDestroyed(){
            for(Payload queued : payloadQueue){
                queued.destroyed();
            }
            payloadQueue.clear();
            payloadCounts.clear();

            super.onDestroyed();
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            if(teamRegion.found()){
                Draw.color(team.color);
                Draw.rect(teamRegion, x, y);
                Draw.color();
            }

            if(payload == null){
                drawStoredPayload();
            }

            drawPayload();
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.payloadCount){
                return payloadAmount();
            }

            return super.sense(sensor);
        }

        @Override
        public double sense(Content content){
            if(content instanceof UnitType){
                return countPayloads(content);
            }

            if(content instanceof Block){
                return countPayloads(content);
            }

            return super.sense(content);
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.payloadType){
                return currentContent();
            }

            return super.senseObject(sensor);
        }

        @Override
        public byte version(){
            return 2;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(payloadQueue.size);
            for(Payload queued : payloadQueue){
                Payload.write(queued, write);
            }
            write.bool(exporting);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            payloadQueue.clear();
            payloadCounts.clear();

            if(revision >= 2){
                short amount = read.s();
                for(int i = 0; i < amount; i++){
                    Payload queued = Payload.read(read);
                    if(queued != null && queued.fits(maxPayloadSize) && canStore(queued.content())){
                        payloadQueue.add(queued);
                        payloadCounts.add(queued.content());
                    }
                }
                exporting = read.bool();
            }else if(revision >= 1){
                PayloadSeq legacyPayloads = new PayloadSeq();
                legacyPayloads.read(read);

                byte type = read.b();
                short id = read.s();
                UnlockableContent storedContent = type == -1 ? null : content.getByID(ContentType.all[type], id);
                exporting = read.bool();

                if(storedContent instanceof Block block){
                    for(int i = 0; i < legacyPayloads.get(block); i++){
                        Payload queued = new BuildPayload(block, team);
                        payloadQueue.add(queued);
                        payloadCounts.add(block);
                    }
                }else if(storedContent instanceof UnitType unitType){
                    for(int i = 0; i < legacyPayloads.get(unitType); i++){
                        Payload queued = new UnitPayload(unitType.create(team));
                        payloadQueue.add(queued);
                        payloadCounts.add(unitType);
                    }
                }
            }
        }
    }
}
