package site.code4fun.model.dto;

import lombok.Data;

@Data
@SuppressWarnings("all")
public class VnPayResultDTO {
    private String vnp_TmnCode;     //Alphanumeric[8]	Bắt buộc	Mã website của merchant trên hệ thống của VNPAY. Ví dụ: 2QXUI4J4
    private long vnp_Amount;    //Numeric[1,12]	Bắt buộc	Số tiền thanh toán. VNPAY phản hồi số tiền nhân thêm 100 lần.
    private String vnp_BankCode; //	Alphanumeric[3,20]	Bắt buộc	Mã Ngân hàng thanh toán. Ví dụ: NCB
    private String vnp_BankTranNo;    //Alphanumeric[1,255]	Tùy chọn	Mã giao dịch tại Ngân hàng. Ví dụ: NCB20170829152730
    private String vnp_CardType;    //Alpha[2,20]	Tùy chọn	Loại tài khoản/thẻ khách hàng sử dụng:ATM,QRCODE
    private String vnp_PayDate;    //Numeric[14]	Tùy chọn	Thời gian thanh toán. Định dạng: yyyyMMddHHmmss
    private String vnp_OrderInfo;    //Alphanumeric[1,255]	Bắt buộc	Thông tin mô tả nội dung thanh toán (Tiếng Việt, không dấu). Ví dụ: **Nap tien cho thue bao 0123456789. So tien 100,000 VND**
    private long vnp_TransactionNo;    //Numeric[1,15]	Bắt buộc	Mã giao dịch ghi nhận tại hệ thống VNPAY. Ví dụ: 20170829153052
    private int vnp_ResponseCode;    //Numeric[2]	Bắt buộc	Mã phản hồi kết quả thanh toán. Quy định mã trả lời 00 ứng với kết quả Thành công cho tất cả các API. Tham khảo thêm tại bảng mã lỗi
    private int vnp_TransactionStatus;    //Numeric[2]	Bắt buộc	Mã phản hồi kết quả thanh toán. Tình trạng của giao dịch tại Cổng thanh toán VNPAY.
    private String vnp_TxnRef;    //Alphanumeric[1,100]	Bắt buộc	Giống mã gửi sang VNPAY khi gửi yêu cầu thanh toán. Ví dụ: 23554
    private String vnp_SecureHashType;    //Alphanumeric[3,10]	Tùy chọn	Loại mã băm sử dụng: SHA256, HmacSHA512
    private String vnp_SecureHash;    //Alphanumeric[32,256]	Bắt buộc	Mã kiểm tra (checksum) để đảm bảo dữ liệu của giao dịch không bị thay đổi trong quá trình chuyển từ VNPAY về Website TMĐT.
}
    /*
    vnp_ResponseCode VNPAY phản hồi qua IPN và Return URL:
00	Giao dịch thành công
07	Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).
09	Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.
10	Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần
11	Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.
12	Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.
13	Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.
24	Giao dịch không thành công do: Khách hàng hủy giao dịch
51	Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.
65	Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.
75	Ngân hàng thanh toán đang bảo trì.
79	Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch
99	Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)

Tra cứu giao dịch (vnp_Command=querydr)
02	Merchant không hợp lệ (kiểm tra lại vnp_TmnCode)
03	Dữ liệu gửi sang không đúng định dạng
91	Không tìm thấy giao dịch yêu cầu
94	Yêu cầu bị trùng lặp trong thời gian giới hạn của API (Giới hạn trong 5 phút)
97	Chữ ký không hợp lệ
99	Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)
Gửi yêu cầu hoàn trả (vnp_Command=refund)
02	Tổng số tiền hoản trả lớn hơn số tiền gốc
03	Dữ liệu gửi sang không đúng định dạng
04	Không cho phép hoàn trả toàn phần sau khi hoàn trả một phần
13	Chỉ cho phép hoàn trả một phần
91	Không tìm thấy giao dịch yêu cầu hoàn trả
93	Số tiền hoàn trả không hợp lệ. Số tiền hoàn trả phải nhỏ hơn hoặc bằng số tiền thanh toán.
94	Yêu cầu bị trùng lặp trong thời gian giới hạn của API (Giới hạn trong 5 phút)
95	Giao dịch này không thành công bên VNPAY. VNPAY từ chối xử lý yêu cầu.
97	Chữ ký không hợp lệ
98	Timeout Exception
99	Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)
    * */