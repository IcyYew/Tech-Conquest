package buildings.resourcebuildings;

import buildings.BuildingManager;
import buildings.BuildingTypes;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

/**
 * Class for the Lumberyard.
 * @author Michael Geltz.
 */
@Entity
@DiscriminatorValue("LUMBERYARD")
public class LumberYard extends ResourceBuilding{

    public LumberYard(int level, ResourceBuildingManager resourceBuildingManager)
    {
        super(BuildingTypes.LUMBERYARD, level, resourceBuildingManager);
        setResourceProductionRate(1);
        setTimeLastCollected(LocalDateTime.now());
        setPower(16);
        setStoneUpgradeCost(400);
        setWoodUpgradeCost(400);
        setResourceLimit(1000);
    }

    public LumberYard()
    {

    }
}
