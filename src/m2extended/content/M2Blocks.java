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
    public static Block leadPlatedConduit, siliconPlatedConduit;
    public static Block siliconInstantRouter, siliconInstantJunction;
    public static Block leadDirectedRouter, leadRightDirectedRouter, leadLeftDirectedRouter, leadDirectedJunction;
    public static Block siliconDirectedRouter, siliconRightDirectedRouter, siliconLeftDirectedRouter, siliconDirectedJunction;

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

        siliconInstantRouter = new InstantRouter("silicon-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 2, Items.silicon, 1));
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 20, Items.silicon, 20);
        }};

        siliconInstantJunction = new InstantJunction("silicon-junction"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 2, Items.silicon, 1));
            health = 30;
            buildCostMultiplier = 6f;
            researchCost = with(Items.copper, 30, Items.lead, 20, Items.silicon, 20);
        }};

        leadDirectedRouter = new DirectedRouter("lead-directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1));
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10);
        }};

        leadRightDirectedRouter = new DirectedRouter("lead-right-directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1));
            outputs(0, -1);
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10);
        }};

        leadLeftDirectedRouter = new DirectedRouter("lead-left-directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1));
            outputs(0, 1);
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10);
        }};

        leadDirectedJunction = new DirectedJunction("lead-directed-junction"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1));
            speed = 26;
            capacity = 6;
            health = 30;
            buildCostMultiplier = 6f;
            researchCost = with(Items.copper, 30, Items.lead, 10);
        }};

        siliconDirectedRouter = new InstantDirectedRouter("silicon-directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1, Items.silicon, 1));
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10, Items.silicon, 20);
        }};

        siliconRightDirectedRouter = new InstantDirectedRouter("silicon-right-directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1, Items.silicon, 1));
            outputs(0, -1);
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10, Items.silicon, 20);
        }};

        siliconLeftDirectedRouter = new InstantDirectedRouter("silicon-left-directed-router"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1, Items.silicon, 1));
            outputs(0, 1);
            buildCostMultiplier = 4f;
            researchCost = with(Items.copper, 30, Items.lead, 10, Items.silicon, 20);
        }};

        siliconDirectedJunction = new InstantDirectedJunction("silicon-directed-junction"){{
            requirements(Category.distribution, with(Items.copper, 3, Items.lead, 1, Items.silicon, 1));
            speed = 26;
            capacity = 6;
            health = 30;
            buildCostMultiplier = 6f;
            researchCost = with(Items.copper, 30, Items.lead, 10, Items.silicon, 20);
        }};

        loadTechTree();
    }

    private static void loadTechTree(){
        addTechNode(Blocks.conveyor, leadArmoredConveyor, Seq.with(new SectorComplete(SectorPresets.groundZero)));
        addTechNode(Blocks.titaniumConveyor, siliconArmoredConveyor, null);
        addTechNode(Blocks.router, leadDirectedRouter, null);
        addTechNode(leadDirectedRouter, leadRightDirectedRouter, null);
        addTechNode(leadDirectedRouter, leadLeftDirectedRouter, null);
        addTechNode(Blocks.junction, leadDirectedJunction, null);
        addTechNode(Blocks.router, siliconInstantRouter, null);
        addTechNode(Blocks.junction, siliconInstantJunction, null);
        addTechNode(leadDirectedRouter, siliconDirectedRouter, null);
        addTechNode(leadRightDirectedRouter, siliconRightDirectedRouter, null);
        addTechNode(leadLeftDirectedRouter, siliconLeftDirectedRouter, null);
        addTechNode(leadDirectedJunction, siliconDirectedJunction, null);
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
