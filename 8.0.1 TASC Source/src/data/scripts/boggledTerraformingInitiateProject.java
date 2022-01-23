package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.*;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.econ.CommRelayCondition;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.*;
import com.fs.starfarer.combat.entities.terrain.Planet;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import com.fs.starfarer.rpg.Person;
import data.campaign.econ.boggledTools;
import data.campaign.econ.conditions.Terraforming_Controller;
import data.campaign.econ.industries.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledTerraformingInitiateProject extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledTerraformingInitiateProject() {}

    public boggledTerraformingInitiateProject(SectorEntityToken entity) {
        this.init(entity);
    }

    protected void init(SectorEntityToken entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;

        this.entity = dialog.getInteractionTarget();
        TextPanelAPI text = dialog.getTextPanel();

        MarketAPI market = this.entity.getMarket();
        PlanetAPI planet = market.getPlanetEntity();
        Terraforming_Controller terraformingController = (Terraforming_Controller) market.getCondition("terraforming_controller").getPlugin();
        String currentProject = terraformingController.getProject();

        if(ruleId.equals("boggledTriggerAridTypeChange"))
        {
            terraformingController.setProject("aridTypeChange");
        }
        else if(ruleId.equals("boggledTriggerFrozenTypeChange"))
        {
            terraformingController.setProject("frozenTypeChange");
        }
        else if(ruleId.equals("boggledTriggerJungleTypeChange"))
        {
            terraformingController.setProject("jungleTypeChange");
        }
        else if(ruleId.equals("boggledTriggerTerranTypeChange"))
        {
            terraformingController.setProject("terranTypeChange");
        }
        else if(ruleId.equals("boggledTriggerTundraTypeChange"))
        {
            terraformingController.setProject("tundraTypeChange");
        }
        else if(ruleId.equals("boggledTriggerWaterTypeChange"))
        {
            terraformingController.setProject("waterTypeChange");
        }
        else if(ruleId.equals("boggledTriggerFarmlandResourceImprovement"))
        {
            terraformingController.setProject("farmlandResourceImprovement");
        }
        else if(ruleId.equals("boggledTriggerOrganicsResourceImprovement"))
        {
            terraformingController.setProject("organicsResourceImprovement");
        }
        else if(ruleId.equals("boggledTriggerVolatilesResourceImprovement"))
        {
            terraformingController.setProject("volatilesResourceImprovement");
        }
        else if(ruleId.equals("boggledTriggerExtremeWeatherConditionImprovement"))
        {
            terraformingController.setProject("extremeWeatherConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerMildClimateConditionImprovement"))
        {
            terraformingController.setProject("mildClimateConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerHabitableConditionImprovement"))
        {
            terraformingController.setProject("habitableConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerAtmosphereDensityConditionImprovement"))
        {
            terraformingController.setProject("atmosphereDensityConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerToxicAtmosphereConditionImprovement"))
        {
            terraformingController.setProject("toxicAtmosphereConditionImprovement");
        }

        return true;
    }
}