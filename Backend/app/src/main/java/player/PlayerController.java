package player;

import buildings.BuildingController;
import buildings.BuildingManager;
import buildings.BuildingTypes;
import buildings.Research.Research;
import buildings.Research.ResearchManager;
import buildings.Research.ResearchRepository;
import buildings.resourcebuildings.ResourceBuildingManager;
import buildings.troopBuildings.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import resources.ResourceManager;
import resources.ResourceType;
import troops.*;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static troops.TroopTypes.ARCHER;

/**
 * A REST controller for managing a player's information
 * @author Michael Geltz
 */
@RestController
public class PlayerController {

    // Player repository storing players data

    @Autowired
    private PlayerRepository playerRepository;


    // Returns all currently existing players and their info

    /**
     * Gets all the players stored in the database
     * @return Every player in the database
     */
    @GetMapping("/players/getall")
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    // Gets a specific players info

    /**
     * Gets a specific player based on ID
     * @param playerID
     * @return A specific player based on ID
     */
    @GetMapping("/players/getPlayer/{playerID}")
    public Player getPlayer(@PathVariable int playerID) {
        return playerRepository.findById(playerID).orElse(null);
    }


    // Creates a new player, to test use Postman POST option with url:
    // http://coms-309-048.class.las.iastate.edu:8080/players/new (then send in a raw JSON body, {"userName" : "(name)", "password" : "(password)"}

    /**
     * Creates a new player based on a provided username and password
     * @param created
     * @return A string showing the ID the new player was created with
     */
    @PostMapping("/players/new")
    public String newPlayer(@RequestBody PlayerCreator created) {

        // Create empty player and store username and password to generate a player ID used in the managers
        if(!passwordCheck(created.getPassword())) {
            return "Invalid password, length needs to be >= 7, contain at least one lowercase character, one uppercase character, one special character, and one digit";
        }
        else {
            Player player = new Player();
            Random rand = new Random();
            player.setUserName(created.getUserName());
            player.setPassword(created.getPassword());
            // Save "empty" player to generate ID
            player = playerRepository.save(player);
            player.setTroops(new TroopManager(player.getPlayerID()));
            player.setResources(new ResourceManager(player.getPlayerID()));
            player.setResearchManager(new ResearchManager(player.getPlayerID()));
            player.setBuildings(new BuildingManager(player.getPlayerID()));
            player.setTroopBuildings(new TroopBuildingManager(player.getPlayerID()));
            player.setResourceBuildings(new ResourceBuildingManager(player.getPlayerID()));
            player.setLocationX(rand.nextInt(30));
            player.setTotalKills(0);
            player.setLocationY(rand.nextInt(30));
            //Save fully created player into database
            playerRepository.save(player);
            // Return id of created player
            return "New player of ID: " + player.getPlayerID();
        }
    }

    // Returns sorted list of players based on power, descending order

    /**
     * Sorts the player repository based on power level, descending
     * @return A list of all players sorted by descending power
     */
    @GetMapping("/players/getsorted")
    public List<Player> getSortedPlayers() {
        List<Player> sortedList = playerRepository.findAll();
        return sortedList.stream()
                .sorted((z, y) -> Double.compare(y.getPower(), z.getPower()))
                .collect(Collectors.toList());
    }

    // Addtroops to a declared player via their ID, to use, use Postman POST option, make sure you already have a player declared and use:
    // http://coms-309-048.class.las.iastate,edu:8080/players/addtroops/(playerID) --> Raw JSON, {"troopType" : "(trooptype)", "quantity" : (integer)}
    // An example request: http://coms-309-048.class.las.iastate,edu:8080/players/addtroops/1 --> {"troopType" : "ARCHER", "quantity" : 100}

