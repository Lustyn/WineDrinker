package com.WineDrinker; /**
 * Created by justy on 10/23/2015. (*cough* and valithor, ;))
 */
import com.runemate.game.api.client.paint.PaintListener;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.util.StopWatch;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingScript;

import java.awt.*;

public class main extends LoopingScript implements PaintListener {

    public String currentState = "";
    private SpriteItem wine;
    private String name = "WineDrinker";
    private Integer centerTitle = calcCenter(name, 200);
    private int drank;
    private final static StopWatch runtime = new StopWatch();
    private float time;

    @Override
    public void onStart(String... args){
        setLoopDelay(300, 500);
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
                currentState = "DRINK";
                wine = Inventory.getItems("Jug of wine").first();
                wine.click();
                Execution.delay(100, 150);
                if(Inventory.getItems("Jug of wine").size() > 1 && Inventory.getItems("Jug of wine").get(1).isValid()){
                    Inventory.getItems("Jug of wine").get(1).getInteractionPoint().hover();
                }
                Execution.delayUntil(this::isNotValid, 750);
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
        g.fillRect(0, 0, 200, 105);
        g.drawRect(0, 0, 200, 105);
        g.setColor(Color.white);
        g.drawString(name, centerTitle, 15);
        g.drawString("Status: " + getCurrentAction(), 5, 30);
        g.drawString("Wine drank: " + (drank/2), 5, 45);
        g.drawString("Time run: " + runtime.getRuntimeAsString(), 5, 60);
        if(drank>0){
            time = runtime.getRuntime()/(drank/2);
        }else{
            time = 1;
        }
        g.drawString("Time per wine: " + time/1000, 5, 75);
        g.drawString("Wine per minute: " + Math.round(60 / (time / 1000)), 5, 90);
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
            //System.out.println(idle);
            return avatar.getAnimationId() == -1 && !avatar.isMoving();
        } else {
            return false;
        }
    }

    boolean isNotValid(){
        if(!wine.isValid()){
            drank=drank+1;
            return true;
        }
        return false;
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
