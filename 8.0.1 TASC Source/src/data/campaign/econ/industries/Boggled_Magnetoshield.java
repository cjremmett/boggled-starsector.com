package data.campaign.econ.industries;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

public class Boggled_Magnetoshield extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted() { return false; }

    @Override
    public void apply()
    {
        super.apply(true);
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(Global.getSettings().getBoolean("boggledMagnetoshieldEnabled") && this.market.hasCondition("irradiated"))
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
        if(!Global.getSettings().getBoolean("boggledMagnetoshieldEnabled"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String getUnavailableReason()
    {
        if(!this.market.hasCondition("irradiated"))
        {
            return this.market.getName() + " isn't irradiated. There's no reason to build a magnetoshield.";
        }
        else
        {
            return "Error in getUnavailableReason() in magnetoshield. Tell Boggled about this on the forums.";
        }
    }

    @Override
    public void buildingFinished()
    {
        boggledTools.removeCondition(this.market, "irradiated");
        super.buildingFinished();
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        boggledTools.addCondition(this.market, "irradiated");

        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    public boolean canImprove()
    {
        return false;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}
