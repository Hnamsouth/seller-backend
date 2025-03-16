package com.vtp.vipo.seller.financialstatement.export;

import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.common.utils.NumUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;

public class SuccessOrderPackageJRRsultSetDataSource  extends JRResultSetDataSource {
    public SuccessOrderPackageJRRsultSetDataSource(ResultSet rs) {
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
                    case "amount" -> {
                        if (ObjectUtils.isEmpty(originalValue))
                            yield originalValue;
                        yield NumUtils.formatBigDecimalToVNDFormat(new BigDecimal(originalValue.toString())) + " đ";
                    }
                    default -> originalValue;
                }
                ;

    }
}
