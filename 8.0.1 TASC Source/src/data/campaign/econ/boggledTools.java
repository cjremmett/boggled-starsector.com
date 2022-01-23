package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CircularFleetOrbit;
import com.fs.starfarer.campaign.CircularOrbit;
import com.fs.starfarer.campaign.CircularOrbitPointDown;
import com.fs.starfarer.campaign.CircularOrbitWithSpin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.campaign.econ.conditions.Terraforming_Controller;
import org.json.JSONException;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class boggledTools {
    public static float getDistanceBetweenPoints(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public static float getDistanceBetweenTokens(SectorEntityToken tokenA, SectorEntityToken tokenB) {
        return getDistanceBetweenPoints(tokenA.getLocation().x, tokenA.getLocation().y, tokenB.getLocation().x, tokenB.getLocation().y);
    }

    public static float getAngle(float focusX, float focusY, float playerX, float playerY) {
        float angle = (float) Math.toDegrees(Math.atan2(focusY - playerY, focusX - playerX));

        //Not entirely sure what math is going on behind the scenes but this works to get the station to spawn next to the player
        angle = angle + 180f;

        return angle;
    }

    public static float getAngleFromPlayerFleet(SectorEntityToken target) {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        return getAngle(target.getLocation().x, target.getLocation().y, playerFleet.getLocation().x, playerFleet.getLocation().y);
    }

    public static float getAngleFromEntity(SectorEntityToken entity, SectorEntityToken target) {
        return getAngle(target.getLocation().x, target.getLocation().y, entity.getLocation().x, entity.getLocation().y);
    }

    public static void surveyAll(MarketAPI market) {
        for (MarketConditionAPI condition : market.getConditions()) {
            condition.setSurveyed(true);
        }
    }

    public static void refreshSupplyAndDemand(MarketAPI market) {
        //Refreshes supply and demand for each industry on the market
        List<Industry> industries = market.getIndustries();
        for (int i = 0; i < industries.size(); i++) {
            industries.get(i).doPreSaveCleanup();
            industries.get(i).doPostSaveRestore();
        }
    }

    public static float getRandomOrbitalAngleFloat(float min, float max) {
        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    public static boolean playerMarketInSystem(SectorEntityToken playerFleet) {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while (allEntitiesInSystem.hasNext()) {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if (entity.getMarket() != null && entity.getMarket().isPlayerOwned()) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestPlayerMarketToken(SectorEntityToken playerFleet) {
        if (!playerMarketInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allPlayerMarketsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while (allEntitiesInSystem.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity.getMarket() != null && entity.getMarket().isPlayerOwned()) {
                    allPlayerMarketsInSystem.add(entity);
                }
            }

            SectorEntityToken closestMarket = null;
            Iterator checkDistancesOfPlayerMarkets = allPlayerMarketsInSystem.iterator();
            while (checkDistancesOfPlayerMarkets.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) checkDistancesOfPlayerMarkets.next();
                if (closestMarket == null) {
                    closestMarket = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestMarket, playerFleet)) {
                    closestMarket = entity;
                }
            }

            return closestMarket;
        }
    }

    public static boolean gasGiantInSystem(SectorEntityToken playerFleet) {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while (allEntitiesInSystem.hasNext()) {
            SectorEntityToken planet = (SectorEntityToken) allEntitiesInSystem.next();
            if (planet instanceof PlanetAPI && ((PlanetAPI) planet).isGasGiant()) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestGasGiantToken(SectorEntityToken playerFleet) {
        if (!gasGiantInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allGasGiantsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while (allEntitiesInSystem.hasNext()) {
                SectorEntityToken planet = (SectorEntityToken) allEntitiesInSystem.next();
                if (planet instanceof PlanetAPI && ((PlanetAPI) planet).isGasGiant()) {
                    allGasGiantsInSystem.add(planet);
                }
            }

            SectorEntityToken closestGasGiant = null;
            Iterator checkDistancesOfGasGiants = allGasGiantsInSystem.iterator();
            while (checkDistancesOfGasGiants.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) checkDistancesOfGasGiants.next();
                if (closestGasGiant == null) {
                    closestGasGiant = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestGasGiant, playerFleet)) {
                    closestGasGiant = entity;
                }
            }

            return closestGasGiant;
        }
    }

    public static boolean colonizableStationInSystem(SectorEntityToken playerFleet) {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while (allEntitiesInSystem.hasNext()) {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if (entity.hasTag("station") && entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION)) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestColonizableStationInSystem(SectorEntityToken playerFleet) {
        if (!colonizableStationInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allColonizableStationsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while (allEntitiesInSystem.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity.hasTag("station") && entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION)) {
                    allColonizableStationsInSystem.add(entity);
                }
            }

            SectorEntityToken closestStation = null;
            Iterator checkDistancesOfStations = allColonizableStationsInSystem.iterator();
            while (checkDistancesOfStations.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) checkDistancesOfStations.next();
                if (closestStation == null) {
                    closestStation = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestStation, playerFleet)) {
                    closestStation = entity;
                }
            }

            return closestStation;
        }
    }

    public static boolean stationInSystem(SectorEntityToken playerFleet) {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while (allEntitiesInSystem.hasNext()) {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if (entity.hasTag("station")) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestStationInSystem(SectorEntityToken playerFleet) {
        if (!stationInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allStationsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while (allEntitiesInSystem.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity.hasTag("station") && !entity.hasTag("boggled_astral_gate")) {
                    allStationsInSystem.add(entity);
                }
            }

            SectorEntityToken closestStation = null;
            Iterator checkDistancesOfStations = allStationsInSystem.iterator();
            while (checkDistancesOfStations.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) checkDistancesOfStations.next();
                if (closestStation == null) {
                    closestStation = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestStation, playerFleet)) {
                    closestStation = entity;
                }
            }

            return closestStation;
        }
    }

    public static ArrayList<String> getListOfFactionsWithMarketInSystem(StarSystemAPI system) {
        ArrayList<String> factionsWithMarketInSystem = new ArrayList<String>();

        Iterator allMarkets = Global.getSector().getEconomy().getMarkets(system).iterator();
        while (allMarkets.hasNext()) {
            MarketAPI market = (MarketAPI) allMarkets.next();
            if (!factionsWithMarketInSystem.contains(market.getFactionId())) {
                factionsWithMarketInSystem.add(market.getFactionId());
            }
        }

        return factionsWithMarketInSystem;
    }

    public static ArrayList<Integer> getCompanionListOfTotalMarketPopulation(StarSystemAPI system, ArrayList<String> factions) {
        ArrayList<Integer> totalFactionMarketSize = new ArrayList<Integer>();
        int buffer = 0;
        Iterator allMarkets = null;

        Iterator allFactionsWithMarketInSystem = factions.iterator();
        while (allFactionsWithMarketInSystem.hasNext()) {
            String faction = (String) allFactionsWithMarketInSystem.next();
            allMarkets = Global.getSector().getEconomy().getMarkets(system).iterator();
            while (allMarkets.hasNext()) {
                MarketAPI market = (MarketAPI) allMarkets.next();
                if (market.getFactionId().equals(faction)) {
                    buffer = buffer + market.getSize();
                }
            }

            totalFactionMarketSize.add(buffer);
            buffer = 0;
        }

        return totalFactionMarketSize;
    }

    public static boolean planetInSystem(SectorEntityToken playerFleet) {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while (allEntitiesInSystem.hasNext()) {
            SectorEntityToken planet = (SectorEntityToken) allEntitiesInSystem.next();
            if (planet instanceof PlanetAPI && !getPlanetType(((PlanetAPI) planet)).equals("star")) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestPlanetToken(SectorEntityToken playerFleet) {
        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()) {
            return null;
        }

        if (!planetInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allPlanetsInSystem = new ArrayList<SectorEntityToken>();

            Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
            while (allEntitiesInSystem.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if (entity instanceof PlanetAPI && !getPlanetType(((PlanetAPI) entity)).equals("star")) {
                    allPlanetsInSystem.add(entity);
                }
            }

            SectorEntityToken closestPlanet = null;
            Iterator checkDistancesOfPlanets = allPlanetsInSystem.iterator();
            while (checkDistancesOfPlanets.hasNext()) {
                SectorEntityToken entity = (SectorEntityToken) checkDistancesOfPlanets.next();
                if (closestPlanet == null) {
                    closestPlanet = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestPlanet, playerFleet)) {
                    closestPlanet = entity;
                }
            }

            return closestPlanet;
        }
    }

    public static String getPlanetType(PlanetAPI planet) {
        //Sets the spec planet type, but not the actual planet type. Need the API fix from Alex to correct this.
        //All code should rely on this function to get the planet type so it should work without bugs.
        //String planetType = planet.getTypeId();
        String planetType = planet.getSpec().getPlanetType();

        if (planetType.equals("nebula_center_old") || planetType.equals("nebula_center_average") || planetType.equals("nebula_center_young") || planetType.equals("star_neutron") || planetType.equals("black_hole") || planetType.equals("star_yellow") || planetType.equals("star_white") || planetType.equals("star_blue_giant") || planetType.equals("star_blue_supergiant") || planetType.equals("star_orange") || planetType.equals("star_orange_giant") || planetType.equals("star_red_supergiant") || planetType.equals("star_red_giant") || planetType.equals("star_red_dwarf") || planetType.equals("star_browndwarf") || planetType.equals("US_star_blue_giant") || planetType.equals("US_star_yellow") || planetType.equals("US_star_orange") || planetType.equals("US_star_red_giant") || planetType.equals("US_star_white") || planetType.equals("US_star_browndwarf") || planetType.equals("SCY_star") || planetType.equals("SCY_companionStar") || planetType.equals("SCY_wormholeUnder") || planetType.equals("SCY_wormholeA") || planetType.equals("SCY_wormholeB") || planetType.equals("SCY_wormholeC") || planetType.equals("istl_sigmaworld") || planetType.equals("istl_dysonshell") || planetType.equals("vayra_star_blue") || planetType.equals("vayra_star_brown") || planetType.equals("vayra_star_yellow_white")) {
            return "star";
        } else if (planetType.equals("gas_giant") || planetType.equals("ice_giant") || planetType.equals("US_gas_giant") || planetType.equals("US_gas_giantB") || planetType.equals("fds_gas_giant") || planetType.equals("SCY_tartarus") || planetType.equals("galaxytigers_gas_giant")) {
            return "gas_giant";
        } else if (planetType.equals("barren") || planetType.equals("barren_castiron") || planetType.equals("barren2") || planetType.equals("barren3") || planetType.equals("barren_venuslike") || planetType.equals("rocky_metallic") || planetType.equals("rocky_unstable") || planetType.equals("rocky_ice") || planetType.equals("irradiated") || planetType.equals("barren-bombarded") || planetType.equals("US_acid") || planetType.equals("US_acidRain") || planetType.equals("US_acidWind") || planetType.equals("US_barrenA") || planetType.equals("US_barrenB") || planetType.equals("US_barrenC") || planetType.equals("US_barrenD") || planetType.equals("US_barrenE") || planetType.equals("US_barrenF") || planetType.equals("US_azure") || planetType.equals("US_burnt") || planetType.equals("US_artificial") || planetType.equals("haunted") || planetType.equals("hmi_crystalline") || planetType.equals("SCY_miningColony") || planetType.equals("SCY_burntPlanet") || planetType.equals("SCY_moon") || planetType.equals("SCY_redRock")) {
            return "barren";
        } else if (planetType.equals("toxic") || planetType.equals("toxic_cold") || planetType.equals("US_green") || planetType.equals("SCY_acid")) {
            return "toxic";
        } else if (planetType.equals("desert") || planetType.equals("desert1") || planetType.equals("arid") || planetType.equals("barren-desert") || planetType.equals("US_dust") || planetType.equals("US_desertA") || planetType.equals("US_desertB") || planetType.equals("US_desertC") || planetType.equals("US_red") || planetType.equals("US_redWind") || planetType.equals("US_lifelessArid") || planetType.equals("US_arid") || planetType.equals("US_crimson") || planetType.equals("US_storm") || planetType.equals("fds_desert") || planetType.equals("SCY_homePlanet") || planetType.equals("istl_aridbread") || planetType.equals("vayra_bread") || planetType.equals("US_auric") || planetType.equals("US_auricCloudy")) {
            return "desert";
        } else if (planetType.equals("terran") || planetType.equals("terran-eccentric") || planetType.equals("US_lifeless") || planetType.equals("US_alkali") || planetType.equals("US_continent") || planetType.equals("US_magnetic") || planetType.equals("US_water") || planetType.equals("US_waterB") || planetType.equals("boggled_paradise")) {
            return "terran";
        } else if (planetType.equals("water")) {
            return "water";
        } else if (planetType.equals("tundra") || planetType.equals("US_purple") || planetType.equals("fds_tundra") || planetType.equals("galaxytigers_tundra")) {
            return "tundra";
        } else if (planetType.equals("jungle") || planetType.equals("US_jungle")) {
            return "jungle";
        } else if (planetType.equals("frozen") || planetType.equals("frozen1") || planetType.equals("frozen2") || planetType.equals("frozen3") || planetType.equals("cryovolcanic") || planetType.equals("US_iceA") || planetType.equals("US_iceB") || planetType.equals("US_blue") || planetType.equals("fds_cryovolcanic") || planetType.equals("fds_frozen")) {
            return "frozen";
        } else if (planetType.equals("lava") || planetType.equals("lava_minor") || planetType.equals("US_lava") || planetType.equals("US_volcanic") || planetType.equals("fds_lava")) {
            return "volcanic";
        } else {
            return "unknown";
        }
    }

    public static boolean marketIsStation(MarketAPI market)
    {
        if(market.getPrimaryEntity() == null || market.getPlanetEntity() == null || market.getPrimaryEntity().hasTag("station"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean terraformingPossibleOnMarket(MarketAPI market)
    {
        if(marketIsStation(market))
        {
            return false;
        }

        if(market.hasCondition("irradiated"))
        {
            return false;
        }

        String planetType = boggledTools.getPlanetType(market.getPlanetEntity());
        if(planetType.equals("star") || planetType.equals("gas_giant") || planetType.equals("volcanic") || planetType.equals("unknown"))
        {
            return false;
        }

        return true;
    }

    public static boolean getCreateMirrorsOrShades(MarketAPI market)
    {
        //Return true for mirrors, false for shades
        //Go by temperature first. If not triggered, will check planet type. Otherwise, just return true.

        if(market.hasCondition("poor_light") || market.hasCondition("very_cold") || market.hasCondition("cold"))
        {
            return true;
        }
        else if(market.hasCondition("very_hot") || market.hasCondition("hot"))
        {
            return false;
        }

        if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("desert") || boggledTools.getPlanetType(market.getPlanetEntity()).equals("jungle"))
        {
            return false;
        }
        else if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("tundra") || boggledTools.getPlanetType(market.getPlanetEntity()).equals("frozen"))
        {
            return true;
        }

        return true;
    }

    public static boolean hasEuteckImprovableConditions(PlanetAPI planet)
    {
        if(planet.getMarket().hasCondition("US_storm") || planet.getMarket().hasCondition("no_atmosphere") || planet.getMarket().hasCondition("thin_atmosphere") || planet.getMarket().hasCondition("toxic_atmosphere") || planet.getMarket().hasCondition("dense_atmosphere") || planet.getMarket().hasCondition("extreme_weather") || planet.getMarket().hasCondition("irradiated") || planet.getMarket().hasCondition("inimical_biosphere") || planet.getMarket().hasCondition("water_surface") || planet.getMarket().hasCondition("meteor_impacts") || planet.getMarket().hasCondition("pollution") || planet.getMarket().hasCondition("very_hot") || planet.getMarket().hasCondition("very_cold"))
        {
            return true;
        }
        else if(!planet.getMarket().hasCondition("habitable") || !planet.getMarket().hasCondition("farmland_bountiful") || !(planet.getMarket().hasCondition("organics_common") || planet.getMarket().hasCondition("organics_abundant") || planet.getMarket().hasCondition("organics_plentiful")))
        {
            return true;
        }

        return false;
    }

    public static SectorEntityToken getFocusOfAsteroidBelt(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_belt"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        return entity.getOrbitFocus();
                    }
                }
            }
        }

        return null;
    }

    public static OrbitAPI getAsteroidFieldOrbit(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_field"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        AsteroidFieldTerrainPlugin asteroidPlugin = (AsteroidFieldTerrainPlugin)terrain.getPlugin();
                        return asteroidPlugin.getEntity().getOrbit();
                    }
                }
            }
        }

        return null;
    }

    public static SectorEntityToken getAsteroidFieldEntity(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_field"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        AsteroidFieldTerrainPlugin asteroidPlugin = (AsteroidFieldTerrainPlugin)terrain.getPlugin();
                        return asteroidPlugin.getEntity();
                    }
                }
            }
        }

        return null;
    }

    public static boolean playerFleetInAsteroidBelt(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_belt"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean playerFleetInAsteroidField(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_field"))
                {
                    if(terrain.getPlugin().containsEntity(playerFleet))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean playerFleetTooCloseToJumpPoint(SectorEntityToken playerFleet)
    {
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof JumpPointAPI && getDistanceBetweenTokens(playerFleet, entity) < 300f)
            {
                return true;
            }
        }

        return false;
    }

    public static Integer getNumAsteroidTerrainsInSystem(SectorEntityToken playerFleet)
    {
        Integer numRoids = 0;
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_field") || terrainID.equals("asteroid_belt"))
                {
                    numRoids++;
                }
            }
        }

        return numRoids;
    }

    public static Integer getNumAsteroidBeltsInSystem(SectorEntityToken playerFleet)
    {
        Integer numBelts = 0;
        Iterator allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity instanceof CampaignTerrainAPI)
            {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI)entity;
                String terrainID = terrain.getPlugin().getTerrainId();

                if(terrainID.equals("asteroid_belt"))
                {
                    numBelts++;
                }
            }
        }

        return numBelts;
    }

    public static String getMiningStationResourceString(Integer numAsteroidTerrains)
    {
        if(numAsteroidTerrains <= 1)
        {
            return "sparse";
        }
        else if(numAsteroidTerrains < 5)
        {
            return "moderate";
        }
        else if(numAsteroidTerrains < 9)
        {
            return "abundant";
        }
        else if(numAsteroidTerrains < 14)
        {
            return "rich";
        }
        else
        {
            return "ultrarich";
        }
    }

    public static int getNumberOfStationExpansions(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if (tag.contains("boggled_station_construction_numExpansions_"))
            {
                return Integer.parseInt(tag.substring(tag.length() - 1));
            }
        }

        return 0;
    }

    public static void incrementNumberOfStationExpansions(MarketAPI market)
    {
        if(getNumberOfStationExpansions(market) == 0)
        {
            market.addTag("boggled_station_construction_numExpansions_1");
        }
        else
        {
            int numExpansionsOld = getNumberOfStationExpansions(market);
            market.removeTag("boggled_station_construction_numExpansions_" + numExpansionsOld);
            market.addTag("boggled_station_construction_numExpansions_" + (numExpansionsOld + 1));
        }
    }

    public static float randomOrbitalAngleFloat()
    {
        Random rand = new Random();
        return rand.nextFloat() * (360f);
    }

    public static void refreshAquacultureAndFarming(MarketAPI market)
    {
        if(market.hasTag("station") || market.getPrimaryEntity().hasTag("station") || market.getPlanetEntity() == null)
        {
            return;
        }
        else
        {
            if(market.hasCondition("water_surface") && market.hasIndustry("farming"))
            {
                market.getIndustry("farming").init("aquaculture", market);
            }
            else if(market.hasIndustry("aquaculture") && !market.hasCondition("water_surface"))
            {
                market.getIndustry("aquaculture").init("farming", market);
            }
        }
    }

    public static boolean playerTooClose(StarSystemAPI system)
    {
        return Global.getSector().getPlayerFleet().isInOrNearSystem(system);
    }

    public static void clearConnectedPlanets(MarketAPI market)
    {
        Iterator removePlanets = market.getConnectedEntities().iterator();
        SectorEntityToken targetEntityToRemove = null;

        while(removePlanets.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)removePlanets.next();
            if (entity instanceof PlanetAPI && !entity.hasTag("station"))
            {
                targetEntityToRemove = entity;
            }
        }

        removePlanets = null;
        if(targetEntityToRemove != null)
        {
            market.getConnectedEntities().remove(targetEntityToRemove);
            clearConnectedPlanets(market);
        }
    }

    public static void clearConnectedStations(MarketAPI market)
    {
        Iterator removeStations = market.getConnectedEntities().iterator();
        SectorEntityToken targetEntityToRemove = null;

        while(removeStations.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)removeStations.next();
            if (entity.hasTag("station"))
            {
                targetEntityToRemove = entity;
            }
        }

        removeStations = null;
        if(targetEntityToRemove != null)
        {
            market.getConnectedEntities().remove(targetEntityToRemove);
            clearConnectedStations(market);
        }
    }

    public static int numReflectorsInOrbit(MarketAPI market)
    {
        int numReflectors = 0;
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.getId().contains("stellar_shade") || entity.hasTag("stellar_mirror") || entity.hasTag("stellar_shade")))
            {
                numReflectors++;
            }
        }

        return numReflectors;
    }

    public static int numMirrorsInOrbit(MarketAPI market)
    {
        int numMirrors = 0;
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.hasTag("stellar_mirror")))
            {
                numMirrors++;
            }
        }

        return numMirrors;
    }

    public static int numShadesInOrbit(MarketAPI market)
    {
        int numShades = 0;
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_shade") || entity.hasTag("stellar_shade")))
            {
                numShades++;
            }
        }

        return numShades;
    }

    public static void clearReflectorsInOrbit(MarketAPI market)
    {
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();

        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.getId().contains("stellar_shade") || entity.hasTag("stellar_mirror") || entity.hasTag("stellar_shade")))
            {
                allEntitiesInSystem.remove();
                market.getStarSystem().removeEntity(entity);
            }
        }
    }

    public static boolean hasIsmaraSling(MarketAPI market)
    {
        Iterator marketsInSystem = Global.getSector().getEconomy().getMarkets(market.getStarSystem()).iterator();
        while(marketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)marketsInSystem.next();
            if(marketElement.getFactionId().equals(market.getFactionId()) && marketElement.hasIndustry("BOGGLED_ISMARA_SLING") && marketElement.getIndustry("BOGGLED_ISMARA_SLING").isFunctional())
            {
                return true;
            }
        }

        return false;
    }

    public static void terraformHabitableToParadise(MarketAPI market)
    {
        String newPlanetType = "boggled_paradise";
        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator allSpecs = Global.getSettings().getAllPlanetSpecs().iterator();
        while(allSpecs.hasNext())
        {
            PlanetSpecAPI spec = (PlanetSpecAPI)allSpecs.next();
            if (spec.getPlanetType().equals(newPlanetType))
            {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());
                myspec.setGlowTexture(spec.getGlowTexture());
                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();
        market.getPlanetEntity().setTypeId("boggled_paradise");

        removeCondition(market, "US_storm");
        removeCondition(market, "US_virus");
        removeCondition(market, "US_mind");
        removeCondition(market, "US_shrooms");

        removeCondition(market, "extreme_weather");
        addCondition(market, "mild_climate");

        removeCondition(market, "thin_atmosphere");
        removeCondition(market, "dense_atmosphere");
        removeCondition(market, "toxic_atmosphere");

        removeCondition(market, "farmland_poor");
        removeCondition(market, "farmland_adequate");
        removeCondition(market, "farmland_rich");
        addCondition(market, "farmland_bountiful");

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void terraformBasedOnProjectType(MarketAPI market, String projectType)
    {
        if(projectType.contains("transformation"))
        {
            String newPlanetType = null;

            if(projectType.equals("Terran transformation"))
            {
                newPlanetType = "terran";

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                removeCondition(market, "very_hot");
                removeCondition(market, "hot");
                removeCondition(market, "cold");
                removeCondition(market, "very_cold");

                addCondition(market, "habitable");

                removeCondition(market, "extreme_weather");
                addCondition(market, "mild_climate");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                addCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }
            else if(projectType.equals("Water transformation"))
            {
                newPlanetType = "water";

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                removeCondition(market, "very_hot");
                removeCondition(market, "hot");
                removeCondition(market, "cold");
                removeCondition(market, "very_cold");

                addCondition(market, "habitable");

                removeCondition(market, "extreme_weather");
                addCondition(market, "mild_climate");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");
                addCondition(market, "water_surface");
            }
            else if(projectType.equals("Jungle transformation"))
            {
                newPlanetType = "jungle";

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                removeCondition(market, "very_hot");
                addCondition(market, "hot");
                removeCondition(market, "cold");
                removeCondition(market, "very_cold");

                addCondition(market, "habitable");

                removeCondition(market, "extreme_weather");
                addCondition(market, "mild_climate");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                addCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }
            else if(projectType.equals("Arid transformation"))
            {
                newPlanetType = "arid";

                removeCondition(market, "very_hot");
                addCondition(market, "hot");
                removeCondition(market, "cold");
                removeCondition(market, "very_cold");

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                addCondition(market, "habitable");

                removeCondition(market, "extreme_weather");
                addCondition(market, "mild_climate");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                addCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }
            else if(projectType.equals("Tundra transformation"))
            {
                newPlanetType = "tundra";

                removeCondition(market, "very_hot");
                removeCondition(market, "hot");
                addCondition(market, "cold");
                removeCondition(market, "very_cold");

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                addCondition(market, "habitable");

                removeCondition(market, "extreme_weather");
                addCondition(market, "mild_climate");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                addCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }
            else if(projectType.equals("Volcanic transformation"))
            {
                newPlanetType = "lava";

                addCondition(market, "very_hot");
                removeCondition(market, "hot");
                removeCondition(market, "cold");
                removeCondition(market, "very_cold");

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                removeCondition(market, "extreme_weather");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }
            else if(projectType.equals("Frozen transformation"))
            {
                newPlanetType = "frozen";

                removeCondition(market, "very_hot");
                removeCondition(market, "hot");
                removeCondition(market, "cold");
                addCondition(market, "very_cold");

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                removeCondition(market, "extreme_weather");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }
            else if(projectType.equals("Auric transformation"))
            {
                newPlanetType = "US_auric";

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                removeCondition(market, "very_hot");
                removeCondition(market, "hot");
                removeCondition(market, "cold");
                removeCondition(market, "very_cold");

                addCondition(market, "habitable");

                removeCondition(market, "extreme_weather");
                addCondition(market, "mild_climate");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                addCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }
            else if(projectType.equals("Continental transformation"))
            {
                newPlanetType = "US_continent";

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                removeCondition(market, "very_hot");
                removeCondition(market, "hot");
                removeCondition(market, "cold");
                removeCondition(market, "very_cold");

                addCondition(market, "habitable");

                removeCondition(market, "extreme_weather");
                addCondition(market, "mild_climate");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                addCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }
            else if(projectType.equals("Artificial transformation"))
            {
                newPlanetType = "US_artificial";

                removeCondition(market, "US_storm");
                removeCondition(market, "US_virus");

                removeCondition(market, "very_hot");
                removeCondition(market, "hot");
                removeCondition(market, "cold");
                removeCondition(market, "very_cold");

                addCondition(market, "US_artificial");
                addCondition(market, "habitable");

                removeCondition(market, "extreme_weather");
                addCondition(market, "mild_climate");

                removeCondition(market, "no_atmosphere");
                removeCondition(market, "thin_atmosphere");
                removeCondition(market, "dense_atmosphere");
                removeCondition(market, "toxic_atmosphere");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "water_surface");
            }

            if(newPlanetType != null)
            {
                PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
                Iterator allSpecs = Global.getSettings().getAllPlanetSpecs().iterator();
                while(allSpecs.hasNext())
                {
                    PlanetSpecAPI spec = (PlanetSpecAPI)allSpecs.next();
                    if (spec.getPlanetType().equals(newPlanetType))
                    {
                        myspec.setAtmosphereColor(spec.getAtmosphereColor());
                        myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                        myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                        myspec.setCloudColor(spec.getCloudColor());
                        myspec.setCloudRotation(spec.getCloudRotation());
                        myspec.setCloudTexture(spec.getCloudTexture());
                        myspec.setGlowColor(spec.getGlowColor());
                        myspec.setGlowTexture(spec.getGlowTexture());
                        myspec.setIconColor(spec.getIconColor());
                        myspec.setPlanetColor(spec.getPlanetColor());
                        myspec.setStarscapeIcon(spec.getStarscapeIcon());
                        myspec.setTexture(spec.getTexture());
                        myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                        ((PlanetSpec)myspec).planetType = newPlanetType;
                        ((PlanetSpec)myspec).name = spec.getName();
                        ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                        break;
                    }
                }
                market.getPlanetEntity().applySpecChanges();
                market.getPlanetEntity().setTypeId(newPlanetType);
            }
        }
        else if(projectType.equals("Make world habitable"))
        {
            addCondition(market, "habitable");
        }
        else if(projectType.equals("Optimize atmospheric conditions"))
        {
            addCondition(market, "habitable");

            removeCondition(market, "extreme_weather");
            addCondition(market, "mild_climate");

            removeCondition(market, "no_atmosphere");
            removeCondition(market, "thin_atmosphere");
            removeCondition(market, "dense_atmosphere");
            removeCondition(market, "toxic_atmosphere");

            removeCondition(market, "very_hot");
            removeCondition(market, "hot");
            removeCondition(market, "cold");
            removeCondition(market, "very_cold");
        }
        else if(projectType.equals("Optimize gravity and tectonics"))
        {
            removeCondition(market, "low_gravity");
            removeCondition(market, "high_gravity");
            removeCondition(market, "tectonic_activity");
            removeCondition(market, "extreme_tectonic_activity");
        }
        else if(projectType.equals("Remove inimical biosphere, radiation and meteor impacts"))
        {
            removeCondition(market, "inimical_biosphere");
            removeCondition(market, "irradiated");
            removeCondition(market, "meteor_impacts");
        }
        else if(projectType.equals("Remove rare ore deposits"))
        {
            removeCondition(market, "rare_ore_sparse");
            removeCondition(market, "rare_ore_moderate");
            removeCondition(market, "rare_ore_abundant");
            removeCondition(market, "rare_ore_rich");
            removeCondition(market, "rare_ore_ultrarich");
        }
        else if(projectType.equals("Maximize all resources"))
        {
            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            addCondition(market, "farmland_bountiful");

            removeCondition(market, "ore_sparse");
            removeCondition(market, "ore_moderate");
            removeCondition(market, "ore_abundant");
            removeCondition(market, "ore_rich");
            addCondition(market, "ore_ultrarich");

            removeCondition(market, "rare_ore_sparse");
            removeCondition(market, "rare_ore_moderate");
            removeCondition(market, "rare_ore_abundant");
            removeCondition(market, "rare_ore_rich");
            addCondition(market, "rare_ore_ultrarich");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            addCondition(market, "volatiles_plentiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            addCondition(market, "organics_plentiful");

            removeCondition(market, "ruins_scattered");
            removeCondition(market, "ruins_widespread");
            removeCondition(market, "ruins_extensive");
            addCondition(market, "ruins_vast");
        }
        else if(projectType.equals("Remove all Unknown Skies conditions"))
        {
            removeCondition(market, "US_magnetic");
            removeCondition(market, "US_artificial");
            removeCondition(market, "US_storm");
            removeCondition(market, "US_virus");
            removeCondition(market, "US_religious");
            removeCondition(market, "US_base");
            removeCondition(market, "US_floating");
            removeCondition(market, "US_crash");
            removeCondition(market, "US_elevator");
            removeCondition(market, "US_shrooms");
            removeCondition(market, "US_tunnels");
            removeCondition(market, "US_mind");
            removeCondition(market, "US_bedrock");
        }

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void terraformVariantToTerran(MarketAPI market)
    {
        String newPlanetType = "terran";
        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext()) {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType)) {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());
                myspec.setGlowTexture(spec.getGlowTexture());
                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();
        market.getPlanetEntity().setTypeId("terran");

        if(market.hasCondition("US_storm"))
        {
            market.removeCondition("US_storm");
        }

        if(market.hasCondition("extreme_weather"))
        {
            market.removeCondition("extreme_weather");
        }

        if(market.hasCondition("farmland_poor"))
        {
            market.removeCondition("farmland_poor");
        }

        if(market.hasCondition("farmland_adequate"))
        {
            market.removeCondition("farmland_adequate");
        }

        if(market.hasCondition("farmland_rich"))
        {
            market.removeCondition("farmland_rich");
        }

        if(!market.hasCondition("farmland_poor") && !market.hasCondition("farmland_adequate") && !market.hasCondition("farmland_rich") && !market.hasCondition("farmland_bountiful"))
        {
            market.addCondition("farmland_bountiful");
        }

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void terraformFrozenToWater(MarketAPI market)
    {
        String newPlanetType = "water";
        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext()) {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType)) {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());
                myspec.setGlowTexture(spec.getGlowTexture());
                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();
        market.getPlanetEntity().setTypeId("water");

        if(!market.hasCondition("water_surface"))
        {
            market.addCondition("water_surface");
        }

        if(market.hasCondition("US_storm"))
        {
            market.removeCondition("US_storm");
        }

        if(market.hasCondition("extreme_weather"))
        {
            market.removeCondition("extreme_weather");
        }

        if(market.hasCondition("farmland_poor"))
        {
            market.removeCondition("farmland_poor");
        }

        if(market.hasCondition("farmland_adequate"))
        {
            market.removeCondition("farmland_adequate");
        }

        if(market.hasCondition("farmland_rich"))
        {
            market.removeCondition("farmland_rich");
        }

        if(market.hasCondition("farmland_bountiful"))
        {
            market.removeCondition("farmland_bountiful");
        }

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void deterraform(MarketAPI market)
    {
        String deterraformTypeID = "null";
        Iterator tags = market.getTags().iterator();

        while(tags.hasNext())
        {
            String tag = (String)tags.next();
            if (tag.length() > 30 && tag.substring(0,29).equals("boggled_original_planet_type_"))
            {
                deterraformTypeID = tag.substring(29);
            }
        }

        String newPlanetType = deterraformTypeID;
        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext()) {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType)) {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());
                myspec.setGlowTexture(spec.getGlowTexture());
                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();

        if(market.hasCondition("water_surface"))
        {
            market.removeCondition("water_surface");
        }

        if(!market.hasCondition("extreme_weather") && !market.hasCondition("mild_climate"))
        {
            market.addCondition("extreme_weather");
        }

        if(market.hasCondition("habitable"))
        {
            market.removeCondition("habitable");
        }

        if(market.hasCondition("farmland_poor"))
        {
            market.removeCondition("farmland_poor");
        }

        if(market.hasCondition("farmland_adequate"))
        {
            market.removeCondition("farmland_adequate");
        }

        if(market.hasCondition("farmland_rich"))
        {
            market.removeCondition("farmland_rich");
        }

        if(market.hasCondition("farmland_bountiful"))
        {
            market.removeCondition("farmland_bountiful");
        }

        surveyAll(market);
        refreshAquacultureAndFarming(market);
        refreshSupplyAndDemand(market);

        market.removeTag("boggled_terraformed");
        market.removeTag("boggled_original_planet_type_" + deterraformTypeID);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Regressed to a " + deterraformTypeID + " planet");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }

        market.removeCondition("deterraforming_tracker");
    }

    public static void swapStationSprite(SectorEntityToken station, String stationType, String stationGreekLetter, int targetSize)
    {
        MarketAPI market = station.getMarket();
        StarSystemAPI system = market.getStarSystem();
        OrbitAPI orbit = null;
        if(station.getOrbit() != null)
        {
            orbit = station.getOrbit();
        }
        CampaignClockAPI clock = Global.getSector().getClock();
        SectorEntityToken newStation = null;
        SectorEntityToken newStationLights = null;

        String size = "null";
        if(targetSize == 1)
        {
            size = "small";
        }
        else if(targetSize == 2)
        {
            size = "medium";
        }
        else if(targetSize == 3)
        {
            size = "large";
        }

        if(size.equals("null"))
        {
            //Do nothing if an erroneous size value was passed.
            return;
        }

        if(stationType.equals("astropolis"))
        {
            newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + stationGreekLetter + "_" + size, market.getFactionId());
            newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + stationGreekLetter + "_" + size + "_lights_overlay", market.getFactionId());
        }
        else if(stationType.equals("mining"))
        {
            newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
            //We can't tell which lights overlay to delete earlier because there could be multiple mining stations in a single system.
            //Therefore we delete them all earlier, then recreate them all later.
        }
        else if(stationType.equals("siphon"))
        {
            newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
            newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + size + "_lights_overlay", market.getFactionId());
        }
        else if(stationType.equals("gatekeeper"))
        {
            newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
            newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + size + "_lights_overlay", market.getFactionId());
        }
        else
        {
            //Do nothing because the station type is unrecognized
            return;
        }

        if(newStation == null)
        {
            //Failed to create a new station likely because of erroneous passed values. Do nothing.
            return;
        }

        newStation.setContainingLocation(station.getContainingLocation());
        if(newStationLights != null)
        {
            newStationLights.setContainingLocation(station.getContainingLocation());
        }

        if(orbit != null)
        {
            newStation.setOrbit(orbit);
            if(newStationLights != null)
            {
                newStationLights.setOrbit(newStation.getOrbit().makeCopy());
            }
        }
        newStation.setMemory(station.getMemory());
        newStation.setFaction(market.getFactionId());
        station.setCircularOrbit(newStation, 0, 0, 1);

        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station))
            {
                if (entity.getOrbit().getClass().equals(CircularFleetOrbit.class))
                {
                    ((CircularFleetOrbit)entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbit.class))
                {
                    ((CircularOrbit)entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbitPointDown.class))
                {
                    ((CircularOrbitPointDown)entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbitWithSpin.class))
                {
                    ((CircularOrbitWithSpin)entity.getOrbit()).setFocus(newStation);
                }
            }
        }

        //Deletes the old station. May cause limited issues related to ships orbiting the old location
        clearConnectedStations(market);
        system.removeEntity(station);

        newStation.setMarket(market);
        market.setPrimaryEntity(newStation);

        surveyAll(market);
        refreshSupplyAndDemand(market);
    }

    public static void deleteOldLightsOverlay(SectorEntityToken station, String stationType, String stationGreekLetter)
    {
        StarSystemAPI system = station.getStarSystem();
        OrbitAPI orbit = null;
        if(station.getOrbit() != null)
        {
            orbit = station.getOrbit();
        }

        if(stationType.equals("astropolis"))
        {
            SectorEntityToken targetTokenToDelete = null;

            if(stationGreekLetter.equals("alpha"))
            {
                Iterator allEntitiesInSystem = system.getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                    if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_astropolis_alpha_small") || entity.hasTag("boggled_lights_overlay_astropolis_alpha_medium") || entity.hasTag("boggled_lights_overlay_astropolis_alpha_large")))
                    {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                allEntitiesInSystem = null;

                if(targetTokenToDelete != null)
                {
                    system.removeEntity(targetTokenToDelete);
                    deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                }
            }
            else if(stationGreekLetter.equals("beta"))
            {
                Iterator allEntitiesInSystem = system.getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                    if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_astropolis_beta_small") || entity.hasTag("boggled_lights_overlay_astropolis_beta_medium") || entity.hasTag("boggled_lights_overlay_astropolis_beta_large")))
                    {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                allEntitiesInSystem = null;

                if(targetTokenToDelete != null)
                {
                    system.removeEntity(targetTokenToDelete);
                    deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                }
            }
            else if(stationGreekLetter.equals("gamma"))
            {
                Iterator allEntitiesInSystem = system.getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                    if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_astropolis_gamma_small") || entity.hasTag("boggled_lights_overlay_astropolis_gamma_medium") || entity.hasTag("boggled_lights_overlay_astropolis_gamma_large")))
                    {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                allEntitiesInSystem = null;

                if(targetTokenToDelete != null)
                {
                    system.removeEntity(targetTokenToDelete);
                    deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                }
            }
        }
        else if(stationType.equals("mining"))
        {
            SectorEntityToken targetTokenToDelete = null;

            Iterator allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if(entity.hasTag("boggled_lights_overlay_mining_small") || entity.hasTag("boggled_lights_overlay_mining_medium"))
                {
                    targetTokenToDelete = entity;
                    break;
                }
            }
            allEntitiesInSystem = null;

            if(targetTokenToDelete != null)
            {
                system.removeEntity(targetTokenToDelete);
                deleteOldLightsOverlay(station, stationType, stationGreekLetter);
            }
        }
        else if(stationType.equals("siphon"))
        {
            SectorEntityToken targetTokenToDelete = null;

            Iterator allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_siphon_small") || entity.hasTag("boggled_lights_overlay_siphon_medium")))
                {
                    targetTokenToDelete = entity;
                    break;
                }
            }
            allEntitiesInSystem = null;

            if(targetTokenToDelete != null)
            {
                system.removeEntity(targetTokenToDelete);
                deleteOldLightsOverlay(station, stationType, stationGreekLetter);
            }
        }
        else if(stationType.equals("gatekeeper"))
        {
            SectorEntityToken targetTokenToDelete = null;

            Iterator allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if(entity.getOrbitFocus() != null && entity.getOrbitFocus().hasTag("boggled_astral_gate") && (entity.hasTag("boggled_lights_overlay_gatekeeper_small") || entity.hasTag("boggled_lights_overlay_gatekeeper_medium")))
                {
                    targetTokenToDelete = entity;
                    break;
                }
            }
            allEntitiesInSystem = null;

            if(targetTokenToDelete != null)
            {
                system.removeEntity(targetTokenToDelete);
                deleteOldLightsOverlay(station, stationType, stationGreekLetter);
            }
        }
        else
        {
            //Do nothing because the station type is unrecognized
            return;
        }
    }

    public static void reapplyMiningStationLights(StarSystemAPI system)
    {
        SectorEntityToken stationToApplyOverlayTo = null;
        int stationsize = 0;

        Iterator allEntitiesInSystem = system.getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if(entity.hasTag("boggled_mining_station_small") && !entity.hasTag("boggled_already_reapplied_lights_overlay"))
            {
                stationToApplyOverlayTo = entity;
                stationsize = 1;
                entity.addTag("boggled_already_reapplied_lights_overlay");
                break;
            }
            else if(entity.hasTag("boggled_mining_station_medium") && !entity.hasTag("boggled_already_reapplied_lights_overlay"))
            {
                stationToApplyOverlayTo = entity;
                stationsize = 2;
                entity.addTag("boggled_already_reapplied_lights_overlay");
                break;
            }
        }
        allEntitiesInSystem = null;

        if(stationToApplyOverlayTo != null)
        {
            if(stationsize == 1)
            {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals("neutral"))
                {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_small_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            }
            else if(stationsize == 2)
            {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals("neutral"))
                {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_medium_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            }
        }
        else
        {
            allEntitiesInSystem = system.getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
                if(entity.hasTag("boggled_already_reapplied_lights_overlay"))
                {
                    entity.removeTag("boggled_already_reapplied_lights_overlay");
                }
            }
        }
    }

    public static boolean marketHasOrbitalStation(MarketAPI market)
    {
        Iterator allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = (SectorEntityToken) allEntitiesInSystem.next();
            if(entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && entity.hasTag("station"))
            {
                return true;
            }
        }

        return false;
    }

    public static int getMaxFarmlandForMarket(MarketAPI market)
    {
        // Returns 0 if the planet can't have farmland
        // Returns 1 through 4 for the levels of farmland, with 1 being poor and 4 being bountiful

        PlanetAPI planet = market.getPlanetEntity();

        if(getPlanetType(planet).equals("star") || getPlanetType(planet).equals("gas_giant") || getPlanetType(planet).equals("barren") || getPlanetType(planet).equals("toxic") || getPlanetType(planet).equals("volcanic") || getPlanetType(planet).equals("frozen") || getPlanetType(planet).equals("water") || getPlanetType(planet).equals("unknown"))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals("jungle") || getPlanetType(planet).equals("desert") || getPlanetType(planet).equals("terran") || getPlanetType(planet).equals("tundra"))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static int getCurrentFarmlandForMarket(MarketAPI market)
    {
        // Returns 0 if the planet has no farmland
        // Returns 1 through 4 for the levels of farmland, with 1 being poor and 4 being bountiful

        if(market.hasCondition("farmland_poor"))
        {
            return 1;
        }
        else if(market.hasCondition("farmland_adequate"))
        {
            return 2;
        }
        else if(market.hasCondition("farmland_rich"))
        {
            return 3;
        }
        else if(market.hasCondition("farmland_bountiful"))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static int getMaxOrganicsForMarket(MarketAPI market)
    {
        // Returns 0 if the planet can't have organics
        // Returns 1 through 4 for the levels of organics, with 1 being trace and 4 being plentiful

        PlanetAPI planet = market.getPlanetEntity();

        if(getPlanetType(planet).equals("star") || getPlanetType(planet).equals("gas_giant") || getPlanetType(planet).equals("barren") || getPlanetType(planet).equals("toxic") || getPlanetType(planet).equals("volcanic") || getPlanetType(planet).equals("frozen") || getPlanetType(planet).equals("unknown"))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals("water") || getPlanetType(planet).equals("jungle") || getPlanetType(planet).equals("terran"))
        {
            return 4;
        }
        else if(getPlanetType(planet).equals("desert"))
        {
            return 3;
        }
        else if(getPlanetType(planet).equals("tundra"))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static int getCurrentOrganicsForMarket(MarketAPI market)
    {
        // Returns 0 if the planet has no organics
        // Returns 1 through 4 for the levels of organics, with 1 being trace and 4 being plentiful

        if(market.hasCondition("organics_trace"))
        {
            return 1;
        }
        else if(market.hasCondition("organics_common"))
        {
            return 2;
        }
        else if(market.hasCondition("organics_abundant"))
        {
            return 3;
        }
        else if(market.hasCondition("organics_plentiful"))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static int getMaxVolatilesForMarket(MarketAPI market)
    {
        // Returns 0 if the planet can't have volatiles
        // Returns 1 through 4 for the levels of volatiles, with 1 being trace and 4 being plentiful

        PlanetAPI planet = market.getPlanetEntity();

        if(getPlanetType(planet).equals("star") || getPlanetType(planet).equals("gas_giant") || getPlanetType(planet).equals("barren") || getPlanetType(planet).equals("toxic") || getPlanetType(planet).equals("volcanic") || getPlanetType(planet).equals("jungle") || getPlanetType(planet).equals("unknown"))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals("frozen") || getPlanetType(planet).equals("tundra") || getPlanetType(planet).equals("water"))
        {
            return 4;
        }
        else if(getPlanetType(planet).equals("desert") || getPlanetType(planet).equals("terran"))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static int getCurrentVolatilesForMarket(MarketAPI market)
    {
        // Returns 0 if the planet has no volatiles
        // Returns 1 through 4 for the levels of volatiles, with 1 being trace and 4 being plentiful

        if(market.hasCondition("volatiles_trace"))
        {
            return 1;
        }
        else if(market.hasCondition("volatiles_diffuse"))
        {
            return 2;
        }
        else if(market.hasCondition("volatiles_abundant"))
        {
            return 3;
        }
        else if(market.hasCondition("volatiles_plentiful"))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static void incrementFarmland(MarketAPI market)
    {
        if(market.hasCondition("farmland_poor"))
        {
            boggledTools.removeCondition(market, "farmland_poor");
            boggledTools.addCondition(market, "farmland_adequate");
        }
        else if(market.hasCondition("farmland_adequate"))
        {
            boggledTools.removeCondition(market, "farmland_adequate");
            boggledTools.addCondition(market, "farmland_rich");
        }
        else if(market.hasCondition("farmland_rich"))
        {
            boggledTools.removeCondition(market, "farmland_rich");
            boggledTools.addCondition(market, "farmland_bountiful");
        }
        else if(market.hasCondition("farmland_bountiful"))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, "farmland_poor");
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Farmland improvement on " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void incrementOrganics(MarketAPI market)
    {
        if(market.hasCondition("organics_trace"))
        {
            boggledTools.removeCondition(market, "organics_trace");
            boggledTools.addCondition(market, "organics_common");
        }
        else if(market.hasCondition("organics_common"))
        {
            boggledTools.removeCondition(market, "organics_common");
            boggledTools.addCondition(market, "organics_abundant");
        }
        else if(market.hasCondition("organics_abundant"))
        {
            boggledTools.removeCondition(market, "organics_abundant");
            boggledTools.addCondition(market, "organics_plentiful");
        }
        else if(market.hasCondition("organics_plentiful"))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, "organics_trace");
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Organics improvement on " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void incrementVolatiles(MarketAPI market)
    {
        if(market.hasCondition("volatiles_trace"))
        {
            boggledTools.removeCondition(market, "volatiles_trace");
            boggledTools.addCondition(market, "volatiles_diffuse");
        }
        else if(market.hasCondition("volatiles_diffuse"))
        {
            boggledTools.removeCondition(market, "volatiles_diffuse");
            boggledTools.addCondition(market, "volatiles_abundant");
        }
        else if(market.hasCondition("volatiles_abundant"))
        {
            boggledTools.removeCondition(market, "volatiles_abundant");
            boggledTools.addCondition(market, "volatiles_plentiful");
        }
        else if(market.hasCondition("volatiles_plentiful"))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, "volatiles_trace");
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Volatiles improvement on " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void incrementOreForPlanetCracking(MarketAPI market)
    {
        if(market.hasCondition("ore_sparse"))
        {
            boggledTools.removeCondition(market, "ore_sparse");
            boggledTools.addCondition(market, "ore_moderate");
        }
        else if(market.hasCondition("ore_moderate"))
        {
            boggledTools.removeCondition(market, "ore_moderate");
            boggledTools.addCondition(market, "ore_abundant");
        }
        else if(market.hasCondition("ore_abundant"))
        {
            boggledTools.removeCondition(market, "ore_abundant");
            boggledTools.addCondition(market, "ore_rich");
        }
        else if(market.hasCondition("ore_rich"))
        {
            boggledTools.removeCondition(market, "ore_rich");
            boggledTools.addCondition(market, "ore_ultrarich");
        }
        else if(market.hasCondition("ore_ultrarich"))
        {
            //Do Nothing
        }
        else
        {
            boggledTools.addCondition(market, "ore_sparse");
        }

        if(market.hasCondition("rare_ore_sparse"))
        {
            boggledTools.removeCondition(market, "rare_ore_sparse");
            boggledTools.addCondition(market, "rare_ore_moderate");
        }
        else if(market.hasCondition("rare_ore_moderate"))
        {
            boggledTools.removeCondition(market, "rare_ore_moderate");
            boggledTools.addCondition(market, "rare_ore_abundant");
        }
        else if(market.hasCondition("rare_ore_abundant"))
        {
            boggledTools.removeCondition(market, "rare_ore_abundant");
            boggledTools.addCondition(market, "rare_ore_rich");
        }
        else if(market.hasCondition("rare_ore_rich"))
        {
            boggledTools.removeCondition(market, "rare_ore_rich");
            boggledTools.addCondition(market, "rare_ore_ultrarich");
        }
        else if(market.hasCondition("rare_ore_ultrarich"))
        {
            //Do Nothing
        }
        else
        {
            boggledTools.addCondition(market, "rare_ore_sparse");
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);
    }

    public static void incrementVolatilesForOuyangOptimization(MarketAPI market)
    {
        if(market.hasCondition("volatiles_trace"))
        {
            boggledTools.removeCondition(market, "volatiles_trace");
            boggledTools.addCondition(market, "volatiles_abundant");
        }
        else if(market.hasCondition("volatiles_diffuse"))
        {
            boggledTools.removeCondition(market, "volatiles_diffuse");
            boggledTools.addCondition(market, "volatiles_plentiful");
        }
        else if(market.hasCondition("volatiles_abundant"))
        {
            boggledTools.removeCondition(market, "volatiles_abundant");
            boggledTools.addCondition(market, "volatiles_plentiful");
        }
        else if(market.hasCondition("volatiles_plentiful"))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, "volatiles_diffuse");
        }

        SectorEntityToken closestGasGiantToken = market.getPrimaryEntity();
        if(closestGasGiantToken != null)
        {
            Iterator allEntitiesInSystem = closestGasGiantToken.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if(entity.hasTag("station") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(closestGasGiantToken) && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station") || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station")) && !entity.getId().equals("beholder_station"))
                {
                    if(entity.getMarket() != null)
                    {
                        market = entity.getMarket();
                        if(market.hasCondition("volatiles_trace"))
                        {
                            boggledTools.removeCondition(market, "volatiles_trace");
                            boggledTools.addCondition(market, "volatiles_abundant");
                        }
                        else if(market.hasCondition("volatiles_diffuse"))
                        {
                            boggledTools.removeCondition(market, "volatiles_diffuse");
                            boggledTools.addCondition(market, "volatiles_plentiful");
                        }
                        else if(market.hasCondition("volatiles_abundant"))
                        {
                            boggledTools.removeCondition(market, "volatiles_abundant");
                            boggledTools.addCondition(market, "volatiles_plentiful");
                        }

                        boggledTools.surveyAll(market);
                        boggledTools.refreshSupplyAndDemand(market);
                        boggledTools.refreshAquacultureAndFarming(market);
                    }
                }
            }
        }
    }

    public static SectorEntityToken getPlanetTokenForQuest(String systemID, String entityID)
    {
        StarSystemAPI system = Global.getSector().getStarSystem(systemID);
        if(system != null)
        {
            SectorEntityToken possibleTarget = system.getEntityById(entityID);
            if(possibleTarget != null)
            {
                if(possibleTarget instanceof PlanetAPI)
                {
                    return possibleTarget;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public static boolean playerCloseToAeolus()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        float distanceFromObject = Math.abs(boggledTools.getDistanceBetweenTokens(playerFleet, boggledTools.getPlanetTokenForQuest("Penelope's Star", "penelope4")));
        if(distanceFromObject < 1000 && distanceFromObject > 0 && playerFleet.getStarSystem().equals(getPlanetTokenForQuest("Penelope's Star", "penelope4").getStarSystem()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static SectorEntityToken getClosestValidPlanetSectorEntityTokenInSystem(StarSystemAPI system)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        SectorEntityToken targetToken = null;

        Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(system).iterator();
        while(allMarketsInSystem.hasNext())
        {
            MarketAPI marketElement = (MarketAPI)allMarketsInSystem.next();
            if (marketElement.isPlayerOwned() && marketElement.getPrimaryEntity() != null && marketElement.getPrimaryEntity() instanceof PlanetAPI)
            {
                if(targetToken == null)
                {
                    targetToken = marketElement.getPrimaryEntity();
                }
                else if(boggledTools.getDistanceBetweenTokens(playerFleet, targetToken) > boggledTools.getDistanceBetweenTokens(playerFleet, marketElement.getPrimaryEntity()))
                {
                    targetToken = marketElement.getPrimaryEntity();
                }
            }
        }

        return targetToken;
    }

    public static void clearOldTerraformingProjectTags(MarketAPI market)
    {
        String oldTag = null;
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_project_"))
            {
                oldTag = tag;
                break;
            }
        }
        allTagsOnMarket = null;

        if(oldTag != null)
        {
            market.getPrimaryEntity().removeTag(oldTag);
            boggledTools.clearOldTerraformingProjectTags(market);
        }
    }

    public static void clearOldTerraformingProjectProgressTags(MarketAPI market)
    {
        String oldTag = null;
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_points_"))
            {
                oldTag = tag;
                break;
            }
        }
        allTagsOnMarket = null;

        if(oldTag != null)
        {
            market.getPrimaryEntity().removeTag(oldTag);
            boggledTools.clearOldTerraformingProjectProgressTags(market);
        }
    }

    public static void clearBothTerraformingTags(MarketAPI market)
    {
        clearOldTerraformingProjectTags(market);
        clearOldTerraformingProjectProgressTags(market);
    }

    public static void initiateTerraformingProject(MarketAPI market, String projectType)
    {
        boggledTools.clearBothTerraformingTags(market);

        market.getPrimaryEntity().addTag("boggled_terraforming_project_" + projectType);
        market.getPrimaryEntity().addTag("boggled_terraforming_progress_points_000000");
    }

    public static boolean terraformingProjectAlreadyOngoing(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_project_"))
            {
                return true;
            }
        }

        return false;
    }

    public static String getTerraformingProjectTypeOngoing(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_project_"))
            {
                return tag.replaceAll("boggled_terraforming_project_", "");
            }
        }

        return null;
    }

    public static int getTerraformingProjectProgressPoints(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getPrimaryEntity().getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_points_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_terraforming_progress_points_", ""));
            }
        }

        return 0;
    }

    public static void incrementTerraformingProjectProgressPoints(MarketAPI market, int amount)
    {
        int currentPoints = getTerraformingProjectProgressPoints(market);

        clearOldTerraformingProjectProgressTags(market);

        currentPoints = currentPoints + amount;

        String strPoints = currentPoints + "";

        while(strPoints.length() < 6)
        {
            strPoints = "0" + strPoints;
        }

        market.getPrimaryEntity().addTag("boggled_terraforming_progress_points_" + strPoints);
    }

    public static int getTerraformingProjectPointRequirement(MarketAPI market, String project)
    {
        if(project.contains("transformation"))
        {
            int points = Global.getSettings().getInt("boggledBaseTerraformingPoints");

            // Global points offsets
            if(market.hasCondition("dense_atmosphere"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForDenseAtmo");
            }
            else if(market.hasCondition("thin_atmosphere"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForThinAtmo");
            }
            else if(market.hasCondition("no_atmosphere"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForNoAtmo");
            }

            if(market.hasCondition("toxic_atmosphere"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForToxicAtmo");
            }

            if(market.hasCondition("irradiated"))
            {
                points = points + Global.getSettings().getInt("boggledExtraPointsForRadiation");
            }

            if(market.hasCondition("habitable"))
            {
                points = points - Global.getSettings().getInt("boggledPointReductionForHabitable");
            }

            if(project.equals("arcology_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(project.replaceAll("_transformation", "")))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForArcology")) / 100;
            }
            else if(project.equals("terran_transformation") || project.equals("terran_eccentric_transformation") || project.equals("auric_transformation") || project.equals("archipelago_transformation") || project.equals("continental_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("terran"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForTerran")) / 100;
            }
            else if(project.equals("water_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(project.replaceAll("_transformation", "")))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForWater")) / 100;
            }
            else if(project.equals("jungle_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("hot"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(project.replaceAll("_transformation", "")))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForJungle")) / 100;
            }
            else if(project.equals("arid_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("hot"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("desert"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForArid")) / 100;
            }
            else if(project.equals("tundra_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("cold"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("tundra"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForTundra")) / 100;
            }
            else if(project.equals("frozen_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points + (4 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("frozen"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForFrozen")) / 100;
            }
            else if(project.equals("volcanic_transformation"))
            {
                if(market.hasCondition("very_hot"))
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("hot"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else if(market.hasCondition("cold"))
                {
                    points = points + (3 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("very_cold"))
                {
                    points = points + (4 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals("volcanic"))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForVolcanic")) / 100;
            }
            else if(project.equals("toxic_transformation"))
            {
                if(market.hasCondition("very_hot") || market.hasCondition("very_cold"))
                {
                    points = points + (2 * Global.getSettings().getInt("boggledTemperaturePointMagnitude"));
                }
                else if(market.hasCondition("hot") || market.hasCondition("cold"))
                {
                    points = points + Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }
                else
                {
                    points = points - Global.getSettings().getInt("boggledTemperaturePointMagnitude");
                }

                if(boggledTools.getPlanetType(market.getPlanetEntity()).equals(project.replaceAll("_transformation", "")))
                {
                    points = (points * Global.getSettings().getInt("boggledPercentageMultiplierForSameTypeTransformation")) / 100;
                }

                return (points * Global.getSettings().getInt("boggledPercentageMultiplierForToxic")) / 100;
            }
        }
        else
        {
            return Global.getSettings().getInt("boggledBaseConditionModificationPoints");
        }

        return 0;
    }

    public static int getLastDayChecked(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_lastDayChecked_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_terraforming_progress_lastDayChecked_", ""));
            }
        }

        return 0;
    }

    public static int getLastMonthChecked(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_lastMonthChecked_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_terraforming_progress_lastMonthChecked_", ""));
            }
        }

        return 0;
    }

    public static int getLastCycleChecked(MarketAPI market)
    {
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_lastCycleChecked_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_terraforming_progress_lastCycleChecked_", ""));
            }
        }

        return 0;
    }

    public static void clearClockCheckTags(MarketAPI market)
    {
        String tagToDelete = null;
        Iterator allTagsOnMarket = market.getTags().iterator();
        while(allTagsOnMarket.hasNext())
        {
            String tag = (String)allTagsOnMarket.next();
            if(tag.contains("boggled_terraforming_progress_last"))
            {
                tagToDelete = tag;
                break;
            }
        }
        allTagsOnMarket = null;

        if(tagToDelete != null)
        {
            market.removeTag(tagToDelete);
            clearClockCheckTags(market);
        }
    }

    public static void addCondition(MarketAPI market, String condition)
    {
        if(!market.hasCondition(condition))
        {
            market.addCondition(condition);
            boggledTools.surveyAll(market);
            boggledTools.refreshSupplyAndDemand(market);
            boggledTools.refreshAquacultureAndFarming(market);
        }
    }

    public static void removeCondition(MarketAPI market, String condition)
    {
        if(market.hasCondition(condition))
        {
            market.removeCondition(condition);
            boggledTools.surveyAll(market);
            boggledTools.refreshSupplyAndDemand(market);
            boggledTools.refreshAquacultureAndFarming(market);
        }
    }

    public static void terraformVariantToVariant(MarketAPI market, String newPlanetType)
    {
        // String oldPlanetType = getPlanetType(market.getPlanetEntity());

        // Not currently in use due to lack of Unknown Skies terraforming options
        // Highly likely to implement in the future
        if(newPlanetType.equals("auric"))
        {
            newPlanetType = "US_auric";
        }
        else if(newPlanetType.equals("archipelago"))
        {
            newPlanetType = "US_water";
        }
        else if(newPlanetType.equals("continental"))
        {
            newPlanetType = "US_continent";
        }

        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext())
        {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType))
            {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());
                myspec.setGlowTexture(spec.getGlowTexture());

                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();

        if(newPlanetType.equals("jungle"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            addCondition(market, "habitable");
            removeCondition(market, "water_surface");
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "farmland_poor");
            addCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            addCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("arid"))
        {
            // Modded conditions

            // Vanilla Conditions
            addCondition(market, "habitable");
            removeCondition(market, "water_surface");
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "farmland_poor");
            addCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            addCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("terran") || newPlanetType.equals("US_auric") || newPlanetType.equals("US_water") || newPlanetType.equals("US_continent"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            addCondition(market, "habitable");
            removeCondition(market, "water_surface");
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "farmland_poor");
            addCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            addCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            addCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("water"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            addCondition(market, "habitable");
            addCondition(market, "water_surface");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("tundra"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            addCondition(market, "habitable");
            removeCondition(market, "water_surface");
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "farmland_poor");
            addCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            addCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            addCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
        else if(newPlanetType.equals("frozen"))
        {
            // Modded conditions
            removeCondition(market, "US_storm");

            // Vanilla Conditions
            removeCondition(market, "habitable");
            removeCondition(market, "water_surface");
            removeCondition(market, "volturnian_lobster_pens");

            removeCondition(market, "farmland_poor");
            removeCondition(market, "farmland_adequate");
            removeCondition(market, "farmland_rich");
            removeCondition(market, "farmland_bountiful");

            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            addCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static String[] getProjectRequirementsStrings(String project)
    {
        if(project.contains("TypeChange"))
        {
            if(project.equals("aridTypeChange"))
            {
                // Requires:
                //  - Not already arid
                //  - Normal atmosphere
                //  - Atmosphere is not toxic or irradiated
                //  - Not cold or very cold temperature
                //  - Stellar Reflectors
                //  - Water Level of 1

                String[] requirements =
                        {
                                "World type allows for terraforming",
                                "Colony is not already an arid world",
                                "Colony has normal atmospheric density",
                                "Colony atmosphere is not toxic or irradiated",
                                "Colony is not cold or very cold",
                                "Colony has stellar reflectors",
                                "Colony has a moderate amount of water present",
                        };
                return requirements;
            }
            else if(project.equals("jungleTypeChange"))
            {
                // Requires:
                //  - Not already jungle
                //  - Normal atmosphere
                //  - Atmosphere is not toxic or irradiated
                //  - Hot or very hot climate
                //  - Stellar Reflectors
                //  - Water Level of 1

                String[] requirements =
                        {
                                "World type allows for terraforming",
                                "Colony is not already a jungle world",
                                "Colony has normal atmospheric density",
                                "Colony atmosphere is not toxic or irradiated",
                                "Colony is hot or very hot",
                                "Colony has stellar reflectors",
                                "Colony has a moderate amount of water present",
                        };
                return requirements;
            }
            else if(project.equals("terranTypeChange"))
            {
                // Requires:
                //  - Not already Terran
                //  - Normal atmosphere
                //  - Atmosphere is not toxic or irradiated
                //  - Not very cold or very hot temperature
                //  - Stellar Reflectors
                //  - Water Level of 1

                String[] requirements =
                        {
                                "World type allows for terraforming",
                                "Colony is not already a Terran world",
                                "Colony has normal atmospheric density",
                                "Colony atmosphere is not toxic or irradiated",
                                "Colony is not very cold or very hot",
                                "Colony has stellar reflectors",
                                "Colony has a moderate amount of water present",
                        };
                return requirements;
            }
            else if(project.equals("waterTypeChange"))
            {
                // Requires:
                //  - Not already water
                //  - Normal atmosphere
                //  - Atmosphere is not toxic or irradiated
                //  - Not very cold or very hot temperature
                //  - Stellar Reflectors
                //  - Water Level of 2

                String[] requirements =
                        {
                                "World type allows for terraforming",
                                "Colony is not already a water world",
                                "Colony has normal atmospheric density",
                                "Colony atmosphere is not toxic or irradiated",
                                "Colony is not very cold or very hot",
                                "Colony has stellar reflectors",
                                "Colony has a large amount of water present",
                        };
                return requirements;
            }
            else if(project.equals("tundraTypeChange"))
            {
                // Requires:
                //  - Not already tundra
                //  - Normal atmosphere
                //  - Atmosphere is not toxic or irradiated
                //  - Not hot or very hot
                //  - Stellar Reflectors
                //  - Water Level of 1

                String[] requirements =
                        {
                                "World type allows for terraforming",
                                "Colony is not already a tundra world",
                                "Colony has normal atmospheric density",
                                "Colony atmosphere is not toxic or irradiated",
                                "Colony is not hot or very hot",
                                "Colony has stellar reflectors",
                                "Colony has a moderate amount of water present",
                        };
                return requirements;
            }
            else if(project.equals("frozenTypeChange"))
            {
                // Requires:
                //  - Not already frozen
                //  - Normal atmosphere
                //  - Very cold
                //  - Water Level of 2

                String[] requirements =
                        {
                                "World type allows for terraforming",
                                "Colony is not already a frozen world",
                                "Colony has normal atmospheric density",
                                "Colony is very cold",
                                "Colony has a large amount of water present",
                        };
                return requirements;
            }
            else
            {
                // Should never be reached unless bad project string passed in.
                String[] requirements =
                        {
                                "You should never see this text. If you do, tell Boggled about it on the forums.",
                        };
                return requirements;
            }
        }
        else if(project.contains("ResourceImprovement"))
        {
            if(project.equals("farmlandResourceImprovement"))
            {
                // Requires:
                //  - Planet type permits improvement in farmland
                //  - Normal atmosphere
                //  - Atmosphere is not toxic or irradiated
                //  - Water Level of 2

                String[] requirements =
                        {
                                "World type supports further farmland improvement",
                                "Colony has normal atmospheric density",
                                "Colony atmosphere is not toxic or irradiated",
                                "Colony has a large amount of water present",
                        };
                return requirements;
            }
            else if(project.equals("organicsResourceImprovement"))
            {
                // Requires:
                //  - Planet type permits improvement in organics

                String[] requirements =
                        {
                                "World type supports further organics improvement",
                        };
                return requirements;
            }
            else if(project.equals("volatilesResourceImprovement"))
            {
                // Requires:
                //  - Planet type permits improvement in volatiles

                String[] requirements =
                        {
                                "World type supports further volatiles improvement",
                        };
                return requirements;
            }
            else
            {
                // Should never be reached unless bad project string passed in.
                String[] requirements =
                        {
                                "You should never see this text. If you do, tell Boggled about it on the forums.",
                        };
                return requirements;
            }
        }
        else if(project.contains("ConditionImprovement"))
        {
            if(project.equals("extremeWeatherConditionImprovement"))
            {
                // Requires:
                //  - Market has Extreme Weather
                //  - Planet can be terraformed
                //  - Market has normal atmosphere
                //  - Market has operational Atmosphere Processor

                String[] requirements =
                        {
                                "World type allows for terraforming",
                                "Colony has extreme weather",
                                "Colony has normal atmospheric density",
                                "Colony has atmosphere processor",
                        };
                return requirements;
            }
            else if(project.equals("mildClimateConditionImprovement"))
            {
                // Requires:
                //  - Market lacks Extreme Weather and Mild Climate
                //  - Market is habitable
                //  - World is Earth-like type
                //  - Market has normal atmosphere
                //  - Market has operational Atmosphere Processor

                String[] requirements =
                        {
                                "Colony has normal climate",
                                "Colony is habitable",
                                "World type allows for mild climate",
                                "Colony has normal atmospheric density",
                                "Colony has atmosphere processor",
                        };
                return requirements;
            }
            else if(project.equals("habitableConditionImprovement"))
            {
                // Requires:
                //  - Market is not already habitable
                //  - World is Earth-like type
                //  - Market has normal atmosphere
                //  - Atmosphere is not toxic or irradiated
                //  - Market has operational Atmosphere Processor

                String[] requirements =
                        {
                                "Colony is not already habitable",
                                "World type allows for human habitability",
                                "Colony has normal atmospheric density",
                                "Colony atmosphere is not toxic or irradiated",
                                "Colony has atmosphere processor",
                        };
                return requirements;
            }
            else if(project.equals("atmosphereDensityConditionImprovement"))
            {
                // Requires:
                //  - Market has atmosphere problem(s)
                //  - Planet can be terraformed
                //  - Market has operational Atmosphere Processor

                String[] requirements =
                        {
                                "Colony atmosphere has suboptimal density",
                                "World type allows for terraforming",
                                "Colony has atmosphere processor",
                        };
                return requirements;
            }
            else if(project.equals("toxicAtmosphereConditionImprovement"))
            {
                // Requires:
                //  - Market has atmosphere problem(s)
                //  - Planet can be terraformed
                //  - Market has operational Atmosphere Processor

                String[] requirements =
                        {
                                "Colony atmosphere is toxic",
                                "World type allows for terraforming",
                                "Colony has atmosphere processor",
                        };
                return requirements;
            }
            else
            {
                // Should never be reached unless bad project string passed in.
                String[] requirements =
                        {
                                "You should never see this text. If you do, tell Boggled about it on the forums.",
                        };
                return requirements;
            }
        }
        else
        {
            // Should never be reached unless bad project string passed in.
            String[] requirements =
                    {
                            "You should never see this text. If you do, tell Boggled about it on the forums.",
                    };
            return requirements;
        }
    }

    public static boolean requirementMet(MarketAPI market, String requirement)
    {
        PlanetAPI planet = market.getPlanetEntity();
        String planetType = getPlanetType(planet);
        Integer planetWaterLevel = getPlanetWaterLevel(market);

        if(requirement.equals("You should never see this text. If you do, tell Boggled about it on the forums."))
        {
            return false;
        }
        else if(requirement.equals("Colony is not already an arid world"))
        {
            return !planetType.equals("desert");
        }
        else if(requirement.equals("Colony is not already a jungle world"))
        {
            return !planetType.equals("jungle");
        }
        else if(requirement.equals("Colony is not already a Terran world"))
        {
            return !planetType.equals("terran");
        }
        else if(requirement.equals("Colony is not already a water world"))
        {
            return !planetType.equals("water");
        }
        else if(requirement.equals("Colony is not already a tundra world"))
        {
            return !planetType.equals("tundra");
        }
        else if(requirement.equals("Colony is not already a frozen world"))
        {
            return !planetType.equals("frozen");
        }
        else if(requirement.equals("Colony has normal atmospheric density"))
        {
            if(market.hasCondition("no_atmosphere") || market.hasCondition("thin_atmosphere") || market.hasCondition("dense_atmosphere"))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else if(requirement.equals("Colony atmosphere is not toxic or irradiated"))
        {
            if(market.hasCondition("toxic_atmosphere") || market.hasCondition("irradiated"))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else if(requirement.equals("Colony is not cold or very cold"))
        {
            return !market.hasCondition("cold") && !market.hasCondition("very_cold");
        }
        else if(requirement.equals("Colony is hot or very hot"))
        {
            return market.hasCondition("hot") || market.hasCondition("very_hot");
        }
        else if(requirement.equals("Colony is not very cold or very hot"))
        {
            return !market.hasCondition("very_hot") && !market.hasCondition("very_cold");
        }
        else if(requirement.equals("Colony is not hot or very hot"))
        {
            return !market.hasCondition("hot") && !market.hasCondition("very_hot");
        }
        else if(requirement.equals("Colony is very cold"))
        {
            return market.hasCondition("very_cold");
        }
        else if(requirement.equals("Colony has stellar reflectors"))
        {
            return marketHasStellarReflectorArray(market);
        }
        else if(requirement.equals("Colony has a moderate amount of water present"))
        {
            return planetWaterLevel == 1 || planetWaterLevel == 2;
        }
        else if(requirement.equals("Colony has a large amount of water present"))
        {
            return planetWaterLevel == 2;
        }
        else if(requirement.equals("Colony has atmosphere processor"))
        {
            return marketHasAtmosphereProcessor(market);
        }
        else if(requirement.equals("Colony is habitable"))
        {
            return market.hasCondition("habitable");
        }
        else if(requirement.equals("Colony is not already habitable"))
        {
            return !market.hasCondition("habitable");
        }
        else if(requirement.equals("Colony has extreme weather"))
        {
            return market.hasCondition("extreme_weather") || market.hasCondition("US_storm");
        }
        else if(requirement.equals("Colony has normal climate"))
        {
            return !market.hasCondition("mild_climate") && !market.hasCondition("extreme_weather") && !market.hasCondition("US_storm");
        }
        else if(requirement.equals("Colony atmosphere is toxic"))
        {
            return market.hasCondition("toxic_atmosphere");
        }
        else if(requirement.equals("Colony atmosphere has suboptimal density"))
        {
            return market.hasCondition("no_atmosphere") || market.hasCondition("thin_atmosphere") || market.hasCondition("dense_atmosphere");
        }
        else if(requirement.equals("World type supports further farmland improvement"))
        {
            return getMaxFarmlandForMarket(market) > getCurrentFarmlandForMarket(market);
        }
        else if(requirement.equals("World type supports further organics improvement"))
        {
            return getMaxOrganicsForMarket(market) > getCurrentOrganicsForMarket(market);
        }
        else if(requirement.equals("World type supports further volatiles improvement"))
        {
            return getMaxVolatilesForMarket(market) > getCurrentVolatilesForMarket(market);
        }
        else if(requirement.equals("World type allows for terraforming"))
        {
            if(planetType.equals("star") || planetType.equals("gas_giant") || planetType.equals("volcanic") || planetType.equals("unknown"))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else if(requirement.equals("World type allows for mild climate") || requirement.equals("World type allows for human habitability"))
        {
            if(planetType.equals("jungle") || planetType.equals("desert") || planetType.equals("terran") || planetType.equals("water") || planetType.equals("tundra"))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public static boolean projectRequirementsMet(MarketAPI market, String project)
    {
        String[] requirements = getProjectRequirementsStrings(project);
        int i = 0;
        for (i = 0; i < requirements.length; i++)
        {
            if(!requirementMet(market, requirements[i]))
            {
                return false;
            }
        }

        // Returns true if no requirement was failed above
        return true;
    }

    public static Boolean printProjectRequirementsReportIfStalled(MarketAPI market, String project, TextPanelAPI text)
    {
        Color highlight = Misc.getHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(project != null && !project.equals("None"))
        {
            // Print requirements, and if not met, print terraforming is stalled
            text.addPara("Project Requirements:", highlight, new String[]{""});
            String[] requirements = boggledTools.getProjectRequirementsStrings(project);
            Boolean foundUnmetRequirement = false;
            int i;
            for (i = 0; i < requirements.length; i++)
            {
                if(boggledTools.requirementMet(market, requirements[i]))
                {
                    text.addPara("      - %s", good, new String[]{requirements[i] + ""});
                }
                else
                {
                    text.addPara("      - %s", bad, new String[]{requirements[i] + ""});
                    foundUnmetRequirement = true;
                }
            }

            return foundUnmetRequirement;
        }

        return false;
    }

    public static void printProjectResults(MarketAPI market, String project, TextPanelAPI text)
    {
        Color highlight = Misc.getHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(project.equals("aridTypeChange"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Arid world starting resources:", highlight, new String[]{""});
            text.addPara("          - Adequate farmland, common organics, no volatiles", highlight, new String[]{""});
            text.addPara("      - Arid world maximum resources:", highlight, new String[]{""});
            text.addPara("          - Bountiful farmland, abundant organics, trace volatiles", highlight, new String[]{""});
            text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
        }
        else if(project.equals("frozenTypeChange"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Frozen world starting resources:", highlight, new String[]{""});
            text.addPara("          - No farmland, no organics, abundant volatiles", highlight, new String[]{""});
            text.addPara("      - Frozen world maximum resources:", highlight, new String[]{""});
            text.addPara("          - No farmland, no organics, plentiful volatiles", highlight, new String[]{""});
            text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
        }
        else if(project.equals("jungleTypeChange"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Jungle world starting resources:", highlight, new String[]{""});
            text.addPara("          - Adequate farmland, common organics, no volatiles", highlight, new String[]{""});
            text.addPara("      - Jungle world maximum resources:", highlight, new String[]{""});
            text.addPara("          - Bountiful farmland, plentiful organics, no volatiles", highlight, new String[]{""});
            text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
        }
        else if(project.equals("terranTypeChange"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Terran world starting resources:", highlight, new String[]{""});
            text.addPara("          - Adequate farmland, trace organics, trace volatiles", highlight, new String[]{""});
            text.addPara("      - Terran world maximum resources:", highlight, new String[]{""});
            text.addPara("          - Bountiful farmland, plentiful organics, trace volatiles", highlight, new String[]{""});
            text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
        }
        else if(project.equals("waterTypeChange"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Water world starting resources:", highlight, new String[]{""});
            text.addPara("          - No organics, no volatiles", highlight, new String[]{""});
            text.addPara("      - Water world maximum resources:", highlight, new String[]{""});
            text.addPara("          - Plentiful organics, plentiful volatiles", highlight, new String[]{""});
            text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
        }
        else if(project.equals("tundraTypeChange"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Tundra world starting resources:", highlight, new String[]{""});
            text.addPara("          - Adequate farmland, trace organics, trace volatiles", highlight, new String[]{""});
            text.addPara("      - Tundra world maximum resources:", highlight, new String[]{""});
            text.addPara("          - Bountiful farmland, trace organics, plentiful volatiles", highlight, new String[]{""});
            text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
        }
        else if(project.equals("farmlandResourceImprovement"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Farming yield improved by one", highlight, new String[]{""});
        }
        else if(project.equals("organicsResourceImprovement"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Organics yield improved by one", highlight, new String[]{""});
        }
        else if(project.equals("volatilesResourceImprovement"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Volatiles yield improved by one", highlight, new String[]{""});
        }
        else if(project.equals("extremeWeatherConditionImprovement"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Extreme weather patterns remediated", highlight, new String[]{""});
        }
        else if(project.equals("mildClimateConditionImprovement"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Climate made mild", highlight, new String[]{""});
        }
        else if(project.equals("habitableConditionImprovement"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Atmosphere made human-breathable", highlight, new String[]{""});
        }
        else if(project.equals("atmosphereDensityConditionImprovement"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Atmosphere with Earth-like density created", highlight, new String[]{""});
        }
        else if(project.equals("toxicAtmosphereConditionImprovement"))
        {
            text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

            text.addPara("      - Atmospheric toxicity remediated", highlight, new String[]{""});
        }
    }

    public static int getPlanetWaterLevel(MarketAPI market)
    {
        // There are checks present elsewhere that will prevent passing in a station market.
        // If that happens anyway, it's best to just throw an exception.

        PlanetAPI planet = market.getPlanetEntity();
        String planetType = getPlanetType(planet);
        if(planetType.equals("water") || planetType.equals("frozen") || hasIsmaraSling(market))
        {
            return 2;
        }
        else if(planetType.equals("desert") || planetType.equals("terran") || planetType.equals("tundra") || planetType.equals("jungle"))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static boolean marketHasAtmosphereProcessor(MarketAPI market)
    {
        if(market.getIndustry("BOGGLED_ATMOSPHERE_PROCESSOR") != null && market.getIndustry("BOGGLED_ATMOSPHERE_PROCESSOR").isFunctional() && market.hasIndustry("BOGGLED_ATMOSPHERE_PROCESSOR"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean marketHasStellarReflectorArray(MarketAPI market)
    {
        if(market.getIndustry("BOGGLED_STELLAR_REFLECTOR_ARRAY") != null && market.getIndustry("BOGGLED_STELLAR_REFLECTOR_ARRAY").isFunctional() && market.hasIndustry("BOGGLED_STELLAR_REFLECTOR_ARRAY"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean marketHasAtmoProblem(MarketAPI market)
    {
        if(!market.hasCondition("mild_climate") || !market.hasCondition("habitable") || market.hasCondition("no_atmosphere") || market.hasCondition("thin_atmosphere") || market.hasCondition("dense_atmosphere") || market.hasCondition("toxic_atmosphere") || market.hasCondition("US_storm"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean marketIsIrradiated(MarketAPI market)
    {
        if(market.hasCondition("irradiated"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static int waterLevelNeededForProject(String project)
    {
        if(project.contains("TypeChange"))
        {
            if(project.equals("waterTypeChange") || project.equals("frozenTypeChange"))
            {
                return 2;
            }
            else
            {
                // Need to adjust this if I end up allowing terraforming to US planet types or barren/toxic.
                // Assumes project type is Terran, jungle, arid or tundra.
                return 1;
            }
        }
        else if(project.contains("ResourceImprovement"))
        {
            if(project.equals("farmlandResourceImprovement"))
            {
                return 2;
            }
            else if(project.equals("organicsResourceImprovement") || project.equals("volatilesResourceImprovement"))
            {
                return 0;
            }
        }
        else if(project.contains("ConditionImprovement"))
        {
            if(project.equals("habitableConditionImprovement") || project.equals("mildClimateConditionImprovement") || project.equals("extremeWeatherConditionImprovement") || project.equals("noAtmosphereConditionImprovement") || project.equals("thinAtmosphereConditionImprovement"))
            {
                return 1;
            }
            else if(project.equals("denseAtmosphereConditionImprovement") || project.equals("toxicAtmosphereConditionImprovement"))
            {
                return 0;
            }
        }

        // Should never be reached unless there's a bug present and/or bad value passed in
        return 0;
    }

    public static String getTooltipProjectName(String currentProject)
    {
        if(currentProject == null || currentProject.equals("None"))
        {
            return "None";
        }
        else if(currentProject.contains("TypeChange"))
        {
            if(currentProject.equals("jungleTypeChange"))
            {
                return "Jungle type change";
            }
            else if(currentProject.equals("aridTypeChange"))
            {
                return "Arid type change";
            }
            else if(currentProject.equals("terranTypeChange"))
            {
                return "Terran type change";
            }
            else if(currentProject.equals("waterTypeChange"))
            {
                return "Water type change";
            }
            else if(currentProject.equals("tundraTypeChange"))
            {
                return "Tundra type change";
            }
            else if(currentProject.equals("frozenTypeChange"))
            {
                return "Frozen type change";
            }
            else
            {
                return "ERROR";
            }
        }
        else if(currentProject.contains("ResourceImprovement"))
        {
            if(currentProject.equals("farmlandResourceImprovement"))
            {
                return "Farmland resource improvement";
            }
            else if(currentProject.equals("organicsResourceImprovement"))
            {
                return "Organics resource improvement";
            }
            else if(currentProject.equals("volatilesResourceImprovement"))
            {
                return "Volatiles resource improvement";
            }
            else
            {
                return "ERROR";
            }
        }
        else if(currentProject.contains("ConditionImprovement"))
        {
            if(currentProject.equals("extremeWeatherConditionImprovement"))
            {
                return "Stabilize weather patterns";
            }
            else if(currentProject.equals("mildClimateConditionImprovement"))
            {
                return "Make climate mild";
            }
            else if(currentProject.equals("habitableConditionImprovement"))
            {
                return "Make atmosphere habitable";
            }
            else if(currentProject.equals("atmosphereDensityConditionImprovement"))
            {
                return "Normalize atmospheric density";
            }
            else if(currentProject.equals("toxicAtmosphereConditionImprovement"))
            {
                return "Reduce atmospheric toxicity";
            }
            else
            {
                return "ERROR";
            }
        }
        else
        {
            return "ERROR";
        }
    }

    public static MarketAPI createMiningStationMarket(SectorEntityToken stationEntity)
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        StarSystemAPI system = stationEntity.getStarSystem();
        String systemName = system.getName();

        //Create the mining station market
        MarketAPI market = Global.getFactory().createMarket(systemName + clock.getCycle() + clock.getMonth() + clock.getDay() + "MiningStationMarket", stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        if(Global.getSettings().getBoolean("boggledMiningStationLinkToResourceBelts"))
        {
            int numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(stationEntity);
            String resourceLevel = boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem);
            market.addCondition("ore_" + resourceLevel);
            market.addCondition("rare_ore_" + resourceLevel);
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
            market.addCondition("ore_" + resourceLevel);
            market.addCondition("rare_ore_" + resourceLevel);
        }

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
        market.getConstructionQueue().addToEnd(Industries.MINING, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);
        //Global.getSector().getCampaignUI().getCurrentInteractionDialog().dismiss();

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);

        return market;
    }

    public static MarketAPI createSiphonStationMarket(SectorEntityToken stationEntity, SectorEntityToken hostGasGiant)
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        StarSystemAPI system = stationEntity.getStarSystem();
        String systemName = system.getName();

        //Create the siphon station market
        MarketAPI market = Global.getFactory().createMarket(systemName + ":" + hostGasGiant.getName() + "SiphonStationMarket", stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        if(Global.getSettings().getBoolean("boggledSiphonStationLinkToGasGiant"))
        {
            if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_TRACE))
            {
                market.addCondition(Conditions.VOLATILES_TRACE);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_DIFFUSE))
            {
                market.addCondition(Conditions.VOLATILES_DIFFUSE);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_ABUNDANT))
            {
                market.addCondition(Conditions.VOLATILES_ABUNDANT);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_PLENTIFUL))
            {
                market.addCondition(Conditions.VOLATILES_PLENTIFUL);
            }
            else //Can a gas giant not have any volatiles at all?
            {
                market.addCondition(Conditions.VOLATILES_TRACE);
            }
        }
        else
        {
            String resourceLevel = "diffuse";
            int staticAmountPerSettings = Global.getSettings().getInt("boggledSiphonStationStaticAmount");
            switch(staticAmountPerSettings)
            {
                case 1:
                    resourceLevel = "trace";
                    break;
                case 2:
                    resourceLevel = "diffuse";
                    break;
                case 3:
                    resourceLevel = "abundant";
                    break;
                case 4:
                    resourceLevel = "plentiful";
                    break;
            }
            market.addCondition("volatiles_" + resourceLevel);
        }

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
        market.getConstructionQueue().addToEnd(Industries.MINING, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);
        //Global.getSector().getCampaignUI().getCurrentInteractionDialog().dismiss();

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
        return market;
    }

    public static MarketAPI createAstropolisStationMarket(SectorEntityToken stationEntity, SectorEntityToken hostPlanet)
    {
        CampaignClockAPI clock = Global.getSector().getClock();

        //Create the astropolis market
        MarketAPI market = Global.getFactory().createMarket(hostPlanet.getName() + "astropolisMarket" + clock.getCycle() + clock.getMonth() + clock.getDay(), stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
        return market;
    }

    public static int getLastDayCheckedForConstruction(SectorEntityToken stationEntity)
    {
        Iterator allTagsOnStation = stationEntity.getTags().iterator();
        while(allTagsOnStation.hasNext())
        {
            String tag = (String)allTagsOnStation.next();
            if(tag.contains("boggled_construction_progress_lastDayChecked_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_construction_progress_lastDayChecked_", ""));
            }
        }

        return 0;
    }

    public static void clearClockCheckTagsForConstruction(SectorEntityToken stationEntity)
    {
        String tagToDelete = null;
        Iterator allTagsOnStation = stationEntity.getTags().iterator();
        while(allTagsOnStation.hasNext())
        {
            String tag = (String)allTagsOnStation.next();
            if(tag.contains("boggled_construction_progress_lastDayChecked_"))
            {
                tagToDelete = tag;
                break;
            }
        }
        allTagsOnStation = null;

        if(tagToDelete != null)
        {
            stationEntity.removeTag(tagToDelete);
            clearClockCheckTagsForConstruction(stationEntity);
        }
    }

    public static void clearBoggledTerraformingControllerTags(MarketAPI market)
    {
        String tagToDelete = null;
        Iterator allTags = market.getTags().iterator();
        while(allTags.hasNext())
        {
            String tag = (String)allTags.next();
            if(tag.contains("boggledTerraformingController"))
            {
                tagToDelete = tag;
                break;
            }
        }
        allTags = null;

        if(tagToDelete != null)
        {
            market.removeTag(tagToDelete);
            clearBoggledTerraformingControllerTags(market);
        }
    }

    public static int getConstructionProgressDays(SectorEntityToken stationEntity)
    {
        Iterator allTagsOnStation = stationEntity.getTags().iterator();
        while(allTagsOnStation.hasNext())
        {
            String tag = (String)allTagsOnStation.next();
            if(tag.contains("boggled_construction_progress_days_"))
            {
                return Integer.parseInt(tag.replaceAll("boggled_construction_progress_days_", ""));
            }
        }

        return 0;
    }

    public static void clearProgressCheckTagsForConstruction(SectorEntityToken stationEntity)
    {
        String tagToDelete = null;
        Iterator allTagsOnStation = stationEntity.getTags().iterator();
        while(allTagsOnStation.hasNext())
        {
            String tag = (String)allTagsOnStation.next();
            if(tag.contains("boggled_construction_progress_days_"))
            {
                tagToDelete = tag;
                break;
            }
        }
        allTagsOnStation = null;

        if(tagToDelete != null)
        {
            stationEntity.removeTag(tagToDelete);
            clearProgressCheckTagsForConstruction(stationEntity);
        }
    }

    public static void incrementConstructionProgressDays(SectorEntityToken stationEntity, int amount)
    {
        int currentDays = getConstructionProgressDays(stationEntity);

        clearProgressCheckTagsForConstruction(stationEntity);

        currentDays = currentDays + amount;

        String strDays = currentDays + "";

        while(strDays.length() < 6)
        {
            strDays = "0" + strDays;
        }

        stationEntity.addTag("boggled_construction_progress_days_" + strDays);
    }

    public static void sendDebugIntelMessage(String message)
    {
        MessageIntel intel = new MessageIntel(message, Misc.getBasePlayerColor());
        intel.addLine(message);
        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, null);
    }

    public static void terraformDebug(MarketAPI market)
    {
        market.getPlanetEntity().setTypeId("terran");

        String newPlanetType = "terran";

        PlanetSpecAPI myspec = market.getPlanetEntity().getSpec();
        Iterator var4 = Global.getSettings().getAllPlanetSpecs().iterator();
        while(var4.hasNext())
        {
            PlanetSpecAPI spec = (PlanetSpecAPI)var4.next();
            if (spec.getPlanetType().equals(newPlanetType))
            {
                myspec.setAtmosphereColor(spec.getAtmosphereColor());
                myspec.setAtmosphereThickness(spec.getAtmosphereThickness());
                myspec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                myspec.setCloudColor(spec.getCloudColor());
                myspec.setCloudRotation(spec.getCloudRotation());
                myspec.setCloudTexture(spec.getCloudTexture());
                myspec.setGlowColor(spec.getGlowColor());

                if(newPlanetType.equals("boggled_arcology"))
                {
                    myspec.setGlowTexture("graphics/planets/arcology_glow.png");
                }
                else
                {
                    myspec.setGlowTexture(spec.getGlowTexture());
                }

                myspec.setIconColor(spec.getIconColor());
                myspec.setPlanetColor(spec.getPlanetColor());
                myspec.setStarscapeIcon(spec.getStarscapeIcon());
                myspec.setTexture(spec.getTexture());
                myspec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec)myspec).planetType = newPlanetType;
                ((PlanetSpec)myspec).name = spec.getName();
                ((PlanetSpec)myspec).descriptionId = ((PlanetSpec)spec).descriptionId;
                break;
            }
        }
        market.getPlanetEntity().applySpecChanges();
    }
}