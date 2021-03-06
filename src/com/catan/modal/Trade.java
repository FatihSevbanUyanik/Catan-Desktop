package com.catan.modal;

import com.catan.Util.Constants;
import com.catan.Util.UTF8Control;
import com.sun.xml.internal.ws.util.StringUtils;

import java.util.*;

public class Trade {

    // properties
    private Player playerTrader;
    private Player playerToBeTraded;
    private HashMap<String, Integer> requestedResourceCards;
    private HashMap<String, Integer> offeredResourceCards;
    private boolean isTradeWithChest;
    private boolean isTradePossible;
    private boolean isTradeCompleted;
    private String errorMessage;
    private GameLog gameLog = GameLog.getInstance();

    // constructor
    public Trade(Player trader, Player playerToBeTraded, HashMap<String, Integer> reqCards,
                 HashMap<String, Integer> offeredCards, boolean isTradeWithChest) {
        this.isTradeWithChest = isTradeWithChest;
        offeredResourceCards = offeredCards;
        requestedResourceCards = reqCards;
        isTradeCompleted = false;
        isTradePossible = true;
        playerTrader = trader;
        errorMessage = "";

        if (!isTradeWithChest) {
            this.playerToBeTraded = playerToBeTraded;
        }

        // evaluation of the possibility of trade
        if (offeredResourceCards.get(Constants.CARD_WOOL)   == 0 &&
            offeredResourceCards.get(Constants.CARD_GRAIN)  == 0 &&
            offeredResourceCards.get(Constants.CARD_LUMBER) == 0 &&
            offeredResourceCards.get(Constants.CARD_BRICK)  == 0 &&
            offeredResourceCards.get(Constants.CARD_ORE)    == 0) {
            errorMessage = "No offered resources specified.";
            isTradePossible = false;
            return;
        }
        if (requestedResourceCards.get(Constants.CARD_WOOL)   == 0 &&
            requestedResourceCards.get(Constants.CARD_GRAIN)  == 0 &&
            requestedResourceCards.get(Constants.CARD_LUMBER) == 0 &&
            requestedResourceCards.get(Constants.CARD_BRICK)  == 0 &&
            requestedResourceCards.get(Constants.CARD_ORE)    == 0) {
            errorMessage = "No requested resources specified.";
            isTradePossible = false;
            return;
        }

        if (playerToBeTraded != null) {
            HashMap<String, ArrayList<SourceCard>> resourceCards = playerToBeTraded.getSourceCards();
            for (String resourceName: Constants.resourceNames) {
                if (resourceCards.get(resourceName).size() < requestedResourceCards.get(resourceName)) {
                    errorMessage = "The trade request from \n" + playerTrader.getName() +
                            " was denied by " + playerToBeTraded.getName() + ".";
                    isTradePossible = false;
                    break;
                }
            }
        }

        addOfferDetailsToGameLog(isTradeWithChest);
        gameLog = GameLog.getInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("com.catan.resources.language", new Locale(Settings.getInstance().getCurrentLanguage()),  new UTF8Control());

        if (isTradePossible) {
            if (playerToBeTraded instanceof PlayerAI) {
                boolean aiDecision = ((PlayerAI) playerToBeTraded).respondToTradeRequest(
                        requestedResourceCards, offeredResourceCards);

                if (aiDecision) {
                    completeTrade();
                    gameLog.addLog(StringUtils.capitalize(playerTrader.getName()) + " " + bundle.getString("gamelogs_hasTradedWith") + " " + StringUtils.capitalize(playerToBeTraded.getName()) + ".", playerTrader.getColor());
                }
                else {
                    errorMessage = "The trade request from \n" + playerTrader.getName() +
                            " was denied by " + playerToBeTraded.getName() + ".";

                }
            }
            else if (isTradeWithChest) {
                completeTrade();
                gameLog.addLog(StringUtils.capitalize(playerTrader.getName()) + " " + bundle.getString("gamelogs_hasTradedWithChest") + ".", playerTrader.getColor());
            }
        }
        else { printTradeDetails(); }
    }

    // methods
    public void completeTrade() {
        if (isTradePossible) {
            System.out.println("==============================================================================================");
            printPlayerDetails();
            ArrayList<Integer> tradeDifferences = new ArrayList<>();
            // calculating differences
            for (String resourceName: Constants.resourceNames) {
                int difference = requestedResourceCards.get(resourceName) - offeredResourceCards.get(resourceName);
                tradeDifferences.add(difference);
            }
            // arranging resource cards of players
            exchangeResources(tradeDifferences, playerTrader);

            if (playerToBeTraded != null) {
                // Resource Cards of the player to be traded.
                tradeDifferences = new ArrayList<>();
                // calculating differences
                for (String resourceName: Constants.resourceNames) {
                    int difference = offeredResourceCards.get(resourceName) - requestedResourceCards.get(resourceName);
                    tradeDifferences.add(difference);
                }
                // arranging resource cards of players
                exchangeResources(tradeDifferences, playerToBeTraded);
            }
            printTradeDetails();
            printPlayerDetails();
            System.out.println("==============================================================================================");
        }
    }

