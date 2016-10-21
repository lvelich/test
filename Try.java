package com.fannie;

package com.ba.balogic;

import com.ba.interfaces.IBanking;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.activation.DataSource;

import com.ba.bean.Transaction;
import com.ba.connection.GetConnection;


public class BankApplication implements IBanking {


	@Override
	public double getbalance(String userId, String accType) {
		String sql = "select balance from account where userid=? and account_type=?";
		// String sql1="show tables";
		GetConnection gc = new GetConnection();
		double temp = 0;

		try {
			gc.ps = GetConnection.mysqlcon().prepareStatement(sql);
			gc.ps.setString(1, userId);
			gc.ps.setString(2, accType);
			gc.rs = gc.ps.executeQuery();
			gc.rs.next();
			temp = gc.rs.getDouble(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// System.out.println("raninto exception here");
		} finally {
			try {
				gc.ps.close();
				gc.rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return temp;

	}

	@Override
	public String validatlogin(String userId) {
		String sql = "select password from userlogin where userid=?";
		// String sql1="show tables";
		GetConnection gc = new GetConnection();
		String temp = null;

		try {
			gc.ps = GetConnection.mysqlcon().prepareStatement(sql);
			gc.ps.setString(1, userId);
			gc.rs = gc.ps.executeQuery();
			gc.rs.next();
			temp = gc.rs.getString(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				gc.ps.close();
				gc.rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return temp;
	}

	@Override
	public boolean transerFunds(String userId,String accFrom, String accTo, double amt) {
		  double currbalF= getbalance(userId, accFrom);
		  double currbalT= getbalance(userId, accTo);
		  double newbalF= currbalF-amt;
		  double newbalT= currbalT+amt;
		String sqlF = "update account set balance=? where account_type=? and userid=? ";
		String sqlT = "update account set balance=? where account_type=? and userid=? ";
		//String acctidF ="select accountid from account where account_type=? and userid=?";
		//String acctidT ="select accountid from account where account_type=? and userid=?";
		
		GetConnection gc = new GetConnection();
		
		try {
			gc.ps = GetConnection.mysqlcon().prepareStatement(sqlF);
			gc.ps.setDouble(1, newbalF);
			gc.ps.setString(2, accFrom);
			gc.ps.setString(3, userId);
			//System.out.println( "exceuted value is"+gc.ps.executeUpdate());
			if( gc.ps.executeUpdate() > 0){
			gc.ps1 = GetConnection.mysqlcon().prepareStatement(sqlT);
			gc.ps1.setDouble(1, newbalT);
			gc.ps1.setString(2, accTo);
			gc.ps1.setString(3, userId);		
			}
			return gc.ps1.executeUpdate() > 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				gc.ps.close();
				gc.ps1.close();
				updateTrans(userId, accFrom, accTo, amt);
				//System.out.println( "completed transfer and update transaction");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public void stopCheques() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean updateTrans(String userId,String accFrom, String accTo, double amt) {
		// TODO Auto-generated method stub
		String acctidF ="select accountid from account where account_type=? and userid=?";
		String acctidT ="select accountid from account where account_type=? and userid=?";
		GetConnection gc = new GetConnection();
		int tempF = 0 ;
		int tempT =0;
		
		try {
			gc.ps = GetConnection.mysqlcon().prepareStatement(acctidF);
			gc.ps.setString(1, accFrom);
			gc.ps.setString(2, userId);
			gc.rs = gc.ps.executeQuery();
			gc.rs.next();
			tempF = gc.rs.getInt(1);
			gc.rs.close();
			gc.ps1 = GetConnection.mysqlcon().prepareStatement(acctidT);
			gc.ps1.setString(1, accTo);
			gc.ps1.setString(2, userId);
			gc.rs = gc.ps1.executeQuery();
			gc.rs.next();
			tempT = gc.rs.getInt(1);
			gc.rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				gc.ps.close();
				gc.ps1.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String tranF ="insert into Transaction(DateofTran,accountid,amount)   values(now(),?,?);";
		String tranT ="insert into Transaction(DateofTran,accountid,amount)   values(now(),?,?);";

		try {
			gc.ps = GetConnection.mysqlcon().prepareStatement(tranF);
			gc.ps.setInt(1, tempF);
			gc.ps.setDouble(2, -amt);
			if( gc.ps.executeUpdate() > 0){
				gc.ps1 = GetConnection.mysqlcon().prepareStatement(tranT);
				gc.ps1.setInt(1, tempT);
				gc.ps1.setDouble(2, amt);
			}
			return gc.ps1.executeUpdate() > 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				gc.ps.close();
				gc.ps1.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public List <Transaction> tranlist(String userId, String accType) {
		 
		int accountid=getAccId(userId, accType);
		//System.out.println("account id is "+accountid);
		String sql_list ="select TransId, DateOfTran,accountid,amount from transaction where accountid=? order by TransId,DateOfTran ";
		GetConnection gc = new GetConnection();
		List <Transaction> Tran_List= new ArrayList<Transaction>();
		// tryythis
		  //Transaction[][] Tran_List1 = new Transaction[][4];
		
		try {
			gc.ps = GetConnection.mysqlcon().prepareStatement(sql_list);
			gc.ps.setInt(1, accountid);
			gc.rs = gc.ps.executeQuery();
			
			while (gc.rs.next()){
				Transaction Tran= new Transaction();
				Tran.setTransId(gc.rs.getInt("TransId"));
				Tran.setDateOfTran(gc.rs.getDate("DateOfTran"));
				Tran.setAccountid(gc.rs.getInt("accountid"));
				Tran.setAmount(gc.rs.getDouble("amount"));
				Tran_List.add(Tran);
				}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				gc.ps.close();
				gc.rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return Tran_List;
	}

	@Override
	public int getAccId(String userId, String accType) {
		String sql_accid ="select accountid from account where account_type=? and userid=?";
		GetConnection gc = new GetConnection();
		int accid = 0 ;
		
		try {
			gc.ps = GetConnection.mysqlcon().prepareStatement(sql_accid);
			gc.ps.setString(1, accType);
			gc.ps.setString(2, userId);
			gc.rs = gc.ps.executeQuery();
			gc.rs.next();
			accid = gc.rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				gc.ps.close();
				gc.rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		return accid;
	}

	@Override
	public boolean updateAddress(String userId, String name, String address, String city, String state, String country,
			String email) {
		// TODO Auto-generated method stub
		return false;
	}
}
