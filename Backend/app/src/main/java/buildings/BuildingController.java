package buildings;

import buildings.Research.Research;
import buildings.Research.ResearchManager;
import buildings.resourcebuildings.ResourceBuilding;
import buildings.resourcebuildings.ResourceBuildingRepository;
import buildings.troopBuildings.TroopBuildingRepository;
import buildings.troopBuildings.TroopTrainingBuilding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import player.Player;
import player.PlayerRepository;
import resources.ResourceType;

import javax.print.attribute.standard.MediaSize;
import java.util.ArrayList;
import java.util.List;

import static troops.TroopTypes.*;

@RestController
public class BuildingController {

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ResourceBuildingRepository resourceBuildingRepository;

    @Autowired
    private TroopBuildingRepository troopBuildingRepository;

    @Autowired
    private OtherBuildingRepository otherBuildingRepository;

   @GetMapping("/buildings/research/getallresearch/{playerID}")
    public ResearchManager researchList(@PathVariable Integer playerID) {
       return playerRepository.getById(playerID).getResearchManager();
    }

    @PostMapping("/buildings/research/levelresearch/attackbonus")
    public void levelAttackBonus(@RequestBody ResearchLevelRequest researchLevelRequest) {
       Player player = playerRepository.getById(researchLevelRequest.getPlayerID());
       Research research = player.getResearchManager().getResearch(researchLevelRequest.getResearchName());
       if (research.getLevel() < 5) {
           research.levelUpResearch(research.getLevel() + 1, player.getResearchManager());
           player.getTroops().getTroop(ARCHER).setDamage(Math.ceil(player.getTroops().getTroop(ARCHER).getDamage() * (research.getLevel() * 1.05)));
           player.getTroops().getTroop(MAGE).setDamage(Math.ceil(player.getTroops().getTroop(MAGE).getDamage() * (research.getLevel() * 1.05)));
           player.getTroops().getTroop(WARRIOR).setDamage(Math.ceil(player.getTroops().getTroop(WARRIOR).getDamage() * (research.getLevel() * 1.05)));
           player.getTroops().getTroop(CAVALRY).setDamage(Math.ceil(player.getTroops().getTroop(CAVALRY).getDamage() * (research.getLevel() * 1.05)));
       }
       player.updatePower();
       playerRepository.save(player);
    }

    @PostMapping("/buildings/research/levelresearch/buildingcost")
    public void levelBuildingCost(@RequestBody ResearchLevelRequest researchLevelRequest) {

    }

    @PostMapping("/buildings/research/levelresearch/researchcost")
    public void levelResearchCost(@RequestBody ResearchLevelRequest researchLevelRequest) {
       Player player = playerRepository.getById(researchLevelRequest.getPlayerID());
       Research research = player.getResearchManager().getResearch(researchLevelRequest.getResearchName());
       Research attackBonus = player.getResearchManager().getResearch("Attack Bonus");
       Research researchCost = player.getResearchManager().getResearch("Research Cost");
       Research trainingSpeed = player.getResearchManager().getResearch("Training Speed");
       Research buildingCost = player.getResearchManager().getResearch("Building Cost");
       Research trainingCapacity = player.getResearchManager().getResearch("Training Capacity");
       Research buildingSpeed = player.getResearchManager().getResearch("Building Speed");
       if (research.getLevel() < 5) {
           research.levelUpResearch(research.getLevel() + 1, player.getResearchManager());
           attackBonus.setPlatinumCost(Math.ceil(attackBonus.getPlatinumCost() * .97));
           trainingSpeed.setPlatinumCost(Math.ceil(trainingSpeed.getPlatinumCost() * .97));
           buildingCost.setPlatinumCost(Math.ceil(buildingCost.getPlatinumCost() * .97));
           trainingCapacity.setPlatinumCost(Math.ceil(trainingCapacity.getPlatinumCost() * .97));
           buildingSpeed.setPlatinumCost(Math.ceil(buildingSpeed.getPlatinumCost() * .97));
       }
       player.updatePower();
       playerRepository.save(player);
    }

