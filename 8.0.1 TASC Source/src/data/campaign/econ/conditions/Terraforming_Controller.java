
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

import javax.swing.*;

public class Terraforming_Controller extends BaseHazardCondition
{
    public Terraforming_Controller() { }

    public static int daysRequiredForTypeChange = Global.getSettings().getInt("boggledTerraformingTime");
    public static int daysRequiredForResourceImprovement = Global.getSettings().getInt("boggledResourceImprovementTime");
    public static int daysRequiredForConditionImprovement = Global.getSettings().getInt("boggledResourceImprovementTime");

    private int daysCompleted = 0;
    private int lastDayChecked = 0;

    private String currentProject = null;

    private void loadVariables()
    {
        Iterator allTags = this.market.getTags().iterator();
        while(allTags.hasNext())
        {
            String tag = (String)allTags.next();
            if(tag.contains("boggledTerraformingControllerDaysCompleted_"))
            {
                daysCompleted = Integer.parseInt(tag.replace("boggledTerraformingControllerDaysCompleted_", ""));
            }
            else if(tag.contains("boggledTerraformingControllerLastDayChecked_"))
            {
                lastDayChecked = Integer.parseInt(tag.replace("boggledTerraformingControllerLastDayChecked_", ""));
            }
            else if(tag.contains("boggledTerraformingControllerCurrentProject_"))
            {
                currentProject = tag.replace("boggledTerraformingControllerCurrentProject_", "");
            }
        }
    }

    private void storeVariables()
    {
        boggledTools.clearBoggledTerraformingControllerTags(this.market);

        this.market.addTag("boggledTerraformingControllerDaysCompleted_" + daysCompleted);
        this.market.addTag("boggledTerraformingControllerLastDayChecked_" + lastDayChecked);
        if(currentProject == null)
        {
            this.market.addTag("boggledTerraformingControllerCurrentProject_" + "None");
        }
        else
        {
            this.market.addTag("boggledTerraformingControllerCurrentProject_" + currentProject);
        }
    }

    public String getProject()
    {
        loadVariables();

        if(currentProject == null)
        {
            return "None";
        }
        else
        {
            return currentProject;
        }
    }

    public void setProject(String project)
    {
        this.daysCompleted = 0;
        this.lastDayChecked = 0;
        currentProject = project;

        if(this.market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Started");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }

        storeVariables();
    }

    public int getDaysCompleted()
    {
        loadVariables();

        return daysCompleted;
    }

    public int getDaysRequired()
    {
        loadVariables();

        if(currentProject.contains("TypeChange"))
        {
            return daysRequiredForTypeChange;
        }
        else if(currentProject.contains("ResourceImprovement"))
        {
            return daysRequiredForResourceImprovement;
        }
        else if(currentProject.contains("ConditionImprovement"))
        {
            return daysRequiredForConditionImprovement;
        }
        else
        {
            return 0;
        }
    }

    public int getDaysRemaining()
    {
        return getDaysRequired() - getDaysCompleted();
    }

    public int getPercentComplete()
    {
        Double daysCompleted = Double.valueOf(getDaysCompleted());
        Double daysRequired = Double.valueOf(getDaysRequired());

        Double percentCompete = (daysCompleted / daysRequired) * 100;
        int returnValue = percentCompete.intValue();
        if(returnValue == 100)
        {
            return 99;
        }
        else
        {
            return returnValue;
        }
    }

    public void advance(float amount)
    {
        loadVariables();

        super.advance(amount);

        if(!this.market.isPlayerOwned())
        {
            boggledTools.removeCondition(this.market, "terraforming_controller");
            return;
        }

        if(currentProject == null)
        {
            this.daysCompleted = 0;
            this.lastDayChecked = 0;
        }
        else
        {
            CampaignClockAPI clock = Global.getSector().getClock();
            if(clock.getDay() != this.lastDayChecked)
            {
                if(boggledTools.projectRequirementsMet(this.market, currentProject))
                {
                    this.daysCompleted++;
                    this.lastDayChecked = clock.getDay();

                    if(this.currentProject.contains("TypeChange") && this.daysCompleted >= daysRequiredForTypeChange)
                    {
                        boggledTools.terraformVariantToVariant(this.market, currentProject.replace("TypeChange", ""));
                        currentProject = null;
                        this.daysCompleted = 0;
                        this.lastDayChecked = 0;
                    }
                    else if(this.currentProject.contains("ResourceImprovement") && this.daysCompleted >= daysRequiredForResourceImprovement)
                    {
                        if(this.currentProject.equals("farmlandResourceImprovement"))
                        {
                            boggledTools.incrementFarmland(this.market);
                            currentProject = null;
                            this.daysCompleted = 0;
                            this.lastDayChecked = 0;
                        }
                        else if(this.currentProject.equals("organicsResourceImprovement"))
                        {
                            boggledTools.incrementOrganics(this.market);
                            currentProject = null;
                            this.daysCompleted = 0;
                            this.lastDayChecked = 0;
                        }
                        else if(this.currentProject.equals("volatilesResourceImprovement"))
                        {
                            boggledTools.incrementVolatiles(this.market);
                            currentProject = null;
                            this.daysCompleted = 0;
                            this.lastDayChecked = 0;
                        }
                    }
                    else if(this.currentProject.contains("ConditionImprovement") && this.daysCompleted >= daysRequiredForConditionImprovement)
                    {
                        if(this.currentProject.equals("extremeWeatherConditionImprovement"))
                        {
                            boggledTools.removeCondition(this.market, "extreme_weather");
                            boggledTools.removeCondition(this.market, "US_storm");
                            currentProject = null;
                            this.daysCompleted = 0;
                            this.lastDayChecked = 0;
                        }
                        else if(this.currentProject.equals("mildClimateConditionImprovement"))
                        {
                            boggledTools.addCondition(this.market, "mild_climate");
                            currentProject = null;
                            this.daysCompleted = 0;
                            this.lastDayChecked = 0;
                        }
                        else if(this.currentProject.equals("habitableConditionImprovement"))
                        {
                            boggledTools.addCondition(this.market, "habitable");
                            currentProject = null;
                            this.daysCompleted = 0;
                            this.lastDayChecked = 0;
                        }
                        else if(this.currentProject.equals("atmosphereDensityConditionImprovement"))
                        {
                            boggledTools.removeCondition(this.market, "no_atmosphere");
                            boggledTools.removeCondition(this.market, "thin_atmosphere");
                            boggledTools.removeCondition(this.market, "dense_atmosphere");
                            currentProject = null;
                            this.daysCompleted = 0;
                            this.lastDayChecked = 0;
                        }
                        else if(this.currentProject.equals("toxicAtmosphereConditionImprovement"))
                        {
                            boggledTools.removeCondition(this.market, "toxic_atmosphere");
                            currentProject = null;
                            this.daysCompleted = 0;
                            this.lastDayChecked = 0;
                        }

                        if (market.isPlayerOwned())
                        {
                            MessageIntel intel = new MessageIntel("Terraforming on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                }
                else
                {
                    this.lastDayChecked = clock.getDay();
                }
            }
        }

        storeVariables();
    }

    public void apply(String id) { super.apply(id); }

    public void unapply(String id) { super.unapply(id); }

    public Map<String, String> getTokenReplacements() { return super.getTokenReplacements(); }

    public boolean showIcon() { return false; }
}
