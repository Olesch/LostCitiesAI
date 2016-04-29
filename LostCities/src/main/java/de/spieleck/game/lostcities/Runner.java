package de.spieleck.game.lostcities;

import java.util.Set;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class Runner {

    private final static String STRAT_PACKAGE = "de.spieleck.game.lostcities.strategy.";
    private final static String STRAT_POSTFIX = "Strategy";
    public final static int RUN_LIMT = 200;

    public static void main(String[] args)
        throws Exception
    {
        // Card.deck().stream().forEach(System.out::print);
        // System.out.println();
        Random rand = new Random();
        boolean startsWithNo = false;
        try { 
            Integer.parseInt(args[0]);
            startsWithNo = true;
        } catch ( Exception ignore ) { };
        if ( startsWithNo && args.length > 2 ) {
            int rounds = Integer.parseInt(args[0]);
            Strategy[] strats = new Strategy[args.length-1];
            String[] stratName = new String[strats.length];
            for(int i = 1; i < args.length; i++) {
                stratName[i-1] = args[i];
                strats[i-1] = load(args[i]);
            }
            int[] points = new int[strats.length];
            int[] score  = new int[strats.length];
            long t1 = System.currentTimeMillis();
            for(int i = 0; i < rounds; i++) {
                // Smoothing: Same game for all pairs in all directions!
                GameState gs0 = new GameState();
                for(int k = 0; k < strats.length; k++) {
                    for(int l = 0; l < strats.length; l++) if ( l != k ) {
                        GameState gs = gs0;
                        while ( !gs.isEnded(RUN_LIMT) ) {
                            Perspective p = gs.perspective();
                            Strategy s = gs.turnNo() % 2 == 0 ? strats[k] : strats[l];
                            Move m = s.decide(p);
                            gs = gs.turn(m);
                        }
                        int scoreK = gs.getScoreA();
                        int scoreL = gs.getScoreB();
                        // System.out.format("%4d:%-4d %s - %s\n", scoreK, scoreL, stratName[k], stratName[l]+" "+gs); 
                        score[k] += scoreK;
                        score[l] += scoreL;
                        if ( scoreK > scoreL ) points[k] += 2;
                        else if ( scoreL > scoreK ) points[l] += 2;
                        else {
                            points[l]++;
                            points[k]++;
                        }
                    }
                }
            }
            long t2 = System.currentTimeMillis();
            System.out.format("*** %.2fs  %.1fms/s\n", 0.001*(t2-t1), (double)(t2-t1)/strats.length/strats.length);
            for(int i = 0; i < strats.length; i++) {
                System.out.format("  %6d  %8d : %s\n", points[i], score[i], stratName[i]);
            }
        } else if ( args.length == 3) {
            Strategy s1 = load(args[0]);
            Strategy s2 = load(args[1]);
            int games = Integer.parseInt(args[2]);
            long t1 = System.currentTimeMillis();
            int win1 = 0, win2 = 0, draw = 0, total1 = 0, total2 = 0;
            for(int i = 0; i < games; i++) {
                GameState gs = new GameState();
                while ( !gs.isEnded() ) {
                    Perspective p = gs.perspective();
                    Strategy s = (i + gs.turnNo()) % 2 == 0 ? s1 : s2;
                    Move m = s.decide(p);
                    gs = gs.turn(m);
                }
                int score1 = i % 2 == 0 ? gs.getScoreA() : gs.getScoreB();
                total1 += score1;
                int score2 = i % 2 == 0 ? gs.getScoreB() : gs.getScoreA();
                total2 += score2;
                if ( score1 > score2 ) win1++;
                else if ( score2 > score1 ) win2++;
                else draw++;
            }
            long t2 = System.currentTimeMillis();
            System.out.format("dt=%.2fs;  win1=%4d; win2=%4d; draw=%3d (%6.2f%%);  avg1=%6.2f; avg2=%6.2f\n", 0.001*(t2-t1), win1, win2, draw, 100.0 *(win1 + 0.5*draw)/games, (double)total1/games, (double)total2/games);
        } else if ( args.length == 2 ) {
            Strategy s1 = load(args[0]);
            Strategy s2 = load(args[1]);
            GameState gs = new GameState();
            System.out.println(gs.perspective(true).niceString());
            System.out.println(gs.perspective(false).niceString());
            while ( !gs.isEnded(RUN_LIMT) ) {
                Perspective p = gs.perspective();
                List<Move> legals = p.legalMoves();
                Strategy s = gs.turnNo() % 2 == 0 ? s1 : s2;
                System.out.println("--- "+gs.turnNo()+" ("+s.getClass().getSimpleName()+") "+p.myScore()+":"+p.hisScore()+".:");
                System.out.println("    "+p.niceString());
                Move m = s.decide(p);
                System.out.println("  "+m+ " of "+legals.size()+";  "+m.draw+":"+p.isColourDrawable(m.draw));
                gs = gs.turn(m);
            }
            System.out.println("=== "+gs.turnNo()+" "+gs.getScoreA()+":"+gs.getScoreB()+" "+s1.getClass().getSimpleName()+"-"+s2.getClass().getSimpleName());
        } else {
            System.err.println("Commandline variants:");
            System.err.println("   StrategyClass1 StrategyClass2 ==> duell, lots of output");
            System.err.println("   StrategyClass1 StrategyClass2 number ==> n matches, summary output");
            System.err.println("   number StrategyClass1 StrategyClass2 ... ==> smoothed tournament, summary output");
            System.exit(1);
        }
    }

    public static Strategy load(String s)
    {
        try {
            return (Strategy)Class.forName(STRAT_PACKAGE+s+STRAT_POSTFIX).newInstance();
        } catch ( Exception ex ) {
            try {
                return (Strategy)Class.forName(STRAT_PACKAGE+s).newInstance();
            } catch ( Exception ex1 ) {
                try {
                    return (Strategy)Class.forName(s).newInstance();
                } catch ( Exception ex2 ) {
                    return null;
                }
            }
        }
    }
}
