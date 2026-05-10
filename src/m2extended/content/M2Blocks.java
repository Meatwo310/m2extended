package m2extended.content;

import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.meta.*;
import m2extended.world.blocks.distribution.*;

import mindustry.content.TechTree.*;
import static mindustry.type.ItemStack.*;

public class M2Blocks{
    public static Block leadArmoredConveyor, siliconArmoredConveyor;
    public static Block directedRouter, rightDirectedRouter, leftDirectedRouter, directedJunction;
    public static Block leadPlatedConduit, siliconPlatedConduit;

    public static void load(){
        leadArmoredConveyor = new ArmoredConveyor("lead-armored-conveyor"){{
            requirements(Category.distribution, with(Items.copper, 1, Items.lead, 1));
            health = 45;
            speed = 0.03f;
            displayedSpeed = 4.2f;
            buildCostMultiplier = 2f;
            researchCost = with(Items.copper, 5, Items.lead, 20);
        }};

        siliconArmoredConveyor = new ArmoredConveyor("silicon-armored-conveyor"){{
            requirements(Category.distribution, with(Items.copper, 1, Items.lead, 1, Items.titanium, 1, Items.silicon, 1));
            health = 65;
            speed = 0.08f;
            displayedSpeed = 11f;
            researchCost = with(Items.copper, 80, Items.lead, 80, Items.titanium, 80, Items.silicon, 80);
        }};

        directedRouter = new DirectedRouter("directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1));
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10);
        }};

        rightDirectedRouter = new DirectedRouter("right-directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1));
            outputs(0, -1);
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10);
        }};

        leftDirectedRouter = new DirectedRouter("left-directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1));
            outputs(0, 1);
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10);
        }};

        directedJunction = new DirectedJunction("directed-junction"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1));
            speed = 26;
            capacity = 6;
            health = 30;
            buildCostMultiplier = 6f;
            researchCost = with(Items.copper, 30, Items.lead, 10);
        }};

        leadPlatedConduit = new ArmoredConduit("lead-plated-conduit"){{
            requirements(Category.liquid, with(Items.metaglass, 1, Items.lead, 1));
            liquidCapacity = 20f;
            health = 45;
            explosivenessScale = 0.5f;
            flammabilityScale = 0.5f;
            buildCostMultiplier = 2f;
            researchCost = with(Items.metaglass, 20, Items.lead, 20);
        }};

        siliconPlatedConduit = new ArmoredConduit("silicon-plated-conduit"){{
            requirements(Category.liquid, with(Items.titanium, 2, Items.metaglass, 1, Items.silicon, 1));
            liquidCapacity = 40f;
            liquidPressure = 1.025f;
            health = 90;
            explosivenessScale = 0.4f;
            flammabilityScale = 0.4f;
            researchCost = with(Items.titanium, 80, Items.metaglass, 80, Items.silicon, 80);
        }};

        loadTechTree();
    }

    private static void loadTechTree(){
        addTechNode(Blocks.conveyor, leadArmoredConveyor, Seq.with(new SectorComplete(SectorPresets.groundZero)));
        addTechNode(Blocks.titaniumConveyor, siliconArmoredConveyor, null);
        addTechNode(Blocks.router, directedRouter, null);
        addTechNode(directedRouter, rightDirectedRouter, null);
        addTechNode(directedRouter, leftDirectedRouter, null);
        addTechNode(Blocks.junction, directedJunction, null);
        addTechNode(Blocks.conduit, leadPlatedConduit, null);
        addTechNode(Blocks.pulseConduit, siliconPlatedConduit, null);
    }

    private static void addTechNode(UnlockableContent parent, UnlockableContent content, Seq<Objective> objectives){
        TechTree.TechNode node = new TechTree.TechNode(parent.techNode, content, content.researchRequirements());

        if(objectives != null){
            node.objectives.addAll(objectives);
        }
    }
}
