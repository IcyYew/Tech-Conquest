package buildings.troopBuildings;

import buildings.BuildingManager;
import buildings.BuildingTypes;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import troops.TroopTypes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static troops.TroopTypes.ARCHER;
import static troops.TroopTypes.WARRIOR;

@Entity
@DiscriminatorValue("WARRIORSCHOOL")
public class WarriorSchool extends TroopTrainingBuilding{
    private int trainingCapacity = 50;
    private double trainingTime = 15.0; // in seconds
    private int stoneTrainingCost = 30;
    private int woodTrainingCost = 40;

    public WarriorSchool(int level, TroopBuildingManager troopBuildingManager)
    {
        super(BuildingTypes.WARRIORSCHOOL, level, troopBuildingManager);

    }
}
