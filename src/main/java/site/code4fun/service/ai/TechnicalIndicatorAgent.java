package site.code4fun.service.ai;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.code4fun.model.mapper.StockMapper;
import site.code4fun.service.ai.dto.BollingerBandsValue;
import site.code4fun.service.ai.dto.PriceData;
import site.code4fun.service.ai.dto.TechnicalIndicatorDto;
import site.code4fun.service.integrations.VietCapService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicalIndicatorAgent {
    private final VietCapService vciRest;
    private final StockMapper stockMapper;
    private final Gson gson;
//    @SystemMessage("""
//        You are a specialized Technical Analyst AI. Your goal is to analyze historical stock price data.
//        Use the available tools to fetch price history.
//        Focus on identifying trends (uptrend, downtrend, sideways), support and resistance levels,
//        potential chart patterns (e.g., head and shoulders, double top/bottom), and key technical indicator signals
//        (like moving average crossovers, RSI levels, MACD signals - based on the data provided).
//        Provide a concise summary of your technical findings for the given stock ticker.
//        Do NOT provide investment advice. Only technical observations.
//        """)
//    String analyze(String ticker);


    public String analyze(String ticker, LocalDate toDate) {
        var now = toDate != null ? toDate : LocalDate.now();
        var res = vciRest.getHistoryPrice(ticker, now.minusDays(30), LocalDate.now() );

        List<PriceData> historicalData = stockMapper.mapToPriceDataList(res);

        if (historicalData.isEmpty()) {
            log.warn("Không có dữ liệu lịch sử để phân tích.");
            return null;
        }

        int smaPeriod = 10;
        int emaPeriod = 10;
        int rsiPeriod = 14;
        int bbandsPeriod = 20;
        double bbandsMultiplier = 2.0;
        int atrPeriod = 14;

        log.info("Calculating indicators for {} data points...", historicalData.size());

        List<Double> sma10 = calculateSMA(historicalData, smaPeriod);
        List<Double> ema10 = calculateEMA(historicalData, emaPeriod);
        List<Double> rsi14 = calculateRSI(historicalData, rsiPeriod);
        List<BollingerBandsValue> bbands = calculateBollingerBands(historicalData, bbandsPeriod, bbandsMultiplier);
        List<Long> obv = calculateOBV(historicalData);
        List<Double> atr14 = calculateATR(historicalData, atrPeriod);
        List<Double> prevWeekClose = calculatePreviousWeekClose(historicalData);
        List<Double> dailyReturn = calculateDailyReturn(historicalData);
        List<Double> weeklyReturn = calculateWeeklyReturn(historicalData);


        var i = historicalData.size() - 1;
        PriceData dayData = historicalData.get(i);

        BollingerBandsValue bb = (i < bbands.size()) ? bbands.get(i) : BollingerBandsValue.nan();
        Long obvVal = (i < obv.size()) ? obv.get(i) : null;
        double smaVal = (i < sma10.size()) ? sma10.get(i) : 0;
        double emaVal = (i < ema10.size()) ? ema10.get(i) : 0;
        double rsiVal = (i < rsi14.size()) ? rsi14.get(i) : 0;
        double atrVal = (i < atr14.size()) ? atr14.get(i) : 0;
        double lagVal = (i < prevWeekClose.size()) ? prevWeekClose.get(i) : 0;
        double dRet = (i < dailyReturn.size()) ? dailyReturn.get(i) : 0;
        double wRet = (i < weeklyReturn.size()) ? weeklyReturn.get(i) : 0;

        var dto = TechnicalIndicatorDto.builder()
                .closeValue(dayData.close())
                .dailyReturn(dRet)
                .weeklyReturn(wRet)
                .onBalanceVol(obvVal)
                .simpMovingAvg(smaVal)
                .exponentialMovingAvg(emaVal)
                .previousWeekClose(lagVal)
                .atr14(atrVal)
                .bollingerBands(bb.toString())
                .rsi14(rsiVal)
                .build();
        return gson.toJson(dto);
    }



    /**
     * Tính Đường trung bình động đơn giản (Simple Moving Average - SMA).
     */
    public static List<Double> calculateSMA(List<PriceData> priceDataList, int period) {
        if (period <= 0 || priceDataList == null || priceDataList.size() < period) {
            return priceDataList == null ? Collections.emptyList() :
                    Collections.nCopies(priceDataList.size(), 0d);
        }

        List<Double> smaValues = new ArrayList<>(Collections.nCopies(priceDataList.size(), 0d));
        double sum = 0.0;

        for (int i = 0; i < period; i++) {
            sum += priceDataList.get(i).close();
        }
        smaValues.set(period - 1, sum / period);

        for (int i = period; i < priceDataList.size(); i++) {
            sum -= priceDataList.get(i - period).close();
            sum += priceDataList.get(i).close();
            smaValues.set(i, sum / period);
        }

        return smaValues;
    }

    /**
     * Tính Đường trung bình động hàm mũ (Exponential Moving Average - EMA).
     */
    public static List<Double> calculateEMA(List<PriceData> priceDataList, int period) {
        if (period <= 0 || priceDataList == null || priceDataList.size() < period) {
            return priceDataList == null ? Collections.emptyList() :
                    Collections.nCopies(priceDataList.size(), 0d);
        }

        List<Double> emaValues = new ArrayList<>(Collections.nCopies(priceDataList.size(), 0d));
        double multiplier = 2.0 / (period + 1.0);

        double initialSum = 0.0;
        for (int i = 0; i < period; i++) {
            initialSum += priceDataList.get(i).close();
        }
        double previousEma = initialSum / period;
        emaValues.set(period - 1, previousEma);

        for (int i = period; i < priceDataList.size(); i++) {
            double currentClose = priceDataList.get(i).close();
            double currentEma = (currentClose - previousEma) * multiplier + previousEma;
            emaValues.set(i, currentEma);
            previousEma = currentEma;
        }

        return emaValues;
    }

    /**
     * Tính Chỉ số sức mạnh tương đối (Relative Strength Index - RSI).
     */
    public static List<Double> calculateRSI(List<PriceData> priceDataList, int period) {
        if (period <= 0 || priceDataList == null || priceDataList.size() < period + 1) {
            return priceDataList == null ? Collections.emptyList() :
                    Collections.nCopies(priceDataList.size(), 0d);
        }

        List<Double> rsiValues = new ArrayList<>(Collections.nCopies(priceDataList.size(), 0d));
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        for (int i = 1; i < priceDataList.size(); i++) {
            double change = priceDataList.get(i).close() - priceDataList.get(i - 1).close();
            gains.add(Math.max(0, change));  // Gain là thay đổi dương hoặc 0
            losses.add(Math.max(0, -change)); // Loss là thay đổi âm (lấy giá trị dương) hoặc 0
        }

        double avgGain = 0.0;
        double avgLoss = 0.0;
        for (int i = 0; i < period; i++) {
            avgGain += gains.get(i);
            avgLoss += losses.get(i);
        }
        avgGain /= period;
        avgLoss /= period;

        double rs = (avgLoss == 0) ? Double.POSITIVE_INFINITY : avgGain / avgLoss; // Tránh chia cho 0
        rsiValues.set(period, 100.0 - (100.0 / (1.0 + rs))); // RSI được tính tại cuối kỳ period+1 (cần period thay đổi)

        for (int i = period; i < gains.size(); i++) {
            double currentGain = gains.get(i);
            double currentLoss = losses.get(i);

            avgGain = ((avgGain * (period - 1)) + currentGain) / period;
            avgLoss = ((avgLoss * (period - 1)) + currentLoss) / period;

            rs = (avgLoss == 0) ? Double.POSITIVE_INFINITY : avgGain / avgLoss;
            double currentRsi = 100.0 - (100.0 / (1.0 + rs));
            rsiValues.set(i + 1, currentRsi);
        }

        return rsiValues;
    }

    /**
     * Tính Độ lệch chuẩn trong một cửa sổ trượt.
     */
    private static List<Double> calculateStandardDeviation(List<Double> values, int period, List<Double> means) {
        int n = values.size();
        if (period <= 0 || n < period || means.size() != n) {
            return Collections.nCopies(n, 0d);
        }

        List<Double> stdDevs = new ArrayList<>(Collections.nCopies(n, 0d));

        for (int i = period - 1; i < n; i++) {
            if (Double.isNaN(means.get(i))) {
                stdDevs.set(i, 0d);
                continue;
            }
            double mean = means.get(i);
            double sumOfSquares = 0.0;
            for (int j = 0; j < period; j++) {
                sumOfSquares += Math.pow(values.get(i - period + 1 + j) - mean, 2);
            }
            double variance = sumOfSquares / period;
            stdDevs.set(i, Math.sqrt(variance));
        }
        return stdDevs;
    }

    /**
     * Tính toán Bollinger Bands.
     */
    public static List<BollingerBandsValue> calculateBollingerBands(List<PriceData> priceDataList, int period, double multiplier) {
        int n = priceDataList.size();
        if (period <= 0 || n < period || multiplier <= 0) {
            return Collections.nCopies(n, BollingerBandsValue.nan());
        }

        List<Double> closes = priceDataList.stream().map(PriceData::close).collect(Collectors.toList());
        List<Double> middleBand = calculateSMA(priceDataList, period);
        List<Double> stdDevs = calculateStandardDeviation(closes, period, middleBand);

        List<BollingerBandsValue> bbValues = new ArrayList<>(n);
        for(int i = 0; i < n; i++) {
            double middle = middleBand.get(i);
            double stdDev = stdDevs.get(i);

            if (Double.isNaN(middle) || Double.isNaN(stdDev)) {
                bbValues.add(BollingerBandsValue.nan());
            } else {
                double upper = middle + (stdDev * multiplier);
                double lower = middle - (stdDev * multiplier);
                bbValues.add(new BollingerBandsValue(middle, upper, lower));
            }
        }
        return bbValues;
    }

    /**
     * Tính toán On-Balance Volume (OBV).
     */
    public static List<Long> calculateOBV(List<PriceData> priceDataList) {
        int n = priceDataList.size();
        if (n == 0) {
            return Collections.emptyList();
        }

        List<Long> obvValues = new ArrayList<>(Collections.nCopies(n, 0L)); // Khởi tạo với 0

        if (n == 1) {
            obvValues.set(0, 0L); // OBV đầu tiên thường là 0 hoặc volume ngày đầu, 0 đơn giản hơn
            return obvValues;
        }

        obvValues.set(0, 0L);

        for (int i = 1; i < n; i++) {
            PriceData current = priceDataList.get(i);
            PriceData previous = priceDataList.get(i - 1);
            long currentVolume = current.volume(); // Lấy volume từ PriceData
            long previousOBV = obvValues.get(i - 1);

            if (current.close() > previous.close()) {
                obvValues.set(i, previousOBV + currentVolume); // Giá tăng, cộng volume
            } else if (current.close() < previous.close()) {
                obvValues.set(i, previousOBV - currentVolume); // Giá giảm, trừ volume
            } else {
                obvValues.set(i, previousOBV);
            }
        }
        return obvValues;
    }

    /**
     * Tính toán True Range (TR) cho mỗi ngày.
     * TR = max(High - Low, abs(High - Previous Close), abs(Low - Previous Close))
     */
    private static List<Double> calculateTrueRange(List<PriceData> priceDataList) {
        int n = priceDataList.size();
        if (n < 2) {
            return Collections.nCopies(n, 0d);
        }

        List<Double> trValues = new ArrayList<>(Collections.nCopies(n, 0d));

        for (int i = 1; i < n; i++) {
            PriceData current = priceDataList.get(i);
            PriceData previous = priceDataList.get(i - 1);

            double highLow = current.high() - current.low();
            double highPrevClose = Math.abs(current.high() - previous.close());
            double lowPrevClose = Math.abs(current.low() - previous.close());

            trValues.set(i, Math.max(highLow, Math.max(highPrevClose, lowPrevClose)));
        }
        return trValues;
    }

    /**
     * Tính toán Average True Range (ATR) sử dụng Wilder's Smoothing.
     */
    public static List<Double> calculateATR(List<PriceData> priceDataList, int period) {
        int n = priceDataList.size();
        if (period <= 0 || n < period + 1) { // Cần ít nhất period+1 ngày để có period TRs
            return Collections.nCopies(n, 0d);
        }

        List<Double> trValues = calculateTrueRange(priceDataList);
        List<Double> atrValues = new ArrayList<>(Collections.nCopies(n, 0d));

        double initialAtrSum = 0.0;
        for (int i = 1; i <= period; i++) {
            if(Double.isNaN(trValues.get(i))) {
                return Collections.nCopies(n, 0d);
            }
            initialAtrSum += trValues.get(i);
        }
        double currentAtr = initialAtrSum / period;
        atrValues.set(period, currentAtr);

        for (int i = period + 1; i < n; i++) {
            if(Double.isNaN(trValues.get(i))) {
                atrValues.set(i, 0d);
                currentAtr = 0;
                continue;
            }
            if(Double.isNaN(currentAtr)){
                atrValues.set(i, 0d);
                continue;
            }
            currentAtr = ((currentAtr * (period - 1)) + trValues.get(i)) / period;
            atrValues.set(i, currentAtr);
        }

        return atrValues;
    }

    /**
     * Tạo đặc trưng trễ cho giá đóng cửa.
     */
    public static List<Double> calculateLagFeature(List<PriceData> priceDataList, int lagPeriod) {
        int n = priceDataList.size();
        if (lagPeriod <= 0 || n < lagPeriod) {
            return Collections.nCopies(n, 0d);
        }

        List<Double> lagValues = new ArrayList<>(Collections.nCopies(n, 0d));

        for (int i = lagPeriod; i < n; i++) {
            lagValues.set(i, priceDataList.get(i - lagPeriod).close());
        }
        return lagValues;
    }

    public static List<Double> calculatePreviousWeekClose(List<PriceData> priceDataList) {
        // Giả sử 1 tuần là 5 ngày giao dịch
        return calculateLagFeature(priceDataList, 5);
    }

    /**
     * Tính toán tỷ suất lợi nhuận so với 'period' ngày trước.
     */
    public static List<Double> calculateReturn(List<PriceData> priceDataList, int period) {
        int n = priceDataList.size();
        if (period <= 0 || n < period + 1) {
            return Collections.nCopies(n, 0d);
        }

        List<Double> returnValues = new ArrayList<>(Collections.nCopies(n, 0d));

        for (int i = period; i < n; i++) {
            double currentClose = priceDataList.get(i).close();
            double previousClose = priceDataList.get(i - period).close();

            if (previousClose == 0) {
                returnValues.set(i, 0d); // Tránh chia cho 0
            } else {
                returnValues.set(i, (currentClose - previousClose) / previousClose);
            }
        }
        return returnValues;
    }

    public static List<Double> calculateDailyReturn(List<PriceData> priceDataList) {
        return calculateReturn(priceDataList, 1);
    }

    public static List<Double> calculateWeeklyReturn(List<PriceData> priceDataList) {
        // Giả sử 1 tuần là 5 ngày giao dịch
        return calculateReturn(priceDataList, 5);
    }

}

