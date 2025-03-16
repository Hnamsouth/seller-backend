package com.vtp.vipo.seller.financialstatement.export;

import com.vtp.vipo.seller.common.utils.DateUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class TransformingJRResultSetDataSource extends JRResultSetDataSource {

    public TransformingJRResultSetDataSource(ResultSet rs) {
        super(rs);
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        // Get the "raw" value from the ResultSet
        Object originalValue = super.getFieldValue(jrField);

        // Look at the field name from the JRXML
        String fieldName = jrField.getName();

        return
                switch (fieldName) {
                    case "time" -> {
                        if (ObjectUtils.isEmpty(originalValue))
                            yield originalValue;
                        Long timeInSeconds = Long.valueOf(originalValue.toString()) ;
                        yield DateUtils.toDateString(
                                DateUtils.convertEpochSecondsToLocalDateTime(timeInSeconds), DateUtils.ddMMyyyy
                        );
                    }
                    case "revenue" -> ObjectUtils.isNotEmpty(originalValue)? (BigDecimal) originalValue : null;
                    case "status" -> {
                        if (ObjectUtils.isEmpty(originalValue))
                            yield "Đơn thành công";
//                        Long allowedWithdrawalTimeInSeconds =  Long.valueOf(originalValue.toString());
//                        if (allowedWithdrawalTimeInSeconds > DateUtils.getCurrentTimeInSeconds())
                        yield "Đơn có thể rút";
//                        else
//                            yield "Đơn thành công";
                    }
                    default -> originalValue;
                }
        ;

    }
}
