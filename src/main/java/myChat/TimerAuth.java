package myChat;

import java.util.Timer;
import java.util.TimerTask;

public class TimerAuth {
    private int startSec;
    private final int limiterSec;
    private static final Object mon = new Object();
    private final ClientHandler clientHandler;

    public TimerAuth(int limitAuthSec, ClientHandler clientHandler) {
        this.limiterSec = limitAuthSec;
        this.clientHandler = clientHandler;
    }

    Timer timer = new Timer();

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            synchronized (mon) {
                startSec++;
                if (!clientHandler.authOk && isTimerDeath()) {
                    clientHandler.sendMessage("/end");
                    timer.cancel();
                    return;
                }
                System.out.println("Sec " + startSec);
            }
        }
    };

    private boolean isTimerDeath() {
        if (startSec == limiterSec) {
            timer.cancel();
            System.out.println("Last sec " + startSec);

            return true;
        }
        return false;
    }

    public void startTimer() {
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    public void stopTimer() {
        System.out.println("Timer stopped");
        timer.cancel();
    }
}

