package site.code4fun.constant;

import lombok.Getter;

@Getter
public enum ReportType {
    BalanceSheet("Chỉ tiêu cân đối kế toán"),
    CashFlow("Chỉ tiêu lưu chuyển tiền tệ"),
    ProfitAndLoss("Chỉ tiêu kết quả kinh doanh");

    private final String value;
    ReportType(String s) {
        this.value = s;
    }
}
