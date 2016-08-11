package uk.ac.ebi.spot.threads;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Temporary Test thread
 * Created by olgavrou on 10/08/2016.
 */
@Component
@Scope("prototype")
public class PrintThread extends Thread {

    @Override
    public void run() {

        System.out.println(getName() + " is running");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(getName() + " is running");
    }

}
