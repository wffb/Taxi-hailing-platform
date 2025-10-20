package config;

import servlet.TestServelet;
import servlet.driver.GetDriverScheduleServlet;
import servlet.driver.UpdateDriverScheduleServlet;
import servlet.ride.*;
import servlet.user.LoginServlet;
import servlet.user.LogoutServlet;
import servlet.user.RegisterServlet;
import servlet.wallet.ChargeWalletServlet;
import servlet.wallet.GetWalletServlet;
import servlet.wallet.TransferWalletServlet;


import javax.servlet.http.HttpServlet;
import java.util.Map;

import java.util.Map;

public class ServeletConfig {

    public static Map<String, HttpServlet> loadServlets() {
        return Map.ofEntries(
                Map.entry("/test", new TestServelet()),

                // driver servlets
                Map.entry("/schedules/getAll", new GetDriverScheduleServlet()),
                Map.entry("/driver/getDrivers", new GetDriverScheduleServlet()),
                Map.entry("/schedules/update", new UpdateDriverScheduleServlet()),

                // ride servlets
                Map.entry("/ride/getAllRides", new GetAllRidesServlet()),
                Map.entry("/ride/requestRide", new RequestRideServlet()),
                Map.entry("/ride/accept", new AcceptRideServlet()),
                Map.entry("/ride/start", new StartRideServlet()),
                Map.entry("/ride/complete", new CompleteRideServlet()),
                Map.entry("/ride/cancel", new CancelRideServlet()),
                Map.entry("/ride/getRidesByUser", new GetRidesByUserServlet()),

                // user servlets
                Map.entry("/user/register", new RegisterServlet()),
                Map.entry("/user/logout", new LogoutServlet()),
                Map.entry("/user/login", new LoginServlet()),

                // wallet servlets
                Map.entry("/wallet", new GetWalletServlet()),
                Map.entry("/wallet/charge", new ChargeWalletServlet()),
                Map.entry("/wallet/transfer", new TransferWalletServlet())


        );
    }
}

