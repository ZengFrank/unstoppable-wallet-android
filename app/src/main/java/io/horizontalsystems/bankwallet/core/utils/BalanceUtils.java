package io.horizontalsystems.bankwallet.core.utils;

//import com.alphawallet.app.entity.tokens.Token;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class BalanceUtils {
    public final static int TOKEN_BALANCE_PRECISION = 4;
    private static String weiInEth  = "1000000000000000000";

    public static BigDecimal weiToEth(BigDecimal wei) {
        return Convert.fromWei(wei, Convert.Unit.ETHER);
    }

    public static String ethToUsd(String priceUsd, String ethBalance) {
        BigDecimal usd = new BigDecimal(ethBalance).multiply(new BigDecimal(priceUsd));
        usd = usd.setScale(2, RoundingMode.DOWN);
        return usd.toString();
    }

    public static String EthToWei(String eth) {
        BigDecimal wei = new BigDecimal(eth).multiply(new BigDecimal(weiInEth));
        return wei.toBigInteger().toString();
    }

    public static String UnitToEMultiplier(String value, BigDecimal decimalPlaces) {
        BigDecimal val = new BigDecimal(value).multiply(decimalPlaces);
        return val.toBigInteger().toString();
    }

    public static BigDecimal weiToGweiBI(BigInteger wei) {
        return Convert.fromWei(new BigDecimal(wei), Convert.Unit.GWEI);
    }

    public static String weiToGwei(BigInteger wei) {
        return Convert.fromWei(new BigDecimal(wei), Convert.Unit.GWEI).toPlainString();
    }

    public static BigInteger gweiToWei(BigDecimal gwei) {
        return Convert.toWei(gwei, Convert.Unit.GWEI).toBigInteger();
    }

    /**
     * Base - taken to mean default unit for a currency e.g. ETH, DOLLARS
     * Subunit - taken to mean subdivision of base e.g. WEI, CENTS
     *
     * @param baseAmountStr - decimal amonut in base unit of a given currency
     * @param decimals - decimal places used to convert to subunits
     * @return amount in subunits
     */
    public static BigInteger baseToSubunit(String baseAmountStr, int decimals) {
        assert(decimals >= 0);
        BigDecimal baseAmount = new BigDecimal(baseAmountStr);
        BigDecimal subunitAmount = baseAmount.multiply(BigDecimal.valueOf(10).pow(decimals));
        try {
            return subunitAmount.toBigIntegerExact();
        } catch (ArithmeticException ex) {
            assert(false);
            return subunitAmount.toBigInteger();
        }
    }

    /**
     * @param subunitAmount - amouunt in subunits
     * @param decimals - decimal places used to convert subunits to base
     * @return amount in base units
     */
    public static BigDecimal subunitToBase(BigInteger subunitAmount, int decimals) {
        assert(decimals >= 0);
        return new BigDecimal(subunitAmount).divide(BigDecimal.valueOf(10).pow(decimals));
    }

    public static boolean isDecimalValue(String value)
    {
        for (char ch : value.toCharArray()) if (!(Character.isDigit(ch) || ch == '.')) return false;
        return true;
    }

    public static String getScaledValueWithLimit(BigDecimal value, long decimals)
    {
        String pattern = "###,###,###,##0.00#######";
        return scaledValue(value, pattern, decimals);
    }

    public static String getScaledValueFixed(BigDecimal value, long decimals, int precision)
    {
        //form precision
        String pattern = "###,###,###,##0.";
        for (int i = 0; i < precision; i++) pattern += "0";
        return scaledValue(value, pattern, decimals);
    }

    public static String getScaledValue(BigDecimal value, long decimals, int precision)
    {
        //form precision
        String pattern = "###,###,###,##0.";
        for (int i = 0; i < precision; i++) pattern += "#";
        return scaledValue(value, pattern, decimals);
    }

    private static String scaledValue(BigDecimal value, String pattern, long decimals)
    {
        DecimalFormat df = new DecimalFormat(pattern);
        value = value.divide(new BigDecimal(Math.pow(10, decimals)), 18, RoundingMode.DOWN);
        df.setRoundingMode(RoundingMode.DOWN);
        return df.format(value);
    }

    /**
     * Default precision method
     *
     * @param valueStr
     * @param decimals
     * @return
     */
    public static String getScaledValue(String valueStr, long decimals)
    {
        return getScaledValue(valueStr, decimals, /*Token.*/TOKEN_BALANCE_PRECISION);
    }

    /**
     * Universal scaled value method
     * @param valueStr
     * @param decimals
     * @return
     */
    public static String getScaledValue(String valueStr, long decimals, int precision) {
        // Perform decimal conversion
        if (decimals > 1 && valueStr != null && valueStr.length() > 0 && Character.isDigit(valueStr.charAt(0)))
        {
            BigDecimal value = new BigDecimal(valueStr);
            return getScaledValue(value, decimals, precision); //represent balance transfers according to 'decimals' contract indicator property
        }
        else if (valueStr != null)
        {
            return valueStr;
        }
        else
        {
            return "0";
        }
    }

}
