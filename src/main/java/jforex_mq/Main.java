package jforex_mq;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ISystemListener;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Main {
    private IClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    //url of the DEMO jnlp
    private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
    private static String userName = "DEMO1234";
    private static String password = "mypassword";

    private static final int reconnectsLimit = 5;

    public static void main(String[] args) throws Exception {
        final IClient client = ClientFactory.getDefaultInstance();
        client.setSystemListener(new ISystemListener() {
            private int reconnects = reconnectsLimit;

            @Override
            public void onStart(long processId) {
                LOGGER.info("Strategy started: " + processId);
            }

            @Override
            public void onStop(long processId) {
                LOGGER.info("Strategy stopped: " + processId);
                if (client.getStartedStrategies().size() == 0) {
                    System.exit(0);
                }
            }

            @Override
            public void onConnect() {
                LOGGER.info("Connected");
                reconnects = reconnectsLimit;
            }

            @Override
            public void onDisconnect() {
                LOGGER.warn("Disconnected");
                if (reconnects > 0) {
                    client.reconnect();
                    --reconnects;
                } else {
                    try {
                        //sleep for 5 seconds before attempting to reconnect
                        Thread.sleep(5000);
                    } catch (InterruptedException e) { }
                    try {
                        client.connect(jnlpUrl, userName, password);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });

        LOGGER.info("Connecting...");
        //connect to the server using jnlp, user name and password
        client.connect(jnlpUrl, userName, password);

        //wait for it to connect
        int i = 15; //wait max fifteen seconds
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            LOGGER.error("Failed to connect to Dukascopy server");
            System.exit(1);
        }
    }
}
