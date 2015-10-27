package com.WineDrinker; /**
 * Created by justy on 10/23/2015.
 */
import com.runemate.game.api.client.paint.PaintListener;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import com.runemate.game.api.hybrid.queries.results.SpriteItemQueryResults;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingScript;

import java.awt.*;
import java.util.ListIterator;

public class main extends LoopingScript implements PaintListener {

    private final static String BOOTH = "Grand Exchange booth";
    public String currentState = "";
    private SpriteItem wine;
    private String name = "WineDrinker";
    private Integer centerTitle = calcCenter(name, 200);
    private int drank;
    private SpriteItem oldWine;
    private final static StopWatch runtime = new StopWatch();

    @Override
    public void onStart(String... args){
        setLoopDelay(250, 700);
        getEventDispatcher().addListener(this);
        drank = 0;
        runtime.start();
    }

    @Override
    public void onLoop() {
        if (RuneScape.isLoggedIn() && isIdle()) {
            if (Inventory.contains("Jug of wine") && isIdle() && !Bank.isOpen()) {
                //System.out.println("test");
                /* Drinking time */
                if (Bank.isOpen()) {
                    Bank.close();
                }
                currentState = "DRINK";
                SpriteItemQueryResults wines = Inventory.getItems("Jug of wine");
                ListIterator<SpriteItem> iterator = wines.listIterator();
                wine = iterator.next();
                while (wine!=null) {
                    wine.click();
                    Execution.delay(50,100);
                    oldWine = wine;
                    if (iterator.hasNext()) {
                        wine = iterator.next();
                        wine.getBounds().getInteractionPoint().hover();
                    }else{
                        wine = null;
                    }
                    Execution.delayUntil(this::isNotValid, 650, 1500);
                    Execution.delay(600,800);
                    if(oldWine != null && !oldWine.isValid()){
                        drank = drank+1;
                    }
                    //drank = drank+1;
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
            } else if(Bank.isOpen()) {
                Bank.close();
            }else{
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
        g.fillRect(0, 0, 200, 75);
        g.drawRect(0, 0, 200, 75);
        g.setColor(Color.white);
        g.drawString(name, centerTitle, 15);
        g.drawString("Status: " + getCurrentAction(), 5, 30);
        g.drawString("Wine drank: " + drank, 5, 45);
        g.drawString("Time run: " + runtime.getRuntimeAsString(), 5, 60);
        //g.drawString("Time run: " + Math.round(drank/(runtime.getRuntime()/1000)), 5, 75);
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

    boolean isNotValid(){
        if(!oldWine.isValid() && Players.getLocal().getAnimationId() == -1){
            drank = drank+1;
            return true;
        }else {
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
