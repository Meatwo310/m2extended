package m2extended.world.blocks.payloads;

import arc.*;
import arc.graphics.g2d.*;
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
        public PayloadSeq payloads = new PayloadSeq();
        public UnlockableContent storedContent;
        public boolean exporting;

        @Override
        public boolean acceptUnitPayload(Unit unit){
            return unit.type.allowedInPayloads && unit.hitSize / tilesize <= maxPayloadSize && canStore(unit.type);
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return payload.fits(maxPayloadSize) && canStore(payload.content());
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
            return payloads.total() + (payload == null ? 0 : 1);
        }

        public UnlockableContent currentContent(){
            if(storedContent != null){
                return storedContent;
            }

            return payload == null ? null : payload.content();
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

            if(enabled && payloads.any()){
                Payload next = createPayload(storedContent);
                if(next != null && canOutput(next)){
                    removeStored(storedContent);
                    payload = next;
                    exporting = true;
                    payVector.setZero();
                    payRotation = rotdeg();
                    moveOutPayload();
                }
            }
        }

        public boolean canOutput(Payload payload){
            Building front = front();
            return front != null && front.team == team && (front.block.outputsPayload || front.block.acceptsPayload) && front.acceptPayload(this, payload);
        }

        public void storePayload(Payload payload){
            storedContent = payload.content();
            payloads.add(storedContent);
        }

        public void removeStored(UnlockableContent content){
            payloads.remove(content);
            if(payloads.total() <= 0){
                payloads.clear();
                storedContent = null;
            }
        }

        public Payload createPayload(UnlockableContent content){
            if(content instanceof Block){
                return new BuildPayload((Block)content, team);
            }else if(content instanceof UnitType){
                return new UnitPayload(((UnitType)content).create(team));
            }

            return null;
        }

        @Override
        public Payload takePayload(){
            if(payload != null){
                Payload taken = payload;
                payload = null;
                return taken;
            }

            if(storedContent != null && payloads.any()){
                Payload taken = createPayload(storedContent);
                removeStored(storedContent);
                return taken;
            }

            return null;
        }

        @Override
        public PayloadSeq getPayloads(){
            return payloads;
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            if(teamRegion.found()){
                Draw.color(team.color);
                Draw.rect(teamRegion, x, y);
                Draw.color();
            }

            UnlockableContent content = currentContent();
            if(content != null && payload == null){
                Draw.z(Layer.blockOver);
                Draw.rect(content.fullIcon, x, y, 24f, 24f);
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
                return payloads.get((UnitType)content) + (payload instanceof UnitPayload && ((UnitPayload)payload).unit.type == content ? 1 : 0);
            }

            if(content instanceof Block){
                return payloads.get((Block)content) + (payload instanceof BuildPayload && ((BuildPayload)payload).block() == content ? 1 : 0);
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
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            payloads.write(write);
            write.b(storedContent == null ? -1 : storedContent.getContentType().ordinal());
            write.s(storedContent == null ? -1 : storedContent.id);
            write.bool(exporting);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            payloads.read(read);

            if(revision >= 1){
                byte type = read.b();
                short id = read.s();
                storedContent = type == -1 ? null : content.getByID(ContentType.all[type], id);
                exporting = read.bool();
            }

            if(storedContent == null){
                payloads.clear();
            }else{
                payloads.removeAll(content -> content != storedContent);
            }
        }
    }
}
