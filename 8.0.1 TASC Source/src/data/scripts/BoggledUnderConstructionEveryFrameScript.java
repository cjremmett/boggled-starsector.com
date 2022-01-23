package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

import java.util.Iterator;

public class BoggledUnderConstructionEveryFrameScript implements EveryFrameScript
{
    private SectorEntityToken stationEntity;
    private boolean isDone = false;
    private int requiredDays = Global.getSettings().getInt("boggledStationConstructionDelayDays");

    public BoggledUnderConstructionEveryFrameScript(SectorEntityToken station)
    {
        this.stationEntity = station;

        CampaignClockAPI clock = Global.getSector().getClock();
        stationEntity.addTag("boggled_construction_progress_lastDayChecked_" + clock.getDay());
        stationEntity.addTag("boggled_construction_progress_days_0");
    }

    public boolean isDone()
    {
        if(isDone)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean runWhilePaused()
    {
        return false;
    }

    public void advance(float var1)
    {
        CampaignClockAPI clock = Global.getSector().getClock();

        // Reload day check
        int lastDayChecked = boggledTools.getLastDayCheckedForConstruction(stationEntity);

        // Exit if a day has not passed
        if(clock.getDay() == lastDayChecked)
        {
            return;
        }
        else
        {
            // Add one day to the construction progress
            boggledTools.incrementConstructionProgressDays(stationEntity, 1);

            //Check if construction should be completed today
            int progress = boggledTools.getConstructionProgressDays(stationEntity);
            if(progress >= requiredDays)
            {
                isDone = true;
                String entityType = stationEntity.getCustomEntityType();
                if(entityType.contains("boggled_mining_station"))
                {
                    boggledTools.createMiningStationMarket(stationEntity);
                }
                else if(entityType.contains("boggled_siphon_station"))
                {
                    boggledTools.createSiphonStationMarket(stationEntity, stationEntity.getOrbitFocus());
                }
                else if(entityType.contains("boggled_astropolis_station"))
                {
                    boggledTools.createAstropolisStationMarket(stationEntity, stationEntity.getOrbitFocus());
                }
            }

            //Update the lastDayChecked to today
            boggledTools.clearClockCheckTagsForConstruction(stationEntity);
            stationEntity.addTag("boggled_construction_progress_lastDayChecked_" + clock.getDay());
        }
    }
}