package com.song7749.dl.dbclient.service;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.song7749.dl.dbclient.entities.ServerInfo;
import com.song7749.dl.dbclient.vo.FieldVO;
import com.song7749.dl.dbclient.vo.IndexVO;
import com.song7749.dl.dbclient.vo.TableVO;
import com.song7749.util.StringUtils;

@Service("dbClientDataSourceManager")
public class DBclientDataSourceManagerImpl implements DBclientDataSourceManager {

	Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<ServerInfo, DataSource> dataSourceMap = new HashMap<ServerInfo, DataSource>();

	@Override
	public DataSource getDataSource(ServerInfo serverInfo) {
		if (dataSourceMap.containsKey(serverInfo)) {
			return dataSourceMap.get(serverInfo);
		}

		BasicDataSource bds = new BasicDataSource();
		bds.setDriverClassName(serverInfo.getDriver().getDriverName());
		try {
			bds.setUrl(getUrl(serverInfo));
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		bds.setUsername(serverInfo.getAccount());
		bds.setPassword(serverInfo.getPassword());
		bds.setValidationQuery("SELECT 1 FROM DUAL");
		bds.setValidationQueryTimeout(60);
		bds.setDefaultAutoCommit(false);
		bds.setMaxActive(3);
		bds.setInitialSize(3);
		bds.setMinIdle(1);
		bds.setMaxIdle(3);
		bds.setMaxWait(5000);
		bds.setTestOnReturn(true);
		bds.setTestOnReturn(false);
		bds.setTestWhileIdle(true);
		bds.setNumTestsPerEvictionRun(5);
		bds.setMinEvictableIdleTimeMillis(10000);
		bds.setTimeBetweenEvictionRunsMillis(50000);
		bds.setRemoveAbandoned(true);
		bds.setRemoveAbandonedTimeout(60);
		bds.setLogAbandoned(true);
		bds.setPoolPreparedStatements(true);
		dataSourceMap.put(serverInfo, bds);

		return bds;
	}

	@Override
	public List<TableVO> selectTableVOList(ServerInfo serverInfo) {

		List<TableVO> list = new ArrayList<TableVO>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			logger.debug(getTableListQuery(serverInfo));

			conn = getDataSource(serverInfo).getConnection();
			ps = conn.prepareStatement(getTableListQuery(serverInfo));
			rs = ps.executeQuery();

			while (rs.next()) {
				list.add(new TableVO(rs.getString("TABLE_NAME"), rs.getString("TABLE_COMMENT")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				closeAll(conn, ps, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public List<FieldVO> selectTableFieldVOList(ServerInfo serverInfo,
			String tableName) {

		List<FieldVO> list = new ArrayList<FieldVO>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			logger.debug(getTableFieldListQuery(serverInfo,tableName));

			conn = getDataSource(serverInfo).getConnection();
			ps = conn.prepareStatement(getTableFieldListQuery(serverInfo,tableName));
			rs = ps.executeQuery();

			while (rs.next()) {
				list.add(new FieldVO(
						rs.getString("COLUMN_ID"),
						rs.getString("COLUMN_NAME"),
						rs.getString("NULLABLE"),
						rs.getString("COLUMN_KEY"),
						rs.getString("DATA_TYPE"),
						rs.getString("DATA_LENGTH"),
						rs.getString("CHARACTER_SET"),
						rs.getString("EXTRA"),
						rs.getString("DEFAULT_VALUE"),
						rs.getString("COMMENT")));

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				closeAll(conn, ps, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Override
	public List<IndexVO> selectTableIndexVOList(ServerInfo serverInfo,
			String tableName) {

		List<IndexVO> list = new ArrayList<IndexVO>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			logger.debug(getTableFieldListQuery(serverInfo,tableName));

			conn = getDataSource(serverInfo).getConnection();
			ps = conn.prepareStatement(getTableIndexListQuery(serverInfo,tableName));
			rs = ps.executeQuery();

			while (rs.next()) {
				switch (serverInfo.getDriver()) {
				case mysql:
					list.add(new IndexVO(
							rs.getString("Table"),
							rs.getString("Key_name"),
							rs.getString("Column_name"),
							rs.getString("Seq_in_index"),
							rs.getString("Cardinality"),
							rs.getString("Non_unique").equals("0") ? "UNIQUE" : "NOT_UNIQUE",
							"asc"));
					break;
				case oracle:
					list.add(new IndexVO(
							rs.getString("OWNER") ,
							rs.getString("INDEX_NAME") ,
							rs.getString("COLUMN_NAME"),
							rs.getString("COLUMN_POSITION"),
							rs.getString("NUM_ROWS"),
							rs.getString("UNIQUENESS"),
							rs.getString("DESCEND")));
					break;

				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				closeAll(conn, ps, rs);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 서버 정보를 이용해서 DB CONNECT URL 을 치환한다.
	 *
	 * @param serverInfo
	 * @return String DB 커넥션 URL
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private String getUrl(ServerInfo serverInfo)
			throws IllegalArgumentException, IllegalAccessException {
		// 커넥션 정보 조합
		String url = serverInfo.getDriver().getUrl()
				+ serverInfo.getDriver().getParameter();
		// 필드 매치
		for (Field f : serverInfo.getClass().getDeclaredFields()) {
			// 필드를 읽기 가능한 상태로 변경
			f.setAccessible(true);
			// 커넥션 변경
			url = StringUtils.replace("\\{"+f.getName()+"\\}",f.get(serverInfo).toString(), url);
		}
		return url;
	}

	private String getTableListQuery(ServerInfo serverInfo){
		return StringUtils
			.replace("\\{schemaName\\}", serverInfo.getSchemaName(),serverInfo.getDriver().getTableListQuery());
	}

	private String getTableFieldListQuery(ServerInfo serverInfo,String tableName){
		String query;
		query = StringUtils.replace("\\{schemaName\\}", serverInfo.getSchemaName(),serverInfo.getDriver().getFieldListQueryQuery());
		query = StringUtils.replace("\\{tableName\\}", tableName ,query);
		return query;
	}

	private String getTableIndexListQuery(ServerInfo serverInfo,String tableName){
		String query;
		query = StringUtils.replace("\\{schemaName\\}", serverInfo.getSchemaName(),serverInfo.getDriver().getIndexListQuery());
		query = StringUtils.replace("\\{tableName\\}", tableName ,query);
		return query;
	}


	/**
	 * 연결 모두 닫기
	 *
	 * @param conn
	 * @param ps
	 * @param rs
	 * @throws SQLException
	 */
	private void closeAll(Connection conn, PreparedStatement ps, ResultSet rs)
			throws SQLException {
		if (null != conn) {
			conn.close();
		}
		if (null != ps) {
			ps.close();
		}
		if (null != rs) {
			rs.close();
		}
	}
}