    private void addOfferDetailsToGameLog(boolean isTradeWithChest) {
        ResourceBundle bundle = ResourceBundle.getBundle("com.catan.resources.language", new Locale(Settings.getInstance().getCurrentLanguage()),  new UTF8Control());
        gameLog = GameLog.getInstance();
        String offereds = "";
        String requests = "";
        for (String resourceName: Constants.resourceNames) {
            if (offeredResourceCards.get(resourceName) > 0) {
                offereds += offeredResourceCards.get(resourceName) + " " + resourceName + ", ";
            }
            if (requestedResourceCards.get(resourceName) > 0) {
                requests += requestedResourceCards.get(resourceName) + " " + resourceName + ", ";
            }
        }
        if (!offereds.equals("")) {
            offereds = offereds.substring(0, offereds.length() - 2);
        }
        if (!requests.equals("")) {
            requests = requests.substring(0, requests.length() - 2);
        }
        if (!isTradeWithChest) {
            gameLog.addLog(StringUtils.capitalize(playerTrader.getName()) + " " + bundle.getString("gamelogs_hasOffered") + " " + StringUtils.capitalize(playerToBeTraded.getName()) + ":" + "\n" +
                    "  " + offereds + "." + "\n" +
                    "  " + StringUtils.capitalize(playerTrader.getName()) + " " + bundle.getString("gamelogs_hasRequested") + ":" + "\n" +
                    "  " + requests + ".", playerTrader.getColor());
        } else {
            gameLog.addLog(playerTrader.getName() + " " + bundle.getString("gamelogs_hasOfferedChest") + ":" + "\n" +
                    "  " + offereds + "." + "\n" +
                    "  " + bundle.getString("gamelogs_chestHasRequired") + ":" + "\n" +
                    "  " + requests + ".", playerTrader.getColor());
        }
    }

    private void exchangeResources(ArrayList<Integer> tradeDifferences, Player player) {
        for (int i = 0; i < tradeDifferences.size(); i++) {
            if (tradeDifferences.get(i) > 0) {
                player.addResources(Constants.resourceNames.get(i), tradeDifferences.get(i));
            } else if (tradeDifferences.get(i) < 0) {
                int difference = -1 * tradeDifferences.get(i);
                player.removeResources(Constants.resourceNames.get(i), difference);
            }
        }
    }

    public void printTradeDetails() {
        if (errorMessage.isEmpty()) {
            if (isTradeWithChest) {
                System.out.println("Trade between " + playerTrader.getName() + " and CHEST:" + isTradePossible);
            }
            showResourceDetails(requestedResourceCards, "OBTAINED RESOURCES");
            showResourceDetails(offeredResourceCards,   "GIVEN RESOURCES");
            isTradeCompleted = true;
        }
        else {
            System.out.println("==============================================================================================");
            System.out.println(errorMessage);
            System.out.println("==============================================================================================");
        }
    }

    private void printPlayerDetails() {
        System.out.println("----------------------------------------------------------------------------------------------");
        if (playerTrader != null) {
            System.out.print("TRADER ==> ");
            playerTrader.showSourceCards();
        }
        if (playerToBeTraded != null) {
            System.out.print("TRADED ==> ");
            playerToBeTraded.showSourceCards();
        }
        System.out.println("----------------------------------------------------------------------------------------------");
    }

    private void showResourceDetails(HashMap<String, Integer> map, String mapTitle) {
        System.out.print("| ");
        for (String resourceName: Constants.resourceNames) {
            int count = map.get(resourceName);
            System.out.print(resourceName + " --> " + count + " | ");
        }
        System.out.println(" => " + mapTitle);
    }

    public Player getPlayerTrader() {
        return playerTrader;
    }

    public Player getPlayerToBeTraded() {
        return playerToBeTraded;
    }

    public HashMap<String, Integer> getRequestedResourceCards() {
        return requestedResourceCards;
    }

    public HashMap<String, Integer> getOfferedResourceCards() {
        return offeredResourceCards;
    }

    public boolean isTradePossible() {
        return isTradePossible;
    }

    public boolean isTradeCompleted() {
        return isTradeCompleted;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}