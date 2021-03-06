package test.java;

import main.java.*;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserDAOTest {
	private static final String CITY_1 = "Nowheresvilletownburg";
	private static final int STATE_1 = 12;
	private static final int ZIP_1 = 12345;
	
	private static final String CITY_2 = "Sunnydale";
	private static final int STATE_2 = 6;
	private static final int ZIP_2 = 99999;
	
	private static final String CITY_3 = "Fran Sancisco";
	private static final int ZIP_3 = 98765;
	
	
	private static final String USERNAME_1 = "cool_username_bro25";
	private static final String PASSWORD_1 = "tHisPWisS00safeIswear";
	
	private static final String USERNAME_2 = "even_better_username";
	private static final String PASSWORD_2 = "7h3gr34t357pw3v3r";
	
	private static final String USERNAME_3 = "coolcoolcool25";
	private static final String PASSWORD_3 = "abc123";
	
	private static final String USERNAME_4 = "catscatscats";
	private static final String PASSWORD_4 = "55555";
	private DBUtil dbUtil;
	private UserDAO userDAO;
	
	@Before
	public void setUp() {
		dbUtil = new DBUtil();
		userDAO = new UserDAO();
		userDAO.createUser(USERNAME_1, PASSWORD_1);	//used by testAddResidence
		userDAO.createUser(USERNAME_3, PASSWORD_3);	//used by testAddResidence
		userDAO.createUser(USERNAME_4, PASSWORD_4);	//used by testAddResidence
	}

	@Test
	public void testCreateUser() {
		assertTrue(userDAO.createUser(USERNAME_2, PASSWORD_2));
		assertFalse(userDAO.createUser(USERNAME_2, "a_new_pw"));
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = dbUtil.getConnection().prepareStatement("SELECT ID, password FROM user WHERE ID=?");
			pstmt.setString(1, USERNAME_2);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				assertEquals("User doesn't have right password stored", PASSWORD_2, rs.getString(2));
			}
			assertFalse("Duplicate user created", rs.next());
		} catch (SQLException e){
			e.printStackTrace();
		} finally {
			DBUtil.closeResultSet(rs);
			DBUtil.closeStatement(pstmt);
		}
	}
	
	@Test
	public void testGetUser() {
		//TODO implement
	}
	
	@Test
	public void testUpdateUser() {
		//TODO implement
	}
	
	@Test
	public void testAddResidence() {
		//NON-EXISTENT LOCATION
		
		//add residence in CITY_1
		userDAO.addResidence(USERNAME_1, CITY_1, STATE_1, ZIP_1);
		
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		try {
			String getLoc = "SELECT ID, city, state, ZIP FROM location WHERE city=?";
			pstmt1 = dbUtil.getConnection().prepareStatement(getLoc);
			pstmt1.setString(1, CITY_1);
			rs1 = pstmt1.executeQuery();
			int locID = -1;
			if (rs1.next()) {	//row 1
				locID = rs1.getInt(1);
				assertEquals("State is incorrect", STATE_1, rs1.getInt(3));
				assertEquals("ZIP is incorrect", ZIP_1, rs1.getInt(4));
			} else {
				fail("City not found in location table");
			}
			assertFalse("Multiple location rows for this city", rs1.next());
			
			pstmt2 = dbUtil.getConnection().prepareStatement("SELECT loc_ID FROM residence WHERE std_ID=?");
			pstmt2.setString(1, USERNAME_1);
			rs2 = pstmt2.executeQuery();
			if (rs2.next()) {
				assertEquals("Location ID doesn't match", locID, rs2.getInt(1));
			} else {
				fail("Row not found in residence table");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(pstmt1);
			DBUtil.closeStatement(pstmt2);
			DBUtil.closeResultSet(rs1);
			DBUtil.closeResultSet(rs2);
		}
		
		
		//EXISTING LOCATION
		PreparedStatement pstmt3 = null;
		ResultSet rs3 = null;
		String q = "INSERT INTO location (city, state, ZIP) VALUES (?, ?, ?)";
		PreparedStatement pstmt4 = null;
		ResultSet rs4 = null;
		
		PreparedStatement pstmt5 = null;
		ResultSet rs5 = null;
		String q2 = "INSERT INTO location (city, ZIP) VALUES (?, ?)";
		PreparedStatement pstmt6 = null;
		ResultSet rs6 = null;
		try {
			pstmt3 = dbUtil.getConnection().prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			pstmt3.setString(1, CITY_2);
			pstmt3.setInt(2, STATE_2);
			pstmt3.setInt(3, ZIP_2);
			pstmt3.executeUpdate();
			rs3 = pstmt3.getGeneratedKeys();
			assertTrue("No key generated", rs3.next());
			int id = rs3.getInt(1);
			userDAO.addResidence(USERNAME_3, CITY_2, STATE_2, ZIP_2);
			pstmt4 = dbUtil.getConnection().prepareStatement("SELECT loc_ID FROM residence WHERE std_ID=?");
			pstmt4.setString(1, USERNAME_3);
			rs4 = pstmt4.executeQuery();
			assertTrue("Residence not found", rs4.next());
			assertTrue("Location ID does not match existing location", rs4.getInt(1) == id);
			
			//existing location but one value is null
			pstmt5 = dbUtil.getConnection().prepareStatement(q2, Statement.RETURN_GENERATED_KEYS);
			pstmt5.setString(1, CITY_3);
			pstmt5.setInt(2, ZIP_3);
			pstmt5.executeUpdate();
			rs5 = pstmt5.getGeneratedKeys();
			assertTrue("No key generated", rs5.next());
			int id2 = rs5.getInt(1);
			userDAO.addResidence(USERNAME_4, CITY_3, 41, ZIP_3);
			pstmt6 = dbUtil.getConnection().prepareStatement("SELECT loc_ID FROM residence WHERE std_ID=?");
			pstmt6.setString(1, USERNAME_4);
			rs6 = pstmt6.executeQuery();
			assertTrue("Residence not found", rs6.next());
			assertFalse("Inappropriately uses existing location", rs6.getInt(1) == id2);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(pstmt3);
			DBUtil.closeResultSet(rs3);
			DBUtil.closeStatement(pstmt4);
			DBUtil.closeResultSet(rs4);
			DBUtil.closeStatement(pstmt5);
			DBUtil.closeResultSet(rs5);
			DBUtil.closeStatement(pstmt6);
			DBUtil.closeResultSet(rs6);
		}
	}

	@After
	public void cleanUp() {
		cleanUpResidence();
		cleanUpLocations();
		cleanUpUser();
		
		//ALWAYS LAST
		dbUtil.closeConnection();
	}
	
	private void cleanUpLocations() {
		PreparedStatement pstmt = null;
		try {
			//using city condition because when we have real locations 
			//populated from schools we don't want to delete them
			pstmt = dbUtil.getConnection().prepareStatement("DELETE FROM location WHERE city=? OR city=? OR city=?");
			pstmt.setString(1, CITY_1);
			pstmt.setString(2, CITY_2);
			pstmt.setString(3, CITY_3);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(pstmt);
		}
	}
	
	private void cleanUpResidence() {
		Statement stmt = null;
		try {
			stmt = dbUtil.getConnection().createStatement();
			stmt.executeUpdate("DELETE FROM residence");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(stmt);
		}
	}
	
	private void cleanUpUser() {
		Statement stmt = null;
		try {
			stmt = dbUtil.getConnection().createStatement();
			stmt.executeUpdate("DELETE FROM user");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.closeStatement(stmt);
		}
	}
}
