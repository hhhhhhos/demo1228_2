package com.example.demo1228_2.config;

import com.example.demo1228_2.dto.BuylistDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JsonToListTypeHandler extends BaseTypeHandler<List<BuylistDto>> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<BuylistDto> parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, mapper.writeValueAsString(parameter));
        } catch (Exception e) {
            throw new SQLException("Error converting List<BuylistDto> to String", e);
        }
    }

    @Override
    public List<BuylistDto> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public List<BuylistDto> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public List<BuylistDto> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private List<BuylistDto> parseJson(String json) {
        try {
            return mapper.readValue(json, new TypeReference<List<BuylistDto>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error converting String to List<BuylistDto>", e);
        }
    }
}
