package com.WineDrinker; /**
 * Created by justy on 10/23/2015.
 */
import com.runemate.game.api.client.paint.PaintListener;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.entities.details.Locatable;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.Traversal;
import com.runemate.game.api.hybrid.location.navigation.web.Web;
import com.runemate.game.api.hybrid.location.navigation.web.WebPath;
import com.runemate.game.api.hybrid.location.navigation.web.WebPathBuilder;
import com.runemate.game.api.hybrid.queries.results.SpriteItemQueryResults;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingScript;

import java.awt.*;
import java.util.ListIterator;

public class main extends LoopingScript implements PaintListener {

    private final static String BOOTH = "Grand Exchange booth";
    public String currentState = "";
    public ListIterator<SpriteItem> iterator = null;

    @Override
    public void onStart(String... args){
        setLoopDelay(250, 700);
        getEventDispatcher().addListener(this);
    }

    @Override
    public void onLoop() {
        if (RuneScape.isLoggedIn() && isIdle()) {
            if (Inventory.contains("Jug of wine") && isIdle()) {
                //System.out.println("test");
                /* Drinking time */
                if (Bank.isOpen()) {
                    Bank.close();
                }
                currentState = "DRINK";
                SpriteItemQueryResults wines = Inventory.getItems("Jug of wine");
                ListIterator<SpriteItem> iterator = wines.listIterator();
                while (iterator.hasNext()) {
                    iterator.next().interact("Drink");
                    Execution.delay(1000);
                    Execution.delayUntil(this::isIdle, 20000, 22000);
                }
            } else if (RuneScape.isLoggedIn() && Inventory.contains("Jug") && Inventory.containsAnyExcept("Jug of wine")) {
                //System.out.println("test3");
                /* Time to bank */
                currentState = "DEPOSIT";
                if (!Bank.isOpen()) {
                    Bank.open();
                    Execution.delayUntil(Bank::isOpen, 250, 2200);
                } else {
                    Bank.depositInventory();
                    Execution.delayUntil(Inventory::isEmpty, 250, 1500);
                }
            } else if (RuneScape.isLoggedIn() && !Inventory.contains("Jug of wine")) {
                //System.out.println("test2");
                /* Need to grab more Jug of wine */
                currentState = "WITHDRAW";
                if (!Bank.isOpen()) {
                    System.out.println("No wine, opening bank...");
                    Bank.open();
                    Execution.delayUntil(Bank::isOpen, 250, 2000);
                } else {
                    System.out.println("Grabbing wine...");
                    if (Bank.getQuantity("Jug of wine") == 0) {
                        if (Bank.isOpen()) {
                            Bank.close();
                            Execution.delayUntil(this::isBankClosed, 2000, 2200);
                        }
                        RuneScape.logout();
                        Execution.delayUntil(this::isLoggedOut, 2000, 2200);
                        stop();
                    }
                    Bank.withdraw("Jug of wine", 28);
                    Execution.delayUntil(Inventory::isFull, 250, 1500);
                }
            } else {
                currentState = "WAIT";
            }
        }
    }

    @Override
    public void onStop(){
    }

    @Override
    public void onPaint(Graphics2D g) {
        Color transBlack = new Color(0, 0, 0, 150);
        g.setColor(transBlack);
        BasicStroke bs3 = new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g.setStroke(bs3);
        Integer width = 200;
        Integer height = 45;
        g.fill3DRect(0, 0, width, height, true);
        g.setColor(Color.white);
        String name = "WineDrinker";
        Integer centerTitle = calcCenter(name, width);
        g.drawString(name, centerTitle, 15);
        g.drawString("Status: "+getCurrentAction(), 5, 30);
    }

    private String getCurrentAction(){
        switch(currentState) {
            case "WITHDRAW":
                return "Withdrawing Jug of wine...";
            case "DEPOSIT":
                return "Depositing Jugs...";
            case "DRINK":
                return "Drinking Jug of wine...";
            default:
                return "Waiting for action to finish...";
        }
    }

    private boolean isIdle() {
        if (RuneScape.isLoggedIn()) {
            Player avatar = Players.getLocal();
            Boolean idle = avatar.getAnimationId() == -1 && !avatar.isMoving();
            //System.out.println(idle);
            return idle;
        } else {
            return false;
        }
    }

    public boolean drink(SpriteItem it) {
        System.out.println(it.getDefinition().getName());
        if (it.getDefinition().getName().contains("Jug of wine")) {
            it.interact("Drink");
            return true;
        } else {
            return false;
        }
    }

    public boolean isLoggedOut() {
        return !RuneScape.isLoggedIn();
    }

    public boolean isBankClosed() {
        return !Bank.isOpen();
    }

    private Integer calcCenter(String str, Integer w) {
        return w / 2 - str.length() - 2;
    }
}