    /**
     * Adds troops to a specific player based on ID, trooptype, and quantity
     * @param playerID
     * @param troopRequest
     * @return A JSON of the player's info
     */
    @PostMapping("/players/addtroops/{playerID}")
    public Player addTroops(@PathVariable int playerID, @RequestBody TroopRequest troopRequest) {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null) {
            player.troops.addTroop(troopRequest.getTroopType(), troopRequest.getQuantity());
            player.updatePower();
            return playerRepository.save(player);
        }
        else {
            return null;
        }
    }

    @PostMapping("/players/changePlayerLocation/{playerID}")
    public Player changePlayerLocation(@PathVariable int playerID, @RequestBody LocationRequest locationRequest)
    {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null)
        {
            player.setLocationX(locationRequest.getLocationX());
            player.setLocationY(locationRequest.getLocationY());
            return playerRepository.save(player);
        }
        else
        {
            return null;
        }
    }

    @PostMapping("/players/calculateTrainingTime/{playerID}")
    public Player calculateTrainingTime(@PathVariable int playerID, @RequestBody TroopRequest troopRequest)
    {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null)
        {
            Troop troop = player.troops.getTroop(troopRequest.getTroopType());
            String formattedTime;
            switch (troopRequest.getTroopType())
            {
                case ARCHER:
                    if (player.resources.getResource(ResourceType.FOOD) >= player.troopBuildings.getTrainingBuilding(BuildingTypes.ARCHERYRANGE).getTrainingCost() * troopRequest.getQuantity())
                    {
                        player.resources.removeResource(ResourceType.FOOD, player.troopBuildings.getTrainingBuilding(BuildingTypes.ARCHERYRANGE).getTrainingCost() * troopRequest.getQuantity());
                        formattedTime = player.troopBuildings.trainTroops(BuildingTypes.ARCHERYRANGE, troopRequest.getQuantity());
                        player.setArcherFinalDate(formattedTime);
                        return playerRepository.save(player);
                    }
                    else
                    {
                        throw new RuntimeException("Not Enough Food!");
                    }
                case MAGE:
                    if (player.resources.getResource(ResourceType.FOOD) >= player.troopBuildings.getTrainingBuilding(BuildingTypes.MAGETOWER).getTrainingCost() * troopRequest.getQuantity())
                    {
                        player.resources.removeResource(ResourceType.FOOD, player.troopBuildings.getTrainingBuilding(BuildingTypes.MAGETOWER).getTrainingCost() * troopRequest.getQuantity());
                        formattedTime = player.troopBuildings.trainTroops(BuildingTypes.MAGETOWER, troopRequest.getQuantity());
                        player.setMageFinalDate(formattedTime);
                        return playerRepository.save(player);
                    }
                    else
                    {
                        throw new RuntimeException("Not Enough Food!");
                    }
                case CAVALRY:
                    if (player.resources.getResource(ResourceType.FOOD) >= player.troopBuildings.getTrainingBuilding(BuildingTypes.STABLES).getTrainingCost() * troopRequest.getQuantity())
                    {
                        player.resources.removeResource(ResourceType.FOOD, player.troopBuildings.getTrainingBuilding(BuildingTypes.STABLES).getTrainingCost() * troopRequest.getQuantity());
                        formattedTime = player.troopBuildings.trainTroops(BuildingTypes.STABLES, troopRequest.getQuantity());
                        player.setCavalryFinalDate(formattedTime);
                        return playerRepository.save(player);
                    }
                    else
                    {
                        throw new RuntimeException("Not Enough Food!");
                    }
                case WARRIOR:
                    if (player.resources.getResource(ResourceType.FOOD) >= player.troopBuildings.getTrainingBuilding(BuildingTypes.WARRIORSCHOOL).getTrainingCost() * troopRequest.getQuantity())
                    {
                        player.resources.removeResource(ResourceType.FOOD, player.troopBuildings.getTrainingBuilding(BuildingTypes.WARRIORSCHOOL).getTrainingCost() * troopRequest.getQuantity());
                        formattedTime = player.troopBuildings.trainTroops(BuildingTypes.WARRIORSCHOOL, troopRequest.getQuantity());
                        player.setWarriorFinalDate(formattedTime);
                        return playerRepository.save(player);
                    }
                    else
                    {
                        throw new RuntimeException("Not Enough Food!");
                    }
            }
        }
        return null;
    }

    // Removetroops from a declared player via their ID, to use, use Postman POST option, make sure you have a player declared and use:
    // http://coms-309-048.class.las.iastate,edu:8080/players/removetroops/(playerID) --> Raw JSON, {"troopType" : "(trooptype)", "quantity" : (integer)}
    // An example request: http://coms-309-048.class.las.iastate,edu:8080/players/removetroops/1 --> {"troopType" : "ARCHER", "quantity" : 100}
    /**
     * Removes troops from a specific player based on ID, trooptype, and quantity
     * @param playerID
     * @param troopRequest
     * @return A JSON of the player's info
     */
    @PostMapping("/players/removetroops/{playerID}")
    public Player removeTroops(@PathVariable int playerID, @RequestBody TroopRequest troopRequest) {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null) {
            player.troops.removeTroop(troopRequest.getTroopType(), troopRequest.getQuantity());
            player.updatePower();
            return playerRepository.save(player);
        }
        else {
            return null;
        }
    }

    // Add resource from a declared player via their ID, to use, use Postman POST option, make sure you have a player declared and use:
    // http://coms-309-048.class.las.iastate,edu:8080/players/addResource/(playerID) --> Raw JSON, {"resourceType" : "(resourcetype)", "quantity" : (integer)}
    // An example request: http://coms-309-048.class.las.iastate,edu:8080/players/addresource/1 --> {"resourceType" : "WOOD", "quantity" : 100}
    /**
     * Adds resources to a specific player based on ID, resourcetype, and quantity
     * @param playerID
     * @param resourceRequest
     * @return A JSON of the player's info
     */
    @PostMapping("/players/addresource/{playerID}")
    public Player addResource(@PathVariable int playerID, @RequestBody ResourceRequest resourceRequest) {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null) {
            player.resources.addResource(resourceRequest.getResourceType(), resourceRequest.getQuantity());
            return playerRepository.save(player);
        }
        else {
            return null;
        }
    }

    // Remove resource from a declared player via their ID, to use, use Postman POST option, make sure you have a player declared and use:
    // http://coms-309-048.class.las.iastate,edu:8080/players/removeresource/(playerID) --> Raw JSON, {"resourceType" : "(resourcetype)", "quantity" : (integer)}
    // An example request: http://coms-309-048.class.las.iastate,edu:8080/players/removeresource/1 --> {"resourceType" : "WOOD", "quantity" : 100}
    /**
     * Removes resources from a specific player based on ID, resourcetype, and quantity
     * @param playerID
     * @param resourceRequest
     * @return A JSON of the player's info
     */
    @PostMapping("/players/removeresource/{playerID}")
    public Player removeResource(@PathVariable int playerID, @RequestBody ResourceRequest resourceRequest) {
        Player player = playerRepository.findById(playerID).orElse(null);
        if (player != null) {
            player.resources.removeResource(resourceRequest.getResourceType(), resourceRequest.getQuantity());
            return playerRepository.save(player);
        }
        else {
            return null;
        }
    }

    // Delete or "ban" a player from the server, effectively removes all information and data of the player from the database
    // To test use Postman DELETE option, make sure the player you are trying to delete exist, use:
    // http://coms-309-048.class.las.iastate,edu:8080/players/banplayer/(playerID)
    // Example request: http://coms-309-048.class.las.iastate,edu:8080/players/banplayer/1

    /**
     * Deletes a player from the database based on playerID
     * @param playerID
     * @return Infomation about the player deleted
     */
    @DeleteMapping("/players/banplayer/{playerID}")
    public String banPlayer(@PathVariable int playerID) {
        // Remove provided player from db
        playerRepository.deleteById(playerID);
        // Return basic string saying player banned etc
        return "Player of ID: " + playerID +" banned from server & removed from database";
    }

    // fight method can be used to see the result of two existing players armies battling
    // To use this method, use Postman GET option, make sure the two players exist and at least have some troops:
    // http://coms-309-048.class.las.iastate,edu:8080/players/fight/(playerID1)/(playerID2)
    // Example request: http://coms-309-048.class.las.iastate,edu:8080/players/fight/1/2

    /**
     * Simulates and calculates a battle result between two players
     * @param playerID1
     * @param playerID2
     * @return String detailing the result of the battle
     */
    @GetMapping("/players/fight/{playerID1}/{playerID2}")
    public String fight(@PathVariable int playerID1, @PathVariable int playerID2) {
        // Declare new troopcombatcalculator to determine battle result between two players
        // player troop counts updated in the combat calculator
        Player p1 = playerRepository.findById(playerID1).orElse(null);
        Player p2 = playerRepository.findById(playerID2).orElse(null);
        if (p1 != null && p2 != null) {
            TroopCombatCalculator tcc = new TroopCombatCalculator(p1.getTroops(), p2.getTroops());
            p1.updatePower();
            p2.updatePower();
            p1.setTotalKills(p1.getTroops().getTotalTroopKills());
            p2.setTotalKills(p2.getTroops().getTotalTroopKills());
            playerRepository.save(p1);
            playerRepository.save(p2);

            return tcc.getResult() + "\n" + p1.getPlayerID() + "\n\n" +  p2.getPlayerID();
        }
        else {
            return null;
        }
    }

    public static boolean passwordCheck(String password) {
        if (password.length() >= 7) {
            Pattern lower = Pattern.compile("[a-z]");
            Pattern upper = Pattern.compile("[A-Z]");
            Pattern digit = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile("[!@#$%^&*()_\\-+=|<>,./?{}\\[\\]~`:;']");

            Matcher hasUpper = upper.matcher(password);
            Matcher hasLower = lower.matcher(password);
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);

            return hasSpecial.find() && hasLower.find() && hasDigit.find() && hasUpper.find();
        }
        else {
            return false;
        }
    }

    public static class LocationRequest {
        private int locationX;
        private int locationY;

        public LocationRequest(int locationX, int locationY)
        {
            setLocationX(locationX);
            setLocationY(locationY);
        }

        public int getLocationX() {
            return locationX;
        }

        public void setLocationX(int locationX) {
            this.locationX = locationX;
        }

        public int getLocationY() {
            return locationY;
        }

        public void setLocationY(int locationY) {
            this.locationY = locationY;
        }
    }
    // Class used for managing resource requests

    /**
     * Resource request class to make sending resource requests easier
     */
    public static class ResourceRequest {
        private ResourceType resourceType;
        private int quantity;

        /**
         * Creates a resource request based on resourcetype and quantity
         * @param resourceType
         * @param quantity
         */
        public ResourceRequest(ResourceType resourceType, int quantity) {
            setResourceType(resourceType);
            setQuantity(quantity);
        }

        /**
         * Gets the resourcetype of the request
         * @return resourcetype of request
         */
        public ResourceType getResourceType() {
            return resourceType;
        }

        /**
         * Sets the resourcetype requested
         * @param resourceType
         */
        public void setResourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
        }

        /**
         * Gets the quantity of a resource requested
         * @return quantity of resource requested
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Sets the quantity of the resource requested
         * @param quantity
         */
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    // Class used for managing player creation

    /**
     * Player creator class to make sending player creation requests easier
     */
    public static class PlayerCreator {
        private String userName;
        private String password;


        /**
         * Creates a player creation request based on username and password
         * @param userName
         * @param password
         */
        public PlayerCreator(String userName, String password) {
            setUserName(userName);
            setPassword(password);
        }

        /**
         * Gets the username of the requested player to be created
         * @return username of player being created
         */
        public String getUserName() {
            return userName;
        }

        /**
         * Sets the username of the player being created
         * @param userName
         */
        public void setUserName(String userName) {
            this.userName = userName;
        }

        /**
         * Gets the password of the player being created
         * @return password of player being created
         */
        public String getPassword() {
            return password;
        }

        /**
         * Sets the password of the player being created
         * @param password
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }

    // Class for managing troop requests

    /**
     * Troop request class to make troop requests easier to send
     */
    public static class TroopRequest {
        private TroopTypes troopType;
        private int quantity;

        /**
         * Creates a troop request based on trooptype and quantity
         * @param troopType
         * @param quantity
         */
        public TroopRequest(TroopTypes troopType, int quantity) {
            setTroopType(troopType);
            setQuantity(quantity);
        }

        /**
         * Gets the trooptype of the troop request
         * @return trooptype of troop request
         */
        public TroopTypes getTroopType() {
            return troopType;
        }

        /**
         * Sets the trooptype of a troop request
         * @param troopType
         */
        public void setTroopType(TroopTypes troopType) {
            this.troopType = troopType;
        }

        /**
         * Gets the quantity of troops requested
         * @return quantity of troops requested
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Sets the quantity of troops requested
         * @param quantity
         */
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
