
package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
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

public class Sprite_Controller extends BaseHazardCondition
{
    public Sprite_Controller() { }

    public void advance(float amount)
    {
        super.advance(amount);

        MarketAPI market = this.market;
        SectorEntityToken entity = market.getPrimaryEntity();

        // Remove condition if it somehow ends up someplace other than one of the stations created by this mod.
        // Gatekeeper stations are no longer part of the mod, but I'll keep their code here in case I decide to reintroduce them at a later time.
        if(!entity.hasTag("boggled_astropolis") && !entity.hasTag("boggled_mining_station") && !entity.hasTag("boggled_siphon_station") && !entity.hasTag("boggled_gatekeeper_station"))
        {
            market.removeCondition("sprite_controller");
            return;
        }

        if(entity.hasTag("boggled_astropolis"))
        {
            if(entity.getCustomEntityType().contains("boggled_astropolis_station_alpha"))
            {
                if(market.getFactionId().equals("neutral"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "alpha");
                }

                if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_astropolis_station_alpha_medium") && !entity.getCustomEntityType().equals("boggled_astropolis_station_alpha_large"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "alpha");
                    boggledTools.swapStationSprite(entity, "astropolis", "alpha", 2);
                }
                else if(market.getSize() >= 6 && !entity.getCustomEntityType().equals("boggled_astropolis_station_alpha_large"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "alpha");
                    boggledTools.swapStationSprite(entity, "astropolis", "alpha", 3);
                }
            }
            else if(entity.getCustomEntityType().contains("boggled_astropolis_station_beta"))
            {
                if(market.getFactionId().equals("neutral"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "beta");
                }

                if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_astropolis_station_beta_medium") && !entity.getCustomEntityType().equals("boggled_astropolis_station_beta_large"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "beta");
                    boggledTools.swapStationSprite(entity, "astropolis", "beta", 2);
                }
                else if(market.getSize() >= 6 && !entity.getCustomEntityType().equals("boggled_astropolis_station_beta_large"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "beta");
                    boggledTools.swapStationSprite(entity, "astropolis", "beta", 3);
                }
            }
            else if(entity.getCustomEntityType().contains("boggled_astropolis_station_gamma"))
            {
                if(market.getFactionId().equals("neutral"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "gamma");
                }

                if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_astropolis_station_gamma_medium") && !entity.getCustomEntityType().equals("boggled_astropolis_station_gamma_large"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "gamma");
                    boggledTools.swapStationSprite(entity, "astropolis", "gamma", 2);
                }
                else if(market.getSize() >= 6 && !entity.getCustomEntityType().equals("boggled_astropolis_station_gamma_large"))
                {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "gamma");
                    boggledTools.swapStationSprite(entity, "astropolis", "gamma", 3);
                }
            }
        }
        else if(entity.hasTag("boggled_mining_station"))
        {
            if(market.getFactionId().equals("neutral"))
            {
                //Remember that we can't identify the correct mining station lights overlay because there could be
                //an unknown number of mining stations in the system in an unknown orbital configuration.
                //Deletes all overlays, then puts them all back.
                StarSystemAPI system = entity.getStarSystem();
                boggledTools.deleteOldLightsOverlay(entity, "mining", null);
                boggledTools.reapplyMiningStationLights(system);
            }

            if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_mining_station_medium"))
            {
                StarSystemAPI system = entity.getStarSystem();
                boggledTools.deleteOldLightsOverlay(entity, "mining", null);
                boggledTools.swapStationSprite(entity, "mining", "null", 2);
                boggledTools.reapplyMiningStationLights(system);
            }
        }
        else if(entity.hasTag("boggled_siphon_station"))
        {
            if(market.getFactionId().equals("neutral"))
            {
                boggledTools.deleteOldLightsOverlay(entity, "siphon", null);
            }

            if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_siphon_station_medium"))
            {
                boggledTools.deleteOldLightsOverlay(entity, "siphon", null);
                boggledTools.swapStationSprite(entity, "siphon", "null", 2);
            }
        }
        else if(entity.hasTag("boggled_gatekeeper_station"))
        {
            if(market.getFactionId().equals("neutral"))
            {
                boggledTools.deleteOldLightsOverlay(entity, "gatekeeper", null);
            }

            if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_gatekeeper_station_medium"))
            {
                boggledTools.swapStationSprite(entity, "gatekeeper", "null", 2);
            }
        }
    }

    public void apply(String id) { super.apply(id); }

    public void unapply(String id) { super.unapply(id); }

    public Map<String, String> getTokenReplacements() { return super.getTokenReplacements(); }

    public boolean showIcon() { return false; }
}
