
package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
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

public class Cramped_Quarters extends BaseHazardCondition implements MarketImmigrationModifier
{
    public Cramped_Quarters() { }

    public void advance(float amount)
    {
        super.advance(amount);
    }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming)
    {
        incoming.getWeight().modifyFlat(this.getModId(), this.getImmigrationBonus(), Misc.ucFirst(this.condition.getName().toLowerCase()));
    }

    private int getStationCrampedThreshold(MarketAPI market)
    {
        return Global.getSettings().getInt("boggledStationCrampedQuartersSizeGrowthReductionStarts") + boggledTools.getNumberOfStationExpansions(market);
    }

    protected float getImmigrationBonus()
    {
        MarketAPI market = this.market;
        int stationCrampedThreshold = getStationCrampedThreshold(market);

        if (market.getSize() >= stationCrampedThreshold + 3)
        {
            return -200.0F;
        }
        else if (market.getSize() >= stationCrampedThreshold + 2)
        {
            return -100.0F;
        }
        else if (market.getSize() >= stationCrampedThreshold + 1)
        {
            return -50.0F;
        }
        else if (market.getSize() >= stationCrampedThreshold)
        {
            return -25.0F;
        }
        else
        {
            return 0.0F;
        }
    }

    public void apply(String id)
    {
        super.apply(id);

        if(this.market.isPlayerOwned() || this.market.getFactionId().equals(Global.getSector().getPlayerFaction().getId()))
        {
            if(this.market.getTariff().getBaseValue() == 0.0f && this.market.getTariff().getModifiedValue() == 0.0f)
            {
                this.market.getTariff().modifyFlat("base_tariff_for_station", 0.30f);
            }
        }

        if(Global.getSettings().getBoolean("boggledStationCrampedQuartersEnabled"))
        {
            // Market growth modifier
            this.market.addTransientImmigrationModifier(this);
        }

        if(Global.getSettings().getInt("boggledStationHazardRatingModifier") != 0)
        {
            // Market hazard modifier
            float hazard = (float)Global.getSettings().getInt("boggledStationHazardRatingModifier") / 100.0F;
            this.market.getHazard().modifyFlat(id, hazard, "Base station hazard");
        }

        if(Global.getSettings().getInt("boggledStationAccessibilityBoost") != 0)
        {
            // Accessibility boost
            float access = (float)Global.getSettings().getInt("boggledStationAccessibilityBoost") / 100.0F;
            this.market.getAccessibilityMod().modifyFlat(this.getModId(), access, "Space station");
        }
    }

    public void unapply(String id)
    {
        super.unapply(id);

        this.market.getTariff().unmodifyFlat("base_tariff_for_station");

        if(Global.getSettings().getBoolean("boggledStationCrampedQuartersEnabled"))
        {
            this.market.removeTransientImmigrationModifier(this);
        }

        if(Global.getSettings().getInt("boggledStationHazardRatingModifier") != 0)
        {
            this.market.getHazard().unmodifyFlat(id);
        }

        if(Global.getSettings().getInt("boggledStationAccessibilityBoost") != 0)
        {
            this.market.getAccessibilityMod().unmodifyFlat("Space station");
        }
    }

    public Map<String, String> getTokenReplacements() { return super.getTokenReplacements(); }

    public boolean showIcon()
    {
        if(Global.getSettings().getBoolean("boggledStationCrampedQuartersEnabled"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded)
    {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("Population growth reduction at this station begins at market size %s and gets worse if the station continues to grow further beyond that limit.", 10.0F, Misc.getHighlightColor(), new String[]{getStationCrampedThreshold(this.market) + ""});

        if(Global.getSettings().getBoolean("boggledStationCrampedQuartersPlayerCanPayToIncreaseStationSize"))
        {
            tooltip.addPara("Stations can be expanded to increase the maximum number of residents. Number of times this station has been expanded: %s", 10.0F, Misc.getHighlightColor(), new String[]{boggledTools.getNumberOfStationExpansions(this.market) + ""});
        }

        if(Global.getSettings().getBoolean("boggledStationCrampedQuartersPlayerCanPayToIncreaseStationSize") && Global.getSettings().getBoolean("boggledStationProgressiveIncreaseInCostsToExpandStation"))
        {
            tooltip.addPara("Station expansions become more progressively more expensive as the size of the station grows. Each new expansion is twice the cost of the previous one.", 10.0F, Misc.getHighlightColor(), new String[]{boggledTools.getNumberOfStationExpansions(this.market) + ""});
        }
    }
}
