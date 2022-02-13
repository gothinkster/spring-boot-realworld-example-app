package io.spring.infrastructure.mybatis;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.joda.time.DateTime;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

@MappedTypes(DateTime.class)
public class DateTimeHandler implements TypeHandler<DateTime> {

    private static final Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    @Override
    public void setParameter(
            PreparedStatement ps,
            int i,
            DateTime parameter,
            JdbcType jdbcType
    ) throws SQLException {
        Timestamp timestamp = parameter == null ?
                null :
                new Timestamp(parameter.getMillis());
        ps.setTimestamp(i, timestamp, UTC_CALENDAR);
    }

    @Override
    public DateTime getResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName, UTC_CALENDAR);
        return timestamp == null ?
                null :
                new DateTime(timestamp.getTime());
    }

    @Override
    public DateTime getResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex, UTC_CALENDAR);
        return timestamp == null ?
                null :
                new DateTime(timestamp.getTime());
    }

    @Override
    public DateTime getResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex, UTC_CALENDAR);
        return timestamp == null ?
                null :
                new DateTime(timestamp.getTime());
    }

}
