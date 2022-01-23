package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledUnderConstructionEveryFrameScript;

public class Construct_Mining_Station extends BaseDurationAbility
{
    private float creditCost = Global.getSettings().getInt("boggledMiningStationBuildCreditCost");
    private float crewCost = Global.getSettings().getInt("boggledMiningStationBuildCrewCost");
    private float heavyMachineryCost = Global.getSettings().getInt("boggledMiningStationBuildHeavyMachineryCost");
    private float metalCost = Global.getSettings().getInt("boggledMiningStationBuildMetalCost");
    private float transplutonicsCost = Global.getSettings().getInt("boggledMiningStationBuildTransplutonicsCost");

    public Construct_Mining_Station() { }

    @Override
    protected void activateImpl()
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        playerCargo.removeCommodity("metals", metalCost);
        playerCargo.removeCommodity("rare_metals", transplutonicsCost);
        playerCargo.removeCommodity("crew", crewCost);
        playerCargo.removeCommodity("heavy_machinery", heavyMachineryCost);

        StarSystemAPI system = playerFleet.getStarSystem();
        SectorEntityToken newMiningStation = system.addCustomEntity("boggled_mining_station" + clock.getCycle() + clock.getMonth() + clock.getDay(), system.getBaseName() + " Mining Station", "boggled_mining_station_small", playerFleet.getFaction().getId());

        //Set the mining station in an orbit that keeps it within the asteroid belt or asteroid field
        if(boggledTools.playerFleetInAsteroidBelt(playerFleet))
        {
            SectorEntityToken focus = boggledTools.getFocusOfAsteroidBelt(playerFleet);
            float orbitRadius = boggledTools.getDistanceBetweenTokens(focus, playerFleet);
            float orbitAngle = boggledTools.getAngleFromPlayerFleet(focus);

            newMiningStation.setCircularOrbitPointingDown(focus, orbitAngle + 1, orbitRadius, orbitRadius / 10.0F);
        }
        else if(boggledTools.playerFleetInAsteroidField(playerFleet))
        {
            OrbitAPI asteroidOrbit = boggledTools.getAsteroidFieldOrbit(playerFleet);

            if (asteroidOrbit != null)
            {
                newMiningStation.setCircularOrbitWithSpin(asteroidOrbit.getFocus(), boggledTools.getAngleFromPlayerFleet(asteroidOrbit.getFocus()), boggledTools.getDistanceBetweenTokens(playerFleet, asteroidOrbit.getFocus()), asteroidOrbit.getOrbitalPeriod(), 5f, 10f);
            }
            else
            {
                SectorEntityToken centerOfAsteroidField = boggledTools.getAsteroidFieldEntity(playerFleet);
                newMiningStation.setCircularOrbitWithSpin(centerOfAsteroidField, boggledTools.getAngleFromPlayerFleet(centerOfAsteroidField), boggledTools.getDistanceBetweenTokens(playerFleet, centerOfAsteroidField), 40f, 5f, 10f);
            }
        }

        SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_small_lights_overlay", playerFleet.getFaction().getId());
        newMiningStationLights.setOrbit(newMiningStation.getOrbit().makeCopy());

        newMiningStation.setInteractionImage("illustrations", "orbital_construction");
        String systemName = system.getName();

