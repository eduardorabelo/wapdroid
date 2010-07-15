/*
 * Wapdroid - Android Location based Wifi Manager
 * Copyright (C) 2009 Bryan Emmanuel
 * 
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Bryan Emmanuel piusvelte@gmail.com
 */
package com.piusvelte.wapdroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class WapdroidDbAdapter {
	public static final String TABLE_ID = "_id";
	public static final String TABLE_CODE = "code";
	public static final String TABLE_NETWORKS = "networks";
	public static final String NETWORKS_SSID = "SSID";
	public static final String NETWORKS_BSSID = "BSSID";
	public static final String TABLE_CELLS = "cells";
	public static final String CELLS_CID = "CID";
	public static final String STATUS = "status";
	public static final int FILTER_ALL = 0;
	public static final int FILTER_INRANGE = 1;
	public static final int FILTER_OUTRANGE = 2;
	public static final int FILTER_CONNECTED = 3;
	public static final String TABLE_LOCATIONS = "locations";
	public static final String LOCATIONS_LAC = "LAC";
	public static final String TABLE_PAIRS = "pairs";
	public static final String PAIRS_CELL = "cell";
	public static final String PAIRS_NETWORK = "network";
	public static final String CELLS_LOCATION = "location";
	public static final String PAIRS_RSSI_MIN = "RSSI_min";
	public static final String PAIRS_RSSI_MAX = "RSSI_max";
	public static final int UNKNOWN_CID = -1;
	public static final int UNKNOWN_RSSI = 99;
	public static final String CREATE_NETWORKS = "create table if not exists networks (_id  integer primary key autoincrement, SSID text not null, BSSID text not null);";
	public static final String CREATE_CELLS = "create table if not exists cells (_id  integer primary key autoincrement, CID integer, location integer);";
	public static final String CREATE_PAIRS = "create table if not exists pairs (_id  integer primary key autoincrement, cell integer, network integer, RSSI_min integer, RSSI_max  integer);";
	public static final String CREATE_LOCATIONS = "create table if not exists locations (_id  integer primary key autoincrement, LAC integer);";
	static final Object[] sDataLock = new Object[0];

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	public final Context mContext;

	public WapdroidDbAdapter(Context context) {
		this.mContext = context;
	}

	public WapdroidDbAdapter open() throws SQLException {
		synchronized (WapdroidDbAdapter.sDataLock) {
			mDbHelper = new DatabaseHelper(mContext);
			mDb = mDbHelper.getWritableDatabase();				
		}
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public void createTables() {
		mDb.execSQL(CREATE_NETWORKS);
		mDb.execSQL(CREATE_CELLS);
		mDb.execSQL(CREATE_PAIRS);
		mDb.execSQL(CREATE_LOCATIONS);
	}

	private String tableId(String table) {
		return table + "." + TABLE_ID;
	}

	private String tableIdAs(String table) {
		return tableId(table) + " as " + TABLE_ID;
	}

	public int fetchNetworkOrCreate(String ssid, String bssid) {
		int network = UNKNOWN_CID;
		String ssid_orig = "", bssid_orig = "";
		ContentValues values = new ContentValues();
		// upgrading, BSSID may not be set yet
		Cursor c = mDb.rawQuery("select " + TABLE_ID + ", " + NETWORKS_SSID + ", " + NETWORKS_BSSID + " from " + TABLE_NETWORKS + " where " + NETWORKS_BSSID + "=\"" + bssid + "\" OR (" + NETWORKS_SSID + "=\"" + ssid + "\" and " + NETWORKS_BSSID + "=\"\")", null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			network = c.getInt(c.getColumnIndex(TABLE_ID));
			ssid_orig = c.getString(c.getColumnIndex(NETWORKS_SSID));
			bssid_orig = c.getString(c.getColumnIndex(NETWORKS_BSSID));
			if (bssid_orig.equals("")) {
				values.put(NETWORKS_BSSID, bssid);
				mDb.update(TABLE_NETWORKS, values, TABLE_ID + "=" + network, null);
			} else if (!ssid_orig.equals(ssid)) {
				values.put(NETWORKS_SSID, ssid);
				mDb.update(TABLE_NETWORKS, values, TABLE_ID + "=" + network, null);
			}
		} else {
			values.put(NETWORKS_SSID, ssid);
			values.put(NETWORKS_BSSID, bssid);
			network = (int) mDb.insert(TABLE_NETWORKS, null, values);
		}
		c.close();
		return network;
	}

	private String inSelectNetworks(String cells) {
		return " in (select " + PAIRS_NETWORK
		+ " from " + TABLE_PAIRS + ", " + TABLE_CELLS + ", " + TABLE_LOCATIONS
		+ " where " + PAIRS_CELL + "=" + tableId(TABLE_CELLS)
		+ " and " + CELLS_LOCATION + "=" + tableId(TABLE_LOCATIONS)
		+ " and (" + cells + "))";
	}

	private String filterText(int filter) {
		return mContext.getResources().getString(filter == FILTER_CONNECTED ? R.string.connected : filter == FILTER_INRANGE ? R.string.withinarea : R.string.outofarea);
	}

	public Cursor fetchNetworks(int filter, String bssid, String cells) {
		return mDb.rawQuery("select " + tableIdAs(TABLE_NETWORKS) + ", " + NETWORKS_SSID + ", " + NETWORKS_BSSID + ", "
				+ ((filter == FILTER_ALL) ?
						("case when " + NETWORKS_BSSID + "='" + bssid + "' then '" + mContext.getResources().getString(R.string.connected)
								+ "' else (case when " + tableId(TABLE_NETWORKS) + inSelectNetworks(cells) + " then '" + mContext.getResources().getString(R.string.withinarea)
								+ "' else '" + mContext.getResources().getString(R.string.outofarea) + "' end) end as ")
								: "'" + (filterText(filter) + "' as "))
								+ STATUS
								+ " from " + TABLE_NETWORKS
								+ (filter != FILTER_ALL ?
										" where "
										+ (filter == FILTER_CONNECTED ?
												NETWORKS_BSSID + "='" + bssid + "'"
												: tableId(TABLE_NETWORKS) + (filter == FILTER_OUTRANGE ? " NOT" : "") + inSelectNetworks(cells))
												: " order by " + STATUS), null);
	}

	public int fetchLocationOrCreate(int lac) {
		int location = UNKNOWN_CID;
		if (lac > 0) {
			Cursor c = mDb.rawQuery("select " + TABLE_ID + " from " + TABLE_LOCATIONS + " where " + LOCATIONS_LAC + "=" + lac, null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				location = c.getInt(c.getColumnIndex(TABLE_ID));
			} else {
				ContentValues values = new ContentValues();
				values.put(LOCATIONS_LAC, lac);
				location = (int) mDb.insert(TABLE_LOCATIONS, null, values);
			}
			c.close();
		}
		return location;
	}

	public int fetchCellOrCreate(int cid, int location) {
		int cell = UNKNOWN_CID;
		// if location==-1, then match only on cid, otherwise match on location or -1
		Cursor c = mDb.rawQuery("select " + TABLE_ID + ", " + CELLS_LOCATION
				+ " from " + TABLE_CELLS
				+ " where " + CELLS_CID + "=" + cid
				+ (location == UNKNOWN_CID ? ""
						: " and (" + CELLS_LOCATION + "=" + UNKNOWN_CID + " or " + CELLS_LOCATION + "=" + location + ")"), null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			cell = c.getInt(c.getColumnIndex(TABLE_ID));
			if ((location != UNKNOWN_CID) && (c.getInt(c.getColumnIndex(CELLS_LOCATION)) == UNKNOWN_CID)){
				// update the location
				ContentValues values = new ContentValues();
				values.put(CELLS_LOCATION, location);
				mDb.update(TABLE_CELLS, values, TABLE_ID + "=" + cell, null);
			}
		} else {
			ContentValues values = new ContentValues();
			values.put(CELLS_CID, cid);
			values.put(CELLS_LOCATION, location);
			cell = (int) mDb.insert(TABLE_CELLS, null, values);
		}
		c.close();
		return cell;
	}

	public int createPair(int cid, int lac, int network, int rssi) {
		int location = fetchLocationOrCreate(lac);
		int cell = fetchCellOrCreate(cid, location);
		int pair = UNKNOWN_CID;
		Cursor c = mDb.rawQuery("select " + TABLE_ID + ", " + PAIRS_RSSI_MIN + ", " + PAIRS_RSSI_MAX
				+ " from " + TABLE_PAIRS
				+ " where " + PAIRS_CELL + "=" + cell + " and " + PAIRS_NETWORK + "=" + network, null);
		if (c.getCount() > 0) {
			if (rssi != UNKNOWN_RSSI) {
				c.moveToFirst();
				pair = c.getInt(c.getColumnIndex(TABLE_ID));
				int rssi_min = c.getInt(c.getColumnIndex(PAIRS_RSSI_MIN));
				int rssi_max = c.getInt(c.getColumnIndex(PAIRS_RSSI_MAX));
				boolean update = false;
				ContentValues values = new ContentValues();
				if (rssi_min > rssi) {
					update = true;
					values.put(PAIRS_RSSI_MIN, rssi);
				} else if ((rssi_max == UNKNOWN_RSSI) || (rssi_max < rssi)) {
					update = true;
					values.put(PAIRS_RSSI_MAX, rssi);
				}
				if (update) mDb.update(TABLE_PAIRS, values, TABLE_ID + "=" + pair, null);
			}
		} else {
			ContentValues values = new ContentValues();
			values.put(PAIRS_CELL, cell);
			values.put(PAIRS_NETWORK, network);
			values.put(PAIRS_RSSI_MIN, rssi);
			values.put(PAIRS_RSSI_MAX, rssi);
			return (int) mDb.insert(TABLE_PAIRS, null, values);
		}
		c.close();
		return pair;
	}

	public Cursor fetchNetworkData(int network) {
		return mDb.rawQuery("select " + tableIdAs(TABLE_PAIRS) + ", " + NETWORKS_SSID + ", " + NETWORKS_BSSID + ", " + CELLS_CID + ", " + LOCATIONS_LAC + ", " + PAIRS_RSSI_MIN + ", " + PAIRS_RSSI_MAX
				+ " from " + TABLE_PAIRS + ", " + TABLE_NETWORKS + ", " + TABLE_CELLS + ", " + TABLE_LOCATIONS
				+ " where " + PAIRS_NETWORK + "=" + tableId(TABLE_NETWORKS)
				+ " and " + PAIRS_CELL + "=" + TABLE_CELLS + "." + TABLE_ID
				+ " and " + CELLS_LOCATION + "=" + tableId(TABLE_LOCATIONS)
				+ " and " + PAIRS_NETWORK + "=" + network, null);
	}

	public Cursor fetchPairData(int pair) {
		return mDb.rawQuery("select " + tableIdAs(TABLE_PAIRS) + ", " + NETWORKS_SSID + ", " + NETWORKS_BSSID + ", " + CELLS_CID + ", " + LOCATIONS_LAC + ", " + PAIRS_RSSI_MIN + ", " + PAIRS_RSSI_MAX
				+ " from " + TABLE_PAIRS + ", " + TABLE_NETWORKS + ", " + TABLE_CELLS + ", " + TABLE_LOCATIONS
				+ " where " + PAIRS_NETWORK + "=" + tableId(TABLE_NETWORKS)
				+ " and " + PAIRS_CELL + "=" + tableId(TABLE_CELLS)
				+ " and " + CELLS_LOCATION + "=" + tableId(TABLE_LOCATIONS)
				+ " and " + tableId(TABLE_PAIRS) + "=" + pair, null);
	}

	public Cursor fetchPairsByNetwork(int network) {
		return mDb.rawQuery("select " + tableIdAs(TABLE_PAIRS) + ", " + CELLS_CID
				+ " from " + TABLE_PAIRS + ", " + TABLE_CELLS
				+ " where " + PAIRS_CELL + "=" + tableId(TABLE_CELLS)
				+ " and "+ PAIRS_NETWORK + "=" + network, null);
	}

	private String inSelectCells(int network, String cells) {
		return " in (select " + tableId(TABLE_CELLS)
		+ " from " + TABLE_PAIRS + ", " + TABLE_CELLS + ", " + TABLE_LOCATIONS
		+ " where " + PAIRS_CELL + "=" + tableId(TABLE_CELLS)
		+ " and " + CELLS_LOCATION + "=" + tableId(TABLE_LOCATIONS)
		+ " and " + PAIRS_NETWORK + "=" + network
		+ " and " + cells + ")";
	}

	public Cursor fetchPairsByNetworkFilter(int filter, int network, int cid, String cells) {
		return mDb.rawQuery("select " + tableIdAs(TABLE_PAIRS) + ", " + CELLS_CID + ", "
				+ "case when " + LOCATIONS_LAC + "=" + UNKNOWN_CID + " then '" + mContext.getResources().getString(R.string.unknown) + "' else " + LOCATIONS_LAC + " end as " + LOCATIONS_LAC + ", "
				+ "case when " + PAIRS_RSSI_MIN + "=" + UNKNOWN_RSSI + " then '" + mContext.getResources().getString(R.string.unknown) + "' else (" + PAIRS_RSSI_MIN + "||'" + mContext.getResources().getString(R.string.colon) + "'||" + PAIRS_RSSI_MAX + "||'" + mContext.getResources().getString(R.string.dbm) + "') end as " + PAIRS_RSSI_MIN + ", "
				+ ((filter == FILTER_ALL) ?
						("case when " + CELLS_CID + "='" + cid + "' then '" + mContext.getResources().getString(R.string.connected)
								+ "' else (case when " + tableId(TABLE_CELLS) + inSelectCells(network, cells) + " then '" + mContext.getResources().getString(R.string.withinarea)
								+ "' else '" + mContext.getResources().getString(R.string.outofarea) + "' end) end as ")
								: "'" + (filterText(filter) + "' as "))
								+ STATUS
								+ " from " + TABLE_PAIRS
								+ " left join " + TABLE_CELLS
								+ " on " + PAIRS_CELL + "=" + tableId(TABLE_CELLS)
								+ " left outer join " + TABLE_LOCATIONS
								+ " on " + CELLS_LOCATION + "=" + tableId(TABLE_LOCATIONS)
								+ " where "+ PAIRS_NETWORK + "=" + network
								+ (filter != FILTER_ALL ?
										" and "
										+ (filter == FILTER_CONNECTED ?
												CELLS_CID + "='" + cid + "'"
												: tableId(TABLE_CELLS) + (filter == FILTER_OUTRANGE ? " NOT" : "") + inSelectCells(network, cells))
												: " order by " + STATUS), null);
	}

	public int updateNetworkRange(String ssid, String bssid, int cid, int lac, int rssi) {
		int network = fetchNetworkOrCreate(ssid, bssid);
		createPair(cid, lac, network, rssi);
		return network;
	}

	public boolean cellInRange(int cid, int lac, int rssi) {
		boolean inRange = false;
		Cursor c = mDb.rawQuery("select " + tableIdAs(TABLE_CELLS)
				+ ", " + CELLS_LOCATION
				+ (rssi != UNKNOWN_RSSI ?
						", (select min(" + PAIRS_RSSI_MIN + ") from " + TABLE_PAIRS + " where " + PAIRS_CELL + "=" + tableId(TABLE_CELLS) + ") as " + PAIRS_RSSI_MIN
						+ ", (select max(" + PAIRS_RSSI_MAX + ") from " + TABLE_PAIRS + " where " + PAIRS_CELL + "=" + tableId(TABLE_CELLS) + ") as " + PAIRS_RSSI_MAX
						: "")
						+ " from " + TABLE_CELLS
						+ " left outer join " + TABLE_LOCATIONS
						+ " on " + CELLS_LOCATION + "=" + tableId(TABLE_LOCATIONS)
						+ " where "+ CELLS_CID + "=" + cid
						+ " and (" + LOCATIONS_LAC + "=" + lac + " or " + CELLS_LOCATION + "=" + UNKNOWN_CID + ")"
						+ (rssi != UNKNOWN_RSSI ?
								" and (((" + PAIRS_RSSI_MIN + "=" + UNKNOWN_RSSI + ") or (" + PAIRS_RSSI_MIN + "<=" + rssi + ")) and ((" + PAIRS_RSSI_MAX + "=" + UNKNOWN_RSSI + ") or (" + PAIRS_RSSI_MAX + ">=" + rssi + ")))"
								: ""), null);
		inRange = (c.getCount() > 0);
		if (inRange && (lac > 0)) {
			// check LAC, as this is a new column
			c.moveToFirst();
			if (c.isNull(c.getColumnIndex(CELLS_LOCATION))) {
				int location = fetchLocationOrCreate(lac);
				ContentValues values = new ContentValues();
				int cell = c.getInt(c.getColumnIndex(TABLE_ID));
				values.put(CELLS_LOCATION, location);
				mDb.update(TABLE_CELLS, values, TABLE_ID + "=" + cell, null);
			}
		}
		c.close();
		return inRange;
	}

	public void cleanCellsLocations() {
		Cursor c = mDb.rawQuery("select " + TABLE_ID + ", " + CELLS_LOCATION + " from " + TABLE_CELLS, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				int cell = c.getInt(c.getColumnIndex(TABLE_ID));
				Cursor p = mDb.rawQuery("select " + TABLE_ID + " from " + TABLE_PAIRS + " where " + PAIRS_CELL + "=" + cell, null);
				if (p.getCount() == 0) {
					mDb.delete(TABLE_CELLS, TABLE_ID + "=" + cell, null);
					int location = c.getInt(c.getColumnIndex(CELLS_LOCATION));
					Cursor l = mDb.rawQuery("select " + TABLE_ID + " from " + TABLE_CELLS + " where " + CELLS_LOCATION + "=" + location, null);
					if (l.getCount() == 0) mDb.delete(TABLE_LOCATIONS, TABLE_ID + "=" + location, null);
					l.close();
				}
				p.close();
				c.moveToNext();
			}
		}
		c.close();
	}

	public void deleteNetwork(int network) {
		mDb.delete(TABLE_NETWORKS, TABLE_ID + "=" + network, null);
		mDb.delete(TABLE_PAIRS, PAIRS_NETWORK + "=" + network, null);
		cleanCellsLocations();
	}

	public void deletePair(int network, int pair) {
		// get paired cell and location
		mDb.delete(TABLE_PAIRS, TABLE_ID + "=" + pair, null);
		Cursor n = fetchPairsByNetwork(network);
		if (n.getCount() == 0) mDb.delete(TABLE_NETWORKS, TABLE_ID + "=" + network, null);
		n.close();
		cleanCellsLocations();
	}
}