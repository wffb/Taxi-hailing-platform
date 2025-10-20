package service;

import DAO.*;
import DAO.uow.UoW;
import DTO.WalletDTO;
import common.util.CacheUtil;
import model.LoginUser;
import model.Payment;
import model.Ride;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.lang.Thread.sleep;

public class WalletService {

    private UserDAO userDAO;
    private RiderDAO riderDAO;
    private PaymentDAO paymentDAO;
    private DriverDAO driverDAO;

    private WalletService() {
        paymentDAO = PaymentDAO.getInstance();
        riderDAO = RiderDAO.getInstance();
        userDAO = UserDAO.getInstance();
        driverDAO = DriverDAO.getInstance();
    }

    // INSTANCE
    private static volatile WalletService INSTANCE;

    public static WalletService getInstance() {
        if (INSTANCE == null) {
            synchronized (WalletService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WalletService();
                }
            }
        }
        return INSTANCE;
    }

    //get wallet and Record
    public WalletDTO getWallet(int userID) {

        //get lock
        String lockId = CacheUtil.tryLock("wallet:" + userID);
        while(lockId == null){
            try {
                sleep(500);
                lockId = CacheUtil.tryLock("wallet:" + userID);

            }catch (Exception e){
                System.out.println("[ERROR] Thread interrupted:"+e.getMessage());
                return null;
            }
        }

        BigDecimal balance  = userDAO.getWallet(userID);


        //release lock
        CacheUtil.releaseLock("wallet:" + userID, lockId);



        List<Payment> records;

        //get records
        String cacheKey = "login:"+ userID;
        LoginUser user = (LoginUser) CacheUtil.LoginUserCache.get(cacheKey);
        if(user == null)
            return null;

        if(user.getRole().equals("Rider")){
            int riderID = 0;
            try{
                riderID = riderDAO.getRiderIDByUserID(userID);
            } catch (SQLException e){
                e.printStackTrace();
                System.out.println("fail to get riderID by userID");
            }
            records = paymentDAO.getByRiderID(riderID);
        }
        else{
            int driverID = 0;
            try{
                driverID = driverDAO.getDriverIDByUserID(userID);
            } catch (SQLException e){
                e.printStackTrace();
                System.out.println("fail to get riderID by userID");
            }
            records = paymentDAO.getByDriverID(driverID);
        }




        return new WalletDTO(balance, records);


    }

    //charge wallet
    public boolean chargeWallet(int userID, BigDecimal amount) {

        //get lock
        String lockId = CacheUtil.tryLock("wallet:" + userID);
        while(lockId == null){
            try {
                sleep(500);
                lockId = CacheUtil.tryLock("wallet:" + userID);

            }catch (Exception e){
                System.out.println("[ERROR] Thread interrupted:"+e.getMessage());
                return false;
            }
        }

        int riderID = 0;
        try{
            riderID = riderDAO.getRiderIDByUserID(userID);
        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("fail to get riderID by userID");
            return false;
        }

        //onl charge rider wallet
        if(riderID == -1)
            return false;

        boolean success = userDAO.chargeWallet(userID, amount);
        boolean success2 = paymentDAO.createPaymentForTopUp(riderID, amount);

        //release lock
        CacheUtil.releaseLock("wallet:" + userID, lockId);

        return (success2 & success);
    }




    //transfer wallet
    public boolean transferWallet(Ride ride){

        int fromID, toID;
        try {
             fromID = RiderDAO.getInstance().getRiderById(ride.getRider_id()).getUser_id();
             toID = DriverDAO.getInstance().getUserIdByDriverId(ride.getDriver_id());

        } catch (SQLException e) {
            System.out.println("fail to get user ID");
            return false;
        }

        if(fromID == -1 || toID == -1) return false;

        return  transferWallet(fromID, toID, BigDecimal.valueOf(ride.getActual_fare()));
    }

    public boolean transferWallet(int fromID, int toID, BigDecimal amount)  {

        if(fromID == toID){
            System.out.println("fromID should not equal to toID"+fromID+" "+toID);
            return false;
        }

        //valid ID
        int riderID = 0;
        try{
            riderID = riderDAO.getRiderIDByUserID(fromID);
        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("fail to get riderID by userID");
            return false;
        }

        int driverID = 0;
        try{
            driverID = driverDAO.getDriverIDByUserID(toID);
        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("fail to get driverID by userID");
            return false;
        }

        if(riderID == -1 || driverID == -1){
            System.out.println("invalid ID when transfer wallet:"+fromID+" "+toID);
            return false;
        }

        //valid amount
        try {
            if(userDAO.getUserById(fromID).getWallet_balance().compareTo(amount) == -1 ){
                System.out.println("account balance not enough");
                return false;
            }


        } catch (SQLException e) {
            System.out.println("fail to get user wallet balance");
            return false;
        }


        //lock order to avoid deadlock
        int firstID = Math.min(fromID, toID);
        int secondID = Math.max(fromID, toID);


        //get lock fromLock - toLock
        String firstLockId = CacheUtil.tryLock("wallet:" + firstID);
        while(firstLockId == null){
            try {
                sleep(300);
                firstLockId = CacheUtil.tryLock("wallet:" + firstID);

            }catch (Exception e){
                System.out.println("[ERROR] Thread interrupted:"+e.getMessage());
                return false;
            }
        }

        String secondLockId = CacheUtil.tryLock("wallet:" + secondID);
        while(secondLockId == null){
            try {
                sleep(300);
               secondLockId = CacheUtil.tryLock("wallet:" + secondID);

            }catch (Exception e){
                System.out.println("[ERROR] Thread interrupted:"+e.getMessage());
                return false;
            }
        }

        //execute transaction
        Future<Boolean> future = UoW.executeAsync(uow -> {
            userDAO.transferWallet(uow, fromID, toID,amount);
        });



        //get result
        boolean success = false;
        while(!future.isDone()){
            try {
                sleep(300);
            } catch (InterruptedException e) {
                break;
            }
        }
        if(future.isDone()) {
            try {
                success = future.get();
            } catch (Exception e) {
                System.out.println("fail to get transaction result:"+e.getMessage());
            }
        }

        //release lock
        CacheUtil.releaseLock("wallet:" + secondID, secondLockId);
        CacheUtil.releaseLock("wallet:" + firstID, firstLockId);

        if(!paymentDAO.createPayment(riderID, driverID, amount)){
            System.out.println("fail to create payment:"+fromID+" to "+toID+" amount "+amount);
        }

        return success;

    }



}
