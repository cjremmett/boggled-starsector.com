package data.campaign.econ.industries;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.String;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.CoreCampaignPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoreScript;
import com.fs.starfarer.api.impl.campaign.events.CoreEventProbabilityManager;
import com.fs.starfarer.api.impl.campaign.fleets.DisposableLuddicPathFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.DisposablePirateFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.MercFleetManagerV2;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.*;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;
import com.fs.starfarer.api.impl.campaign.econ.CommRelayCondition;

import javax.swing.*;

public class Boggled_CHAMELEON extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    public static float IMPROVE_STABILITY_BONUS = 1f;

    private int daysWithoutShortageDeciv = 0;
    private int lastDayCheckedDeciv = 0;
    public static int requiredDaysToRemoveDeciv = 200;

    private int daysWithoutShortageRogue = 0;
    private int lastDayCheckedRogue = 0;
    public static int requiredDaysToRemoveRogue = 200;

    public static float UPKEEP_MULT = 0.75F;
    public static int DEMAND_REDUCTION = 1;

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        if(this.market.hasCondition("decivilized_subpop") && this.isFunctional())
        {
            CampaignClockAPI clock = Global.getSector().getClock();

            boolean shortage = false;
            if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
            {
                Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
                if(deficit.two != 0)
                {
                    shortage = true;
                }
            }

            if(clock.getDay() != this.lastDayCheckedDeciv && !shortage)
            {
                this.daysWithoutShortageDeciv++;
                this.lastDayCheckedDeciv = clock.getDay();

                if(this.daysWithoutShortageDeciv >= requiredDaysToRemoveDeciv)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Decivilized subpopulation on " + market.getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Eradicated");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                    }

                    if(this.market.hasCondition("decivilized_subpop"))
                    {
                        this.market.removeCondition("decivilized_subpop");
                    }

                    boggledTools.surveyAll(market);
                    boggledTools.refreshSupplyAndDemand(market);
                    boggledTools.refreshAquacultureAndFarming(market);
                }
            }
        }

        if(this.market.hasCondition("rogue_ai_core") && this.isFunctional())
        {
            CampaignClockAPI clock = Global.getSector().getClock();

            boolean shortage = false;
            if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
            {
                Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
                if(deficit.two != 0)
                {
                    shortage = true;
                }
            }

            if(clock.getDay() != this.lastDayCheckedRogue && !shortage)
            {
                this.daysWithoutShortageRogue++;
                this.lastDayCheckedRogue = clock.getDay();

                if(this.daysWithoutShortageRogue >= requiredDaysToRemoveRogue)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Rogue AI core on " + market.getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Terminated");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                    }

                    if(this.market.hasCondition("rogue_ai_core"))
                    {
                        this.market.removeCondition("rogue_ai_core");
                    }

                    boggledTools.surveyAll(market);
                    boggledTools.refreshSupplyAndDemand(market);
                    boggledTools.refreshAquacultureAndFarming(market);
                }
            }
        }
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            int size = this.market.getSize();
            this.demand("domain_artifacts", size);
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(Global.getSettings().getBoolean("boggledDomainTechContentEnabled") && Global.getSettings().getBoolean("boggledCHAMELEONEnabled"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    @Override
    public String getUnavailableReason()
    {
        return "Error in getUnavailableReason() in the CHAMELEON structure. Please tell Boggled about this on the forums.";
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        this.daysWithoutShortageDeciv = 0;
        this.lastDayCheckedDeciv = 0;
        this.daysWithoutShortageRogue = 0;
        this.lastDayCheckedRogue = 0;

        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        // Inserts pacification status after description
        if(this.market.hasCondition("decivilized_subpop") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            // 200 days to recivilize; divide daysWithoutShortage by 2 to get the percent
            int percentComplete = this.daysWithoutShortageDeciv / 2;

            // Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("Approximately %s of the decivilized subpopulation on " + this.market.getName() + " has been eradicated.", opad, highlight, new String[]{percentComplete + "%"});
        }

        // Inserts rogue AI core removal status after description
        if(this.market.hasCondition("rogue_ai_core") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            // 200 days to remove rogue core; divide daysWithoutShortage by 2 to get the percent
            int percentComplete = this.daysWithoutShortageRogue / 2;

            // Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("An investigation into the whereabouts of the rogue AI core on " + this.market.getName() + " is approximately %s complete.", opad, highlight, new String[]{percentComplete + "%"});
        }

        if(this.isDisrupted() && (this.market.hasCondition("decivilized_subpop") || this.market.hasCondition("rogue_ai_core")) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            Color bad = Misc.getNegativeHighlightColor();
            tooltip.addPara("Progress is stalled while CHAMELEON is disrupted.", bad, opad);
        }
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return true;
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        boolean shortage = false;
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        if(shortage && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            float opad = 10.0F;
            Color bad = Misc.getNegativeHighlightColor();

            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            if(deficit.two != 0)
            {
                tooltip.addPara("CHAMELEON is inactive due to a shortage of Domain-era artifacts.", bad, opad);
            }
        }
    }

    @Override
    public float getPatherInterest()
    {
        boolean shortage = false;
        if(Global.getSettings().getBoolean("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        if(isFunctional() && !isBuilding() && this.aiCoreId != null && this.aiCoreId.equals("alpha_core") && !shortage)
        {
            // Effectively ends any Pather activity because Pather interest will be negative.
            // Simply removing the Pather cells condition probably causes bugs - this is easier.
            return -1000f;
        }
        else
        {
            return 10.0F;
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Pather cells on " + this.market.getName() + " are eliminated.", 0.0F, highlight, new String[]{(int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION});
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Pather cells on " + this.market.getName() + " are eliminated.", opad, highlight, new String[]{(int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION});
        }
    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    protected void applyImproveModifiers()
    {
        if (isImproved())
        {
            market.getStability().modifyFlat("CHAMELEON_improve", IMPROVE_STABILITY_BONUS, getImprovementsDescForModifiers() + " (CHAMELEON)");
        }
        else
        {
            market.getStability().unmodifyFlat("CHAMELEON_improve");
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();


        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            info.addPara("Stability increased by %s.", 0f, highlight, "" + (int) IMPROVE_STABILITY_BONUS);
        } else {
            info.addPara("Increases stability by %s.", 0f, highlight, "" + (int) IMPROVE_STABILITY_BONUS);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}