    @PostMapping("/buildings/research/levelresearch/trainingspeed")
    public void levelTrainingSpeed(@RequestBody ResearchLevelRequest researchLevelRequest) {
       Player player = playerRepository.getById(researchLevelRequest.getPlayerID());
       Research research = player.getResearchManager().getResearch(researchLevelRequest.getResearchName());
       TroopTrainingBuilding archeryRange = player.getTroopBuildings().getTrainingBuilding(BuildingTypes.ARCHERYRANGE);
       TroopTrainingBuilding mageTower = player.getTroopBuildings().getTrainingBuilding(BuildingTypes.MAGETOWER);
       TroopTrainingBuilding warriorSchool = player.getTroopBuildings().getTrainingBuilding(BuildingTypes.WARRIORSCHOOL);
       TroopTrainingBuilding stables = player.getTroopBuildings().getTrainingBuilding(BuildingTypes.STABLES);
       if (research.getLevel() < 5) {
           research.levelUpResearch(research.getLevel() + 1, player.getResearchManager());
           archeryRange.setTrainingTime(Math.floor(archeryRange.getTrainingTime() * .95));
           mageTower.setTrainingTime(Math.floor(mageTower.getTrainingTime() * .95));
           stables.setTrainingTime(Math.floor(stables.getTrainingTime() * .95));
           warriorSchool.setTrainingTime(Math.floor(warriorSchool.getTrainingTime() * .95));
       }
       player.updatePower();
       playerRepository.save(player);
    }

    @PostMapping("/buildings/research/levelresearch/trainingcapacity")
    public void levelTrainingCapacity(@RequestBody ResearchLevelRequest researchLevelRequest) {
       Player player = playerRepository.getById(researchLevelRequest.getPlayerID());
       Research research = player.getResearchManager().getResearch("Training Capacity");
       TroopTrainingBuilding archeryRange = player.getTroopBuildings().getTrainingBuilding(BuildingTypes.ARCHERYRANGE);
       TroopTrainingBuilding mageTower = player.getTroopBuildings().getTrainingBuilding(BuildingTypes.MAGETOWER);
       TroopTrainingBuilding warriorSchool = player.getTroopBuildings().getTrainingBuilding(BuildingTypes.WARRIORSCHOOL);
       TroopTrainingBuilding stables = player.getTroopBuildings().getTrainingBuilding(BuildingTypes.STABLES);
       if (research.getLevel() < 5) {
           research.levelUpResearch(research.getLevel() + 1, player.getResearchManager());
           archeryRange.setTrainingCapacity(archeryRange.getTrainingCapacity() + 10);
           mageTower.setTrainingCapacity(mageTower.getTrainingCapacity() + 10);
           warriorSchool.setTrainingCapacity(warriorSchool.getTrainingCapacity() + 10);
           stables.setTrainingCapacity(stables.getTrainingCapacity() + 10);
       }
       player.updatePower();
       playerRepository.save(player);
    }

