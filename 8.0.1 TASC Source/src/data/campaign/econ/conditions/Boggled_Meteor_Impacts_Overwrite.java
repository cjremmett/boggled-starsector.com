
package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CircularFleetOrbit;
import com.fs.starfarer.campaign.CircularOrbit;
import com.fs.starfarer.campaign.CircularOrbitPointDown;
import com.fs.starfarer.campaign.CircularOrbitWithSpin;
import com.fs.starfarer.combat.entities.terrain.Planet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import data.campaign.econ.boggledTools;

public class Boggled_Meteor_Impacts_Overwrite extends BaseHazardCondition
{
    public Boggled_Meteor_Impacts_Overwrite() { }

    public void apply(String id)
    {
        super.apply(id);

        if(this.market.hasIndustry(Industries.PLANETARYSHIELD) && this.market.getIndustry(Industries.PLANETARYSHIELD).isFunctional())
        {
            this.market.suppressCondition("meteor_impacts");
        }
    }

    public void unapply(String id)
    {
        super.unapply(id);

        if(!this.market.hasIndustry(Industries.PLANETARYSHIELD) || !this.market.getIndustry(Industries.PLANETARYSHIELD).isFunctional())
        {
            this.market.unsuppressCondition("meteor_impacts");
        }
    }
}