        MarketAPI market = null;
        if(!Global.getSettings().getBoolean("boggledStationConstructionDelayEnabled"))
        {
            market = boggledTools.createMiningStationMarket(newMiningStation);
        }
        else
        {
            newMiningStation.addScript(new BoggledUnderConstructionEveryFrameScript(newMiningStation));
            Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0F, 1.0F);
        }

        //Delete abandoned mining stations and transfer their cargo to the newly created one
        CargoAPI cargo = null;
        ArrayList<SectorEntityToken> stationsToDelete = new ArrayList<SectorEntityToken>();

        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if(entity.hasTag("boggled_mining_station") && entity.getFaction().getId().equals("neutral"))
            {
                stationsToDelete.add(entity);
            }
        }
        allEntitiesInSystem = null;

        for(int i = 0; i < stationsToDelete.size(); i++)
        {
            cargo = stationsToDelete.get(i).getMarket().getSubmarket("storage").getCargo();
            if(!cargo.isEmpty())
            {
                //Put the deleted stations' cargo into the new station market if it was created
                //Otherwise, if the station is still under construction, put it into the player cargo
                if(market != null)
                {
                    market.getSubmarket("storage").getCargo().addAll(cargo);
                }
                else
                {
                    playerCargo.addAll(cargo);
                }
            }
            playerFleet.getStarSystem().removeEntity(stationsToDelete.get(i));
        }
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        boolean playerHasResources = true;
        boolean miningStationCapReached = false;
        int miningStationsInSystem = 0;
        int miningStationCap = Global.getSettings().getInt("boggledMaxNumMiningStationsPerSystem");

        if(!(boggledTools.playerFleetInAsteroidBelt(playerFleet) || boggledTools.playerFleetInAsteroidField(playerFleet)))
        {
            return false;
        }

        if(boggledTools.playerFleetTooCloseToJumpPoint(playerFleet))
        {
            return false;
        }

        if(miningStationCap == 0)
        {
            return false;
        }

        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if(entity.hasTag("boggled_mining_station") && !entity.getFaction().getId().equals("neutral"))
            {
                miningStationsInSystem++;
            }
        }

        if(miningStationsInSystem >= miningStationCap)
        {
            miningStationCapReached = true;
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            playerHasResources = false;
        }

        if(playerCargo.getCommodityQuantity("metals") < metalCost)
        {
            playerHasResources = false;
        }

        if(playerCargo.getCommodityQuantity("rare_metals") < transplutonicsCost)
        {
            playerHasResources = false;
        }

        if(playerCargo.getCommodityQuantity("crew") < crewCost)
        {
            playerHasResources = false;
        }

        if(playerCargo.getCommodityQuantity("heavy_machinery") < heavyMachineryCost)
        {
            playerHasResources = false;
        }

        return !this.isOnCooldown() && this.disableFrames <= 0 && !miningStationCapReached && playerHasResources;
    }

    @Override
    public boolean hasTooltip()
    {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle("Construct Mining Station");
        float pad = 10.0F;
        tooltip.addPara("Construct a mining station in an asteroid belt or asteroid field. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "",(int)crewCost + "",(int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});

        boolean playerFleetInAsteroidBelt = false;

        if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            if (boggledTools.playerFleetInAsteroidBelt(playerFleet) || boggledTools.playerFleetInAsteroidField(playerFleet))
            {
                playerFleetInAsteroidBelt = true;
            }
        }

        if(Global.getSettings().getBoolean("boggledMiningStationLinkToResourceBelts"))
        {
            if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
            {
                Integer numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(playerFleet);
                tooltip.addPara("There are %s asteroid belts in the " + playerFleet.getStarSystem().getName() + ". A mining station constructed here would have %s resources.", pad, highlight, new String[]{numAsteroidBeltsInSystem +"", boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem)});
            }
        }
        else
        {
            String resourceLevel = "moderate";
            int staticAmountPerSettings = Global.getSettings().getInt("boggledMiningStationStaticAmount");
            switch(staticAmountPerSettings)
            {
                case 1:
                    resourceLevel = "sparse";
                    break;
                case 2:
                    resourceLevel = "moderate";
                    break;
                case 3:
                    resourceLevel = "abundant";
                    break;
                case 4:
                    resourceLevel = "rich";
                    break;
                case 5:
                    resourceLevel = "ultrarich";
                    break;
            }
            tooltip.addPara("Mining stations have %s ore and rare ore resources.", pad, highlight, new String[]{resourceLevel});
        }

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot construct a mining station in hyperspace.", bad, pad);
        }
        else if(boggledTools.playerFleetTooCloseToJumpPoint(playerFleet))
        {
            tooltip.addPara("You cannot construct a mining station so close to a jump point.", bad, pad);
        }

        boolean miningStationCapReached = false;
        int miningStationsInSystem = 0;
        int miningStationCap = Global.getSettings().getInt("boggledMaxNumMiningStationsPerSystem");

        if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if(entity.hasTag("boggled_mining_station") && !entity.getFaction().getId().equals("neutral"))
                {
                    miningStationsInSystem++;
                }
            }
        }

        if(miningStationsInSystem >= miningStationCap)
        {
            miningStationCapReached = true;
        }

        if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if(entity.hasTag("boggled_mining_station") && entity.getFaction().getId().equals("neutral"))
                {
                    tooltip.addPara("There is at least one abandoned player-built mining station in this system. If you construct a new mining station, any abandoned stations will be destroyed and any cargo stored on them will be transferred to the new station.", pad, highlight, new String[]{});
                }
            }
        }

        if(!playerFleetInAsteroidBelt)
        {
            tooltip.addPara("Your fleet is too far away from an asteroid belt or asteroid field to build a mining station.", bad, pad);
        }

        if(miningStationCapReached && miningStationCap == 0)
        {
            tooltip.addPara("Construction of player-built mining stations has been disabled in the settings.json file. To enable construction of mining stations, change the value boggledMaxNumMiningStationsPerSystem to something other than zero.", bad, pad);
        }
        else if (miningStationCapReached && miningStationCap == 1)
        {
            tooltip.addPara("Each system can only support one player-built mining station. The mining station that already exists must be abandoned before a new mining station can be built in this system.", bad, pad);
        }
        else if(miningStationCapReached && miningStationCap > 1)
        {
            tooltip.addPara("Each system can only support " + miningStationCap + " player-built mining stations. You must abandon one or more existing mining stations before a new mining station can be constructed in this system.", bad, pad);
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara("Insufficient credits.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("crew") < crewCost)
        {
            tooltip.addPara("Insufficient crew.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara("Insufficient heavy machinery.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("metals") < metalCost)
        {
            tooltip.addPara("Insufficient metals.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("rare_metals") < transplutonicsCost)
        {
            tooltip.addPara("Insufficient transplutonics.", bad, pad);
        }
    }

    @Override
    public boolean isTooltipExpandable() { return false; }

    @Override
    protected void applyEffect(float v, float v1) { }

    @Override
    protected void deactivateImpl() { }

    @Override
    protected void cleanupImpl() { }
}