    @GetMapping("/buildings/getPlayerBuildings/{playerID}")
    public List<Building> getPlayerBuildings(@PathVariable int playerID)
    {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null)
        {
            List<Building> list = new ArrayList<>();
            list.addAll(player.resourceBuildings.getResourceBuildings());
            list.addAll(player.troopBuildings.getTroopBuildings());
            list.addAll(player.buildings.getOtherBuildings());
            return list;
        }
        return null;
    }

    @GetMapping("/buildings/getBuilding/{buildingID}")
    public Building getBuilding(@PathVariable int buildingID)
    {
        return buildingRepository.findById(buildingID).orElse(null);
    }

    @GetMapping("/buildings/getTotalBuildingPower/{playerID}")
    public long getTotalBuildingPower(@PathVariable int playerID)
    {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null)
        {
            long totalPower = 0;
            totalPower += player.troopBuildings.calculateTotalTroopBuildingPower();
            totalPower += player.resourceBuildings.calculateTotalResourceBuildingPower();
            totalPower += player.buildings.calculateTotalOtherBuildingPower();
            return totalPower;
        }
        return 0;
    }

    @PostMapping("/buildings/upgrade/{playerID}")
    public String upgradeBuilding(@PathVariable int playerID, @RequestBody BuildingRequest buildingRequest)
    {
        Player player = playerRepository.findById(playerID).orElse(null);

        if (player != null)
        {
            switch (buildingRequest.getBuildingType())
            {
                case FARM:
                case LUMBERYARD:
                case PLATINUMMINE:
                case QUARRY:
                    ResourceBuilding resourceBuilding = player.resourceBuildings.getResourceBuilding(buildingRequest.getBuildingType());
                    if (player.resources.getResource(ResourceType.WOOD) >= resourceBuilding.getWoodUpgradeCost() &&
                            player.resources.getResource(ResourceType.STONE) >= resourceBuilding.getStoneUpgradeCost())
                    {
                        try {
                            player.resources.removeResource(ResourceType.STONE, resourceBuilding.getStoneUpgradeCost());
                            player.resources.removeResource(ResourceType.WOOD, resourceBuilding.getWoodUpgradeCost());
                            player.resourceBuildings.upgradeBuilding(buildingRequest.getBuildingType());
                            playerRepository.save(player);
                            return "Building upgraded to level " + player.resourceBuildings.getLevel(buildingRequest.getBuildingType()) + ".";
                        }
                        catch (Exception e)
                        {
                            return e.getMessage();
                        }
                    }
                    else
                    {
                        return "Not Enough Resources";
                    }
                case ARCHERYRANGE:
                case MAGETOWER:
                case STABLES:
                case WARRIORSCHOOL:
                    TroopTrainingBuilding troopTrainingBuilding = player.troopBuildings.getTrainingBuilding(buildingRequest.getBuildingType());
                    if (player.resources.getResource(ResourceType.WOOD) >= troopTrainingBuilding.getWoodUpgradeCost() &&
                            player.resources.getResource(ResourceType.STONE) >= troopTrainingBuilding.getStoneUpgradeCost())
                    {
                        try
                        {
                            player.resources.removeResource(ResourceType.STONE, troopTrainingBuilding.getStoneUpgradeCost());
                            player.resources.removeResource(ResourceType.WOOD, troopTrainingBuilding.getWoodUpgradeCost());
                            player.troopBuildings.upgradeBuilding(buildingRequest.getBuildingType());
                            playerRepository.save(player);
                            return "Building upgraded to level " + player.troopBuildings.getLevel(buildingRequest.getBuildingType()) + ".";
                        }
                        catch (Exception e)
                        {
                            return e.getMessage();
                        }
                    }
                    else
                    {
                        return "Not Enough Resources";
                    }
                case MAINBUILDING:
                case RESEARCHBUILDING:
                    OtherBuilding otherBuilding = player.buildings.getOtherBuilding(buildingRequest.getBuildingType());
                    if (player.resources.getResource(ResourceType.WOOD) >= otherBuilding.getWoodUpgradeCost() &&
                            player.resources.getResource(ResourceType.STONE) >= otherBuilding.getStoneUpgradeCost())
                    {
                        try
                        {
                            player.resources.removeResource(ResourceType.STONE, otherBuilding.getStoneUpgradeCost());
                            player.resources.removeResource(ResourceType.WOOD, otherBuilding.getWoodUpgradeCost());
                            player.buildings.upgradeBuilding(buildingRequest.getBuildingType());
                            playerRepository.save(player);
                            return "Building upgraded to level " + player.buildings.getLevel(buildingRequest.getBuildingType()) + ".";
                        }
                        catch (Exception e)
                        {
                            return e.getMessage();
                        }
                    }
                    else
                    {
                        return "Not Enough Resources";
                    }
            }
        }
        return null;
    }

    public static class ResearchLevelRequest {
        private String researchName;
        private Integer playerID;

        public ResearchLevelRequest(String researchName, Integer playerID) {
            this.researchName = researchName;
            this.playerID = playerID;
        }

        public String getResearchName() {
            return researchName;
        }

        public void setResearchName(String researchName) {
            this.researchName = researchName;
        }

        public Integer getPlayerID() {
            return playerID;
        }

        public void setPlayerID(Integer playerID) {
            this.playerID = playerID;
        }
    }
    @GetMapping("/buildings/getResourcesHeld/{buildingID}")
    public int getResourcesHeld(@PathVariable int buildingID)
    {
        ResourceBuilding building = resourceBuildingRepository.findById(buildingID).orElse(null);
        if (building != null)
        {
            return building.getResources();
        }
        else
        {
            return 0;
        }
    }

    @PostMapping("/buildings/collectResources/{playerID}")
    public int collectResources(@PathVariable int playerID, @RequestBody BuildingRequest buildingRequest)
    {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null)
        {
            int amountCollected = player.resourceBuildings.collectResources(buildingRequest.buildingType);
            switch (buildingRequest.getBuildingType())
            {
                case FARM:
                    player.resources.addResource(ResourceType.FOOD, amountCollected);
                case LUMBERYARD:
                    player.resources.addResource(ResourceType.WOOD, amountCollected);
                case PLATINUMMINE:
                    player.resources.addResource(ResourceType.PLATINUM, amountCollected);
                case QUARRY:
                    player.resources.addResource(ResourceType.STONE, amountCollected);
            }
            playerRepository.save(player);
            return amountCollected;
        }
        return 0;
    }

    @PostMapping("/buildings/updateResources/{playerID}")
    public List<Integer> updateResources(@PathVariable int playerID)
    {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null)
        {
            List<Integer> list = new ArrayList<>();
            for (ResourceBuilding building : player.resourceBuildings.resourceBuildingManager)
            {
                building.updateResources();
                list.add(building.getResources());
            }
            playerRepository.save(player);
            return list;
        }
        return null;
    }

    public static class BuildingRequest
    {
        private BuildingTypes buildingType;

        public BuildingRequest(BuildingTypes buildingType)
        {
            setBuildingType(buildingType);
        }

        public BuildingTypes getBuildingType() {
            return buildingType;
        }

        public void setBuildingType(BuildingTypes buildingType) {
            this.buildingType = buildingType;
        }
    }
}

