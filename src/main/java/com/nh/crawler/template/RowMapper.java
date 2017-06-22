package com.nh.crawler.template;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract interface RowMapper {

	public abstract Object mapRow(ResultSet set) throws SQLException;

}
