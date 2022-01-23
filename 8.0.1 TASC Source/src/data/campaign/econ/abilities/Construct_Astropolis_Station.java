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

public class Construct_Astropolis_Station extends BaseDurationAbility
{
    private float creditCost = Global.getSettings().getInt("boggledAstropolisStationBuildCreditCost");
    private float crewCost = Global.getSettings().getInt("boggledAstropolisStationBuildCrewCost");
    private float heavyMachineryCost = Global.getSettings().getInt("boggledAstropolisStationBuildHeavyMachineryCost");
    private float metalCost = Global.getSettings().getInt("boggledAstropolisStationBuildMetalCost");
    private float transplutonicsCost = Global.getSettings().getInt("boggledAstropolisStationBuildTransplutonicsCost");

    public Construct_Astropolis_Station() { }

    private int numAstroInOrbit(SectorEntityToken targetPlanet)
    {
        int numAstropoli = 0;
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return 0;
        }

        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.hasTag("boggled_astropolis") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(targetPlanet))
            {
                numAstropoli++;
            }
        }

        return numAstropoli;
    }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        playerCargo.removeCommodity("metals", metalCost);
        playerCargo.removeCommodity("rare_metals", transplutonicsCost);
        playerCargo.removeCommodity("crew", crewCost);
        playerCargo.removeCommodity("heavy_machinery", heavyMachineryCost);

        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);
        int numAstro = numAstroInOrbit(targetPlanet);
        StarSystemAPI system = playerFleet.getStarSystem();
        float orbitRadius = targetPlanet.getRadius() + 375.0F;

        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        String playerFactionID = playerFaction.getId();

        if(numAstro == 0)
        {
            SectorEntityToken newAstropolis = system.addCustomEntity("boggled_astropolisAlpha", targetPlanet.getName() + " Astropolis Alpha", "boggled_astropolis_station_alpha_small", playerFactionID);
            newAstropolis.setCircularOrbitPointingDown(targetPlanet, boggledTools.randomOrbitalAngleFloat(), orbitRadius, orbitRadius / 10.0F);
            newAstropolis.setInteractionImage("illustrations", "orbital_construction");

            SectorEntityToken newAstropolisLights = system.addCustomEntity("boggled_astropolisAlphaLights", targetPlanet.getName() + " Astropolis Alpha Lights Overlay", "boggled_astropolis_station_alpha_small_lights_overlay", playerFactionID);
            newAstropolisLights.setOrbit(newAstropolis.getOrbit().makeCopy());

            MarketAPI market = null;
            if(!Global.getSettings().getBoolean("boggledStationConstructionDelayEnabled"))
            {
                market = boggledTools.createAstropolisStationMarket(newAstropolis, targetPlanet);
            }
            else
            {
                newAstropolis.addScript(new BoggledUnderConstructionEveryFrameScript(newAstropolis));
                Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0F, 1.0F);
            }
        }
        else if(numAstro == 1)
        {
            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            SectorEntityToken alphaAstroToken = null;

            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if (entity.hasTag("boggled_astropolis") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(targetPlanet) && entity.getCustomEntityType().contains("boggled_astropolis_station_alpha"))
                {
                    alphaAstroToken = entity;
                    break;
                }
            }

            SectorEntityToken newAstropolis = system.addCustomEntity("boggled_astropolisBeta", targetPlanet.getName() + " Astropolis Beta", "boggled_astropolis_station_beta_small", playerFactionID);
            newAstropolis.setCircularOrbitPointingDown(targetPlanet, alphaAstroToken.getCircularOrbitAngle() + 120f, orbitRadius, orbitRadius / 10.0F);
            newAstropolis.setInteractionImage("illustrations", "orbital_construction");

            SectorEntityToken newAstropolisLights = system.addCustomEntity("boggled_astropolisBetaLights", targetPlanet.getName() + " Astropolis Beta Lights Overlay", "boggled_astropolis_station_beta_small_lights_overlay", playerFactionID);
            newAstropolisLights.setOrbit(newAstropolis.getOrbit().makeCopy());

            MarketAPI market = null;
            if(!Global.getSettings().getBoolean("boggledStationConstructionDelayEnabled"))
            {
                market = boggledTools.createAstropolisStationMarket(newAstropolis, targetPlanet);
            }
            else
            {
                newAstropolis.addScript(new BoggledUnderConstructionEveryFrameScript(newAstropolis));
                Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0F, 1.0F);
            }
        }
        else if(numAstro == 2)
        {
            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            SectorEntityToken alphaAstroToken = null;

            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if (entity.hasTag("boggled_astropolis") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(targetPlanet) && entity.getCustomEntityType().contains("boggled_astropolis_station_alpha"))
                {
                    alphaAstroToken = entity;
                    break;
                }
            }

            SectorEntityToken newAstropolis = system.addCustomEntity("boggled_astropolisGamma", targetPlanet.getName() + " Astropolis Gamma", "boggled_astropolis_station_gamma_small", playerFactionID);
            newAstropolis.setCircularOrbitPointingDown(targetPlanet, alphaAstroToken.getCircularOrbitAngle() - 120f, orbitRadius, orbitRadius / 10.0F);
            newAstropolis.setInteractionImage("illustrations", "orbital_construction");

            SectorEntityToken newAstropolisLights = system.addCustomEntity("boggled_astropolisGammaLights", targetPlanet.getName() + " Astropolis Gamma Lights Overlay", "boggled_astropolis_station_gamma_small_lights_overlay", playerFactionID);
            newAstropolisLights.setOrbit(newAstropolis.getOrbit().makeCopy());

            MarketAPI market = null;
            if(!Global.getSettings().getBoolean("boggledStationConstructionDelayEnabled"))
            {
                market = boggledTools.createAstropolisStationMarket(newAstropolis, targetPlanet);
            }
            else
            {
                newAstropolis.addScript(new BoggledUnderConstructionEveryFrameScript(newAstropolis));
                Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0F, 1.0F);
            }
        }
    }

    final class astropolisOrbitBlocker
    {
        public SectorEntityToken blocker;
        public String reason;

        public astropolisOrbitBlocker(SectorEntityToken blocker, String reason)
        {
            this.blocker = blocker;
            this.reason = reason;
        }
    }

    private astropolisOrbitBlocker astropolisOrbitBlocked(SectorEntityToken targetPlanet)
    {
        //check if the host market radius is too small
        if(targetPlanet.getRadius() < 125f)
        {
            return new astropolisOrbitBlocker(null, "radius_too_small");
        }

        //check if the host market is too close to its orbital focus
        if(targetPlanet.getOrbitFocus() != null && targetPlanet.getCircularOrbitRadius() < (targetPlanet.getOrbitFocus().getRadius() + 900f))
        {
            return new astropolisOrbitBlocker(targetPlanet.getOrbitFocus(), "too_close_to_focus");
        }

        //check if the host market is too close to a star
        if(targetPlanet.getOrbitFocus() != null && targetPlanet.getOrbitFocus().isStar() && targetPlanet.getCircularOrbitRadius() < (targetPlanet.getOrbitFocus().getRadius() + 1400f))
        {
            return new astropolisOrbitBlocker(targetPlanet.getOrbitFocus(), "too_close_to_star");
        }

        //check if the host market has a moon that is too close to it
        Iterator allPlanetsInSystem = targetPlanet.getStarSystem().getPlanets().iterator();
        while(allPlanetsInSystem.hasNext())
        {
            PlanetAPI planet = (PlanetAPI) allPlanetsInSystem.next();
            if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(targetPlanet) && planet.getCircularOrbitRadius() < (targetPlanet.getRadius() + 500f) && planet.getRadius() != 0)
            {
                return new astropolisOrbitBlocker(planet, "moon_too_close");
            }
        }
        allPlanetsInSystem = null;

        //Check if the host market has four moons - need to block building here because it creates a visual bug where the astropolis
        //appears on top of one of the other four moons in the system view
        allPlanetsInSystem = targetPlanet.getStarSystem().getPlanets().iterator();
        int numMoons = 0;
        while(allPlanetsInSystem.hasNext())
        {
            PlanetAPI planet = (PlanetAPI) allPlanetsInSystem.next();
            if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(targetPlanet) && planet.getRadius() != 0)
            {
                numMoons++;
            }
        }

        if(numMoons >= 4)
        {
            return new astropolisOrbitBlocker(null, "too_many_moons");
        }
        allPlanetsInSystem = null;

        //check if the host market and other planets are orbiting the same focus are too close to each other
        allPlanetsInSystem = targetPlanet.getStarSystem().getPlanets().iterator();
        while(allPlanetsInSystem.hasNext())
        {
            PlanetAPI planet = (PlanetAPI)allPlanetsInSystem.next();
            if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(targetPlanet.getOrbitFocus()))
            {
                if(Math.abs(planet.getCircularOrbitRadius() - targetPlanet.getCircularOrbitRadius()) < 400f && Math.abs(planet.getCircularOrbitRadius() - targetPlanet.getCircularOrbitRadius()) != 0)
                {
                    return new astropolisOrbitBlocker(planet, "same_focus_too_close");
                }
            }
        }

        return null;
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);
        if(targetPlanet == null || targetPlanet.getMarket() == null || (!targetPlanet.getMarket().isPlayerOwned() && targetPlanet.getMarket().getFaction() != Global.getSector().getFaction("neutral")))
        {
            return false;
        }

        // Checks for governorship by player. Governed planets have isPlayerOwned() equals true but
        // the controlling FactionAPI is still major faction, not player
        if(targetPlanet.getMarket().isPlayerOwned() && targetPlanet.getMarket().getFaction() != playerFleet.getFaction() && !Global.getSettings().getBoolean("boggledCanBuildAstropolisOnPurchasedGovernorshipPlanets"))
        {
            return false;
        }

        if((boggledTools.getDistanceBetweenTokens(targetPlanet, playerFleet) - targetPlanet.getRadius()) > 500f)
        {
            return false;
        }

        astropolisOrbitBlocker block = astropolisOrbitBlocked(targetPlanet);
        if(block != null && !Global.getSettings().getBoolean("boggledAstropolisIgnoreOrbitalRequirements"))
        {
            return false;
        }

        int astroLimit = Global.getSettings().getInt("boggledMaxNumAstropoliPerPlanet");

        if(astroLimit == 0)
        {
            return false;
        }

        int astroInOrbit = numAstroInOrbit(targetPlanet);
        if(astroInOrbit >= astroLimit)
        {
            return false;
        }

        boolean playerHasResources = true;
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

        return !this.isOnCooldown() && this.disableFrames <= 0 && playerHasResources;
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
        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle("Construct Astropolis Station");
        float pad = 10.0F;
        tooltip.addPara("Construct an astropolis station in orbit around a planet or moon. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "",(int)crewCost + "",(int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot construct an astropolis station in hyperspace.", bad, pad);
        }

        if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && targetPlanet != null)
        {
            if((boggledTools.getDistanceBetweenTokens(targetPlanet, playerFleet) - targetPlanet.getRadius()) > 500f)
            {
                float distanceInSu = (boggledTools.getDistanceBetweenTokens(targetPlanet, playerFleet) - targetPlanet.getRadius()) / 2000f;
                String distanceInSuString = String.format("%.2f", distanceInSu);
                float requiredDistanceInSu = 500f / 2000f;
                String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
                tooltip.addPara("The world closest to your location is " + targetPlanet.getName() + ". Your fleet is " + distanceInSuString + " stellar units away. You must be within " + requiredDistanceInSuString + " stellar units to construct an astropolis station.", bad, pad);
            }
            else
            {
                tooltip.addPara("Target host world: %s", pad, highlight, new String[]{targetPlanet.getName()});
            }

            if(targetPlanet.getMarket() != null && (!targetPlanet.getMarket().isPlayerOwned() && targetPlanet.getMarket().getFaction() != Global.getSector().getFaction("neutral")))
            {
                tooltip.addPara("You cannot construct an astropolis station in orbit around a world already controlled by another faction.", bad, pad);
            }

            //Checks for governorship by player. Governed planets have isPlayerOwned() equals true but
            //the controlling FactionAPI is still major faction, not player
            if(targetPlanet.getMarket() != null && targetPlanet.getMarket().isPlayerOwned() && targetPlanet.getMarket().getFaction() != playerFleet.getFaction() && !Global.getSettings().getBoolean("boggledCanBuildAstropolisOnPurchasedGovernorshipPlanets"))
            {
                tooltip.addPara("You cannot construct an astropolis station in orbit around a world owned by another faction.", bad, pad);
            }

            if(!Global.getSettings().getBoolean("boggledAstropolisIgnoreOrbitalRequirements"))
            {
                astropolisOrbitBlocker astroblocker = astropolisOrbitBlocked(targetPlanet);
                if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("radius_too_small"))
                {
                    tooltip.addPara(targetPlanet.getName() + " is too small to host an astropolis.", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("too_close_to_focus"))
                {
                    tooltip.addPara("An astropolis would be unable to achieve a satisfactory orbit around " + targetPlanet.getName() + " because it is orbiting too close to " + astroblocker.blocker.getName() + ".", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("too_close_to_star"))
                {
                    tooltip.addPara(targetPlanet.getName() + " is too close to " + astroblocker.blocker.getName() + " to host an astropolis.", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("moon_too_close"))
                {
                    tooltip.addPara("An astropolis would be unable to achieve a satisfactory orbit around " + targetPlanet.getName() + " because " + astroblocker.blocker.getName() + " is orbiting too close to it.", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("same_focus_too_close"))
                {
                    tooltip.addPara("An astropolis would be unable to maintain a satisfactory orbit around " + targetPlanet.getName() + " because " + astroblocker.blocker.getName() + " periodically approaches very near to this world.", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("too_many_moons"))
                {
                    tooltip.addPara("An astropolis would be unable to maintain a satisfactory orbit around " + targetPlanet.getName() + " because there are four or more moons orbiting it.", bad, pad);
                }
            }

            int maxAstropoliPerPlanet = Global.getSettings().getInt("boggledMaxNumAstropoliPerPlanet");
            int astroInOrbit = numAstroInOrbit(targetPlanet);
            if(maxAstropoliPerPlanet > 3)
            {
                tooltip.addPara("Permissible values for boggledMaxNumAstropoliPerPlanet are 1, 2 or 3. Please use the settings file for this mod to enter a permissible value.", bad, pad);
            }
            else if(maxAstropoliPerPlanet == 0)
            {
                tooltip.addPara("Astropolis construction has been disabled because boggledMaxNumAstropoliPerPlanet is set to zero in the settings file. To enable construction, set boggledMaxNumAstropoliPerPlanet to 1, 2 or 3.", bad, pad);
            }
            else if(astroInOrbit >= maxAstropoliPerPlanet && maxAstropoliPerPlanet == 1)
            {
                tooltip.addPara("Each world can only support a single astropolis. " + targetPlanet.getName() + " already has an astropolis in orbit.", bad, pad);
            }
            else if(astroInOrbit >= maxAstropoliPerPlanet && maxAstropoliPerPlanet > 1)
            {
                tooltip.addPara("Each world can support a maximum of " + maxAstropoliPerPlanet + " astropoli. " + targetPlanet.getName() + " has reached or exceeded that limit.", bad, pad);
            }
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