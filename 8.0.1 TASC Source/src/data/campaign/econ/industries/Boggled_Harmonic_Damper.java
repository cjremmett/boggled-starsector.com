package data.campaign.econ.industries;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.String;

import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

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
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
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
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.campaign.econ.BoggledStationConstructionIDs;
import data.campaign.econ.boggledTools;

public class Boggled_Harmonic_Damper extends BaseIndustry
{
    public static float IMPROVE_DEFENSE_BONUS = 1.25f;

    public static float GAMMA_DEFENSE_BONUS = 1.25f;
    public static float BETA_DEFENSE_BONUS = 1.50f;
    public static float ALPHA_DEFENSE_BONUS = 2.00f;

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<String>();
    static
    {
        SUPPRESSED_CONDITIONS.add(Conditions.TECTONIC_ACTIVITY);
        SUPPRESSED_CONDITIONS.add(Conditions.EXTREME_TECTONIC_ACTIVITY);
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(isFunctional())
        {
            for (String cid : SUPPRESSED_CONDITIONS)
            {
                market.suppressCondition(cid);
            }
        }

        if (this.aiCoreId == null)
        {
            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("boggled_harmonic_damper_ai_bonus");
        }
        else if (this.aiCoreId.equals("gamma_core"))
        {
            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("boggled_harmonic_damper_ai_bonus", GAMMA_DEFENSE_BONUS, "Gamma core (Harmonic damper)");
        }
        else if (this.aiCoreId.equals("beta_core"))
        {
            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("boggled_harmonic_damper_ai_bonus", BETA_DEFENSE_BONUS, "Beta core (Harmonic damper)");
        }
        else if (this.aiCoreId.equals("alpha_core"))
        {
            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("boggled_harmonic_damper_ai_bonus", ALPHA_DEFENSE_BONUS, "Alpha core (Harmonic damper)");
        }
    }

    @Override
    public void unapply()
    {
        for (String cid : SUPPRESSED_CONDITIONS)
        {
            market.unsuppressCondition(cid);
        }

        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("boggled_harmonic_damper_ai_bonus");
        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("boggled_harmonic_damper_improve_bonus");

        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        MarketAPI market = this.market;

        if(!Global.getSettings().getBoolean("boggledHarmonicDamperEnabled") || !Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!Global.getSettings().getBoolean("boggledHarmonicDamperEnabled") || !Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        return true;
    }

    @Override
    public String getUnavailableReason()
    {
        MarketAPI market = this.market;

        if(!Global.getSettings().getBoolean("boggledHarmonicDamperEnabled") || !Global.getSettings().getBoolean("boggledTerraformingContentEnabled"))
        {
            return "Error in getUnavailableReason() in Harmonic Damper. Please report this to boggled on the forums.";
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(this.market))
        {
            return "Error in getUnavailableReason() in Harmonic Damper. Please report this to boggled on the forums.";
        }

        return "Error in getUnavailableReason() in Harmonic Damper. Please report this to boggled on the forums.";
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        //Prevents AI cores from modifying upkeep
    }

    @Override
    protected void applyAlphaCoreSupplyAndDemandModifiers()
    {
        //Prevents AI cores from modifying supply and demand
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        float a = ALPHA_DEFENSE_BONUS;
        String str = Strings.X + (a) + "";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases ground defenses by %s.", 0.0F, highlight, str);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases ground defenses by %s.", opad, highlight, str);
        }
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }

        float a = BETA_DEFENSE_BONUS;
        String str = Strings.X + (a) + "";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases ground defenses by %s.", opad, highlight, str);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases ground defenses by %s.", opad, highlight, str);
        }
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }

        float a = GAMMA_DEFENSE_BONUS;
        String str = Strings.X + (a) + "";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases ground defenses by %s.", opad, highlight, str);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases ground defenses by %s.", opad, highlight, str);
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
            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult("boggled_harmonic_damper_improve_bonus", IMPROVE_DEFENSE_BONUS, "Improvements (Harmonic damper)");
        }
        else
        {
            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult("boggled_harmonic_damper_improve_bonus");
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        float a = IMPROVE_DEFENSE_BONUS;
        String str = Strings.X + (a) + "";

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            info.addPara("Ground defenses increased by %s.", 0f, highlight, str);
        } else {
            info.addPara("Increases ground defenses by %s.", 0f, highlight, str);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        float opad = 10.0F;

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED ||!isFunctional())
        {
            tooltip.addPara("If operational, would counter the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
            }
        }

        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional())
        {
            tooltip.addPara("Countering the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
            }
        }
    }
}

